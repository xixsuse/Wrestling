/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import wrestling.MainApp;
import wrestling.model.Event;
import wrestling.model.GameController;
import wrestling.model.Segment;
import wrestling.model.Worker;

public class EventScreenController implements Initializable {

    private MainApp mainApp;
    private GameController gameController;

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
    private GridPane gridPane;

    private final List<Pane> segmentPanes = new ArrayList<>();
    private final List<SegmentPaneController> segmentPaneControllers = new ArrayList<>();
    private final List<Segment> segments = new ArrayList<>();

    private Segment currentSegment() {
        return segments.get(currentSegmentNumber.intValue());
    }

    //this would be for keeping track of the index number of the currently
    //selected segment
    private Number currentSegmentNumber;

    public Number getCurrentSegmentNumber() {
        return currentSegmentNumber;
    }

    private void setCurrentSegmentNumber(int number) {

        Integer intObject = new Integer(number);

        Number newNumber = (Number) intObject;
        setCurrentSegmentNumber(newNumber);
    }

    private void setCurrentSegmentNumber(Number number) {

        //remove the previous segment pane from the grid first if it's still there
        if (gridPane.getChildren().contains(segmentPanes.get(currentSegmentNumber.intValue()))) {
            gridPane.getChildren().remove(segmentPanes.get(currentSegmentNumber.intValue()));
        }

        currentSegmentNumber = number;

        //here we  update the central pane to show the new current segment
        //but first make sure it isn't there already!
        if (!gridPane.getChildren().contains(segmentPanes.get(currentSegmentNumber.intValue()))) {
            gridPane.add(segmentPanes.get(currentSegmentNumber.intValue()), 1, 0);
            GridPane.setRowSpan(segmentPanes.get(currentSegmentNumber.intValue()), 3);
        }

        updateLabels();

    }

    private Event currentEvent;

    public void setCurrentEvent(Event event) {
        this.currentEvent = event;
    }

    @FXML
    private void handleButtonAction(ActionEvent event) throws IOException {

        if (event.getSource() == runEventButton) {

            //select the first segment so when we come back to do a new event
            //it will be highlighted already
            segmentListView.getSelectionModel().selectFirst();

            //have to update the event segments first
            //this updates the segments list as well as the current event-in-progress
            updateEvent();

            //create a new event with the updated segment list, date, player promotion
            Event finishedEvent = new Event(segments, gameController.date(), gameController.playerPromotion());
            finishedEvent.scheduleEvent(gameController.date());

            //clear the segments, so when we come back to do a new event
            //it will be empty again
            segments.clear();

            //go through the segmentPaneControllers and clear all the teams
            for (SegmentPaneController current : segmentPaneControllers) {
                current.clear();
            }

            //tell the main app to show the browser and pass the event
            //so it can be selected by the corresponding controller
            //mainApp.showBrowser(finishedEvent);
            
            
            //advance the day
            mainApp.nextDay();

            //reset the current event
            newCurrentEvent();
            updateLabels();

        } else if (event.getSource() == addSegmentButton) {
            addSegment();
        } else if (event.getSource() == removeSegmentButton) {
            removeSegment();
        }
    }

    //this updates the segment list associated with the controller
    //and calls to update everything on the screen to reflect this
    //this should perhaps be more primary/streamlined, update this and get everything else
    //like labels and listview content from the item
    public void updateEvent() {

        segments.clear();
        for (SegmentPaneController currentController : segmentPaneControllers) {
            segments.add(currentController.getSegment());
        }

        currentEvent.setSegments(segments);

        updateLabels();
    }

    //updates lists and labels
    public void updateLabels() {

        totalCostLabel.setText("Total Cost: $" + currentEvent.currentCost());

        for (SegmentNameItem current : segmentListView.getItems()) {

            current.segment.set(segments.get(segmentListView.getItems().indexOf(current)));
            current.name.set(current.segment.get().toString());
        }

        updateWorkerListView();

    }

    private int segmentListViewWidth = 300;

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
            controller.initializeMore();

            //update the segment listview
            SegmentNameItem item = new SegmentNameItem();
            segmentListView.getItems().add(item);
            item.segment.set(controller.getSegment());
            item.name.set("Segment " + segments.size());

