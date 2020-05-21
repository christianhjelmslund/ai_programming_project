package IIOO.masystem.heuristics;

import IIOO.masystem.Agent;
import DECmasystem.Box;
import IIOO.masystem.Goal;
import DECmasystem.State;

import java.awt.*;
import java.util.HashMap;

public class PCDWithMaximizeDistToOtherColors extends Heuristic {
    HashMap<Point, int[][]> distMaps;


    public PCDWithMaximizeDistToOtherColors(State initialState) {
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

        for (Box box : n.boxes) {
            Point p = new Point(box.row, box.column);
            int[][] distancesFromBox = distMaps.get(p);
            int minDistToAgent = 10000;
            int minDistToGoal = 10000;
            int minDistToAgentOtherColor = 10000;
            int minDistToBoxOtherColor = 10000;

            for (Agent agent : n.agents) {
                int distanceToAgent = distancesFromBox[agent.row][agent.column];
                if (agent.color == box.color) {
                    if (distanceToAgent < minDistToAgent) {//Minimize distance from boxes to goals
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
            h = h + minDistToGoal;
            h = h + minDistToAgent;
            h = h - minDistToAgentOtherColor/100;
            h = h - minDistToBoxOtherColor/100;

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
