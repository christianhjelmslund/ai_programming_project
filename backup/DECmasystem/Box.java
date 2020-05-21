package DECmasystem;

import IIOO.masystem.Goal;

public class Box extends MoveableObject {

    public char letter;
    public boolean marked = false;
    public Goal assignedGoal;


    @Override
    public boolean equals(Object object){
        Box other = (Box) object;
        return this.row==other.row && this.column == other.column && this.letter == other.letter; //Color is unnecessary as boxes of same letter cannot have different colors
    }

    public Box() {

    }

    public Box(int row, int column, int color, char letter, Goal assignedGoal){
        super(row,column,color);
        this.letter = letter;
        this.marked = false;
        this.assignedGoal = assignedGoal;
    }



    @Override
    public String toString() {
        return letter + ": (" + Integer.toString(row) + "," +  Integer.toString(column) + ")";
    }

//    public Box deepCopy(){
//        Box box = new Box();
//        box.column = column;
//        box.row = row;
//        box.color = color;
//        box.letter = letter;
//        return box;
//    }


}
