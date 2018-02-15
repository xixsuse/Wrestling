package wrestling.view;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import wrestling.MainApp;
import wrestling.model.Event;
import wrestling.model.Match;
import wrestling.model.Worker;
import wrestling.model.interfaces.Segment;
import wrestling.model.modelView.SegmentView;
import wrestling.view.interfaces.ControllerBase;
import wrestling.view.utility.RefreshSkin;
import wrestling.view.utility.ViewUtils;

public class EventScreenController extends ControllerBase implements Initializable {

    private int totalSegments;

    @FXML
    private Button runEventButton;

    @FXML
    private Button addSegmentButton;

    @FXML
    private Button removeSegmentButton;

    @FXML
    private ListView<SegmentNameItem> segmentListView;

    @FXML
    private Label totalCostLabel;

    @FXML
    private ListView<Worker> workersListView;

    @FXML
    private AnchorPane segmentPaneHolder;

    @FXML
    private Label eventTitleLabel;

    private final List<Pane> segmentPanes = new ArrayList<>();
    private final List<SegmentPaneController> segmentPaneControllers = new ArrayList<>();
    private final List<SegmentView> segments = new ArrayList<>();

    private Event currentEvent;

    //this would be for keeping track of the index number of the currently
    //selected segment
    private Number currentSegmentNumber;

    @Override
    public void setCurrent(Object obj) {
        if (obj instanceof Event) {
            currentEvent = (Event) obj;
            updateLabels();
        } else {
            logger.log(Level.ERROR, "Invalid object passed to EventScreen");
        }
    }

    private SegmentView currentSegment() {
        return segments.get(currentSegmentNumber.intValue());
    }

    private void setCurrentSegmentNumber(int number) {

        Number newNumber = number;
        setCurrentSegmentNumber(newNumber);
    }

    private void setCurrentSegmentNumber(Number number) {
        currentSegmentNumber = number;

        segmentPaneHolder.getChildren().clear();
        segmentPaneHolder.getChildren().add(segmentPanes.get(currentSegmentNumber.intValue()));

        updateLabels();

    }

    @FXML
    private void handleButtonAction(ActionEvent event) throws IOException {

        if (event.getSource() == runEventButton) {
            runEvent();
        } else if (event.getSource() == addSegmentButton) {
            addSegment();
        } else if (event.getSource() == removeSegmentButton) {
            removeSegment();
        }
    }

    private void runEvent() throws IOException {
        //select the first segment so when we come back to do a new event
        //it will be highlighted already
        //maybe we should have an onEnter and onExit function instead...
        //although this is basically onExit already (minus the actual exit)
        segmentListView.getSelectionModel().selectFirst();

        //have to update the event segments first
        updateSegments();

        List<SegmentView> eventSegments = new ArrayList<>();

        //eliminate empty segments here
        for (SegmentView segmentView : segments) {
            if (!segmentView.getWorkers().isEmpty()) {
                eventSegments.add(segmentView);
            }
        }

        //create the event with the segments assembled
        gameController.getEventFactory().createEventFromTemp(currentEvent,
                segments,
                currentEvent.getDate(),
                playerPromotion());

        //clear the segments, so when we come back to do a new event
        //it will be empty again
        segments.clear();

        //go through the segmentPaneControllers and clear all the teams
        for (SegmentPaneController current : segmentPaneControllers) {
            current.clear();
        }

        //tell the main app to show the browser and pass the event
        //so it can be selected by the corresponding controller
        mainApp.showLastEvent();
        //advance the day
        mainApp.nextDay();

        updateLabels();
    }

    //this updates the segment list associated with the controller
    //and calls to update everything on the screen to reflect this
    //this should perhaps be more primary/streamlined, update this and get everything else
    //like labels and listview content from the item
    public void updateSegments() {

        segments.clear();
        for (SegmentPaneController currentController : segmentPaneControllers) {
            segments.add(currentController.getTempMatch());
        }

        updateLabels();
    }

    //updates lists and labels
    @Override
    public void updateLabels() {

        if (currentEvent != null) {
            eventTitleLabel.setText("Now booking: " + currentEvent.toString());
        }

        totalCostLabel.setText("Total Cost: $" + currentCost());

        for (SegmentNameItem segmentNameItem : segmentListView.getItems()) {

            segmentNameItem.segment.set(segments.get(segmentListView.getItems().indexOf(segmentNameItem)));
            segmentNameItem.name.set(gameController.getMatchManager().getMatchString((SegmentView) segmentNameItem.segment.get()));
        }

        updateWorkerListView();

        ((RefreshSkin) segmentListView.getSkin()).refresh();

    }

