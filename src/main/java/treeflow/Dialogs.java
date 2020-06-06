package treeflow;

import javafx.scene.control.Alert;

public class Dialogs {

    private static final String DOUBLE_CLICK_DESCRIPTION = "A) DoubleClick: Übliches Navigieren\n"
            + "- Ein Ordner kann mit einem Doppelklick geöffnet werden.\n"
            + "- Ein Button über der Ordneransicht führt zurück in den übergeordneten Ordner.\n";

    private static final String TREE_FLOW_DESCRIPTION = "B) TreeFlow: Steuerung mit Mausgesten\n"
            + "- Ein Ordner kann durch das Klicken, Halten und nach Rechts ziehen geöffnet werden.\n"
            + "- Der übergeordnete Ordner kann durch das Klicken, Halten und nach Links ziehen geöffnet werden.\n";

    public static void showTrialDialog(boolean treeFlow) {
        String method = treeFlow ? "TreeFlow" : "DoubleClick";
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Nächste Methode: " + method);
        alert.setHeaderText("Als nächstes kommt die Methode " + method + " zum Einsatz.");
        alert.setContentText(treeFlow ? TREE_FLOW_DESCRIPTION : DOUBLE_CLICK_DESCRIPTION);
        alert.showAndWait();
    }

    public static void showStartDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("TreeFlow");
        alert.setHeaderText("In dieser Studie möchte ich das Navigieren in einem Verzeichnisbaum untersuchen.");
        alert.setContentText("Dafür werden folgende Methoden untersucht:\n\n"
                + DOUBLE_CLICK_DESCRIPTION + "\n"
                + TREE_FLOW_DESCRIPTION + "\n"
                + "Bei beiden Methoden wird eine Datei durch einen Doppelklick geöffnet.\n\n"
                + "Aufgabenbeschreibung:\n\n"
                + "- Ziel ist es, eine Datei im Verzeichnisbaum zu finden. Dafür wird der aktuelle Pfad und der gesuchte Dateiname angezeigt.\n"
                + "- Nun soll mit den beiden Methoden im Verzeichnisbaum nach der Datei gesucht werden und diese anschließend mit einem Doppelklick geöffnet werden.\n\n"
                + "Wichtig: Die Ordnernamen und Dateinamen sind zufällig zugeordnet, das bedeutet, dass im Ordner \"Dokumente\" auch Bilder enthalten sein können.\n");
        alert.showAndWait();
    }

    public static void showEndDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("TreeFlow");
        alert.setHeaderText("Damit ist die Studie beendet.");
        alert.setContentText("Vielen Dank für deine Teilnahme!");
        alert.showAndWait();
    }
}
