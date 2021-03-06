package openwrestling.view.browser.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import openwrestling.model.SegmentItem;
import openwrestling.model.gameObjects.EventTemplate;
import openwrestling.model.gameObjects.Promotion;
import openwrestling.model.gameObjects.TagTeam;
import openwrestling.model.segmentEnum.BrowseMode;
import openwrestling.view.utility.GameScreen;
import openwrestling.view.utility.ScreenCode;
import openwrestling.view.utility.SortControl;
import openwrestling.view.utility.ViewUtils;
import openwrestling.view.utility.interfaces.ControllerBase;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;


public class BrowserController extends ControllerBase implements Initializable {

    @FXML
    private Button rosterButton;

    @FXML
    private GridPane gridPane;

    @FXML
    private Button eventsButton;

    @FXML
    private Button staffButton;

    @FXML
    private Button titlesButton;

    @FXML
    private Button stablesButton;

    @FXML
    private Button rosterSplitButton;

    @FXML
    private Button teamsButton;

    @FXML
    private Button freeAgentsButton;

    @FXML
    private Button hireStaffButton;

    @FXML
    private Button myPromotionButton;

    private List<Button> browseButtons;

    @FXML
    private ComboBox promotionComboBox;

    @FXML
    private Label currentPromotionLabel;

    @FXML
    private AnchorPane sortControlPane;

    @FXML
    private ListView mainListView;

    @FXML
    private AnchorPane mainDisplayPane;
    private GameScreen displaySubScreen;

    private GameScreen sortControl;

    private Label categoryButton;

    private Promotion currentPromotion;

    private BrowseMode currentBrowseMode;
    private SortControl sortControlController;

    private void setCurrentPromotion(Promotion newPromotion) {
        currentPromotion = newPromotion;
        sortControlController.setCurrentPromotion(newPromotion);

        if (currentPromotion != null) {
            categoryButton.setText(newPromotion.toString());

            promotionComboBox.getSelectionModel().select(currentPromotion);

            currentPromotionLabel.setText(currentPromotion.getName() + "\n"
                    + "Level " + currentPromotion.getLevel()
                    + "\tPopularity " + currentPromotion.getPopularity()
                    + "\tFunds: " + gameController.getBankAccountManager().getBankAccount(currentPromotion).getFunds());

            if (displaySubScreen != null && displaySubScreen.controller instanceof WorkerOverviewController) {
                ((WorkerOverviewController) displaySubScreen.controller).setPromotion(currentPromotion);
            }

            updateLabels();
        }

    }

    @Override
    public void updateLabels() {

        List currentListToBrowse = currentListToBrowse();
        if (currentListToBrowse != null) {

            Comparator comparator = sortControl != null ? ((SortControl) sortControl.controller).getCurrentComparator() : null;
            FilteredList filteredList = new FilteredList<>(FXCollections.observableArrayList(currentListToBrowse), p
                    -> !((SortControl) sortControl.controller).isFiltered(p));

            mainListView.setItems(new SortedList<>(filteredList, comparator));

            if (mainListView.getSelectionModel().getSelectedItem() == null && !mainListView.getItems().isEmpty()) {
                mainListView.getSelectionModel().selectFirst();
            } else if (mainListView.getItems().isEmpty()) {
                displaySubScreen.controller.setCurrent(null);
            }
        }

        if (displaySubScreen != null) {
            displaySubScreen.controller.updateLabels();
        }
    }

    @FXML
    private void handleButtonAction(ActionEvent event) throws IOException {

        Button button = (Button) event.getSource();

        if (button == myPromotionButton) {
            setCurrentPromotion(playerPromotion());
        } else {
            currentBrowseMode = BrowseMode.valueOf(button.getId());
            ViewUtils.updateSelectedButton(button, browseButtons);
        }

        browse();
    }

    private void browse() {

        mainDisplayPane.getChildren().clear();
        displaySubScreen = ViewUtils.loadScreenFromResource(
                currentBrowseMode.subScreenCode(), mainApp, gameController, mainDisplayPane);

        if (displaySubScreen.controller instanceof StableController) {
            ((StableController) displaySubScreen.controller).setBrowseMode(currentBrowseMode);
        }

        sortControlController.setBrowseMode(currentBrowseMode);

        updateLabels();

        mainListView.getSelectionModel()
                .selectFirst();

    }

