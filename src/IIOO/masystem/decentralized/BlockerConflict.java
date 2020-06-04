package IIOO.masystem.decentralized;

import java.util.ArrayList;

import IIOO.masystem.Command;

public class BlockerConflict extends Conflict {
    private final int row;
    private final int col;

    public BlockerConflict(int agentIdx, int row, int col) {
        super(agentIdx, -1);
        this.row = row;
        this.col = col;
    }

    @Override
    public void resolve(AgentAI[] agentAIs, ArrayList<Command> commands, ArrayList<Objective> objectives) {
        commands.set(agentIdx1, agentAIs[agentIdx1].goBack());
        objectives.add(new FreeCellObjective(row, col));
    }
}