package uk.zatcham.musicdl;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) throws IOException {

        boolean isWin = System.getProperty("os.name").toLowerCase().startsWith("windows");
        if (!isWin) {
            System.out.println("This program will only run on Windows.. sorry");
            System.exit(0);
        }

        Scanner scan = new Scanner(System.in);
        System.out.println("Select Y for Youtube, or S for soundcloud");
        String i1 = scan.nextLine();
        i1 = i1.toLowerCase();

        if (i1.equals("y")) {
            System.out.println("Youtube selected");
            System.out.println("Enter URL: ");
            String url = scan.nextLine();
            System.out.println("Enter folder name: ");
            String folder = scan.nextLine();

            Path p = Paths.get(folder);
            if (Files.exists(p)) {
                System.out.println("Folder already exists. Continue?");
                String i2 = scan.nextLine().toLowerCase();
                if (i2.equals("y")) {
                    try {
                        Files.createDirectories(p);
                        copyReqs(folder);
                        downloadYT(folder, url);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                try {
                    // Create directory and move files
                    Files.createDirectories(p);
                    copyReqs(folder);
                    // Files.copy(new File("assets/aria2c.exe").toPath(), new File(folder + "/aria2c.exe").toPath()); // Disabled due to aria2c issue

                    // Do download
                    downloadYT(folder, url);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        } else if (i1.equals("s")) {
            System.out.println("Soundcloud selected");
            System.out.println("Enter URL: ");
            String url = scan.nextLine();
            System.out.println("Enter folder name: ");
            String folder = scan.nextLine();
            System.out.println("Enter oauth token: ");
            String token = scan.nextLine();

            Path p = Paths.get(folder);
            if (Files.exists(p)) {
                System.out.println("Folder already exists. Continue?");
                String i2 = scan.nextLine().toLowerCase();
                if (i2.equals("y")) {
                    try {
                        Files.createDirectories(p);
                        copyReqs(folder);
                        downloadSC(folder, url, token);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                try {
                    // Create directory and move files
                    Files.createDirectories(p);
                    copyReqs(folder);

                    // Do download
                    downloadSC(folder, url, token);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    private static void copyReqs(String folder) {
        try {
            Files.copy(new File("assets/yt-dlp.exe").toPath(), new File(folder + "/yt-dlp.exe").toPath());
            Files.copy(new File("assets/ffmpeg.exe").toPath(), new File(folder + "/ffmpeg.exe").toPath());
            Files.copy(new File("assets/ffprobe.exe").toPath(), new File(folder + "/ffprobe.exe").toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void downloadYT(String folder, String url) {
        try {
            String[] command = {folder + "/yt-dlp.exe", "--extract-audio", "-f", "ba", "--embed-thumbnail", "--audio-quality", "0", "--audio-format", "mp3", "-o", folder + "/%(title)s.%(ext)s", "--add-metadata", "--cookies-from-browser", "edge", url};
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = process.waitFor();
            System.out.println("Process exited with code: " + exitCode);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void downloadSC(String folder, String url, String token) {
        try {
            String[] command = {folder + "/yt-dlp.exe", "--extract-audio", "-f", "ba", "--embed-thumbnail", "--audio-quality", "0", "--audio-format", "mp3", "-o", folder + "/%(title)s.%(ext)s", "--add-metadata", "--cookies-from-browser", "edge", "--add-header", "Authorisation: OAuth " + token, url};
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = process.waitFor();
            System.out.println("Process exited with code: " + exitCode);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}

