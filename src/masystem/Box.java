package masystem;

public class Box {

    public int row;
    public int column;
    public int color;
    public char letter;

    public Box deepCopy(){
        Box box = new Box();
        box.column = column;
        box.row = row;
        box.color = color;
        box.letter = letter;
        return box;
    }

}
