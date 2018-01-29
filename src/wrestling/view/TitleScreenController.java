package wrestling.view;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wrestling.MainApp;

public class TitleScreenController extends ControllerBase implements Initializable {

    @FXML
    private Button newRandomGameButton;

    @FXML
    private Button newImportGameButton;

    @FXML
    private Button continueGameButton;

    @FXML
    private ImageView imageView;

    @FXML
    private Text versionText;

    private transient Logger logger = LogManager.getLogger(getClass());

    @FXML
    private void handleButtonAction(ActionEvent event) {

        if (event.getSource() == newRandomGameButton) {
            try {
                mainApp.newRandomGame();
            } catch (IOException ex) {
                logger.log(Level.ERROR, "Exception on new random game", ex);
                mainApp.generateAlert("Error", "Import game data failed", ex.getMessage()).showAndWait();
            }
        } else if (event.getSource() == newImportGameButton) {

            try {
                showImportDialog();
            } catch (IOException ex) {
                logger.log(Level.ERROR, "Exception on import game", ex);
                mainApp.generateAlert("Error", "Import game data failed", ex.getMessage()).showAndWait();
            }

        } else if (event.getSource() == continueGameButton) {
            try {
                mainApp.continueGame();
            } catch (IOException ex) {
                logger.log(Level.ERROR, "Exception on continue game", ex);
                mainApp.generateAlert("Error", "Continue from saved game failed", ex.getMessage()).showAndWait();
            }

        }

    }

    private boolean showImportDialog() throws IOException {
        Stage importPopup = new Stage();

        importPopup.initModality(Modality.APPLICATION_MODAL);
        importPopup.setTitle("New Import Game");

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource("view/ImportDialog.fxml"));

        AnchorPane importDialog = (AnchorPane) loader.load();

        ImportDialogController controller = loader.getController();
        controller.setDependencies(mainApp, gameController);
        controller.updateLabels();
        controller.setStage(importPopup);

        Scene importScene = new Scene(importDialog);

        importScene.getStylesheets().add("style.css");

        importPopup.setScene(importScene);

        importPopup.showAndWait();

        return true;
    }

    @Override
    public void initializeMore() {
        versionText.setText("Version " + mainApp.getVERSION() + "\n"
                + "For feedback and support contact " + mainApp.getCONTACT());
    }

    public void setImage(Image image) {
        this.imageView.setImage(image);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
