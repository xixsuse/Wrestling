package wrestling.model;

import java.io.IOException;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import wrestling.model.factory.ContractFactory;
import wrestling.model.factory.EventFactory;
import wrestling.model.factory.PromotionFactory;
import wrestling.model.factory.TitleFactory;

/**
 *
 * game controller handles game stuff
 */
public final class GameController implements Serializable {

    /*
    returns a random worker from a list of workers
     */
    public static Worker getRandomFromList(List<Worker> list) {
        Random randomizer = new Random();

        return list.get(randomizer.nextInt(list.size()));
    }

    private LocalDate gameDate;
    private LocalDate payDay;
    private Promotion playerPromotion;
    private List<Promotion> promotions = new ArrayList<>();
    private List<PromotionAi> promotionAis = new ArrayList<>();
    private List<Worker> workers = new ArrayList<>();

    private final DirtSheet dirtSheet;
    private final ContractFactory contractFactory;
    private final EventFactory eventFactory;
    private final PromotionFactory promotionFactory;
    private final TitleFactory titleFactory;

    public GameController() throws IOException {

        dirtSheet = new DirtSheet();
        contractFactory = new ContractFactory(this);
        eventFactory = new EventFactory(this);
        promotionFactory = new PromotionFactory(this);
        titleFactory = new TitleFactory(this);

        //set the initial date here
        gameDate = LocalDate.of(2015, 1, 1);

        payDay = gameDate.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));

        //eventFactory = new EventFactory(this);
        promotionFactory.preparePromotions(this);

    }

    public void newDirt(String string) {
        dirtSheet.newDirt(string, gameDate);
    }

    public void newDirt(String string, List<Worker> workers, Promotion promotion, EventArchive eventArchive) {
        newDirt(string, workers, Arrays.asList(promotion), eventArchive);
    }

    public void newDirt(String string, List<Worker> workers, List<Promotion> promotions, EventArchive eventArchive) {
        Dirt dirt = new Dirt(string, gameDate, workers, promotions, eventArchive);
        dirtSheet.newDirt(dirt);
    }

    //is it payday?
    public boolean isPayDay() {
        boolean isPayDay = false;

        if (gameDate.isEqual(payDay)) {

            isPayDay = true;
        }

        return isPayDay;
    }

    //only called by MainApp
    public void nextDay() {

        //iterate through all promotions
        for (PromotionAi pAi : promotionAis) {

            pAi.dailyUpdate();

        }

        //if it is payday, advance next payday to two fridays in the future
        if (isPayDay()) {
            payDay = payDay.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
            payDay = payDay.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));

        }

        //advance the day by one
        gameDate = LocalDate.from(gameDate).plusDays(1);

    }

    public LocalDate date() {
        return gameDate;
    }

    public void setPlayerPromotion(Promotion promotion) {
        playerPromotion = promotion;
        //set the Ai for non-player promotions
        setAi();
    }

    public Promotion playerPromotion() {
        return playerPromotion;
    }

    public List<Worker> allWorkers() {
        return workers;
    }

    public List<Worker> freeAgents(Promotion promotion) {

        List<Worker> freeAgents = new ArrayList<>();
        for (Worker worker : workers) {

            if (worker.canNegotiate(promotion)) {
                freeAgents.add(worker);
            }
        }

        return freeAgents;
    }

    public void setPromotions(List<Promotion> promotions) {
        this.promotions = promotions;

        for (Promotion promotion : promotions) {
            promotion.bankAccount().addFunds(10000);
        }
    }

    public void setWorkers(List<Worker> workers) {
        this.workers = workers;
    }

    private void setAi() {
        //add ai where necessary
        for (Promotion promotion : getPromotions()) {
            if (!promotion.equals(playerPromotion)) {
                promotionAis.add(new PromotionAi(promotion, this));
            }

        }
    }

    /**
     * @return the promotions
     */
    public List<Promotion> getPromotions() {
        return promotions;
    }

    /**
     * @return the contractFactory
     */
    public ContractFactory getContractFactory() {
        return contractFactory;
    }

    /**
     * @return the eventFactory
     */
    public EventFactory getEventFactory() {
        return eventFactory;
    }

    /**
     * @return the titleFactory
     */
    public TitleFactory getTitleFactory() {
        return titleFactory;
    }

}
