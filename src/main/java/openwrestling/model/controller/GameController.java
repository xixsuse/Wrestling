package openwrestling.model.controller;

import lombok.Getter;
import openwrestling.Logging;
import openwrestling.manager.BankAccountManager;
import openwrestling.manager.BroadcastTeamManager;
import openwrestling.manager.ContractManager;
import openwrestling.manager.DateManager;
import openwrestling.manager.EntourageManager;
import openwrestling.manager.EventManager;
import openwrestling.manager.GameObjectManager;
import openwrestling.manager.InjuryManager;
import openwrestling.manager.NewsManager;
import openwrestling.manager.PromotionManager;
import openwrestling.manager.RelationshipManager;
import openwrestling.manager.RosterSplitManager;
import openwrestling.manager.SegmentManager;
import openwrestling.manager.StableManager;
import openwrestling.manager.StaffManager;
import openwrestling.manager.TagTeamManager;
import openwrestling.manager.TitleManager;
import openwrestling.manager.WorkerManager;
import openwrestling.model.factory.ContractFactory;
import openwrestling.model.factory.EventFactory;
import openwrestling.model.factory.MatchFactory;
import openwrestling.model.factory.RandomGameAssetGenerator;
import openwrestling.model.gameObjects.Event;
import openwrestling.model.gameObjects.EventTemplate;
import openwrestling.model.gameObjects.Promotion;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static openwrestling.model.factory.EventFactory.bookEventsForNewEventTemplate;

@Getter
public final class GameController extends Logging implements Serializable {

    private final ContractFactory contractFactory;
    private final EventFactory eventFactory;
    private final MatchFactory matchFactory;

    private final DateManager dateManager;
    private final ContractManager contractManager;
    private final EventManager eventManager;
    private final TitleManager titleManager;
    private final WorkerManager workerManager;
    private final PromotionManager promotionManager;
    private final TagTeamManager tagTeamManager;
    private final SegmentManager segmentManager;
    private final InjuryManager injuryManager;
    private final NewsManager newsManager;
    private final StaffManager staffManager;
    private final StableManager stableManager;
    private final RelationshipManager relationshipManager;
    private final BankAccountManager bankAccountManager;
    private final RosterSplitManager rosterSplitManager;
    private final EntourageManager entourageManager;
    private final NextDayController nextDayController;
    private final BroadcastTeamManager broadcastTeamManager;

    private List<GameObjectManager> managers;

    private final PromotionController promotionController;

    private final int EVENT_MONTHS = 6;

    public GameController(boolean randomGame) throws IOException {
        //set the initial date here
        dateManager = new DateManager(LocalDate.of(2015, 1, 5));


        bankAccountManager = new BankAccountManager();
        promotionManager = new PromotionManager(bankAccountManager);

        newsManager = new NewsManager();

        stableManager = new StableManager();
        relationshipManager = new RelationshipManager();
        rosterSplitManager = new RosterSplitManager();
        broadcastTeamManager = new BroadcastTeamManager();

        contractManager = new ContractManager(promotionManager,
                newsManager,
                relationshipManager,
                bankAccountManager);

        workerManager = new WorkerManager(contractManager);
        staffManager = new StaffManager(contractManager);
        titleManager = new TitleManager(dateManager, workerManager);

        entourageManager = new EntourageManager(workerManager);
        tagTeamManager = new TagTeamManager(workerManager);
        segmentManager = new SegmentManager(dateManager, tagTeamManager, stableManager);
        injuryManager = new InjuryManager(newsManager, workerManager, dateManager);
        contractFactory = new ContractFactory(contractManager);

        eventManager = new EventManager(
                contractManager,
                dateManager,
                segmentManager);

        matchFactory = new MatchFactory(segmentManager, dateManager, injuryManager, workerManager, staffManager);


        eventFactory = new EventFactory(
                contractManager,
                eventManager,
                matchFactory,
                promotionManager,
                titleManager,
                workerManager,
                tagTeamManager,
                stableManager,
                relationshipManager,
                newsManager,
                bankAccountManager,
                segmentManager);


        promotionController = new PromotionController(
                contractFactory,
                eventFactory,
                matchFactory,
                contractManager,
                dateManager,
                eventManager,
                titleManager,
                workerManager,
                newsManager,
                staffManager);

        nextDayController = NextDayController.builder()
                .promotionController(promotionController)
                .dateManager(dateManager)
                .eventManager(eventManager)
                .workerManager(workerManager)
                .promotionManager(promotionManager)
                .relationshipManager(relationshipManager)
                .bankAccountManager(bankAccountManager)
                .injuryManager(injuryManager)
                .newsManager(newsManager)
                .contractManager(contractManager)
                .build();

        if (randomGame) {
            RandomGameAssetGenerator randomGameAssetGenerator = new RandomGameAssetGenerator(
                    contractFactory,
                    dateManager,
                    promotionManager,
                    workerManager,
                    staffManager,
                    bankAccountManager,
                    contractManager);
            randomGameAssetGenerator.preparePromotions();
        }

        managers = List.of(
                bankAccountManager,
                broadcastTeamManager,
                contractManager,
                dateManager,
                entourageManager,
                eventManager,
                injuryManager,
                newsManager,
                promotionManager,
                relationshipManager,
                rosterSplitManager,
                segmentManager,
                stableManager,
                staffManager,
                tagTeamManager,
                titleManager,
                workerManager);

    }

    public void initializeGameData() {
        List<EventTemplate> generatedEventTemplates = promotionManager.getPromotions().stream()
                .filter(promotion -> eventManager.getEventTemplates(promotion).isEmpty())
                .flatMap(promotion -> EventFactory.generateMonthlyEventTemplates(promotion, dateManager.today()).stream())
                .collect(Collectors.toList());
        eventManager.createEventTemplates(generatedEventTemplates);

        List<Event> initialEvents = eventManager.getEventTemplates().stream()
                .flatMap(eventTemplate -> bookEventsForNewEventTemplate(eventTemplate).stream())
                .collect(Collectors.toList());

        List<EventTemplate> updatedBookedUntilDates = initialEvents.stream()
                .map(Event::getEventTemplate)
                .collect(Collectors.toList());

        eventManager.updateEventTemplates(updatedBookedUntilDates);
        eventManager.createEvents(initialEvents);
        newsManager.addWelcomeNewsItem(promotionManager.getPlayerPromotion());
    }

    public void loadGameDataFromDatabase() {
        managers.forEach(GameObjectManager::selectData);
    }

    //only called by MainApp
    public void nextDay() {
        long start = System.currentTimeMillis();
        logger.log(Level.DEBUG, "nextDay start");
        contractManager.dailyUpdate(dateManager.today());
        logger.log(Level.DEBUG, "promotion loop");
        for (Promotion promotion : promotionManager.getPromotions()) {
            injuryManager.dailyUpdate(dateManager.today(), promotion);
            promotionController.trainerUpdate(promotion);
            if (dateManager.isPayDay()) {
                promotionController.payDay(promotion, dateManager.today());
            }
            if (!promotionManager.getPlayerPromotion().equals(promotion)) {
                promotionController.dailyUpdate(promotion);
            }
        }
        logger.log(Level.DEBUG, String.format("nextDay before controller %d", System.currentTimeMillis() - start));
        nextDayController.nextDay();

        dateManager.nextDay();
        logger.log(Level.DEBUG, String.format("nextDay end total time %d", System.currentTimeMillis() - start));
    }


}
