package masystem;

import heuristics.BoxesDistanceToGoal;
import heuristics.Heuristic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SearchClient {
    public State initialState;
    public State stateHolder;

    public SearchClient(BufferedReader serverMessages) throws Exception {
        // Read lines specifying colors
        String line = serverMessages.readLine();
        if (line.matches("^[a-z]+:\\s*[0-9A-Z](\\s*,\\s*[0-9A-Z])*\\s*$")) {
            System.err.println("Error, client does not support colors.");
            System.exit(1);
        }
        int column = 0;
        int row = 0;

        boolean agentFound = false;

        this.stateHolder = new State(null);

        while (!line.equals("")) {
            // find the longest line, e.g. the max length in the x-direction)
            if (line.length() > column) {
                column = line.length();
            }

            for (int col = 0; col < line.length(); col++) {
                char chr = line.charAt(col);
                if (chr == '+') { // Wall.
                    this.stateHolder.walls[row][col] = true;
                } else if ('0' <= chr && chr <= '9') { // masystem.Agent.
                    if (agentFound) {
                        System.err.println("Error, not a single agent level");
                        System.exit(1);
                    }
                    agentFound = true;
                    this.stateHolder.agents[0].row = row;
                    this.stateHolder.agents[0].column = col;
                } else if ('A' <= chr && chr <= 'Z') { // Box.
                    this.stateHolder.boxes[row][col] = chr;
                } else if ('a' <= chr && chr <= 'z') { // Goal.
                    this.stateHolder.goals[row][col] = chr;
                } else if (chr == ' ') {
                    // Free space.
                } else {
                    System.err.println("Error, read invalid level character: " + (int) chr);
                    System.exit(1);
                }
            }
            line = serverMessages.readLine();

            // counts the number of rows
            row++;
        }

        // Create new initial state
        // The walls and goals are static, so no need to initialize the arrays every
        // time
        State.MAX_ROW = row;
        State.MAX_COL = column;

        this.initialState = new State(null);
        this.initialState.boxes = this.stateHolder.boxes;
        this.initialState.agents[0].column = this.stateHolder.agents[0].column;
        this.initialState.agents[0].row = this.stateHolder.agents[0].row;
    }

    public ArrayList<State> Search(BestFirstStrategy bestFirstStrategy) {
        System.err.format("Search starting with bestFirstStrategy %s.\n", bestFirstStrategy.toString());
        bestFirstStrategy.addToFrontier(this.initialState);

        int iterations = 0;
        while (true) {
            if (iterations == 1000) {
                System.err.println(bestFirstStrategy.searchStatus());
                iterations = 0;
            }

            if (bestFirstStrategy.frontierIsEmpty()) {
                return null;
            }

            State leafState = bestFirstStrategy.getAndRemoveLeaf();

            if (leafState.isGoalState()) {
                return leafState.extractPlan();
            }

            bestFirstStrategy.addToExplored(leafState);
            for (State n : leafState.getExpandedStates()) { // The list of expanded states is shuffled randomly; see
                                                            // masystem.State.java.
                if (!bestFirstStrategy.isExplored(n) && !bestFirstStrategy.inFrontier(n)) {
                    bestFirstStrategy.addToFrontier(n);
                }
            }
            iterations++;
        }
    }

}
