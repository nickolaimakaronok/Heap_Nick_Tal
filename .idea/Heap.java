import java.lang.Math;
/**
 * Heap
 *
 * An implementation of Fibonacci heap over positive integers 
 * with the possibility of not performing lazy melds and 
 * the possibility of not performing lazy decrease keys.
 *
 */
public class Heap
{
    public final boolean lazyMelds;
    public final boolean lazyDecreaseKeys;
    public HeapItem min;

    public int size;             // For size()
    public int numTrees;         // For numTrees()
    public int markedNodes;      // For numMarkedNodes()

    public int linksCount;       // For totalLinks()
    public int cutsCount;        // For totalCuts()
    public int heapifyCostCount; // For totalHeapifyCosts()
    
    /**
     *
     * Constructor to initialize an empty heap.
     *
     */
    public Heap(boolean lazyMelds, boolean lazyDecreaseKeys)
    {
        this.lazyMelds = lazyMelds;
        this.lazyDecreaseKeys = lazyDecreaseKeys;
        this.min = null;
        this.size = 0;
        this.numTrees = 0;
        this.markedNodes = 0;
        this.linksCount = 0;
        this.cutsCount = 0;
        this.heapifyCostCount = 0;

    }

    /**
     * 
     * pre: key > 0
     *
     * Insert (key,info) into the heap and return the newly generated HeapNode.
     *
     */
    public HeapItem insert(int key, String info) 
    {
        //Create the Item first (passing null for node initially to avoid cycle)
        HeapItem newItem = new HeapItem(null, key, info);

        // Create the Node, linking it to the Item
        HeapNode newNode = new HeapNode(newItem, null, null, null, null, 0);

        // Fix the back-pointer: Item -> Node
        newItem.node = newNode;

        // Ensure circular linking: A single node points to itself
        newNode.next = newNode;
        newNode.prev = newNode;

        // Create a temporary heap (a heap with just this one node)
        Heap singleHeap = new Heap(this.lazyMelds, this.lazyDecreaseKeys);
        singleHeap.min = newItem;
        singleHeap.size = 1;      // It has 1 element
        singleHeap.numTrees = 1;  // It has 1 tree (of rank 0)

        // Meld the singleton heap into the current heap ("this")
        this.meld(singleHeap);

        // Return the new item
        return newItem;
    }


    /**
     * 
     * Return the minimal HeapNode, null if empty.
     *
     */
    public HeapItem findMin()
    {
        return this.min;
    }

    /**
     * 
     * Delete the minimal item.
     *
     */
    public void deleteMin() {
        if (this.min == null) {
            return;
        }

        // Capture the node we are about to delete
        HeapNode nodeToDelete = this.min.node;
        HeapNode firstChild = nodeToDelete.child;

        // Process Children: Promote them to roots and unmark them
        if (firstChild != null) {
            HeapNode currentChild = firstChild;

            // Iterate through the entire circular list of children
            do {
                currentChild.parent = null; // Detach from the deleted node

                if (currentChild.mark) { // Change Mark to unmarked(0)
                    currentChild.mark = 0;
                    this.markedNodes--;
                }

                currentChild = currentChild.next;
            } while (currentChild != firstChild);
        }

        // Update the Root List Structure

        // Case A: The node to delete was the ONLY root in the heap
        if (nodeToDelete.next == nodeToDelete) {
            // If it had children, the children ring becomes the new root ring.
            // If no children, firstChild is null and min becomes null (heap empty).
            if (firstChild != null) {
                this.min = firstChild.item;
            } else {
                this.min = null;
            }
        }
        // Case B: The node to delete has siblings in the root list
        else {
            HeapNode leftNeighbor = nodeToDelete.prev;
            HeapNode rightNeighbor = nodeToDelete.next;

            if (firstChild != null) {
                // Splice the children ring into the root ring in place of nodeToDelete
                HeapNode lastChild = firstChild.prev;

                // Connect Left Side: leftNeighbor <-> firstChild
                leftNeighbor.next = firstChild;
                firstChild.prev = leftNeighbor;

                // Connect Right Side: lastChild <-> rightNeighbor
                lastChild.next = rightNeighbor;
                rightNeighbor.prev = lastChild;
            } else {
                // No children, just bridge the gap between neighbors
                leftNeighbor.next = rightNeighbor;
                rightNeighbor.prev = leftNeighbor;
            }

            // Temporarily point min to a valid root
            this.min = rightNeighbor.item;
        }

        // Update Heap State
        this.size--;

        // Cleanly detach the deleted node to prevent accidental access
        nodeToDelete.prev = null;
        nodeToDelete.next = null;
        nodeToDelete.child = null;
        nodeToDelete.parent = null;


        // Consolidate the trees (Successive Linking)
        if (this.size > 0) {
            successiveLinking();
        } else {
            // We nullify the structure
            this.min = null;
            this.numTrees = 0;
        }
    }


    private void link(HeapNode y, HeapNode x) {
        // For successive linkign only

        // Make y a child of x
        y.parent = x;

        // Connect y to x's child list
        if (x.child == null) {
            x.child = y;
            y.next = y;
            y.prev = y;
        } else {
            HeapNode childHead = x.child;
            HeapNode childLast = childHead.prev;

            // Insert y at the end of the child list
            childLast.next = y;
            y.prev = childLast;
            y.next = childHead;
            childHead.prev = y;
        }

        // Update Rank and State and linksCount
        x.rank++;
        y.mark = 0; // Roots lose their mark when becoming children
        this.linksCount++;
    }


