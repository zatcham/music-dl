package uk.zatcham.musicdl;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Helpers {

//    public Helpers() {
//
//    }

    /**
     * Deletes all assets from folder used
     * @param folder - Folder used
     * @return bool
     */
    public static boolean cleanUpAfterRun(String folder) {
        // Clean up fodler by deleting assets
        System.out.println("Deleting assets");
        String[] fileNames = {"yt-dlp.exe", "ffmpeg.exe", "ffprobe.exe"};
        boolean allSuccess = true;
        for (String fileName : fileNames) {
            File file = new File(folder, fileName);
            if (file.exists()) {
                boolean success = file.delete();

                if (success) {
                    System.out.println("File '" + fileName + "' deleted successfully.");
                } else {
                    System.out.println("Failed to delete file '" + fileName + "'.");
                    allSuccess = false;
                }
            } else {
                System.out.println("File '" + fileName + "' does not exist.");
                allSuccess = false;
            }
        }
        if (allSuccess) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Copys required assets into folder
     * @param folder - Folder that is to be used for download
     */
    public static void copyReqs(String folder) {
        try {
            // Need yt-dlp ffmpeg and ffprobe
            Files.copy(new File("assets/yt-dlp.exe").toPath(), new File(folder + "/yt-dlp.exe").toPath());
            Files.copy(new File("assets/ffmpeg.exe").toPath(), new File(folder + "/ffmpeg.exe").toPath());
            Files.copy(new File("assets/ffprobe.exe").toPath(), new File(folder + "/ffprobe.exe").toPath());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error when copying assets: " + e);
            if (e instanceof java.nio.file.FileAlreadyExistsException) {
                int opt = JOptionPane.showConfirmDialog(null, "Continue? ", "Music DL", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.NO_OPTION) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Creates the folder for downloaded files to go into
     *
     * @param folder - Folder to be used for download
     * @return bool
     */
    public static boolean createFolder(String folder) {
        Path p = Paths.get(folder);
        if (Files.exists(p)) {
            System.out.println(folder + " already exists.");
            int opt = JOptionPane.showConfirmDialog(null, folder + " already exists. Would you like to continue? ", "Music DL", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                Helpers.copyReqs(folder);
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Operation cancelled.", "Music DL", JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
        } else {
            try {
                Files.createDirectories(p);
                Helpers.copyReqs(folder);
                return true;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error when creating folder: " + e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Exits program
     */
    public static void exitProg() {
        // Check user does want to exit
        int opt = JOptionPane.showConfirmDialog(null, "Are you sure you wish to exit?", "Music DL", JOptionPane.YES_NO_OPTION);

        if (opt == JOptionPane.YES_OPTION) {
            System.out.println("Goodbye xx");
            System.exit(0);
        } else {
            System.out.println("Program not exited");
        }
    }

    /**
     * Open current working directory in Explorer
     */
    public static void openCurrentDir() {
        try {
            String currentDir = System.getProperty("user.dir");
            Desktop.getDesktop().open(new File(currentDir));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks assets folder exists, and all files exist within it.
     */
    public static void checkAssetsExist() {
        System.out.println("Checking all assets exist");
        // Check 'assets' folder exists
        File folder = new File("assets/");
        if (folder.exists() && folder.isDirectory()) {
            System.out.println("assets folder exists, continuing.");
            // Check all assets exist in folder
            String[] fileNames = {"yt-dlp.exe", "ffmpeg.exe", "ffprobe.exe"};
            ArrayList<String> missingFiles = new ArrayList<>();
            boolean allExist = true;
            for (String fileName : fileNames) {
                File file = new File(folder, fileName);
                if (!file.exists() || file.isDirectory()) {
                    allExist = false;
                    missingFiles.add(fileName);
                }
            }
            if (allExist) {
                System.out.println("All files exist in assets folder");
            } else {
                // Something is missing
                System.out.println("One or more files do not exist.");
                System.out.println(missingFiles.toString());
                System.out.println("Exiting...");
                JOptionPane.showMessageDialog(null, "The following files are missing from the assets folder: " + missingFiles.toString(), "Music DL", JOptionPane.ERROR_MESSAGE);
                int opt = JOptionPane.showConfirmDialog(null, "Would you like to download the assets?", "Music DL", JOptionPane.YES_NO_OPTION);

                if (opt == JOptionPane.YES_OPTION) {
                    // Download assets
                    AssetsDownloader.main(null);
                    checkAssetsExist(); // Check assets now exist
                } else {
                    System.exit(0);
                    // TODO: Add download option , in prog
                }
            }
        } else {
            // Assets folder does not exist
            System.out.println("Assets folder does not exist, asking user if they would like it created.");
            int opt = JOptionPane.showConfirmDialog(null, "'assets' folder is missing. Would you like to create it", "Music DL", JOptionPane.YES_NO_OPTION);

            if (opt == JOptionPane.YES_OPTION) {
                // Create assets folder
                boolean success = folder.mkdir();
                if (success) {
                    JOptionPane.showMessageDialog(null, "'assets' folder created successfully. ", "Music DL", JOptionPane.INFORMATION_MESSAGE);
                    checkAssetsExist(); // Loop to re-check for assets
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to create 'assets' folder. ", "Music DL", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Folder creation cancelled. Exiting..", "Music DL", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
        }
    }

}
