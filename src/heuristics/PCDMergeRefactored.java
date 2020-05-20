package heuristics;


import masystem.*;

import java.awt.*;
import java.util.*;
import java.util.List;

import com.google.common.collect.Collections2;


public class PCDMergeRefactored extends Heuristic {
    HashMap<Point, int[][]> distMaps;
    //TODO: Hvorfor er disse ikke (char, Goal)? frem for point...
    HashMap<Goal, ArrayList<Goal>> dependencies = new HashMap<>();
    public HashSet<Character> seenLetters = new HashSet<>();
    private int distMapInitValue = 10000;

    //Weights
    int factorDistBoxToAgent = 1;
    int factorDistBoxToGoal = 2;
    int factorRewardForPlacingBoxAtGoal = State.MAX_ROW*State.MAX_COL;
    int factorForPunishments = 1;


    public PCDMergeRefactored(State initialState) {
        super(initialState);


        //Init dictionary for each cell in map, containing its distance map to every other cell
        initDistMapForAllCells(initialState);

        assignBoxesToGoals(initialState);


        initDependenciesOfBoxes(initialState);
    }


    public int h(State n) {
        int h = 0;


        int distsToBoxesFromAgents = getDistsToBoxesFromAgents(n); //Calculates dists from agents to boxes which are not at a goal

        int minDistsToAssignedGoal = getDistsToAssignedGoals(n); //Minimizes distance from goals to an initially assigned box, which were closest to the goal

        int punishmentsForAgentsNotMoving = punishmentsForAgentsNotMoving(n);

        //int punishmentsForBlocks = getPunishmentsForBlocks(n);

        h = h + distsToBoxesFromAgents*factorDistBoxToAgent;
        h = h + minDistsToAssignedGoal*factorDistBoxToGoal;
        h = h + punishmentsForAgentsNotMoving;



        return h*2; //Resolves to weighted A*
        //return h;

    }

    /*
    private int getPunishmentsForBlocks(State n) {
        int h = 0;
        for (Box box : n.boxes) {
            if (box.assignedGoal == null){
                continue;
            }
            //Get assigned goal
            Goal goal = box.assignedGoal;

            //Get nearest agent
            int[][] distsToBox = distMaps.get(new Point(box.row, box.column));
            int minDist = State.MAX_ROW*State.MAX_COL;
            Agent nearestAgent = null;
            for (Agent agent : n.agents) {
                int distToBox = distsToBox[agent.row][agent.column];
                if (agent.color == box.color && minDist > distToBox){
                    nearestAgent = agent;
                    minDist = distToBox;
                }
            }



        }

        return h;
    }

     */

    public int punishmentsForAgentsNotMoving(State state){
        int punishments = 0;
        State parent = state.parent;
        if (parent==null){
            return 0;
        }

        for (int i = 0; i < state.agents.length ; i++) {
            if ( (state.agents[i].row == parent.agents[i].row && state.agents[i].column == parent.agents[i].column) ){
                punishments+= factorForPunishments;
            }
        }
        return punishments;

    }

