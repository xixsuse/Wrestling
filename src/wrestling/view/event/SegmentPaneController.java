package wrestling.view.event;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import wrestling.model.MatchFinishes;
import wrestling.model.MatchRules;
import wrestling.model.Worker;
import wrestling.model.modelView.SegmentView;
import wrestling.view.utility.Screen;
import wrestling.view.utility.ScreenCode;
import wrestling.view.utility.ViewUtils;
import wrestling.view.utility.interfaces.ControllerBase;

public class SegmentPaneController extends ControllerBase implements Initializable {

    private static final int DEFAULTTEAMS = 2;

    @FXML
    private VBox teamsPane;

    @FXML
    private Button addTeamButton;

    @FXML
    private Button removeTeamButton;

    @FXML
    private ComboBox matchRules;

    @FXML
    private ComboBox matchFinishes;

    @FXML
    private Button matchButton;

    @FXML
    private Button angleButton;

    @FXML
    private Button interferenceButton;

    private List<Screen> teamPaneWrappers;

    private EventScreenController eventScreenController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logger = LogManager.getLogger(this.getClass());
        teamPaneWrappers = new ArrayList<>();

    }

    @Override
    public void initializeMore() {

        for (int i = 0; i < DEFAULTTEAMS; i++) {

            addTeam();

        }

        intitializeMatchFinshesCombobox();

        initializeMatchRulesCombobox();

        updateMatchRulesCombobox();

    }

    private void intitializeMatchFinshesCombobox() {

        matchFinishes.getSelectionModel().selectedItemProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
            if (newValue != null) {
                //only update when a new value is being selected
                updateLabels();
            }

        });
        //set the list cells display
        matchFinishes.setCellFactory(new Callback<ListView<MatchFinishes>, ListCell<MatchFinishes>>() {
            @Override
            public ListCell<MatchFinishes> call(ListView<MatchFinishes> p) {
                return new ListCell<MatchFinishes>() {

                    @Override
                    protected void updateItem(MatchFinishes item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item.description());
                        }
                    }
                };
            }
        });

        //set the main cell display
        matchFinishes.setButtonCell(
                new ListCell<MatchFinishes>() {
            @Override
            protected void updateItem(MatchFinishes t, boolean bln) {
                super.updateItem(t, bln);
                if (bln) {
                    setText("");
                } else {
                    setText(t.description());
                }
            }
        });

    }

    private void initializeMatchRulesCombobox() {

        //for when selection changes
        matchRules.setOnAction(event -> {
            updateMatchRulesCombobox();
            updateLabels();

        });

        //set the items, here we could filter out rules that the promotion can't use
        matchRules.setItems(FXCollections.observableArrayList(MatchRules.values()));

        //set the list cells display
        matchRules.setCellFactory(new Callback<ListView<MatchRules>, ListCell<MatchRules>>() {
            @Override
            public ListCell<MatchRules> call(ListView<MatchRules> p) {
                return new ListCell<MatchRules>() {

                    @Override
                    protected void updateItem(MatchRules item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item.description());
                        }
                    }
                };
            }
        });

        //set the main cell display
        matchRules.setButtonCell(
                new ListCell<MatchRules>() {
            @Override
            protected void updateItem(MatchRules t, boolean bln) {
                super.updateItem(t, bln);
                if (bln) {
                    setText("");
                } else {
                    setText(t.description());
                }
            }
        });

        matchRules.getSelectionModel().selectFirst();

    }

    private void updateMatchRulesCombobox() {
        MatchRules current = (MatchRules) matchRules.getSelectionModel().getSelectedItem();
        MatchFinishes lastFinish = (MatchFinishes) matchFinishes.getSelectionModel().getSelectedItem();
        List<MatchFinishes> finishes = new ArrayList<>();
        for (MatchFinishes f : MatchFinishes.values()) {

            if (current.nodq() && f.nodq()) {
                finishes.add(f);
            } else if (!current.nodq()) {
                finishes.add(f);
            }
        }

        matchFinishes.setItems(FXCollections.observableArrayList(finishes));
        if (matchFinishes.getItems().contains(lastFinish)) {
            matchFinishes.getSelectionModel().select(lastFinish);
        } else {
            matchFinishes.getSelectionModel().selectFirst();
        }
    }

    public void setEventScreenController(EventScreenController eventScreenController) {
        this.eventScreenController = eventScreenController;

    }

    @FXML
    private void handleButtonAction(ActionEvent event) throws IOException {

        if (event.getSource() == addTeamButton) {
            addTeam();
        } else if (event.getSource() == removeTeamButton) {
            removeTeam();
        } else if (event.getSource() == matchButton) {
            for (Screen screen : teamPaneWrappers) {
                ((TeamPaneWrapper) screen.controller).setMatch();
            }
        } else if (event.getSource() == angleButton) {
            for (Screen screen : teamPaneWrappers) {
                ((TeamPaneWrapper) screen.controller).setAngle();
            }
        } else if (event.getSource() == interferenceButton) {
            addInterference();
        }
    }

    //removes a worker from any teams he might be on
    //called from a teamPaneController when adding a worker
    //from another team to avoid duplicates
    public void removeWorker(Worker worker) {
        for (Screen screen : teamPaneWrappers) {
            ((TeamPaneWrapper) screen.controller).getTeamPaneController().removeWorker(worker);
        }
    }

    public List<Worker> getWorkers() {
        List<Worker> workers = new ArrayList<>();
        for (Screen screen : teamPaneWrappers) {
            workers.addAll(((TeamPaneWrapper) screen.controller).getTeamPaneController().getWorkers());
        }
        return workers;
    }

    private Screen addTeam() {

        Screen teamPaneWrapper = ViewUtils.loadScreenFromResource(ScreenCode.TEAM_PANE_WRAPPER, mainApp, gameController);

        teamPaneWrappers.add(teamPaneWrapper);

        TeamPaneController controller = ((TeamPaneWrapper) teamPaneWrapper.controller).getTeamPaneController();
        controller.setDragDropHandler(this, eventScreenController);
        controller.setTeamNumber(teamPaneWrappers.size() - 1);
        ((TeamPaneWrapper) teamPaneWrapper.controller).getXButton().setOnAction(e -> removeTeam(teamPaneWrapper));

        teamsPane.getChildren().add(teamPaneWrapper.pane);
        updateTeamNames();

        eventScreenController.updateSegments();
        
        return teamPaneWrapper;

    }
    
    private void removeTeam() {

        //we may want an alternate method where a minimum of one team is kept
        if (!teamPaneWrappers.isEmpty()) {

            teamPaneWrappers.remove(teamPaneWrappers.size() - 1);

            teamsPane.getChildren().remove(teamsPane.getChildren().size() - 1);

            //tell the event screen to update particularly the segment listView
            //because we have changed the segment name
            eventScreenController.updateSegments();

        }

    }
    
    private void addInterference() {
        Screen newTeam = addTeam();
        ((TeamPaneWrapper)newTeam.controller).setInterference();
    }

    @Override
    public void updateLabels() {
        for (Screen screen : teamPaneWrappers) {
            screen.controller.updateLabels();

        }
        eventScreenController.updateSegments();
    }

    private void removeTeam(Screen teamPaneWrapper) {
        if (teamsPane.getChildren().contains(teamPaneWrapper.pane)) {
            teamsPane.getChildren().remove(teamPaneWrapper.pane);
        }
        if (teamPaneWrappers.contains(teamPaneWrapper)) {
            teamPaneWrappers.remove(teamPaneWrapper);
        }
        eventScreenController.updateSegments();
    }

    //called by a team pane from drag dropped to update the team priority
    public void swapTeamIndices(int indexA, int indexB) {
        Collections.swap(teamPaneWrappers, indexA, indexB);
        //tell the event screen to update the event to reflect the new team priorirty
        eventScreenController.updateSegments();
        updateTeamNames();

    }

    //this will need to be more complex when more types of segments are added
    private void updateTeamNames() {
        for (Screen screen : teamPaneWrappers) {
            String teamName = "";
            switch (teamPaneWrappers.indexOf(screen)) {
                case 0:
                    teamName = "Winner";
                    break;
                default:
                    teamName = "Loser";
                    break;
            }
            ((TeamPaneWrapper) screen.controller).setTeamTypeLabel(teamName);

        }
    }

    public SegmentView getSegmentView() {
        //this would return whatever segment we generate, match or angle
        //along with all the rules etc
        SegmentView match = new SegmentView();
        match.setFinish((MatchFinishes) matchFinishes.getSelectionModel().getSelectedItem());
        match.setRules((MatchRules) matchRules.getSelectionModel().getSelectedItem());
        match.setTeams(getTeams());
        return match;
    }

    /*
    just remove all the teams and add new ones to get back up to the default size
     */
    public void clear() {
        while (!teamPaneWrappers.isEmpty()) {
            removeTeam();
        }

        for (int i = 0; i < DEFAULTTEAMS; i++) {
            addTeam();
        }

    }

    private List<List<Worker>> getTeams() {

        List<List<Worker>> teams = new ArrayList<>();

        for (Screen screen : teamPaneWrappers) {
            teams.add(((TeamPaneWrapper) screen.controller).getTeamPaneController().getWorkers());
        }

        return teams;
    }
}
