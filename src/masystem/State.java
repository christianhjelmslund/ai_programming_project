package masystem;

import com.google.common.collect.Sets;

import java.awt.*;
import java.util.*;
import java.util.List;

public class State {
    private static final Random RNG = new Random(1);

    //All static variables will be overwritten when reading the level
    public static int MAX_ROW = 70;
    public static int MAX_COL = 70;
    public static int NUMBER_OF_AGENTS = 0;
    public static int NUMBER_OF_BOXES = 0;
    public static boolean[][] WALLS = new boolean[MAX_ROW][MAX_COL];
    public static char[][] GOALS = new char[MAX_ROW][MAX_COL];


    public  Agent[] agents;
    public Box[] boxes;

    public State parent;
    public List<Command> actions;

    private int g; //depth in graph

    private int _hash = 0;


    public State(State parent, Box[] boxes, Agent[] agents) {
        this.boxes =  boxes;
        this.agents = agents;

        this.parent = parent;
        if (parent == null) {
            this.g = 0;
        } else {
            this.g = parent.g() + 1;
        }
    }

    public int g() {
        return this.g;
    }

    public boolean isInitialState() {
        return this.parent == null;
    }

    public boolean isGoalState() {

        for (Box box: boxes) {
            if(!(GOALS[box.row][box.column] > 0 && GOALS[box.row][box.column] == box.letter)){
                return false;
            }
        }
        return true;
    }

    public Set<List<Command>> calcExpandedStates() {
        Set<Command>[] agentCommands = new Set[NUMBER_OF_AGENTS];
        // For each agent add all valid commands to Set
        for (int i = 0; i < agentCommands.length; i++) {
            agentCommands[i] = new HashSet<>();
            for (int j = 0; j < Command.EVERY.length; j++) {
                if (isValidCommand(agents[i], Command.EVERY[j])) {
                    agentCommands[i].add(Command.EVERY[j]);
                }
            }
        }
        return Sets.cartesianProduct(agentCommands);
    }

    // Checks if an individual command performed by a certain agent is valid
    public boolean isValidCommand(Agent agent, Command cmd) {
        if (cmd.actionType == Command.Type.NoOp)
            return true;

        int newAgentRow = agent.row + Command.dirToRowChange(cmd.dir1);
        int newAgentCol = agent.column + Command.dirToColChange(cmd.dir1);
        Box box;

        if (newAgentRow < 0 || newAgentRow > this.MAX_ROW || newAgentCol < 0 || newAgentCol > this.MAX_COL){
            return false;
        }

        switch (cmd.actionType) {
            case Move:
                return cellIsFree(newAgentRow, newAgentCol);
            case Push:
                box = getBox(newAgentRow, newAgentCol);
                if (box == null || agent.color != box.color)
                    return false;

                int boxRow = newAgentRow + Command.dirToRowChange(cmd.dir2);
                int boxCol = newAgentCol + Command.dirToColChange(cmd.dir2);

                return cellIsFree(boxRow, boxCol);
            case Pull:
                // Cell is free where agent is going
                int boxRowPull = agent.row + Command.dirToRowChange(cmd.dir2);
                int boxColPull = agent.column + Command.dirToColChange(cmd.dir2);
                box = getBox(boxRowPull, boxColPull);


                return cellIsFree(newAgentRow, newAgentCol) && box != null && agent.color == box.color;
                // .. and there's a box in "dir2" of the agent
            default:
                throw new IllegalArgumentException("Command " + cmd + " was not of type Move, Pull, Push or NoMove");

        }
    }


//    private void updateBoxListInChildState(int oldBoxRow, int oldBoxCol, int newBoxRow, int newBoxCol, State childState){
//        Point oldBoxPos = new Point(oldBoxRow, oldBoxCol);
//        Box box = childState.boxes.get(oldBoxPos);
//        childState.boxes.remove(oldBoxPos);
//        box.row = newBoxRow;
//        box.column = newBoxCol;
//        childState.boxes.put(new Point(newBoxRow,newBoxCol),box);
//    }

