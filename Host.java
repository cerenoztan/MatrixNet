import java.util.ArrayList;
//host object
public class Host {
    //unique hostID
    String hostID;
    //unique clearance level
    int clearance;
    //ArrayList of backdoors connected to the host object
    ArrayList<Backdoor> backdoors;
    //unique index of the host for fast-access
    int index;
    //constructor
    Host(String hostID,int clearance,int index){
        this.hostID=hostID;
        this.clearance=clearance;
        this.backdoors=new ArrayList<>();
        this.index=index;
    }
}
