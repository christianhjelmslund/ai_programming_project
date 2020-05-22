package IIOO.masystem.decentralized;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import IIOO.masystem.*;
import IIOO.masystem.heuristics.Heuristic;

public class DecentralizedSearch {

    private ArrayList<AgentAI> agentAIs;
    private Heuristic heuristic;
    public final DecentralizedState initialState;

    public DecentralizedSearch(DecentralizedState state) {
        initialState = state;
    }

    private ArrayList<Conflict> findConflicts(ArrayList<Command> commands, State state) {
        ArrayList<Conflict> conflicts = new ArrayList<>();
        ArrayList<Coordinate> reservedCoords = new ArrayList<>();
        // TODO: The variables below should be dictionaries
        Hashtable<Integer, Coordinate> fromCoordDict = new Hashtable<>();
        Hashtable<Integer, Coordinate> toCoordDict = new Hashtable<>();

        ArrayList<Coordinate> failedActionFromCoords = new ArrayList<>();
        ArrayList<Coordinate> failedActionToCoords = new ArrayList<>();
        for (int agentIdx = 0; agentIdx < commands.size(); agentIdx++) {
            Command command = commands.get(agentIdx);
            Agent agent = state.agents[agentIdx];
            // state.isValidCommand(agent, command);
            switch (command.actionType) {
                case Move:
                    int row = agent.row + Command.dirToRowChange(command.dir1);
                    int column = agent.column + Command.dirToColChange(command.dir1);
                    Coordinate coord = new Coordinate(row, column, agentIdx);
                    
                    if (state.isValidCommand(agent, command)) {
                        int coordIdx = reservedCoords.indexOf(coord);
                        if (coordIdx == -1) {
                            reservedCoords.add(coord);
                        } else {
                            Conflict conflict = new Conflict(reservedCoords.get(coordIdx).agentIdx, coord.agentIdx, Conflict.Type.FreeCell);
                            conflicts.add(conflict);
                        }
                    } else {
                        toCoordDict.put(agentIdx, coord);
                        fromCoordDict.put(agentIdx, new Coordinate(agent.row, agent.column, agentIdx));

                        failedActionToCoords.add(coord);
                        failedActionFromCoords.add(new Coordinate(agent.row, agent.column, agentIdx));
                    }
                        
                    break;
            
                default:
                    break;
            }
        }

        for (Coordinate toCoordinate : failedActionToCoords) { //TODO: Change to dictionaries
            int agentIdx = toCoordinate.agentIdx;
            Coordinate fromCoordinate = null;
            Coordinate conflictingFromCoordinate = null;
            for (Coordinate c : failedActionFromCoords) {
                if (agentIdx == c.agentIdx) {
                    fromCoordinate = c;
                } else if (toCoordinate.equals(c)) {
                    conflictingFromCoordinate = c;
                }
            }

            if (conflictingFromCoordinate != null) {
                Coordinate conflictingToCoodinate = null;
                for (Coordinate c : failedActionToCoords) {
                    if (c.agentIdx == conflictingFromCoordinate.agentIdx) {
                        conflictingToCoodinate = c;
                    }
                }

                if (conflictingToCoodinate != null && fromCoordinate.equals(conflictingToCoodinate)) {
                    conflicts.add(new Conflict(agentIdx, conflictingToCoodinate.agentIdx, Conflict.Type.DeadLock));
                } else {
                    conflicts.add(new Conflict(agentIdx, conflictingFromCoordinate.agentIdx, Conflict.Type.Blocker));
                }
            } else {
                // TODO: add a blocker conflict. who is responsible
            }

        }

        return conflicts;
    }

    private ArrayList<Command> resolveConflicts(ArrayList<AgentAI> agentAIs, ArrayList<Command> commands, ArrayList<Objective> objectives, State state) {
        ArrayList<Conflict> conflicts = findConflicts(commands, state);
        for (Conflict conflict : conflicts) {
            int lowAgentIdx = conflict.getLowIdx();
            int highAgentIdx = conflict.getHighIdx();

            switch (conflict.conflictType) { // See Conflict.java for explanations of conflict types
                case FreeCell:
                    commands.set(highAgentIdx, new Command());
                    agentAIs.get(highAgentIdx).decreasePlanIdx(1);
                    break;
                
                case DeadLock:
                    commands.set(lowAgentIdx, new Command());
                    agentAIs.get(lowAgentIdx).decreasePlanIdx(1);

                    commands.set(highAgentIdx, agentAIs.get(highAgentIdx).goBack());
                    break;

                case Blocker:
                    commands.set(conflict.agentIdx1, new Command());
                    agentAIs.get(conflict.agentIdx1).decreasePlanIdx(1);

                    // TODO: add objective
                    // objectives.add(new FreeCellObjective(row, col));
                
                default:
                    System.err.println("Not a valid conflict type");
                    break;
            }
        }

        return commands;
    }

