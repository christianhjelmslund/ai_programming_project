package DECmasystem;

import java.io.IOException;
import java.util.ArrayList;

import DECheuristics.Heuristic;

public class DecentralizedSearch {

    private ArrayList<AgentAI> agentAIs;
    private Heuristic heuristic;
    public State initialState;

    public DecentralizedSearch(CENmasystem.State cenState) throws Exception {

        Agent[] childAgents = new Agent[cenState.NUMBER_OF_AGENTS];
        Box[] childBoxes = new Box[cenState.NUMBER_OF_BOXES];
        Goal[] GOALS = new Goal[cenState.GOALS.length];


        for (int i = 0; i < cenState.NUMBER_OF_AGENTS; i++) {
            childAgents[i] = new Agent(cenState.agents[i].row, cenState.agents[i].column, cenState.agents[i].color);
        }

        for (int i = 0; i < cenState.NUMBER_OF_BOXES; i++) {
            childBoxes[i] = new Box(cenState.boxes[i].row, cenState.boxes[i].column, cenState.boxes[i].color, cenState.boxes[i].letter, null);

        }

        for (int i = 0; i < cenState.GOALS.length; i++) {
            GOALS[i] =new Goal(cenState.GOALS[i].row, cenState.GOALS[i].column, cenState.GOALS[i].letter);
        }

        initialState = new State(null, childBoxes, childAgents);
        initialState.GOALS = GOALS;
    }

    public ArrayList<State> Search(BestFirstStrategy bestFirstStrategy) throws IOException {
        // The variables below might not need to be global
        this.heuristic = bestFirstStrategy.getHeuristic();
        this.agentAIs = new ArrayList<>();

        for (int i = 0; i < this.initialState.agents.length; i++) {
            this.agentAIs.add(new AgentAI(this.initialState.agents[i], i, new BestFirstStrategy(this.heuristic), this.initialState));
        }

        System.err.format("Search starting with bestFirstStrategy %s.\n", this.toString());

        State currentState = this.initialState;

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

        if (!plansAreConsistent(plans)) {
            replan();
        }


        // Step 3: Use the first action of each plan to update the currentState
        ArrayList<State> finalPlan = new ArrayList<>();

        int planLength = 0;
        for (ArrayList<State> state : plans) {
            if (planLength < state.size()) {
                planLength = state.size();
            }
        }

        for (int i = 0; i < planLength; i++) {
            currentState = currentState.ChildState();
            currentState.actions = new ArrayList<>();

            for (int j = 0; j < plans.size(); j++) {
                Command c;
                try {
                    c = plans.get(j).get(i).actions.get(0);
                    currentState.executeCommand(c, j);
                } catch (Exception e) {
                    c = new Command();
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

        for (Goal goal : State.GOALS) {

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
            System.err.println(goal + " assigned to " + chosenAgentAI);
        }
    }

    private void replan() {
    }

    private boolean plansAreConsistent(ArrayList<ArrayList<State>> plans) {
        return true;
    }

//    private State updateState(State currentState, ArrayList<Command> commands) {
//        return null;
//    }
//
//    private Command determineCommand(State currentState, State newState) {
//        return null;
//    }

    public int countFrontier() {
        int count = 0;
        for (AgentAI agentAI : this.agentAIs) {
            count += agentAI.getBestFirstStategy().countFrontier();
        }
        return count;
    }

    public int countExplored() {
        int count = 0;
        for (AgentAI agentAI : this.agentAIs) {
            count += agentAI.getBestFirstStategy().countExplored();
        }
        return count;
    }

    public float timeSpent() {
        if (this.agentAIs.isEmpty()) {
            return 0;
        }
        return agentAIs.get(0).getBestFirstStategy().timeSpent();
    }

    public String searchStatus() {
        return String.format("#Explored: %,6d, #Frontier: %,6d, #Generated: %,6d, Time: %3.2f s \t%s", countExplored(), countFrontier(), countExplored() + countFrontier(), timeSpent(), Memory.stringRep());
    }

    @Override
    public String toString() {
        return "Decentralized best-first Search using " + this.heuristic.toString();
    }
}