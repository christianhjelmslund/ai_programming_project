package masystem;

public class Box extends MoveableObject {

    public char letter;

    public Box() {

    }

    public Box(int row, int column, int color, char letter){
        super(row,column,color);
        this.letter = letter;
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
