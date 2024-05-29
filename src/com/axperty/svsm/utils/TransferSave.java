package com.axperty.svsm.utils;

import java.io.IOException;

public class TransferSave {
    // Move Save to Steam
    public static void moveSaveToSteam(String saveName, String saveId) {
        String androidPath = "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/" + saveName + "_" + saveId;
        String steamPath = System.getenv("APPDATA") + "/StardewValley/Saves/" + saveName + "_" + saveId;
        executeSaveTransfer(androidPath, steamPath);
    }

    // Move Save to Android
    public static void moveSaveToAndroid(String saveName, String saveId) {
        String steamPath = System.getenv("APPDATA") + "/StardewValley/Saves/" + saveName + "_" + saveId;
        String androidPath = "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/" + saveName + "_" + saveId;
        executeSaveTransfer(steamPath, androidPath);
    }

    // --- Save Transfer Methods ---
    private static void executeSaveTransfer(String sourcePath, String destinationPath) {
        try {
            // Determine whether to push or pull based on source path
            String[] command = (sourcePath.startsWith("/storage"))
                    ? new String[]{"adb", "pull", sourcePath, destinationPath}
                    : new String[]{"adb", "push", sourcePath, destinationPath};

            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Save transferred successfully!");
            } else {
                System.err.println("Error transferring save. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            System.err.println("Error transferring save: " + ex.getMessage());
        }
    }
}