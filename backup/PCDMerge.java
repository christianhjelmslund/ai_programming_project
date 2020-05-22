package IIOO.masystem.heuristics;

import IIOO.masystem.Agent;
import DECmasystem.Box;
import IIOO.masystem.Goal;
import DECmasystem.State;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class PCDMerge extends Heuristic {
    HashMap<Point, int[][]> distMaps;
    HashMap<Character, ArrayList<Point>> dependencies = new HashMap<>();
    public HashSet<Character> seenLetters = new HashSet<>();


    public PCDMerge(State initialState) {
        super(initialState);

        //Init dictionary for each cell in map, containing its distance map to every other cell
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
            Point pGoal = null;
            for (Goal goal : State.GOALS){
                if (goal.letter == box.letter){
                    pGoal = new Point(goal.column, goal.row);
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

    public int[][] createDistMap(int max_row, int max_col){
        //creates a 2d int array with all values being 100000
        int[][] distMap = new int[max_row][max_col];
        for (int x=0; x<max_row; x++){
            for (int y=0; y<max_col; y++){
                distMap[x][y] = 100000;
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

    //TODO: Expand to having some goals 'occupied'?
    //TODO: Add agents distance to a box?
    //TODO:

    public int h(State n) {
        int h = 0;


        for (Box box : n.boxes) {
            boolean isSatisfied = true;
            if (dependencies.containsKey(box.letter)){
                for (Point depend: dependencies.get(box.letter)){ //for all the goals that musst be satisfied before our goal
                    if (!isSatisfied(n, depend)){
                        //System.err.println(box.letter+" is not satisfied: "+box.column+","+box.row);
                        // System.err.println(depend.toString());
                        isSatisfied = false;
                        break;
                    }
                }
            }
            if (!isSatisfied){
                //not adjusting heuristic
                h = h + 50;
                continue;
            }

            //System.err.println(box.letter);

            //TODO: Forstår ikke hvad denne bruges til? Debug?
            if (!seenLetters.contains(box.letter)){
                seenLetters.add(box.letter);
                System.err.println("New Letter: "+box.letter);
                System.err.println(seenLetters.toString());
                System.err.println(n.toString());
            }

            Point p = new Point(box.row, box.column);
            int[][] distancesFromBox = distMaps.get(p);
            int minDistToAgent = 10000;
            int minDistToGoal = 10000;
            int minDistToAgentOtherColor = 10000;
            int minDistToBoxOtherColor = 10000;

            for (Agent agent : n.agents) {
                int distanceToAgent = distancesFromBox[agent.row][agent.column];
                if (agent.color == box.color) {
                    if (distanceToAgent < minDistToAgent) {
                        minDistToAgent = distanceToAgent;
                    }
                } else { //Maximize distance between Agent and Boxes of different colors
                    if (distanceToAgent < minDistToAgentOtherColor) {
                        minDistToAgentOtherColor = distanceToAgent;
                    }
                }
            }

            //Maximize distance between boxes of different colors
            for (Box boxi : n.boxes) {
                if (box.color != boxi.color){
                    int distanceToBox = distancesFromBox[boxi.row][boxi.column];
                    if (distanceToBox < minDistToBoxOtherColor){
                        minDistToBoxOtherColor = distanceToBox;
                    }
                }
            }


            //Minimize distance from boxes to goals
            for (Goal goal : n.GOALS) {
                if (goal.letter == box.letter){
                    int distanceToGoal = distancesFromBox[goal.row][goal.column];
                    if (distanceToGoal < minDistToGoal){
                        minDistToGoal = distanceToGoal;
                    }
                }
            }

            Point boxPoint = new Point(box.column, box.row);
            //System.err.println("At "+box.column+","+box.row+" - "+box.letter);
            if (isSatisfied(n, boxPoint)){
                minDistToAgent = (int) (minDistToAgent*-0.5);
            } else {
                h = h + minDistToAgent;
            }

            h = h + minDistToGoal;
            h = h - minDistToAgentOtherColor/100;
            h = h - minDistToBoxOtherColor/100;

        }
        return h;
    }


    //TODO: Kan vi slette denne?
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
