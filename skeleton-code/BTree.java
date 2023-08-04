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

        // check for violations
        if (file.n >= t) {
            return true;
        }

        // fix violations if present
        boolean violationsFixed = redistribute(file, fileParent);
        if (violationsFixed) {
            return true;
        }

        violationsFixed = mergeForDelete(path);
        
        return violationsFixed;
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
     * Attempts to redistribute elements from violating node file
     * and its next sibling.
     *
     * @param file The violating node
     * @param parent Parent node of file
     * @returns True if redistribution is successful and no violations persist, False otherwise
     * @author Steven Knaack
     */
    private static boolean redistribute(BTreeNode file, BTreeNode parent) {
        // TODO update to generalize
        // determine if we can redistribute 
        BTreeNode nextFile = file.next;
        int t = file.t;
        if (nextFile == null || nextFile.n == t) return false;

        // pop first element from nextFile
        long[] popped = nextFile.pop(0);

        long keyToMove = popped[0];
        long valueToMove = popped[1];

        // add the elements to move to File
        file.keys[t - 1] = keyToMove;
        file.values[t - 1] = valueToMove;
        file.n++;

        // copy up first element key of nextFile to parent node and return
        long newKey = nextFile.keys[0];
        long keyInFile = file.keys[0];
        int childIndex;
        try {
            childIndex = parent.traverseInternalNode(keyInFile);
        } catch (RuntimeException e) {
            System.out.print("Error while redistributing: " + e.getMessage());
            return false;
        }

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
     * Merges leaf of path and and a sibling nd percolate up 
     * until there are no violations
     *
     * @param path An ArrayList specifying all nodes leading to the leaf that we are merging
     * @returns True if merge is successful and no violations persist, False otherwise. 
     * @author Steven Knaack
     */
    private boolean mergeForDelete(ArrayList<BTreeNode> path) {
        // TODO Write this method
        return false;
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
