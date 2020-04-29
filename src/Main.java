import heuristics.BoxesDistanceToGoal;
import heuristics.PCDWithMaximizeDistToOtherColors;
import heuristics.PreCalcDistForCompleteMap;
import heuristics.PreCalculatedDistances;
import masystem.BestFirstStrategy;
import masystem.SearchClient;
import masystem.State;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws Exception {
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));

        // Use stderr to print to console
        System.err.println("DEBUG BY THESE PRINTS");

        // Client name statement
        System.out.println("PearEasy Client");

        // Read level and create the initial state of the problem
        SearchClient client = new SearchClient(serverMessages);

        //BestFirstStrategy bestFirstStrategy = new BestFirstStrategy(new BoxesDistanceToGoal(client.initialState));
        BestFirstStrategy bestFirstStrategy = new BestFirstStrategy(new PCDWithMaximizeDistToOtherColors(client.initialState));

        ArrayList<State> solution;
        try {
            solution = client.Search(bestFirstStrategy);
        } catch (OutOfMemoryError ex) {
            System.err.println("Maximum memory usage exceeded.");
            solution = null;
        }

        if (solution == null) {
            System.err.println(bestFirstStrategy.searchStatus());
            System.err.println("Unable to solve level.");
            System.exit(0);
        } else {
            System.err.println("\nSummary for " + bestFirstStrategy.toString());
            System.err.println("Found solution of length " + solution.size() + ". " + bestFirstStrategy.searchStatus());

            for (State n : solution) {
                String act = n.actions.toString();

                StringBuilder actions = new StringBuilder();

                for (int i = 0; i < n.actions.size(); i++) {
                    actions.append(n.actions.get(i).toString());
                    actions.append(";");

                }

                actions.replace(actions.length() - 1, actions.length(), "");

                System.out.println(actions);

                String response = serverMessages.readLine();
                if (response != null && response.contains("false")) {
                    System.err.format("Server responsed with %s to the inapplicable action: %s\n", response, act);
                    System.err.format("%s was attempted in \n%s\n", act, n.toString());
                    break;
                }
            }
        }
    }
}
