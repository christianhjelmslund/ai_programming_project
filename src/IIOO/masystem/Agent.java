package IIOO.masystem;


public class Agent extends MoveableObject {

    public Agent() {

    }
    public Agent(int row, int column, int color){
        super(row,column,color);
    }

    @Override
    public String toString() {
       return "AgentColor: " + color + " At: " + row + "," + column;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj instanceof Agent) {
            Agent agent = (Agent) obj;
            return agent.row == this.row
                    && agent.column == this.column
                    && agent.color == this.color;
        }
        return false;
    }
}
