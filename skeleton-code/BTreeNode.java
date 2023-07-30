class BTreeNode {

    /**
     * Array of the keys stored in the node.
     */
    long[] keys;
    /**
     * Array of the values[recordID] stored in the node. This will only be filled when the node is a leaf node.
     */
    long[] values;
    /**
     * Minimum degree (defines the range for number of keys)
     **/
    int t;
    /**
     * Pointers to the children, if this node is not a leaf.  If
     * this node is a leaf, then null.
     */
    BTreeNode[] children;
    /**
     * Number of key-value pairs in this node.
     * based on piazza post @109
     */
    int n;

    /**
     * true when node is leaf. Otherwise false
     */
    boolean leaf;

    /**
     * point to other next node when it is a leaf node. Otherwise null
     */
    BTreeNode next;

    // Constructor
    BTreeNode(int t, boolean leaf) {
        this.t = t;
        this.leaf = leaf;
        this.keys = new long[2 * t - 1];
        this.children = new BTreeNode[2 * t];
        this.n = 0;
        this.next = null;
        this.values = new long[2 * t - 1];
    }

    public void splitChild(int i, BTreeNode y) {
        BTreeNode z = new BTreeNode(y.t, y.leaf);
        z.n = t-1;

        for (int j = 0; j < t - 1; j++) {
            z.keys[j] = y.keys[j + t];
            z.values[j] = y.values[j + t];
        }

        if (!y.leaf) {
            for (int j = 0; j < t; j++)
	        {	            
                z.children[j] = y.children[j + t];
            } 
        } else {
            z.next= y.next;
        }
        y.next = z;
       
        y.n = t-1;

        for (int j = n; j >= i + 1; j--) {
            children[j + 1] = children[j];
        }
    
        children[i + 1] = z;
        
        for (int j = n - 1; j >= i; j--) {
            keys[j + 1] = keys[j];
            values[j + 1] = values[j];
        }

        keys[i] = y.keys[t - 1];
        values[i] = y.values[t - 1];
    
        n = n + 1;
    }

    public void insertNonFull(Student student) {
        int i = n - 1;
        long key = student.recordId;
        if (leaf) {
            // This loop does two things:
            // a) Finds the location of the new key to be inserted
            // b) Moves all greater keys to one place ahead
            while (i >= 0 && keys[i] > key) {
                keys[i + 1] = keys[i];
                values[i + 1] = values[i];
                i--;
            }
    
            // Insert the new key at the found location
            keys[i + 1] = key;
            values[i + 1] = student.studentId;
            n = n + 1;
        } { // If this node is not a leaf
            // Find the child which is going to have the new key
            while (i >= 0 && keys[i] > key)
                i--;
    
            // Check if the found child is full
            if (children[i + 1].n == 2 * t - 1) {
                // If the child is full, then split it
                splitChild(i + 1, children[i + 1]);
    
                // After splitting, the middle key of children[i] goes up and
                // children[i] is split into two. Check which of the two
                // is going to have the new key
                if (keys[i + 1] < key)
                    i++;
            }
            // Recursively call the 'insertNonFull' function for the appropriate child node
            children[i + 1].insertNonFull(student);
        }
    }
}
