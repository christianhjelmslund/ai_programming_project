package CENmasystem;

import java.util.ArrayList;

public class SearchClient {
    public State initialState;

    public SearchClient(State state) throws Exception {
        this.initialState = state;
    }



    public ArrayList<State> Search(BestFirstStrategy bestFirstStrategy) {

        System.err.format("Search starting with bestFirstStrategy %s.\n", bestFirstStrategy.toString());

        bestFirstStrategy.addToFrontier(this.initialState);


        int iterations = 0;
        while (true) {

            if (bestFirstStrategy.frontierIsEmpty()) {
                return null;
            }

            State leafState = bestFirstStrategy.getAndRemoveLeaf();

            if (iterations == 1000) {


                System.err.println(bestFirstStrategy.searchStatus());
                System.err.println(leafState.toString());
                System.err.println("Heuristic value: " + bestFirstStrategy.heuristic.h(leafState));

                //PCDMergeRefactored heu = (PCDMergeRefactored) bestFirstStrategy.heuristic;
                //System.err.println(heu.getDistsToAssignedGoals(leafState));
                // System.err.println("Heuristic value: " + bestFirstStrategy.heuristic.h(leafState));
                // PCDMergeTaskOriented heu = (PCDMergeTaskOriented) bestFirstStrategy.heuristic;
                // System.err.println(heu.getDistsToAssignedGoals(leafState));
                iterations = 0;
            }

            boolean solutionF = false;


            if (leafState.isGoalState(solutionF)) {
                return leafState.extractPlan();
            }

            bestFirstStrategy.addToExplored(leafState);

            /*
            int optiHeu = 1000;

            if (iterations == 500) {
                System.err.println("Current State: ");
                System.err.println(leafState.toString());
                System.err.println("Heuristic value: " + bestFirstStrategy.heuristic.h(leafState));
            }


             */


            for (State n : leafState.getExpandedStates()) { // The list of expanded states is shuffled randomly; see

                if (!bestFirstStrategy.isExplored(n) && !bestFirstStrategy.inFrontier(n)) {

                    /*

                    if (iterations == 500 && bestFirstStrategy.heuristic.h(n) < optiHeu) {
                        optiHeu = bestFirstStrategy.heuristic.h(n);
                        System.err.println("Expanded States: ");
                        System.err.println(n.toString());
                        System.err.println("H: " + bestFirstStrategy.heuristic.h(n));
                        System.err.println("____________________");
                    }

                     */

                    bestFirstStrategy.addToFrontier(n);
                }
            }
            iterations++;
        }
    }

}
