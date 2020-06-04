package IIOO.masystem.heuristics;

import IIOO.masystem.Agent;
import IIOO.masystem.Box;
import IIOO.masystem.Goal;
import IIOO.masystem.State;

import java.awt.*;
import java.util.*;

//TODO Make sure that agents do not *pull* boxes into corridors if they cannot fit.

public class DecentralizedHeuristic extends Heuristic {
    //Weights
    final int factorDistBoxToAgent = 1;
    final int factorDistBoxToGoal = 2;
    final int factorRewardForPlacingBoxAtGoal = State.MAX_ROW * State.MAX_COL;
    final int typicalPunishmentFactor = 50;
    final int distanceToKeepBoxesFromGoalsb4Turn = 3;

    public DecentralizedHeuristic(State initialState) {
        //Init dictionary for each cell in map, containing its distance map to every other cell
        initDistMapForAllCells();
        assignBoxesToGoals(initialState);
        initDependenciesOfBoxes(initialState);
        initCorridors();
        initBlockingBoxes(initialState);
    }

    public int h(State n) {
        int h = 0;

        int distsToBoxesFromAgents = getDistsToBoxesFromAgents(n); //Calculates dists from agents to boxes which are not at a goal
        int minDistsToAssignedGoal = getDistsToAssignedGoals(n); //Minimizes distance from goals to an initially assigned box, which were closest to the goal
        int punishmentsForAgentsNotMoving = punishmentsForAgentsNotMoving(n);

        h = h + distsToBoxesFromAgents * factorDistBoxToAgent;
        h = h + minDistsToAssignedGoal * factorDistBoxToGoal;
        h = h + punishmentsForAgentsNotMoving;
        h = h + moveAgentToAgentGoalWhenDone(n);

        return h * 2; //Resolves to weighted A*
    }

    public int getDistsToAssignedGoals(State n) {
        int sum = 0;
        for (Box box : n.boxes) {
            if (box.assignedGoal != null) {
                Goal goal = box.assignedGoal;
                int[][] distancesFromGoal = distMaps.get(new Point(goal.row, goal.column));
                int distToGoal = distancesFromGoal[box.row][box.column];

                if (isDependenciesSatisfied(n, box)) {
                    if (goal.row == box.row && goal.column == box.column) {
                        sum -= factorRewardForPlacingBoxAtGoal; //reward placing box at goal when dependencies are satisfied
                    } else {
                        sum += distToGoal;
                        for (Box boxOther : n.boxes) {
                            if (boxesAreAdjacent(box, boxOther)) {
                                sum += 1;
                                break;
                            }
                        }
                    }
                } else { //If dependencies for box is not satisfied, punish current state
                    sum += typicalPunishmentFactor;
                    if (distToGoal < distanceToKeepBoxesFromGoalsb4Turn) {
                        sum += distanceToKeepBoxesFromGoalsb4Turn - distToGoal; //Better the longer away
                    }
                }
            }
        }

        return sum;
    }

    public int getDistsToBoxesFromAgents(State n) {
        int maxDist = State.MAX_COL * State.MAX_ROW * State.MAX_ROW;
        int summedDistsForAgents = 0;
        int punishCorridor = 0;

        //Sum distances from every agent to nearest box
        for (Agent agent : n.agents) {
            Box minBox = null;
            int minDistToBox = maxDist;
            int minDistToBoxWithAssignedGoal = maxDist;
            int[][] distancesFromAgent = distMaps.get(new Point(agent.row, agent.column));
            for (int idx = 0; idx < n.boxes.length; idx++) {
                Box box = n.boxes[idx];
                int distToBox = distancesFromAgent[box.row][box.column];
                if (box.color == agent.color && !boxIsAtGoal(box)) { //Only minimize dist to boxes not at goal and moveable by agent:
                    //Check distances to boxes with assigned goals
                    if (box.assignedGoal != null && distToBox < minDistToBoxWithAssignedGoal && isDependenciesSatisfied(n, box)) {
                        minDistToBoxWithAssignedGoal = distToBox;
                        minBox = box;
                    }
                    //Check distances to every box
                    if (distToBox < minDistToBox) {
                        minDistToBox = distToBox;
                    }
                }
                ArrayList<Point> vitalPath = isBlocking.get(idx);
                // System.err.println(vitalPath);
                if (box.color == agent.color && vitalPath.size() > 0) { //is known to block
                    if (vitalPath.contains(new Point(box.column, box.row))) { //still on vital path, blocking
                        // Point blockedAgentPoint = vitalPath.get(vitalPath.size()-1);
                        // System.err.println("Prior "+box.letter);
                        summedDistsForAgents += distToBox * 3;
                        Point blockedAgent = vitalPath.get(vitalPath.size() - 1);
                        // System.err.println(blockedAgent.toString());
                        summedDistsForAgents += distMaps.get(new Point(box.row, box.column))[blockedAgent.y][blockedAgent.x];
                    }
                }
            }

            // If minDist is not changed => No boxes to move or all boxes at goal => don't increase heuristic value
            if (minDistToBox != maxDist) {
                //Prioritize minimizing dist to boxes with goals assigned
                if (minDistToBoxWithAssignedGoal != maxDist) {
                    summedDistsForAgents += minDistToBoxWithAssignedGoal;
                    if (minBox != null) {
                        //Punish agent if it pulls box into a corridor
                        Point boxPoint = new Point(minBox.column, minBox.row);
                        Point agentPoint = new Point(agent.column, agent.row);
                        if (corridor.containsKey(boxPoint) || corridor.containsKey(agentPoint)) { //minBox is at corridor

                            Point goalPoint = new Point(minBox.assignedGoal.row, minBox.assignedGoal.column);

                            if (distMaps.get(goalPoint)[minBox.row][minBox.column] > distMaps.get(goalPoint)[agent.row][agent.column]) {
                                punishCorridor += 2;
                            }
                        }
                    }
                } else { //Otherwise prioritize minimizing dist to any box
                    summedDistsForAgents += minDistToBox;
                }
            }
        }

        return summedDistsForAgents + punishCorridor;
    }
}