    public void assignBoxesToGoals(State initialState){


        HashMap<Goal, Integer> hvorforGrDenneForskel = new HashMap<>();



        //WTFFFFFFF
        //If so - replace the current assignment
        Goal goal1 = new Goal(0,0, 'b');
        Goal goal2 = new Goal(0,0, 'b');
        Goal goal3 = new Goal(0,0, 'b');
        Goal goal4 = new Goal(0,0, 'b');
        Goal goal5 = new Goal(0,0, 'b');
        Goal goal6 = new Goal(0,0, 'b');
        Goal goal7 = new Goal(0,0, 'b');
        Goal goal8 = new Goal(0,0, 'b');
        Goal goal9 = new Goal(0,0, 'b');
        Goal goal10 = new Goal(0,0, 'b');
        Goal goal11 = new Goal(0,0, 'b');
        Goal goal12 = new Goal(0,0, 'b');
        Goal goal13 = new Goal(0,0, 'b');

        hvorforGrDenneForskel.put(goal1, 1 );
        hvorforGrDenneForskel.put(goal2, 2 );
        hvorforGrDenneForskel.put(goal3, 3 );
        hvorforGrDenneForskel.put(goal4, 4 );
        hvorforGrDenneForskel.put(goal5, 5 );
        hvorforGrDenneForskel.put(goal6, 66 );
        hvorforGrDenneForskel.put(goal7, 7 );
        hvorforGrDenneForskel.put(goal8, 234 );
        hvorforGrDenneForskel.put(goal9, 3 );
        hvorforGrDenneForskel.put(goal10, 1 );
        hvorforGrDenneForskel.put(goal11, 2 );
        hvorforGrDenneForskel.put(goal12, 2 );
        //hvorforGrDenneForskel.put(goal13, 2 );


        //WTFFFFFFF



        //Algorithm from: https://github.com/aalmi/HungarianAlgorithm/blob/master/HungarianAlgorithm.java
        int[][] costMatrix = new int[initialState.boxes.length][initialState.boxes.length];

        for (Box box : initialState.boxes) {
            box.assignedGoal = null;
        }

        //Now assign dist from every goal to every box in costMatrix
        for (int i = 0; i < initialState.boxes.length ; i++) { //Each column is a goal
            Goal goal = null;
            int[][] distmap = null;
            if (i<State.GOALS.length){
                goal = State.GOALS[i];
                distmap = distMaps.get(new Point(goal.row, goal.column));
            }
            for (int j = 0; j < initialState.boxes.length ; j++) { //Each row represents a box
                Box box = initialState.boxes[j];
                if (goal == null){
                     //Adding fictive goals, which the least "popular" (most distant) boxes will be assigned to
                    //By the end, we will just remove these rows, as the boxes should have no goals
                    costMatrix[i][j] = 0;
                } else if (goal.letter != box.letter){
                    costMatrix[i][j] = distMapInitValue;
                } else {
                    costMatrix[i][j] = distmap[box.row][box.column];
                }
            }
        }

        HungarianAlgorithm hun = new HungarianAlgorithm(costMatrix);
        int[][] assignmentIndexes = hun.findOptimalAssignment();

        //Now assign actual boxes to goals, where subaarays in assignmentIndexes are {boxIndex, nearestGoalIndex}
        for (int i = 0; i < assignmentIndexes.length; i++) {
            if (assignmentIndexes[i][1] < State.GOALS.length) {
                Box box = initialState.boxes[assignmentIndexes[i][0]];
                Goal goal = State.GOALS[assignmentIndexes[i][1]];
                box.assignedGoal = goal;
            }
        }

        System.err.println("HUNGARIAN ALGORITHM OUTPUT:");

        for (Box box : initialState.boxes) {
            Goal goal = box.assignedGoal;
            if (goal != null) {
                System.err.println("Goal " + goal.letter + ": (" + goal.row + "," + goal.column + ") -> Box: " + box);
            } else {
                System.err.println("Box: " + box + "was not assigned any goal");
            }

        }
        System.err.println("______________________");


    }




    public int getDistsToAssignedGoals(State n) {
        int sum = 0;
        for (Box box : n.boxes) {
            if (box.assignedGoal != null) {
                Goal goal = box.assignedGoal;
                if (goal.row == box.row && goal.column == box.column){
                    sum -= factorRewardForPlacingBoxAtGoal; //reward placing box at goal
                } else if (isDependanciesSatisfied(n, box)){
                    int[][] distancesFromGoal = distMaps.get(new Point(goal.row, goal.column));
                    sum += distancesFromGoal[box.row][box.column];
                } else {
                    sum += 50;
                }
            }
        }
        return sum;
    }



    public int getDistsToBoxesFromAgents(State n) {

        int maxDist = State.MAX_COL * State.MAX_ROW*State.MAX_ROW;
        int summedDistsForAgents = 0;

        //Sum distances from every agent to nearest box
        for (Agent agent : n.agents) {
            int minDistToBox = maxDist;
            int minDistToBoxWithAssignedGoal = maxDist;
            int[][] distancesFromAgent = distMaps.get(new Point(agent.row, agent.column));
            for (Box box : n.boxes) {
                if (box.color == agent.color && !boxIsAtGoal(box)) { //Only minimize dist to boxes not at goal and moveable by agent:

                    int distToBox = distancesFromAgent[box.row][box.column];

                    //Check distances to boxes with assigned goals
                    if (box.assignedGoal != null && distToBox < minDistToBoxWithAssignedGoal && isDependanciesSatisfied(n, box)) {
                        minDistToBoxWithAssignedGoal = distToBox;
                    }
                    //Check distances to every box
                    if (distToBox < minDistToBox) {
                        minDistToBox = distToBox;
                    }
                }
            }
            
            // If minDist is not changed => No boxes to move or all boxes at goal => don't increase heuristic value
            if (minDistToBox != maxDist) {
                //Prioritize minimizing dist to boxes with goals assigned
                if (minDistToBoxWithAssignedGoal != maxDist) {
                    summedDistsForAgents += minDistToBoxWithAssignedGoal;
                    //Otherwise prioritize minimizing dist to any box
                } else {
                    summedDistsForAgents += minDistToBox;
                }
            }
        }

        return summedDistsForAgents;
    }



