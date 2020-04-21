package heuristics;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

import masystem.Agent;
import masystem.Box;
import masystem.State;

public class PreCalcDistForCompleteMap extends Heuristic {
    HashMap<Point, int[][]> distMaps;
    ArrayList<int[]> goals = new ArrayList<int[]>(); //Used to loop over all goals faster than traversing every column*row


    public PreCalcDistForCompleteMap(State initialState) {
        super(initialState);
        //Init dictionary for each cell in map, containing its distance map to every other cell
        distMaps = new HashMap<Point, int[][]>();

        for (int x = 0; x < State.WALLS.length; x++) {
            for (int y = 0; y < State.WALLS[x].length; y++) {

                if (State.WALLS[x][y])
                    continue; //Only calculate distances from where there are no walls

                if (State.GOALS[x][y] >= 'A' && State.GOALS[x][y] <='Z')
                    goals.add(new int[] { x, y }); // add goal to goals for
                    int[][] distMap = createDistMap(State.MAX_ROW, State.MAX_COL);
                    distMap = getDistMapRec(distMap, x, y, initialState, 0); //Calc distances from cell (x,y) to every other cell
                    distMaps.put(new Point(x, y), distMap);

            }
        }
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

    //TODO: Expand to having some goals 'occupied'?
    //TODO: Add agents distance to a box?
    //TODO:

    public int h(State n) {
        int h = 0;
        int loopCount = 0;
        for (Box box : n.boxes) {
            Point p = new Point(box.row, box.column);
            int[][] distancesFromBox = distMaps.get(p);
            int minDistToAgent = 10000;
            int minDistToGoal = 10000;

            //Minimize distance from boxes to agents
            for (Agent agent : n.agents) {
                if (agent.color == box.color){
                    int distanceToAgent = distancesFromBox[agent.row][agent.column];
                    if (distanceToAgent < minDistToAgent){
                        minDistToAgent = distanceToAgent;
                    }
                }
            }

            //Minimize distance from boxes to goals
            for (int[] goal : goals) {
                if (State.GOALS[goal[0]][goal[1]] == box.letter){
                    int distanceToGoal = distancesFromBox[goal[0]][goal[1]];
                    if (distanceToGoal < minDistToGoal){
                        minDistToGoal = distanceToGoal;
                    }
                }
            }
            h = h + minDistToGoal;
            h = h + minDistToAgent;
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

