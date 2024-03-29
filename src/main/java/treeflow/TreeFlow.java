// Interaktive Systeme, Sommersemester 2020, David Jozefiak, TreeFlowStudy
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
    private final SimpleBooleanProperty treeFlow = new SimpleBooleanProperty(rng.nextBoolean());
    private long startTime = 0;
    private long endTime = 0;
    private int actions = 0;

    private final SimpleStringProperty path = new SimpleStringProperty("");
    private final SimpleStringProperty target = new SimpleStringProperty("");

    private VirtualDirectory currentDirectory;
    private final ObservableList<VirtualElement> files = FXCollections.observableArrayList();
    private final Stack<VirtualDirectory> returnPoints = new Stack<>();

    private final ListView<VirtualElement> listView = new ListView<>();

    private final AudioClip clickSound = new AudioClip(getClass().getResource("/sounds/click.wav").toExternalForm());
    private final AudioClip successSound = new AudioClip(getClass().getResource("/sounds/success.wav").toExternalForm());
    private final AudioClip failureSound = new AudioClip(getClass().getResource("/sounds/failure.wav").toExternalForm());

    private final BlinkingLabel targetLabel = new BlinkingLabel();

    private Point2D flowPoint = new Point2D(0, 0);
    private boolean flowAllowed = true;

    private final EventHandler<MouseEvent> doubleClickHandler = (event) -> {
        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }
        if (event.getClickCount() != 2) {
            return;
        }
        VirtualElement navigationTarget = listView.getSelectionModel().getSelectedItem();
        if (treeFlow.get() && navigationTarget instanceof VirtualDirectory) {
            return;
        }
        int nextItem = listView.getSelectionModel().getSelectedIndex();
        openElement(navigationTarget);
        listView.getSelectionModel().select(nextItem);
    };

    private final EventHandler<ActionEvent> moveUpHandler = (event) -> {
        moveToParent();
    };

    private final EventHandler<MouseEvent> prepareFlowHandler = (event) -> {
        if (!treeFlow.get() || !flowAllowed) {
            return;
        }
        flowPoint = new Point2D(event.getSceneX(), event.getSceneY());
    };

    private final EventHandler<MouseEvent> actionFlowHandler = (event) -> {
        if (!treeFlow.get() || !flowAllowed) {
            return;
        }

        Point2D currentPoint = new Point2D(event.getSceneX(), event.getSceneY());
        double offset = flowPoint.getX() - currentPoint.getX();
        VirtualElement navigationTarget = listView.getSelectionModel().getSelectedItem();

        if (offset >= -30 && offset <= 30) {
            listView.setCursor(Cursor.DEFAULT);
        }

        if (offset < -30) {
            if (navigationTarget instanceof VirtualFile) {
                listView.setCursor(Cursor.DEFAULT);
                return;
            }
            listView.setCursor(Cursors.RIGHT);
        } else if (offset > 30) {
            if (returnPoints.empty()) {
                listView.setCursor(Cursor.DEFAULT);
                return;
            }
            listView.setCursor(Cursors.LEFT);
        }

        if (offset < -100) {
            int nextFlow = listView.getSelectionModel().getSelectedIndex();
            openElement(navigationTarget);
            listView.setCursor(Cursor.DEFAULT);
            listView.getSelectionModel().select(nextFlow);
            flowAllowed = false;
        } else if (offset > 100) {
            int nextFlow = listView.getSelectionModel().getSelectedIndex();
            moveToParent();
            listView.setCursor(Cursor.DEFAULT);
            listView.getSelectionModel().select(nextFlow);
            flowAllowed = false;
        }
    };

    private final EventHandler<MouseEvent> endFlowHandler = (event) -> {
        if (!treeFlow.get()) {
            return;
        }
        listView.setCursor(Cursor.DEFAULT);
        flowAllowed = true;
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
        actions++;
        if (file == VirtualElementHelper.getCurrentTarget()) {
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
        // generate file tree
        VirtualDirectory root = VirtualElementHelper.generateTree();
        currentDirectory = root;
        returnPoints.clear();
        files.setAll(root.getChildren());
        target.set(VirtualElementHelper.getCurrentTarget().getName());
        path.set(currentPath());

        trial.set(trial.get() + 1);
        treeFlow.set(!treeFlow.get());
        endTime = 0;
        actions = 0;

        Dialogs.showTrialDialog(treeFlow.get());

        startTime = System.currentTimeMillis();
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
                        setGraphic(null);
                    } else {
                        setText(element.getName());
                        setGraphic(element.getImage());
                    }
                }
            };

            cell.hoverProperty().addListener((observable, wasHovered, isHovered) -> {
                if (wasHovered && !cell.isEmpty()) {
                    listView.getSelectionModel().clearSelection();
                }
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
        scene.getStylesheets().add(getClass().getResource("/stylesheets/TreeFlow.css").toExternalForm());

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
        Dialogs.showStartDialog();
        startTrial();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
