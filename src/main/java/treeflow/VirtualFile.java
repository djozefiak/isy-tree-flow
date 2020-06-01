package treeflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class VirtualFileHelper {

    private static final Random rng = new Random();

    private static final String[] FILE_NAMES = {
        "Rechnung.docx", "Seminararbeit.docx", "Ausarbeitung.docx", "Bachelorarbeit.docx",
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

    public static String getRandomFileName() {
        return FILE_NAMES[rng.nextInt(FILE_NAMES.length)];
    }

    public static String getRandomDirectoryName() {
        return DIRECTORY_NAMES[rng.nextInt(DIRECTORY_NAMES.length)];
    }
}

class VirtualFile {

    private final String name;
    private final boolean isDirectory;
    private final List<VirtualFile> children = new ArrayList<>();

    public VirtualFile(String name, boolean isDirectory) {
        this.name = name;
        this.isDirectory = isDirectory;
    }

    public VirtualFile() {
        this.name = VirtualFileHelper.getRandomFileName();
        this.isDirectory = false;
    }

    private VirtualFile(int depth) {
        this.name = VirtualFileHelper.getRandomDirectoryName();
        this.isDirectory = true;
        if (depth <= 0) {
            children.add(new VirtualFile());
            children.add(new VirtualFile());
            children.add(new VirtualFile());
            children.add(new VirtualFile());
        } else {
            children.add(new VirtualFile(depth - 1));
            children.add(new VirtualFile(depth - 1));
            children.add(new VirtualFile(depth - 1));
            children.add(new VirtualFile(depth - 1));
        }
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public List<VirtualFile> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return (isDirectory ? "D" : "F") + " " + name;
    }

    public static VirtualFile generateRoot() {
        VirtualFile root = new VirtualFile("ROOT", true);
        root.getChildren().add(new VirtualFile());
        root.getChildren().add(new VirtualFile());
        root.getChildren().add(new VirtualFile(1));
        root.getChildren().add(new VirtualFile(1));
        return root;
    }
}
