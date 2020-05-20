package heuristics;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import masystem.Agent;
import masystem.Box;
import masystem.Goal;
import masystem.State;

public class PreCalcDistVitalPathsDependancy extends Heuristic {
    HashMap<Point, int[][]> distMaps;
    HashMap<Character, Point> goals2 = new HashMap<>();
    HashMap<Integer, ArrayList<Point>> vitalPaths = new HashMap<>();
    ArrayList<int[]> goals = new ArrayList<int[]>(); // Used to loop over all goals faster than traversing every
                                                     // column*row
    HashMap<Character, ArrayList<Point>> dependencies = new HashMap<>();
    HashMap<Character, ArrayList<Box>> dependenciesBlockingBoxes = new HashMap<>();
    public HashSet<Character> seenLetters = new HashSet<>();

    public PreCalcDistVitalPathsDependancy(State initialState) {
        super(initialState);

        System.err.println("Boxes:");
        for (Box box : initialState.boxes){
            System.err.println(box.letter);
        }

        // Init dictionary for each cell in map, containing its distance map to every
        // other cell
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
        
        int id = 0;
        for (Box box : initialState.boxes) { // TODO: only works for one goal per letter
            for (Agent agent : initialState.agents){
                if (agent.color == box.color){
                    ArrayList<Point> path = getPath(initialState, agent, box, id, box.color);
                    if (path.size() > 0){
                        vitalPaths.put(id, path);
                        id++;
                    }
                }
            }
            Point pGoal = null;
            for (Goal goal : State.GOALS){
                if (goal.letter == box.letter){
                    pGoal = new Point(goal.column, goal.row);
                    //getPath2(initialState, box, goal, id, box.color); //need not return
                    break;
                }
            }
            if (pGoal != null){
                ArrayList<Goal> theyDepend = getDependancy(initialState, new Point(box.column,box.row), pGoal);
                for(Goal g : theyDepend){
                    char letter2 = g.letter;

                    if (!dependencies.containsKey(letter2)){
                        dependencies.put(letter2, new ArrayList<Point>());
                    }
                    dependencies.get(letter2).add(pGoal);
                }
            }
        }
        System.err.println("DEPEND.: "+dependencies.toString());
        System.err.println("DependBox: "+dependenciesBlockingBoxes.toString());

        for (Box box : initialState.boxes){
            if (box.marked){
                System.err.println("Box "+box.letter+" marked by path "+vitalPaths.get(box.markedBy).toString());
            }
        }

            // for (int[] pGoal2 : goals) {
            //     if (!canAccessExt((new Point(box.column, box.row)), pGoal, new Point(pGoal2[1], pGoal2[0]),
            //             initialState)) {
            //         // goal2 is in the way of goal
            //         if (dependency.containsKey(box.letter)) {
            //             char[] dependTemp = dependency.get(box.letter);
            //             // add the letter to this
            //         } else {
            //             dependency.put(box.letter, new char[] { State.GOALS[pGoal2[0]][pGoal2[1]] });
            //         }

            //     }
            // }

        //System.err.println("DEBUG "+dependencies.toString());
        // for (Character key : dependencies.keySet()){
        //     System.err.println("For key "+key);
        //     System.err.println(dependencies.get(key).toString());
        // }
        // Box someBox = initialState.boxes[0];
        // System.err.println("Getting dependencies for box "+someBox.letter+", "+someBox.column);
        // ArrayList<Point> temp = getDependancy(initialState, new Point(someBox.column, someBox.row), new Point(9, 1));
        // System.err.println("DEPEND: " + temp.toString());
    }

    public int[][] createDistMap(int max_row, int max_col) {
        // creates a 2d int array with all values being 100000
        int[][] distMap = new int[max_row][max_col];
        for (int x = 0; x < max_row; x++) {
            for (int y = 0; y < max_col; y++) {
                distMap[x][y] = 100000;
            }
        }
        return distMap;
    }

