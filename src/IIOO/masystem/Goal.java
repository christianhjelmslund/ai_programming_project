package IIOO.masystem;

public class Goal {
    public final int row;
    public final int column;
    public final char letter;

    public Goal(int row, int column, char letter){
        this.row = row;
        this.column = column;
        this.letter = letter;
    }

    @Override
    public String toString(){
        return "Goal: (" + row + "," + column+ ")" + letter;
    }
}
