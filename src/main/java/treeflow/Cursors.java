package treeflow;

import javafx.scene.ImageCursor;
import javafx.scene.image.Image;

public class Cursors {

    public static final ImageCursor RIGHT = new ImageCursor(
            new Image(Cursors.class.getResource("/cursors/right.png").toExternalForm()),
            506, 255);

    public static final ImageCursor LEFT = new ImageCursor(
            new Image(Cursors.class.getResource("/cursors/left.png").toExternalForm()),
            5, 255);

}
