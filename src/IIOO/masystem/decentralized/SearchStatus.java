package IIOO.masystem.decentralized;

import IIOO.masystem.Memory;
import IIOO.masystem.TimeSpent;
import java.util.Timer;
import java.util.TimerTask;

public class SearchStatus extends TimerTask {
    private final AgentAI[] agentAIS;
    private final Timer timer;

    public SearchStatus(AgentAI[] agentAIS) {
        this.agentAIS = agentAIS;
        timer = new Timer();
        timer.scheduleAtFixedRate(this, 0, 2000);
    }

    @Override
    public void run() {
        int activeAgents = 0;
        StringBuilder searchStatus = new StringBuilder();
        searchStatus.append(String.format("Status after %s\n", TimeSpent.timeSpent()));
        for (int i = 0; i < agentAIS.length; i++) {
            if (agentAIS[i].runningStatus() != Thread.State.TERMINATED){
                activeAgents++;
            }
            searchStatus.append(String.format("AgentAI %d: %s\n", i, agentAIS[i].searchStatus()));
        }
        searchStatus.append(String.format("Memory used: %s\nActive agentAIs: %d\n", Memory.stringRep(),activeAgents));
        System.err.println(searchStatus);
    }

    public void terminate() {
        timer.cancel();
        timer.purge();
        StringBuilder searchStatus = new StringBuilder();
        searchStatus.append(String.format("Finished after %s with result:\n", TimeSpent.timeSpent()));
        for (int i = 0; i < agentAIS.length; i++) {
            searchStatus.append(String.format("AgentAI %d: %s\n", i, agentAIS[i].searchStatus()));
        }
        searchStatus.append(String.format("Memory used: %s\nTime used: %s\n", Memory.stringRep(), TimeSpent.timeSpent()));
        System.err.println(searchStatus);
    }

}
