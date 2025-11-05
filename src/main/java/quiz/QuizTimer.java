package quiz;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

public class QuizTimer {
    private final IntegerProperty timeRemaining = new SimpleIntegerProperty();
    private Timeline timeline;

    public void start(int seconds, Runnable onFinish) {
        stop();
        timeRemaining.set(seconds);
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining.set(timeRemaining.get() - 1);
            if (timeRemaining.get() <= 0) {
                stop();
                onFinish.run();
            }
        }));
        timeline.setCycleCount(seconds);
        timeline.play();
    }

    public void stop() {
        if (timeline != null) timeline.stop();
    }

    public IntegerProperty timeRemainingProperty() {
        return timeRemaining;
    }
}
