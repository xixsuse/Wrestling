package wrestling.view;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import wrestling.MainApp;
import wrestling.model.controller.GameController;
import wrestling.model.Worker;

public class ContractPaneController implements Initializable {

    private MainApp mainApp;
    private GameController gameController;

    @FXML
    private ComboBox typeComboBox;

    @FXML
    private ComboBox lengthComboBox;

    @FXML
    private Label costLabel;

    @FXML
    private Button signButton;

    private boolean exclusive;

    private Worker worker;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;

        initializeMore();
    }

    public void setWorker(Worker worker) {
        this.worker = worker;

        if (gameController.canNegotiate(worker, gameController.playerPromotion())) {
            setDisable(false);
        } else {
            setDisable(true);
        }
    }

    //disables the pane when negotiation is impossible
    private void setDisable(boolean disable) {

        signButton.setDisable(disable);
        lengthComboBox.setDisable(disable);

        typeComboBox.setDisable(disable);

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    private void initializeMore() {

        List<String> exclusiveOpen = new ArrayList<>(Arrays.asList("Open"));

        if (gameController.playerPromotion().getLevel() == 5) {
            exclusiveOpen.add("Exclusive");
        }

        typeComboBox.getItems().addAll(exclusiveOpen);
        typeComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.equals("Exclusive")) {
                    exclusive = true;
                } else {
                    exclusive = false;
                }

                updateLabels();

            }
        });
        typeComboBox.getSelectionModel().selectFirst();

        lengthComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                updateCostLabel();
            }
        });

        updateLengthComboBox();

    }

    @FXML
    private void handleButtonAction(ActionEvent event) throws IOException {

        if (event.getSource().equals(signButton)) {

            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Sign Contract");
            alert.setHeaderText(termString());
            alert.setContentText("Sign this contract?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                //sign the contract and disable the pane so they can't sign another
                signContract();
                setDisable(true);
            } else {
                //do nothing
            }

            updateLabels();
        }
    }

    private void signContract() {

        gameController.getContractFactory().createContract(
                worker,
                gameController.playerPromotion(),
                exclusive,
                duration(),
                gameController.date());

    }

    public void updateLabels() {

        updateLengthComboBox();
        updateCostLabel();

    }

    private String termString() {
        String terms = "$" + gameController.getContractFactory().calculateAppearanceCost(worker, exclusive);

        if (exclusive) {
            terms += " Bi-Weekly";
        } else {
            terms += " per Apperance";
        }

        return terms;
    }

    private void updateCostLabel() {
        if (worker != null && gameController.canNegotiate(worker, gameController.playerPromotion())) {
            costLabel.setText(termString());
        } else {
            costLabel.setText("Under Contract");
        }

    }

    private int duration() {
        return lengthComboBox.getSelectionModel().getSelectedIndex() + 1;
    }

    private void updateLengthComboBox() {

        int lengthMax = 12;

        lengthComboBox.getItems().clear();

        List<String> lengthList = new ArrayList<>();

        for (Integer i = 1; i <= lengthMax; i++) {
            lengthList.add(i.toString());
        }

        lengthComboBox.getItems().addAll(lengthList);

        lengthComboBox.getSelectionModel().selectFirst();
    }
}