    private void assignFreeCellObjectives(ArrayList<AgentAI> agentAIs2, ArrayList<Objective> freeCellObjectives) {
        // TODO: implement this method
    
    }

    public ArrayList<State> searchV2(BestFirstStrategy bestFirstStrategy) {
        ArrayList<State> finalPlan = new ArrayList<>();
        // The variables below might not need to be global
        this.heuristic = bestFirstStrategy.getHeuristic();
        this.agentAIs = new ArrayList<>();

        for (int i = 0; i < this.initialState.agents.length; i++) {
            DecentralizedState state = (DecentralizedState) initialState.ChildState();
            state.parent = null;
            this.agentAIs.add(new AgentAI(this.initialState.agents[i], i, new BestFirstStrategy(this.heuristic), state));
        }

        DecentralizedState currentState = this.initialState;

        assignObjectivesToAgentAIs(agentAIs, currentState);

        ArrayList<Objective> freeCellObjectives = new ArrayList<>();

        while (!currentState.isGoalState()) {
            ArrayList<Command> commands = new ArrayList<>();
            assignFreeCellObjectives(agentAIs, freeCellObjectives);

            for (AgentAI agentAI : agentAIs) {
                commands.add(agentAI.getCommand(currentState));
            }

            commands = resolveConflicts(agentAIs, commands, freeCellObjectives, currentState);

            currentState = (DecentralizedState) currentState.ChildState();
            currentState.executeCommands(commands);
            currentState.actions = commands;
            finalPlan.add(currentState);
        }

        return finalPlan;
    }

    public ArrayList<State> Search(BestFirstStrategy bestFirstStrategy) {
        // The variables below might not need to be global
        this.heuristic = bestFirstStrategy.getHeuristic();
        this.agentAIs = new ArrayList<>();

        for (int i = 0; i < this.initialState.agents.length; i++) {
            DecentralizedState state = (DecentralizedState) initialState.ChildState();
            state.parent = null;
            this.agentAIs.add(new AgentAI(this.initialState.agents[i], i, new BestFirstStrategy(this.heuristic), state));
        }

        DecentralizedState currentState = this.initialState;

        // Step 1: Assign objectives to all AgentAIs
        assignObjectivesToAgentAIs(agentAIs, currentState);


        // Step 2: Have all AgentAIs extract a plan that solves their objectives
        ArrayList<ArrayList<State>> plans = new ArrayList<>();
        for (AgentAI agentAI : agentAIs) {
            agentAI.start();
        }

        SearchStatus searchStatus = new SearchStatus(agentAIs);

        for (AgentAI agentAI : agentAIs) {
            plans.add(agentAI.getPlan());
        }

        // Step 3: Use the first action of each plan to update the currentState
        ArrayList<State> finalPlan = new ArrayList<>();

        int longestPlan = 0;
        for (ArrayList<State> state : plans) {
            if (longestPlan < state.size()) {
                longestPlan = state.size();
            }
        }

        for (int i = 0; i < longestPlan; i++) {
            currentState = (DecentralizedState) currentState.ChildState();
            currentState.actions = new ArrayList<>();

            for (int planIndex = 0; planIndex < plans.size(); planIndex++) {
                Command c;

                if (plans.get(planIndex).size() <= i) {
                    c = new Command();
                } else {
                    c = plans.get(planIndex).get(i).actions.get(0);
                    currentState.setAgentIdx(planIndex);
                    currentState.executeCommand(c);
                }
                currentState.actions.add(c);
            }
            finalPlan.add(currentState);
        }

        searchStatus.terminate();
        return finalPlan;

    }

    private void assignObjectivesToAgentAIs(ArrayList<AgentAI> agentAIs, State state) {
        // TODO: make sure that agent goals are assigned to corresponding agents
        // Every goal is given to some agent

        for (Goal goal : State.BOXGOALS) {

            AgentAI chosenAgentAI = null;

            int minObjectiveCount = Integer.MAX_VALUE; // used to assign an objective to the agent with the fewest objectives

            for (AgentAI agentAI : agentAIs) {
                for (Box box : state.boxes) {
                    if (goal.letter != box.letter || agentAI.getAgent().color != box.color) {
                        continue;
                    }
                    if (agentAI.countObjectives() < minObjectiveCount) {
                        chosenAgentAI = agentAI;
                        minObjectiveCount = agentAI.countObjectives();
                    }
                }
            }
            chosenAgentAI.addObjective(goal);
        }

        for (Goal goal : State.AGENTGOALS) {
            agentAIs.get(Character.getNumericValue(goal.letter)).addObjective(goal);
            System.err.println("Assigned Agent " + goal.toString() + " to " + agentAIs.indexOf(agentAIs.get(Character.getNumericValue(goal.letter))));
        }


    }

    @Override
    public String toString() {
        return "Decentralized best-first Search using " + this.heuristic.toString();
    }
}