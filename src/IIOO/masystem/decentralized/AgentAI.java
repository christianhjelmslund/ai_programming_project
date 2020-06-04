package IIOO.masystem.decentralized;

import IIOO.masystem.*;
import java.util.ArrayList;
import java.lang.Thread;
import java.util.Set;

import com.google.common.collect.Sets;

public class AgentAI implements Runnable {
    private Thread thread;
    private final Agent agent;
    private final int agentIdx;
    public final BestFirstStrategy bestFirstStrategy;
    private final Set<Objective> objectives;
    private ArrayList<State> plan;
    private int planIdx = 0;
    private String finalSearchStatus = null;

    public AgentAI(Agent agent, int agentIdx, BestFirstStrategy bestFirstStrategy) {
        this.agent = agent;
        this.agentIdx = agentIdx;
        this.bestFirstStrategy = bestFirstStrategy;
        this.objectives = Sets.newHashSet();
    }

    public Agent getAgent() {
        return this.agent;
    }

    public boolean hasObjective() {
        return objectives != null && !objectives.isEmpty();
    }

    public boolean hasPlan() {
        return plan != null && planIdx < plan.size();
    }

    public Command getCommand(State state) {
        if (hasObjective()) {
            if (objectivesReached(state)) {
                return new Command();
            }
            if (!hasPlan()) {
                search(state);
            }
            if (!hasPlan())
                return new Command();
            Command command = plan.get(planIdx).actions.get(0);
            planIdx++;
            return command;
        } else {
            return new Command();
        }
    }

    public void search(State state) {

        bestFirstStrategy.addToFrontier(state, objectives);

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
                    bestFirstStrategy.addToFrontier(n, objectives);
                }
            }
        }
    }

    private boolean objectivesReached(State leafState) {
        for (Objective objective : objectives) {
            if (!objective.isReached(leafState)) return false;
        }
        return true;
    }

    public void addObjective(Objective objective) {
        this.objectives.add(objective);
    }

    @Override
    public String toString() {
        return "AgentAI for AgentNumber: " + this.agentIdx + " and " + this.agent.toString();
    }

    @Override
    public void run() {
        // search();
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

    public boolean decreasePlanIdx(int amount) {
        if (planIdx - amount < 0) {
            return false;
        }
        planIdx -= amount;
        return true;

    }

    public Command goBack() {
        if (decreasePlanIdx(2)) {
            return plan.get(planIdx).actions.get(0).invert();
        }
        return new Command();
    }
}