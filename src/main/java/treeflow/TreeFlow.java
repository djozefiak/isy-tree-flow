package treeflow;

import java.util.Random;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Alert;
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
    private final ObservableList<VirtualFile> files = FXCollections.observableArrayList();

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

        // root contains all previously defined elements
        VBox root = new VBox(infoHeader, pathHeader, listView);
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/css/TreeFlow.css").toExternalForm());

        // populate file tree
        files.setAll(VirtualFile.generateRoot().getChildren());

        // add event handler for double click on a list element
        listView.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                if (event.getClickCount() == 2) {
                    System.out.println(listView.getSelectionModel().getSelectedItem());
                    listView.getSelectionModel().clearSelection();
                }
            }
        });

        primaryStage.setResizable(false);
        primaryStage.setTitle("TreeFlow");
        primaryStage.setScene(scene);
        primaryStage.show();
        // showStartDialog();
        // startTrial();
    }

    private void showStartDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("TreeFlow");
        alert.setHeaderText("In dieser Studie möchte ich das Navigieren in einem Verzeichnisbaum untersuchen.");
        alert.setContentText("Dafür werden folgende Methoden untersucht:\n\n"
                + "A) DoubleClick: Übliches Navigieren\n"
                + "- Ein Ordner kann mit einem Doppelklick geöffnet werden.\n"
                + "- Ein Button über der Ordneransicht führt zurück in den übergeordneten Ordner.\n\n"
                + "B) TreeFlow: Steuerung mit Mausgesten\n"
                + "- Ein Ordner kann durch das Klicken, Halten und nach Rechts ziehen geöffnet werden.\n"
                + "- Der übergeordnete Ordner kann durch das Klicken, Halten und nach Links ziehen geöffnet werden.\n"
                + "- Während der Navigation kann die linke Maustaste die ganze Zeit gehalten werden, um die Gesten zu verketten.\n\n"
                + "Bei beiden Methoden wird eine Datei durch einen Doppelklick geöffnet.\n\n"
                + "Aufgabenbeschreibung:\n\n"
                + "- Ziel ist es, eine Datei im Verzeichnisbaum zu finden. Dafür wird der aktuelle Pfad und der gesuchte Dateiname angezeigt.\n"
                + "- Nun soll mit den beiden Methoden im Verzeichnisbaum nach der Datei gesucht werden und diese anschließend mit einem Doppelklick geöffnet werden.");
        alert.showAndWait();
    }

    private void showEndDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("TreeFlow");
        alert.setHeaderText("Damit ist die Studie beendet.");
        alert.setContentText("Vielen Dank für deine Teilnahme!");
        alert.showAndWait();
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
            showEndDialog();
            System.exit(0);
        } else {
            startTrial();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
