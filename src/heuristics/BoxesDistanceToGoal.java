package heuristics;

import masystem.State;

import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BoxesDistanceToGoal extends Heuristic {

    public BoxesDistanceToGoal(State initialState) {
        super(initialState);
        //Preprocess initial state if desired
    }

    @Override
    public int h(State n) {
        HashMap<String, Point> boxMap = new HashMap<>();
        HashMap<String, Point> goalMap = new HashMap<>();

        for (int i = 0; i < State.MAX_ROW ; i++) {
            for (int j = 0; j < State.MAX_COL ; j++) {
                if ('A' <= n.boxes[i][j]  && n.boxes[i][j]  <= 'Z'){
                    boxMap.put(String.valueOf(n.boxes[i][j]).toLowerCase(), new Point(i, j));
                }

                if ('a' <= State.goals[i][j]  && State.goals[i][j]  <= 'z'){
                    goalMap.put(String.valueOf(State.goals[i][j]).toLowerCase(), new Point(i, j));
                }
            }
        }

        AtomicInteger cost = new AtomicInteger(0);

        boxMap.forEach((k,v) -> {
            Point goal = goalMap.get(k);
            int xDistance = Math.abs(goal.x - v.x);
            int yDistance = Math.abs(goal.y - v.y);
            cost.addAndGet(xDistance + yDistance);
        });

        return cost.get();
    }
}
