package uk.zatcham.musicdl;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AssetsDownloader {

    public static void main(String[] args) {
        String downloadUrl = "https://zm-it.co.uk/music-dl/assets.zip"; // URL of the file to download
        String destinationDirectory = "assets"; // Destination directory for the downloaded and unzipped file

        try {
            String fileName = getFileNameFromUrl(downloadUrl);
            String downloadedFilePath = destinationDirectory + File.separator + fileName;

            ProgressMonitor progressMonitor = new ProgressMonitor(null, "Downloading File", "", 0, 100);
            progressMonitor.setMillisToPopup(0); // Show the progress monitor immediately

            downloadFile(downloadUrl, downloadedFilePath, progressMonitor);

            // Unzip the file
            unzipFile(downloadedFilePath, destinationDirectory);

            // Delete the downloaded zip file
            deleteFile(downloadedFilePath);

            JOptionPane.showMessageDialog(null, "File downloaded, unzipped, and zip file deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static String getFileNameFromUrl(String downloadUrl) {
        String[] urlParts = downloadUrl.split("/");
        return urlParts[urlParts.length - 1];
    }

    private static void downloadFile(String downloadUrl, String destinationPath, ProgressMonitor progressMonitor) throws IOException {
        URL url = new URL(downloadUrl);

        try (BufferedInputStream in = new BufferedInputStream(url.openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(destinationPath)) {

            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            long fileSize = url.openConnection().getContentLengthLong();
            long totalBytesRead = 0;

            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                int progress = (int) ((totalBytesRead * 100) / fileSize);
                progressMonitor.setProgress(progress);
                progressMonitor.setNote(String.format("Downloading... %d%%", progress));

                if (progressMonitor.isCanceled()) {
                    fileOutputStream.close();
                    deleteFile(destinationPath);
                    throw new IOException("Download canceled by user.");
                }
            }
        }
    }

    public static void unzipFile(String zipFilePath, String destinationDirectory) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(Files.newInputStream(Path.of(zipFilePath))))) {
            byte[] buffer = new byte[1024];
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String entryName = zipEntry.getName();
                Path entryPath = Path.of(destinationDirectory, entryName);
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.copy(zipInputStream, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zipInputStream.closeEntry();
            }
        }
    }

    public static void deleteFile(String filePath) throws IOException {
        Files.deleteIfExists(Path.of(filePath));
    }

}