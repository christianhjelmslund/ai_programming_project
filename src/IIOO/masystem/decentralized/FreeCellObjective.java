package IIOO.masystem.decentralized;
import IIOO.masystem.State;

public class FreeCellObjective extends Objective {
    public int agentId;

    public FreeCellObjective(int row, int col, int agentId) {
        super(row, col);
        this.agentId = agentId;
    }

    @Override
    public boolean isReached(State state) {
        return state.cellIsFree(this.getRow(), this.getCol());
    }
    
}