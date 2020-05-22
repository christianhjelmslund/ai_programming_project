package IIOO.masystem;


public class Box extends MoveableObject {

    public final char letter;
    public Goal assignedGoal;


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (this.getClass() != o.getClass())
            return false;
        Box other = (Box) o;
        return this.row == other.row && this.column == other.column && this.letter == other.letter; //Color is unnecessary as boxes of same letter cannot have different colors
    }

    public Box(int row, int column, int color, char letter, Goal assignedGoal) {
        super(row, column, color);
        this.letter = letter;
        this.assignedGoal = assignedGoal;
    }

    @Override
    public String toString() {
        return letter + ": (" + row + "," + column + ")";
    }
}
