package IIOO.masystem.centralized;

import IIOO.masystem.Agent;
import IIOO.masystem.Box;
import IIOO.masystem.Command;
import IIOO.masystem.State;
import com.google.common.collect.Sets;

import java.awt.*;
import java.util.*;
import java.util.List;

public class CentralizedState extends State {

    public CentralizedState(State parent, Box[] boxes, Agent[] agents) {
        super(parent, boxes, agents);
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

    @Override
    public ArrayList<State> getExpandedStates() {

        Set<List<Command>> expandedStatesCombinations = calcExpandedStates();
        ArrayList<State> expandedStates = new ArrayList<>(Command.EVERY.length);

        Set<Point> reservedSpots = new HashSet<>();
        Set<Point> reservedBoxes = new HashSet<>();
        State childState;
        int counterForNoOp;
        int reservedSpotsAmount;
        int reservedBoxesAmount;
        int agentRow;
        int agentCol;
        int newReservedRow;
        int newReservedCol;
        int boxRow;
        int boxCol;
        boolean noConflictMove;

        for (List<Command> agentCmds :
                expandedStatesCombinations) {

            reservedSpots.clear();
            reservedBoxes.clear();

            childState = ChildState();
            noConflictMove = true;
            counterForNoOp = 0;

            // We use logic from theory assignment about when two actions cause a conflict
            for (int i = 0; i < agentCmds.size(); i++) {
                Command agentCmd = agentCmds.get(i);

                reservedSpotsAmount = reservedSpots.size();
                reservedBoxesAmount = reservedBoxes.size();
                agentRow = agents[i].row;
                agentCol = agents[i].column;

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

                        if (noConflictMove) { //Only update when noConflictMove - otherwise two agents may try to move same box in the child state
                            childState.agents[i].move(newReservedRow, newReservedCol);
                            childState.getBox(boxRow, boxCol).move(agentRow, agentCol);
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

                        if (noConflictMove) { //Only update when noConflictMove - otherwise two agents may try to move same box in the child state
                            childState.agents[i].move(boxRow, boxCol);
                            childState.getBox(boxRow, boxCol).move(newReservedRow, newReservedCol);
                        }

                        break;
                    case NoOp:
                        counterForNoOp += 1;
                        if (counterForNoOp == agentCmds.size()) {
                            noConflictMove = false;
                            break;
                        }
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

    @Override
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
            childBoxes[i] = new Box(box.row, box.column, box.color, box.letter, box.assignedGoal);
        }

        return new CentralizedState(this, childBoxes, childAgents);
    }
}
