class BTreeNode {

    /**
     * Array of the keys stored in the node.
     */
    long[] keys;
    /**
     * Array of the values[recordID] stored in the node. This will only be filled
     * when the node is a leaf node.
     */
    long[] values;
    /**
     * Minimum degree (defines the range for number of keys)
     **/
    int t;
    /**
     * Pointers to the children, if this node is not a leaf. If
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

        // Create a new node z that will store the right half of the split
        BTreeNode z = new BTreeNode(y.t, y.leaf);
        // Set the number of keys for z
        z.n = t;
    
        // Split the keys and values
        for (int j = 0; j < t-1; j++) {
            z.keys[j] = y.keys[j + t];
            z.values[j] = y.values[j + t];
        }
    
         // If y is not a leaf
         // copy the right half of the children from y to z
        if (!y.leaf) {
            for (int j = 0; j < t; j++) {
                z.children[j] = y.children[j + t];
            }
        } else { // Set the next pointer if y is a leaf
            z.next = y.next;
        }
        // Link y to z
        y.next = z;
    
        y.n = t - 1; // Update the number of keys in y
        z.n = t - 1; // Update the number of keys in z

        // Shift children to the right to make space for z
        for (int j = n; j >= i + 1; j--) {
            children[j + 1] = children[j];
        }

        // Insert z at the correct position
        children[i + 1] = z;
    
        // Shift keys and values to the right to make space for the key moved up from y
        for (int j = n - 1; j >= i; j--) {
            keys[j + 1] = keys[j];
            values[j + 1] = values[j];
        }
        
        // Move the first key from z up to the current node
        keys[i] = z.keys[0];
        values[i] = z.values[0];
    
        // Increment the number of keys in the current node
        n = n + 1;
    }
    
    public void insertNonFull(Student student) {
        // Initialize index for the location of the new key
        int i = n - 1;
        // The key to be inserted
        long key = student.recordId;
        
        // If this node is a leaf, find the location and move all greater keys
        if (leaf) {
            // Find the location and move all greater keys
            while (i >= 0 && keys[i] > key) {
                keys[i + 1] = keys[i];
                values[i + 1] = values[i];
                i--;
            }
    
            // Insert the new key and value
            keys[i + 1] = key;
            values[i + 1] = student.studentId;
            // Increment the number of keys in the current node
            n = n + 1;
        
        } else { // If this node is not a leaf
            // Find the child
            while (i >= 0 && keys[i] > key) {
                i--;
            }
    
            // If the found child is full, split it
            if (children[i + 1].n == 2 * t - 1) {
                splitChild(i + 1, children[i + 1]);
    
                // After splitting, check which of the two children 
                // will have the new key
                if (keys[i + 1] < key) {
                    i++;
                }
            }
            // Call insertNonFull recursively on the chosen 
            children[i + 1].insertNonFull(student);
        }
    }


    
}
