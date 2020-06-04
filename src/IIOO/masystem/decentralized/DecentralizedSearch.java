package IIOO.masystem.decentralized;

import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable;

import IIOO.masystem.*;
import IIOO.masystem.Command.Type;
import IIOO.masystem.heuristics.DecentralizedHeuristic;
import IIOO.masystem.heuristics.Heuristic;

public class DecentralizedSearch {
    private final AgentAI[] agentAIs;
    private final Heuristic heuristic;
    public final DecentralizedState initialState;

    public DecentralizedSearch(DecentralizedState state, BestFirstStrategy bestFirstStrategy) {
        initialState = state;
        this.heuristic = bestFirstStrategy.getHeuristic();
        this.agentAIs = new AgentAI[State.NUMBER_OF_AGENTS];

        for (int i = 0; i < agentAIs.length; i++) {
            DecentralizedState childState = initialState.ChildState();
            childState.parent = null;
            this.agentAIs[i] = new AgentAI(this.initialState.agents[i], i, new BestFirstStrategy(this.heuristic));
        }
    }

    private ArrayList<Conflict> findConflicts(ArrayList<Command> commands, State state) {
        ArrayList<Conflict> conflicts = new ArrayList<>();
        ArrayList<Coordinate> reservedCoords = new ArrayList<>();

        Hashtable<Integer, Coordinate> fromCoordDict = new Hashtable<>();
        Hashtable<Integer, Coordinate> toCoordDict = new Hashtable<>();
        ArrayList<Coordinate> fromCoordsList = new ArrayList<>();
        ArrayList<Coordinate> toCoordsList = new ArrayList<>();

        for (int agentIdx = 0; agentIdx < commands.size(); agentIdx++) {
            Command command = commands.get(agentIdx);
            if (command.actionType == Type.NoOp) {
                continue;
            }
            Agent agent = state.agents[agentIdx];
            int row;
            int column;
            Coordinate toCoord = new Coordinate();
            Coordinate fromCoord = new Coordinate();
            switch (command.actionType) {
                case Move:
                    row = agent.row + Command.dirToRowChange(command.dir1);
                    column = agent.column + Command.dirToColChange(command.dir1);
                    fromCoord = new Coordinate(agent.row, agent.column, agentIdx);
                    toCoord = new Coordinate(row, column, agentIdx);
                    break;

                case Push:
                    int boxRow = agent.row + Command.dirToRowChange(command.dir1);
                    int boxCol = agent.column + Command.dirToColChange(command.dir1);
                    row = boxRow + Command.dirToRowChange(command.dir2);
                    column = boxCol + Command.dirToColChange(command.dir2);
                    fromCoord = new Coordinate(boxRow, boxCol, agentIdx);
                    toCoord = new Coordinate(row, column, agentIdx);
                    break;

                case Pull:
                    fromCoord = new Coordinate(agent.row, agent.column, agentIdx);
                    row = agent.row + Command.dirToRowChange(command.dir1);
                    column = agent.column + Command.dirToColChange(command.dir1);
                    toCoord = new Coordinate(row, column, agentIdx);
                    break;

                default:
                    break;
            }

            if (state.isValidCommand(agent, command)) {
                int coordIdx = reservedCoords.indexOf(toCoord);
                if (coordIdx == -1) {
                    reservedCoords.add(toCoord);
                } else {
                    FreeCellConflict conflict = new FreeCellConflict(reservedCoords.get(coordIdx).agentIdx, toCoord.agentIdx);
                    conflicts.add(conflict);
                }
            } else {
                toCoordDict.put(agentIdx, toCoord);
                fromCoordDict.put(agentIdx, fromCoord);
                
                toCoordsList.add(toCoord);
                fromCoordsList.add(fromCoord);
            }
        }

        for (Coordinate toCoord : toCoordsList) {
            int agentIdx = toCoord.agentIdx;
            Coordinate fromCoord = fromCoordDict.get(agentIdx);

            int conflictingCoordIdx = fromCoordsList.indexOf(toCoord);
            if (conflictingCoordIdx != -1) {
                Coordinate conflictingFromCoord = fromCoordsList.get(conflictingCoordIdx);
                int conflictingAgentIdx = conflictingFromCoord.agentIdx;
                Coordinate conflictingToCoord = toCoordDict.get(conflictingAgentIdx);

                if (fromCoord.equals(conflictingToCoord)) { // Two agents try to take eachothers place
                    conflicts.add(new DeadLockConflict(agentIdx, conflictingAgentIdx));
                }
            } else {
                conflicts.add(new BlockerConflict(agentIdx, toCoord.row, toCoord.column));
            }
        }

        return conflicts;
    }

