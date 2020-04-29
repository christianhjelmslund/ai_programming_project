
/*package heuristics;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

import masystem.Box;
import masystem.State;

public class PreCalculatedDistances extends Heuristic {

    HashMap<Character, int[][]> distMaps;
    ArrayList<int[]> goals = new ArrayList<int[]>();


    public PreCalculatedDistances(State initialState) {
        super(initialState);
        //make dictionary for each goal, containing its distance map
        //goals with same letter share their dist map
        distMaps = new HashMap<Character, int[][]>();
        for (int x=0; x<State.GOALS.length; x++) {
            for (int y = 0; y < State.GOALS[x].length; y++) {
                char letter = State.GOALS[x][y]; // get char
                int intLetter = letter; // enable us to check if letter == null
                if (intLetter != 0) { // got a goal
                    goals.add(new int[] { x, y }); // add goal to goals
                    int[][] distMap;
                    if (distMaps.containsKey(letter)) { // extend twin goal's distmap
                        distMap = distMaps.get(letter);
                    } else {
                        distMap = createDistMap(State.MAX_ROW, State.MAX_COL); // create new distmap
                    }
                    distMap = getDistMapRec(distMap, x, y, initialState, 0); // get distmap
                    distMaps.put(letter, distMap); // add to dict
                }
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

        ArrayList<int[]> boxes = new ArrayList<int[]>();

        for (Box box: n.boxes ) {
            boxes.add(new int[] { box.row, box.column});
        }


        for (int[] goalCoords : goals) { // for each goal, get coords and letter
            int goalX = goalCoords[0];
            int goalY = goalCoords[1];
            char goalLetter = State.GOALS[goalX][goalY];
            int minDist = 10000;

            for (int[] boxCoords: boxes){
                int boxX = boxCoords[0];
                int boxY = boxCoords[1];
                Box box = n.getBox(boxX,boxY);

                if (box != null && box.letter == goalLetter){ //for all boxes with same letter as goal
                    int distToBox = distMaps.get(goalLetter)[boxX][boxY]; //get lowest dist to any of these boxes
                    if (distToBox < minDist){
                        minDist = distToBox;
                    }
                }
            }
            //System.err.println(minDist);
            h = h + minDist;
        }
        return h;
    }


    public int hOld(State n) {
        int h = 0;
        int loopCount = 0;
        for (int x=0; x<State.MAX_ROW; x++){
            for (int y=0; y<State.MAX_COL; y++){
                char letter = n.boxes[x][y];
                int intLetter = letter;
                if (intLetter != 0){ //for all boxes
                    h += distMaps.get(Character.toLowerCase(letter))[x][y]; //add their distance to nearest goal to h
                    // Add Manhatten distance between agent and the box
                    // TODO: Expand to multiagent
                    if (h != 0) {
                        Agent a = n.agents[0];
                        h += 0.91 * (Math.abs(x - a.row) + Math.abs(y - a.column) - 1);
                    }
                }
            }
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

 */