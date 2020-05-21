package CENmasystem;

public class Agent extends MoveableObject {

    public Agent() {

    }
    public Agent(int row, int column, int color){
        super(row,column,color);
    }

    @Override
    public boolean equals(Object object){
        Agent agent = (Agent) object;
        return agent.row == this.row && agent.column == this.column && agent.color == this.color;
    }

    @Override
    public String toString() {
       return "AgentColor: " + Integer.toString(color) + " At: " + Integer.toString(row) + "," +  Integer.toString(column);
    }
}
