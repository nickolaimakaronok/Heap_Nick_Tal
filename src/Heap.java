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

                if (currentChild.mark) { // Change Mark to unmarked
                    currentChild.mark = false;
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

        if (nodeToDelete.item != null) {
            nodeToDelete.item.node = null;
        }


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
        y.mark = false; // Roots lose their mark when becoming children
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
        // Checking the rightness of input
        if(x == null || x.node == null) {
            return;
        }

        if(diff <= 0) {
            return;
        }

        //Decreasing the key value
        x.key -= diff;



        //if lazyDecreaseKeys == false an then we do heapifyUp
        //because we don't need to meld anything in decrease key without cutting we don't even check it (lazyMelds)


        if(this.lazyDecreaseKeys == false) {
            HeapNode node_x = x.node;
            //heapifyUp by values and not nodes(so no million pointers changes needed)
            while(node_x.parent != null && node_x.item.key < node_x.parent.item.key) {
                swapItems(node_x, node_x.parent);
                this.heapifyCostCount++;

                //x had moved to its parent's node after saw so the node_x has to be updated to a new to x.node
                node_x = x.node;
            }

            if (this.min == null || x.key < this.min.key) {
                this.min = x;
            }

            return;
        }

        //if lazyDecreaseKeys == true we do CUT and Cascading cut

        HeapNode itemNode = x.node;
        HeapNode parentNode = itemNode.parent;

        if(parentNode != null && itemNode.item.key < parentNode.item.key) {

            //we make a flag if our node's parent is a Root
            boolean parentWasRootBeforeCut = (parentNode.parent == null);
            cut(itemNode);
            cascadingCut(parentNode, parentWasRootBeforeCut);
        }

        if (this.min == null || x.key < this.min.key) {
            this.min = x;
        }

    }



    //swapping two nodes according to the rules of FORUM
    private void swapItems(HeapNode a, HeapNode b) {
        HeapItem tmp = a.item;
        a.item = b.item;
        b.item = tmp;

        if(a.item != null) {
            a.item.node = a;
        }
        if(b.item != null) {
            b.item.node = b;
        }
    }

    //cut node v
    private void cut(HeapNode cutNode) {
        if(cutNode == null) {
            return;
        }

        HeapNode parentNode = cutNode.parent;

        if(parentNode == null) {
            return; //already a root
        }

        // remove cutNode from parentNode's child list
        if(cutNode.next == cutNode) {
            // cutNode is an only child
            parentNode.child = null;
        } else {

            // erasing from siblings ring
            cutNode.prev.next = cutNode.next;
            cutNode.next.prev = cutNode.prev;

            // if parentNode.child pointed to cutNode then change it to a sibling
            if(parentNode.child == cutNode) {
                parentNode.child = cutNode.next;
            }

        }

        // update rating
        parentNode.rank--;

        // detach cutNode
        cutNode.parent = null;

        // unmark cutNode when it becomes a root
        if(cutNode.mark) {
            cutNode.mark = false;
            this.markedNodes--;
        }

        // cut counter update
        this.cutsCount++;

        // making a singleton circular list
        cutNode.next = cutNode;
        cutNode.prev = cutNode;

        // add cutNode to root lst;

        addRoot(cutNode);

        if(this.lazyMelds == false) {
            successiveLinking();
        }
    }

    private void addRoot(HeapNode rootToAdd) {
        if(rootToAdd == null) {
            return;
        }

        if(this.min == null) {
            this.min = rootToAdd.item;
            rootToAdd.next = rootToAdd;
            rootToAdd.prev = rootToAdd;
            this.numTrees = 1;
            return;
        }

        HeapNode rootListHead = this.min.node; //min easier to find could be anu node
        HeapNode rootListTail = rootListHead.prev; //last root in teh circular list

        //now we want to insert our new Root in between

        rootListTail.next = rootToAdd;
        rootToAdd.prev = rootListTail;

        rootToAdd.next = rootListHead;
        rootListHead.prev = rootToAdd;

        this.numTrees++;

        if(rootToAdd.item != null && rootToAdd.item.key < this.min.key) {
            this.min = rootToAdd.item;
        }

    }


    // we go all the way up until we see unmarked parent node
    // also if the parent is root then we stop (the flag we created)


    private void cascadingCut(HeapNode nodeThatLostChild, boolean wasRootWhenChildWasLost) {
        if(nodeThatLostChild == null) {
            return;
        }

        //if it was root and lost teh child we stop(roots are not marked/cut)
        if(wasRootWhenChildWasLost) {
            // make sure everything is ok
            if(nodeThatLostChild.mark) {
                nodeThatLostChild.mark = false;
                this.markedNodes--;
            }
            return;
        }

        // Node was not a root and was not marked
        if(nodeThatLostChild.mark == false) {
            nodeThatLostChild.mark = true;
            this.markedNodes++;
            return;
        }

        // Node was already marked then we cut it and continue up
        HeapNode parentOfNode = nodeThatLostChild.parent;
        boolean parentWasRootWhenChildWasLost = (parentOfNode != null && parentOfNode.parent == null);
        cut(nodeThatLostChild); //we cut the node if lost more than one child
        cascadingCut(parentOfNode, parentWasRootWhenChildWasLost); //recursion going up and doing cascading cuts

    }


    /**
     * 
     * Delete the x from the heap.
     *
     */
    public void delete(HeapItem x) 
    {
        //I assume that all value in Heaps are positive numbers or == 0 (written in forum)
        if(x == null || this.min == null || x.node == null) {
            return;
        }


        if(x == this.min) {
            this.deleteMin();
            return;
        }

        if(x.key == Integer.MAX_VALUE) {
            this.decreaseKey(x, Integer.MAX_VALUE);
            this.decreaseKey(x, 1);
            this.deleteMin();
            return;
        }

        this.decreaseKey(x, x.key+1); //now it's negative and it should be the minimum since all non-negative

        this.deleteMin();

    }


    /**
     * 
     * Meld the heap with heap2
     * pre: heap2.lazyMelds = this.lazyMelds AND heap2.lazyDecreaseKeys = this.lazyDecreaseKeys
     *
     */
    public void meld(Heap heap2)
    {
        // Checking if heap2 is empty or null
        if (heap2 == null || heap2.min == null) {
            return;
        }
        // Accumulating history statistics from heap2
        // The melded heap inherits the history (links, cuts, costs) of the heaps that created it
        this.linksCount += heap2.linksCount;
        this.cutsCount += heap2.cutsCount;
        this.heapifyCostCount += heap2.heapifyCostCount;

        // Handling the case where the current heap (this) is empty.
        // We take ownership of heap2's data.
        if (this.min == null) {
            this.min = heap2.min;
            this.size = heap2.size;
            this.numTrees = heap2.numTrees;
            this.markedNodes = heap2.markedNodes;

            // Clear heap2 so it is no longer usable (not neccessry)
            heap2.min = null;
            heap2.size = 0;
            heap2.numTrees = 0;
            heap2.markedNodes = 0;
            heap2.linksCount = 0;
            heap2.cutsCount = 0;
            heap2.heapifyCostCount = 0;
            return;
        }
        // Updating current structure counters (size, trees, marks).
        this.size += heap2.size;
        this.numTrees += heap2.numTrees;
        this.markedNodes += heap2.markedNodes;

        // Merging the two circular doubly linked lists
        HeapNode min1 = this.min.node;       // Head of this list
        HeapNode tail1 = min1.prev;          // Tail of this list
        HeapNode min2 = heap2.min.node;      // Head of heap2 list
        HeapNode tail2 = min2.prev;          // Tail of heap2 list

        tail1.next = min2;
        min2.prev = tail1;
        tail2.next = min1;
        min1.prev = tail2;

        // Handling lazy vs. non-lazy logic
        if (this.lazyMelds) {
            // If lazy, we update the minimum pointer if heap2 has a smaller key.
            if (heap2.min.key < this.min.key) {
                this.min = heap2.min;
            }
        } else {
            // If not lazy, we perform successive linking immediately
            // This function updates this.min at the end
            successiveLinking();
        }

        // Clearing heap2 to ensure it is no longer usable by the user
        heap2.min = null;
        heap2.size = 0;
        heap2.numTrees = 0;
        heap2.markedNodes = 0;
        heap2.linksCount = 0;
        heap2.cutsCount = 0;
        heap2.heapifyCostCount = 0;
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
        return this.markedNodes;
    }
    
    
    /**
     * 
     * Return the total number of links.
     * 
     */
    public int totalLinks()
    {
        return this.linksCount;
    }
    
    
    /**
     * 
     * Return the total number of cuts.
     * 
     */
    public int totalCuts()
    {
        return this.cutsCount;
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
        public boolean mark;
        public int rank;



        public HeapNode(HeapItem item, HeapNode child, HeapNode next, HeapNode prev, HeapNode parent, int rank) {
            this.item = item;
            this.child = child;
            this.next = next;
            this.prev = prev;
            this.parent = parent;
            this.rank = rank;
            this.mark = false;
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
