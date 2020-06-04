package IIOO.masystem.decentralized;

import java.util.ArrayList;

import IIOO.masystem.Command;

public abstract class Conflict {
    public final int agentIdx1;
    public final int agentIdx2;

    // Conflict types explanations
    // DeadLock:    Two agentAIs are somehow blocking the action of the other. 
    //              Neither can make their move.
    //              The agentAI with highest idx will go back one state in its plan.
    //              The other agent will do a NoOp
    // FreeCell:    Two agentAIs both try to use the same free cell in their action.
    //              The agent with lowest idx will perform its action.
    //              The other agent will do a NoOp
    // Blocker:     An agent (A2) somehow blocks the plan of another agent (A1).
    //              Agent (A1) will create a free cell objective, which should be assigned to agent (A2).

    public Conflict(int aIdx1, int aIdx2) {
        this.agentIdx1 = aIdx1;
        this.agentIdx2 = aIdx2;
    }

    public int getLowIdx() {
        return Math.min(agentIdx1, agentIdx2);
    }

    public int getHighIdx() {
        return Math.max(agentIdx1, agentIdx2);
    }

	public abstract void resolve(AgentAI[] agentAIs, ArrayList<Command> commands, ArrayList<Objective> objectives);
}