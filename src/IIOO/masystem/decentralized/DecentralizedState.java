package IIOO.masystem.decentralized;

import IIOO.masystem.Agent;
import IIOO.masystem.Box;
import IIOO.masystem.Command;
import IIOO.masystem.State;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DecentralizedState extends State {
    private int agentIdx = -1;

    public DecentralizedState(State parent, Box[] boxes, Agent[] agents, int agentIdx) {
        super(parent, boxes, agents);
        this.agentIdx = agentIdx;
    }

    public DecentralizedState(State parent, Box[] boxes, Agent[] agents){
        super(parent, boxes, agents);
    }


    public Set<Command> calcExpandedStates() {
        Set<Command> agentCommands = new HashSet<>();
        for (int i = 0; i < Command.EVERY.length; i++) {
            if (isValidCommand(agents[agentIdx], Command.EVERY[i])) {
                agentCommands.add(Command.EVERY[i]);
            }
        }

        return agentCommands;
    }

    @Override
    public ArrayList<State> getExpandedStates() {
        Set<Command> validCommands = calcExpandedStates();
        ArrayList<State> expandedStates = new ArrayList<>();
        DecentralizedState childState;
        for (Command command : validCommands) {
            if (command.actionType != Command.Type.NoOp) {
                childState = (DecentralizedState)ChildState();
                childState.executeCommand(command);
                ArrayList<Command> actions = new ArrayList<>();
                actions.add(command);
                childState.actions = actions;

                expandedStates.add(childState);
            }
        }
        return expandedStates;
    }

    @Override
    public State ChildState() {
        Agent[] childAgents = new Agent[NUMBER_OF_AGENTS];
        Box[] childBoxes = new Box[NUMBER_OF_BOXES];

        for (int i = 0; i < NUMBER_OF_AGENTS; i++) {
            childAgents[i] = new Agent(agents[i].row, agents[i].column, agents[i].color);
        }

        for (int i = 0; i < NUMBER_OF_BOXES; i++) {
            childBoxes[i] = new Box(boxes[i].row, boxes[i].column, boxes[i].color, boxes[i].letter, boxes[i].assignedGoal);
        }

        return new DecentralizedState(this, childBoxes, childAgents, this.agentIdx);
    }

    public void executeCommands(ArrayList<Command> commands) {
        for (int agentIdx = 0; agentIdx < commands.size(); agentIdx++) {
            this.setAgentIdx(agentIdx);
            executeCommand(commands.get(agentIdx));
        }
    }

    public void executeCommand(Command command) {
        int agentRow = this.agents[agentIdx].row;
        int agentCol = this.agents[agentIdx].column;
        int newAgentRow;
        int newAgentCol;
        int boxRow;
        int boxCol;
        switch (command.actionType) {
            case Pull:
                newAgentRow = agentRow + Command.dirToRowChange(command.dir1);
                newAgentCol = agentCol + Command.dirToColChange(command.dir1);
                boxRow = agentRow + Command.dirToRowChange(command.dir2);
                boxCol = agentCol + Command.dirToColChange(command.dir2);

                this.agents[agentIdx].move(newAgentRow, newAgentCol);
                this.getBox(boxRow, boxCol).move(agentRow, agentCol);
                break;

            case Move:
                newAgentRow = agentRow + Command.dirToRowChange(command.dir1);
                newAgentCol = agentCol + Command.dirToColChange(command.dir1);
                this.agents[agentIdx].move(newAgentRow, newAgentCol);
                break;

            case Push:
                boxRow = agentRow + Command.dirToRowChange(command.dir1);
                boxCol = agentCol + Command.dirToColChange(command.dir1);
                this.agents[agentIdx].move(boxRow, boxCol);

                int prevBoxRow = boxRow;
                int prevBoxCol = boxCol;

                boxRow += Command.dirToRowChange(command.dir2);
                boxCol += Command.dirToColChange(command.dir2);

                if (this.getBox(prevBoxRow, prevBoxCol) == null) {

                    System.err.println("ERROR HERE");
                    System.err.println(prevBoxRow);
                    System.err.println(prevBoxCol);
                    for (Box box : boxes) {
                        System.err.println(box);
                    }
                }


                this.getBox(prevBoxRow,prevBoxCol).move(boxRow, boxCol);

                break;

            case NoOp:
                break;

            default:
                throw new IllegalArgumentException(
                        "Command " + command + " was not of type NoMove, Pull, Push or Move");
        }
    }

    public void setAgentIdx(int agentIdx) {
        this.agentIdx = agentIdx;
    }
}
