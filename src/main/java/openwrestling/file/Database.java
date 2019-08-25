package openwrestling.file;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.builtin.PassThroughConverter;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import openwrestling.entities.ContractEntity;
import openwrestling.entities.Entity;
import openwrestling.entities.PromotionEntity;
import openwrestling.entities.RosterSplitEntity;
import openwrestling.entities.RosterSplitWorkerEntity;
import openwrestling.entities.StableEntity;
import openwrestling.entities.StableWorkerEntity;
import openwrestling.entities.WorkerEntity;
import openwrestling.model.gameObjects.Contract;
import openwrestling.model.gameObjects.GameObject;
import openwrestling.model.gameObjects.Promotion;
import openwrestling.model.gameObjects.RosterSplit;
import openwrestling.model.gameObjects.Stable;
import openwrestling.model.gameObjects.Worker;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class Database {

    private static String dbUrl;

    public static Map<Class<? extends GameObject>, Class<? extends Entity>> daoClassMap = new HashMap<>() {{
        put(Promotion.class, PromotionEntity.class);
        put(Worker.class, WorkerEntity.class);
        put(Stable.class, StableEntity.class);
        put(RosterSplit.class, RosterSplitEntity.class);
        put(Contract.class, ContractEntity.class);
    }};

    public static String createNewDatabase(String fileName) {

        String url = "jdbc:sqlite:C:/temp/" + fileName + ".db";

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created." + url);
                dbUrl = url;
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        createTables(url);

        return url;
    }


    public static Connection connect(String url) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
        return connection;
    }

    public static void insertEntityList(List<? extends Entity> toInsert, ConnectionSource connectionSource) {
        if (toInsert.isEmpty()) {
            return;
        }

        try {
            Dao dao = DaoManager.createDao(connectionSource, toInsert.get(0).getClass());

            dao.callBatchTasks((Callable<Void>) () -> {
                for (Entity entity : toInsert) {
                    dao.create(entity);
                }
                return null;
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List selectAll(Class sourceClass) {
        try {
            Class<? extends Entity> targetClass = daoClassMap.get(sourceClass);
            ConnectionSource connectionSource = new JdbcConnectionSource(dbUrl);
            Dao dao = DaoManager.createDao(connectionSource, targetClass);
            MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

            mapperFactory.getConverterFactory().registerConverter(new PassThroughConverter(LocalDate.class));
            MapperFacade mapper = mapperFactory.getMapperFacade();
            List entites = dao.queryForAll();
            List targets = new ArrayList();

            entites.stream().forEach(entity -> targets.add(mapper.map(entity, sourceClass)));
            return targets;
        } catch (Exception e) {

        }
        return List.of();
    }

    public static <T extends GameObject> T insertGameObject(GameObject gameObject) {
        return (T) insertList(List.of(gameObject)).get(0);
    }

    public static List<? extends GameObject> insertList(List<? extends GameObject> gameObjects) {
        if (gameObjects.isEmpty()) {
            return gameObjects;
        }

        try {
            ConnectionSource connectionSource = new JdbcConnectionSource(dbUrl);

            Class<? extends Entity> targetClass = daoClassMap.get(gameObjects.get(0).getClass());
            Class sourceClass = gameObjects.get(0).getClass();

            Dao dao = DaoManager.createDao(connectionSource, targetClass);
            MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
            mapperFactory.getConverterFactory().registerConverter(new PassThroughConverter(LocalDate.class));
            BoundMapperFacade mapper2 = mapperFactory.getMapperFacade(gameObjects.get(0).getClass(), targetClass);
            List toInsert = gameObjects.stream().map(gameObject -> {
                Object entity = null;
                try {
                    entity = targetClass.getConstructor().newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (entity == null) {
                    return null;
                }

                for (Field field : entity.getClass().getDeclaredFields()) {
                    if (field.isAnnotationPresent(ForeignCollectionField.class)) {
                        try {

                            field.set(entity, dao.getEmptyForeignCollection(field.getName()));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
                Object result = mapper2.map(gameObject, entity);

                return result;
            })
                    .collect(Collectors.toList());

            MapperFacade mapper = mapperFactory.getMapperFacade();
            List toReturn = new ArrayList();
            dao.callBatchTasks((Callable<Void>) () -> {
                for (Object object : toInsert) {
                    Entity entity = (Entity) object;
                    dao.create(entity);
                    insertEntityList(entity.childrenToInsert(), connectionSource);
                    toReturn.add(mapper.map(entity, sourceClass));
                }
                return null;
            });

            return toReturn;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static void createTables(String url) {
        try {

            ConnectionSource connectionSource = new JdbcConnectionSource(url);

            List<Class> classes = List.of(
                    WorkerEntity.class,
                    PromotionEntity.class,
                    StableEntity.class,
                    StableWorkerEntity.class,
                    RosterSplitEntity.class,
                    RosterSplitWorkerEntity.class,
                    ContractEntity.class);

            for (Class entityClass : classes) {
                Dao dao = DaoManager.createDao(connectionSource, entityClass);
                TableUtils.dropTable(dao, true);
                TableUtils.createTable(connectionSource, entityClass);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
