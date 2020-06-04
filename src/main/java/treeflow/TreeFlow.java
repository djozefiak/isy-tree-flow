package treeflow;

import java.util.Random;
import java.util.Stack;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TreeFlow extends Application {

    Random rng = new Random();
    CSVWriter writer = new CSVWriter();

    private final String participantId = String.format("%08x", rng.nextInt());
    private final SimpleIntegerProperty trial = new SimpleIntegerProperty(0);
    private final SimpleBooleanProperty treeFlow = new SimpleBooleanProperty(false); // rng.nextBoolean()
    private long startTime = 0;
    private long endTime = 0;
    private int actions = 0;

    private final SimpleStringProperty path = new SimpleStringProperty("");
    private final SimpleStringProperty target = new SimpleStringProperty("");

    private VirtualDirectory currentDirectory;
    private final ObservableList<VirtualElement> files = FXCollections.observableArrayList();
    private final Stack<VirtualDirectory> returnPoints = new Stack<>();

    private final ListView<VirtualElement> listView = new ListView<>();

    private final AudioClip clickSound = new AudioClip(getClass().getResource("/media/click_1.wav").toExternalForm());
    private final AudioClip successSound = new AudioClip(getClass().getResource("/media/success.wav").toExternalForm());
    private final AudioClip failureSound = new AudioClip(getClass().getResource("/media/failure.wav").toExternalForm());

    private final BlinkingLabel targetLabel = new BlinkingLabel();

    private Point2D flowPoint = new Point2D(0, 0);

    private final EventHandler<MouseEvent> doubleClickHandler = (event) -> {
        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }
        if (event.getClickCount() != 2) {
            return;
        }
        VirtualElement navigationTarget = listView.getSelectionModel().getSelectedItem();
        openElement(navigationTarget);
    };

    private final EventHandler<ActionEvent> moveUpHandler = (event) -> {
        moveToParent();
    };

    private final EventHandler<MouseEvent> prepareFlowHandler = (event) -> {
        flowPoint = new Point2D(event.getSceneX(), event.getSceneY());
    };

    private final EventHandler<MouseEvent> actionFlowHandler = (event) -> {
        Point2D currentPoint = new Point2D(event.getSceneX(), event.getSceneY());
        double offset = flowPoint.getX() - currentPoint.getX();
        if (offset < -50) {
            listView.setCursor(Cursors.RIGHT);
        } else if (offset > 50) {
            listView.setCursor(Cursors.LEFT);
        }
        if (offset < -100) {
            VirtualElement navigationTarget = listView.getSelectionModel().getSelectedItem();
            openElement(navigationTarget);
            flowPoint = currentPoint;
            listView.setCursor(Cursor.DEFAULT);
        } else if (offset > 100) {
            moveToParent();
            flowPoint = currentPoint;
            listView.setCursor(Cursor.DEFAULT);
        }
    };

    private final EventHandler<MouseEvent> endFlowHandler = (event) -> {
        listView.setCursor(Cursor.DEFAULT);
    };

    private void openElement(VirtualElement element) {
        if (element instanceof VirtualFile) {
            VirtualFile fileTarget = (VirtualFile) element;
            openFile(fileTarget);
        } else if (element instanceof VirtualDirectory) {
            VirtualDirectory directoryTarget = (VirtualDirectory) element;
            openDirectory(directoryTarget);
        }
    }

    private void openFile(VirtualFile file) {
        if (file == VirtualElementHelper.getCurrentTarget()) {
            actions++;
            successSound.play();
            endTrial();
        } else {
            targetLabel.blink();
            failureSound.play();
        }
    }

    private void openDirectory(VirtualDirectory directory) {
        returnPoints.push(currentDirectory);
        currentDirectory = directory;
        files.setAll(directory.getChildren());
        path.set(currentPath());
        listView.getSelectionModel().clearSelection();
        actions++;
        clickSound.play();
    }

    private void moveToParent() {
        if (returnPoints.empty()) {
            return;
        }
        VirtualDirectory parent = returnPoints.pop();
        currentDirectory = parent;
        files.setAll(parent.getChildren());
        path.set(currentPath());
        actions++;
        clickSound.play();
    }

    private String currentPath() {
        StringBuilder sb = new StringBuilder();
        for (VirtualDirectory dir : returnPoints) {
            sb.append(dir.getName());
            sb.append("/");
        }
        sb.append(currentDirectory.getName());
        sb.append("/");
        return sb.toString();
    }

    private void startTrial() {
        trial.set(trial.get() + 1);
        treeFlow.set(!treeFlow.get());
        startTime = System.currentTimeMillis();
        endTime = 0;
        actions = 0;

        // generate file tree
        VirtualDirectory root = VirtualElementHelper.generateTree();
        currentDirectory = root;
        returnPoints.clear();
        files.setAll(root.getChildren());
        target.set(VirtualElementHelper.getCurrentTarget().getName());
        path.set(currentPath());
    }

    private void endTrial() {
        endTime = System.currentTimeMillis();
        long time = endTime - startTime;

        writer.write(participantId, trial.get(), treeFlow.get(), time, actions);

        if (trial.get() >= 20) {
            writer.close();
            Dialogs.showEndDialog();
            System.exit(0);
        } else {
            startTrial();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // header with participant id, trial number and current method
        Label participantIdLabel = new Label("Participant: " + participantId);
        Label trialLabel = new Label();
        trialLabel.textProperty().bind(Bindings.concat("Trial: ").concat(trial));
        Label treeFlowLabel = new Label();
        treeFlowLabel.textProperty().bind(Bindings.concat("Method: ")
                .concat(Bindings.when(treeFlow).then("TreeFlow").otherwise("DoubleClick")));

        HBox infoHeader = new HBox(participantIdLabel, trialLabel, treeFlowLabel);

        // button for moving up a level, current path display and search target
        Button moveUp = new Button("\u2191");
        moveUp.setFocusTraversable(false);
        moveUp.disableProperty().bind(treeFlow);
        Label pathLabel = new Label();
        pathLabel.textProperty().bind(Bindings.concat("Path: ").concat(path));
        targetLabel.textProperty().bind(Bindings.concat("Target: ").concat(target));

        HBox pathHeader = new HBox(moveUp, pathLabel, targetLabel);

        // shows files and directories in the current path
        listView.setFocusTraversable(false);
        listView.setCellFactory(lv -> {
            ListCell<VirtualElement> cell = new ListCell<VirtualElement>() {
                @Override
                public void updateItem(VirtualElement element, boolean empty) {
                    super.updateItem(element, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(element.toString());
                    }
                }
            };
            cell.hoverProperty().addListener((observable, wasHovered, isHovered) -> {
                if (isHovered && !cell.isEmpty()) {
                    listView.getSelectionModel().select(cell.getItem());
                }
            });

            return cell;
        });

        listView.setItems(files);
        VBox.setVgrow(listView, Priority.ALWAYS);

        // main contains all previously defined elements
        VBox main = new VBox(infoHeader, pathHeader, listView);
        Scene scene = new Scene(main, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/css/TreeFlow.css").toExternalForm());

        // add event handler for double click on a list element
        listView.setOnMouseClicked(doubleClickHandler);
        listView.setOnMousePressed(prepareFlowHandler);
        listView.setOnMouseDragged(actionFlowHandler);
        listView.setOnMouseReleased(endFlowHandler);

        // add event handler for moving up a level
        moveUp.setOnAction(moveUpHandler);

        primaryStage.setResizable(false);
        primaryStage.setTitle("TreeFlow");
        primaryStage.setScene(scene);
        primaryStage.show();
        // Dialogs.showStartDialog();
        startTrial();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
