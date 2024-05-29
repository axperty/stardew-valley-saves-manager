package com.axperty.svsm.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GetData {
    // Helper function to get the last modified date of an Android file using ADB
    private static String getAndroidLastModifiedDate(String fileName) {
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
            System.err.println("Error getting last modified date for Android save: " + e.getMessage());
            return "N/A";
        }
    }

    // Helper function to get the last modified date of a Steam save
    private static String getLastModifiedDate(Path path) {
        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            FileTime lastModifiedTime = attr.lastModifiedTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            return dateFormat.format(new Date(lastModifiedTime.toMillis()));
        } catch (IOException e) {
            System.err.println("Error getting last modified date for Steam save: " + e.getMessage());
            return "N/A";
        }
    }

    public static List<String[]> getAndroidSavesData() {
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
                System.err.println("No Android device connected.");
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
                    saveData.add(new String[]{saveName + " Farm", saveId, lastPlayed});
                }
            }

            // Error handling
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Error executing ADB command. Exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error getting Android save data: " + e.getMessage());
        }

        return saveData;
    }

    public static List<String[]> getSteamSavesData() {
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
                            saveData.add(new String[]{saveName + " Farm", saveID, lastPlayed});
                        } else {
                            System.err.println("Skipping invalid save folder name: " + fileName);
                        }
                    });

        } catch (IOException e) {
            System.err.println("Error: Unable to access Steam saves directory: " + e.getMessage());
        }

        return saveData;
    }
}