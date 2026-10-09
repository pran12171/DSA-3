package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import extraction.TextExtractor;
import model.AssignmentSubmission;
import similarity.SimilarityEngine;
import storage.SubmissionRepository;

public class MainFrame extends JFrame {
    private final JTextField studentNameField = new JTextField();
    private final JTextField filePathField = new JTextField();
    private final JComboBox<String> fileTypeCombo = new JComboBox<>(new String[] {"TXT", "PDF"});
    private final JTextArea resultArea = new JTextArea();

    // Session reference documents list
    private final DefaultListModel<Path> referenceFilesModel = new DefaultListModel<>();
    private final JList<Path> referenceFilesList = new JList<>(referenceFilesModel);
    private final JLabel referenceCountLabel = new JLabel("Reference Documents: 0");

    // Result panel value labels
    private final JLabel currentSubmissionValue = new JLabel("-");
    private final JLabel previousDocumentsValue = new JLabel("0");
    private final JLabel filesComparedValue = new JLabel("0");
    private final JLabel overallSimilarityValue = new JLabel("0.00%");
    private final JLabel thresholdValue = new JLabel("10.00%");
    private final JLabel finalStatusValue = new JLabel("FIRST SUBMISSION");

    private final SubmissionRepository repository = new SubmissionRepository();

    public MainFrame() {
        setTitle("STUDENT ASSIGNMENT SIMILARITY ANALYZER");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 780);
        setLocationRelativeTo(null);
        setResizable(true);
        setLayout(new BorderLayout(12, 12));

        JPanel titlePanel = buildTitlePanel();
        add(titlePanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(16, 16));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JPanel inputPanel = buildInputPanel();
        JPanel resultPanel = buildResultPanel();

        contentPanel.add(inputPanel, BorderLayout.WEST);
        contentPanel.add(resultPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        setMinimumSize(new Dimension(960, 680));
        setLocationRelativeTo(null);

        // Initial session state reset
        updateSessionCounts();
        clearResultDisplay();
    }