            updateEvent();

        } catch (IOException e) {

            e.printStackTrace();

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

            /*
            if removing the segment from the listview hasn't changed the index selected,
            we need to to update it ourselves
             */
            if (segmentListView.getSelectionModel().getSelectedIndex() == indexToRemove) {
                gridPane.getChildren().remove(segmentPanes.get(indexToRemove));
                segmentPanes.remove(indexToRemove);
                //set the current segment to itself to get the new segment pane
                //at the index we have just removed
                setCurrentSegmentNumber(currentSegmentNumber);

            } else {
                //else just remove the segment pane from the segment pane list
                segmentPanes.remove(indexToRemove);
            }

            //remove the controller too
            segmentPaneControllers.remove(indexToRemove);

            //update the event since we have changed the number of segments
            updateEvent();
        }

    }

    /*
    prepares the segment listView
    this may need modification if we allow adding/removing segments
     */
    private void initializeSegmentListView() {

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
    used for the segment listview, instead of strings we keep an observable list
    of SegmentNameItems. This forces the listview to update everytime we update
    a SegmentNameItem.
     */
    private static class SegmentNameItem {

        StringProperty name = new SimpleStringProperty();

        ObjectProperty<Segment> segment = new SimpleObjectProperty();

        public static Callback<SegmentNameItem, Observable[]> extractor() {
            return new Callback<SegmentNameItem, Observable[]>() {
                @Override
                public Observable[] call(SegmentNameItem param) {
                    return new Observable[]{param.segment, param.name};
                }
            };
        }

        @Override
        public String toString() {
            return name.get();
        }
    }

    /*
    special cell for the segment sorter that handles sorting by drag
    and drop
     */
    private class SorterCell extends ListCell<SegmentNameItem> {

        public SorterCell() {
            ListCell thisCell = this;

            setOnDragDetected(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (getItem() == null) {

                        return;
                    }

                    Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(getText());
                    LocalDragboard.getInstance().putValue(SegmentNameItem.class, getItem());
                    content.putString(getItem().name.get());

                    dragboard.setContent(content);;

                    event.consume();
                }
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

            setOnDragDropped(new EventHandler<DragEvent>() {
                @Override
                public void handle(DragEvent event) {
                    if (getText() == null) {

                        return;

                    }

                    boolean success = false;

                    LocalDragboard ldb = LocalDragboard.getInstance();
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
                }
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
                setText(item.name.get());
            }
        }
    }

    private void newCurrentEvent() {

        this.currentEvent = new Event(gameController.date(), gameController.playerPromotion());
    }

    /*
    additional initialization to be called externally after we have our mainApp etc.
     */
    private void initializeMore() {

        //here we set a blank event, this will have to take an event from somewhere else
        //ideally?
        newCurrentEvent();

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
        final EventHandler<DragEvent> dragOverHandler = new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent dragEvent) {

                dragEvent.acceptTransferModes(TransferMode.MOVE);

            }
        };
        
        workersListView.setOnDragOver(dragOverHandler);
        
        //do this last as it is dependent on currentSegment
        updateWorkerListView();
        
        //add the special DragDropHanlder
        workersListView.setOnDragDropped(new WorkersListViewDragDropHandler());

    }

    private void updateWorkerListView() {

        //get the workers and add them to the listview on the left
        ObservableList<Worker> workersList = FXCollections.observableArrayList();

        List<Worker> roster = gameController.playerPromotion().getRoster();

        for (Worker worker : roster) {

            //we only want to include workers that aren't already in the segment
            //as well as workers who aren't already booked on the event date
            if (!currentSegment().allWorkers().contains(worker) && !worker.isBooked(currentEvent.getDate())) {

                workersList.add(worker);
            }

        }

        workersListView.setItems(workersList);
        
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        totalSegments = 8;

        currentSegmentNumber = 0;
        initializeSegmentListView();

        setWorkerCellFactory(workersListView);

    }

    private void setWorkerCellFactory(ListView listView) {
        listView.setCellFactory(new Callback<ListView<Worker>, ListCell<Worker>>() {

            @Override
            public ListCell<Worker> call(ListView<Worker> listView) {
                return new WorkerCell(listView.getItems());
            }
        });

    }
    
    /*
    to be used by the workersListView on the left of the screen
    should only be needed for when the user is dropping a worker
    on the listView that has been dragged from one of the teams
    */
    private class WorkersListViewDragDropHandler implements EventHandler<DragEvent> {


        @Override
        public void handle(DragEvent event) {

            LocalDragboard ldb = LocalDragboard.getInstance();
            if (ldb.hasType(Worker.class)) {
                Worker worker = ldb.getValue(Worker.class);

                if(!workersListView.getItems().contains(worker)) {
                    segmentPaneControllers.get(currentSegmentNumber.intValue()).removeWorker(worker);
                    workersListView.getItems().add(worker);
                }
                
                

                updateLabels();
                segmentPaneControllers.get(currentSegmentNumber.intValue()).updateLabels();
                updateEvent();

                //Clear, otherwise we end up with the worker stuck on the dragboard?
                ldb.clearAll();

            }
        }

        }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;

    }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;

        initializeMore();
    }

}
