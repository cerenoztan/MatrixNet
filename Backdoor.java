//backdoor object between two hosts
public class Backdoor {
    //unique ID of the first host
    String hostID1;
    //unique ID of the second host
    String hostID2;
    int latency;
    int bandwidth;
    int firewall;
    //indicated whether the backdoor is sealed
    boolean isSealed;
    //index of the first host
    int index1;
    //index of the second host
    int index2;
    //Constructor of a new backdoor
    Backdoor(String hostID1, String hostID2,int latency,int bandwidth,int firewall){
        this.hostID1=hostID1;
        this.hostID2=hostID2;
        this.latency=latency;
        this.bandwidth=bandwidth;
        this.firewall=firewall;
        //starting as unsealed
        this.isSealed=false;
        this.index1 = -1;
        this.index2 = -1;
    }
}