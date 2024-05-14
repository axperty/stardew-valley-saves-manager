import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StardewValleySavesManager {
    private JFrame frame;
    private JTabbedPane tabbedPane;
    private JTable androidTable;
    private JTable steamTable;
    private JTextField steamSearchField;
    private JTextField androidSearchField;
    private TableRowSorter<DefaultTableModel> steamSorter;
    private TableRowSorter<DefaultTableModel> androidSorter;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                StardewValleySavesManager window = new StardewValleySavesManager();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public StardewValleySavesManager() {
        initialize();
    }

    private void initialize() {

        // Set the Windows Look and Feel
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Windows Look and Feel not available: " + e.getMessage());
        }

        // Initialize main frame
        frame = new JFrame("Stardew Valley Saves Manager");
        frame.setSize(550, 350);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set window to appear in the center
        frame.setLocationRelativeTo(null);

        // Create Menu Bar
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        // About Menu
        JMenu aboutMenu = new JMenu("About");
        JMenuItem aboutMenuItem = new JMenuItem("Credits");
        aboutMenuItem.addActionListener(e -> showAboutDialog());
        aboutMenu.add(aboutMenuItem);

        JMenuItem donateMenuItem = new JMenuItem("Donate");
        donateMenuItem.addActionListener(e -> donateLink());
        aboutMenu.add(donateMenuItem);
        menuBar.add(aboutMenu);

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem connectAndroidMenuItem = new JMenuItem("Connect Android device");
        connectAndroidMenuItem.addActionListener(e -> connectAndroidLink());
        helpMenu.add(connectAndroidMenuItem);
        menuBar.add(helpMenu);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

        // Steam Panel
        steamTable = createTable();
        JPanel steamPanel = createPlatformPanel(steamTable);
        tabbedPane.addTab("Steam Folder", steamPanel);

        // Android Panel
        androidTable = createTable();
        JPanel androidPanel = createPlatformPanel(androidTable);
        tabbedPane.addTab("Android Folder", androidPanel);

        // Initial actions
        backupAllSteamSaves();
        updateSteamTable();
    }

    // About Dialog
    private void showAboutDialog() {
        JDialog dialog = new JDialog(frame, "Credits", true);
        dialog.setSize(450, 290);
        dialog.setResizable(false);
        dialog.setLayout(new BorderLayout());

        // Panel to hold the content and center it
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // Panel for the vertically stacked content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Title
        JLabel titleLabel = new JLabel("<html><br>Stardew Valley Saves Manager</html>");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16));
        contentPanel.add(titleLabel);

        // Description
        JLabel descriptionLabel = new JLabel("<html><br>A simple manager for Stardew Valley saves, that allows to <br>backup, delete, or move " +
                "saves from Steam or your <br/>Android device easily.<br/></html>");
        descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(Font.PLAIN, 14));
        contentPanel.add(descriptionLabel);

        // Credits
        JLabel creditsLabel = new JLabel("<html><br/>Developed by Axperty.<br/>Under the MIT License.<br/>Version 0.0.1<br/><br/>Stardew Valley is a game made by ConcernedApe.<br/></html>");
        creditsLabel.setFont(creditsLabel.getFont().deriveFont(Font.BOLD, 12));
        contentPanel.add(creditsLabel);

        // Add the contentPanel to the centerPanel
        centerPanel.add(contentPanel);

        // Add the centerPanel to the NORTH of the dialog
        dialog.add(centerPanel, BorderLayout.NORTH);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }


    // Menu Links
    private void donateLink() {
        try {
            Desktop.getDesktop().browse(new URI("https://paypal.me/kevgelhorn"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error opening donation link: " + ex.getMessage());
        }
    }

    private void connectAndroidLink() {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/axperty/stardew-valley-saves-manager?tab=readme-ov-file#requirements"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error opening donation link: " + ex.getMessage());
        }
    }

    private JPanel createPlatformPanel(JTable table) {
        JPanel panel = new JPanel(new BorderLayout());

        // Center panel for subtitle and table
        JPanel centerPanel = new JPanel(new BorderLayout());

        // Subtitle panel
        JPanel subtitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel subtitleLabel = new JLabel("");
        subtitleLabel.setFont(new Font(subtitleLabel.getFont().getName(), Font.PLAIN, 14));
        subtitlePanel.add(subtitleLabel);
        centerPanel.add(subtitlePanel, BorderLayout.NORTH);

        // Modify subtitle text based on the table being used
        if (table == steamTable) {
            subtitleLabel.setText("Files on your Steam Folder");
        } else if (table == androidTable) {
            subtitleLabel.setText("Files on your Android Folder");
        }

        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        // Search bar panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel searchLabel = new JLabel("Search");
        JTextField searchField = new JTextField(15);
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        centerPanel.add(searchPanel, BorderLayout.SOUTH);

        // Add search functionality based on the table
        if (table == steamTable) {
            steamSearchField = searchField;
            steamSorter = new TableRowSorter<>((DefaultTableModel) steamTable.getModel());
            steamTable.setRowSorter(steamSorter);
            searchField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    filterTable(steamSearchField, steamSorter);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    filterTable(steamSearchField, steamSorter);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    filterTable(steamSearchField, steamSorter);
                }
            });
        } else if (table == androidTable) {
            androidSearchField = searchField;
            androidSorter = new TableRowSorter<>((DefaultTableModel) androidTable.getModel());
            androidTable.setRowSorter(androidSorter);
            searchField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    filterTable(androidSearchField, androidSorter);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    filterTable(androidSearchField, androidSorter);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    filterTable(androidSearchField, androidSorter);
                }
            });
        }

        // Panel for buttons to arrange them horizontally
        JPanel buttonPanel = new JPanel(new FlowLayout());

        // Move to other platform button
        String targetPlatform = (table == androidTable) ? "Steam" : "Android";
        JButton moveButton = new JButton("Move Save to " + targetPlatform);
        moveButton.addActionListener(e -> moveSaveBetweenPlatforms(table, targetPlatform));
        buttonPanel.add(moveButton);

        // Backup Save button
        JButton backupButton = new JButton("Backup Save");
        backupButton.addActionListener(e -> backupSave(table));
        buttonPanel.add(backupButton);

        // Delete Save button
        JButton deleteButton = new JButton("Delete Save");
        deleteButton.addActionListener(e -> deleteSave(table));
        buttonPanel.add(deleteButton);

        // Refresh List button
        JButton refreshButton = new JButton("Refresh List");
        refreshButton.addActionListener(e -> updateTables());
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void filterTable(JTextField searchField, TableRowSorter<DefaultTableModel> sorter) {
        String text = searchField.getText();
        if (text.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            try {
                // Search in both Save Name and ID columns
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0, 1));
            } catch (java.util.regex.PatternSyntaxException e) {
                System.err.println("Invalid regex pattern: " + e.getMessage());
            }
        }
    }

    // Move Save Between Platforms
    private void moveSaveBetweenPlatforms(JTable sourceTable, String targetPlatform) {
        int selectedRow = sourceTable.getSelectedRow();
        if (selectedRow != -1) {
            String saveName = (String) sourceTable.getValueAt(selectedRow, 0);
            String saveId = (String) sourceTable.getValueAt(selectedRow, 1);

            if (targetPlatform.equals("Steam")) {
                moveSaveToSteam(saveName, saveId);
            } else if (targetPlatform.equals("Android")) {
                moveSaveToAndroid(saveName, saveId);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please choose a save to move.");
        }
    }

    // Move Save to Steam
    private void moveSaveToSteam(String saveName, String saveId) {
        String androidPath = "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/" + saveName + "_" + saveId;
        String steamPath = System.getenv("APPDATA") + "/StardewValley/Saves/" + saveName + "_" + saveId;
        executeSaveTransfer(androidPath, steamPath);
    }

    // Move Save to Android
    private void moveSaveToAndroid(String saveName, String saveId) {
        String steamPath = System.getenv("APPDATA") + "/StardewValley/Saves/" + saveName + "_" + saveId;
        String androidPath = "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/" + saveName + "_" + saveId;
        executeSaveTransfer(steamPath, androidPath);
    }

    // --- Save Transfer Methods ---
    private void executeSaveTransfer(String sourcePath, String destinationPath) {
        try {
            // Determine whether to push or pull based on source path
            String[] command = (sourcePath.startsWith("/storage"))
                    ? new String[]{"adb", "pull", sourcePath, destinationPath}
                    : new String[]{"adb", "push", sourcePath, destinationPath};

            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                updateTables();
                JOptionPane.showMessageDialog(frame, "Save moved successfully!");
            } else {
                System.err.println("No Android device connected.");
                JOptionPane.showMessageDialog(frame,
                        "No Android device connected. Please connect your device and click 'Move Save to Android'.",
                        "No Device",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error moving save. Check console for details.");
        }
    }

    // --- Table Methods ---
    private JTable createTable() {
        JTable table = new JTable();
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Farm Name");
        model.addColumn("ID");
        model.addColumn("Last Played");
        table.setModel(model);
        table.setRowHeight(25);
        JTableHeader header = table.getTableHeader();
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        return table;
    }

    // Update Lists
    private void updateTables() {
        updateAndroidTable();
        updateSteamTable();
    }

    // Update Android Table
    private void updateAndroidTable() {
        DefaultTableModel model = (DefaultTableModel) androidTable.getModel();
        model.setRowCount(0); // Clear existing data
        List<String[]> saveData = getAndroidSavesData();
        for (String[] data : saveData) {
            model.addRow(data);
        }
    }

    // Update Steam Table
    private void updateSteamTable() {
        DefaultTableModel model = (DefaultTableModel) steamTable.getModel();
        model.setRowCount(0); // Clear existing data
        List<String[]> saveData = getSteamSavesData();
        for (String[] data : saveData) {
            model.addRow(data);
        }
    }

    // --- Data Retrieval Methods (using ADB) ---
    private List<String[]> getAndroidSavesData() {
        List<String[]> saveData = new ArrayList<>();
        try {
            // Check if an Android device is connected
            String[] deviceListCommand = {"adb", "devices"};
            Process deviceListProcess = Runtime.getRuntime().exec(deviceListCommand);
            BufferedReader deviceListReader = new BufferedReader(new InputStreamReader(deviceListProcess.getInputStream()));

            // Skip the first line ("List of devices attached")
            deviceListReader.readLine();
            String deviceLine;
            boolean deviceFound = false;
            while ((deviceLine = deviceListReader.readLine()) != null) {
                if (deviceLine.trim().endsWith("device")) {
                    deviceFound = true;
                    break;
                }
            }

            if (!deviceFound) {
                JOptionPane.showMessageDialog(frame,
                        "No Android device connected. Please connect your device and click 'Refresh'.",
                        "No Device",
                        JOptionPane.WARNING_MESSAGE);
                return saveData;
            }

            // List files in the Android saves directory
            String[] command = {"adb", "shell", "ls", "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves"};
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // Process each file found
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("_");
                if (parts.length == 2) {
                    String saveName = parts[0];
                    String saveId = parts[1];

                    // Get last modified date using ADB
                    String lastPlayed = getAndroidLastModifiedDate(saveName + "_" + saveId);
                    saveData.add(new String[]{saveName, saveId, lastPlayed});
                }
            }

            // Error handling
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Error executing ADB command. Exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return saveData;
    }

    private List<String[]> getSteamSavesData() {
        List<String[]> saveData = new ArrayList<>();

        // Get the Steam user's AppData path
        String appDataPath = System.getenv("APPDATA");
        if (appDataPath == null) {
            System.err.println("Error: Unable to get APPDATA environment variable.");
            return saveData;
        }

        String savesDirectory = appDataPath + "/StardewValley/Saves";

        // Read directory contents using Java File API
        try {
            Files.list(Paths.get(savesDirectory))
                    .filter(Files::isDirectory)
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String[] parts = fileName.split("_");
                        if (parts.length == 2) {
                            String saveName = parts[0];
                            String saveID = parts[1];
                            String lastPlayed = getLastModifiedDate(path);
                            saveData.add(new String[]{saveName, saveID, lastPlayed});
                        } else {
                            System.err.println("Skipping invalid save folder name: " + fileName);
                        }
                    });

        } catch (IOException e) {
            System.err.println("Error: Unable to access Steam saves directory: " + e.getMessage());
        }

        return saveData;
    }

    private String getLastModifiedDate(Path path) {
        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            FileTime lastModifiedTime = attr.lastModifiedTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            return dateFormat.format(new Date(lastModifiedTime.toMillis()));
        } catch (IOException e) {
            System.err.println("Error getting last modified date for " + path.toString() +
                    ": " + e.getMessage());
            return "N/A";
        }
    }

    // Helper function to get the last modified date of an Android file using ADB
    private String getAndroidLastModifiedDate(String fileName) {
        try {
            String filePath = "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/" + fileName;
            String[] command = {"adb", "shell", "stat", "-c", "%y", filePath};
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String lastModified = reader.readLine();

            // Format the output
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy");
            Date date = inputFormat.parse(lastModified.trim());
            return outputFormat.format(date);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    private void backupSave(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            String saveName = (String) table.getValueAt(selectedRow, 0);
            String saveId = (String) table.getValueAt(selectedRow, 1);

            // Get source path based on selected tab
            String sourcePath = (table == androidTable)
                    ? "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/" + saveName + "_" + saveId
                    : System.getenv("APPDATA") + "/StardewValley/Saves/" + saveName + "_" + saveId;

            // Open file chooser dialog
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showSaveDialog(frame);

            if (result == JFileChooser.APPROVE_OPTION) {
                Path destinationPath = Paths.get(fileChooser.getSelectedFile().getAbsolutePath(), saveName + "_" + saveId);
                try {
                    if (sourcePath.startsWith("/storage")) {
                        // Use ADB to pull the save from Android
                        String[] command = {"adb", "pull", sourcePath, destinationPath.toString()};
                        Process process = Runtime.getRuntime().exec(command);
                        int exitCode = process.waitFor();
                        if (exitCode == 0) {
                            JOptionPane.showMessageDialog(frame, "Save backed up successfully!");
                        } else {
                            System.err.println("Error backing up save from Android. Exit code: " + exitCode);
                            JOptionPane.showMessageDialog(frame, "Error backing up save. Check console for details.");
                        }
                    } else {
                        // Copy the save locally
                        Files.copy(Paths.get(sourcePath), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                        JOptionPane.showMessageDialog(frame, "Save backed up successfully!");
                    }
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error backing up save. Check console for details.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a save to backup.");
        }
    }

    private void deleteSave(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            String saveName = (String) table.getValueAt(selectedRow, 0);
            String saveId = (String) table.getValueAt(selectedRow, 1);

            // Confirmation dialog
            int result = JOptionPane.showConfirmDialog(frame,
                    "Are you sure you want to delete the save \"" + saveName + "_" + saveId + "\"?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                String savePath = (table == androidTable)
                        ? "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/" + saveName + "_" + saveId
                        : System.getenv("APPDATA") + "/StardewValley/Saves/" + saveName + "_" + saveId;

                try {
                    if (table == androidTable) {
                        // Execute ADB command to delete save from Android
                        String[] deleteCommand = {"adb", "shell", "rm", "-rf", savePath};
                        Process deleteProcess = Runtime.getRuntime().exec(deleteCommand);
                        int deleteExitCode = deleteProcess.waitFor();
                        if (deleteExitCode != 0) {
                            System.err.println("Error deleting save from Android. Exit code: " + deleteExitCode);
                            JOptionPane.showMessageDialog(frame, "Error deleting save. Check console for details.");
                            return;
                        }
                    } else {
                        // Delete save from Steam folder using Java File API
                        Files.walk(Paths.get(savePath))
                                .sorted((p1, p2) -> -1 * p1.compareTo(p2)) // Delete files first, then directory
                                .forEach(path -> {
                                    try {
                                        Files.delete(path);
                                    } catch (IOException ex) {
                                        System.err.println("Error deleting " + path + ": " + ex.getMessage());
                                        JOptionPane.showMessageDialog(frame, "Error deleting save. Check console for details.");
                                    }
                                });
                    }

                    updateTables();
                    JOptionPane.showMessageDialog(frame, "Save deleted successfully!");
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error deleting save. Check console for details.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a save to delete.");
        }
    }

    private void backupAllSteamSaves() {
        String appDataPath = System.getenv("APPDATA");
        if (appDataPath == null) {
            System.err.println("Error: Unable to get APPDATA environment variable.");
            return;
        }

        String savesDirectory = appDataPath + "/StardewValley/Saves";
        String backupDirectory = System.getProperty("user.dir") + "/SteamSavesBackup"; // Backup folder in the same directory as the jar

        // Create the "Backup" directory if it doesn't exist
        Path backupPath = Paths.get(backupDirectory);
        if (!Files.exists(backupPath)) {
            try {
                Files.createDirectory(backupPath);
            } catch (IOException e) {
                System.err.println("Error creating backup directory: " + e.getMessage());
                return; // Stop if directory creation fails
            }
        }

        // Copy each save folder to the "Backup" directory
        try {
            Files.list(Paths.get(savesDirectory))
                    .filter(Files::isDirectory)
                    .forEach(sourcePath -> {
                        String saveName = sourcePath.getFileName().toString();
                        Path destinationPath = Paths.get(backupDirectory, saveName);
                        try {
                            copyDirectory(sourcePath, destinationPath);
                            System.out.println("Backed up save: " + saveName);
                        } catch (IOException e) {
                            System.err.println("Error backing up save " + saveName + ": " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error reading saves directory: " + e.getMessage());
        }
    }

    // Helper function to recursively copy a directory
    private void copyDirectory(Path source, Path destination) throws IOException {
        if (Files.notExists(destination)) {
            Files.createDirectory(destination);
        }

        Files.walk(source)
                .forEach(sourcePath -> {
                    Path destPath = destination.resolve(source.relativize(sourcePath));
                    try {
                        Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.err.println("File already exists " + sourcePath + ": " + e.getMessage());
                    }
                });
    }
}