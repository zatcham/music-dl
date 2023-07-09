package uk.zatcham.musicdl;
import javax.swing.*;
import java.util.List;
// Java AWT must be imported one by one instead of * as otherwise awt.list is used
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DownloaderUI extends JFrame {
    private JRadioButton soundcloudRadioButton;
    private JRadioButton youtubeRadioButton;
    private JTextField urlTextField;
    private JTextField folderTextField;
    private JTextField oauthTokenTextField;
    private JTextArea consoleTextArea;
    private JButton startButton;
    private JButton stopButton;

    public DownloaderUI() {
        setTitle("Downloader UI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));

        try {
            // Set the Nimbus Look and Feel
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Panel for radio buttons
        JPanel radioButtonPanel = new JPanel();
        soundcloudRadioButton = new JRadioButton("SoundCloud");
        youtubeRadioButton = new JRadioButton("YouTube");
        ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(soundcloudRadioButton);
        radioGroup.add(youtubeRadioButton);
        radioButtonPanel.add(soundcloudRadioButton);
        radioButtonPanel.add(youtubeRadioButton);

        // Panel for input fields
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 10, 10, 10);

        constraints.gridx = 0;
        constraints.gridy = 0;
        inputPanel.add(new JLabel("URL: "), constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 2.0;
        urlTextField = new JTextField();
        inputPanel.add(urlTextField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        inputPanel.add(new JLabel("Folder Name: "), constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 2.0;
        folderTextField = new JTextField();
        inputPanel.add(folderTextField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        JLabel oauthTokenLabel = new JLabel("OAuth Token: ");
        inputPanel.add(oauthTokenLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.weightx = 2.0;
        oauthTokenTextField = new JTextField();
        inputPanel.add(oauthTokenTextField, constraints);

        // Panel for console area
        JPanel consolePanel = new JPanel(new BorderLayout());
        consolePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        consoleTextArea = new JTextArea();
        consoleTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(consoleTextArea);
        consolePanel.add(scrollPane, BorderLayout.CENTER);
        consoleTextArea.setBackground(Color.black);
        consoleTextArea.setForeground(Color.white);

        // Start and stop button
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false); // Stop button disable by default
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startDownload();
            }
        });
        // Stop Button listener
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (process != null) {
                    process.destroyForcibly(); // this doesnt work
                }
                try {
                    Runtime.getRuntime().exec("taskkill /F /IM yt-dlp.exe");
                    cleanUpAfterRun(folderTextField.getText()); // usually doesnt get cleaned up at first
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                stopButton.setEnabled(false);
                startButton.setEnabled(true);
            }
        });


        // Radio button listener for OAuth token field
        soundcloudRadioButton.addItemListener(e -> {
            if (e.getStateChange() == 1) { // selected
                oauthTokenLabel.setVisible(true);
                oauthTokenTextField.setVisible(true);
            } else {
                oauthTokenLabel.setVisible(false);
                oauthTokenTextField.setVisible(false);
            }
        });
        // Set to initally be hidden
        oauthTokenLabel.setVisible(false);
        oauthTokenTextField.setVisible(false);

        // Split panel for input fields and console area
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, consolePanel);
        splitPane.setResizeWeight(0.5);

        // Add components to the main frame
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(radioButtonPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Music DL");

        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.addActionListener(e -> openSettings());
        menu.add(settingsItem);

        JMenuItem infoItem = new JMenuItem("About");
        infoItem.addActionListener(e -> openInfo());
        menu.add(infoItem);

        menuBar.add(menu);
//        menuBar.add(infoItem);
//        menuBar.add(settingsItem);
        setJMenuBar(menuBar);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void openSettings() {
        JOptionPane.showMessageDialog(null, "Settings", "Music DL", JOptionPane.ERROR_MESSAGE);
    }

    private void openInfo() {
        JOptionPane.showMessageDialog(null, "About", "Music DL", JOptionPane.ERROR_MESSAGE);
    }

    private void startDownload() {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Retrieve user input
                String url = urlTextField.getText();
                String folderName = folderTextField.getText();
                String oauthToken = oauthTokenTextField.getText();

                // Determine the selected platform
                String platform = "";
                if (soundcloudRadioButton.isSelected()) {
                    platform = "SoundCloud";
                } else if (youtubeRadioButton.isSelected()) {
                    platform = "YouTube";
                } else {
                    JOptionPane.showMessageDialog(null, "Please select a platform.", "Error", JOptionPane.ERROR_MESSAGE);
                    return null;
                }

                // Change button enabled
                startButton.setEnabled(false);
                stopButton.setEnabled(true);

                // Perform the download operation based on the selected platform
                consoleTextArea.append("---\n");
                consoleTextArea.append("Platform: " + platform + "\n");
                consoleTextArea.append("URL: " + url + "\n");
                consoleTextArea.append("Folder Name: " + folderName + "\n");
                consoleTextArea.append("OAuth Token: " + oauthToken + "\n");

                consoleTextArea.append("Creating folder\n");
                // Create folder to be used, check if successful
                if (createFolder(folderName)) {
                    consoleTextArea.append("Starting yt-dlp...\n");

                    // Run platform's download function
                    if (platform.equals("SoundCloud")) {
                        downloadSC(folderName, url, oauthToken);
                        cleanUpAfterRun(folderName);
                    } else if (platform.equals("YouTube")) {
                        downloadYT(folderName, url);
                        consoleTextArea.append("Deleting assets from folder.. \n");
                        if (cleanUpAfterRun(folderName)) {
                            consoleTextArea.append("Successfully deleted assets \n");
                            consoleTextArea.append("Download Complete \n");
                            consoleTextArea.append("----- \n");
                            stopButton.setEnabled(false);
                            startButton.setEnabled(true);
                        } else {
                            consoleTextArea.append("Error occured whilst deleting assets\n");
                            // Oh well -- if stop button used, try deleting there
                            consoleTextArea.append("Download Complete \n");
                            consoleTextArea.append("----- \n");
                            stopButton.setEnabled(false);
                            startButton.setEnabled(true);
                        }
                    }
                } else {
                    consoleTextArea.append("Error creating folder\n");
                }

                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String line : chunks) {
                    consoleTextArea.append(line + "\n");
                }
            }

//            @Override
//            protected void done() {
//                consoleTextArea.append("Process exited.\n");
//            }
        };

        worker.execute();

    }

    public static void main(String[] args) throws IOException {
        // Do pre req checks
        // Check OS is Windows
        boolean isWin = System.getProperty("os.name").toLowerCase().startsWith("windows");
        if (!isWin) {
            JOptionPane.showMessageDialog(null, "This program will only run on Windows. Exiting..", "Music DL", JOptionPane.ERROR_MESSAGE);
            System.out.println("Error: Invalid OS. ");
            System.exit(0);
        }
        // Check all assets exist
        checkAssetsExist();

        // Run UI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DownloaderUI();
            }
        });
    }

    /**
     * Checks assets folder exists, and all files exist within it.
     */
    public static void checkAssetsExist() throws IOException {
        System.out.println("Checking all assets exist");
        // Check 'assets' folder exists
        File folder = new File("assets/");
        if (folder.exists() && folder.isDirectory()) {
            System.out.println("assets folder exists, continuing.");
            // Check all assets exist in folder
            String[] fileNames = {"yt-dlp.exe", "ffmpeg.exe", "ffprobe.exe"};
            ArrayList<String> missingFiles = new ArrayList<String>();
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

    /**
     * Creates the folder for downloaded files to go into
     *
     * @param folder
     * @return bool
     */
    public static boolean createFolder(String folder) {
        Path p = Paths.get(folder);
        if (Files.exists(p)) {
            System.out.println(folder + " already exists.");
            int opt = JOptionPane.showConfirmDialog(null, folder + " already exists. Would you like to continue? ", "Music DL", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                copyReqs(folder);
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Operation cancelled.", "Music DL", JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
        } else {
            try {
                Files.createDirectories(p);
                copyReqs(folder);
                return true;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error when creating folder: " + e);
                throw new RuntimeException(e);
            }
        }
    }


    private static void copyReqs(String folder) {
        try {
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

    private Process process;

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

    private boolean cleanUpAfterRun(String folder) {
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

}

