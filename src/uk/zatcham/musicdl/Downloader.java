package uk.zatcham.musicdl;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Downloader {
    private JTextArea consoleTextArea;
    private JButton startButton, stopButton;
    private Process process;

    public Downloader(JTextArea consoleTextArea) {
        this.consoleTextArea = consoleTextArea;
    }

    /**
     * Starts download (called from UI)
     * @param platform - Platform chosen by user
     * @param folder - Folder to download into (assuming assets are already copied in)
     * @param url - Source URL to download from
     * @param token - OAuth token for soundcloud from user
     */
    public void startDownload(String platform, String folder, String url, String token) {
        if (platform.equals("YouTube")) {
            downloadYT(folder, url);
        } else if (platform.equals("SoundCloud")) {
            downloadSC(folder, url, token);
        }
    }

    /**
     * Download YT files
     * @param folder - Folder to download into
     * @param url - URL to dnwload from
     */
    private void downloadYT(String folder, String url) {
        try {
            // TODO : Add settings window to allow cookies from other browsers and change yt-dlp settings
            String[] command = {folder + "/yt-dlp.exe", "--extract-audio", "-f", "ba", "--embed-thumbnail", "--audio-quality", "0", "--audio-format", "mp3", "-o", folder + "/%(title)s.%(ext)s", "--add-metadata", "--cookies-from-browser", "edge", url};
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String finalLine = line;
                SwingUtilities.invokeLater(() -> {
                    consoleTextArea.append(finalLine + " \n");
                    consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
                    System.out.println(finalLine);
                });
            }
            int exitCode = process.waitFor();
            consoleTextArea.append("Process exited \n");
            System.out.println("Process exited with code: " + exitCode);
            consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
        } catch (InterruptedException | IOException e) {
            consoleTextArea.append("Error: " + e + " \n");
            throw new RuntimeException(e);
        }
    }


    /**
     * Downloads from Soundcloud, same as YT except has oauth token ability
     * @param folder - folder to download into
     * @param url - URL to download from
     * @param token - oauth token to use
     */
    private void downloadSC(String folder, String url, String token) {
        try {
            String[] command = {folder + "/yt-dlp.exe", "--extract-audio", "-f", "ba", "--embed-thumbnail", "--audio-quality", "0", "--audio-format", "mp3", "-o", folder + "/%(title)s.%(ext)s", "--add-metadata", "--cookies-from-browser", "edge", "--add-header", "Authorisation: OAuth " + token, url};
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                consoleTextArea.append(line + " \n");
                consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
                System.out.println(line);
            }
            int exitCode = process.waitFor();
            consoleTextArea.append("Process exited");
            System.out.println("Process exited with code: " + exitCode);
            consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
        } catch (InterruptedException | IOException e) {
            consoleTextArea.append("Error: " + e + " \n");
            throw new RuntimeException(e);
        }
    }


}
