package wrestling.model;

import java.io.Serializable;
import java.util.List;
import wrestling.model.controller.WorkerController;

public class Worker implements Serializable {

    private static int serialNumber = 0;

    /**
     * @return the serialNumber
     */
    public static int getSerialNumber() {
        return serialNumber;
    }

    /**
     * @param aSerialNumber the serialNumber to set
     */
    public static void setSerialNumber(int aSerialNumber) {
        serialNumber = aSerialNumber;
    }

    private String name;
    private String shortName;
    private String imageString;

    private int striking;
    private int flying;
    private int wrestling;
    private int charisma;
    private int behaviour;
    private int popularity;

    private boolean manager;
    private boolean fullTime;
    private boolean mainRoster;

    private int minimumPopularity;

    private WorkerController controller;

    public Worker() {
        minimumPopularity = 0;

        name = "Worker #" + serialNumber;
        serialNumber++;

    }

    @Override
    public String toString() {
        return this.getName();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the striking
     */
    public int getStriking() {
        return striking;
    }

    /**
     * @param striking the striking to set
     */
    public void setStriking(int striking) {
        this.striking = striking;
    }

    /**
     * @return the flying
     */
    public int getFlying() {
        return flying;
    }

    /**
     * @param flying the flying to set
     */
    public void setFlying(int flying) {
        this.flying = flying;
    }

    /**
     * @return the wrestling
     */
    public int getWrestling() {
        return wrestling;
    }

    /**
     * @param wrestling the wrestling to set
     */
    public void setWrestling(int wrestling) {
        this.wrestling = wrestling;
    }

    /**
     * @return the look
     */
    public int getCharisma() {
        return charisma;
    }

    /**
     * @param charisma the look to set
     */
    public void setCharisma(int charisma) {
        this.charisma = charisma;
    }

    /**
     * @return the reputation
     */
    public int getBehaviour() {
        return behaviour;
    }

    /**
     * @param behaviour the reputation to set
     */
    public void setBehaviour(int behaviour) {
        this.behaviour = behaviour;
    }

    /**
     * @return the popularity
     */
    public int getPopularity() {
        return popularity;
    }

    /**
     * @param popularity the popularity to set
     */
    public void setPopularity(int popularity) {
        this.popularity = popularity;
        //once workers reach a level of popularity, they can never  drop below 50% of that
        if ((popularity / 2) > getMinimumPopularity()) {
            setMinimumPopularity(popularity / 2);
        }
    }

    /**
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @param shortName the shortName to set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * @return the manager
     */
    public boolean isManager() {
        return manager;
    }

    /**
     * @param manager the manager to set
     */
    public void setManager(boolean manager) {
        this.manager = manager;
    }

    /**
     * @return the fullTime
     */
    public boolean isFullTime() {
        return fullTime;
    }

    /**
     * @param fullTime the fullTime to set
     */
    public void setFullTime(boolean fullTime) {
        this.fullTime = fullTime;
    }

    /**
     * @return the mainRoster
     */
    public boolean isMainRoster() {
        return mainRoster;
    }

    /**
     * @param mainRoster the mainRoster to set
     */
    public void setMainRoster(boolean mainRoster) {
        this.mainRoster = mainRoster;
    }

    /**
     * @return the imageString
     */
    public String getImageString() {
        return imageString;
    }

    /**
     * @param imageString the imageString to set
     */
    public void setImageString(String imageString) {
        this.imageString = imageString;
    }

    /**
     * @return the minimumPopularity
     */
    public int getMinimumPopularity() {
        return minimumPopularity;
    }

    /**
     * @param minimumPopularity the minimumPopularity to set
     */
    public void setMinimumPopularity(int minimumPopularity) {
        this.minimumPopularity = minimumPopularity;
    }

    /**
     * @return the workerController
     */
    public WorkerController getController() {
        return controller;
    }

    /**
     * @param controller the workerController to set
     */
    public void setController(WorkerController controller) {
        this.controller = controller;
    }

}
