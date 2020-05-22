package IIOO.masystem.decentralized;

import IIOO.masystem.*;

import java.util.ArrayList;
import java.lang.Thread;

public class AgentAI implements Runnable {

    private Thread thread;
    private Agent agent;
    private final int agentIdx;
    private final BestFirstStrategy bestFirstStrategy;
    private final State state;
    private ArrayList<Objective> newObjectives; // TODO: will should replace the objectives field.
    private ArrayList<Goal> objectives; // Objectives are goal positions
    private ArrayList<State> plan;
    private int planIdx = 0;
    private String finalSearchStatus = null;

    public AgentAI(Agent agent, int agentIdx, BestFirstStrategy bestFirstStrategy, State state) {
        this.agent = agent;
        this.agentIdx = agentIdx;
        this.bestFirstStrategy = bestFirstStrategy;
        this.state = state;
        this.objectives = new ArrayList<>();
    }

    public Agent getAgent() {
        return this.agent;
    }

    public State getState() {
        return this.state;
    }

    public boolean hasObjective() {
        return newObjectives != null && !newObjectives.isEmpty();
    }

    public boolean hasPlan() {
        return plan != null && planIdx < plan.size();
    }

    public Command getCommand(State state) {
        if (hasObjective()) {
            if (!hasPlan()) {
                search();
            }
            if (!hasPlan()) { // It could find a solution to its objective
                
                return new Command();
            }
            
            Command command = plan.get(planIdx).actions.get(0);
            planIdx++;
            return command;
        } else {
            return new Command();
        }
    }

    public void search() {

        bestFirstStrategy.addToFrontier(state);

        while (true) {
            if (bestFirstStrategy.frontierIsEmpty()) {
                finalSearchStatus = "Didn't manage to find a plan";
                break;
            }

            DecentralizedState leafState = (DecentralizedState)bestFirstStrategy.getAndRemoveLeaf();

            if (objectivesReached(leafState)) {
                finalSearchStatus = bestFirstStrategy.searchStatus(false);
                plan = leafState.extractPlan();
                planIdx = 0;
                break;
            }

            bestFirstStrategy.addToExplored(leafState);

            leafState.setAgentIdx(agentIdx);
            for (State n : leafState.getExpandedStates()) {
                if (!bestFirstStrategy.isExplored(n) && !bestFirstStrategy.inFrontier(n)) {
                    bestFirstStrategy.addToFrontier(n);
                }
            }
        }
    }

    private boolean objectivesReached(State leafState) {
        for (Goal objective : objectives) {
            int row = objective.row;
            int col = objective.column;
            char goalChar = objective.letter;

            boolean goalHasBox = false;
            for (Box box : leafState.boxes) {
                if (box.letter == goalChar && box.row == row && box.column == col) {
                    goalHasBox = true;
                    break;
                }
            }
            if (!goalHasBox) {
                return false;
            }
        }
        return true;
    }

    public int countObjectives() {
        return objectives.size();
    }

    public void addObjective(Goal objective) {
        this.objectives.add(objective);
    }

    @Override
    public String toString() {
        return "AgentAI for AgentNumber: " + this.agentIdx + " and " + this.agent.toString();
    }

    @Override
    public void run() {
        search();
    }

    public void start() {
        thread = new Thread(this, "thread");
        thread.start();
    }

    public Thread.State runningStatus() {
        return thread.getState();
    }

    public String searchStatus() {
        if (thread.getState() != Thread.State.TERMINATED){
            return String.format("\t\t\t%s",bestFirstStrategy.searchStatus(false));
        }

        return String.format("Done: \t%s", finalSearchStatus);
    }

    public ArrayList<State> getPlan() {

        try {
            thread.join();
            return this.plan;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void decreasePlanIdx(int amount) {
        planIdx -= amount;
    }

    public Command goBack() {
        decreasePlanIdx(2);
        return plan.get(planIdx).actions.get(0).invert();
    }


}