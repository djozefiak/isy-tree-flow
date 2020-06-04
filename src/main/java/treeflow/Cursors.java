package treeflow;

import javafx.scene.ImageCursor;
import javafx.scene.image.Image;

public class Cursors {

    public static final ImageCursor UP = new ImageCursor(
            new Image(Cursors.class.getResource("/graphics/up.png").toExternalForm()),
            255, 5);
    public static final ImageCursor RIGHT = new ImageCursor(
            new Image(Cursors.class.getResource("/graphics/right.png").toExternalForm()),
            506, 255);
    public static final ImageCursor DOWN = new ImageCursor(
            new Image(Cursors.class.getResource("/graphics/down.png").toExternalForm()),
            255, 506);
    public static final ImageCursor LEFT = new ImageCursor(
            new Image(Cursors.class.getResource("/graphics/left.png").toExternalForm()),
            5, 255);
    public static final ImageCursor HORIZONTAL = new ImageCursor(
            new Image(Cursors.class.getResource("/graphics/horizontal.png").toExternalForm()),
            255, 255);
    public static final ImageCursor VERTICAL = new ImageCursor(
            new Image(Cursors.class.getResource("/graphics/vertical.png").toExternalForm()),
            255, 255);
}
