package masystem;

import java.awt.*;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SearchClient {
    public State initialState;

    public SearchClient(BufferedReader serverMessages) throws Exception {

        int maxRow = 70;
        int maxCol = 70;
        boolean[][] walls = new boolean[maxRow][maxCol];
        char[][] goals = new char[maxRow][maxCol];
        char[][] boxes2dArray = new char[maxRow][maxCol];
        Map <Character,Integer> boxColors = new HashMap();

        ArrayList<Agent> agents = new ArrayList<>();
        Map<Point,Box> boxes = new HashMap<>();
        String line = "init";

        while (!line.contains("#colors")){
            //read lines until reaching colors
            line = serverMessages.readLine();
        }

        //Read first color
        line = serverMessages.readLine();

        //Categorize agents and boxes2dArray into colors
        int color = 0; //Using integer to represent a color for agent and boxes2dArray
        while (line.contains("blue") || line.contains("red") || line.contains("cyan") || line.contains("purple") || line.contains("green")
                || line.contains("orange") || line.contains("pink") || line.contains("grey") || line.contains("lightblue") || line.contains("brown")){


            //Agents
            for (int i = 0; i < line.length() ; i++) {
                if ('0' <= line.charAt(i) && line.charAt(i) <= '9'){
                    Agent agent = new Agent();
                    agents.add(agent);
                    agent.color = color;
                }
            }

            //Boxes
            for (int i = 0; i < line.length() ; i++) {
                if ('A' <= line.charAt(i) && line.charAt(i) <= 'Z'){
                    boxColors.put(line.charAt(i), color); // Dict maps 'Letter' (of box) -> 'Color'
                }
            }
            color++;
            line = serverMessages.readLine();
        }


        //Read initial state
        if (!line.contains("#initial")){
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
                        agents.get(number).row = row;
                        agents.get(number).column = col;
                    } else if ('A' <= chr && chr <= 'Z') { // Box.
                        boxes2dArray[row][col] = chr;
                        Box box = new Box();
                        box.letter = chr;
                        box.color = boxColors.get(chr);
                        box.row = row;
                        box.column = col;
                        boxes.put(new Point(box.row,box.column), box);
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

        if (!line.contains("#goal")){
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
                        goals[row][col] = chr;
                    }
                }
                line = serverMessages.readLine();
                row++; // counts the number of rows
            }
        }

        //Resize walls and goals array
        boolean[][] wallsResized = new boolean[row][column];
        char[][] goalsResized = new char[row][column];

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                wallsResized[i][j] = walls[i][j];
                goalsResized[i][j] = goals[i][j];
            }
        }



        // Create new initial state
        // The WALLS and GOALS are static, so no need to initialize the arrays every
        // time
        State.MAX_ROW = row;
        State.MAX_COL = column;
        State.WALLS = wallsResized;
        State.GOALS = goalsResized;
        State.BOXCOLORS = boxColors;
        this.initialState = new State(null, boxes2dArray, agents, boxes);
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
