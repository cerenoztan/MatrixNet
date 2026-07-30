//a Route with latency , step count and path string
public class Route {
    long latency;
    int steps;
    String path;
    //Constructor initializing the route with maximum values
    //represents an unreached Route at first
    Route(){
        this.latency=Long.MAX_VALUE;
        this.steps=Integer.MAX_VALUE;
        this.path=null;
    }
}
