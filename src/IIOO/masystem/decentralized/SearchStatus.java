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
        StringBuilder searchStatus = new StringBuilder();
        for (int i = 0; i < agentAIS.size(); i++) {
            if (agentAIS.get(i).runningStatus() != Thread.State.TERMINATED){
                activeAgents++;
            }
//            System.err.printf("AgentAI %d: ", i);
            searchStatus.append(String.format("AgentAI %d: %s\n", i, agentAIS.get(i).searchStatus()));

        }
        searchStatus.append(String.format("Memory used: %s\nTime used: %3.2f s\nActive agentAIs: %d\n", Memory.stringRep(), timeSpent(), activeAgents));
        System.err.println(searchStatus);
    }

    public float timeSpent() {
        return (System.currentTimeMillis() - this.startTime) / 1000f;
    }

    public void terminate() {
        timer.cancel();
        timer.purge();
        StringBuilder searchStatus = new StringBuilder();
        searchStatus.append("Finished with result:\n");
        for (int i = 0; i < agentAIS.size(); i++) {
//            System.err.printf("AgentAI %d: ", i);
            searchStatus.append(String.format("AgentAI %d: %s\n", i, agentAIS.get(i).searchStatus()));
        }
        searchStatus.append(String.format("Memory used: %s\nTime used: %3.2f s\n", Memory.stringRep(), timeSpent()));
        System.err.println(searchStatus);
    }

}
