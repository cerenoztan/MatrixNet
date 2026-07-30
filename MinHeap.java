import java.util.ArrayList;
public class MinHeap {
    // internal storage for the min-heap, implemented using an ArrayList
    private ArrayList<State> heap = new ArrayList<>();
    //returns true if heap is empty
    boolean isEmpty() {
        return heap.isEmpty();
    }
    //adding a new State into heap then
    void add(State newState) {
        heap.add(newState);
        //then restoring heap property with percolateUp method
        percolateUp(heap.size() - 1);
    }
    //removing and returning the min element of the min-heap /root in this case
    State poll() {
        //if the heap is empty return nothing
        if (heap.isEmpty()) return null;
        //the root element of the heap
        State result = heap.get(0);
        //the last element of the heap
        State last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            //moving the last element to the root
            heap.set(0, last);
            //then  restore heap order with percolateDown
            percolateDown(0);
        }
        //return the removed root element
        return result;
    }
    //moves an element up the heap until the min-heap property is restored
    private void percolateUp(int index) {
        //continue until reaching the root
        while (index > 0) {
            //parent index
            int parent = (index - 1) / 2;
            //if current element >its parent the heap property is satisfied
            if (compare(heap.get(index), heap.get(parent)) >= 0)
                break;
            //if not satisfied swap the current element with its parent
            swap(index, parent);
            //move up to the parent's index for the element
            index = parent;
        }
    }
    // moving an element down the heap until the min-heap property is restored
    private void  percolateDown(int index) {
        //total number of elements
        int count = heap.size();
        //continue until the element is in the correct position
        while (true) {
            //index of left and right child
            int left = 2 * index+ 1;
            int right = 2 * index+ 2;
            //assuming the current is the smallest index by the min-heap property
            int smallestIndex = index;
            //compare with left child
            if (left < count && compare(heap.get(left), heap.get(smallestIndex)) < 0) {
                smallestIndex = left;
            }
            //compare with right child
            if (right < count && compare(heap.get(right), heap.get(smallestIndex)) < 0) {
                smallestIndex = right;
            }
            //if current element at the right position end the loop
            if (smallestIndex == index) break;
            //swap with the smallest child at the while loop
            swap(index, smallestIndex);
            index = smallestIndex;
        }
    }
    //swapping two elements in the heap
    private void swap(int index1, int index2) {
        //the State element at the index1
        State temporary = heap.get(index1);
        //move the State element at the index2 to index1
        heap.set(index1, heap.get(index2));
        //place the temporary element into index2
        heap.set(index2, temporary);
    }
    //comparing based on three priorities
    private int compare(State state1, State state2) {
        //lower latency
        if (state1.latency != state2.latency)
            return Long.compare(state1.latency, state2.latency);
        //fewer steps
        if (state1.steps != state2.steps)
            return Integer.compare(state1.steps, state2.steps);
        //lexicographically smaller path
        return state1.path.compareTo(state2.path);
    }
}
