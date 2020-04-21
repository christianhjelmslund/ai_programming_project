package masystem;

public class Box extends MoveableObject {

    public char letter;


    @Override
    public boolean equals(Object object){
        Box other = (Box) object;
        return this.row==other.row && this.column == other.column && this.letter == other.letter; //Color is unnecessary as boxes of same letter cannot have different colors
    }

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
