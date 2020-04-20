package shared.data;

public class Log {

    int generation = 0;
    long startTime = 0;
    long endTime = 0;

    public Log(int generation, long startTime, long endTime) {
        this.generation = generation;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getDuration() {
        return endTime - startTime;
    }

    @Override
    public String toString() {
        return "Log{" +
                "generation=" + generation +
                ", duration=" + (endTime - startTime) + "ms" +
                '}';
    }
}
