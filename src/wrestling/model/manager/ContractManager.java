package wrestling.model.manager;

import java.io.Serializable;
import java.time.LocalDate;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.ArrayList;
import java.util.List;
import wrestling.model.Contract;
import wrestling.model.StaffContract;
import wrestling.model.interfaces.iContract;
import wrestling.model.interfaces.iPerson;
import wrestling.model.modelView.PromotionView;
import wrestling.model.modelView.StaffView;
import wrestling.model.modelView.WorkerView;
import wrestling.model.segmentEnum.TransactionType;
import wrestling.model.utility.ContractUtils;
import wrestling.model.NewsItem;
import static wrestling.model.constants.GameConstants.APPEARANCE_MORALE_BONUS;
import static wrestling.model.constants.GameConstants.MORALE_PENALTY_DAYS_BETWEEN;
import static wrestling.model.utility.ContractUtils.isMoraleCheckDay;
import static wrestling.model.constants.GameConstants.MAX_RELATIONSHIP_LEVEL;

public class ContractManager implements Serializable {

    private final List<Contract> contracts;
    private final List<StaffContract> staffContracts;

    private final PromotionManager promotionManager;
    private final TitleManager titleManager;
    private final NewsManager newsManager;
    private final RelationshipManager relationshipManager;

    public ContractManager(PromotionManager promotionManager, TitleManager titleManager, NewsManager newsManager, RelationshipManager relationshipManager) {
        contracts = new ArrayList<>();
        staffContracts = new ArrayList<>();
        this.promotionManager = promotionManager;
        this.titleManager = titleManager;
        this.newsManager = newsManager;
        this.relationshipManager = relationshipManager;
    }

    public void dailyUpdate(LocalDate date) {
        for (Contract contract : contracts) {
            if (!contract.isActive()) {
                continue;
            }
            if (!nextDay(contract, date)) {
                titleManager.stripTitles(contract);
            }
            if (isMoraleCheckDay(contract, date)) {
                handleMoraleCheck(contract, date);
            }
        }

        for (StaffContract contract : staffContracts) {
            nextDay(contract, date);
        }
    }

    public void addContract(Contract contract) {
        contracts.add(contract);
    }

    public void addContract(StaffContract contract) {
        staffContracts.add(contract);
    }

    public List<Contract> getContracts(PromotionView promotion) {
        List<Contract> promotionContracts = new ArrayList<>();
        for (Contract contract : contracts) {
            if (contract.isActive() && contract.getPromotion().equals(promotion)) {
                promotionContracts.add(contract);
            }
        }

        return promotionContracts;
    }

    public List<StaffContract> getStaffContracts(PromotionView promotion) {
        List<StaffContract> promotionStaffContracts = new ArrayList<>();
        for (StaffContract contract : staffContracts) {
            if (contract.isActive() && contract.getPromotion().equals(promotion)) {
                promotionStaffContracts.add(contract);
            }
        }
        return promotionStaffContracts;
    }

    public List<iContract> allContracts() {
        List<iContract> allContracts = new ArrayList<>();
        allContracts.addAll(contracts);
        allContracts.addAll(staffContracts);
        return allContracts;
    }

    public List<? extends iContract> getContracts(iPerson person) {
        return person instanceof WorkerView
                ? getContracts((WorkerView) person)
                : getContracts((StaffView) person);
    }

    public List<Contract> getContracts(WorkerView worker) {
        List<Contract> workerContracts = new ArrayList<>();
        for (Contract contract : contracts) {
            if (contract.isActive() && contract.getWorker().equals(worker)) {
                workerContracts.add(contract);
            }
        }

        return workerContracts;
    }

    public List<StaffContract> getContracts(StaffView staff) {
        List<StaffContract> contractsForStaff = new ArrayList<>();
        for (StaffContract contract : staffContracts) {
            if (contract.isActive() && contract.getStaff().equals(staff)) {
                contractsForStaff.add(contract);
            }
        }

        return contractsForStaff;
    }

    public StaffContract getContract(StaffView staff) {
        StaffContract staffContract = null;
        for (StaffContract contract : staffContracts) {
            if (contract.isActive() && contract.getStaff().equals(staff)) {
                staffContract = contract;
                break;
            }
        }

        return staffContract;
    }

    public Contract getContract(WorkerView worker, PromotionView promotion) {
        Contract workerContract = null;
        for (Contract contract : contracts) {
            if (contract.isActive() && contract.getWorker().equals(worker)
                    && contract.getPromotion().equals(promotion)) {
                workerContract = contract;
                break;
            }
        }

        return workerContract;
    }

    public List<WorkerView> getActiveRoster(PromotionView promotion) {

        List<WorkerView> roster = new ArrayList<>();
        for (Contract contract : contracts) {
            if (contract.isActive() && contract.getPromotion().equals(promotion)
                    && contract.getWorker().isFullTime()) {
                roster.add(contract.getWorker());
            }
        }

        return roster;
    }

    public List<WorkerView> getPushed(PromotionView promotion) {
        List<WorkerView> roster = new ArrayList<>();
        for (Contract contract : contracts) {
            if (contract.isActive() && contract.getPromotion().equals(promotion)
                    && contract.isPushed()) {
                roster.add(contract.getWorker());
            }

        }

        return roster;
    }

    //depreciates monthly contracts
    public boolean nextDay(iContract contract, LocalDate today) {
        if (contract.getEndDate().isBefore(today)) {
            terminateContract(contract);
            return false;
        }

        return true;
    }

