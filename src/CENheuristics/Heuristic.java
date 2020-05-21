package CENheuristics;

import CENmasystem.State;


public abstract class Heuristic {
    public Heuristic(State initialState) {
        // Here's a chance to pre-process the static parts of the level.
    }

    public abstract int h(State n);
    //TODO: Things to consider for heuristics:
    /*
    -
    -


     */
    
}