    //dynamic current cost calculation
    private int currentCost() {

        int currentCost = 0;

        for (Segment segment : segments) {
            if (segment instanceof Match) {
                for (Worker worker : gameController.getMatchManager().getWorkers((Match) segment)) {
                    currentCost += gameController.getContractManager().getContract(worker, playerPromotion()).getAppearanceCost();
                }
            }
        }
        return currentCost;
    }

    /*
    adds a segment to the segment listview, creates the corresponding segment
    pane and controller and adds them to the proper arrays for reference
     */
    private void addSegment() {

        try {

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/SegmentPane.fxml"));
            Pane segmentPane = (Pane) loader.load();

            //keep a reference to the segment pane
            segmentPanes.add(segmentPane);

            //keep a reference to the controller
            SegmentPaneController controller = loader.getController();
            segmentPaneControllers.add(controller);

            controller.setEventScreenController(this);
            controller.setDependencies(mainApp, gameController);

            //update the segment listview
            SegmentNameItem item = new SegmentNameItem();
            segmentListView.getItems().add(item);
            item.segment.set(controller.getTempMatch());
            item.name.set("Segment " + segments.size());

            updateSegments();

        } catch (IOException ex) {
            logger.log(Level.ERROR, ex);
        }
    }

    private void removeSegment() {

        SegmentNameItem currentSegment = segmentListView.getSelectionModel().getSelectedItem();

        if (currentSegment == null) {

            segmentListView.getSelectionModel().selectLast();
            currentSegment = segmentListView.getSelectionModel().getSelectedItem();
        }

        if (segments.size() > 1) {

            int indexToRemove = segmentListView.getItems().indexOf(currentSegment);

            segmentListView.getItems().remove(currentSegment);

            segmentPanes.remove(indexToRemove);

            setCurrentSegmentNumber(currentSegmentNumber);

            //remove the controller too
            segmentPaneControllers.remove(indexToRemove);

            //update the event since we have changed the number of segments
            updateSegments();
        }

    }

