package IIOO.masystem.heuristics;

import IIOO.masystem.Agent;
import IIOO.masystem.Box;
import IIOO.masystem.Goal;
import IIOO.masystem.State;

import java.awt.*;
import java.util.*;

//TODO Make sure that agents do not *pull* boxes into corridors if they cannot fit.


public class PCDMergeRefactored extends Heuristic {
    HashMap<Point, int[][]> distMaps;
    //TODO: Hvorfor er disse ikke (char, Goal)? frem for point...
    HashMap<Goal, ArrayList<Goal>> dependencies = new HashMap<>();
    public HashSet<Character> seenLetters = new HashSet<>();
    HashMap<Point, Boolean> corridor = new HashMap<>();
    HashMap<Integer, ArrayList<Point>> isBlocking = new HashMap<>();

    private int distMapInitValue = 10000;

    //Weights
    int factorDistBoxToAgent = 1;
    int factorDistBoxToGoal = 2;
    int factorRewardForPlacingBoxAtGoal = State.MAX_ROW*State.MAX_COL;
    int typicalPunishmentFactor = 50;
    int factorForNotMovingPunishments = 2;
    int distanceToKeepBoxesFromGoalsb4Turn = 3;


    public PCDMergeRefactored(State initialState) {
        super(initialState);


        //Init dictionary for each cell in map, containing its distance map to every other cell
        initDistMapForAllCells(initialState);

        assignBoxesToGoals(initialState);
        initDependenciesOfBoxes(initialState);
        initCorridors(initialState);
        initBlockingBoxes(initialState);
    }


    public int h(State n) {
        int h = 0;


        int distsToBoxesFromAgents = getDistsToBoxesFromAgents(n); //Calculates dists from agents to boxes which are not at a goal

        int minDistsToAssignedGoal = getDistsToAssignedGoals(n); //Minimizes distance from goals to an initially assigned box, which were closest to the goal

        int punishmentsForAgentsNotMoving = punishmentsForAgentsNotMoving(n);

        //int punishmentForBeingCloseToOtherColors = getPunishmentForBeingCloseToOtherColors(n);

        h = h + distsToBoxesFromAgents*factorDistBoxToAgent;
        h = h + minDistsToAssignedGoal*factorDistBoxToGoal;
        h = h + punishmentsForAgentsNotMoving;
        h = h + moveAgentToAgentGoalWhenDone(n);
        //h = h + punishmentForBeingCloseToOtherColors;

   /*     double rand = Math.random();
        if (rand > 0.9995){
            h -= 2;
        } */


        return h*2; //Resolves to weighted A*
        //return h;

    }

