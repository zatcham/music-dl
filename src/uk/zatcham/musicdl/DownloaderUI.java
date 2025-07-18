// Main class (UI)

package uk.zatcham.musicdl;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;
// Java AWT must be imported one by one instead of * as otherwise awt.list is used
import java.io.IOException;
import java.util.prefs.*;
// UI (using flatLaF for theme)
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;

public class DownloaderUI extends JFrame {

    // Declare UI components, all as final
    private final JRadioButton soundcloudRadioButton;
    private final JRadioButton youtubeRadioButton;
    private final JTextField urlTextField;
    private final JTextField folderTextField;
    private final JTextField oauthTokenTextField;
    private final JTextArea consoleTextArea;
    private final JButton startButton;
    private final JButton stopButton;

    public DownloaderUI() {
        FlatLightLaf.setup(); // Required for Flatlaf UI
        FlatDarkLaf.setup();
        setTitle("Music DL");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));

        // Set LaF
        try {
            // Set the Nimbus Look and Feel
//            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            // Using preferences to store user settings
            Preferences userPrefs = Preferences.userNodeForPackage(DownloaderUI.class);
            if (userPrefs.getBoolean("darkMode", true)) {
                UIManager.setLookAndFeel( new FlatDarkLaf() );
            } else {
                UIManager.setLookAndFeel( new FlatLightLaf() );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Panel for radio buttons to select platform
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
        startButton.addActionListener(e -> startDownload());
        // Stop Button listener
        stopButton.addActionListener(e -> {
            if (process != null) {
                process.destroyForcibly(); // this doesnt work
            }
            try {
                Runtime.getRuntime().exec("taskkill /F /IM yt-dlp.exe"); // Exec is deprecated
                Helpers.cleanUpAfterRun(folderTextField.getText()); // usually doesnt get cleaned up at first
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            stopButton.setEnabled(false);
            startButton.setEnabled(true); // re-enable after run
        });


        // Radio button listener for OAuth token field.
        soundcloudRadioButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) { // selected
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

        JMenuItem folderOpenItem = new JMenuItem("Open Folder");
        folderOpenItem.addActionListener(e -> Helpers.openCurrentDir());
        menu.add(folderOpenItem);

        menu.addSeparator();

        JMenuItem infoItem = new JMenuItem("About");
        infoItem.addActionListener(e -> openInfo());
        menu.add(infoItem);

        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.addActionListener(e -> openSettings());
        menu.add(settingsItem);

        menu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> Helpers.exitProg());
        menu.add(exitItem);

        menuBar.add(menu);
        setJMenuBar(menuBar);

        // Add buttons to UI
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }


    /**
     * Opens Settings UI
     */
    private void openSettings() {
        System.out.println("Opening settings..");
        // Get settings
        Preferences userPrefs = Preferences.userNodeForPackage(DownloaderUI.class);
        boolean darkMode = userPrefs.getBoolean("darkMode", true);
        boolean keepAwake = userPrefs.getBoolean("keepAwake", false);
        String currentBrowser = userPrefs.get("browser", "edge");

        // Create UI
        JFrame frame = new JFrame("Settings - Music DL");
        frame.setSize(500, 250);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Browser selection
        String[] browsers = {"Edge", "Chrome", "Firefox"}; // List of browsers
        JLabel browserLabel = new JLabel("Browser: ");
        JComboBox<String> browserComboBox = new JComboBox<>(browsers);
        System.out.println("Current Browser: " + currentBrowser);
        browserComboBox.getModel().setSelectedItem(currentBrowser);
        browserComboBox.addActionListener(e -> {
            String b = (String) browserComboBox.getSelectedItem();
            userPrefs.put("browser", b);
            System.out.println("Setting " + b + " as browser");
        });

        // Dark mode toggle
        JLabel darkModeLabel = new JLabel("Dark Mode: ");
        JToggleButton darkModeToggle = new JToggleButton("Off");
        darkModeToggle.addActionListener(e -> {
            if (darkModeToggle.isSelected()) {
                darkModeToggle.setText("On");
                userPrefs.putBoolean("darkMode", true);
                JOptionPane.showMessageDialog(null, "Changes will apply on program restart", "Music DL", JOptionPane.INFORMATION_MESSAGE);
                System.out.println("Setting darkMode to true");
            } else {
                darkModeToggle.setText("Off");
                userPrefs.putBoolean("darkMode", false);
                JOptionPane.showMessageDialog(null, "Changes will apply on program restart", "Music DL", JOptionPane.INFORMATION_MESSAGE);
                System.out.println("Setting darkMode to false");
            }
        });
        if (darkMode) {
            darkModeToggle.setText("On");
            darkModeToggle.setSelected(true);
            System.out.println("Dark mode currently selected");
        }

        // Keep Awake toggle
        JLabel keepAwakeLabel = new JLabel("Keep PC Awake: ");
        JToggleButton keepAwakeToggle = new JToggleButton("Off");
        if (keepAwake) {
            keepAwakeToggle.setSelected(true);
            keepAwakeToggle.setText("On");
            System.out.println("Keep Awake selected");
        } else {
            keepAwakeToggle.setText("Off");
        }
        keepAwakeToggle.addActionListener(e -> {
            if (keepAwakeToggle.isSelected()) {
                userPrefs.putBoolean("keepAwake", true);
                System.out.println("Setting keepAwake to true");
                keepAwakeToggle.setText("On");
                JOptionPane.showMessageDialog(null, "PC will be kept awake whilst downloading", "Music DL", JOptionPane.INFORMATION_MESSAGE);
            } else {
                userPrefs.putBoolean("keepAwake", false);
                System.out.println("Setting keepAwake to false");
                keepAwakeToggle.setText("Off");
                JOptionPane.showMessageDialog(null, "PC will not be kept awake whilst downloading", "Music DL", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Add items to UI
        panel.add(browserLabel);
        panel.add(browserComboBox);
        panel.add(darkModeLabel);
        panel.add(darkModeToggle);
        panel.add(keepAwakeLabel);
        panel.add(keepAwakeToggle);
        // Finish UI
        frame.add(panel);
        frame.setVisible(true);
    }

    /**
     * Opens Info box
     */
    private void openInfo() {
        String programName = "yt-dlp UI";
        String version = "0.5";
        String creator = "Zach Matcham (zatcham)";
        String desc = "A simple UI to control download files with yt-dlp";
        String msg = String.format("Program Name: %s\nVersion: %s\nDeveloper: %s\n\n%s", programName, version, creator, desc);
        // Using msg box to show info
        JOptionPane.showMessageDialog(null, msg, "Music DL", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Start download using swing worker
     */
    private void startDownload() {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                // Retrieve user input
                String url = urlTextField.getText();
                String folderName = folderTextField.getText();
                String oauthToken = oauthTokenTextField.getText();

                // Determine the selected platform
                String platform;
                if (soundcloudRadioButton.isSelected()) {
                    platform = "SoundCloud";
                } else if (youtubeRadioButton.isSelected()) {
                    platform = "YouTube";
                } else {
                    JOptionPane.showMessageDialog(null, "Please select a platform.", "Error", JOptionPane.ERROR_MESSAGE);
                    return null;
                }

                // Validate fields and stop if empty
                if (urlTextField.getText().isEmpty()) {
                    // TODO : URL validation
                    JOptionPane.showMessageDialog(null, "Please enter a URL.", "Error", JOptionPane.ERROR_MESSAGE);
                    return null;
                } else if (folderTextField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter a folder name.", "Error", JOptionPane.ERROR_MESSAGE);
                    return null;
                } // no need to check oauth as can work  (lower quality tho)

                // Change button enabled
                startButton.setEnabled(false);
                stopButton.setEnabled(true);

                // Has user selected keep awake
                Preferences userPrefs = Preferences.userNodeForPackage(DownloaderUI.class);
                boolean sleepDisabled = false; // TODO
                if (userPrefs.getBoolean("keepAwake", false) && !sleepDisabled) {
                    KeepAwake.PowerManagement pm = KeepAwake.PowerManagement.INSTANCE;
                    sleepDisabled = true;
                    pm.preventSleep();
                }

                // Perform the download operation based on the selected platform
                consoleTextArea.append("-----\n");
                consoleTextArea.append("Platform: " + platform + "\n");
                consoleTextArea.append("URL: " + url + "\n");
                consoleTextArea.append("Folder Name: " + folderName + "\n");
                if (platform.equals("SoundCloud")) { // Only show Oauth token if sc selected
                    consoleTextArea.append("OAuth Token: " + oauthToken + "\n");
                }

                consoleTextArea.append("Creating folder\n");
                // Create folder to be used, check if successful
                if (Helpers.createFolder(folderName)) {
                    consoleTextArea.append("Starting yt-dlp...\n");
                    // Download
                    Downloader downloader = new Downloader(consoleTextArea);
                    downloader.startDownload(platform, folderName, url, "");
                    // Delete assets after run - TODO bug
                    consoleTextArea.append("Deleting assets from folder.. \n");
                    // Delete all assets after run
                    if (Helpers.cleanUpAfterRun(folderName)) {
                        consoleTextArea.append("Successfully deleted assets \n");
                        consoleTextArea.append("Download Complete \n");
                        consoleTextArea.append("----- \n");
                        stopButton.setEnabled(false);
                        startButton.setEnabled(true);
                        if (sleepDisabled) { // TODO
                            sleepDisabled = false;
                            KeepAwake.PowerManagement pm = KeepAwake.PowerManagement.INSTANCE;
                            pm.allowSleep();
                        }
                        
                    } else {
                        consoleTextArea.append("Error occured whilst deleting assets\n");
                        // Oh well -- if stop button used, try deleting there
                        consoleTextArea.append("Download Complete \n");
                        consoleTextArea.append("----- \n");
                        stopButton.setEnabled(false);
                        startButton.setEnabled(true);
                    }
                } else {
                    consoleTextArea.append("Error creating folder\n");
                }

                return null;
            }

            // Start process, worker is needed to stop UI from freezing
            @Override
            protected void process(List<String> chunks) {
                for (String line : chunks) {
                    consoleTextArea.append(line + "\n");
                }
            }
        };
        worker.execute();
    }

    /**
     * Main function, checks the pre-requisites then runs UI
     */
    public static void main(String[] args) throws IOException {
        // Do pre req checks
        // Check OS is Windows
        boolean isWin = System.getProperty("os.name").toLowerCase().startsWith("windows");
        if (!isWin) {
            JOptionPane.showMessageDialog(null, "This program will only run on Windows. Exiting..", "Music DL", JOptionPane.ERROR_MESSAGE);
            System.out.println("Error: Invalid OS. ");
            System.exit(0);
        }
        // Check if first run
        Preferences userPrefs = Preferences.userNodeForPackage(DownloaderUI.class);
        if (userPrefs.getBoolean("firstRun", true)) {
            userPrefs.putBoolean("firstRun", false);
            // TODO
//            userPrefs.put("downloaderArgs", "/yt-dlp.exe", "--extract-audio", "-f", "ba", "--embed-thumbnail", "--audio-quality", "0", "--audio-format", "mp3", "--add-metadata", "--cookies-from-browser", "edge");
            // "-o", folder + "/%(title)s.%(ext)s"
            userPrefs.put("browser", "edge");
        }
        // Check all assets exist
        Helpers.checkAssetsExist();

        // Run UI
        SwingUtilities.invokeLater(DownloaderUI::new);
    }

    private Process process;

}

