package DECheuristics;


import DECmasystem.*;

import java.awt.*;
import java.util.*;
import java.util.List;

import com.google.common.collect.Collections2;


public class PCDMergeTaskOriented extends Heuristic {
    HashMap<Point, int[][]> distMaps;
    //TODO: Hvorfor er disse ikke (char, Goal)? frem for point...
    HashMap<Goal, ArrayList<Goal>> dependencies = new HashMap<>();
    public HashSet<Character> seenLetters = new HashSet<>();
    public ArrayList<Goal> goalsOrdered = new ArrayList<>();
    private int distMapInitValue = 10000;
    HashMap<Goal, Integer> goalImportance = new HashMap<>();

    //Weights
    int factorDistBoxToAgent = 1;
    int factorDistBoxToGoal = 2;
    int factorRewardForPlacingBoxAtGoal = State.MAX_ROW*State.MAX_COL;
    int factorForPunishments = 1;


    public PCDMergeTaskOriented(State initialState) {
        super(initialState);
        //Init dictionary for each cell in map, containing its distance map to every other cell
        initDistMapForAllCells(initialState);
        assignBoxesToGoals(initialState);
        initDependenciesOfBoxes(initialState);
        assignImportanceToGoals(initialState);
        initOrderingOfGoals(initialState);
        System.err.println("Ordering of goals: ");
        for (Goal key : goalImportance.keySet()){
            System.err.print(key.letter+": "+goalImportance.get(key)+", ");
        }
        System.err.println();
        System.err.println("Ordering of goals: ");
        for (Goal goal : goalsOrdered){
            System.err.print(goal.letter+", ");
        }
        System.err.println();
        //System.err.println("Path from 1,1 to 3,3 "+getPath(initialState, new Point(1,1), new Point(3,3)));
    }

    int h = 0;
    public int h(State n) {

        int maxDist = State.MAX_COL*State.MAX_ROW;

        for (Goal goal : goalsOrdered){
            if (isSatisfied(n, goal)){
                h -= maxDist;
                continue;
            }
            // goal is not satisfied
            satisfyGoal(n, goal);
            break;
        }

        return h; //Resolves to weighted A*
    }

    private void satisfyGoal(State n, Goal goal){
        // System.err.println("Satisfying goal "+goal.letter);
        //get the box of the particular goal
        Box box = null;
        for (Box boxTemp : n.boxes){
            if (boxTemp.assignedGoal == goal){
                box = boxTemp;
            }
        }
        if (box == null){
            System.err.println("ERR: No box assigned to this particular goal");
        }
        //---

        //move agent to goal
        moveAgentToBox(n, box);
        
        //move the box to goal
        moveBoxToGoal(n, box, goal);
    }

    private void moveAgentToBox(State n, Box box){
        // System.err.println("Moving agent to box "+box.letter);

        //find a suitable agent
        int[][] distMap = distMaps.get(new Point(box.row, box.column));
        Agent agent = null;
        int minDist = 10000000;
        for (Agent agentTemp : n.agents){
            int dist = distMap[agentTemp.row][agentTemp.column];
            if (dist < minDist){
                agent = agentTemp;
                minDist = dist;
            }
        }

        if (agent == null){
            System.err.println("ERR: No agent for box.");
        }

        //unblock path from agent to box
        ArrayList<Point> pathFromAgentToBox = getPath(n, new Point(agent.column, agent.row), new Point(box.column, box.row));
        unblockPath(n, pathFromAgentToBox); //TODO make sure that the agent and box does not block its own path!

        //move agent to box
        h += distMaps.get(new Point(box.row, box.column))[agent.row][agent.column];
    }

    private void unblockPath(State n, ArrayList<Point> path){
        // System.err.println("Unblocking path");
        //clear a path
        for (Point p : path){
            for (Agent agentTemp: n.agents){
                if (agentTemp.column == p.x && agentTemp.row == p.y){
                    Point freeCell = findNearestUnblockingCell(n, p, path);
                    h += distMaps.get(new Point(agentTemp.row,agentTemp.column))[freeCell.y][freeCell.x];
                }
            }
            for (Box boxTemp: n.boxes){
                if (boxTemp.column == p.x && boxTemp.row == p.y){
                    Point freeCell = findNearestUnblockingCell(n, p, path);
                    h += distMaps.get(new Point(boxTemp.row,boxTemp.column))[freeCell.y][freeCell.x];
                }
            }
        }
    }

