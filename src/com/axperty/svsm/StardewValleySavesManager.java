package com.axperty.svsm;

import com.axperty.svsm.properties.SetLanguage;
import com.axperty.svsm.utils.GetData;
import com.axperty.svsm.utils.TableBuilder;
import com.axperty.svsm.utils.TransferSave;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
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
    private ResourceBundle bundle;

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
        bundle = SetLanguage.getBundle();
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
        frame = new JFrame();
        // Set window title using resource bundle
        frame.setTitle(bundle.getString("window.title"));
        frame.setSize(550, 350);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set window to appear in the center
        frame.setLocationRelativeTo(null);

        // Create Menu Bar
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        // Options Menu
        JMenu optionsMenu = new JMenu(bundle.getString("menu.options.title"));

        JMenuItem refreshList = new JMenuItem(bundle.getString("menu.options.refresh"));
        refreshList.addActionListener(e -> TableBuilder.updateTables(androidTable, steamTable));
        optionsMenu.add(refreshList);

        menuBar.add(optionsMenu);

        // Help Menu
        JMenu helpMenu = new JMenu(bundle.getString("menu.help.title"));

        JMenuItem connectAndroidMenuItem = new JMenuItem(bundle.getString("menu.help.connect_android"));
        connectAndroidMenuItem.addActionListener(e -> connectAndroidLink());
        helpMenu.add(connectAndroidMenuItem);

        JMenuItem donateMenuItem = new JMenuItem(bundle.getString("menu.help.donate"));
        donateMenuItem.addActionListener(e -> donateLink());
        helpMenu.add(donateMenuItem);

        JMenuItem aboutMenuItem = new JMenuItem(bundle.getString("menu.help.about"));
        aboutMenuItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

        // Steam Panel
        steamTable = TableBuilder.createTable(bundle);
        JPanel steamPanel = createPlatformPanel(steamTable);
        tabbedPane.addTab(bundle.getString("panel.steam_folder.title"), steamPanel);

        // Android Panel
        androidTable = TableBuilder.createTable(bundle);
        JPanel androidPanel = createPlatformPanel(androidTable);
        tabbedPane.addTab(bundle.getString("panel.android_folder.title"), androidPanel);

        // Initial actions
        //backupAllSteamSaves();
        TableBuilder.updateSteamTable(steamTable);
    }

    // About Dialog
    private void showAboutDialog() {
        JDialog dialog = new JDialog(frame, bundle.getString("dialog.about.title"), true);
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
        JLabel creditsLabel = new JLabel("<html><br/>Developed by Axperty<br/>Under the MIT License<br/>Version 0.0.0.2-alpha<br/><br/>Stardew Valley is a game made by ConcernedApe.<br/></html>");
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
            JOptionPane.showMessageDialog(frame, bundle.getString("dialog.error.donate_link") + ex.getMessage());
        }
    }

    private void connectAndroidLink() {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/axperty/stardew-valley-saves-manager?tab=readme-ov-file#requirements"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, bundle.getString("dialog.error.connect_android_link" + ex.getMessage()));
        }
    }

    // Create the panel to display data
    private JPanel createPlatformPanel(JTable table) {
        JPanel panel = new JPanel(new BorderLayout());

        // Center panel for the table
        JPanel centerPanel = new JPanel(new BorderLayout());

        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        // Search bar panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel searchLabel = new JLabel(bundle.getString("search.bar.title"));
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
        String targetPlatform = (table == androidTable) ? " Steam" : " Android";
        JButton moveButton = new JButton(bundle.getString("label.move_save_to") + targetPlatform);
        moveButton.addActionListener(e -> moveSaveBetweenPlatforms(table, targetPlatform));
        buttonPanel.add(moveButton);

        // Backup Save button
        JButton backupButton = new JButton(bundle.getString("button.backup"));
        backupButton.addActionListener(e -> backupSave(table));
        buttonPanel.add(backupButton);

        // Delete Save button
        JButton deleteButton = new JButton(bundle.getString("button.delete_save"));
        deleteButton.addActionListener(e -> deleteSave(table));
        buttonPanel.add(deleteButton);

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
                TransferSave.moveSaveToSteam(saveName, saveId);
            } else if (targetPlatform.equals("Android")) {
                TransferSave.moveSaveToAndroid(saveName, saveId);
            }
        } else {
            JOptionPane.showMessageDialog(frame, bundle.getString("dialog.select_save_to_move"));
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
                            JOptionPane.showMessageDialog(frame, bundle.getString("dialog.error.backup.device"));
                        }
                    } else {
                        // Copy the save locally
                        Files.copy(Paths.get(sourcePath), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                        JOptionPane.showMessageDialog(frame, bundle.getString("dialog.success.backup_device"));
                    }
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, bundle.getString("dialog.error.backup.device"));
                }
            }
        } else {
            JOptionPane.showMessageDialog(frame, bundle.getString("dialog.select_save_to_backup"));
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
                            JOptionPane.showMessageDialog(frame, bundle.getString("dialog.error.delete_save"));
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
                                        JOptionPane.showMessageDialog(frame, bundle.getString("dialog.error.delete_save"));
                                    }
                                });
                    }

                    TableBuilder.updateTables(steamTable, androidTable);
                    JOptionPane.showMessageDialog(frame, "Save deleted successfully!");
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, bundle.getString("dialog.error.delete_save"));
                }
            }
        } else {
            JOptionPane.showMessageDialog(frame, bundle.getString("dialog.select_save_to_delete"));
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