package treeflow;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CSVWriter {

    private FileWriter writer;

    private static final String FILENAME = "DavidJozefiakTreeFlow.csv";
    private static final String[] HEADER = {"Participant", "Trial", "Method", "Time", "Actions"};
    private static final String SEPERATOR = ";";

    public CSVWriter() {
        try {
            File file = new File(FILENAME);
            writer = new FileWriter(file);
            writer.write(String.join(SEPERATOR, HEADER) + "\n");
            writer.flush();
        } catch (IOException ex) {
            System.err.println("Error: Could Not Write CSV Header");
        }
    }

    public void write(String participant, int trial, int method, long time, int actions) {
        try {
            writer.append(participant + SEPERATOR
                    + trial + SEPERATOR
                    + method + SEPERATOR
                    + time + SEPERATOR
                    + actions + "\n");
        } catch (IOException ex) {
            System.err.println("Error: Could Not Write CSV Row");
        }
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException ex) {
            System.err.println("Error: Could Not Close CSV File");
        }
    }

}
