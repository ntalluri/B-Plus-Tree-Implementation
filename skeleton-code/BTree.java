import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
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

    long search(long studentId) {
        /**
         * TODO:
         * Implement this function to search in the B+Tree.
         * Return recordID for the given StudentID.
         * Otherwise, print out a message that the given studentId has not been found in the table and return -1.
         */
        return -1;
    }

    BTree insert(Student student) {
        /**
         * TODO:
         * Implement this function to insert in the B+Tree.
         * Also, insert in student.csv after inserting in B+Tree.
         */
        return this;
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
            if (i == keys.length) {
                return false;
            }

            i++;
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
     * @return true if deleted, false if row DNE or an exception is thrown
     * @author Steven Knaack
     */
    static boolean deleteFromCSV(long studentId) {
        try {
            // open file reader and writer
            String file_name = "Student.csv";
            File input_file = new File(file_name);
            Scanner reader = new Scanner(input_file);

            File output_file = new File("Student_temp.csv");
            FileWriter writer = new FileWriter(output_file);
            
            boolean deleted = false;
            while (reader.hasNextLine()) {
                // get line of Student.csv
                String line = reader.nextLine();

                String[] splitLine = line.split(",");
                long lineStudentId = Long.parseLong(splitLine[0]);

                // output line to Student_temp.csv iff line doesn't have studentId
                if (studentId == lineStudentId) {
                    deleted = true;
                } else {
                    writer.write(line);
                }
            }

            reader.close();
            writer.close();

            if (!deleted) {
                return false;
            }

            // delete old Student.csv and rename Student_temp.csv to Student.csv
            input_file.delete();

            File new_file_name = new File(file_name);
            output_file.renameTo(new_file_name);

        } catch (Exception e) {
            System.out.println("Error while deleting from Student.csv");
            return false;
        }

        return true;
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
        // TODO Write this method

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

            node.n++;
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

        /**
         * TODO:
         * Implement this function to print the B+Tree.
         * Return a list of recordIDs from left to right of leaf nodes.
         *
         */
        return listOfRecordID;
    }
}
