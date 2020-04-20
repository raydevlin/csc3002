package shared.data;

import java.util.ArrayList;

public class LogContainer {

    ArrayList<Log> logs;

    long totalTime = -1;
    long targetTime = -1;
    long createdTime = -1;
    long shortestDuration = Long.MAX_VALUE;
    long longestDuration = -1;

    public LogContainer(long targetTime) {
        this.targetTime = targetTime;
        this.logs = new ArrayList<>();
        this.createdTime = System.currentTimeMillis();
    }

    public void add(Log log) {
        long duration = log.getDuration();
        if(duration < shortestDuration) shortestDuration = duration;
        if(duration > longestDuration) longestDuration = duration;
        totalTime += duration;
        logs.add(log);
    }

    public long getAverageTime() {
        return totalTime / (long)logs.size();
    }

    public long getShortestDuration() {
        return shortestDuration;
    }

    public long getLongestDuration() {
        return longestDuration;
    }

    public long getStartTime() {
        return logs.get(0)!=null?logs.get(0).startTime:createdTime;
    }

    public ArrayList<Log> getLogs() {
        return logs;
    }
}
