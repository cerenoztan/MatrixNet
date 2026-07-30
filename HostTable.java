import java.util.ArrayList;
import java.util.LinkedList;
//hash table for storing host objects
public class HostTable {
    //size is large prime number
    private static final int tableSize=200003;
    //hashTable using separate chaining
    private ArrayList<LinkedList<Host>> hashTable;
    // hosts stored by insertion index for fast index-based access
    private ArrayList<Host> hostsByIndex = new ArrayList<>();
    //initial total number of hosts
    private int count= 0;
    //constructor initializing the hash table with empty buckets
    public HostTable(){
        hashTable=new ArrayList<>(tableSize);
        for (int i = 0; i < tableSize; i++) {
            //LinkedList for collisions
            hashTable.add(new LinkedList<>());
        }
    }
    //hashing to convert a String into an index
    private int hash(String id) {
        long hashing= 0;
        for (int i = 0; i < id.length(); i++) {
            hashing = hashing * 131 + id.charAt(i);
        }
        return (int) (hashing & 0x7fffffff) % tableSize;
    }
    //finding the host with the given ID from the hash table
    public Host find(String id) {
        int index = hash(id);
        LinkedList<Host> bucket = hashTable.get(index);
        //search within the bucket of the index
        for (Host host : bucket) {
            if (host.hostID.equals(id)) {
                return host;
            }
        }
        //null if not found
        return null;
    }
    //inserting a new host to hash table
    public Host insert(String id, int clearance) {
        //hash index of the given ID
        int index = hash(id);
        //bucket of the given hash index
        LinkedList<Host> bucket = hashTable.get(index);
        for (Host host : bucket) {
            if (host.hostID.equals(id)) {
                //if host already exists return null
                return null;
            }
        }
        //creating the new host if it doesn't initially exist
        Host newHost = new Host(id, clearance, count);
        //adding the host at the front
        bucket.addFirst(newHost);
        //storing the host by index for fast index-based access
        hostsByIndex.add(newHost);
        //incrementing total count after insertion
        count++;
        return newHost;
    }

    public int size(){
        //return the total number of hosts
        return count;
    }
    //returning an ArrayList containing all hosts
    public ArrayList<Host> getAllHosts() {
        //initial capacity equal to number of hosts
        ArrayList<Host> result = new ArrayList<>(count);
        for (LinkedList<Host> bucket : hashTable) {
            //adding all hosts from each bucket in the hash table
            result.addAll(bucket);
        }
        return result;
    }
    //getting a host by its index for fast-access
    public Host getByIndex(int index) {
        //returning null if index is out of bound
        if (index < 0 || index >= hostsByIndex.size()) return null;
        return hostsByIndex.get(index);
    }

}
