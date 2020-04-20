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
    public static Map <Character,Integer> BOXCOLORS = new HashMap(); //Boxes letter -> Box color
    public static boolean[][] WALLS = new boolean[MAX_ROW][MAX_COL];
    public static char[][] GOALS = new char[MAX_ROW][MAX_COL];

    public char[][] boxes;
    public ArrayList<Agent> agents;


// Arrays are indexed from the top-left of the level, with first index being row
// and second being column.
// Row 0: (0,0) (0,1) (0,2) (0,3) ...
// Row 1: (1,0) (1,1) (1,2) (1,3) ...
// Row 2: (2,0) (2,1) (2,2) (2,3) ...
// ...
// (Start in the top left corner, first go down, then go right)
// E.g. this.WALLS[2] is an array of booleans having size max_col.
// this.WALLS[row][col] is true if there's a wall at (row, col)
//

    public State parent;
    public List<Command> actions;

    private int g; //depth in graph

    private int _hash = 0;

    public State(State parent, char[][] boxes, ArrayList<Agent> agents) {
        this.boxes = boxes;
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
        for (int row = 1; row < MAX_ROW - 1; row++) {
            for (int col = 1; col < MAX_COL - 1; col++) {
                char g = GOALS[row][col];
                char b = boxes[row][col];
                if (g > 0 && b != g) {
                    return false;
                }
            }
        }
        return true;
    }

    public Set<List<Command>> calcExpandedStates() {
        Set<Command>[] agentCommands = new Set[agents.size()];

        // For each agent add all valid commands to Set
        for (int i = 0; i < agentCommands.length; i++) {
            agentCommands[i] = new HashSet<>();
            for (int j = 0; j < Command.EVERY.length; j++) {
                if (isValidCommand(agents.get(i), Command.EVERY[j])) {
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

        switch (cmd.actionType) {
            case Move:
                return cellIsFree(newAgentRow, newAgentCol);
            case Push:
                // Make sure that there's actually a box to move
//                System.err.println(newAgentRow);
//                System.err.println(newAgentCol);
//                System.err.println("-----");
//                System.err.println(this.boxAt(newAgentRow, newAgentCol));


                // System.err.println(BOXCOLORS.get(this.boxes[newAgentRow][newAgentCol]));
                if (!this.boxAt(newAgentRow, newAgentCol) ||
                        agent.color != BOXCOLORS.get(this.boxes[newAgentRow][newAgentCol]))
                    return false;

                int boxRow = newAgentRow + Command.dirToRowChange(cmd.dir2);
                int boxCol = newAgentCol + Command.dirToColChange(cmd.dir2);

                return cellIsFree(boxRow, boxCol);
            case Pull:
                // Cell is free where agent is going
                int boxRowPull = agent.row + Command.dirToRowChange(cmd.dir2);
                int boxColPull = agent.column + Command.dirToColChange(cmd.dir2);

                if (!cellIsFree(newAgentRow, newAgentCol) || !this.boxAt(boxRowPull, boxColPull) ||
                        agent.color != BOXCOLORS.get(this.boxes[boxRowPull][boxColPull]))
                    return false;

                // .. and there's a box in "dir2" of the agent
                return true;
            default:
                throw new IllegalArgumentException("Command " + cmd + " was not of type Move, Pull, Push or NoMove");

        }
    }

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
                int agentRow = agents.get(i).row;
                int agentCol = agents.get(i).column;
                int newReservedRow;
                int newReservedCol;
                int boxRow;
                int boxCol;

                switch (agentCmd.actionType) {
                    case Pull:
                        newReservedRow = agentRow + Command.dirToRowChange(agentCmd.dir1);
                        newReservedCol = agentCol + Command.dirToColChange(agentCmd.dir1);
                        boxRow = agentRow + Command.dirToRowChange(agentCmd.dir2);
                        boxCol = agentCol + Command.dirToColChange(agentCmd.dir2);

                        reservedBoxes.add(new Point(boxRow, boxCol));

                        reservedSpots.add(new Point(newReservedRow, newReservedCol));
                        reservedSpots.add(new Point(agentRow, agentCol));

                        childState.agents.get(i).row = newReservedRow;
                        childState.agents.get(i).column = newReservedCol;
                        childState.boxes[boxRow][boxCol] = 0;
                        childState.boxes[agentRow][agentCol] = boxes[boxRow][boxCol];

                        if (reservedBoxesAmount + 1 != reservedBoxes.size() ||
                                reservedSpotsAmount + 2 != reservedSpots.size())
                            noConflictMove = false;
                        break;
                    case Move:
                        //TODO: TJek at de ikke skifter plads
                        newReservedRow = agentRow + Command.dirToRowChange(agentCmd.dir1);
                        newReservedCol = agentCol + Command.dirToColChange(agentCmd.dir1);
                        childState.agents.get(i).row = newReservedRow;
                        childState.agents.get(i).column = newReservedCol;

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

                        childState.agents.get(i).row = boxRow;
                        childState.agents.get(i).column = boxCol;
                        childState.boxes[boxRow][boxCol] = 0;
                        childState.boxes[newReservedRow][newReservedCol] = boxes[boxRow][boxCol];
                        if (reservedBoxesAmount + 1 != reservedBoxes.size() ||
                                reservedSpotsAmount + 2 != reservedSpots.size())
                            noConflictMove = false;
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

    /*
    for (Command c : Command.EVERY) {
        // TODO: Just to avoid errors temporarily. Should be changed
        if (c.actionType == Command.Type.NoMove)
            continue;
        // Determine applicability of action
        int newAgentRow = this.agents.get(0).row + Command.dirToRowChange(c.dir1); //returns 0 if argument is neither E or W
        int newAgentCol = this.agents.get(0).column + Command.dirToColChange(c.dir1);

        if (c.actionType == Command.Type.Move) {
            // Check if there's a wall or box on the cell to which the agent is moving
            if (this.cellIsFree(newAgentRow, newAgentCol)) {
                State n = this.ChildState();
                n.action = c;
                n.agents.get(0).row = newAgentRow;
                n.agents.get(0).column = newAgentCol;
                expandedStates.add(n);
            }
        } else if (c.actionType == Command.Type.Push) {
            // Make sure that there's actually a box to move
            if (this.boxAt(newAgentRow, newAgentCol)) {
                int newBoxRow = newAgentRow + Command.dirToRowChange(c.dir2);
                int newBoxCol = newAgentCol + Command.dirToColChange(c.dir2);
                // .. and that new cell of box is free
                if (this.cellIsFree(newBoxRow, newBoxCol)) {
                    State n = this.ChildState();
                    n.action = c;
                    n.agents.get(0).row = newAgentRow;
                    n.agents.get(0).column = newAgentCol;
                    n.boxes[newBoxRow][newBoxCol] = this.boxes[newAgentRow][newAgentCol];
                    n.boxes[newAgentRow][newAgentCol] = 0;
                    expandedStates.add(n);
                }
            }
        } else if (c.actionType == Command.Type.Pull) {
            // Cell is free where agent is going
            if (this.cellIsFree(newAgentRow, newAgentCol)) {
                int boxRow = this.agents.get(0).row + Command.dirToRowChange(c.dir2);
                int boxCol = this.agents.get(0).column + Command.dirToColChange(c.dir2);
                // .. and there's a box in "dir2" of the agent
                if (this.boxAt(boxRow, boxCol)) {
                    State n = this.ChildState();
                    n.action = c;
                    n.agents.get(0).row = newAgentRow;
                    n.agents.get(0).column = newAgentCol;
                    n.boxes[this.agents.get(0).row][this.agents.get(0).column] = this.boxes[boxRow][boxCol];
                    n.boxes[boxRow][boxCol] = 0;
                    expandedStates.add(n);
                }
            }
        }
    }*/

        Collections.shuffle(expandedStates, RNG);
        return expandedStates;
    }

    // TODO: Tjek performance her
    private boolean cellIsFree(int row, int col) {

        boolean hasAgent = false;

        for (Agent agent :
                agents) {
            if (agent.row == row && agent.column == col){
                hasAgent = true;
                break;
            }
        }

        return !WALLS[row][col] && boxes[row][col] == 0 && !hasAgent;
    }

//TODO: CheckIfConflict...ish

    private boolean boxAt(int row, int col) {
        return this.boxes[row][col] > 0;
    }

    private State ChildState() {
        ArrayList<Agent> childAgents = new ArrayList<>();
        char[][] childBoxes = new char[MAX_ROW][MAX_COL];


        for (Agent agent : agents) {
            childAgents.add(new Agent(agent.row, agent.column, agent.color));
        } //Copy agents to child
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                childBoxes[row][col] = boxes[row][col];
            }
        }//Copy boxes to child

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

    @Override
    public int hashCode() {
        if (this._hash == 0) {
            final int prime = 31;
            int result = 1;
            for (Agent agent : this.agents) {
                result = prime * result + agent.column;
                result = prime * result + agent.row;
            }
            result = prime * result + Arrays.deepHashCode(this.boxes);
            result = prime * result + Arrays.deepHashCode(this.GOALS);
            result = prime * result + Arrays.deepHashCode(this.WALLS);
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
        if (this.agents.get(0).row != other.agents.get(0).row || this.agents.get(0).column != other.agents.get(0).column) //TODO: Extend to checking each agent by color?
            return false;
        if (!Arrays.deepEquals(this.boxes, other.boxes))
            return false;
        if (!Arrays.deepEquals(this.GOALS, other.GOALS))
            return false;
        return Arrays.deepEquals(this.WALLS, other.WALLS);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int row = 0; row < MAX_ROW; row++) {
            if (!this.WALLS[row][0]) {
                break;
            }
            for (int col = 0; col < MAX_COL; col++) {
                if (this.boxes[row][col] > 0) {
                    s.append(this.boxes[row][col]);
                } else if (this.GOALS[row][col] > 0) {
                    s.append(this.GOALS[row][col]);
                } else if (this.WALLS[row][col]) {
                    s.append("+");
                } else if (row == this.agents.get(0).row && col == this.agents.get(0).column) { //TODO: Extend to checking each agent
                    s.append("0");
                } else {
                    s.append(" ");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }


}