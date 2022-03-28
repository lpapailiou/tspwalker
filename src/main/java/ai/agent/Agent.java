package ai.agent;

import javafx.animation.Timeline;
import ui.State;

public abstract class Agent {

    protected final State state = State.getInstance();
    protected final Timeline timeline = state.getTimeline();

}
