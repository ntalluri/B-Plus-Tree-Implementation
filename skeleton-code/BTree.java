import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * B+Tree Structure
 * Key - StudentId
 * Leaf Node should contain [ key,recordId ]
 */
class BTree {

    /**
     * Pointer to the root node.
     */
    private BTreeNode root;
    /**
     * Number of key-value pairs allowed in the tree/the minimum degree of B+Tree
     **/
    private int t;

    BTree(int t) {
        this.root = null;
        this.t = t;
    }

    long search(long studentId) {
        /**
         * TODO:
         * Implement this function to search in the B+Tree.
         * Return recordID for the given StudentID.
         * Otherwise, print out a message that the given studentId has not been found in
         * the table and return -1.
         */
        return -1;
    }


    public BTree insert(Student student) {
        /**
         * TODO:
         * Implement this function to insert in the B+Tree.
         * Also, insert in student.csv after inserting in B+Tree.
         */

        // if root is empty
        // create new root and fill with student
        if (root == null) {
            root = new BTreeNode(t, true);
            root.keys[0] = student.studentId;
            root.values[0] = student.recordId;
            root.n = 1;
        } else {
            // if root is full
            // create a new root, split the current root, and insert the student into the appropriate child
            if (root.n == 2 * t) {
                BTreeNode s = new BTreeNode(t, false);
                s.children[0] = root;
                splitChild(s, 0, root);
                int i = 0;
                if (s.keys[0] < student.studentId) {
                    i++;
                }
                insertNotFull(s.children[i], student);
                root = s;
            } else {
                // if the root is not full
                // just insert the student.
                insertNotFull(root, student);
            }
        }

        // Write student to Student.csv
        writeToCSV(student);

        return this;
    }

    // nsert a key into a non-full node
    private void insertNotFull(BTreeNode x, Student student) {
        int i = x.n - 1;

        // if a leaf node
        // find the correct position 
        // and shift keys/values to insert the student
        if (x.leaf) {
            while (i >= 0 && x.keys[i] > student.studentId) {
                x.keys[i + 1] = x.keys[i]; // Shift keys
                x.values[i + 1] = x.values[i]; // Shift values
                i--;
            }

            x.keys[i + 1] = student.studentId; // Insert key
            x.values[i + 1] = student.recordId; // Insert value
            x.n = x.n + 1;
        } else { // if not a leaf node, find the correct child to insert into.
            while (i >= 0 && x.keys[i] > student.studentId) {
                i--;
            }

            // if child is full, split and insert into the correct child.
            if (x.children[i + 1].n == 2 * t) {
                splitChild(x, i + 1, x.children[i + 1]);
                if (x.keys[i + 1] < student.studentId) {
                    i++;
                }
            }

            // recursively insert into the correct child.
            insertNotFull(x.children[i + 1], student);
        }
    }

    private void splitChild(BTreeNode parent, int i, BTreeNode nodeToSplit) {
        BTreeNode newNode = new BTreeNode(t, nodeToSplit.leaf);
        newNode.n = t - 1;

        // if it's not a leaf, the middle key shouldn't stay in the child, 
        // but should be pushed up to the parent.
        
        if (!nodeToSplit.leaf) {
            System.arraycopy(nodeToSplit.keys, t, newNode.keys, 0, t - 1);
            System.arraycopy(nodeToSplit.values, t, newNode.values, 0, t - 1); // copy values
            nodeToSplit.n = t - 1; // reduce the keys in nodeToSplit
        } else {
            System.arraycopy(nodeToSplit.keys, t, newNode.keys, 0, t);
            System.arraycopy(nodeToSplit.values, t, newNode.values, 0, t); // copy values
            nodeToSplit.n = t;

            // link the new node as the next leaf.
            newNode.next = nodeToSplit.next;
            nodeToSplit.next = newNode;
        }

         // if not a leaf, also copy children.
        if (!nodeToSplit.leaf) {
            System.arraycopy(nodeToSplit.children, t, newNode.children, 0, t);
        }

        // make space for the new child in the parent node
        for (int j = parent.n; j > i; j--) {
            parent.children[j + 1] = parent.children[j];
        }
        parent.children[i + 1] = newNode;

        // make space for the new key in the parent node
        for (int j = parent.n - 1; j >= i; j--) {
            parent.keys[j + 1] = parent.keys[j];
            parent.values[j + 1] = parent.values[j]; // Shift values
        }

        // for leaf nodes, copy up the key, but don't remove it from the leaf
        if (nodeToSplit.leaf) {
            parent.keys[i] = newNode.keys[0]; // copy up the first key of the new node
        } else {
            parent.keys[i] = nodeToSplit.keys[t - 1]; // copy up middle key
            parent.values[i] = nodeToSplit.values[t - 1]; // copy up middle value
        }
        parent.n = parent.n + 1;
    }

    private void writeToCSV(Student student) {
        try {
            FileWriter writer = new FileWriter("skeleton-code/Student.csv", true);
            BufferedWriter bw = new BufferedWriter(writer);
            bw.write(student.studentId + ",");
            bw.write(student.studentName + ",");
            bw.write(student.major + ",");
            bw.write(student.level + ",");
            bw.write(student.age + ",");
            bw.write(student.recordId + "\n");
            bw.close();
        }catch(IOException e){
            System.err.println("An error occurred while writing to Student.csv");
            e.printStackTrace();
        }
    }

    boolean delete(long studentId) {
        /**
         * TODO:
         * Implement this function to delete in the B+Tree.
         * Also, delete in student.csv after deleting in B+Tree, if it exists.
         * Return true if the student is deleted successfully otherwise, return false.
         */
        return true;
    }

    List<Long> print() {

        List<Long> listOfRecordID = new ArrayList<>();

        /**
         * TODO:
         * Implement this function to print the B+Tree.
         * Return a list of recordIDs from left to right of leaf nodes.
         *
         */
        return listOfRecordID;
    }

}
