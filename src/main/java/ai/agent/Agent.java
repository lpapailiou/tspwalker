package ai.agent;

import javafx.animation.Timeline;
import ui.State;

public abstract class Agent {

    protected final State state = State.getInstance();
    protected Timeline timeline = null;
    final protected int delay;
    abstract void run();

    public Agent() {
        this.delay = state.getDelay();
    }


}