    private void resolveConflicts(AgentAI[] agentAIs, ArrayList<Command> commands, ArrayList<Objective> objectives, State state) {
        ArrayList<Conflict> conflicts = findConflicts(commands, state);
        for (Conflict conflict : conflicts) {
            conflict.resolve(agentAIs, commands, objectives);
        }
    }

    private void assignFreeCellObjectives(ArrayList<Objective> freeCellObjectives, DecentralizedState currentState) {
        ArrayList<Objective> toBeRemoved = new ArrayList<>();
        for (Objective freeCellObjective : freeCellObjectives) {
            boolean foundStuffInCell = false;
            for (int i = 0; i < currentState.agents.length; i++) {
                if (currentState.agents[i].row == freeCellObjective.getRow() && currentState.agents[i].column == freeCellObjective.getCol()) {
                    agentAIs[i].addObjective(freeCellObjective);
                    toBeRemoved.add(freeCellObjective);
                    foundStuffInCell = true;
                    break;
                }
            }
            for (Box box : currentState.boxes) {
                if (box.row == freeCellObjective.getRow() && box.column == freeCellObjective.getCol()) {
                    DecentralizedHeuristic heu = (DecentralizedHeuristic) heuristic;
                    int[][] distsToBox = heu.distMaps.get(new Point(box.row, box.column));
                    int agentIdx = findNearestAgentToBox(box, distsToBox);
                    agentAIs[agentIdx].addObjective(freeCellObjective);
                    toBeRemoved.add(freeCellObjective);
                    foundStuffInCell = true;
                    break;
                }
            }

            if (!foundStuffInCell) {
                System.err.println(currentState);
                System.err.println("Should have found either agent or box on free cell");
                System.err.println("on cell row: " + freeCellObjective.getRow() + ", col: " + freeCellObjective.getCol());
                System.exit(1);
            }
        }

        for (Objective obj : toBeRemoved) {
            freeCellObjectives.remove(obj);
        }


    }

    public void printCommands(ArrayList<Command> commands) {
        for (int i = 0; i < commands.size(); i++) {
            System.err.println("Agent " + i + " does " + commands.get(i));
        }
    }

    public ArrayList<State> Search() {
        ArrayList<State> finalPlan = new ArrayList<>();

        DecentralizedState currentState = this.initialState;

        assignGoalObjectivesToAgentAIs(agentAIs, currentState);

        ArrayList<Objective> freeCellObjectives = new ArrayList<>();

        while (!currentState.isGoalState()) {
            ArrayList<Command> commands = new ArrayList<>();
            
            for (AgentAI agentAI : agentAIs) {
                commands.add(agentAI.getCommand(currentState));
            }
            resolveConflicts(agentAIs, commands, freeCellObjectives, currentState);
            
            assignFreeCellObjectives(freeCellObjectives, currentState);
            currentState = currentState.ChildState();
            currentState.executeCommands(commands);
            currentState.actions = commands;
            finalPlan.add(currentState);
        }

        return finalPlan;
    }

    private void assignGoalObjectivesToAgentAIs(AgentAI[] agentAIs, State state) {
        // TODO: make sure that agent goals are assigned to corresponding agents
        // Every goal is given to some agent

        for (Box box : state.boxes) {
            //Assign each goal as an objective (paired with the box assigned)
            Goal goal = box.assignedGoal;
            //Using dist map to find nearest agent
            DecentralizedHeuristic heu = (DecentralizedHeuristic) heuristic;
            int[][] distsToBox = heu.distMaps.get(new Point(box.row, box.column));
            if (goal != null) {
                int nearestAgentIdx = findNearestAgentToBox(box, distsToBox);
                agentAIs[nearestAgentIdx].addObjective(new GoalObjective(goal.row, goal.column, goal.letter));
                System.err.println("Assigned Goal " + goal.toString() + " to " + nearestAgentIdx);
            }
        }


        for (Goal goal : State.AGENTGOALS) {
            Objective agentGoal = new GoalObjective(goal.row, goal.column, goal.letter);
            int agentIdx = Character.getNumericValue(goal.letter);
            agentAIs[agentIdx].addObjective(agentGoal);
            System.err.println("Assigned Agent " + goal.toString() + " to " + agentIdx);
        }
    }

    private int findNearestAgentToBox(Box box, int[][] distsToBox) {
        int nearestAgentIdx = 0;
        int distToNearestAgent = Integer.MAX_VALUE;
        for (int i = 0; i < agentAIs.length; i++) {
            Agent agent = agentAIs[i].getAgent();
            int distToAgent = distsToBox[agent.row][agent.column];
            if (distToAgent < distToNearestAgent && agent.color == box.color) {
                nearestAgentIdx = i;
                distToNearestAgent = distToAgent;
            }
        }
        return nearestAgentIdx;
    }

    @Override
    public String toString() {
        return "Decentralized best-first Search using " + this.heuristic.toString();
    }
}