    public ArrayList<State> getExpandedStates() {
        // TODO: following method calculates the new set of expanded states
        //  but is currently not used. How we handle the expanded states
        //  still needs to be implemented.
        Set<List<Command>> expandedStatesCombinations = calcExpandedStates();
        //TODO: Generalize to several agents - remember to check for conflicts
        ArrayList<State> expandedStates = new ArrayList<>(Command.EVERY.length);


        // TODO: Fjern kombinationen hvor alle agenter laver NoMove
        Set<Point> reservedSpots = new HashSet<>();
        Set<Point> reservedBoxes = new HashSet<>();
        State childState;



        for (List<Command> agentCmds :
                expandedStatesCombinations) {

            reservedSpots.clear();
            reservedBoxes.clear();

            childState = ChildState();
            boolean noConflictMove = true;

            // We use logic from theory assignment about when two actions cause a conflict
            for (int i = 0; i < agentCmds.size(); i++) {
                Command agentCmd = agentCmds.get(i);

                int reservedSpotsAmount = reservedSpots.size();
                int reservedBoxesAmount = reservedBoxes.size();
                int agentRow = agents[i].row;
                int agentCol = agents[i].column;
                int newReservedRow;
                int newReservedCol;
                int boxRow;
                int boxCol;
                Point oldBoxPos;
                Box box;

                switch (agentCmd.actionType) {
                    case Pull:
                        newReservedRow = agentRow + Command.dirToRowChange(agentCmd.dir1);
                        newReservedCol = agentCol + Command.dirToColChange(agentCmd.dir1);
                        boxRow = agentRow + Command.dirToRowChange(agentCmd.dir2);
                        boxCol = agentCol + Command.dirToColChange(agentCmd.dir2);

                        reservedBoxes.add(new Point(boxRow, boxCol));

                        reservedSpots.add(new Point(newReservedRow, newReservedCol));
                        reservedSpots.add(new Point(agentRow, agentCol));


                        if (reservedBoxesAmount + 1 != reservedBoxes.size() || reservedSpotsAmount + 2 != reservedSpots.size())
                            noConflictMove = false;

                          if (noConflictMove){ //Only update when noConflictMove - otherwise two agents may try to move same box in the child state
                              childState.agents[i].move(newReservedRow, newReservedCol);
                              childState.getBox(boxRow,boxCol).move(agentRow, agentCol);
                          }
                        break;
                    case Move:
                        newReservedRow = agentRow + Command.dirToRowChange(agentCmd.dir1);
                        newReservedCol = agentCol + Command.dirToColChange(agentCmd.dir1);
                        childState.agents[i].move(newReservedRow, newReservedCol);

                        reservedSpots.add(new Point(newReservedRow, newReservedCol));
                        if (reservedSpotsAmount + 1 != reservedSpots.size())
                            noConflictMove = false;
                        break;
                    case Push:
                        boxRow = agentRow + Command.dirToRowChange(agentCmd.dir1);
                        boxCol = agentCol + Command.dirToColChange(agentCmd.dir1);
                        newReservedRow = boxRow + Command.dirToRowChange(agentCmd.dir2);
                        newReservedCol = boxCol + Command.dirToColChange(agentCmd.dir2);
                        reservedBoxes.add(new Point(boxRow, boxCol));
                        reservedSpots.add(new Point(newReservedRow, newReservedCol));
                        reservedSpots.add(new Point(boxRow, boxCol));


                        if (reservedBoxesAmount + 1 != reservedBoxes.size() ||
                                reservedSpotsAmount + 2 != reservedSpots.size())
                            noConflictMove = false;

                        if (noConflictMove){ //Only update when noConflictMove - otherwise two agents may try to move same box in the child state
                            childState.agents[i].move(boxRow, boxCol);
                            childState.getBox(boxRow,boxCol).move(newReservedRow, newReservedCol);
                        }

                        break;
                    case NoOp:
                        newReservedRow = agentRow;
                        newReservedCol = agentCol;
                        reservedSpots.add(new Point(newReservedRow, newReservedCol));
                        if (reservedSpotsAmount + 1 != reservedSpots.size())
                            noConflictMove = false;
                        break;
                    default:
                        throw new IllegalArgumentException("Command " + agentCmd + " was not of type NoMove, Pull, Push or Move");
                }
            }

            if (noConflictMove) {

                // Add to expandedStates
                childState.actions = agentCmds;
                expandedStates.add(childState);

            }
        }

        Collections.shuffle(expandedStates, RNG);
        return expandedStates;
    }