    private int getMinDistsToBoxesFromGoals(State n) {

        int maxDist = State.MAX_COL * State.MAX_ROW;
        int summedDistsForGoals = 0;

        //Sum distances from every goal to nearest box
        for (Goal goal : State.GOALS) {
            int minDist = maxDist;
            int[][] distancesFromGoal = distMaps.get(new Point(goal.row, goal.column));

            //Minimize distance from goal to nearest box:
            for (Box box : n.boxes) {
                int distToBox = distancesFromGoal[box.row][box.column];
                if (distToBox < minDist && !boxIsAtGoal(box) && goal.letter == box.letter) {
                    minDist = distToBox;
                }
            }

            if (minDist == 0) {
                summedDistsForGoals +=factorRewardForPlacingBoxAtGoal;
            }
            if (minDist != maxDist) {
                summedDistsForGoals += minDist;
            }
        }
        return summedDistsForGoals;
    }

    private boolean isDependanciesSatisfied(State n, Box box){
        boolean isSatisfied = true;
        if (dependencies.containsKey(box.assignedGoal)) {
            for (Goal depend : dependencies.get(box.assignedGoal)) { //for all the goals that musst be satisfied before our goal
                if (!isSatisfied(n, depend)) {
                    //System.err.println(box.letter+" is not satisfied: "+box.column+","+box.row);
                    // System.err.println(depend.toString());
                    isSatisfied = false;
                    break;
                }
            }
        }
        return isSatisfied;
    }

