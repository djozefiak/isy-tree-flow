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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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

    private VirtualFile currentFile;
    private final ObservableList<VirtualFile> files = FXCollections.observableArrayList();
    private final Stack<VirtualFile> returnPoints = new Stack<>();

    private final ListView<VirtualFile> listView = new ListView<>();

    private final EventHandler<MouseEvent> doubleClickHandler = (event) -> {
        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }
        if (event.getClickCount() != 2) {
            return;
        }
        VirtualFile navigationTarget = listView.getSelectionModel().getSelectedItem();
        if (navigationTarget.isDirectory()) {
            files.setAll(navigationTarget.getChildren());
            returnPoints.push(currentFile);
            currentFile = navigationTarget;
            listView.getSelectionModel().clearSelection();
        } else {
            System.out.println("Reached file: " + navigationTarget);
        }
    };

    private final EventHandler<ActionEvent> moveUpHandler = (event) -> {
        if (returnPoints.empty()) {
            return;
        }
        VirtualFile parent = returnPoints.pop();
        currentFile = parent;
        files.setAll(parent.getChildren());
    };

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
        Label pathLabel = new Label();
        pathLabel.textProperty().bind(Bindings.concat("Path: ").concat(path));
        Label targetLabel = new Label();
        targetLabel.textProperty().bind(Bindings.concat("Target: ").concat(target));

        HBox pathHeader = new HBox(moveUp, pathLabel, targetLabel);

        // shows files and directories in the current path
        listView.setFocusTraversable(false);
        listView.setItems(files);
        VBox.setVgrow(listView, Priority.ALWAYS);

        // main contains all previously defined elements
        VBox main = new VBox(infoHeader, pathHeader, listView);
        Scene scene = new Scene(main, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/css/TreeFlow.css").toExternalForm());

        // generate file tree
        VirtualFile root = VirtualFile.generateRoot();
        files.setAll(root.getChildren());
        currentFile = root;

        // add event handler for double click on a list element
        listView.setOnMouseClicked(doubleClickHandler);

        // add event handler for moving up a level
        moveUp.setOnAction(moveUpHandler);

        primaryStage.setResizable(false);
        primaryStage.setTitle("TreeFlow");
        primaryStage.setScene(scene);
        primaryStage.show();
        // Dialogs.showStartDialog();
        // startTrial();
    }

    public void startTrial() {
        trial.set(trial.get() + 1);
        treeFlow.set(!treeFlow.get());
        startTime = System.currentTimeMillis();
        endTime = 0;
        actions = 0;
    }

    public void endTrial() {
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

    public static void main(String[] args) {
        launch(args);
    }

}