    private void successiveLinking() {
        // Put heap-roots to buckets
        HeapNode[] buckets = toBuckets();

        // Rebuild the heap from the buckets
        fromBuckets(buckets);
    }

    private HeapNode[] toBuckets() {
        // Initialize buckets
        // Initially did with the Phi but then changed to *2 for safety
        int n = Math.max(1, this.size);
        int log2 = (int) Math.floor(Math.log(n) / Math.log(2));
        int result_bucket_size = 2 * log2 + 10;


        HeapNode[] buckets = new HeapNode[result_bucket_size];

        if (this.min == null) {
            return buckets;
        }

        // Start of the root list
        HeapNode start = this.min.node;

        start.prev.next = null; // We cut the circle

        HeapNode current = start;

        // Iterate over every node in the original root list
        while (current != null) {
            HeapNode nextNode = current.next; // Save pointer to next root

            // Isolate current node so it can be linked cleanly
            current.next = current;
            current.prev = current;

            HeapNode x = current;
            int r = x.rank;

            // Merge trees of the same rank (collision in bucket)
            while (buckets[r] != null) {
                HeapNode y = buckets[r]; // The other tree of same rank

                // Compare KEYS in the ITEMS to decide who is parent
                if (x.item.key > y.item.key) {
                    // Swap x and y so x is always the smaller key (the parent)
                    HeapNode temp = x;
                    x = y;
                    y = temp;
                }

                link(y, x);        // Make y a child of x
                buckets[r] = null; // Clear the bucket we just merged from
                r++;               // Rank of x has increased, continue checking next bucket
            }

            buckets[r] = x;     // Store the combined tree in the new rank bucket
            current = nextNode; // Move to next original root
        }

        return buckets;
    }


    private void fromBuckets(HeapNode[] buckets) {
        // Reset Heap State
        this.min = null;
        this.numTrees = 0;

        HeapNode first = null;
        HeapNode last = null;

        // Iterate through buckets to rebuild the list
        for (int i = 0; i < buckets.length; i++) {
            if (buckets[i] != null) {
                HeapNode node = buckets[i];
                this.numTrees++; // Count this tree

                if (first == null) {
                    // This is the first tree we found
                    first = node;
                    last = node;
                    this.min = node.item; // Initialize min
                } else {
                    // Append to the end of the list
                    last.next = node;
                    node.prev = last;
                    last = node;

                    // Update global min
                    if (node.item.key < this.min.key) {
                        this.min = node.item;
                    }
                }
            }
        }

        // Close the circular list (make it circular again)
        if (first != null) {
            first.prev = last;
            last.next = first;
        }
    }




    /**
     * 
     * pre: 0<=diff<=x.key
     * 
     * Decrease the key of x by diff and fix the heap.
     * 
     */
    public void decreaseKey(HeapItem x, int diff) 
    {    
        return;
    }

    /**
     * 
     * Delete the x from the heap.
     *
     */
    public void delete(HeapItem x) 
    {    
        return;
    }


    /**
     * 
     * Meld the heap with heap2
     * pre: heap2.lazyMelds = this.lazyMelds AND heap2.lazyDecreaseKeys = this.lazyDecreaseKeys
     *
     */
    public void meld(Heap heap2)
    {
        return;
    }
    
    
    /**
     * 
     * Return the number of elements in the heap
     *   
     */
    public int size()
    {
        return size;
    }


    /**
     * 
     * Return the number of trees in the heap.
     * 
     */
    public int numTrees()
    {
        return this.numTrees;
    }
    
    
    /**
     * 
     * Return the number of marked nodes in the heap.
     * 
     */
    public int numMarkedNodes()
    {
        return this.numMarkedNodes;
    }
    
    
    /**
     * 
     * Return the total number of links.
     * 
     */
    public int totalLinks()
    {
        return this.totalLinks;
    }
    
    
    /**
     * 
     * Return the total number of cuts.
     * 
     */
    public int totalCuts()
    {
        return this.totalCuts;
    }
    

    /**
     * 
     * Return the total heapify costs.
     * 
     */
    public int totalHeapifyCosts()
    {
        return this.heapifyCostCount;
    }
    
    
    /**
     * Class implementing a node in a Heap.
     *  
     */
    public static class HeapNode{
        public HeapItem item;
        public HeapNode child;
        public HeapNode next;
        public HeapNode prev;
        public HeapNode parent;
        public int mark;
        public int rank;



        public HeapNode(HeapItem item, HeapNode child, HeapNode next, HeapNode prev, HeapNode parent, int rank) {
            this.item = item;
            this.child = child;
            this.next = next;
            this.prev = prev;
            this.parent = parent;
            this.rank = rank;
            this.mark = 0;
        }

        public HeapNode() {
            this(null, null, null, null, null, 0);
        }
    }
    
    /**
     * Class implementing an item in a Heap.
     *  
     */
    public static class HeapItem{
        public HeapNode node;
        public int key;
        public String info;

        public HeapItem(HeapNode node, int key, String info) {
            this.node = node;
            this.key = key;
            this.info = info;
        }

        public HeapItem() {
            this(null, 0 , "");
        }


    }
}
