package IIOO.masystem.decentralized;

import java.util.ArrayList;

import IIOO.masystem.Command;

public class FreeCellConflict extends Conflict {
    public FreeCellConflict(int agentIdx1, int agentIdx2) {
        super(agentIdx1, agentIdx2);
    }

    @Override
    public void resolve(AgentAI[] agentAIs, ArrayList<Command> commands, ArrayList<Objective> objectives) {
        int highAgentIdx = this.getHighIdx();
        commands.set(highAgentIdx, new Command());
        agentAIs[highAgentIdx].decreasePlanIdx(1);
    }
}