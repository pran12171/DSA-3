package storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import model.AssignmentSubmission;

public class SubmissionRepository {
    public static final Path REPOSITORY_PATH = Paths.get("data", "submissions");

    public SubmissionRepository() {
        try {
            Files.createDirectories(REPOSITORY_PATH);
        } catch (IOException e) {
            throw new IllegalStateException("Storage directory could not be created: " + e.getMessage(), e);
        }
    }

    public List<AssignmentSubmission> loadPreviousSubmissions() {
        List<AssignmentSubmission> submissions = new ArrayList<>();
        if (!Files.exists(REPOSITORY_PATH)) {
            return submissions;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(REPOSITORY_PATH, "*.meta")) {
            for (Path metaFile : stream) {
                try {
                    Properties properties = new Properties();
                    try (InputStream input = Files.newInputStream(metaFile)) {
                        properties.load(input);
                    }

                    AssignmentSubmission submission = new AssignmentSubmission();
                    submission.setStudentName(properties.getProperty("studentName", "Unknown"));
                    submission.setFileName(properties.getProperty("fileName", metaFile.getFileName().toString()));
                    submission.setFilePath(properties.getProperty("filePath", metaFile.toString().replace(".meta", "")));
                    submission.setFileType(properties.getProperty("fileType", "TXT"));
                    submission.setExtractedText(properties.getProperty("extractedText", ""));
                    submission.setSubmissionDate(properties.getProperty("submissionDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                    submission.setStatus(properties.getProperty("status", "FIRST SUBMISSION"));
                    try {
                        submission.setSimilarityPercentage(Double.parseDouble(properties.getProperty("similarityPercentage", "0.0")));
                    } catch (NumberFormatException ignored) {
                        submission.setSimilarityPercentage(0.0);
                    }
                    submissions.add(submission);
                } catch (IOException ignored) {
                    // Skip unreadable files gracefully.
                }
            }
        } catch (IOException ignored) {
            return submissions;
        }

        submissions.sort((a, b) -> a.getSubmissionDate().compareTo(b.getSubmissionDate()));
        return submissions;
    }

    public boolean saveSubmission(AssignmentSubmission submission, Path sourceFile) {
        if (submission == null || sourceFile == null) {
            return false;
        }

        try {
            Files.createDirectories(REPOSITORY_PATH);
            String sanitizedName = sanitize(submission.getStudentName());
            String originalName = sourceFile.getFileName().toString();
            String targetFileName = sanitizedName + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "_" + originalName;
            Path targetFile = REPOSITORY_PATH.resolve(targetFileName);
            Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);

            submission.setFileName(originalName);
            submission.setFilePath(targetFile.toString());
            submission.setSubmissionDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            Properties properties = new Properties();
            properties.setProperty("studentName", submission.getStudentName());
            properties.setProperty("fileName", submission.getFileName());
            properties.setProperty("filePath", submission.getFilePath());
            properties.setProperty("fileType", submission.getFileType());
            properties.setProperty("extractedText", submission.getExtractedText() == null ? "" : submission.getExtractedText());
            properties.setProperty("submissionDate", submission.getSubmissionDate());
            properties.setProperty("status", submission.getStatus());
            properties.setProperty("similarityPercentage", String.valueOf(submission.getSimilarityPercentage()));

            Path metaFile = REPOSITORY_PATH.resolve(targetFileName + ".meta");
            try (OutputStream output = Files.newOutputStream(metaFile)) {
                properties.store(output, "Submission metadata");
            }

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isDuplicateSubmission(String studentName, String fileName) {
        List<AssignmentSubmission> submissions = loadPreviousSubmissions();
        for (AssignmentSubmission submission : submissions) {
            if (submission.getStudentName() != null && submission.getStudentName().equalsIgnoreCase(studentName)
                    && submission.getFileName() != null && submission.getFileName().equalsIgnoreCase(fileName)) {
                return true;
            }
        }
        return false;
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "student";
        }
        String cleaned = value.trim().replaceAll("[^a-zA-Z0-9._-]", "_");
        return cleaned.isBlank() ? "student" : cleaned;
    }
}
