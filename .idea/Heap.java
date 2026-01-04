
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
    public HeapNode min;
    public int total_size;
    public int total_links;
    public int total_cuts;
    public int total_marked_nodes;
    public int num_of_trees;
    public int total_heapify_cost = 0;
    
    /**
     *
     * Constructor to initialize an empty heap.
     *
     */
    public Heap(boolean lazyMelds, boolean lazyDecreaseKeys)
    {
        //Initialization
        this.lazyMelds = lazyMelds;
        this.lazyDecreaseKeys = lazyDecreaseKeys;
        this.min = null;
        this.total_cuts = 0;
        this.total_links = 0;
        this.total_size = 0;
        this.num_of_trees = 0;
        this.total_heapify_cost = 0;
        this.total_marked_nodes = 0;


    }


    /**
     * 
     * pre: key > 0
     *
     * Insert (key,info) into the heap and return the newly generated HeapNode.
     *
     */
    public HeapNode insert(int key, String info) 
    {
        HeapNode newNode = new HeapNode(key, info);

        //Creating a temporary heap of one element for melding after
        Heap tempHeap = new Heap(this.lazyMelds, this.lazyDecreaseKeys);
        tempHeap.total_size = 1;
        tempHeap.num_of_trees = 1;
        tempHeap.min = newNode;

        //we are using meld for adding
        this.meld(tempHeap);
        return newNode;
    }

    /**
     * 
     * Return the minimal HeapNode, null if empty.
     *
     */
    public HeapNode findMin()
    {
        return this.min; // should be replaced by student code
    }

    /**
     * 
     * Delete the minimal item.
     *
     */
    public void deleteMin()
    {

        return; // should be replaced by student code
    }



    /**
     * 
     * pre: 0<=diff<=x.key
     * 
     * Decrease the key of x by diff and fix the heap.
     * 
     */
    public void decreaseKey(HeapNode x, int diff) 
    {
        x.key = x.key - diff;

        if (this.min == null || x.key < this.min.key) {
            this.min = x;
        }

        if(this.lazyMelds == false && this.lazyDecreaseKeys == false) { //binominal heap
            heapifyUp(x);
        } else if (this.lazyMelds == true && this.lazyDecreaseKeys == false) { //lazy binominal heap
            heapifyUp(x);
        } else if(this.lazyMelds == true && this.lazyDecreaseKeys == true) {//fibonacci heap

            if(x.parent != null && x.key < x.parent.key) {

                HeapNode parent_before_cut = x.parent;

                int wasMarked = x.mark;
                cut_node_from_tree(x);
                addToRootList(x);
                this.total_cuts++;

                if (wasMarked == 1) {
                    this.total_marked_nodes--;
                }
                x.mark = 0;

                if (parent_before_cut.parent != null) { // roots are typically not marked
                    if (parent_before_cut.mark == 0) {
                        parent_before_cut.mark = 1;
                        this.total_marked_nodes++;
                    } else {
                        cascadingCut(parent_before_cut);
                    }
                }


            }


        } else if(this.lazyMelds == false && this.lazyDecreaseKeys == true) { //a very cool and useful heap

        }
        return;
    }

    private void cascadingCut(HeapNode nodeX) {
        int wasMarked = nodeX.mark;
        HeapNode parentX = nodeX.parent;
        cut_node_from_tree(nodeX);

        addToRootList(nodeX);
        this.total_cuts++;

        if (wasMarked == 1) {
            this.total_marked_nodes--;
        }

        nodeX.mark = 0;

        if(parentX != null && parentX.parent != null) {
            if(parentX.mark == 0) {
                parentX.mark = 1;
                this.total_marked_nodes++;
            } else {
                cascadingCut(parentX);
            }
        }
    }

    private void addToRootList(HeapNode x) {
        // x is a singleton ring (x.next=x, x.prev=x) and x.parent==null
        if (this.min == null) {
            this.min = x;
            x.next = x;
            x.prev = x;
            this.num_of_trees = 1;
            return;
        }
        // put x into root ring right before min
        x.next = this.min;
        x.prev = this.min.prev;
        this.min.prev.next = x;
        this.min.prev = x;

        this.num_of_trees++;
        if (x.key < this.min.key) this.min = x;
    }

    public void cut_node_from_tree(HeapNode x) {
        if (x == null || x.parent == null) return;

        HeapNode node_parent = x.parent;
        HeapNode node_next = x.next;
        HeapNode node_prev = x.prev;
        if(node_next == x) {
            // x is the only child (singleton ring)
            node_parent.child = null;
        } else {
            // x has siblings: splice it out of the child ring
            node_prev.next = node_next;
            node_next.prev = node_prev;

            // if the parent's child pointer pointed at x, move it to a remaining child
            if (node_parent.child == x) {
                node_parent.child = node_next;
            }

        }

        node_parent.rank--;

        // make x a standalone node (singleton ring), ready to be added to root list
        x.parent = null;
        x.next = x;
        x.prev = x;

    }


    /**
     * Moves a node up the tree by physycally swapping it with its parent.
     * This preserves the external references to the HeapNodes.
     */
    private void heapifyUp(HeapNode x) {
        while (x.parent != null) {
            // If the heap property is satisfied (child >= parent), stop
            if (x.key >= x.parent.key) {
                break;
            }

            HeapNode y = x.parent; // y is the parent of x

            // Perform the physical swap
            swap(x, y);

            // Increment cost
            this.total_heapify_cost++;

            // After swap, 'x' is now physically above 'y'.
        }
    }


     // Physically swap a child node (x) with its parent (y).
    private void swap(HeapNode x, HeapNode y) {
        // pre: x != null, y != null, x.parent == y

        // Saving pointers before changing anything
        HeapNode gp = y.parent;          // grandparent (may be null if y is a root)

        // Saving sibling-ring pointers (so we don't lose them when we overwrite next/prev)
        HeapNode xNext = x.next;
        HeapNode xPrev = x.prev;

        HeapNode yNext = y.next;
        HeapNode yPrev = y.prev;

        // Save x's children (they will become y's children after the swap)
        HeapNode xChild = x.child;

        // Replacing y by x in the "outer" ring (y's sibling ring: roots or gp's children)
        x.parent = gp;
        if (gp != null && gp.child == y) {
            gp.child = x;
        }

        if (yNext == y) {
            // y was alone in its ring
            x.next = x;
            x.prev = x;
        } else {
            // put x into y's position
            x.next = yNext;
            x.prev = yPrev;
            yPrev.next = x;
            yNext.prev = x;
        }

        // Replace x by y in the "inner" ring (x's old sibling ring = y's children ring)
        if (xNext == x) {
            // x was the only child in y's child ring
            y.next = y;
            y.prev = y;
        } else {
            // put y into x's position
            y.next = xNext;
            y.prev = xPrev;
            xPrev.next = y;
            xNext.prev = y;
        }

        // Now y is in the old children ring, and will become a child of x.
        y.parent = x;

        // Fixing child pointers + parent pointers of affected rings
        // x becomes parent of the entire (former) y-children ring, now containing y
        x.child = y;

        // Ensuring every node in y's (new) sibling ring has parent = x
        HeapNode cur = y.next;
        while (cur != y) {
            cur.parent = x;
            cur = cur.next;
        }

        // y adopts x's old children
        y.child = xChild;
        if (xChild != null) {
            xChild.parent = y;
            HeapNode c = xChild.next;
            while (c != xChild) {
                c.parent = y;
                c = c.next;
            }
        }

        // Swap ranks (degree)
        int tmpRank = x.rank;
        x.rank = y.rank;
        y.rank = tmpRank;

        // If y was the heap minimum root pointer, x is now there
        if (this.min == y) {
            this.min = x;
        }
    }

    /**
     * 
     * Delete the x from the heap.
     *
     */
    public void delete(HeapNode x) 
    {
        int diff_x = x.key + this.min.key + 1;
        decreaseKey(x,diff_x);
        deleteMin();
        return; // should be replaced by student code
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
        return this.total_size; // should be replaced by student code
    }


    /**
     * 
     * Return the number of trees in the heap.
     * 
     */
    public int numTrees()
    {
        return this.num_of_trees; // should be replaced by student code
    }
    
    
    /**
     * 
     * Return the number of marked nodes in the heap.
     * 
     */
    public int numMarkedNodes()
    {
        return this.total_marked_nodes; // should be replaced by student code
    }
    
    
    /**
     * 
     * Return the total number of links.
     * 
     */
    public int totalLinks()
    {
        return this.total_links; // should be replaced by student code
    }
    
    
    /**
     * 
     * Return the total number of cuts.
     * 
     */
    public int totalCuts()
    {
        return total_cuts; // should be replaced by student code
    }
    

    /**
     * 
     * Return the total heapify costs.
     * 
     */
    public int totalHeapifyCosts()
    {
        return this.total_heapify_cost; // should be replaced by student code
    }
    
    
    /**
     * Class implementing a node in a ExtendedFibonacci Heap.
     *  
     */
    public static class HeapNode{
        public int key;
        public String info;
        public HeapNode child;
        public HeapNode next;
        public HeapNode prev;
        public HeapNode parent;
        public int rank;
        public int mark;

        public HeapNode(int key, String info) {
            this.key = key;
            this.info = info;
            this.child = null;
            this.parent = null;
            this.rank = 0;
            this.mark = 0;

            this.next = this;
            this.prev = this;
        }


    }
}
