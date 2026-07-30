//used for optimal route finding algorithm
public class State implements Comparable<State> {
    //the host of the State
    Host host;
    //total latency to reach the host
    long latency;
    //total steps to reach the host
    int steps;
    //path taken so far for the State
    String path;

    //previous State in the path used for building the path
    State prev;
    //boolean to indicate if the State has been removed
    boolean removed;
    //constructor without prev used for traceRouteLambdaZero
    State(Host host, long latency, int steps, String path) {
        this.host = host;
        this.latency = latency;
        this.steps = steps;
        this.path = path;
        this.prev = null;
        this.removed = false;
    }
    // constructor with prev used for traceRouteLambdaPositiveTopK
    State(Host host, long latency, int steps, String path, State prev) {
        this.host = host;
        this.latency = latency;
        this.steps = steps;
        this.path = path;
        this.prev = prev;
        this.removed = false;
    }
    //comparing by the 3 priorities
    //used for Comparable
    public int compareTo(State other) {
        //lower latency
        if (this.latency != other.latency) return Long.compare(this.latency, other.latency);
        //fewer steps
        if (this.steps != other.steps) return Integer.compare(this.steps, other.steps);
        //for null path conditions
        if (this.path == null && other.path == null) return 0;
        if (this.path == null) return 1;
        if (other.path == null) return -1;
        //lexicographically smaller path
        return this.path.compareTo(other.path);
    }
}
