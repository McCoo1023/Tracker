package Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class GuidanceTracker {
    private static HashMap<String, LocalDateTime[]> sessions = new HashMap<>();
    private static final String FILE_NAME = "sessions_log.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static JTextArea activeSessionsArea;
    private static final String DESKTOP_PATH = System.getProperty("user.home") + "\\Desktop";

    public static void main(String[] args) {
        // Create the frame
        JFrame frame = new JFrame("Guidance Session Tracker");
        frame.setSize(550, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create components
        JPanel inputPanel = new JPanel();
        JLabel infoLabel = new JLabel("Enter your ID #:");
        JTextField studentIdField = new JTextField(10);
        JButton enterButton = new JButton("Enter");
        JButton exitButton = new JButton("Exit");
        JButton saveLogButton = new JButton("Save Log");
        activeSessionsArea = new JTextArea(12, 40);
        activeSessionsArea.setEditable(false); // Make the text area non-editable

        // Add action listeners to the buttons
        enterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String studentId = studentIdField.getText();
                enterSession(studentId);
                studentIdField.setText(""); // Clear the field
                updateActiveSessionsDisplay();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String studentId = studentIdField.getText();
                exitSession(studentId);
                studentIdField.setText(""); // Clear the field
                updateActiveSessionsDisplay();
            }
        });

        saveLogButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveCurrentLog();
            }
        });

        // Add components to the input panel
        inputPanel.add(infoLabel);
        inputPanel.add(studentIdField);
        inputPanel.add(enterButton);
        inputPanel.add(exitButton);
        inputPanel.add(saveLogButton);

        // Add components to frame and set layout
        frame.getContentPane().add(inputPanel, BorderLayout.NORTH);
        frame.getContentPane().add(new JScrollPane(activeSessionsArea), BorderLayout.CENTER);

        // Display the frame
        frame.setVisible(true);
    }

    private static void enterSession(String studentId) {
        if (!sessions.containsKey(studentId)) {
            sessions.put(studentId, new LocalDateTime[]{LocalDateTime.now(), null});
        }
    }

    private static void exitSession(String studentId) {
        if (sessions.containsKey(studentId)) {
            LocalDateTime[] times = sessions.get(studentId);
            times[1] = LocalDateTime.now(); // Set exit time
            String record = studentId + " --- " + times[0].format(formatter) + " --- " + times[1].format(formatter);
            writeToFile(record);
        }
    }

    private static void updateActiveSessionsDisplay() {
        StringBuilder activeSessions = new StringBuilder();
        for (Map.Entry<String, LocalDateTime[]> entry : sessions.entrySet()) {
            LocalDateTime[] times = entry.getValue();
            activeSessions.append("ID: ").append(entry.getKey())
                          .append(" - Time In: ").append(times[0].format(formatter));
            if (times[1] != null) {
                activeSessions.append(" - Time Out: ").append(times[1].format(formatter));
            }
            activeSessions.append("\n");
        }
        activeSessionsArea.setText(activeSessions.toString());
    }

    private static void writeToFile(String record) {
        try (FileWriter fw = new FileWriter(Paths.get(DESKTOP_PATH, FILE_NAME).toString(), true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.print(record);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    private static void saveCurrentLog() {
        String currentLog = activeSessionsArea.getText();
        String currentLogFileName = "current_sessions_log.txt";
        try (FileWriter fw = new FileWriter(Paths.get(DESKTOP_PATH, currentLogFileName).toString());
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.print(currentLog);
        } catch (IOException e) {
            System.err.println("Error writing current log to file: " + e.getMessage());
        }
    }
}