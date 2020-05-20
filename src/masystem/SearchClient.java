package masystem;

import heuristics.Heuristic;
import heuristics.PCDMergeRefactored;
import heuristics.PCDMergeTaskOriented;

import java.awt.*;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchClient {
    public State initialState;

    public SearchClient(BufferedReader serverMessages) throws Exception {

        int maxRow = 70;
        int maxCol = 70;
        boolean[][] walls = new boolean[maxRow][maxCol];


        boolean[] agentsFoundInMap = new boolean[10];
        Map<Character, Integer> boxColors = new HashMap<>();

        Agent[] agents = new Agent[10];
        ArrayList<Goal> goals = new ArrayList<>();
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
                        goals.add(new Goal(row, col, chr));
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
        changeUnmovableBoxesToWalls(boxes, wallsResized, agentsList);

        // Create new initial state
        // The WALLS and GOALS are static, so no need to initialize the arrays every
        // time
        State.NUMBER_OF_AGENTS = agentsList.size();
        State.NUMBER_OF_BOXES = boxes.size();
        State.MAX_ROW = row;
        State.MAX_COL = column;
        State.WALLS = wallsResized;

        Box[] boxesArray = new Box[boxes.size()];
        Agent[] agentsArray = new Agent[agentsList.size()];
        Goal[] goalsArray = new Goal[goals.size()];

        for (int i = 0; i < goalsArray.length; i++) {
            goalsArray[i] = goals.get(i);
        }
        for (int i = 0; i < boxesArray.length; i++) {
            boxesArray[i] = boxes.get(i);
        }
        for (int i = 0; i < agentsArray.length; i++) {
            agentsArray[i] = agentsList.get(i);
        }

        State.GOALS = goalsArray;
        this.initialState = new State(null, boxesArray, agentsArray);
    }

    private ArrayList<Agent> removeAgentsWhichAreNotInMap(Agent[] agents, boolean[] agentsFoundInMap) {
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


    public ArrayList<State> Search(BestFirstStrategy bestFirstStrategy) {

        System.err.format("Search starting with bestFirstStrategy %s.\n", bestFirstStrategy.toString());

        bestFirstStrategy.addToFrontier(this.initialState);


        int iterations = 0;
        while (true) {

            if (bestFirstStrategy.frontierIsEmpty()) {
                return null;
            }

            State leafState = bestFirstStrategy.getAndRemoveLeaf();

            if (iterations == 100) {

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

    public void changeUnmovableBoxesToWalls(ArrayList<Box> boxes, boolean[][] walls, ArrayList<Agent> agentsList){
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
    }


}
