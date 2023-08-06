import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.File;

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

    /**
     * Private helper method for search
     * 
     * @param studentId (the key value)
     * @return recordID for given StudentID
     */
     private long search(BTreeNode node, long studentId) {
        if (node == null) {
            System.out.println("Student ID has not been found.");
            return -1;
        }

        int i = 0; // this is the index value that would be used for the
                   // the node's key or value, and the pointer to children if
                   // desired StudentID is not found in the node
                   // would need to be reinitialized for children nodes

        while (node.keys[i] < studentId) {
            i++; // increment along the node until we find key >= studentId
        }

        // if somehow the index is >= number of key/value pairs, error. Piazza post 109
        if (node.n <= i) {
            System.out.println("Error. Index out of bounds!");
            return -1;
        }

        if (node.keys[i] == studentId && node.leaf) {
            return node.values[i]; // return recordID
        }

        // if the key is found but isn't in leaf node
        if (node.keys[i] == studentId && !node.leaf) {
            return search(node.children[i+1], studentId); // we look at the
            // right side of of the key, from rule lecture 7/19
        } else if (node.leaf) { // if key isn't found in leaf node
            System.out.println("Student ID has not been found.");
            return -1;
        } else {
            return search(node.children[i], studentId); // recursively go to children nodes
        }
    }

 

    long search(long studentId) {
        /**
         * TODO:
         * Implement this function to search in the B+Tree.
         * Return recordID for the given StudentID.
         * Otherwise, print out a message that the given studentId has not been found in the table and return -1.
         */
        return search(root, studentId); // will use helper method for search
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
        root.writeToCSV(this, student);

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

            if (i == 0 && student.studentId < x.keys[i]) {
                i = -1;
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
            parent.children[j] = parent.children[j - 1];
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

    /**
     * Delete an existing student given a StudentID from this BTree and Student.csv
     * 
     * @param studentId The studentId of the student to delete
     * @return True if the deletion is completed successfully, otherwise, return False
     * @author Steven Knaack
     */
    boolean delete(long studentId) { 
        // search for file and parent nodes containing studentId
        ArrayList<BTreeNode> path = this.getNodePathForDelete(studentId);
        BTreeNode fileParent = path.get(path.size() - 1);
        BTreeNode file = path.get(path.size() - 1);

        // locate student ID, return false if DNE
        long[] keys = file.keys;

        long currValue; 
        int i = -1;
        do {
            i++;

            if (i == keys.length) {
                return false;
            }

            currValue = keys[i];
            
            if (currValue == 0) {
                return false;
            }

        } while (i <= keys.length && currValue != studentId);

        // delete studentId from tree 
        try {
            file.pop(i);
        } catch (RuntimeException e) {
            System.out.println("Error while trying to delete studentId=" 
                 + studentId + " from BTree: " + e.getMessage());
            return false;
        }

        // delete studentId from Student.csv
        deleteFromCSV(studentId);

        // Fix violations

        boolean violationsPersist = file.n < file.t;
        BTreeNode violatingNode;
        BTreeNode violatingNodeParent;
        BTreeNode violatingNodeGrandParent;

        while (violationsPersist && path.size() > 0) {
            // get nodes
            violatingNode = path.remove(path.size() - 1);
            violatingNodeParent = (path.size() > 0) ? path.get(path.size() - 1) : null;
            violatingNodeGrandParent = (path.size() > 1) ? path.get(path.size() - 2) : null;

            // try to redistribute
            violationsPersist = redistribute(violatingNode, violatingNodeParent);
            if (!violationsPersist) return true;

            // try to merge
            violationsPersist = merge(violatingNode, violatingNodeParent, violatingNodeGrandParent);
        }
    
        return true;
    }

    
    /**
     * Traverses BTree and returns node path of leaf file that 
     * studentId would exist in (if it does exist)
     * 
     * @param studentId A long integer key (a studentId) to traverse 
     * for the appropriate leaf file of in this Btree
     * @return An ArrayList containing all the BTreeNodes in path to 
     * leaf where studentId would exist. This is of the form 
     * { root, internalNode1, ..., leafParent, leaf }
     * @author Steven Knaack
     */
    private ArrayList<BTreeNode> getNodePathForDelete(long studentId) {
        // initialize path
        ArrayList<BTreeNode> path =  new ArrayList<BTreeNode>();

        BTreeNode currNode = root;
        path.add(currNode);

        // traverse tree and return
        int childIndex;
        while (!currNode.leaf) {
            try {
                childIndex = currNode.traverseInternalNode(studentId);
            } catch (RuntimeException e) {
                System.out.print("Error while getting tree path: " 
                                + e.getMessage());
                return null;
            }

            currNode = currNode.children[childIndex];
            path.add(currNode);
        }

        return path;
    }

    /**
     * Deletes the row containing studentId from student.csv
     * 
     * @param studentId studentId of the row to bet deleted
     * @author Steven Knaack
     */
    void deleteFromCSV(long studentId) {
        try {
            // open file reader and writer
            String fileName = "skeleton-code/Student.csv";
            String newFileName = "skeleton-code/Student_temp.csv";

            File inputFile = new File(fileName);
            Scanner input = new Scanner(inputFile);

            File outputFile = new File(newFileName);
            FileWriter writer = new FileWriter(outputFile);
            
            while (input.hasNextLine()) {
                // get line of Student.csv
                String line = input.nextLine();

                String[] splitLine = line.split(",");
                long lineStudentId = Long.parseLong(splitLine[0]);

                // output line to Student_temp.csv iff line doesn't have studentId
                if (studentId != lineStudentId) {
                    writer.write(line + "\r\n");
                }
            }

            // delete old Student.csv and rename Student_temp.csv to Student.csv
            input.close();
            writer.close();

            inputFile.delete();
            
            File originalFile = new File(fileName);
            File newFile = new File(newFileName);
            
            newFile.renameTo(originalFile);
        } catch (Exception e) {
            System.out.println("Error while deleting from Student.csv: " 
                + e.getMessage());
        }
    }

    /**
     * Attempts to redistribute elements from a non-root violating node file
     * and its next sibling.
     *
     * @param file The violating node
     * @param parent Parent node of file
     * @returns True if redistribution is successful and no violations persist, False otherwise
     * @author Steven Knaack
     */
    private static boolean redistribute(BTreeNode file, BTreeNode parent) {
        // determine if we can redistribute 
        int childIndex = parent.traverseInternalNode(file.keys[0]);

        if (childIndex == parent.n) return false;

        BTreeNode nextFile = parent.children[childIndex + 1];
        
        int t = file.t;
        if (nextFile.n == t) return false;
        

        // pop first element from nextFile
        long keyToMove = 0;
        long valueToMove = 0;
        BTreeNode childToMove = null;
        if (!file.leaf) {
            childToMove = nextFile.children[0];
            keyToMove = childToMove.keys[0];
        }

        long[] popped = nextFile.pop(0);

        if (file.leaf) {
           keyToMove = popped[0];
           valueToMove = popped[1];
        } 

        // add the element to move to File
        file.keys[file.n] = keyToMove;

        if (file.leaf) {
            file.values[file.n] = valueToMove;
        } else {
            file.children[file.n + 1] = childToMove;
            
        }
        file.n++;

        // copy up first element key of nextFile to parent node and return
        long newKey = nextFile.keys[0];
        long keyInFile = file.keys[0];
        childIndex = parent.traverseInternalNode(keyInFile);

        int indexToChange;
        if (childIndex == parent.n) {
            indexToChange = childIndex - 1;
        } else {
            indexToChange = childIndex;
        }

        parent.keys[indexToChange] = newKey;

        return true;
    }

    /**
     * Merges non-root node and and the most appropraite sibling 
     *
     * @param node The violating node
     * @param parent The parent of node
     * @param grandParent The parent of parent
     * @returns True if parent now violates, false otherwise
     * @author Steven Knaack
     */
    private boolean merge(BTreeNode node, BTreeNode parent, BTreeNode grandParent) {
        // find appropriate sibling
        long key = node.keys[0];
        int key_index =  parent.traverseInternalNode(key);
        boolean isLastNode = key_index == parent.n;

        BTreeNode nextNode;  
        if (!isLastNode) { // usual: appropriate node is next node
            nextNode = parent.children[key_index + 1];
        } else {
            nextNode = node; // nextNode is right node
            key_index -= 1;
            node = parent.children[key_index]; // node will refer to left node
        }

        // merge
        long tempKey;
        long tempValue;
        BTreeNode tempChild;

        if (!node.leaf) { // if internal take node from parent
            long[] temp = parent.pop(key_index);
            node.keys[node.n] = temp[0];
            node.n++;

            if (parent.n == 0 && parent.t != 1) {
                root = node;
            } else if (parent.n == 0) {
                int gp_index = grandParent.traverseInternalNode(temp[0]);
                grandParent.children[gp_index] = node;
            } else {
                // update parent pointer to merge node
                parent.children[key_index] = node;
            }
        }

        for (int i = 0; i < nextNode.n; i++) {
            tempKey = nextNode.keys[i];
            node.keys[node.n + i] = tempKey;
            

            if (node.leaf) {
                tempValue = nextNode.values[i];
                node.values[node.n + i] = tempValue;
            } else {
                tempChild = nextNode.children[i];
                node.children[node.n + i] = tempChild;
            }
        }

        if (!node.leaf) { // deal with ending pointer
            int i = nextNode.n;
            tempChild = nextNode.children[i];
            node.children[node.n + i] = tempChild;
        }

        node.n += nextNode.n;
        return parent.n < parent.t;
    }

    List<Long> print() {

        List<Long> listOfRecordID = new ArrayList<>();
        BTreeNode node = root;

        /** 
            TODO:
            Implement this function to print the B+Tree.
            Return a list of recordIDs from left to right of leaf nodes.
        **/
        if (node == null) {
            System.out.println("No values to print");
            return null;
         } else {
            // attain leftmost leaf node
            while (!node.leaf) {
                node = node.children[0];
            }

            while (node != null) {
                for (int i = 0; i < node.n; i++) {
                    listOfRecordID.add(node.values[i]);
                }
                node = node.next;
            }
         }
        //System.out.println(listOfRecordID);
        return listOfRecordID;
    }

}
