package masystem;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class StateTest {

    char[][] boxes = new char[2][2];
    ArrayList<Agent> agents = new ArrayList<>();
    State state = new State(null, boxes, agents);

    @Before
    public void setUp() throws Exception {
        agents.add(new Agent(1,1,1));
    }

    @org.junit.Test
    public void isInitialState() {
        boolean isInitial = state.isInitialState();
        assertTrue(isInitial);
    }

    @Test
    public void isNotInitialState() {
        state.parent = new State(null, boxes, agents);
        boolean isInitial = state.isInitialState();
        assertFalse(isInitial);
    }

    @org.junit.Test
    public void isGoalState() {
    }

    @org.junit.Test
    public void calcExpandedStates() {
    }

    @org.junit.Test
    public void isValidCommand() {
    }
}