    /*
    prepares the segment listView
    this may need modification if we allow adding/removing segments
     */
    private void initializeSegmentListView() {

        RefreshSkin skin = new RefreshSkin(segmentListView);
        segmentListView.setSkin(skin);

        ObservableList<SegmentNameItem> items = FXCollections.observableArrayList(SegmentNameItem.extractor());

        segmentListView.setCellFactory(param -> new SorterCell());

        segmentListView.setItems(items);

        segmentListView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                //check that we have a valid newValue, because strange things happen otherwise
                //when we clear the list and refresh it
                if (newValue.intValue() >= 0) {
                    setCurrentSegmentNumber(newValue);

                }

            }
        });
    }


    /*
    additional initialization to be called externally after we have our mainApp etc.
     */
    @Override
    public void initializeMore() {

        //here we set a blank event
        initializeSegmentListView();

        /*
        create versespanes and controllers for each segment and keeps references
        will need to be more flexible when other segment types are possible
         */
        for (int i = 0; i < totalSegments; i++) {
            addSegment();
        }

        segmentListView.getSelectionModel().selectFirst();

        //for the workersListView to accept dragged items
        final EventHandler<DragEvent> dragOverHandler = (DragEvent dragEvent) -> {
            dragEvent.acceptTransferModes(TransferMode.MOVE);
        };

        workersListView.setOnDragOver(dragOverHandler);

        //do this last as it is dependent on currentSegment
        updateWorkerListView();

        //add the special DragDropHandlder
        workersListView.setOnDragDropped(new WorkersListViewDragDropHandler());

    }

    private void updateWorkerListView() {

        //get the workers and add them to the listview on the left
        ObservableList<Worker> workersList = FXCollections.observableArrayList();

        List<Worker> roster = gameController.getContractManager().getFullRoster(playerPromotion());

        for (Worker worker : roster) {
            //we only want to include workers that aren't already in the segment
            //as well as workers who aren't already booked on the event date (today)
            if (workerIsAvailableForCurrentSegment(worker)) {
                workersList.add(worker);
            }
        }

        workersListView.setItems(workersList);
        ((RefreshSkin) workersListView.getSkin()).refresh();

    }

    private boolean workerIsAvailableForCurrentSegment(Worker worker) {
        return !currentSegment().getWorkers().contains(worker)
                && gameController.getEventManager().isAvailable(
                        worker,
                        gameController.getDateManager().today(),
                        playerPromotion());
    }

    private boolean workerIsBookedOnShow(Worker worker) {
        for (SegmentPaneController controller : segmentPaneControllers) {
            if (controller.getWorkers().contains(worker)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        logger = LogManager.getLogger(this.getClass());

        totalSegments = 8;

        currentSegmentNumber = 0;
        initializeSegmentListView();

        setWorkerCellFactory(workersListView);

        RefreshSkin skin = new RefreshSkin(workersListView);

        workersListView.setSkin(skin);

    }

    private void setWorkerCellFactory(ListView listView) {

        listView.setCellFactory(lv -> new ListCell<Worker>() {

            @Override
            public void updateItem(final Worker worker, boolean empty) {
                super.updateItem(worker, empty);
                if (workerIsBookedOnShow(worker)) {
                    getStyleClass().add("highStat");
                } else {
                    getStyleClass().remove("highStat");
                }
                ViewUtils.initListCellForWorkerDragAndDrop(this, worker, empty);

            }

        });

    }

    private static class SegmentNameItem {

        public static Callback<SegmentNameItem, Observable[]> extractor() {
            return (SegmentNameItem param) -> new Observable[]{param.segment, param.name};
        }
        StringProperty name = new SimpleStringProperty();
        ObjectProperty<Segment> segment = new SimpleObjectProperty();

        @Override
        public String toString() {
            return name.get();
        }
    }

    private class SorterCell extends ListCell<SegmentNameItem> {

        private Label myLabel;

        public SorterCell() {
            ListCell thisCell = this;

            setOnDragDetected((MouseEvent event) -> {
                if (getItem() == null) {

                    return;
                }

                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();

                content.putString(getText());
                LocalDragboard.getINSTANCE().putValue(SegmentNameItem.class, getItem());
                content.putString(getItem().name.get());

                dragboard.setContent(content);

                event.consume();
            });

            setOnDragOver(event -> {
                if (event.getGestureSource() != thisCell
                        && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }

                event.consume();
            });

            setOnDragEntered(event -> {
                if (event.getGestureSource() != thisCell
                        && event.getDragboard().hasString()) {
                    setOpacity(0.3);
                }
            });

            setOnDragExited(event -> {
                if (event.getGestureSource() != thisCell
                        && event.getDragboard().hasString()) {
                    setOpacity(1);
                }
            });

            setOnDragDropped((DragEvent event) -> {

                if (getGraphic() == null) {
                    return;

                }

                boolean success = false;

                LocalDragboard ldb = LocalDragboard.getINSTANCE();
                if (ldb.hasType(SegmentNameItem.class)) {
                    SegmentNameItem segmentNameItem = ldb.getValue(SegmentNameItem.class);
                    ObservableList<SegmentNameItem> items = getListView().getItems();
                    int draggedIdx = items.indexOf(segmentNameItem);
                    int thisIdx = items.indexOf(getItem());

                    //swap all parallel arrays associated with the segment
                    Collections.swap(items, thisIdx, draggedIdx);
                    Collections.swap(segmentPanes, thisIdx, draggedIdx);
                    Collections.swap(segmentPaneControllers, thisIdx, draggedIdx);
                    Collections.swap(segments, thisIdx, draggedIdx);

                    setCurrentSegmentNumber(thisIdx);

                    segmentListView.getSelectionModel().select(segmentNameItem);
                    success = true;
                }

                event.setDropCompleted(success);

                event.consume();
            });

            setOnDragDone(DragEvent::consume);
        }

        @Override
        protected void updateItem(SegmentNameItem item, boolean empty) {

            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {

                myLabel = new Label(item.name.getValue());
                myLabel.setTextAlignment(TextAlignment.CENTER);

                myLabel.setWrapText(true);
                myLabel.setMaxWidth(segmentListView.getWidth() - 40);

                myLabel.getStyleClass().add("sorterLabel");
                setGraphic(myLabel);

            }

        }
    }

    /*
    to be used by the workersListView on the left of the screen
    should only be needed for when the user is dropping a worker
    on the listView that has been dragged from one of the teams
     */
    private class WorkersListViewDragDropHandler implements EventHandler<DragEvent> {

        @Override
        public void handle(DragEvent event) {

            LocalDragboard ldb = LocalDragboard.getINSTANCE();
            if (ldb.hasType(Worker.class)) {
                Worker worker = ldb.getValue(Worker.class);

                if (!workersListView.getItems().contains(worker)) {
                    segmentPaneControllers.get(currentSegmentNumber.intValue()).removeWorker(worker);
                    workersListView.getItems().add(worker);
                }

                updateLabels();
                segmentPaneControllers.get(currentSegmentNumber.intValue()).updateLabels();
                updateSegments();

                //Clear, otherwise we end up with the worker stuck on the dragboard?
                ldb.clearAll();

            }
        }

    }

}
