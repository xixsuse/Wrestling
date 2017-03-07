package wrestling.model.factory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import wrestling.model.Contract;
import wrestling.model.Promotion;
import wrestling.model.Worker;

/**
 * attached to the gameController, it is called whenever a new contract is to be
 * created
 *
 */
public final class ContractFactory {

    //create a contract with predetermined attributes
    public static void createContract(Worker worker, Promotion promotion, boolean monthly, boolean exclusive, int duration, int cost, LocalDate startDate) {
        //create the contract
        Contract contract = new Contract();

        contract.setWorker(worker);
        contract.setPromotion(promotion);

        contract.setDuration(duration);
        contract.setAppearanceCost(cost);
        contract.setStartDate(startDate);

        //assign the contract
        promotion.addContract(contract);
        worker.addContract(contract);
    }

    //create a default contract
    public static void createContract(Worker worker, Promotion promotion) {

        //create the contract
        Contract contract = new Contract();

        contract.setWorker(worker);
        contract.setPromotion(promotion);

        //exclusive contracts are default for top level promotions
        if (promotion.getLevel() == 5) {

            contract.setExclusive(true);

            //'buy out' any the other contracts the worker has
            for (Contract c : worker.getContracts()) {

                if (!c.getPromotion().equals(promotion)) {

                    c.buyOutContract();
                }
            }
        } else {
            contract.setExclusive(false);
        }

        int duration = 30;

        //scale the duration and exclusivity based on promotion level
        for (int i = 0; i < promotion.getLevel(); i++) {
            duration += 30;
        }

        contract.setDuration(duration);

        calculateAppearanceCost(contract);

        //assign the contract
        promotion.addContract(contract);
        worker.addContract(contract);

    }

    /*
    calculate the cost for a contract if not explicitly specified
     */
    private static void calculateAppearanceCost(Contract contract) {

        int unitCost = 0;

        List<Integer> pricePoints = new ArrayList<>();

        pricePoints.addAll(Arrays.asList(0, 10, 20, 50, 75, 100, 250, 500, 1000, 10000, 100000));

        double nearest10 = contract.getWorker().getPopularity() / 10 * 10;
        double multiplier = (contract.getWorker().getPopularity() - nearest10) / 10;

        int ppIndex = (int) nearest10 / 10;

        unitCost = pricePoints.get(ppIndex);

        if (nearest10 != 100) {
            double extra = (pricePoints.get(ppIndex + 1) - unitCost) * multiplier;
            unitCost += (int) extra;
        }

        if (contract.isExclusive()) {
            unitCost *= 1.5;
        }

        contract.setAppearanceCost(unitCost);

    }

}
