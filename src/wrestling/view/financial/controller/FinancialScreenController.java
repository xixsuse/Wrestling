package wrestling.view.financial.controller;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import wrestling.model.segmentEnum.StaffType;
import wrestling.view.utility.GameScreen;
import wrestling.view.utility.ScreenCode;
import wrestling.view.utility.ViewUtils;
import wrestling.view.utility.interfaces.ControllerBase;

public class FinancialScreenController extends ControllerBase implements Initializable {

    private List<List<Label>> sheetLabels;

    @FXML
    private Label gate1;

    @FXML
    private Label gate2;

    @FXML
    private Label gate3;

    private List<Label> gateLabels;

    @FXML
    private Label workerExpense1;

    @FXML
    private Label workerExpense2;

    @FXML
    private Label workerExpense3;

    @FXML
    private AnchorPane medicalBase;

    @FXML
    private AnchorPane creativeBase;

    @FXML
    private AnchorPane roadAgentBase;

    private List<Label> workerExpenseLabels;

    private List<GameScreen> departmentScreens;

    private String sheetCell(char type, int monthsAgo) {

        LocalDate startDate = gameController.getDateManager().today().minusMonths(monthsAgo).withDayOfMonth(1);

        LocalDate endDate = gameController.getDateManager().today();

        if (monthsAgo == 0) {
            endDate = gameController.getDateManager().today();
        } else {
            endDate = gameController.getDateManager().today().withDayOfMonth(monthsAgo).minusDays(1);
        }

        int amount = gameController.getPromotionManager().getBankAccount(playerPromotion())
                .getTransactionTotal(
                        type,
                        startDate,
                        endDate);

        return "$" + amount;
    }

    @Override
    public void updateLabels() {
        for (int i = 0; i < gateLabels.size(); i++) {
            gateLabels.get(i).setText(sheetCell('e', i));
            workerExpenseLabels.get(i).setText(sheetCell('w', i));
        }

        for (GameScreen screen : departmentScreens) {
            screen.controller.updateLabels();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gateLabels = new ArrayList<>();
        departmentScreens = new ArrayList<>();
        gateLabels.addAll(Arrays.asList(gate1, gate2, gate3));

        workerExpenseLabels = new ArrayList<>();
        workerExpenseLabels.addAll(Arrays.asList(workerExpense1, workerExpense2, workerExpense3));

    }

    @Override
    public void initializeMore() {
        GameScreen medical = ViewUtils.loadScreenFromResource(ScreenCode.DEPARTMENT, mainApp, gameController, medicalBase);
        medical.controller.setCurrent(StaffType.MEDICAL);
        departmentScreens.add(medical);

        GameScreen roadAgents = ViewUtils.loadScreenFromResource(ScreenCode.DEPARTMENT, mainApp, gameController, roadAgentBase);
        roadAgents.controller.setCurrent(StaffType.ROAD_AGENT);
        departmentScreens.add(roadAgents);

        GameScreen creative = ViewUtils.loadScreenFromResource(ScreenCode.DEPARTMENT, mainApp, gameController, creativeBase);
        creative.controller.setCurrent(StaffType.CREATIVE);
        departmentScreens.add(creative);

    }

}
