package IIOO.masystem.decentralized;
import IIOO.masystem.State;
import IIOO.masystem.Box;
import IIOO.masystem.Agent;

public class GoalObjective extends Objective {
    private char character; 

    public GoalObjective(int row, int col, char c) {
        super(row, col);
        this.character = c;
    }

	@Override
	public boolean isReached(State state) {
        if ('0' <= character && character <= '9') { // Agent goal
            int i = Character.getNumericValue(character);
            Agent agent = state.agents[i];
            return agent.row == this.getRow() && agent.column == this.getCol();
        }

        for (Box box : state.boxes) { // Box goal
            if (box.letter == this.character && box.row == this.getRow() && box.column == this.getCol()) {
                return true;
            }
        }

		return false;
	}

}