    public int[][] getDistMapRec(int[][] distMap, int x, int y, State state, int dist) {
        distMap[x][y] = dist;

        for (int i = 0; i < 4; i++) { // for all directions
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
                continue; // do not update walls
            }
            if (dist + 1 < distMap[newX][newY]) {
                distMap = getDistMapRec(distMap, newX, newY, state, dist + 1); // run recursive for neighbour
            }
        }
        return distMap;
    }

    class Step {
        public Step parent;
        public int row, col;

        public Step(int col, int row, Step parent) {
            this.col = col;
            this.row = row;
            this.parent = parent;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null)
                return false;
            if (this.getClass() != o.getClass())
                return false;
            Step other = (Step) o;
            return row == other.row && col == other.col;
        }

        @Override
        public int hashCode() {
            return new Point(this.col, this.row).hashCode();
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

        while (!frontier.isEmpty() || !frontier2.isEmpty()){
            Step toExpand = null;
            if (!frontier.isEmpty()){
                toExpand = frontier.pop();
            } else {
                toExpand = frontier2.pop();
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

                if (newX == goal.x && newY == goal.y){
                    ArrayList<Goal> goals = new ArrayList<>();
                    Step parent = toExpand;
                    while (parent != null){
                        for (Goal goalTemp : State.GOALS){
                            if (goalTemp.row == parent.row && goalTemp.column == parent.col){
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

                if (State.WALLS[newY][newX]){ //wall
                    continue;
                }

                Step newStep = new Step(newX, newY, toExpand);

                if (explored.contains(newStep)){
                    continue; //already added to stack
                }

                boolean isGoal = false;
                for (Goal goalTemp : State.GOALS){
                    if (goalTemp.row == newY && goalTemp.column == newX){
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

    public ArrayList<Point> getPath (State state, Agent startAgent, Box goalBox, int id, int color) {
        Point start = new Point(startAgent.column, startAgent.row);
        Point goal = new Point(goalBox.column, goalBox.row);
        // make work for multigoal
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

                if (newX == goal.x && newY == goal.y){
                    ArrayList<Point> path = new ArrayList<>();
                    Step parent = toExpand;
                    while (parent != null){
                        for (Box box : state.boxes){
                            if (box.column == parent.col && box.row == parent.row && box.color != color){ //foreign box on path
                                System.err.println("Marked "+box.letter);
                                box.marked = true;
                                box.markedBy = id;
                                if (!dependenciesBlockingBoxes.containsKey(goalBox.letter)){
                                    dependenciesBlockingBoxes.put(goalBox.letter, new ArrayList<Box>());
                                }
                                dependenciesBlockingBoxes.get(goalBox.letter).add(box); //Add marked box
                            }
                        }
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

    public ArrayList<Point> getPath2 (State state, Box startBox, Goal goalGoal, int id, int color) {
        Point start = new Point(startBox.column, startBox.row);
        Point goal = new Point(goalGoal.column, goalGoal.row);
        // make work for multigoal
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

                if (newX == goal.x && newY == goal.y){
                    ArrayList<Point> path = new ArrayList<>();
                    Step parent = toExpand;
                    while (parent != null){
                        for (Box box : state.boxes){
                            if (box.column == parent.col && box.row == parent.row && box.color != color){ //foreign box on path
                                System.err.println("Marked "+box.letter);
                                box.marked = true;
                                box.markedBy = id;
                                if (!dependenciesBlockingBoxes.containsKey(goalGoal.letter)){
                                    dependenciesBlockingBoxes.put(goalGoal.letter, new ArrayList<Box>());
                                }
                                dependenciesBlockingBoxes.get(goalGoal.letter).add(box); //Add marked box
                            }
                        }
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


    public boolean isSatisfied(State state, Point bPoint){
        for (Box box : state.boxes){
            if (box.column == bPoint.x && box.row == bPoint.y){
                //box at point
                for (Goal goal : State.GOALS){
                    if (box.row == goal.row && box.column == goal.column && box.letter == goal.letter){
                        //System.err.println(box.letter+" satisfied at "+box.column+","+box.row+", "+bPoint.toString());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean boxBlocking(Box box){
        if (vitalPaths.containsKey(box.markedBy)){
            if (vitalPaths.get(box.markedBy).contains(new Point(box.column, box.row))){ //box blocking vital path
                return true;
            }
        }
        return false;
    }

    public int h(State n) {
        int h = 0;

        for (Box box : n.boxes) {
            if (box.marked){
                if (boxBlocking(box)){ 
                    h = h + 0; //punish for still blocking
                } else {
                    box.marked = false;
                }
            }

            boolean isSatisfied = true;
            if (dependencies.containsKey(box.letter)){
                for (Point depend: dependencies.get(box.letter)){ //for all the goals that musst be satisfied before our goal
                    if (!isSatisfied(n, depend)){ //if not all demand goal are satisfied
                        //System.err.println(box.letter+" is not satisfied: "+box.column+","+box.row);
                        // System.err.println(depend.toString());
                        isSatisfied = false;
                        break;
                    }
                }
            }
            if (dependenciesBlockingBoxes.containsKey(box.letter)){//if not all blocking boxes has been satisfied
                for (Box blockingBox : dependenciesBlockingBoxes.get(box.letter)){
                    for (Box realBox : n.boxes){
                        if (realBox.letter == blockingBox.letter){
                            if (boxBlocking(realBox)){
                                isSatisfied = false;
                                break;
                            }
                        }
                    }
                }
            }

            if (!isSatisfied){
                //not adjusting heuristic
                h = h + 100;
                continue;
            }

            if (!seenLetters.contains(box.letter)){
                seenLetters.add(box.letter);
                System.err.println("New Letter: "+box.letter);
                System.err.println(seenLetters.toString());
                for (Box boxtemp : n.boxes){
                    System.err.print(boxtemp.letter + ": "+boxtemp.marked+". ");
                }
            }

            Point p = new Point(box.row, box.column);
            int[][] distancesFromBox = distMaps.get(p);
            int minDistToAgent = 10000;
            int minDistToGoal = 10000;

            //Minimize distance from boxes to agents
            for (Agent agent : n.agents) {
                if (agent.color == box.color) {
                    int distanceToAgent = distancesFromBox[agent.row][agent.column];
                    if (distanceToAgent < minDistToAgent) {
                        minDistToAgent = distanceToAgent;
                    }
                }
            }

            //Minimize distance from boxes to goals
            boolean hasGoal = false;
            for (Goal goal : State.GOALS) {
                if (goal.letter == box.letter){
                    hasGoal = true;
                    int distanceToGoal = distancesFromBox[goal.row][goal.column];
                    if (distanceToGoal < minDistToGoal){
                        minDistToGoal = distanceToGoal;
                    }
                }
            }
            if (!hasGoal){
                minDistToGoal = 0;
            }

            //Point boxPoint = new Point(box.column, box.row);
            //System.err.println("At "+box.column+","+box.row+" - "+box.letter);
            // if (isSatisfied(n, boxPoint)){
            //     minDistToAgent = (int) (minDistToAgent*-0.5);
            // }

            h = h + minDistToAgent;
            h = h + minDistToGoal;
        }

        return h;
    }

    public void printDistMaps(HashMap<Character, int[][]> distMaps){
        System.err.println("DistMap:");
        for (char key: distMaps.keySet()){ //for each distMap
            for (int[] line: distMaps.get(key)){
                for (int val: line){
                    System.err.print(val+", ");
                }
                System.err.println();
            }
            System.err.println("-----");
        }
    }
}

