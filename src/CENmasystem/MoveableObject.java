package CENmasystem;

public abstract class MoveableObject {

    public int row;
    public int column;
    public int color;

    public MoveableObject() {

    }

    public MoveableObject(int row, int column, int color) {
        this.row = row;
        this.column = column;
        this.color = color;
    }

    public void move(int row, int column){
        this.row = row;
        this.column = column;
    }

}
