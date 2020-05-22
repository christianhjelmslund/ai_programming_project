package CENmasystem;

import CENheuristics.Heuristic;
import IIOO.masystem.Memory;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

public class BestFirstStrategy implements Comparator<State> {
    private HashSet<State> explored;
    private PriorityQueue<State> frontier;
    private HashSet<State> frontierSet;

    private final long startTime;

    public Heuristic heuristic;


    public BestFirstStrategy(Heuristic heuristic) {
        this.explored = new HashSet<>();
        this.startTime = System.currentTimeMillis();
        this.heuristic = heuristic;
        this.frontier = new PriorityQueue<>(this);
        this.frontierSet = new HashSet<>();
    }


    public int fAStar(State n) {
        return n.g() + this.heuristic.h(n);
    }


    @Override
    public int compare(State n1, State n2) {
        return fAStar(n1)-fAStar(n2);
    }


    public void addToExplored(State n) {
        this.explored.add(n);
    }

    public boolean isExplored(State n) {
        return this.explored.contains(n);
    }

    public int countExplored() {
        return this.explored.size();
    }

    public State getAndRemoveLeaf() {
        State state = frontier.poll();
        frontierSet.remove(state);
        return state;
    }

    public void addToFrontier(State n) {
        frontier.add(n);
        frontierSet.add(n);
    }

    public int countFrontier() {
        return frontier.size();
    }

    public boolean frontierIsEmpty() {
        return frontier.isEmpty();
    }

    public boolean inFrontier(State n) {
        return frontierSet.contains(n);
    }


    public String searchStatus() {
        return String.format("#Explored: %,6d, #Frontier: %,6d, #Generated: %,6d, Time: %3.2f s \t%s", this.countExplored(), this.countFrontier(), this.countExplored()+this.countFrontier(), this.timeSpent(), Memory.stringRep());
    }

    public float timeSpent() {
        return (System.currentTimeMillis() - this.startTime) / 1000f;
    }

    @Override
    public String toString() {
        return "Best-first Search using " + this.heuristic.toString();
    }


}




