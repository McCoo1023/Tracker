package Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class GuidanceTracker {
    private static HashMap<String, LocalDateTime[]> sessions = new HashMap<>();
    private static final String FILE_NAME = "sessions_log.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static DefaultTableModel model;
    private static final String[] columnNames = {"Select", "ID", "Time In", "Time Out", "Elapsed Time"};

    public static void main(String[] args) {
        // Create the frame
        JFrame frame = new JFrame("Guidance Session Tracker");
        frame.setSize(700, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // HUFSD Logo
        try {
            BufferedImage iconImage = ImageIO.read(GuidanceTracker.class.getResource("HUFSD.png"));
            frame.setIconImage(iconImage);
        } catch (IOException e) {
            System.err.println("Error setting icon: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        // Create components
        JPanel inputPanel = new JPanel();
        JLabel infoLabel = new JLabel("Enter your ID #:");
        JTextField studentIdField = new JTextField(10);
        JButton enterButton = new JButton("Enter");
        JButton exitButton = new JButton("Exit");
        JButton saveLogButton = new JButton("Save Log");
        model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model) {
            public Class getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }
        };
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Adjusting the column width
        int selectColumnWidth = new JLabel("Select").getPreferredSize().width + 7; // 10 for extra breathing room
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(selectColumnWidth);

        // Add action listeners to the buttons
        enterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String studentId = studentIdField.getText();
                enterSession(studentId);
                studentIdField.setText(""); // Clear the field
                updateTableDisplay();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int rowIndex = getSelectedRowIndex();
                if (rowIndex != -1) {
                    String studentId = model.getValueAt(rowIndex, 1).toString();
                    exitSession(studentId);
                    updateTableDisplay();
                }
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
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

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
            String record = studentId + " --- " + times[0].format(formatter) + " --- " + times[1].format(formatter) + " --- Counselor's Name\n";
            writeToFile(record);
        }
    }

    private static int getSelectedRowIndex() {
        for (int i = 0; i < model.getRowCount(); i++) {
            Boolean isChecked = (Boolean) model.getValueAt(i, 0);
            if (isChecked != null && isChecked) {
                return i;
            }
        }
        return -1; // No selection
    }
    
    private static String getDesktopPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (os.contains("win")) {
            return userHome + "\\Desktop"; // Windows
        } else if (os.contains("mac")) {
            return userHome + "/Desktop"; // MacOS
        } else {
            return userHome + "/Desktop"; // Assuming a Linux or other Unix-like OS
        }
    }
    
    private static final String DESKTOP_PATH = getDesktopPath();


    private static void updateTableDisplay() {
        model.setRowCount(0); // Clear existing rows
        for (Map.Entry<String, LocalDateTime[]> entry : sessions.entrySet()) {
            LocalDateTime[] times = entry.getValue();
            Vector<Object> row = new Vector<>();
            row.add(false);
            row.add(entry.getKey());
            row.add(times[0].format(formatter));
            String timeOutString = times[1] != null ? times[1].format(formatter) : "";
            row.add(timeOutString);
            row.add(calculateElapsedTime(times[0], times[1]));
            model.addRow(row);
        }
    }
    
    private static String calculateElapsedTime(LocalDateTime timeIn, LocalDateTime timeOut) {
        if (timeIn != null && timeOut != null) {
            long seconds = java.time.Duration.between(timeIn, timeOut).getSeconds();
            long minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%d min %d seconds", minutes, seconds);
        } else {
            return "";
        }
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
        StringBuilder currentLog = new StringBuilder();
        for (int i = 0; i < model.getRowCount(); i++) {
            for (int j = 1; j < model.getColumnCount(); j++) { // Skip the checkbox column
                currentLog.append(model.getValueAt(i, j)).append(" --- ");
            }
            currentLog.append("\n");
        }
        String currentLogFileName = "current_sessions_log.txt";
        try (FileWriter fw = new FileWriter(Paths.get(DESKTOP_PATH, currentLogFileName).toString());
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.print(currentLog.toString());
        } catch (IOException e) {
            System.err.println("Error writing current log to file: " + e.getMessage());
        }
    }
}