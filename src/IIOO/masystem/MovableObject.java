package IIOO.masystem;

public abstract class MovableObject {

    public int row;
    public int column;
    public int color;

    public MovableObject() {

    }

    public MovableObject(int row, int column, int color) {
        this.row = row;
        this.column = column;
        this.color = color;
    }

    public void move(int row, int column){
        this.row = row;
        this.column = column;
    }

}