    private List currentListToBrowse() {
        Promotion promotion = currentBrowseMode.equals(BrowseMode.FREE_AGENTS)
                ? playerPromotion() : currentPromotion;
        return currentBrowseMode.listToBrowse(gameController, promotion);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        logger = LogManager.getLogger(this.getClass());

        this.browseButtons = new ArrayList<>(Arrays.asList(
                eventsButton, freeAgentsButton, myPromotionButton, rosterButton,
                stablesButton, staffButton, teamsButton, titlesButton, hireStaffButton,
                rosterSplitButton
        ));

        rosterButton.setId(BrowseMode.WORKERS.name());
        freeAgentsButton.setId(BrowseMode.FREE_AGENTS.name());
        eventsButton.setId(BrowseMode.EVENTS.name());
        teamsButton.setId(BrowseMode.TAG_TEAMS.name());
        titlesButton.setId(BrowseMode.TITLES.name());
        staffButton.setId(BrowseMode.STAFF.name());
        hireStaffButton.setId(BrowseMode.HIRE_STAFF.name());
        stablesButton.setId(BrowseMode.STABLES.name());
        rosterSplitButton.setId(BrowseMode.ROSTER_SPLIT.name());

        ViewUtils.lockGridPane(gridPane);

        categoryButton = new Label();

        currentBrowseMode = BrowseMode.WORKERS;

    }

    private void initializePromotionCombobox() {

        //set up the promotion combobox
        promotionComboBox.getItems().addAll(gameController.getPromotionManager().getPromotions());

        // show the promotion acronym
        Callback cellFactory = (Callback<ListView<Promotion>, ListCell<Promotion>>) (ListView<Promotion> p) -> new ListCell<Promotion>() {

            @Override
            protected void updateItem(Promotion item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.getShortName());
                }
            }
        };

        promotionComboBox.setCellFactory(cellFactory);
        promotionComboBox.setButtonCell((ListCell) cellFactory.call(null));

        promotionComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Promotion>() {
            @Override
            public void changed(ObservableValue<? extends Promotion> observable, Promotion oldValue, Promotion newValue) {
                setCurrentPromotion(newValue);

            }
        });

    }

    @Override
    public void initializeMore() {
        try {
            initializePromotionCombobox();

            sortControl = ViewUtils.loadScreenFromResource(ScreenCode.SORT_CONTROL, mainApp, gameController, sortControlPane);
            sortControlController = (SortControl) sortControl.controller;
            sortControlController.setUpdateAction(e -> {
                updateLabels();
            });

            mainListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>() {
                @Override
                public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
                    if (displaySubScreen != null && newValue != null) {
                        if (displaySubScreen.controller instanceof WorkerOverviewController && currentPromotion != null) {
                            ((WorkerOverviewController) displaySubScreen.controller).setPromotion(currentPromotion);
                        }
                        displaySubScreen.controller.setCurrent(newValue);
                    }
                }
            });

            promotionComboBox.setValue(playerPromotion());

            rosterButton.fire();

        } catch (Exception ex) {
            logger.log(Level.ERROR, "Error initializing broswerController", ex);
            throw new RuntimeException(ex);
        }

    }

    @Override
    public void setCurrent(Object obj) {
        if (obj instanceof EventTemplate) {
            EventTemplate template = (EventTemplate) obj;
            setCurrentPromotion(template.getPromotion());
            selectSegmentItem(BrowseMode.EVENTS, template);
        } else if (obj instanceof TagTeam) {
            TagTeam tagTeam = (TagTeam) obj;
            if (gameController.getTagTeamManager().getTagTeams(playerPromotion()).contains(tagTeam)) {
                setCurrentPromotion(playerPromotion());
            } else {
                for (Promotion promotion : gameController.getPromotionManager().getPromotions()) {
                    if (gameController.getTagTeamManager().getTagTeams(promotion).contains(tagTeam)) {
                        setCurrentPromotion(playerPromotion());
                        break;
                    }
                }
            }
            selectSegmentItem(BrowseMode.TAG_TEAMS, tagTeam);
        } else if (obj instanceof BrowseParams) {
            BrowseParams params = (BrowseParams) obj;
            setCurrentPromotion(params.promotion);
            setBrowseMode(params.browseMode);
            sortControlController.setFilter(params.filter);
        }
    }

    private void setBrowseMode(BrowseMode browseMode) {
        currentBrowseMode = browseMode;
        ViewUtils.updateSelectedButton(
                browseButtons.stream().filter(button -> button.getId().equals(browseMode.name())).findFirst().get(),
                browseButtons);
        browse();
        sortControlController.clearFilters();
    }

    private void selectSegmentItem(BrowseMode browseMode, SegmentItem segmentItem) {
        setBrowseMode(browseMode);
        mainListView.scrollTo(segmentItem);
        mainListView.getSelectionModel().select(segmentItem);
    }

}
