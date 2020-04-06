package heuristics;

import masystem.State;


public abstract class Heuristic {
    public Heuristic(State initialState) {
        // Here's a chance to pre-process the static parts of the level.
    }

    public abstract int h(State n);
    
}