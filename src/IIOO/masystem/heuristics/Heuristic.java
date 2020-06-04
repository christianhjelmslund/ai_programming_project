package IIOO.masystem.heuristics;


import IIOO.masystem.Agent;
import IIOO.masystem.Box;
import IIOO.masystem.Goal;
import IIOO.masystem.State;
import IIOO.masystem.decentralized.Objective;

import java.awt.*;
import java.util.*;

public abstract class Heuristic {

    public Set<Objective> objectives;
    public HashMap<Point, int[][]> distMaps;
    final HashMap<Point, Boolean> corridor = new HashMap<>();
    final HashMap<Integer, ArrayList<Point>> isBlocking = new HashMap<>();
    protected final int distMapInitValue = 10000;
    final int factorForNotMovingPunishments = 2;
    HashMap<Goal, ArrayList<Goal>> dependencies = new HashMap<>();

    //benchmarking control panel
    final boolean unblockingDeactivated = false;
    final boolean distMapDeactivated = false;
    final boolean punishForStandingStillDeactivated = false;
    final boolean punishCorridorDeactivated = false;
    final boolean isDependenciesDeactivated = false;


    public abstract int h(State n);

    public void setObjectives(Set<Objective> objectives) {
        this.objectives = objectives;
    }

    public boolean boxesAreAdjacent(Box box1, Box box2){
        return (box1.row == box2.row + 1 && box1.column == box2.column) ||
                (box1.row == box2.row -1 && box1.column == box2.column) ||
                (box1.row == box2.row && box1.column == box2.column +1) ||
                (box1.row == box2.row && box1.column == box2.column -1);
    }