    private Point findNearestUnblockingCell(State n, Point cell, ArrayList<Point> path){
        // System.err.println("Finding nearest unblocking cell");
        LinkedList<Point> frontier = new LinkedList<Point>();
        HashSet<Point> explored = new HashSet<Point>();

        frontier.add(cell);
        while (!frontier.isEmpty()){
            cell = frontier.pollFirst();
            explored.add(cell);
            if (!path.contains(cell)){
                return cell;
            }
            int dx = 0;
            int dy = 0;
            for (int dir = 0; dir < 4; dir++){
                switch (dir){
                    case 0: //up
                        dx--;
                        break;
                    case 1:
                        dy--;
                        break;
                    case 2:
                        dx++;
                        break;
                    case 3:
                        dy++;
                        break;
                }
                Point newPoint = new Point(cell.x+dx,cell.y+dy);
                if (!explored.contains(newPoint)){
                    frontier.add(newPoint);
                }
            }
        }
        return null;
    }

    private void moveBoxToGoal(State n, Box box, Goal goal){
        //unblock path from box to goal
        ArrayList<Point> pathFromBoxToGoal = getPath(n, new Point(box.column, box.row), new Point(goal.column, goal.row));
        unblockPath(n, pathFromBoxToGoal); //TODO make sure that the agent and box does not block its own path!

        //move box to goal
        h += distMaps.get(new Point(box.row, box.column))[goal.row][goal.column];
    }




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

    private void initOrderingOfGoals(State initialState){
        for (int rank = 1000; rank > 0; rank--){
            for (Goal key : goalImportance.keySet()){
                int goalRank = goalImportance.get(key);
                if (goalRank == rank){
                    goalsOrdered.add(key); //add goals from bottem up
                }
            }
        }
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

    void assignImportanceToGoals(State init){
        for (Goal goal : State.GOALS){
            goalImportance.put(goal,1); //add all goals to goal importance
        }
        for (Goal key : dependencies.keySet()){
            for (Goal dependOn : dependencies.get(key)){
                //goal with letter key depends on this goal
                goalImportance.put(dependOn, goalImportance.get(dependOn)+1); //add one to importance
            }
        }
    }

    public ArrayList<Point> getPath (State state, Point start, Point goal) {
        LinkedList<Step> frontier = new LinkedList<>();
        LinkedList<Step> frontier2 = new LinkedList<>();
        HashSet<Step> explored = new HashSet<>();

        Step init = new Step(start.x, start.y, null);

        frontier.add(init);
        explored.add(init);

        while (!frontier.isEmpty() || !frontier2.isEmpty()){
            Step toExpand = null;
            if (!frontier.isEmpty()){
                toExpand = frontier.pollFirst();
            } else {
                toExpand = frontier2.pollFirst();
            }
            
            //get children
            for (int dir=0; dir<4; dir++){
                int newX = toExpand.col;
                int newY = toExpand.row;
                switch(dir){
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

                if (newX == goal.x && newY == goal.y){ //found goal point
                    ArrayList<Point> path = new ArrayList<>();
                    path.add(new Point(newX, newY)); //adding goal
                    Step parent = toExpand;
                    while (parent.parent != null){
                        path.add(new Point(parent.col, parent.row));
                        parent = parent.parent; //go one up
                    }
                    return path;
                }

                if (newX < 0 || newX >= State.MAX_COL || newY < 0 || newY >= State.MAX_ROW) {
                    continue; // out of bounds
                }

                if (State.WALLS[newY][newX]){ //wall
                    continue;
                }

                Step newStep = new Step(newX, newY, toExpand);

                if (explored.contains(newStep)){
                    continue; //already added to stack
                }

                explored.add(newStep);
                boolean isBox = false;
                for (Box box : state.boxes){
                    if (box.column == newX && box.row == newY){
                        isBox = true;
                        break;
                    }
                }

                if (isBox) {
                    frontier2.addLast(newStep);    
                } else {
                    frontier.addLast(newStep);
                }
            }
        } //while end
        return new ArrayList<Point>(); //no path / no dependencies
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
