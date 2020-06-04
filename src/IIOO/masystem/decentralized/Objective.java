package IIOO.masystem.decentralized;

import IIOO.masystem.State;
import java.util.Objects;

public abstract class Objective {
    private final int row;
    private final int column;

    public Objective(int row, int col) {
        this.row = row;
        this.column = col;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Objective objective = (Objective) o;
        return row == objective.row &&
                column == objective.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return column;
    }

    public abstract boolean isReached(State state);

}