    public void assignBoxesToGoals(State initialState){
        //Algorithm from: https://github.com/aalmi/HungarianAlgorithm/blob/master/HungarianAlgorithm.java
        int[][] costMatrix = new int[initialState.boxes.length][initialState.boxes.length];

        for (Box box : initialState.boxes) {
            box.assignedGoal = null;
        }

        //Now assign dist from every goal to every box in costMatrix
        for (int i = 0; i < initialState.boxes.length ; i++) { //Each column is a goal
            Goal goal = null;
            int[][] distmap = null;
            if (i<State.BOXGOALS.length){
                goal = State.BOXGOALS[i];
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
        for (int[] assignmentIndex : assignmentIndexes) {
            if (assignmentIndex[1] < State.BOXGOALS.length) {
                Box box = initialState.boxes[assignmentIndex[0]];
                box.assignedGoal = State.BOXGOALS[assignmentIndex[1]];
            }
        }
    }

    public boolean isSatisfied(State state, Goal goal) {
        for (Box box : state.boxes) {
            if (box.assignedGoal == goal) {
                if (box.column == goal.column && box.row == goal.row && box.letter == goal.letter) {
                    return true;
                }
            }
        }
        return false;
    }

    public int moveAgentToAgentGoalWhenDone(State n){
        int h = 0;

        for (int i = 0; i<n.agents.length; i++){
            Agent agent = n.agents[i];
            boolean allBoxesSat = true;
            for (Box box : n.boxes){
                if (box.color != agent.color) continue; //only consider agents goal
                if (box.assignedGoal == null) continue; //only consider boxes with goals
                if (!isSatisfied(n, box.assignedGoal)){
                    allBoxesSat = false;
                    break;
                }
            }
            if (!allBoxesSat){
                continue;
            }

            //if all boxes of same color as agent is satisifed. Move to agent goal
            for (Goal agentGoal : State.AGENTGOALS){
                if ((Character.getNumericValue(agentGoal.letter)) == i){
                    //agentGoal is agents agentgoal ps rip
                    h += distMaps.get(new Point(agent.row,agent.column))[agentGoal.row][agentGoal.column];
                    break;
                }
            }
        }
        return h;
    }

    public int punishmentsForAgentsNotMoving(State state) {
        int punishments = 0;
        State parent = state.parent;

        if (parent == null) {
            return 0;
        }

        for (int i = 0; i < state.agents.length; i++) {
            if ((state.agents[i].row == parent.agents[i].row && state.agents[i].column == parent.agents[i].column)) {
                punishments += factorForNotMovingPunishments;
            }
        }
        return punishments;
    }

    protected boolean isDependenciesSatisfied(State n, Box box){
        if (isDependenciesDeactivated)
            return true;

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

    // __________________________ DEPENDENCIES _____________________________________
    protected void initDependenciesOfBoxes(State initialState) {
        for (Box box : initialState.boxes) {
            if (box.assignedGoal == null) {
                continue;
            }
            Point pGoal = new Point(box.assignedGoal.column, box.assignedGoal.row);
            ArrayList<Goal> theyDepend = getDependency(new Point(box.column, box.row), pGoal);
            for (Goal g : theyDepend) {
                if (!dependencies.containsKey(g)) {
                    dependencies.put(g, new ArrayList<>());
                }
                dependencies.get(g).add(box.assignedGoal);
            }
        }
    }

    public ArrayList<Goal> getDependency(Point start, Point goal) {
        // make work for multigoal
        Stack<Step> frontier = new Stack<>();
        Stack<Step> frontier2 = new Stack<>();
        HashSet<Step> explored = new HashSet<>();

        Step init = new Step(start.x, start.y, null);

        frontier.add(init);
        explored.add(init);

        while (!frontier.isEmpty() || !frontier2.isEmpty()) {
            Step toExpand;
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
                    while (parent.parent != null) {
                        for (Goal goalTemp : State.BOXGOALS) {
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
                for (Goal goalTemp : State.BOXGOALS) {
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
        return new ArrayList<>(); //no path / no dependencies
    }

    // __________________________ DISTMAP _____________________________________
    protected void initDistMapForAllCells() {
        distMaps = new HashMap<>();

        for (int x = 0; x < State.WALLS.length; x++) {
            for (int y = 0; y < State.WALLS[x].length; y++) {


                if (State.WALLS[x][y])
                    continue; //Only calculate distances from where there are no walls

                int[][] distMap = createDistMap(State.MAX_ROW, State.MAX_COL);
                distMap = getDistMapRec(distMap, x, y, 0); //Calc distances from cell (x,y) to every other cell
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

    public int[][] getDistMapRec(int[][] distMap, int x, int y, int dist) {
        distMap[x][y] = dist;

        if (distMapDeactivated){
            for (int xt = 0; xt<distMap.length; xt++){
                for (int yt = 0; yt<distMap[0].length; yt++){
                    distMap[xt][yt] = Math.abs(x-xt)+Math.abs(y-yt);
                }
            }
            return distMap;
        }
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
                distMap = getDistMapRec(distMap, newX, newY, dist + 1); //run recursive for neighbour
            }
        }
        return distMap;
    }

    public void initCorridors(){
        for (int y = 0; y<State.MAX_ROW; y++){
            for (int x = 0; x<State.MAX_COL; x++){
                Point p = new Point(x,y);
                if (isCorridor(p)){
                    corridor.put(p, true);
                }
            }
        }
    }

    public boolean isCorridor(Point p){
        if (State.WALLS[p.y][p.x]){
            return false;
        }
        int emptyNeighbors = 0;
        for (int dir = 0; dir < 4; dir++){
            int dx = 0;
            int dy = 0;
            switch(dir){
                case 0:
                    dy--;
                    break;
                case 1:
                    dx--;
                    break;
                case 2:
                    dy++;
                    break;
                case 3:
                    dx++;
                    break;
            }
            if (p.y+dy < 0 || p.x+dx < 0){
                continue;
            }
            if (p.y+dy >= State.MAX_ROW || p.x+dx >= State.MAX_COL){
                continue;
            }
            if (!State.WALLS[p.y+dy][p.x+dx]){ //neighbor is free
                emptyNeighbors++;
            }
        }
        return emptyNeighbors < 3;
    }

    public ArrayList<Point> getVitalPath (State state, Point start, Point goal) {
        LinkedList<Step> frontier = new LinkedList<>();
        LinkedList<Step> frontier2 = new LinkedList<>();
        HashSet<Step> explored = new HashSet<>();

        Step init = new Step(start.x, start.y, null);

        frontier.add(init);
        explored.add(init);

        while (!frontier.isEmpty() || !frontier2.isEmpty()){
            Step toExpand;
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
                    path.add(new Point(newX, newY));
                    Step parent = toExpand;
                    while (parent != null){
                        path.add(new Point(parent.col, parent.row));
                        parent = parent.parent; //go one up
                    }
                    return path;
                }

                addNewCellToFrontier(state, frontier, frontier2, explored, toExpand, newX, newY);
            }
        } //while end
        return new ArrayList<>(); //no blockingBoxes / no dependencies
    }

    protected void addNewCellToFrontier(State state, LinkedList<Step> frontier, LinkedList<Step> frontier2, HashSet<Step> explored, Step toExpand, int newX, int newY) {
        if (newX < 0 || newX >= State.MAX_COL || newY < 0 || newY >= State.MAX_ROW) {
            return;
        }

        if (State.WALLS[newY][newX]){ //wall
            return;
        }

        Step newStep = new Step(newX, newY, toExpand);

        if (explored.contains(newStep)){
            return;
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


    public void initBlockingBoxes(State initialState) {
        for (int idx = 0; idx < initialState.boxes.length; idx++) {
            isBlocking.put(idx, new ArrayList<>());
        }
        //find blocking between agent and its boxes
        for (Agent agent : initialState.agents) {
            for (int idx = 0; idx < initialState.boxes.length; idx++) {
                Box box = initialState.boxes[idx];
                if (box.color != agent.color) {
                    continue;
                }
                if (box.assignedGoal == null) {
                    continue;
                }
                //find all blocking boxes between agent and boxes with same colour and an assigned goal
                ArrayList<Box> allBlockingBoxes = getBlockingBoxes(initialState, new Point(agent.column, agent.row), new Point(box.column, box.row));
                ArrayList<Point> vitalPath = getVitalPath(initialState, new Point(agent.column, agent.row), new Point(box.column, box.row));
                //convert blocking boxes to idx
                ArrayList<Integer> blockingIdx = new ArrayList<>();
                for (Box blockingBox : allBlockingBoxes) {
                    for (int i = 0; i < initialState.boxes.length; i++) {
                        if (initialState.boxes[i] == blockingBox) {
                            blockingIdx.add(i);
                        }
                    }
                }
                for (int i : blockingIdx) {
                    if (initialState.boxes[i].color == agent.color) {
                        continue; //ignore its own boxes
                    }
                    isBlocking.put(i, vitalPath);
                }
            }
        }
    }

    public ArrayList<Box> getBlockingBoxes(State state, Point start, Point goal) {
        LinkedList<Step> frontier = new LinkedList<>();
        LinkedList<Step> frontier2 = new LinkedList<>();
        HashSet<Step> explored = new HashSet<>();

        Step init = new Step(start.x, start.y, null);

        frontier.add(init);
        explored.add(init);

        while (!frontier.isEmpty() || !frontier2.isEmpty()) {
            Step toExpand;
            if (!frontier.isEmpty()) {
                toExpand = frontier.pollFirst();
            } else {
                toExpand = frontier2.pollFirst();
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

                if (newX == goal.x && newY == goal.y) { //found goal point
                    ArrayList<Box> blockingBoxes = new ArrayList<>();
                    Step parent = toExpand;
                    while (parent != null) {
                        for (Box box : state.boxes) {
                            if (box.column == parent.col && box.row == parent.row) { //box on path
                                blockingBoxes.add(box);
                            }
                        }
                        parent = parent.parent; //go one up
                    }
                    return blockingBoxes;
                }

                addNewCellToFrontier(state, frontier, frontier2, explored, toExpand, newX, newY);
            }
        } //while end
        return new ArrayList<>(); //no blockingBoxes / no dependencies
    }

    protected boolean boxIsAtGoal(Box box) {
        return box.assignedGoal != null && (box.column == box.assignedGoal.column && box.row == box.assignedGoal.row);
    }

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
