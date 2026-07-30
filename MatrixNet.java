import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class MatrixNet {
    //initializing the hostTable to store all the hosts
    private HostTable hostTable=new HostTable();

    //checking for the valid hostID requirements
    private boolean isValidhostID(String hostID){
        //for each character
        for(int i=0;i<hostID.length();i++){
            char letter=hostID.charAt(i);
            //checking if character is an upper case letter
            boolean upperCase=(letter>='A' && letter<='Z');
            //checking if character is a digit
            boolean digit=(letter>='0' && letter<='9');
            //checking if character is an underscore
            boolean underscore= (letter=='_');
            //if character is none of the allowed booleans
            if(!(upperCase|| digit||underscore)){
                //invalid hostID
                return false;
            }
        }
        //all characters are valid so valid hostID
        return true;
    }
    private boolean isValidLevel(int level){
        //used for firewall levels and clearance levels
        //if not between these integers then invalid level
        return level>=1 && level<=5;
    }
    private boolean isPositive(int value){
        //used for latency and bandwidth values
        //if not positive integer then invalid value
        return value>0;
    }
    //spawning a host with hostID and clearance level
    //hosts are represented as vertices of a graph.
    public String spawnHost(String hostID,int clearance){
        //invalid hostID or invalid clearance level
        if(!isValidhostID(hostID) || !isValidLevel(clearance)){
            return "Some error occurred in spawn_host.\n";
        }
        //inserting the new host into host table
        Host newHost=hostTable.insert(hostID,clearance);
        //if the host already exists newHost insertion returns null
        if(newHost==null){
            return "Some error occurred in spawn_host.\n";
        }
        //successful insertion string
        return "Spawned host "+hostID+" with clearance level "+clearance+".\n";
    }
    //creating a backdoor link between two existing hosts
    //backdoor is undirected so for a backdoor two hosts are stored
    //backdoor has latency,bandwidth,firewall properties
    public String linkBackdoor(String hostID1,String hostID2,int latency,int bandwidth,int firewall){
        // a host cannot be linked to itself error string
        if(hostID1.equals(hostID2)){
            return "Some error occurred in link_backdoor.\n";
        }
        //invalid hostID1 or hostID2
        if(!isValidhostID(hostID1)|| !isValidhostID(hostID2)){
            return "Some error occurred in link_backdoor.\n";
        }
        //invalid latency or bandwidth value
        if(!isPositive(latency) ||!isPositive(bandwidth)) {
            return "Some error occurred in link_backdoor.\n";
        }
        //invalid firewall level
        if(!isValidLevel(firewall)){
            return "Some error occurred in link_backdoor.\n";
        }
        //both hosts from hostTable with find method of hostTable
        Host host1=hostTable.find(hostID1);
        Host host2=hostTable.find(hostID2);
        //both hosts must exist
        //find method returns null if the host does not exist inside hostTable
        if(host1==null || host2==null){
            return "Some error occurred in link_backdoor.\n";
        }
        //checking if a backdoor already exists between these two hosts
        //looking from host1 backdoors ArrayList
        for(Backdoor backdoor:host1.backdoors){
            //since undirected using a forward and reverse boolean for both ways of equality
            boolean forward=backdoor.hostID1.equals(hostID1) && backdoor.hostID2.equals(hostID2);
            boolean reverse=backdoor.hostID1.equals(hostID2) && backdoor.hostID2.equals(hostID1);
            //if already a backdoor exists creating the same backdoor is not allowed so error string
            if(forward || reverse){
                return "Some error occurred in link_backdoor.\n";
            }
        }
        //creating the backdoor link between two hosts
        Backdoor newBackDoor=new Backdoor(hostID1,hostID2,latency,bandwidth,firewall);
        //storing the host indices to use for the optimal route
        newBackDoor.index1 = host1.index;
        newBackDoor.index2 = host2.index;
        //adding the backdoor to both of the hosts since undirected backdoors are stored as an ArrayList for each host
        host1.backdoors.add(newBackDoor);
        host2.backdoors.add(newBackDoor);
        //return successful string
        return "Linked "+hostID1+" <-> "+hostID2+" with latency "+latency+"ms, bandwidth "
                +bandwidth+"Mbps, firewall "+firewall+".\n";
    }
    //to seal or unseal the backdoor by isSealed boolean
    public String sealBackdoor(String hostID1,String hostID2){
        //invalid hostID1 or hostID2
        if(!isValidhostID(hostID1) || !isValidhostID(hostID2)){
            return  "Some error occurred in seal_backdoor.\n";
        }
        //hosts from hostTable
        Host host1=hostTable.find(hostID1);
        Host host2=hostTable.find(hostID2);
        //if host1 or host2 does not exist it is an error
        if(host1==null || host2==null){
            return  "Some error occurred in seal_backdoor.\n";
        }
        //to find the backdoor that is both a backdoor for host1 and host2
        //starting with null
        Backdoor targetBackdoor=null;
        for(Backdoor backdoor:host1.backdoors){
            //since undirected using a forward and reverse boolean for both ways of equality
            boolean forward=backdoor.hostID1.equals(hostID1) && backdoor.hostID2.equals(hostID2);
            boolean reverse=backdoor.hostID1.equals(hostID2) && backdoor.hostID2.equals(hostID1);
            if(forward || reverse){
                //the target backdoor that links host1 and host2 is found
                // for loop ends.
                targetBackdoor=backdoor;
                break;
            }
        }
        //if target backdoor is null no matching backdoor is linked between hosts
        if(targetBackdoor==null){
            return  "Some error occurred in seal_backdoor.\n";
        }
        //if backdoor is unsealed making sealed with boolean and sealed successful string
        if(!targetBackdoor.isSealed){
            targetBackdoor.isSealed=true;
            return "Backdoor "+hostID1+" <-> "+hostID2+" sealed.\n";
        }
        else{
            //backdoor is sealed making unsealed with boolean and unsealed successful string
            targetBackdoor.isSealed=false;
            return "Backdoor "+hostID1+" <-> "+hostID2+" unsealed.\n";
        }
    }
    //finding the optimal route based on lambda and constraints
    public String traceRoute(String sourceID, String destinationID, int minBandwidth, int lambda) {
        //invalid sourceID or destinationID
        if (!isValidhostID(sourceID) || !isValidhostID(destinationID)) {
            return "Some error occurred in trace_route.\n";
        }
        //invalid lambda, lambda must be non-negative
        if ( lambda < 0) {
            return "Some error occurred in trace_route.\n";
        }
        //source host and destination host from the host table
        Host source = hostTable.find(sourceID);
        Host destination = hostTable.find(destinationID);
        //both hosts must exist
        if (source == null || destination == null) {
            return "Some error occurred in trace_route.\n";
        }
        //source host is the same as destination host
        // returning itself case
        if (sourceID.equals(destinationID)) {
            return "Optimal route " + sourceID + " -> " + destinationID + ": " +
                    sourceID + " (Latency = 0ms)\n";
        }
        //different algorithms based on lambda
        //Dijkstra shortest path algorithm when lambda is zero
        if (lambda == 0) {
            //Dijkstra function with constraints
            return traceRouteLambdaZero(source, destination, minBandwidth);
        } else {
            //finding top-k candidates based on the changed extra latency due to lambda being positive
            //returning the best scenario from k paths at the end
            return traceRouteLambdaPositiveTopK(source, destination, minBandwidth, lambda);        }
    }
    //optimal route when lambda=0
    private String traceRouteLambdaZero(Host source, Host destination, int minBandwidth) {
        //number of total hosts
        int size = hostTable.size();
        //for Dijkstra unvisited hosts have infinite distance
        long infinity = Long.MAX_VALUE;
        //ArrayList that stores the best route for each host with Route class
        ArrayList<Route> best = new ArrayList<>(size);
        //initializing Route for all hosts
        for (int i = 0; i < size; i++) {
            //each Route starts with the default values of Route class
            best.add(new Route());
        }
        // this is the initialization step of Dijkstra’s algorithm
        //only setting initial values for source host
        //no edge needed from source to source so latency=0
        best.get(source.index).latency = 0;
        //no backdoor is used from source to source so steps=0
        best.get(source.index).steps = 0;
        //the start of path is sourceID
        best.get(source.index).path = source.hostID;
        //PriorityQueue implementation class
        //to pop the State class with the smallest/min latency
        //necessary for Dijkstra's algorithm
        MinHeap heap = new MinHeap();
        //adding the initial source State to the heap
        heap.add(new State(source, 0, 0, source.hostID));
        //continuing the while loop while there are candidate States in the heap
        while (!heap.isEmpty()) {
            //current State is the minimum element of the heap extracted with the poll method of MinHeap class
            State currentState = heap.poll();
            //host of the current State
            Host currentHost = currentState.host;
            //best designated Route of the current host found from the best ArrayList with the index
            Route bestofCurrent= best.get(currentHost.index);
            //if the State extracted from the heap does not exactly match the best already found
            //this means the State is outdated for Dijkstra so should not be considered
            if (currentState.latency != bestofCurrent.latency ||
                    currentState.steps != bestofCurrent.steps ||
                    (currentState.path == null && bestofCurrent.path != null) ||
                    (currentState.path != null && !currentState.path.equals(bestofCurrent.path))) {
                continue;
            }
            //if destination is reached the optimal route is found so the loop can end with break
            //early termination
            if (currentHost == destination) break;
            //all backdoors connected to the current host of the current State
            for (Backdoor backdoor : currentHost.backdoors) {
                //not used if it is sealed
                if (backdoor.isSealed) continue;
                //not used if it is smaller than the minimum requirement of bandwidth
                if (backdoor.bandwidth < minBandwidth) continue;
                //not used if current host's clearance level smaller than firewall level of the backdoor
                if (currentHost.clearance < backdoor.firewall) continue;
                //the other host of the linked backdoor with the current host
                Host neighborHost;
                //if-else since backdoor is undirected
                //if current host matches hostID1 neighbor is hostID2
                if (backdoor.hostID1.equals(currentHost.hostID)) {
                    neighborHost = hostTable.find(backdoor.hostID2);
                } else {
                    //if current host matches hostID2 neighbor is hostID1
                    neighborHost = hostTable.find(backdoor.hostID1);
                }
                //skipping if the neighbor host cannot be found
                if (neighborHost == null) continue;
                //the index of neighor host
                int neighborIndex = neighborHost.index;
                //extra latency of the backdoor with lambda being ignored
                long extra = backdoor.latency;
                //new latency to reach the neighbor host
                long newLatency = currentState.latency + extra;
                //number of steps to reach the neighbor host
                int newSteps = currentState.steps + 1;
                //path including the neighbor host
                String newPath = currentState.path + " -> " + neighborHost.hostID;
                //relaxation part
                //the best route information for the neighbor host
                Route bestofNeighbor = best.get(neighborIndex);
                //comparing the new-found route with the already known best route
                //isBetter to compare different properties: latency, steps,path
                //to find which is preferable
                if (isBetter(newLatency, newSteps, newPath, bestofNeighbor.latency, bestofNeighbor.steps, bestofNeighbor.path)) {
                    //updating best-latency when newly found route is better
                    bestofNeighbor.latency = newLatency;
                    //updating best-steps when newly found route is better
                    bestofNeighbor.steps = newSteps;
                    //updating best-path when newly found route is better
                    bestofNeighbor.path = newPath;
                    //inserting the newly updated route as a State into the heap
                    //used for further expansion at the loop when the neighborHost enters as a currentHost
                    heap.add(new State(neighborHost, newLatency, newSteps, newPath));
                }
            }
        }
        //finalization
        //best route information for the destination found at the while loop
        Route result = best.get(destination.index);
        //if path is null or latency still infinite meaning not reached
        //answer is no route found string
        if (result.path == null || result.latency == infinity) {
            return "No route found from " + source.hostID + " to " + destination.hostID + "\n";
        }
        //optimal route message from the result path
        return "Optimal route " + source.hostID + " -> " + destination.hostID + ": " +
                result.path + " (Latency = " + result.latency + "ms)\n";
    }
    //comparing route properties
    //first smaller latency, second fewer steps , lastly lexicographically smaller
    private  boolean isBetter(long newLatency,int newSteps,String newPath, long bestLatency,int bestSteps,String bestPath){
        //if there is no existing route
        //automatically new route is better/true
        if(bestPath==null) {
            return true;
        }
        //smaller latency is better/true
        if(newLatency<bestLatency) {
            return true;
        }
        //larger latency is worse/false
        if(newLatency>bestLatency) {
            return false;
        }
        //fewer steps is better/true
        if (newSteps < bestSteps){
            return true;
        }
        //more steps is worse/false
        if (newSteps > bestSteps) {
            return false;
        }
        //if latency and steps are equal
        //lexicographically comparing the strings
        return  newPath.compareTo(bestPath) < 0;
    }
    //modified Dijkstra algorithm that keeps top k candidate routes per host
    //to handle lambda effect on the cost
    private String traceRouteLambdaPositiveTopK(Host source, Host destination, int minBandwidth, int lambda) {
        //total number of hosts
        int size = hostTable.size();
        //maximum number of steps to avoid cycle
        int maxStep = size - 1;
        //for each host store an ArrayList of its best k candidate State
        ArrayList<ArrayList<State>> best = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            //initializing empty ArrayList for each host
            best.add(new ArrayList<>());
        }
        //heap ordered by latency,steps and lexicographically smaller
        MinHeap heap = new MinHeap();

        // initial State for the source host
        //previous State=null no previous State of source
        State start = new State(source, 0, 0, tieBreaker(0, source), null);
        //adding the initial State to best list of source host
        best.get(source.index).add(start);
        //adding the initial State to the heap
        heap.add(start);
        //while loop continues until there are candidate States for the optimal route
        while (!heap.isEmpty()) {
            //extract the minimum element of the heap
            State currenState = heap.poll();
            //if there isn't any State left, end the loop
            if (currenState == null) break;
            //when the State is removed it means the State is no longer a candidate for the top-K
            //so skipping when no longer necessary
            if (currenState.removed) continue;
            //current host of the current State
            Host currentHost = currenState.host;
            //if the destination is reached, the loop can end
            if (currentHost == destination) break;
            //total steps cannot expand maximum step numbers in order to avoid cycles
            if (currenState.steps >= maxStep) continue;
            //backdoors of current host
            for (Backdoor backdoor : currentHost.backdoors) {
                if (backdoor.isSealed) continue;
                if (backdoor.bandwidth < minBandwidth) continue;
                if (currentHost.clearance < backdoor.firewall) continue;

                int neighborIndex;
                //determining the index of the neighbor for faster time complexity instead of find method
                if (backdoor.index1 == currentHost.index) {
                    neighborIndex = backdoor.index2;
                }
                else {
                    neighborIndex = backdoor.index1;
                }
                //neighbor host by the index with getByIndex method of host table
                Host neighborHost = hostTable.getByIndex(neighborIndex);
                //continue if neighbor host cannot be found
                if (neighborHost == null) continue;
                //new number of steps for the new Route
                int newSteps = currenState.steps + 1;
                //total steps cannot expand maximum step numbers in order to avoid cycles
                //important for optimal
                if (newSteps > maxStep) continue;
                //lambda implements a step-penalty that affects as the steps increase
                long extra = backdoor.latency + (long) lambda * currenState.steps;
                //new latency with penalty for the new candidate Route
                long newLatency = currenState.latency + extra;
                //ArrayList of best states for the neighbor host
                //top-K routes found so far for the host
                ArrayList<State> stateArrayList = best.get(neighborHost.index);
                //if the new candidate is dominated by an existing State of the stateArrayList
                //so it can not lead to a better solution continuing for new States
                if (isDominatedByStates(newLatency, newSteps, stateArrayList)) continue;
                //remove any existing States that are dominated by this new candidate State
                //keeping only better States in the ArrayList
                removeDominatedByStates(newLatency, newSteps, stateArrayList);
                //creating a new candidate State that reaches neighborHost with the updated properties
                State nextState = new State(neighborHost, newLatency, newSteps,tieBreaker(newSteps, neighborHost), currenState);
                //adding the new candidate State in the neighbor's best candidates ArrayList
                stateArrayList.add(nextState);
                //keeping only the best K elements of the ArrayList for better time complexity
                //removed=true when the ArrayList is larger than K
                topK(stateArrayList);
                //if the State currently explored is not marked as removed
                //exploring further the neighbors by adding inside the min heap
                if (!nextState.removed) {
                    heap.add(nextState);
                }
            }
        }
        //all candidate States that reached the destination host after the while loop
        ArrayList<State> destinationArrayList = best.get(destination.index);
        //selection the optimal route with the required properties
        //initally null
        State result = null;
        //tracing each candidate State and comparing between
        for (State state : destinationArrayList ) {
            //skip if already dominated by another State
            if (state.removed) continue;
            //initializing the null result with the first valid candidate to start the comparison
            if (result == null) result = state;
            //smaller latency
            else if (state.latency < result.latency) result = state;
            //if equal latency
            else if (state.latency == result.latency) {
                //smaller steps
                if (state.steps < result.steps) result = state;
                //if steps equal too
                else if (state.steps == result.steps) {
                    //smaller lexicographic ordering
                    if (state.path.compareTo(result.path) < 0) result = state;
                }
            }
        }
        //if no valid candidate State exists then no optimal Route found
        if (result == null) {
            return "No route found from " + source.hostID + " to " + destination.hostID + "\n";
        }
        //making the full String with the help of parent pointers to have a better time complexity
        String fullPath = buildFullPath(result);
        //successful optimal route message
        return "Optimal route " + source.hostID + " -> " + destination.hostID + ": " +
                fullPath + " (Latency = " + result.latency + "ms)\n";
    }
    //K for each method to designate the number of candidate States
    private static final int K = 3;

    //checking domination by comparing two Route
    private boolean isDominated(long latA, int stepsA, long latB, int stepsB) {
        //larger latency or more steps
        //cannot dominate
        if (latA > latB || stepsA > stepsB) return false;
        //if better latency or better steps condition
        //can dominate
        return (latA < latB) || (stepsA < stepsB);
    }
    //checking domination by comparing a candidate route is dominated by
    //any of the State from the stateArrayList
    private boolean isDominatedByStates(long latency, int steps, ArrayList<State> stateArrayList) {
        //all States of the host
        for (State state : stateArrayList) {
            //ignore remove marked States (already determined)
            //if an existing State dominates the candidate by the two Route comparison return true
            if (!state.removed && isDominated(state.latency, state.steps, latency, steps)) {
                //candidate is dominated
                return true;
            }
        }
        //none of the existing States of the stateArrayList dominates the candidate
        return false;
    }
    //removing all States dominated by candidate for top-K mechanism
    private void removeDominatedByStates(long latency, int steps, ArrayList<State> stateArrayList) {
        //all States of the host
        for (State state: stateArrayList) {
            //ignore remove marked States (already determined)
            //if candidate dominates by the two Route comparison mark state of the stateArrayList as removed
            //to stop using further for optimal Route determination
            if (!state.removed && isDominated(latency, steps, state.latency, state.steps)) {
                state.removed = true;
            }
        }
        //removing all the removed marked States from the stateArrayList
        for (int i = stateArrayList.size() - 1; i >= 0; i--) {
            if (stateArrayList.get(i).removed) {
                stateArrayList.remove(i);
            }
        }
    }
    //used when candidate routes have the same latency and steps
    private String tieBreaker(int steps, Host host) {
        //converting the step count to a String
        String stepString = Integer.toString(steps);
        //10 chars for padded steps and 1 for | and hostID length
        StringBuilder sb = new StringBuilder(11 + host.hostID.length());
        //add leading zeros until 10 char is reached
        for (int i = stepString .length(); i < 10; i++) sb.append('0');
        //append step count + separator + hostID
        sb.append(stepString).append('|').append(host.hostID);
        //tie-breaker key
        return sb.toString();
    }
    //keeping only top-K
    //marking as removed when worse than top-K
    private void topK(ArrayList<State> stateArrayList) {
        //if the size does not exceed K no removed marks needed
        if (stateArrayList.size() <= K) return;
        //using Comparable function of the State class to sort States based on the conditions
        stateArrayList.sort((state1, state2) -> {
            if (state1.latency != state2.latency) return Long.compare(state1.latency, state2.latency);
            if (state1.steps != state2.steps) return Integer.compare(state1.steps, state2.steps);
            return state1.path.compareTo(state2.path);
        });
        //removing other than top-K
        while (stateArrayList.size() > K) {
            //removing the last element after sorting
            State worstState = stateArrayList.remove(stateArrayList.size() - 1);
            //marking as removed to skip at further use
            worstState.removed = true;
        }
    }

    //building the full path only once to avoid String concatenation at each operation
    //using the lastState to link back to previous links until the source State
    private String buildFullPath(State lastState) {
        //temporary ArrayList to store hostIDs in reverse order from the lastState
        ArrayList<String> reverse = new ArrayList<>();
        //starting from the destination State
        State currentState = lastState;
        //traversing backwards and adding each State into the reverse ArrayList
        //then making currentState one step closer to source State
        while (currentState != null) {
            reverse.add(currentState.host.hostID);
            currentState = currentState.prev;
        }
        //building the String in the correct order
        StringBuilder sb = new StringBuilder();
        for (int i = reverse.size() - 1; i >= 0; i--) {
            sb.append(reverse.get(i));
            if (i > 0) sb.append(" -> ");
        }
        //returning the String of the StringBuilder
        return sb.toString();
    }
    public String scanConnectivity() {
        //getting all hosts from the host table
        ArrayList<Host> allHosts = hostTable.getAllHosts();
        int hostCount = allHosts.size();
        //a network with zero or one host is trivially fully connected
        if (hostCount <= 1) {
            return "Network is fully connected.\n";
        }
        //track whether each host by index has been visited
        ArrayList<Boolean> visited = new ArrayList<>();
        //initially add every host as not visited
        for (int i = 0; i < hostCount; i++) {
            visited.add(false);
        }
        //counter of the number of connected components
        int components = 0;
        //iterating through all hosts
        for (Host start: allHosts) {
            //skip already visited hosts
            if (visited.get(start.index)) continue;
            //new connected component increasing the counter
            components++;
            //stack for depth-first search
            ArrayList<Host> toVisit = new ArrayList<>();
            toVisit.add(start);
            //mark the starting host as visited
            visited.set(start.index, true);
            //dfs traversal
            while (!toVisit.isEmpty()) {
                //get the last host added stack LIFO property with ArrayList implementation
                Host currentHost = toVisit.remove(toVisit.size() - 1);
                //exploring backdoors of current host
                for (Backdoor backdoor : currentHost.backdoors) {
                    //skip if sealed
                    if (backdoor.isSealed) continue;
                    Host neighbor;
                    //determining the neighbor host if-else since backdoor is undirected
                    if (backdoor.hostID1.equals(currentHost.hostID)) {
                        neighbor = hostTable.find(backdoor.hostID2);
                    } else {
                        neighbor = hostTable.find(backdoor.hostID1);
                    }
                    //skip if no neighbor host is found
                    if (neighbor == null) continue;
                    //visit the neighbor if it has not been visited yet
                    if (!visited.get(neighbor.index)) {
                        visited.set(neighbor.index, true);
                        toVisit.add(neighbor);
                    }
                }
            }
        }
        //if there is only one connected component=fully connected
        if (components == 1) {
            return "Network is fully connected.\n";
        } else {
            //returning number of disconnected components
            return "Network has " + components + " disconnected components.\n";
        }
    }
    public String hostBreach(String hostID){
        //invalid hostID
        if(!isValidhostID(hostID)){
            return "Some error occurred in simulate_breach.\n";
        }
        //finding the host from host table
        Host host=hostTable.find(hostID);
        //if the host doesn't exist
        if(host==null){
            return "Some error occurred in simulate_breach.\n";
        }
        //the number of connected components when none of the hosts is removed since removedHost==null
        int before = countComponentsExcludingHost(null);
        //the number of connected components when the specified host is removed by the  countComponentsExcludingHost function
        int after  = countComponentsExcludingHost(host);
        //if the number of disconnected components increases after removing the host
        //then the host is an articulation point
        if(after>before){
            return "Host "+hostID+" IS an articulation point.\n"+
                    "Failure results in "+after+ " disconnected components.\n";
        }
        //otherwise removing the host does not affect network connectivity
        else{
            return "Host "+hostID+" is NOT an articulation point. Network remains the same.\n";
        }
    }

    private int countComponentsExcludingHost(Host removedHost) {
        //getting all hosts from the host table
        ArrayList<Host> allHosts = hostTable.getAllHosts();
        int hostCount = allHosts.size();
        //if there are no hosts,there are no connected components
        if (hostCount == 0) {
            return 0;
        }
        //track whether each host by index has been visited
        ArrayList<Boolean> visited = new ArrayList<>();
        //initially add every host as not visited
        for (int i = 0; i < hostCount; i++) {
            visited.add(false);
        }
        //counter of the number of connected components
        int components = 0;
        //iterating through all hosts
        for (Host start : allHosts) {
            //skip the host that is being temporarily removed from the network
            if (removedHost != null && start.hostID.equals(removedHost.hostID)) {
                continue;
            }
            //skip already visited hosts
            if (visited.get(start.index)) continue;
            //new connected component increasing the counter
            components++;
            //stack for depth-first search
            ArrayList<Host> toVisit = new ArrayList<>();
            toVisit.add(start);
            //mark the starting host as visited
            visited.set(start.index, true);
            //dfs traversal
            while (!toVisit.isEmpty()) {
                //get the last host added stack LIFO property with ArrayList implementation
                Host currrentHost = toVisit.remove(toVisit.size() - 1);
                //exploring backdoors of current host
                for (Backdoor backdoor : currrentHost.backdoors) {
                    //skip if sealed
                    if (backdoor.isSealed) continue;
                    Host neighbor;
                    //determining the neighbor host if-else since backdoor is undirected
                    if (backdoor.hostID1.equals(currrentHost.hostID)) {
                        neighbor = hostTable.find(backdoor.hostID2);
                    } else {
                        neighbor = hostTable.find(backdoor.hostID1);
                    }
                    //skip if no neighbor host is found
                    if (neighbor == null) continue;
                    //skip the removed host if encountered
                    if (removedHost != null && neighbor.hostID.equals(removedHost.hostID)) {
                        continue;
                    }
                    //visit the neighbor if it has not been visited yet
                    if (!visited.get(neighbor.index)) {
                        visited.set(neighbor.index, true);
                        toVisit.add(neighbor);
                    }
                }
            }
        }
        //return total number of components when the host is removed
        return components;
    }
    private int countComponentsExcludingBackdoor(Backdoor targetBackdoor) {
        //getting all hosts from the host table
        ArrayList<Host> allHosts = hostTable.getAllHosts();
        int hostCount = allHosts.size();
        //if there are no hosts,there are no connected components
        if (hostCount == 0) {
            return 0;
        }
        //track whether each host by index has been visited
        ArrayList<Boolean> visited = new ArrayList<>();
        //initially add every host as not visited
        for (int i = 0; i < hostCount; i++) {
            visited.add(false);
        }
        //counter of the number of connected components
        int components = 0;
        //iterating through all hosts
        for (Host start : allHosts) {
            //skip already visited hosts
            if (visited.get(start.index)) continue;
            //new connected component increasing the counter
            components++;
            //stack for depth-first search
            ArrayList<Host> toVisit = new ArrayList<>();
            toVisit.add(start);
            //mark the starting host as visited
            visited.set(start.index, true);
            //dfs traversal
            while (!toVisit.isEmpty()) {
                //get the last host added stack LIFO property with ArrayList implementation
                Host currentHost = toVisit.remove(toVisit.size() - 1);
                //exploring backdoors of current host
                for (Backdoor backdoor : currentHost.backdoors) {
                    //skip if sealed
                    if (backdoor.isSealed) continue;
                    //skip the target backdoor if it is being temporarily removed
                    if (targetBackdoor != null && backdoor == targetBackdoor) {
                        continue;
                    }
                    Host neighbor;
                    //determining the neighbor host if-else since backdoor is undirected
                    if (backdoor.hostID1.equals(currentHost.hostID)) {
                        neighbor = hostTable.find(backdoor.hostID2);
                    } else {
                        neighbor = hostTable.find(backdoor.hostID1);
                    }
                    //skip if no neighbor host is found
                    if (neighbor == null) continue;
                    //visit the neighbor if it has not been visited yet
                    if (!visited.get(neighbor.index)) {
                        visited.set(neighbor.index, true);
                        toVisit.add(neighbor);
                    }
                }
            }
        }
        //return total number of components when the backdoor is removed
        return components;
    }


    public String backdoorBreach(String hostID1, String hostID2){
        //invalid hostIDs or hostIDs are equal
        if(!isValidhostID(hostID1) || !isValidhostID(hostID2) || hostID1.equals(hostID2)){
            return "Some error occurred in simulate_breach.\n";
        }
        //finding the hosts
        Host host1=hostTable.find(hostID1);
        Host host2=hostTable.find(hostID2);
        //both host must exist
        if (host1 == null || host2 == null){
            return "Some error occurred in simulate_breach.\n";
        }
        //finding the backdoor connecting host1 and host2
        Backdoor targetBackdoor = null;
        for(Backdoor backdoor: host1.backdoors){
            boolean forward=backdoor.hostID1.equals(hostID1) && backdoor.hostID2.equals(hostID2);
            boolean reverse=backdoor.hostID1.equals(hostID2) && backdoor.hostID2.equals(hostID1);
            if(forward || reverse){
                targetBackdoor=backdoor;
                break;
            }
        }
        //if no backdoor exists between them, error string
        if(targetBackdoor==null){
            return "Some error occurred in simulate_breach.\n";
        }
        //if the backdoor is already sealed, error string
        if(targetBackdoor.isSealed){
            return "Some error occurred in simulate_breach.\n";
        }
        //count connected components before removing any backdoor
        int before=countComponentsExcludingBackdoor(null);
        //count connected components after removing the target backdoor
        int after=countComponentsExcludingBackdoor(targetBackdoor);
        //if removing the backdoor increases the number of components
        //then the backdoor is a bridge
        if(after> before){
            return "Backdoor "+hostID1+" <-> "+hostID2+" IS a bridge.\n"+
                    "Failure results in "+after+" disconnected components.\n";
        }
        //otherwise the network remains connected in the same way
        else{
            return "Backdoor "+hostID1+" <-> "+hostID2+" is NOT a bridge. Network remains the same.\n";
        }
    }

    public String oracleReport(){
        //all hosts in the network
        ArrayList<Host> allHosts = hostTable.getAllHosts();
        //total count of hosts in the network
        int totalHosts = allHosts.size();
        //count of all unsealed backdoors in the network
        int totalUnsealedBackdoors=countUnsealed(allHosts);
        //count of the number of connected components in the network
        int components=countComponentsExcludingHost(null);
        String connectivity;
        //checking whether network is connected or disconnected by int components
        if(components<=1){
            connectivity="Connected";
        }
        else{
            connectivity="Disconnected";
        }
        //checking whether the network contains any cycles
        boolean containsCycle=containsCycles();
        String containsCycleText;
        //cycle result to String
        if(containsCycle){
            containsCycleText="Yes";
        }
        else{
            containsCycleText="No";
        }
        //average bandwidth of all the backdoors
        double averageBandwidth=calculateAverageBandwidth();
        //average clearance level of all hosts
        double averageClearance=calculateAverageClearanceLevel();
        //output string
        return    "--- Resistance Network Report ---\n" +
                        "Total Hosts: " + totalHosts + "\n" +
                        "Total Unsealed Backdoors: " + totalUnsealedBackdoors + "\n" +
                        "Network Connectivity: " + connectivity + "\n" +
                        "Connected Components: " + components + "\n" +
                        "Contains Cycles: " + containsCycleText + "\n" +
                        "Average Bandwidth: " + averageBandwidth + "Mbps\n" +
                        "Average Clearance Level: " + averageClearance+"\n";
    }
    public int countUnsealed(ArrayList<Host> allHosts){
        //count of total unsealed backdoors
        int total=0;
        //iterating through all hosts
        for(Host host:allHosts){
            //all backdoors of the current host
            for(Backdoor backdoor:host.backdoors){
                //skip if sealed
                if(backdoor.isSealed) continue;
                Host neighborHost;
                //the other host of the backdoor
                if(backdoor.hostID1.equals(host.hostID)){
                    neighborHost=hostTable.find(backdoor.hostID2);
                }
                else{
                    neighborHost=hostTable.find(backdoor.hostID1);
                }
                //skip if the neighbor host doesn't exist
                if(neighborHost==null) continue;
                //counting each backdoor only once by using indexing to prevent double count
                //since backdoor is undirected
                if(host.index<neighborHost.index){
                    total++;
                }
            }
        }
        //return total number of unsealed backdoors
        return total;
    }

    private boolean containsCycles() {
        //all hosts in the network
        ArrayList<Host> allHosts = hostTable.getAllHosts();
        int total = hostTable.size();
        //if there are no hosts there can be no cycle
        if (total == 0) {
            return false;
        }
        //tracking visited hosts using an ArrayList
        ArrayList<Boolean> visited = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            visited.add(false);
        }
        //checking each connected component
        for (Host start : allHosts) {
            //skip if visited
            if (visited.get(start.index)) continue;
            //stacks for dfs and parent tracking
            ArrayList<Host> toVisit = new ArrayList<>();
            ArrayList<Host> parentOf = new ArrayList<>();
            //starting from the current host
            toVisit.add(start);
            parentOf.add(null);
            visited.set(start.index, true);
            //dfs traversal
            while (!toVisit.isEmpty()) {
                //removing the current host and its parent
                Host currentHost = toVisit.remove(toVisit.size() - 1);
                Host parentHost = parentOf.remove(parentOf.size() - 1);
                //all backdoors of current host
                for (Backdoor backdoor : currentHost.backdoors) {
                    //skip sealed backdoors
                    if (backdoor.isSealed) {
                        continue;
                    }
                    Host neighborHost;
                    //finding the neighbor host
                    if (backdoor.hostID1.equals(currentHost.hostID)) {
                        neighborHost = hostTable.find(backdoor.hostID2);
                    } else if (backdoor.hostID2.equals(currentHost.hostID)) {
                        neighborHost  = hostTable.find(backdoor.hostID1);
                    } else {
                        continue;
                    }
                    //skip if the neighbor host does not exist
                    if (neighborHost == null) {
                        continue;
                    }
                    //visiting the neighbor host if not visited
                    if (!visited.get(neighborHost .index)) {
                        visited.set(neighborHost .index, true);
                        toVisit.add(neighborHost );
                        parentOf.add(currentHost);
                    } else {
                        //if the neighbor is visited
                        //and is not the parent a cycle exists
                        if (parentHost == null || neighborHost .index != parentHost.index) {
                            //a cycle exists
                            return true;
                        }
                    }
                }
            }
        }
        //no cycles have been found in the network
        return false;
    }
    public double calculateAverageBandwidth(){
        //all hosts in the network
        ArrayList<Host> allHosts = hostTable.getAllHosts();
        //sum of bandwidths of all unsealed backdoors
        long totalBandwidth=0;
        //number of unsealed backdoors
        int backdoorCount=0;
        //iterating through all hosts
        for(Host host: allHosts){
            //all backdoors of the host
            for(Backdoor backdoor:host.backdoors){
                //skip sealed backdoors
                if(backdoor.isSealed) continue;
                Host neighborHost;
                //find the neighbor host
                if (backdoor.hostID1.equals(host.hostID)) {
                    neighborHost = hostTable.find(backdoor.hostID2);
                } else {
                    neighborHost = hostTable.find(backdoor.hostID1);
                }
                //find if the neighboring host does not exist
                if (neighborHost == null) continue;
                //count each backdoor only once using indexing
                if (host.index < neighborHost.index) {
                    //adding to total
                    //increasing the count
                    totalBandwidth += backdoor.bandwidth;
                    backdoorCount++;
                }
            }
        }
        //if there are no unsealed backdoors the average bandwidth is 0
        if (backdoorCount==0) return 0.0;
        // calculating the average bandwidth and round to one decimal place
        BigDecimal average=BigDecimal.valueOf((double) totalBandwidth / backdoorCount);
        average=average.setScale(1, RoundingMode.HALF_UP);
        //return the rounded average
        return average.doubleValue();
    }

    public double calculateAverageClearanceLevel() {
        //all hosts in the network
        ArrayList<Host> allHosts = hostTable.getAllHosts();
        //if there are no hosts the average clearance level is 0
        if (allHosts.isEmpty()) return 0.0;
        //total clearance level
        long totalClearance = 0;
        //adding clearance level of each host without any constraints
        for (Host host : allHosts) {
            totalClearance += host.clearance;
        }
        //calculating the raw average clearance level
        double rawAverage = (double) totalClearance / allHosts.size();
        // rounding the average to one decimal place using HALF_UP rounding
        BigDecimal averageClearance = BigDecimal.valueOf(rawAverage)
                .setScale(1, RoundingMode.HALF_UP);
        //return the rounded average
        return averageClearance.doubleValue();
    }

}
