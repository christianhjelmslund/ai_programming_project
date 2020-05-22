package IIOO.masystem.heuristics;


import IIOO.masystem.State;

public abstract class Heuristic {
    public Heuristic() {
        // Here's a chance to pre-process the static parts of the level.
    }

    public abstract int h(State n);
    
}