    private JPanel buildTitlePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(13, 71, 161));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel titleLabel = new JLabel("STUDENT ASSIGNMENT SIMILARITY ANALYZER", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("DSA-Based Plagiarism & Similarity Detection System", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitleLabel.setForeground(new Color(220, 230, 255));

        panel.setLayout(new BorderLayout(6, 6));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(subtitleLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildInputPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setPreferredSize(new Dimension(490, 600));
        container.setOpaque(false);

        // --- SECTION 1: Current Submission ---
        JPanel currentSubPanel = new JPanel(new GridBagLayout());
        currentSubPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(190, 200, 215)), "Current Submission"));
        currentSubPanel.setBackground(new Color(248, 250, 252));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Student Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.25;
        JLabel studentLabel = new JLabel("Student Name");
        studentLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        currentSubPanel.add(studentLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 0.75;
        studentNameField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        studentNameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        currentSubPanel.add(studentNameField, gbc);

        // Assignment File
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.25;
        JLabel fileLabel = new JLabel("Assignment File");
        fileLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        currentSubPanel.add(fileLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.50;
        filePathField.setEditable(false);
        filePathField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        filePathField.setToolTipText("Selected assignment file");
        filePathField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        currentSubPanel.add(filePathField, gbc);

        gbc.gridx = 2; gbc.weightx = 0.25;
        JButton chooseFileButton = new JButton("Choose File");
        chooseFileButton.setFocusPainted(false);
        chooseFileButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        chooseFileButton.addActionListener(e -> chooseCurrentFile());
        currentSubPanel.add(chooseFileButton, gbc);

        // File Type
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.25;
        JLabel typeLabel = new JLabel("File Type");
        typeLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        currentSubPanel.add(typeLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 0.75;
        fileTypeCombo.setEnabled(false);
        fileTypeCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        fileTypeCombo.setFocusable(false);
        currentSubPanel.add(fileTypeCombo, gbc);

        container.add(currentSubPanel);
        container.add(Box.createVerticalStrut(10));

        // --- SECTION 2: Reference / Previous Assignments (Current Session) ---
        JPanel refPanel = new JPanel(new BorderLayout(8, 8));
        refPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(190, 200, 215)), "Reference / Comparison Documents (Current Session)"));
        refPanel.setBackground(new Color(248, 250, 252));

        JPanel refHeaderPanel = new JPanel(new BorderLayout(6, 6));
        refHeaderPanel.setOpaque(false);
        refHeaderPanel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        referenceCountLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        referenceCountLabel.setForeground(new Color(30, 70, 120));
        refHeaderPanel.add(referenceCountLabel, BorderLayout.WEST);

        JPanel refButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        refButtonPanel.setOpaque(false);

        JButton addRefButton = new JButton("+ Add Reference Files");
        addRefButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        addRefButton.setBackground(new Color(30, 130, 60));
        addRefButton.setForeground(Color.WHITE);
        addRefButton.setFocusPainted(false);
        addRefButton.addActionListener(e -> addReferenceFiles());

        JButton removeRefButton = new JButton("Remove Selected");
        removeRefButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        removeRefButton.setFocusPainted(false);
        removeRefButton.addActionListener(e -> removeSelectedReferenceFiles());

        JButton clearRefButton = new JButton("Clear References");
        clearRefButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        clearRefButton.setFocusPainted(false);
        clearRefButton.addActionListener(e -> clearReferenceFiles());

        refButtonPanel.add(addRefButton);
        refButtonPanel.add(removeRefButton);
        refButtonPanel.add(clearRefButton);
        refHeaderPanel.add(refButtonPanel, BorderLayout.EAST);

        refPanel.add(refHeaderPanel, BorderLayout.NORTH);

        // List of Reference Documents
        referenceFilesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        referenceFilesList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        referenceFilesList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Path path) {
                    String fileName = path.getFileName().toString();
                    String ext = fileName.toLowerCase().endsWith(".pdf") ? "PDF" : "TXT";
                    label.setText(String.format(" %d. %-35s [%s]", index + 1, fileName, ext));
                    label.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
                }
                return label;
            }
        });

        JScrollPane refScrollPane = new JScrollPane(referenceFilesList);
        refScrollPane.setPreferredSize(new Dimension(460, 160));
        refScrollPane.setBorder(BorderFactory.createLineBorder(new Color(210, 218, 226)));
        refPanel.add(refScrollPane, BorderLayout.CENTER);

        container.add(refPanel);
        container.add(Box.createVerticalStrut(12));

        // --- SECTION 3: Action Buttons ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        actionPanel.setOpaque(false);

        JButton checkButton = new JButton("CHECK SIMILARITY");
        checkButton.setBackground(new Color(25, 118, 210));
        checkButton.setForeground(Color.WHITE);
        checkButton.setFocusPainted(false);
        checkButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        checkButton.addActionListener(e -> checkSimilarity());

        JButton clearButton = new JButton("CLEAR ALL");
        clearButton.setFocusPainted(false);
        clearButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        clearButton.addActionListener(e -> clearForm());

        JButton historyButton = new JButton("History");
        historyButton.setFocusPainted(false);
        historyButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        historyButton.addActionListener(e -> showHistory());

        JButton algorithmsButton = new JButton("Algorithms");
        algorithmsButton.setFocusPainted(false);
        algorithmsButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        algorithmsButton.addActionListener(e -> showAlgorithms());

        actionPanel.add(checkButton);
        actionPanel.add(clearButton);
        actionPanel.add(historyButton);
        actionPanel.add(algorithmsButton);
        container.add(actionPanel);

        return container;
    }

    private JPanel buildResultPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Analysis Result"));
        panel.setPreferredSize(new Dimension(560, 560));
        panel.setBackground(new Color(250, 251, 253));

        JPanel metricsPanel = new JPanel(new GridBagLayout());
        metricsPanel.setOpaque(false);
        metricsPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;

        JLabel currentLabel = new JLabel("Current Submission");
        currentLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 0; metricsPanel.add(currentLabel, gbc);
        currentSubmissionValue.setFont(new Font("SansSerif", Font.PLAIN, 14));
        currentSubmissionValue.setForeground(new Color(43, 62, 80));
        gbc.gridx = 1; metricsPanel.add(currentSubmissionValue, gbc);

        JLabel previousLabel = new JLabel("Reference Documents");
        previousLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 1; metricsPanel.add(previousLabel, gbc);
        previousDocumentsValue.setFont(new Font("SansSerif", Font.PLAIN, 14));
        previousDocumentsValue.setForeground(new Color(43, 62, 80));
        gbc.gridx = 1; metricsPanel.add(previousDocumentsValue, gbc);

        JLabel comparedLabel = new JLabel("Files Compared");
        comparedLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 2; metricsPanel.add(comparedLabel, gbc);
        filesComparedValue.setFont(new Font("SansSerif", Font.PLAIN, 14));
        filesComparedValue.setForeground(new Color(43, 62, 80));
        gbc.gridx = 1; metricsPanel.add(filesComparedValue, gbc);

        panel.add(metricsPanel);
        panel.add(Box.createVerticalStrut(8));

        JPanel divider = new JPanel();
        divider.setLayout(new BorderLayout());
        divider.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(205, 212, 219)));
        divider.setPreferredSize(new Dimension(10, 6));
        panel.add(divider);

        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
        scorePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        scorePanel.setOpaque(false);

        JLabel similarityHeader = new JLabel("OVERALL SIMILARITY");
        similarityHeader.setFont(new Font("SansSerif", Font.BOLD, 15));
        similarityHeader.setAlignmentX(CENTER_ALIGNMENT);
        scorePanel.add(similarityHeader);

        overallSimilarityValue.setFont(new Font("SansSerif", Font.BOLD, 30));
        overallSimilarityValue.setForeground(new Color(30, 96, 165));
        overallSimilarityValue.setAlignmentX(CENTER_ALIGNMENT);
        scorePanel.add(Box.createVerticalStrut(6));
        scorePanel.add(overallSimilarityValue);

        scorePanel.add(Box.createVerticalStrut(14));

        JLabel thresholdHeader = new JLabel("THRESHOLD");
        thresholdHeader.setFont(new Font("SansSerif", Font.BOLD, 13));
        thresholdHeader.setAlignmentX(CENTER_ALIGNMENT);
        scorePanel.add(thresholdHeader);

        thresholdValue.setFont(new Font("SansSerif", Font.BOLD, 17));
        thresholdValue.setAlignmentX(CENTER_ALIGNMENT);
        scorePanel.add(thresholdValue);

        scorePanel.add(Box.createVerticalStrut(14));

        JLabel finalStatusHeader = new JLabel("FINAL STATUS");
        finalStatusHeader.setFont(new Font("SansSerif", Font.BOLD, 13));
        finalStatusHeader.setAlignmentX(CENTER_ALIGNMENT);
        scorePanel.add(finalStatusHeader);

        finalStatusValue.setFont(new Font("SansSerif", Font.BOLD, 20));
        finalStatusValue.setAlignmentX(CENTER_ALIGNMENT);
        finalStatusValue.setOpaque(true);
        finalStatusValue.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 208, 216)),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)));
        finalStatusValue.setBackground(new Color(227, 239, 255));
        finalStatusValue.setForeground(new Color(20, 90, 150));
        scorePanel.add(finalStatusValue);

        panel.add(scorePanel);

        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setBackground(new Color(245, 247, 250));
        resultArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        resultArea.setText("Analysis ready.");
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);

        panel.add(new JScrollPane(resultArea));

        return panel;
    }

    private void chooseCurrentFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Assignment Files (*.txt, *.pdf)", "txt", "pdf"));
        chooser.setAcceptAllFileFilterUsed(false);

        int option = chooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            Path selected = chooser.getSelectedFile().toPath();
            String resolvedType = resolveFileType(selected);
            if (resolvedType == null) {
                JOptionPane.showMessageDialog(this, "Please select a TXT or PDF assignment.", "Unsupported File Type", JOptionPane.ERROR_MESSAGE);
                return;
            }
            filePathField.setText(selected.toString());
            filePathField.setToolTipText(selected.toString());
            fileTypeCombo.setSelectedItem(resolvedType);
        }
    }

    private void addReferenceFiles() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter("Assignment Files (*.txt, *.pdf)", "txt", "pdf"));
        chooser.setAcceptAllFileFilterUsed(false);

        int option = chooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            java.io.File[] selectedFiles = chooser.getSelectedFiles();
            if (selectedFiles == null || selectedFiles.length == 0) {
                if (chooser.getSelectedFile() != null) {
                    selectedFiles = new java.io.File[] { chooser.getSelectedFile() };
                }
            }
            if (selectedFiles != null) {
                int added = 0;
                for (java.io.File file : selectedFiles) {
                    Path path = file.toPath();
                    String resolved = resolveFileType(path);
                    if (resolved == null) {
                        JOptionPane.showMessageDialog(this, "Skipping unsupported file: " + path.getFileName(), "Unsupported File Type", JOptionPane.WARNING_MESSAGE);
                        continue;
                    }
                    if (!Files.exists(path) || !Files.isRegularFile(path)) {
                        continue;
                    }
                    // Avoid duplicate entries in list
                    boolean exists = false;
                    for (int i = 0; i < referenceFilesModel.size(); i++) {
                        if (referenceFilesModel.get(i).equals(path)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        referenceFilesModel.addElement(path);
                        added++;
                    }
                }
                updateSessionCounts();
            }
        }
    }

    private void removeSelectedReferenceFiles() {
        List<Path> selected = referenceFilesList.getSelectedValuesList();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select reference file(s) from the list to remove.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        for (Path path : selected) {
            referenceFilesModel.removeElement(path);
        }
        updateSessionCounts();
    }

    private void clearReferenceFiles() {
        referenceFilesModel.clear();
        updateSessionCounts();
    }

    private void updateSessionCounts() {
        int count = referenceFilesModel.size();
        referenceCountLabel.setText("Reference Documents: " + count);
        previousDocumentsValue.setText(String.valueOf(count));
        filesComparedValue.setText(String.valueOf(count));
    }

    private String resolveFileType(Path filePath) {
        if (filePath == null) {
            return null;
        }
        String fileName = filePath.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".txt")) {
            return "TXT";
        }
        if (fileName.endsWith(".pdf")) {
            return "PDF";
        }
        return null;
    }

    private void checkSimilarity() {
        String studentName = studentNameField.getText() == null ? "" : studentNameField.getText().trim();
        String filePathText = filePathField.getText() == null ? "" : filePathField.getText().trim();

        if (studentName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the student name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (filePathText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a TXT or PDF assignment.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Path filePath = Paths.get(filePathText);
        if (!Files.exists(filePath)) {
            JOptionPane.showMessageDialog(this, "The selected file does not exist.", "File Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String actualFileType = resolveFileType(filePath);
        if (actualFileType == null) {
            JOptionPane.showMessageDialog(this, "Please select a TXT or PDF assignment.", "Unsupported File Type", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedType = (String) fileTypeCombo.getSelectedItem();
        if (selectedType != null && !selectedType.equals(actualFileType)) {
            JOptionPane.showMessageDialog(this, "Please select a TXT or PDF assignment.", "Unsupported File Type", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Snapshot of current session reference files
        List<Path> sessionRefPaths = new ArrayList<>();
        for (int i = 0; i < referenceFilesModel.size(); i++) {
            sessionRefPaths.add(referenceFilesModel.get(i));
        }

        updateProgressText("Extracting assignment text...");
        currentSubmissionValue.setText(filePath.getFileName().toString());
        previousDocumentsValue.setText(String.valueOf(sessionRefPaths.size()));
        filesComparedValue.setText(String.valueOf(sessionRefPaths.size()));

        SwingWorker<SimilarityEngine.AnalysisResult, Void> worker = new SwingWorker<>() {
            @Override
            protected SimilarityEngine.AnalysisResult doInBackground() throws Exception {
                if (Files.size(filePath) == 0L) {
                    throw new IllegalArgumentException("Selected file is empty.");
                }

                String extractedText = TextExtractor.extractText(filePath);
                if (extractedText == null || extractedText.trim().isEmpty()) {
                    if (actualFileType.equals("PDF")) {
                        throw new IllegalArgumentException("Unable to extract text from PDF.");
                    }
                    throw new IllegalArgumentException("Selected file is empty.");
                }

                AssignmentSubmission current = new AssignmentSubmission(
                        studentName, filePath.getFileName().toString(), filePath.toString(), actualFileType, extractedText);

                // Build reference submissions strictly from current session documents
                List<AssignmentSubmission> sessionReferences = new ArrayList<>();
                for (Path refPath : sessionRefPaths) {
                    if (!Files.exists(refPath) || Files.size(refPath) == 0L) {
                        continue;
                    }
                    String refType = resolveFileType(refPath);
                    if (refType == null) {
                        continue;
                    }
                    try {
                        String refText = TextExtractor.extractText(refPath);
                        if (refText != null && !refText.trim().isEmpty()) {
                            sessionReferences.add(new AssignmentSubmission(
                                    "Reference", refPath.getFileName().toString(), refPath.toString(), refType, refText));
                        }
                    } catch (Exception ex) {
                        // Skip unreadable reference file gracefully
                    }
                }

                SimilarityEngine.AnalysisResult result;
                if (sessionReferences.isEmpty()) {
                    current.setStatus("FIRST SUBMISSION");
                    current.setSimilarityPercentage(0.0);
                    result = new SimilarityEngine.AnalysisResult(0.0, "FIRST SUBMISSION", 0, 0,
                            String.format("Current File: %s | Previous Docs: 0 | Files Compared: 0 | Similarity: 0.00%% | Status: FIRST SUBMISSION",
                                    filePath.getFileName().toString()));
                } else {
                    updateProgressText("Running DSA analysis on session documents...");
                    updateProgressText("Calculating TF-IDF...");
                    updateProgressText("Calculating cosine similarity...");
                    result = SimilarityEngine.analyzeSubmission(current, sessionReferences);
                }

                current.setStatus(result.getFinalStatus());
                current.setSimilarityPercentage(result.getSimilarityPercentage());

                // Persist current submission to storage for history records
                repository.saveSubmission(current, filePath);
                return result;
            }

            @Override
            protected void done() {
                try {
                    SimilarityEngine.AnalysisResult result = get();
                    updateResultDisplay(studentName, filePath.getFileName().toString(), result);
                    resultArea.setText(formatResultText(studentName, filePath.getFileName().toString(), result));
                } catch (Exception ex) {
                    String message = ex.getMessage() == null ? "" : ex.getMessage();
                    if (message.contains("PDF") || message.contains("extract") || message.contains("Unable to extract text from PDF")) {
                        JOptionPane.showMessageDialog(MainFrame.this, "Unable to extract text from PDF.", "PDF Error", JOptionPane.ERROR_MESSAGE);
                    } else if (message.contains("empty") || message.contains("empty file") || message.contains("Selected file is empty")) {
                        JOptionPane.showMessageDialog(MainFrame.this, "Selected file is empty.", "Empty File", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(MainFrame.this, message, "Extraction Error", JOptionPane.ERROR_MESSAGE);
                    }
                    clearResultDisplay();
                }
            }
        };

        worker.execute();
    }

    private String formatResultText(String studentName, String fileName, SimilarityEngine.AnalysisResult result) {
        if (result.getPreviousDocuments() == 0) {
            return String.format(
                    "==================================================\nSTUDENT ASSIGNMENT SIMILARITY ANALYZER\n==================================================\n\nStudent          : %s\nCurrent File     : %s\nPrevious Docs    : 0\nFiles Compared   : 0\n\n--------------------------------------------------\n\nOVERALL SIMILARITY : %.2f%%%nFINAL STATUS        : %s\n==================================================\n",
                        studentName, fileName, result.getSimilarityPercentage(), result.getFinalStatus());
        }

                return String.format(
                "==================================================\nSTUDENT ASSIGNMENT SIMILARITY ANALYZER\n==================================================\n\nStudent          : %s\nCurrent File     : %s\nPrevious Docs    : %d\nFiles Compared   : %d\n\n--------------------------------------------------\n\nOVERALL SIMILARITY : %.2f%%%nTHRESHOLD           : 10.00%%%nFINAL STATUS        : %s\n==================================================\n",
                    studentName, fileName, result.getPreviousDocuments(), result.getFilesCompared(), result.getSimilarityPercentage(), result.getFinalStatus())
                    + "\n" + result.getSummary();
    }

    private void updateProgressText(String message) {
        resultArea.setText(message);
    }

    private void updateResultDisplay(String studentName, String fileName, SimilarityEngine.AnalysisResult result) {
        currentSubmissionValue.setText(fileName);
        previousDocumentsValue.setText(String.valueOf(result.getPreviousDocuments()));
        filesComparedValue.setText(String.valueOf(result.getFilesCompared()));
        overallSimilarityValue.setText(String.format(Locale.US, "%.2f%%", result.getSimilarityPercentage()));
        thresholdValue.setText("10.00%");
        finalStatusValue.setText(result.getFinalStatus());

        if ("ORIGINAL".equalsIgnoreCase(result.getFinalStatus())) {
            finalStatusValue.setBackground(new Color(223, 240, 216));
            finalStatusValue.setForeground(new Color(25, 109, 62));
            finalStatusValue.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(155, 207, 168)),
                    BorderFactory.createEmptyBorder(7, 14, 7, 14)));
        } else if ("COPIED".equalsIgnoreCase(result.getFinalStatus())) {
            finalStatusValue.setBackground(new Color(242, 222, 222));
            finalStatusValue.setForeground(new Color(153, 0, 0));
            finalStatusValue.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(205, 150, 150)),
                    BorderFactory.createEmptyBorder(7, 14, 7, 14)));
        } else {
            finalStatusValue.setBackground(new Color(227, 239, 255));
            finalStatusValue.setForeground(new Color(20, 90, 150));
            finalStatusValue.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 208, 216)),
                    BorderFactory.createEmptyBorder(7, 14, 7, 14)));
        }
    }

    private void clearResultDisplay() {
        currentSubmissionValue.setText("-");
        previousDocumentsValue.setText(String.valueOf(referenceFilesModel.size()));
        filesComparedValue.setText(String.valueOf(referenceFilesModel.size()));
        overallSimilarityValue.setText("0.00%");
        thresholdValue.setText("10.00%");
        finalStatusValue.setText("FIRST SUBMISSION");
        finalStatusValue.setBackground(new Color(227, 239, 255));
        finalStatusValue.setForeground(new Color(20, 90, 150));
        finalStatusValue.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 208, 216)),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)));
        resultArea.setText("Analysis ready.");
    }

    private void clearForm() {
        studentNameField.setText("");
        filePathField.setText("");
        fileTypeCombo.setSelectedIndex(0);
        referenceFilesModel.clear();
        updateSessionCounts();
        clearResultDisplay();
    }

    private void showHistory() {
        List<AssignmentSubmission> submissions = repository.loadPreviousSubmissions();
        HistoryPanel.open(submissions);
    }

    private void showAlgorithms() {
        AlgorithmPanel.open();
    }
}
