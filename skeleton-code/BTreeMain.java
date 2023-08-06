import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Main Application.
 */
public class BTreeMain {

    public static void main(String[] args) {

        /** Read the input file -- input.txt */
        Scanner scan = null;
        try {
            scan = new Scanner(new File("skeleton-code/input.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }

        /** Read the minimum degree of B+Tree first */

        int degree = scan.nextInt();

        BTree bTree = new BTree(degree);

        /** Reading the database student.csv into B+Tree Node */
        List<Student> studentsDB = getStudents();

        for (Student s : studentsDB) {
            bTree.insert(s);
        }

        System.out.println(bTree.print());

        /** Start reading the operations now from input file */
        try {
            while (scan.hasNextLine()) {
                Scanner s2 = new Scanner(scan.nextLine());

                while (s2.hasNext()) {

                    String operation = s2.next();

                    switch (operation) {
                        case "insert": {

                            long studentId = Long.parseLong(s2.next());
                            String studentName = s2.next() + " " + s2.next();
                            String major = s2.next();
                            String level = s2.next();
                            int age = Integer.parseInt(s2.next());
                            /**
                             * TODO: Write a logic to generate recordID
                             * based piazza post @108
                             */

                            long recordID;
                            if (s2.hasNext()) {
                                recordID = Long.parseLong(s2.next());
                            } else {
                                recordID = generateUniqueRecordID(bTree);
                            }

                             //System.out.println(recordID); //TODO: Delete Later

                            Student s = new Student(studentId, age, studentName, major, level, recordID);
                            bTree.insert(s);

                            break;
                        }
                        case "delete": {
                            long studentId = Long.parseLong(s2.next());
                            boolean result = bTree.delete(studentId);
                            if (result)
                                System.out.println("Student deleted successfully.");
                            else
                                System.out.println("Student deletion failed.");

                            break;
                        }
                        case "search": {
                            long studentId = Long.parseLong(s2.next());
                            long recordID = bTree.search(studentId);
                            if (recordID != -1)
                                System.out.println("Student exists in the database at " + recordID);
                            else
                                System.out.println("Student does not exist.");
                            break;
                        }
                        case "print": {
                            List<Long> listOfRecordID = new ArrayList<>();
                            listOfRecordID = bTree.print();
                            System.out.println("List of recordIDs in B+Tree " + listOfRecordID.toString());
                        }
                        default:
                            System.out.println("Wrong Operation");
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static long generateUniqueRecordID(BTree bTree) {
        Random rand = new Random();
        long recordID;
        do {
            // randomly create a new recordID
            recordID = Math.abs(rand.nextLong());
        } while (bTree.search(recordID) != -1); // check if the generated ID already exists in the BTree
    
        return recordID;
    }

    private static List<Student> getStudents() {
        /**
         * TODO:
         * 
         * Extract the students information from "Students.csv"
         * return the list<Students>
         */
        List<Student> studentList = new ArrayList<>();
        Scanner scnr = null;
        try {
            scnr = new Scanner(new File("skeleton-code/Student.csv"));
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }

        while (scnr.hasNextLine()) {
            Student student = new Student(0, 0, null, null, null, 0);
            String line = scnr.nextLine();
            if (line != null) {
                String[] studInfo = line.split(",");
                student.studentId = Long.parseLong(studInfo[0]);
                student.studentName = studInfo[1];
                student.major = studInfo[2];
                student.level = studInfo[3];
                student.age = Integer.parseInt(studInfo[4]);
                student.recordId = Long.parseLong(studInfo[5]);
            }
            studentList.add(student);
        }

        scnr.close();
        return studentList;
    }
}