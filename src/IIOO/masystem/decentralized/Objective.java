package IIOO.masystem.decentralized;
import IIOO.masystem.State;

public abstract class Objective {
    private int row;
    private int column;

    public Objective(int row, int col) {
        this.row = row;
        this.column = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return column;
    }

    public abstract boolean isReached(State state);
}