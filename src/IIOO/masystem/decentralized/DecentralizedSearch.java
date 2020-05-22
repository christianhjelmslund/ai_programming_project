package IIOO.masystem.decentralized;

import java.util.ArrayList;

import IIOO.masystem.*;
import IIOO.masystem.heuristics.Heuristic;

public class DecentralizedSearch {

    private Heuristic heuristic;
    public final DecentralizedState initialState;

    public DecentralizedSearch(DecentralizedState state) {
        initialState = state;
    }

    public ArrayList<State> Search(BestFirstStrategy bestFirstStrategy) {
        // The variables below might not need to be global
        this.heuristic = bestFirstStrategy.getHeuristic();
        ArrayList<AgentAI> agentAIs = new ArrayList<>();

        for (int i = 0; i < this.initialState.agents.length; i++) {
            DecentralizedState state = initialState.ChildState();
            state.parent = null;
            agentAIs.add(new AgentAI(this.initialState.agents[i], i, new BestFirstStrategy(this.heuristic), state));
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

//        if (!plansAreConsistent(plans)) {
//            replan();
//        }

        // Step 3: Use the first action of each plan to update the currentState
        ArrayList<State> finalPlan = new ArrayList<>();

        int longestPlan = 0;
        for (ArrayList<State> state : plans) {
            if (longestPlan < state.size()) {
                longestPlan = state.size();
            }
        }

        for (int i = 0; i < longestPlan; i++) {
            currentState = currentState.ChildState();
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
    }

    @Override
    public String toString() {
        return "Decentralized best-first Search using " + this.heuristic.toString();
    }
}