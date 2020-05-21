package DECmasystem;

import java.util.ArrayList;
import java.lang.Thread;

public class AgentAI implements Runnable {

    private Thread thread;
    private Agent agent;
    private int agentIdx;
    private BestFirstStrategy bestFirstStrategy;
    private State state;
    private ArrayList<Goal> objectives; // Objectives are goal positions
    private ArrayList<State> plan;
    private String finalSearchStatus = null;

    public AgentAI(Agent agent, int agentIdx, BestFirstStrategy bestFirstStrategy, State state) {
        this.agent = agent;
        this.agentIdx = agentIdx;
        this.bestFirstStrategy = bestFirstStrategy;
        this.state = state;
        this.objectives = new ArrayList<>();
    }

    public BestFirstStrategy getBestFirstStategy() {
        return this.bestFirstStrategy;
    }

    public Agent getAgent() {
        return this.agent;
    }

    public State getState() {
        return this.state;
    }

    public int getAgentIdx() {
        return this.agentIdx;
    }

    public void search() {

        bestFirstStrategy.addToFrontier(state);

        int iterations = 0;
        while (true) {
//            if (iterations == 5000) {
//                System.err.println(bestFirstStrategy.searchStatus());
//                iterations = 0;
//            }

            if (bestFirstStrategy.frontierIsEmpty()) {
                finalSearchStatus = "Didn't manage to find a plan";
                break;
            }

            State leafState = bestFirstStrategy.getAndRemoveLeaf();

            if (objectivesReached(leafState)) {
                finalSearchStatus = bestFirstStrategy.searchStatus(false);
                plan = leafState.extractPlan();
                break;
//                return leafState.extractPlan();
            }

            bestFirstStrategy.addToExplored(leafState);

            for (State n : leafState.getExpandedStatesOneAgent(this.agentIdx)) {
                if (!bestFirstStrategy.isExplored(n) && !bestFirstStrategy.inFrontier(n)) {
                    bestFirstStrategy.addToFrontier(n);
                }
            }
            iterations++;
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
            return bestFirstStrategy.searchStatus(false);
        }

        return "Done: " + finalSearchStatus;
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


}