    private boolean cellIsFree(int row, int col) {
        return !WALLS[row][col] && !hasAgent(row,col) && !hasBox(row,col);
    }

    public Box getBox(int row, int col) {
        for (Box box : boxes) {
            if (box.row == row && box.column == col) {
                return box;
            }
        }
        return null;
    }


    private boolean hasAgent(int row, int col) {
        for (Agent agent : agents) {
            if (agent.row == row && agent.column == col) {
                return true;
            }
        }
        return false;
    }

    private boolean hasBox(int row, int col) {
        for (Box box : boxes) {
            if (box.row == row && box.column == col) {
                return true;
            }
        }
        return false;
    }


    public Agent getAgent(int row, int col) {
        for (Agent agent : agents) {
            if (agent.row == row && agent.column == col) {
                return agent;
            }
        }
        return null;
    }



    public State ChildState() {
        Agent[] childAgents = new Agent[NUMBER_OF_AGENTS];
        Box[] childBoxes = new Box[NUMBER_OF_BOXES];
        Agent agent;
        Box box;

        for (int i = 0; i < NUMBER_OF_AGENTS; i++) {
            agent = agents[i];
            childAgents[i] = new Agent(agent.row, agent.column, agent.color);
        }

        for (int i = 0; i < NUMBER_OF_BOXES; i++) {
            box = boxes[i];
            childBoxes[i] = new Box(box.row, box.column, box.color, box.letter);
        }

        return new State(this, childBoxes, childAgents);
    }

    public ArrayList<State> extractPlan() {
        ArrayList<State> plan = new ArrayList<>();
        State n = this;
        while (!n.isInitialState()) {
            plan.add(n);
            n = n.parent;
        }
        Collections.reverse(plan);
        return plan;
    }

//    TODO: Check if we can remove hashCode() since we don't use map anymore
    @Override
    public int hashCode() {
        if (this._hash == 0) {
            final int prime = 31;
            int result = 1;
            // TODO: Think about if we need to do it for color and
            for (Agent agent : agents) {
                result = prime * result + agent.column;
                result = prime * result + agent.row;
                result = prime * result + agent.color;
            }

            for (Box box : boxes) {
                result = prime * result + box.column;
                result = prime * result + box.row;
                result = prime * result + box.color;
                result = prime * result + box.letter;
            }

            result = prime * result + Arrays.deepHashCode(GOALS);
            result = prime * result + Arrays.deepHashCode(WALLS);

            this._hash = result;
        }
        return this._hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (this.getClass() != obj.getClass())
            return false;

        State other = (State) obj;

        for (int i = 0; i < other.agents.length ; i++) {
            if (!agentArrayContains(this.agents, other.agents[i])){
                return false;
            }
        }

        for (int i = 0; i < other.boxes.length ; i++) {
            if (!boxArrayContains(this.boxes, other.boxes[i])){
                return false;
            }
        }

        return true;
    }

    public static boolean agentArrayContains(Agent[] agents, MoveableObject agent) {
        for (Agent agenti : agents) {
            if (agenti.equals(agent)) {
                return true;
            }
        }
        return false;

    }

    public static boolean boxArrayContains(Box[] boxes, MoveableObject box) {
        for (Box boxi : boxes) {
            if (boxi.equals(box)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        for (int row = 0; row < MAX_ROW; row++) {
            if (!WALLS[row][0]) {
                break;
            }
            for (int col = 0; col < MAX_COL; col++) {
                //TODO: update to put all boxes
                if (row == this.boxes[0].row && col == this.boxes[0].column) {
                    s.append(getBox(row,col).letter);
                } else if (GOALS[row][col] > 0) {
                    s.append(GOALS[row][col]);
                } else if (WALLS[row][col]) {
                    s.append("+");
                } else {
                    boolean blanc = true;
                    //agents
                    for (int i = 0; i < agents.length; i++) {
                        if (row == agents[i].row && col == agents[i].column){
                            s.append(i);
                            blanc = false;
                        }
                    }
                    //boxes
                    for (int i = 0; i < boxes.length; i++) {
                        if (row == boxes[i].row && col == boxes[i].column){
                            s.append(boxes[i].letter);
                            blanc = false;
                        }
                    }
                    if (blanc)
                        s.append(" ");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }
}