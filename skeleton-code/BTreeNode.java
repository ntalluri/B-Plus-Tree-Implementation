import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

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
        this.keys = new long[2 * t];
        this.children = new BTreeNode[2 * t];
        this.n = 0;
        this.next = null;
        this.values = new long[2 * t];
    }

    /**
     * Traverses this internal BTreeNode and returns index of appropriate child pointer 
     * given a key
     * 
     * @param key A long integer key (a studentId) to traverse for the appropriate
     * range of in internalNode.keys
     * @return An integer index for internalNode.children which should be 
     * traversed to next given key
     * @throws RuntimeException If BTreeNode is not an internal node, or if there is an
     * error traversing it
     * @author Steven Knaack
     */
     int traverseInternalNode(long key) {
        // check internal node status
        if (leaf) {
            throw new RuntimeException("Given node is an internal node");
        }

        // traverse: return i if reached end of oversized array or if we find correct key
        int childIndex = -1;
        for (int i = 0; i <= keys.length; i++) {
            if (i == keys.length) {
                childIndex = i;
                break;
            }

            long currKey = keys[i];
            
            if (currKey == 0 || key < currKey) {
                childIndex = i;
                break;
            }
        }

        // check validity and return
        if (childIndex < 0) {
            throw new RuntimeException("Error while traversing internal node");
        }

        return childIndex;
    }
    /**
     * If a leaf node: removes (with shuffle) and returns the studentId and recordId at
     * the specified index from this BTreeNode if a leaf.
     * 
     * If an internal node: remove and return key at index, remove 'smaller' of two 
     * children associated with (assumption is children were merged into the larger
     * index)
     *
     * @param index Index of elements to pop
     * @return A long array of format 
     * { this.keys[index], this.values[index] } if a leaf or
     * { this.keys[index], 0 } if an internal node
     * and updates the given BTreeNode
     * @author Steven Knaack
     */
     long[] pop(int index) {
        // get return values
        long keyToMove = keys[index];
        long valueToMove = (leaf) ? values[index] : 0;

        long[] popped = new long[] {keyToMove, valueToMove};

        // shuffle elements 
        for (int i = index + 1; i < n; i++) {
            long tempKey = keys[i];
            keys[i - 1] = tempKey;

            if (leaf) {
                long tempValue = values[i];
                values[i - 1] = tempValue;
            } else {
                BTreeNode tempChild = children[i];
                children[i - 1] = tempChild;
            }
        }

        // Shuffle extra child if deleting last key
        if (!leaf && index == n - 1) {
            BTreeNode tempChild = children[index];
            children[index] = tempChild;
        }

        // Delete last element
        keys[n - 1] = 0;
        if (leaf) {
            values[n - 1] = 0;
        } else {
            children[n] = null;
        }

        n--;
        return popped;
    }

    void writeToCSV(BTree bTree, Student student) {
        try {
            String fileName = "skeleton-code/Student.csv";
            String newFileName = "skeleton-code/Student_temp_i.csv";

            File inputFile = new File(fileName);
            Scanner input = new Scanner(inputFile);
    
            File outputFile = new File(newFileName);
            outputFile.createNewFile();
            FileWriter writer = new FileWriter(outputFile);
    
            boolean containsStudent = false;
            while (input.hasNextLine()) {
                String line = input.nextLine();

                String[] lineSplit = line.split(",");
                long sid = Long.parseLong(lineSplit[0]);

                if (sid == student.studentId) containsStudent = true;

                writer.write(line + "\r\n");
            }   
            
            if (!containsStudent) {
                String student_str = student.studentId + "," +
                                    student.studentName + "," +
                                    student.major + "," +
                                    student.level + "," +
                                    student.age + "," + 
                                    student.recordId + "\r\n";

                writer.write(student_str);
            }
            
            input.close();
            writer.close();

            inputFile.delete();
            
            File originalFile = new File(fileName);
            File newFile = new File(newFileName);
            
            newFile.renameTo(originalFile);
        }catch(IOException e){
            System.err.println("An error occurred while writing to Student.csv");
            e.printStackTrace();
        }
    }

}
