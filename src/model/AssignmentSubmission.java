package model;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AssignmentSubmission {
    private String studentName;
    private String fileName;
    private String filePath;
    private String fileType;
    private String extractedText;
    private String submissionDate;
    private String status;
    private double similarityPercentage;

    public AssignmentSubmission() {
        this.submissionDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.status = "FIRST SUBMISSION";
    }

    public AssignmentSubmission(String studentName, String fileName, String filePath, String fileType, String extractedText) {
        this();
        this.studentName = studentName;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.extractedText = extractedText;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public String getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(String submissionDate) {
        this.submissionDate = submissionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getSimilarityPercentage() {
        return similarityPercentage;
    }

    public void setSimilarityPercentage(double similarityPercentage) {
        this.similarityPercentage = similarityPercentage;
    }

    public String getFileExtension() {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    public boolean hasValidFile() {
        return filePath != null && !filePath.isBlank() && new File(filePath).exists();
    }
}