    boolean allGoalsSatisfied(State n, Agent agent){
        //RIP this methods runtime
        for (Box box : n.boxes){
            if (box.color == agent.color){
                for (Goal goal : n.GOALS){
                    if (goal.letter == box.letter){
                        if (!isSatisfied(n, goal)){
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }





    // __________________________ DEPENDENCIES _____________________________________

    private void initDependenciesOfBoxes(State initialState) {
        for (Box box : initialState.boxes) {
            if (box.assignedGoal == null){
                continue;
            }
            Point pGoal = new Point(box.assignedGoal.column, box.assignedGoal.row);
            ArrayList<Goal> theyDepend = getDependancy(initialState, new Point(box.column, box.row), pGoal);
            for (Goal g : theyDepend) {
                if (!dependencies.containsKey(g)) {
                    dependencies.put(g, new ArrayList<Goal>());
                }
                dependencies.get(g).add(box.assignedGoal);
            }
        }
        System.err.println("Dependencies:");

        for (Goal g : dependencies.keySet()) {
            System.err.println(g.letter + ": " + dependencies.get(g).toString());
        }
    }

    public ArrayList<Goal> getDependancy(State state, Point start, Point goal) {
        // make work for multigoal
        Stack<Step> frontier = new Stack<>();
        Stack<Step> frontier2 = new Stack<>();
        HashSet<Step> explored = new HashSet<>();

        Step init = new Step(start.x, start.y, null);

        frontier.add(init);
        explored.add(init);

        while (!frontier.isEmpty() || !frontier2.isEmpty()) {
            Step toExpand = null;
            if (!frontier.isEmpty()) {
                toExpand = frontier.pop();
            } else {
                toExpand = frontier2.pop();
            }

            //get children
            for (int dir = 0; dir < 4; dir++) {
                int newX = toExpand.col;
                int newY = toExpand.row;
                switch (dir) {
                    case 0:
                        newY--;
                        break;
                    case 1:
                        newX--;
                        break;
                    case 2:
                        newY++;
                        break;
                    case 3:
                        newX++;
                        break;
                }

                if (newX == goal.x && newY == goal.y) {
                    ArrayList<Goal> goals = new ArrayList<>();
                    Step parent = toExpand;
                    while (parent != null) {
                        for (Goal goalTemp : State.GOALS) {
                            if (goalTemp.row == parent.row && goalTemp.column == parent.col) {
                                goals.add(goalTemp);
                            }
                        }
                        parent = parent.parent; //go one up
                    }
                    return goals;
                }

                if (newX < 0 || newX >= State.MAX_COL || newY < 0 || newY >= State.MAX_ROW) {
                    continue; // out of bounds
                }

                if (State.WALLS[newY][newX]) { //wall
                    continue;
                }

                Step newStep = new Step(newX, newY, toExpand);

                if (explored.contains(newStep)) {
                    continue; //already added to stack
                }

                boolean isGoal = false;
                for (Goal goalTemp : State.GOALS) {
                    if (goalTemp.row == newY && goalTemp.column == newX) {
                        isGoal = true;
                        break;
                    }
                }
                explored.add(newStep);
                if (isGoal) {
                    frontier2.push(newStep);
                } else {
                    frontier.push(newStep);
                }
            }
        } //while end
        return new ArrayList<Goal>(); //no path / no dependencies
    }


    // __________________________ DISTMAP _____________________________________

    private void initDistMapForAllCells(State initialState) {
        distMaps = new HashMap<Point, int[][]>();

        for (int x = 0; x < State.WALLS.length; x++) {
            for (int y = 0; y < State.WALLS[x].length; y++) {


                if (State.WALLS[x][y])
                    continue; //Only calculate distances from where there are no walls

                int[][] distMap = createDistMap(State.MAX_ROW, State.MAX_COL);
                distMap = getDistMapRec(distMap, x, y, initialState, 0); //Calc distances from cell (x,y) to every other cell
                distMaps.put(new Point(x, y), distMap);

            }
        }
    }

    public int[][] createDistMap(int max_row, int max_col) {
        //creates a 2d int array with all values being 100000
        int[][] distMap = new int[max_row][max_col];
        for (int x = 0; x < max_row; x++) {
            for (int y = 0; y < max_col; y++) {
                distMap[x][y] = distMapInitValue;
            }
        }
        return distMap;
    }

    public int[][] getDistMapRec(int[][] distMap, int x, int y, State state, int dist) {
        distMap[x][y] = dist;

        for (int i = 0; i < 4; i++) { //for all directions
            int newX = x;
            int newY = y;
            // get neighbor coords
            switch (i) {
                case 0: // up
                    newY--;
                    break;
                case 1: // left
                    newX--;
                    break;
                case 2: // down
                    newY++;
                    break;
                case 3: // right
                    newX++;
                    break;
            }
            if (newX < 0 || newX >= distMap.length || newY < 0 || newY >= distMap[0].length) {
                continue; // out of bounds
            }
            if (State.WALLS[newX][newY]) {
                continue; //do not update walls
            }
            if (dist + 1 < distMap[newX][newY]) {
                distMap = getDistMapRec(distMap, newX, newY, state, dist + 1); //run recursive for neighbour
            }
        }
        return distMap;
    }

    public boolean isSatisfied(State state, Goal goal) {
        for (Box box : state.boxes) {
            if (box.assignedGoal == goal){
                if (box.column == goal.column && box.row == goal.row && box.letter == goal.letter) {
                    return true;
                }
            }
        }
        return false;
    }

    //TODO: If we stay with assignedGoals: just check if box.row == box.assignedGoal.row etc...
    private boolean boxIsAtGoal(Box box) {
        if (box.assignedGoal == null){
            return false;
        }
        return box.column == box.assignedGoal.column && box.row == box.assignedGoal.row;
    }

    //TODO: Kan vi slette denne?
    public void printDistMaps(HashMap<Character, int[][]> distMaps) {
        System.err.println("DistMap:");
        for (char key : distMaps.keySet()) { //for each distMap
            for (int[] line : distMaps.get(key)) {
                for (int val : line) {
                    System.err.print(val + ", ");
                }
                System.err.println();
            }
            System.err.println("-----");
        }
    }
}