    public void initBlockingBoxes(State initialState){
        for (int idx = 0; idx < initialState.boxes.length; idx++){
            isBlocking.put(idx, new ArrayList<Point>());
        }
        //find blocking between agent and its boxes
        for (Agent agent : initialState.agents){
            for (int idx = 0; idx < initialState.boxes.length; idx++){
                Box box = initialState.boxes[idx];
                if (box.color != agent.color){
                    continue;
                }
                if (box.assignedGoal == null){
                    continue;
                }
                //find all blocking boxes between agent and boxes with same colour and an assigned goal
                ArrayList<Box> allBlockingBoxes = getBlockingBoxes(initialState, new Point(agent.column, agent.row), new Point(box.column, box.row));
                ArrayList<Point> vitalPath = getVitalPath(initialState, new Point(agent.column, agent.row), new Point(box.column, box.row));
                System.err.println(agent.column+","+agent.row+" has blocking "+allBlockingBoxes.toString());
                //convert blocking boxes to idx
                ArrayList<Integer> blockingIdx = new ArrayList<>();
                for (Box blockingBox : allBlockingBoxes){
                    for (int i = 0; i<initialState.boxes.length; i++){
                        if (initialState.boxes[i] == blockingBox){
                            blockingIdx.add(i);
                        }
                    }
                }
                for (int i : blockingIdx){
                    if (initialState.boxes[i].color == agent.color){
                        continue; //ignore its own boxes
                    }
                    isBlocking.put(i, vitalPath);
                }
            }
        }
        System.err.println("Blocking boxes: ");
        for (int key : isBlocking.keySet()){
            System.err.println(initialState.boxes[key].letter+": "+isBlocking.get(key));
        }
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


    public int punishmentsForAgentsNotMoving(State state){
        int punishments = 0;
        State parent = state.parent;

        if (parent==null){
            return 0;
        }

        for (int i = 0; i < state.agents.length ; i++) {
            if ( (state.agents[i].row == parent.agents[i].row && state.agents[i].column == parent.agents[i].column) ){
                punishments+= factorForNotMovingPunishments;
            }
        }
        return punishments;
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
        for (int i = 0; i < assignmentIndexes.length; i++) {
            if (assignmentIndexes[i][1] < State.BOXGOALS.length) {
                Box box = initialState.boxes[assignmentIndexes[i][0]];
                Goal goal = State.BOXGOALS[assignmentIndexes[i][1]];
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

    public boolean boxesAreAdjacent(Box box1, Box box2){
        return (box1.row == box2.row + 1 && box1.column == box2.column) ||
                (box1.row == box2.row -1 && box1.column == box2.column) ||
                (box1.row == box2.row && box1.column == box2.column +1) ||
                (box1.row == box2.row && box1.column == box2.column -1);
    }

    public int getDistsToAssignedGoals(State n) {
        int sum = 0;
        for (Box box : n.boxes) {
            if (box.assignedGoal != null) {
                Goal goal = box.assignedGoal;
                int[][] distancesFromGoal = distMaps.get(new Point(goal.row, goal.column));
                int distToGoal = distancesFromGoal[box.row][box.column];

                if (isDependanciesSatisfied(n, box)) {
                    if (goal.row == box.row && goal.column == box.column) {
                        sum -= factorRewardForPlacingBoxAtGoal; //reward placing box at goal when dependencies are satisfied
                    } else {
                        sum += distToGoal;
                        for (Box boxOther : n.boxes) {
                            if (boxesAreAdjacent(box, boxOther) ) {
                                sum+=1;
                                break;
                            }
                        }
                    }
                } else { //If dependencies for box is not satisfied, punish current state
                    sum += typicalPunishmentFactor;
                    //     if (distToGoal < distanceToKeepBoxesFromGoalsb4Turn){
                    //        sum += distanceToKeepBoxesFromGoalsb4Turn-distToGoal; //Better the longer away
                    //    }
                }
            }
        }
        return sum;
    }

    public int getDistsToBoxesFromAgents(State n) {
        int maxDist = State.MAX_COL * State.MAX_ROW*State.MAX_ROW;
        int summedDistsForAgents = 0;
        int punishCorridor = 0;

        //Sum distances from every agent to nearest box
        for (Agent agent : n.agents) {
            Box minBox = null;
            int minDistToBox = maxDist;
            int minDistToBoxWithAssignedGoal = maxDist;
            int[][] distancesFromAgent = distMaps.get(new Point(agent.row, agent.column));
            boolean isBlock = true;
            for (int idx = 0; idx < n.boxes.length; idx++){
                Box box = n.boxes[idx];
                int distToBox = distancesFromAgent[box.row][box.column];
                if (box.color == agent.color && !boxIsAtGoal(box)) { //Only minimize dist to boxes not at goal and moveable by agent:
                    //Check distances to boxes with assigned goals
                    if (box.assignedGoal != null && distToBox < minDistToBoxWithAssignedGoal && isDependanciesSatisfied(n, box)) {
                        minDistToBoxWithAssignedGoal = distToBox;
                        minBox = box;
                    }
                    //Check distances to every box
                    if (distToBox < minDistToBox) {
                        minDistToBox = distToBox;
                    }
                }
                ArrayList<Point> vitalPath = isBlocking.get(idx);
                // System.err.println(vitalPath);
                if (box.color == agent.color && vitalPath.size() > 0){ //is known to block
                    if (vitalPath.contains(new Point(box.column, box.row))){ //still on vital path, blocking
                        // Point blockedAgentPoint = vitalPath.get(vitalPath.size()-1);
                        // System.err.println("Prior "+box.letter);
                        summedDistsForAgents += distToBox*3;
                        Point blockedAgent = vitalPath.get(vitalPath.size()-1);
                        // System.err.println(blockedAgent.toString());
                        summedDistsForAgents += distMaps.get(new Point(box.row, box.column))[blockedAgent.y][blockedAgent.x];
                        isBlock = false;
                    }
                }
            }

            // If minDist is not changed => No boxes to move or all boxes at goal => don't increase heuristic value
            if (minDistToBox != maxDist) {
                //Prioritize minimizing dist to boxes with goals assigned
                if (minDistToBoxWithAssignedGoal != maxDist) {
                    summedDistsForAgents += minDistToBoxWithAssignedGoal;
                    if (minBox != null){
                        //Punish agent if it pulls box into a corridor
                        // if (minBox.letter == 'I'){
                        //     System.err.println("I point: "+minBox.column+", "+minBox.row);
                        //     System.err.println(n.toString());
                        // }
                        Point boxPoint = new Point(minBox.column, minBox.row);
                        Point agentPoint = new Point(agent.column, agent.row);
                        if (corridor.containsKey(boxPoint) || corridor.containsKey(agentPoint)){ //minBox is at corridor

                            Point goalPoint = new Point(minBox.assignedGoal.row, minBox.assignedGoal.column);
                            //System.err.println("ERROR AT: " + goalPoint);
                            /*
                            for (Box box :
                                    n.boxes) {
                                System.err.println(box);
                            }
                            for (Goal goal :
                                    State.AGENTGOALS) {
                                System.err.println("Agent Goal:" + goal.row + "," +goal.column);
                            }
                            for (Goal goal :
                                    State.BOXGOALS) {
                                System.err.println("BOX Goal:" + goal.row + "," +goal.column);
                            }
                            */


                            if (distMaps.get(goalPoint)[minBox.row][minBox.column] > distMaps.get(goalPoint)[agent.row][agent.column]){
                                punishCorridor += 2;
                            }
                        }
                    }
                } else { //Otherwise prioritize minimizing dist to any box
                    summedDistsForAgents += minDistToBox;
                }
            }
        }

        return summedDistsForAgents+punishCorridor;
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
                for (Goal goal : n.BOXGOALS){
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
            System.err.print(g.letter+": ");
            for (Goal depend: dependencies.get(g)){
                System.err.print(depend.letter+", ");
            }
            System.err.println();
            // System.err.println(g.letter + ": " + dependencies.get(g).toString());
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

    public void initCorridors(State initialState){
        System.err.println("Corridors:");
        for (int y = 0; y<State.MAX_ROW; y++){
            for (int x = 0; x<State.MAX_COL; x++){
                Point p = new Point(x,y);
                if (isCorridor(initialState, p)){
                    corridor.put(p, true);
                    System.err.print(p.toString()+", ");
                }
            }
        }
    }

    public boolean isCorridor(State initialState, Point p){
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

    public ArrayList<Box> getBlockingBoxes (State state, Point start, Point goal) {
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
                    ArrayList<Box> blockingBoxes = new ArrayList<>();
                    //TODO ADD BOX IF THERE IS ONE AT goal
                    Step parent = toExpand;
                    while (parent != null){
                        for (Box box : state.boxes){
                            // System.err.println(parent.col+","+parent.row+"!="+box.column+","+box.row);
                            if (box.column == parent.col && box.row == parent.row){ //box on path
                                // System.err.println("YES");
                                blockingBoxes.add(box);
                            }
                        }
                        parent = parent.parent; //go one up
                    }
                    return blockingBoxes;
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
        return new ArrayList<Box>(); //no blockingBoxes / no dependencies
    }

    public ArrayList<Point> getVitalPath (State state, Point start, Point goal) {
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
                    path.add(new Point(newX, newY));
                    //TODO ADD Point IF THERE IS ONE AT goal
                    Step parent = toExpand;
                    while (parent != null){
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
        return new ArrayList<Point>(); //no blockingBoxes / no dependencies
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
    }}