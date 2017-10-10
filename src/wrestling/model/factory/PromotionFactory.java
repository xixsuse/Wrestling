package wrestling.model.factory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import wrestling.model.GameController;
import wrestling.model.Promotion;
import wrestling.model.Worker;
import wrestling.model.utility.ModelUtilityFunctions;


/*
for generating promotions in a random game
 */
public class PromotionFactory {
    

    public void preparePromotions(GameController gameController) throws IOException {

        List<Promotion> promotions = new ArrayList<>();
        List<Worker> allWorkers = new ArrayList<>();

        int numberOfPromotions = 20;
        int startingFunds = 10000;
        double[] levelRatios = {0.3, 0.2, 0.2, 0.2, 0.1};

        for (int i = 0; i < levelRatios.length; i++) {

            double target = numberOfPromotions * levelRatios[i];
            double currentPromotions = 0;

            //levels are 1 to 5
            int currentLevel = i + 1;

            List<Promotion> currentLevelPromotions = new ArrayList<>();

            while (currentPromotions < target) {

                Promotion newPromotion = new Promotion();

                newPromotion.setLevel(currentLevel);

                currentLevelPromotions.add(newPromotion);

                currentPromotions++;

            }

            int rosterSize = 10 + (currentLevel * 10);

            for (Promotion promotion : currentLevelPromotions) {

                //add funds (this could be based on promotion level)
                promotion.bankAccount().addFunds(startingFunds * promotion.getLevel());

                //assign workers based on promotion level
                do {

                    Worker worker = WorkerFactory.randomWorker(ModelUtilityFunctions.randRange(promotion.getLevel() - 1, promotion.getLevel() + 1));

                    gameController.getContractFactory().createContract(worker, promotion, gameController.date());

                } while (gameController.getFullRoster(promotion).size() < rosterSize);

                allWorkers.addAll(gameController.getFullRoster(promotion));
            }

            //add all the workers and promotions we have generated for this
            //level to the main lists
            promotions.addAll(currentLevelPromotions);

        }

        gameController.setPromotions(promotions);
        gameController.setWorkers(allWorkers);

    }

    private final GameController gc;

    public PromotionFactory(GameController gc) {
        this.gc = gc;
    }

}
