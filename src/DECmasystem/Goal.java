package DECmasystem;

public class Goal {
    public int row;
    public int column;
    public char letter;

    public Goal(int row, int column, char letter){
        this.row = row;
        this.column = column;
        this.letter = letter;
    }

    @Override
    public String toString() {
        return "Goal letter: " + this.letter + " at " + this.row + "," + this.column;
    }
}
