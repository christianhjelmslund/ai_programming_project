package IIOO.masystem.decentralized;
import IIOO.masystem.State;

public class FreeCellObjective extends Objective {

    public FreeCellObjective(int row, int col) {
        super(row, col);
    }

    @Override
    public boolean isReached(State state) {
        return state.cellIsFree(this.getRow(), this.getCol());
    }
    
}