    public void appearance(LocalDate date, WorkerView worker, PromotionView promotion) {
        Contract contract = getContract(worker, promotion);
        relationshipManager.addRelationshipValue(worker, promotion, APPEARANCE_MORALE_BONUS);
        contract.setLastShowDate(date);
        promotionManager.getBankAccount(contract.getPromotion()).removeFunds(contract.getAppearanceCost(), TransactionType.WORKER, date);
    }

    public void payDay(LocalDate date, Contract contract) {
        if (contract.getMonthlyCost() != 0) {
            promotionManager.getBankAccount(contract.getPromotion()).removeFunds(Math.toIntExact(contract.getMonthlyCost()),
                    TransactionType.WORKER, date);
        }
    }

    public void payDay(LocalDate date, StaffContract contract) {
        if (contract.getMonthlyCost() != 0) {
            promotionManager.getBankAccount(contract.getPromotion()).removeFunds(contract.getMonthlyCost(),
                    TransactionType.STAFF, date);
        }
    }

    public void paySigningFee(LocalDate date, iContract contract) {
        promotionManager.getBankAccount(contract.getPromotion()).removeFunds(
                ContractUtils.calculateSigningFee(contract.getPerson(), date),
                contract.getPerson() instanceof WorkerView ? TransactionType.WORKER : TransactionType.STAFF,
                date);
    }

    public void buyOutContracts(WorkerView worker, PromotionView newExclusivePromotion, LocalDate buyOutDate) {
        //'buy out' any the other contracts the worker has
        for (Contract c : getContracts(worker)) {
            if (!c.getPromotion().equals(newExclusivePromotion)) {
                c.setEndDate(buyOutDate);
            }
        }
    }

    public void buyOutContracts(StaffView staff, PromotionView newExclusivePromotion, LocalDate buyOutDate) {
        //'buy out' any the other contracts the worker has
        for (StaffContract c : getContracts(staff)) {
            if (!c.getPromotion().equals(newExclusivePromotion)) {
                c.setEndDate(buyOutDate);
            }
        }
    }

    public void terminateContract(iContract contract) {
        contract.getPromotion().removeFromRoster(contract.getWorker());
        contract.getPromotion().removeFromStaff(contract.getStaff());
        if (contract.getWorker() != null) {
            contract.getWorker().removeContract((Contract) contract);
        } else if (contract.getStaff() != null) {
            contract.getStaff().setStaffContract(null);
        }
        contract.setActive(false);
    }

    public String getTerms(iContract contract) {
        String string = String.format("%s ending %s", contract.getPromotion().getShortName(), contract.getEndDate());

        if (contract.isExclusive()) {
            string += " $" + contract.getMonthlyCost() + " Monthly.";
        } else {
            string += " $" + contract.getAppearanceCost() + " per appearance.";
        }

        return string;
    }

    public boolean canNegotiate(iPerson person, PromotionView promotion) {
        //this would have to be more robust
        //such as checking how much time is left on our contract
        boolean canNegotiate = true;

        for (iContract contract : person.getContracts()) {
            if (contract.isExclusive() || contract.getPromotion().equals(promotion)) {
                canNegotiate = false;
            }
        }

        return canNegotiate;
    }

    public String contractString(WorkerView worker) {

        StringBuilder bld = new StringBuilder();
        for (Contract current : getContracts(worker)) {

            bld.append(getTerms(current));
            bld.append("\n");
        }
        return bld.toString();
    }

    public String contractPromotionsString(iPerson person, LocalDate date) {
        StringBuilder bld = new StringBuilder();
        for (iContract current : getContracts(person)) {
            if (!bld.toString().isEmpty()) {
                bld.append("/");
            }
            bld.append(current.getPromotion().getShortName());
        }
        return bld.toString();
    }

    public String contractTermsString(WorkerView worker, LocalDate date) {
        StringBuilder bld = new StringBuilder();
        for (Contract current : getContracts(worker)) {
            if (!bld.toString().isEmpty()) {
                bld.append("/");
            }
            if (current.isExclusive()) {
                bld.append(String.format("%s (%d day%s)",
                        current.getPromotion().getShortName(),
                        DAYS.between(date, current.getEndDate()),
                        DAYS.between(date, current.getEndDate()) > 1 ? "s" : ""));
            } else {
                bld.append(current.getPromotion().getShortName());
            }
        }
        return bld.toString();
    }

    public int averageWorkerPopularity(PromotionView promotion) {
        int totalPop = 0;
        int averagePop = 0;

        if (!promotion.getFullRoster().isEmpty()) {
            for (WorkerView worker : promotion.getFullRoster()) {
                totalPop += worker.getPopularity();
            }
            averagePop = totalPop / promotion.getFullRoster().size();
        }

        return averagePop;
    }

    private void handleMoraleCheck(iContract contract, LocalDate date) {
        long daysBetween = DAYS.between(contract.getLastShowDate(), date);
        int penalty = Math.round(daysBetween / MORALE_PENALTY_DAYS_BETWEEN);
        if (penalty > 0) {
            relationshipManager.addRelationshipValue(contract.getWorker(), contract.getPromotion(), -penalty);
            addMoraleNewsItem(contract, daysBetween, penalty, date);
        }
    }

    private void addMoraleNewsItem(iContract contract, long daysBetween, int penalty, LocalDate date) {
        NewsItem newsItem = new NewsItem(String.format("%s loses morale", contract.getWorker().getShortName()),
                String.format("%s has not worked a show for %s in %d days, and loses %d morale.",
                        contract.getWorker().getLongName(),
                        contract.getPromotion().getName(),
                        daysBetween,
                        penalty)
        );
        newsItem.setDate(date);
        newsItem.setPromotion(contract.getPromotion());
        newsManager.addNews(newsItem);
    }
}
