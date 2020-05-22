package IIOO.masystem.centralized;


import IIOO.masystem.BestFirstStrategy;
import IIOO.masystem.State;

import java.util.ArrayList;

public class SearchClient {
    public final State initialState;

    public SearchClient(State state) {
        this.initialState = state;
    }

    public ArrayList<State> Search(BestFirstStrategy bestFirstStrategy) {

        System.err.format("Search starting with bestFirstStrategy %s.\n", bestFirstStrategy.toString());

        bestFirstStrategy.addToFrontier(this.initialState);
        while (true) {

            if (bestFirstStrategy.frontierIsEmpty()) {
                return null;
            }

            State leafState = bestFirstStrategy.getAndRemoveLeaf();

            if (leafState.isGoalState()) {
                return leafState.extractPlan();
            }

            bestFirstStrategy.addToExplored(leafState);

            for (State n : leafState.getExpandedStates()) { // The list of expanded states is shuffled randomly; see
                if (!bestFirstStrategy.isExplored(n) && !bestFirstStrategy.inFrontier(n)) {
                    bestFirstStrategy.addToFrontier(n);
                }
            }
        }
    }
}
