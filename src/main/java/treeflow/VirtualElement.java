package treeflow;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

abstract class VirtualElement {

    protected final String name;
    protected final ImageView image;

    public VirtualElement(String name) {
        this.name = name;
        this.image = new ImageView();
        this.image.setPreserveRatio(true);
        this.image.setSmooth(false);
    }

    public String getName() {
        return name;
    }

    public ImageView getImage() {
        return image;
    }

    @Override
    public String toString() {
        return "<E> " + name;
    }
}

class VirtualFile extends VirtualElement {

    public VirtualFile(String name) {
        super(name);
        String extension = name.substring(name.lastIndexOf('.') + 1);
        URL resource = getClass().getResource("/icons/" + extension + ".png");
        if (resource != null) {
            image.setImage(new Image(resource.toExternalForm()));
        } else {
            image.setImage(new Image(getClass().getResource("/icons/default.png").toExternalForm()));
        }
    }

    @Override
    public String toString() {
        return "<F> " + name;
    }
}

class VirtualDirectory extends VirtualElement {

    private final List<VirtualElement> children = new ArrayList<>();

    public VirtualDirectory(String name) {
        super(name);
        image.setImage(new Image(getClass().getResource("/icons/directory.png").toExternalForm()));
    }

    public List<VirtualElement> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "<D> " + name;
    }
}

class VirtualElementHelper {

    private static final Random rng = new Random();

    private static final String[] FILE_NAMES = {
        "Rechnung.doc", "Seminararbeit.doc", "Ausarbeitung.doc", "Bachelorarbeit.doc",
        "Rechnung.txt", "Bestellung.txt", "Notizen.txt", "Passwörter.txt",
        "Rechnung.pdf", "Seminararbeit.pdf", "Ausarbeitung.pdf", "Masterarbeit.pdf",
        "Strand.png", "Urlaub.png", "Katze.png", "Hund.png",
        "Minecraft.exe", "Steam.exe", "NetBeans.exe", "Eclipse.exe",
        "Firefox.exe", "Thunderbird.exe"
    };

    private static final String[] DIRECTORY_NAMES = {
        "Dateien", "Dokumente", "Bilder",
        "Studium", "Arbeit", "Programme"
    };

    private static final List<VirtualFile> files = new ArrayList<>();

    private static final List<VirtualDirectory> directories = new ArrayList<>();

    private static VirtualFile currentTarget;

    public static void reset() {
        files.clear();
        for (String name : FILE_NAMES) {
            files.add(new VirtualFile(name));
        }
        directories.clear();
        for (String name : DIRECTORY_NAMES) {
            directories.add(new VirtualDirectory(name));
        }
        Collections.shuffle(files, rng);
        Collections.shuffle(directories, rng);
        currentTarget = files.get(rng.nextInt(files.size()));
    }

    private static VirtualFile getRandomFile() {
        if (files.isEmpty()) {
            System.err.println("Error: File Generator Exhausted");
            return null;
        }
        return files.remove(rng.nextInt(files.size()));
    }

    private static VirtualDirectory getRandomDirectory() {
        if (directories.isEmpty()) {
            System.err.println("Error: Directory Generator Exhausted");
            return null;
        }
        return directories.remove(rng.nextInt(directories.size()));
    }

    private static VirtualDirectory generateDirectories(int depth) {
        VirtualDirectory dir = getRandomDirectory();
        if (depth > 0) {
            dir.getChildren().add(generateDirectories(depth - 1));
            dir.getChildren().add(generateDirectories(depth - 1));
            dir.getChildren().add(getRandomFile());
            dir.getChildren().add(getRandomFile());
        } else {
            dir.getChildren().add(getRandomFile());
            dir.getChildren().add(getRandomFile());
            dir.getChildren().add(getRandomFile());
            dir.getChildren().add(getRandomFile());
        }
        return dir;
    }

    public static VirtualFile getCurrentTarget() {
        return currentTarget;
    }

    public static VirtualDirectory generateTree() {
        reset();
        VirtualDirectory root = new VirtualDirectory("");
        root.getChildren().add(generateDirectories(1));
        root.getChildren().add(generateDirectories(1));
        root.getChildren().add(getRandomFile());
        root.getChildren().add(getRandomFile());
        return root;
    }
}
