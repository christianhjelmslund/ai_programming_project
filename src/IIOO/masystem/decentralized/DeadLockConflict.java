package IIOO.masystem.decentralized;

import java.util.ArrayList;

import IIOO.masystem.Command;
import IIOO.masystem.Command.Type;

public class DeadLockConflict extends Conflict {
    public DeadLockConflict(int agentIdx1, int agentIdx2) {
        super(agentIdx1, agentIdx2);
    }

    @Override
    public void resolve(AgentAI[] agentAIs, ArrayList<Command> commands, ArrayList<Objective> objectives) {
        int lowAgentIdx = this.getLowIdx();
        int highAgentIdx = this.getHighIdx();

        if (commands.get(lowAgentIdx).actionType != Type.NoOp) { // Otherwise the conflict has already been handled
            commands.set(lowAgentIdx, new Command());
            agentAIs[lowAgentIdx].decreasePlanIdx(1);
            
            commands.set(highAgentIdx, agentAIs[highAgentIdx].goBack());
        }
    }
}