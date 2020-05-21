package IIOO.masystem.decentralized;


import IIOO.masystem.Memory;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SearchStatus extends TimerTask {
    private final ArrayList<AgentAI> agentAIS;
    private final long startTime;
    private final Timer timer;

    public SearchStatus(ArrayList<AgentAI> agentAIS) {
        this.agentAIS = agentAIS;
        this.startTime = System.currentTimeMillis();
        timer = new Timer();
        timer.scheduleAtFixedRate(this, 0, 2000);
    }

    @Override
    public void run() {

        int activeAgents = 0;
        for (int i = 0; i < agentAIS.size(); i++) {
            System.err.printf("AgentAI %d: ", i);
            if (agentAIS.get(i).runningStatus() != Thread.State.TERMINATED){
                activeAgents++;
            }
            String searchStatus = agentAIS.get(i).searchStatus();
            System.err.println(searchStatus);

        }
        System.err.printf("\nMemory used: %s\n", Memory.stringRep());
        System.err.printf("Time used: %3.2f s\n", timeSpent());
        System.err.printf("Active agentsAIs: %d\n\n", activeAgents);

    }

    public float timeSpent() {
        return (System.currentTimeMillis() - this.startTime) / 1000f;
    }

    public void terminate() {
        System.err.printf("Total memory used: %s\n", Memory.stringRep());
        System.err.printf("Total time used: %s\n", timeSpent());
        timer.cancel();
    }

}
