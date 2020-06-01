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

    private final SimpleStringProperty target = new SimpleStringProperty("");
    private final SimpleStringProperty path = new SimpleStringProperty("");

    private VirtualFile currentFile;
    private final ObservableList<VirtualFile> files = FXCollections.observableArrayList();
    private final Stack<VirtualFile> returnPoints = new Stack<>();

    @Override
    public void start(Stage primaryStage) {
        // header with participant id, trial number and current method
        Label l_participantId = new Label("Participant: " + participantId);
        Label l_trial = new Label();
        l_trial.textProperty().bind(Bindings.concat("Trial: ").concat(trial));
        Label l_treeFlow = new Label();
        l_treeFlow.textProperty().bind(Bindings.concat("Method: ")
                .concat(Bindings.when(treeFlow).then("TreeFlow").otherwise("DoubleClick")));

        HBox infoHeader = new HBox(l_participantId, l_trial, l_treeFlow);

        // button for moving up a level, current path display and search target
        Button moveUp = new Button("\u2191");
        moveUp.setFocusTraversable(false);
        Label l_path = new Label();
        l_path.textProperty().bind(Bindings.concat("Path: ").concat(path));
        Label l_target = new Label();
        l_target.textProperty().bind(Bindings.concat("Target: ").concat(target));

        HBox pathHeader = new HBox(moveUp, l_path, l_target);

        // shows files and directories in the current path
        ListView<VirtualFile> listView = new ListView<>();
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
        listView.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
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
        });

        // add event handler for double click on a list element
        moveUp.setOnAction((event) -> {
            if (returnPoints.empty()) {
                return;
            }
            VirtualFile parent = returnPoints.pop();
            currentFile = parent;
            files.setAll(parent.getChildren());
        });

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

        writer.write(participantId, trial.get(), treeFlow.get() ? 1 : 0, time, actions);

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
