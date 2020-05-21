import CENmasystem.*;
import DECmasystem.DecentralizedSearch;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Main {


    public static void main(String[] args) throws Exception {

        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in));

        // Use stderr to print to console
        System.err.println("DEBUG BY THESE PRINTS");

        // Client name statement
        System.out.println("PearEasy Client");

        State initialState = loadInitialState(serverMessages);

        boolean centralized = !(initialState.agents.length >= 7 && initialState.MAX_ROW*initialState.MAX_COL >= 105);



        SearchClient cenClient = null;
        DecentralizedSearch decClient = null;

        CENmasystem.BestFirstStrategy cenBestFirstStrategy = null;
        DECmasystem.BestFirstStrategy decBestFirstStrategy = null;


        //___________________________________CENTRALIZED____________________________________________________

        // Read level and create the initial state of the problem
        if (centralized) {
            cenClient = new SearchClient(initialState);
            cenBestFirstStrategy = new CENmasystem.BestFirstStrategy(new CENheuristics.PCDMergeRefactored(cenClient.initialState));
            ArrayList<CENmasystem.State> solution;
            try {
                solution = cenClient.Search(cenBestFirstStrategy);
            } catch (OutOfMemoryError ex) {
                System.err.println("Maximum memory usage exceeded.");
                solution = null;
            }

            if (solution == null) {
                System.err.println(cenBestFirstStrategy.searchStatus());
                System.err.println("Unable to solve level.");
                System.exit(0);
            } else {
                System.err.println("\nSummary for " + cenBestFirstStrategy.toString());
                System.err.println("Found solution of length " + solution.size() + ". " + cenBestFirstStrategy.searchStatus());

                for (CENmasystem.State n : solution) {
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







        //___________________________________DECENTRALIZED____________________________________________________

        } else {
            decClient = new DecentralizedSearch(initialState);
            decBestFirstStrategy = new DECmasystem.BestFirstStrategy(new DECheuristics.PCDMergeRefactored(decClient.initialState));

            ArrayList<DECmasystem.State> solution;
            try {
                solution = decClient.Search(decBestFirstStrategy);
            } catch (OutOfMemoryError ex) {
                System.err.println("Maximum memory usage exceeded.");
                solution = null;
            }

            if (solution == null) {
                System.err.println(decBestFirstStrategy.searchStatus(true));
                System.err.println("Unable to solve level.");
                System.exit(0);
            } else {
                System.err.println("\nSummary for " + decBestFirstStrategy.toString());
                System.err.println("Found solution of length " + solution.size() + ". " + decBestFirstStrategy.searchStatus(true));

                for (DECmasystem.State n : solution) {
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

    private static State loadInitialState(BufferedReader serverMessages) throws Exception {
        int maxRow = 70;
        int maxCol = 70;
        boolean[][] walls = new boolean[maxRow][maxCol];


        boolean[] agentsFoundInMap = new boolean[10];
        Map<Character, Integer> boxColors = new HashMap<>();

        Agent[] agents = new Agent[10];
        ArrayList<Goal> agentGoals = new ArrayList<>();
        ArrayList<Goal> boxGoals = new ArrayList<>();
        ArrayList<Box> boxes = new ArrayList<>(); // max number of boxes

        String line = "init";

        while (!line.contains("#colors")) {
            //read lines until reaching colors
            line = serverMessages.readLine();
        }

        //Read first color
        line = serverMessages.readLine();

        //Categorize agents and boxes2dArray into colors
        int color = 0; //Using integer to represent a color for agent and boxes2dArray
        while (line.contains("blue") || line.contains("red") || line.contains("cyan") || line.contains("purple") || line.contains("green")
                || line.contains("orange") || line.contains("pink") || line.contains("grey") || line.contains("lightblue") || line.contains("brown")) {

            //Agents
            for (int i = 0; i < line.length(); i++) {
                if ('0' <= line.charAt(i) && line.charAt(i) <= '9') {
                    Agent agent = new Agent();
                    agent.color = color;
                    agents[Character.getNumericValue(line.charAt(i))] = agent;
                }
            }
            //Boxes
            for (int i = 0; i < line.length(); i++) {
                if ('A' <= line.charAt(i) && line.charAt(i) <= 'Z') {
                    boxColors.put(line.charAt(i), color);
                }
            }
            color++;
            line = serverMessages.readLine();
        }


        //Read initial state
        if (!line.contains("#initial")) {
            System.err.println("Level format is not correct");

        } else {
            line = serverMessages.readLine();

            int row = 0;
            while (!line.contains("#goal")) {

                for (int col = 0; col < line.length(); col++) {
                    char chr = line.charAt(col);
                    if (chr == '+') { // Wall.
                        walls[row][col] = true;
                    } else if ('0' <= chr && chr <= '9') { // Agent
                        int number = Character.getNumericValue(chr);

                        agents[number].row = row;
                        agents[number].column = col;
                        agentsFoundInMap[number] = true;

                    } else if ('A' <= chr && chr <= 'Z') { // Box
                        Box box = new Box(row, col, boxColors.get(chr), chr, false, -1, null);
                        boxes.add(box);
                    } else if (chr == ' ') {
                        // Free space.
                    } else {
                        System.err.println("Error, read invalid level character: " + (int) chr);
                        System.exit(1);
                    }
                }
                line = serverMessages.readLine();
                row++; // counts the number of rows
            }
        }


        //Read goal state
        int column = 0;
        int row = 0;

        if (!line.contains("#goal")) {
            System.err.println("Level format is not correct");

        } else {
            line = serverMessages.readLine();
            while (!line.equals("#end")) {
                // find the longest line, e.g. the max length in the x-direction)
                if (line.length() > column) {
                    column = line.length();
                }

                for (int col = 0; col < line.length(); col++) {
                    char chr = line.charAt(col);

                    if ('A' <= chr && chr <= 'Z') { // Goal.
                        boxGoals.add(new Goal(row, col, chr));
                    } else if ('0' <= chr && chr <= '9') { // agentGoal
                        agentGoals.add(new Goal(row, col, chr));
                    }
                }
                line = serverMessages.readLine();
                row++; // counts the number of rows
            }
        }


        boolean[][] wallsResized = new boolean[row][column];
        //Resize walls
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                wallsResized[i][j] = walls[i][j];
            }
        }

        ArrayList<Agent> agentsList = removeAgentsWhichAreNotInMap(agents, agentsFoundInMap);
        ArrayList<Box> unmovableBoxes = changeUnmovableBoxesToWalls(boxes, wallsResized, agentsList);
        removeGoalsOfUnmovableBoxes(unmovableBoxes, boxGoals);

        // Create new initial state
        // The WALLS and GOALS are static, so no need to initialize the arrays every
        // time
        CENmasystem.State.NUMBER_OF_AGENTS = agentsList.size();
        DECmasystem.State.NUMBER_OF_AGENTS = agentsList.size();

        CENmasystem.State.NUMBER_OF_BOXES = boxes.size();
        DECmasystem.State.NUMBER_OF_BOXES = boxes.size();

        CENmasystem.State.MAX_ROW = row;
        DECmasystem.State.MAX_ROW = row;

        CENmasystem.State.MAX_COL = column;
        DECmasystem.State.MAX_COL = column;

        CENmasystem.State.WALLS = wallsResized;
        DECmasystem.State.WALLS = wallsResized;


        Box[] boxesArray = new Box[boxes.size()];
        Agent[] agentsArray = new Agent[agentsList.size()];
        Goal[] boxGoalsArray = new Goal[boxGoals.size()];
        Goal[] agentGoalsArray = new Goal[agentGoals.size()];

        for (int i = 0; i < boxGoalsArray.length; i++) {
            boxGoalsArray[i] = boxGoals.get(i);
        }
        for (int i = 0; i < boxesArray.length; i++) {
            boxesArray[i] = boxes.get(i);
        }
        for (int i = 0; i < agentsArray.length; i++) {
            agentsArray[i] = agentsList.get(i);
        }

        for (int i = 0; i < agentGoalsArray.length; i++) {
            agentGoalsArray[i] = agentGoals.get(i);
        }

        CENmasystem.State.BOXGOALS = boxGoalsArray;
        CENmasystem.State.AGENTGOALS = agentGoalsArray;

        State initialState = new State(null, boxesArray, agentsArray);
        return initialState;
    }

    private static void removeGoalsOfUnmovableBoxes(ArrayList<Box> unmovableBoxes, ArrayList<Goal> boxGoals) {
        HashSet<Character> goalCharsToDelete = new HashSet<>();
        ArrayList<Goal> goalsToBeRemoved = new ArrayList<>();

        for (Box box: unmovableBoxes) {
            goalCharsToDelete.add(box.letter);
        }

        for (Goal goal : boxGoals) {
            if (goalCharsToDelete.contains(goal.letter)){
                goalsToBeRemoved.add(goal);
                System.err.println("Removing" + goal.toString());
            }
        }
        boxGoals.removeAll(goalsToBeRemoved);
    }

    private static ArrayList<Agent> removeAgentsWhichAreNotInMap(Agent[] agents, boolean[] agentsFoundInMap) {
        ArrayList<Agent> agentsList = new ArrayList<>();
        for (int i = 0; i < agents.length; i++) {
            if (agents[i] != null) {
                agentsList.add(agents[i]);
            }
        }
        //Remove agents which are declared in description (with color and nr) but not placed in map
        int nrOfAgents = agentsList.size();
        for (int i = nrOfAgents - 1; i>0; i--) {
            if (!agentsFoundInMap[i] && nrOfAgents - 1 >= i){
                Agent agent = agentsList.remove(i);
                System.err.println("Removing " + agent.toString());
            }
        }
        return agentsList;
    }

    public static ArrayList<Box> changeUnmovableBoxesToWalls(ArrayList<Box> boxes, boolean[][] walls, ArrayList<Agent> agentsList){
        ArrayList<Box> boxesChangedToWalls = new ArrayList<>();
        for (Box box : boxes) {
            boolean boxIsMovable = false;
            for (Agent agent : agentsList) {
                if (agent.color == box.color){
                    boxIsMovable = true;
                }
            }
            if (!boxIsMovable){
                boxesChangedToWalls.add(box);
                walls[box.row][box.column] = true;
            }
        }

        boxes.removeAll(boxesChangedToWalls);
        System.err.println("Boxes changed to walls: " + boxesChangedToWalls.size());
        return boxesChangedToWalls;
    }


}
