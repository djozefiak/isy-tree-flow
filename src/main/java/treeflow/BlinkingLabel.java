package treeflow;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class BlinkingLabel extends Label {

    private static final PseudoClass blink = PseudoClass.getPseudoClass("blink");
    private final Timeline blinkTimeline;

    public BlinkingLabel() {
        super();
        blinkTimeline = new Timeline(
                new KeyFrame(Duration.millis(100), e -> {
                    this.pseudoClassStateChanged(blink, true);
                }),
                new KeyFrame(Duration.millis(200), e -> {
                    this.pseudoClassStateChanged(blink, false);
                })
        );
        blinkTimeline.setCycleCount(4);
    }

    public BlinkingLabel(String text) {
        this();
        this.setText(text);
    }

    public void blink() {
        blinkTimeline.playFromStart();
    }

}
