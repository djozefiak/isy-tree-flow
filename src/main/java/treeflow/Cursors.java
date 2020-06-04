package treeflow;

import javafx.scene.ImageCursor;
import javafx.scene.image.Image;

public class Cursors {

    public static final ImageCursor UP = new ImageCursor(
            new Image(Cursors.class.getResource("/media/up.png").toExternalForm()),
            255, 5);
    public static final ImageCursor RIGHT = new ImageCursor(
            new Image(Cursors.class.getResource("/media/right.png").toExternalForm()),
            506, 255);
    public static final ImageCursor DOWN = new ImageCursor(
            new Image(Cursors.class.getResource("/media/down.png").toExternalForm()),
            255, 506);
    public static final ImageCursor LEFT = new ImageCursor(
            new Image(Cursors.class.getResource("/media/left.png").toExternalForm()),
            5, 255);
    public static final ImageCursor HORIZONTAL = new ImageCursor(
            new Image(Cursors.class.getResource("/media/horizontal.png").toExternalForm()),
            255, 255);
    public static final ImageCursor VERTICAL = new ImageCursor(
            new Image(Cursors.class.getResource("/media/vertical.png").toExternalForm()),
            255, 255);
}
