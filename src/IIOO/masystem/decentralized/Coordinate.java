package IIOO.masystem.decentralized;

public class Coordinate {
    
    public int row;
    public int column;
    public int agentIdx;

    public Coordinate(int row, int col, int agentIdx) {
        this.row = row;
        this.column = col;
        this.agentIdx = agentIdx;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj instanceof Coordinate) {
            Coordinate coord = (Coordinate) obj;
            if (coord.row == this.row && coord.column == this.column) {
                return true;
            }
        }
        return false;
    }
}