package similarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import algorithms.AhoCorasick;
import algorithms.CosineSimilarity;
import algorithms.DamerauLevenshtein;
import algorithms.KMP;
import algorithms.LCPKasai;
import algorithms.Levenshtein;
import algorithms.NaiveMatcher;
import algorithms.RabinKarp;
import algorithms.SuffixArray;
import algorithms.TFIDF;
import algorithms.ZFunction;
import model.AssignmentSubmission;
import preprocessing.TextPreprocessor;

public class SimilarityEngine {
    private static final double THRESHOLD = 10.0;

    public static String classifySimilarity(double similarityPercentage) {
        return similarityPercentage <= THRESHOLD ? "ORIGINAL" : "COPIED";
    }

    public static class AnalysisResult {
        private final double similarityPercentage;
        private final String finalStatus;
        private final int previousDocuments;
        private final int filesCompared;
        private final String summary;

        public AnalysisResult(double similarityPercentage, String finalStatus, int previousDocuments, int filesCompared,
                             String summary) {
            this.similarityPercentage = similarityPercentage;
            this.finalStatus = finalStatus;
            this.previousDocuments = previousDocuments;
            this.filesCompared = filesCompared;
            this.summary = summary;
        }

        public double getSimilarityPercentage() {
            return similarityPercentage;
        }

        public String getFinalStatus() {
            return finalStatus;
        }

        public int getPreviousDocuments() {
            return previousDocuments;
        }

        public int getFilesCompared() {
            return filesCompared;
        }

        public String getSummary() {
            return summary;
        }
    }

    public static AnalysisResult analyzeSubmission(AssignmentSubmission current, List<AssignmentSubmission> previousSubmissions) {
        if (current == null) {
            return new AnalysisResult(0.0, "FIRST SUBMISSION", 0, 0, "No current submission available.");
        }

        if (previousSubmissions == null || previousSubmissions.isEmpty()) {
            current.setStatus("FIRST SUBMISSION");
            current.setSimilarityPercentage(0.0);
            return new AnalysisResult(0.0, "FIRST SUBMISSION", 0, 0,
                    "No previous submissions available for comparison.");
        }

        String currentText = current.getExtractedText() == null ? "" : current.getExtractedText();
        TextPreprocessor currentProcessor = new TextPreprocessor(currentText);
        List<String> currentTokens = currentProcessor.getTokens();

        List<List<String>> documentTokens = new ArrayList<>();
        for (AssignmentSubmission previous : previousSubmissions) {
            if (previous == null || previous.getExtractedText() == null || previous.getExtractedText().isBlank()) {
                continue;
            }
            documentTokens.add(new TextPreprocessor(previous.getExtractedText()).getTokens());
        }
        if (!currentTokens.isEmpty()) {
            documentTokens.add(currentTokens);
        }

        Map<String, Integer> docFrequency = TFIDF.buildDocumentFrequency(documentTokens);
        Map<String, Double> currentVector = TFIDF.computeDocumentVector(currentTokens, docFrequency, documentTokens.size()).getTfidfVector();

        Map<String, Double> aggregatePreviousVector = new HashMap<>();
        for (List<String> previousTokens : documentTokens.subList(0, documentTokens.size() - 1)) {
            Map<String, Double> vector = TFIDF.computeDocumentVector(previousTokens, docFrequency, documentTokens.size()).getTfidfVector();
            for (Map.Entry<String, Double> entry : vector.entrySet()) {
                aggregatePreviousVector.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
        }

        double cosine = CosineSimilarity.similarity(currentVector, aggregatePreviousVector);
        double similarity = Math.max(0.0, Math.min(100.0, cosine * 100.0));
        String status = classifySimilarity(similarity);

        invokeSupportAlgorithms(currentText, previousSubmissions);
        String advancedReport = AdvancedAnalysis.analyze(current, previousSubmissions);

        current.setStatus(status);
        current.setSimilarityPercentage(similarity);

        String summary;
        if (previousSubmissions.isEmpty()) {
            summary = String.format(
                    "STUDENT ASSIGNMENT SIMILARITY ANALYZER%n" +
                            "Student          : %s%n" +
                            "Current File     : %s%n" +
                            "Previous Docs    : %d%n" +
                            "Files Compared   : %d%n" +
                            "--------------------------------------------------%n" +
                            "OVERALL SIMILARITY : %.2f%%%n" +
                            "FINAL STATUS        : %s",
                    current.getStudentName(), current.getFileName(), 0, 0,
                    0.0, "FIRST SUBMISSION");
        } else {
            summary = String.format(
                    "STUDENT ASSIGNMENT SIMILARITY ANALYZER%n" +
                            "Student          : %s%n" +
                            "Current File     : %s%n" +
                            "Previous Docs    : %d%n" +
                            "Files Compared   : %d%n" +
                            "--------------------------------------------------%n" +
                            "OVERALL SIMILARITY : %.2f%%%n" +
                            "THRESHOLD           : %.2f%%%n" +
                            "FINAL STATUS        : %s",
                    current.getStudentName(), current.getFileName(), previousSubmissions.size(), previousSubmissions.size(),
                    similarity, THRESHOLD, status) + System.lineSeparator() + advancedReport;
        }

        return new AnalysisResult(similarity, status, previousSubmissions.size(), previousSubmissions.size(), summary);
    }

    private static void invokeSupportAlgorithms(String currentText, List<AssignmentSubmission> previousSubmissions) {
        if (currentText == null || currentText.isBlank()) {
            return;
        }

        StringBuilder previousText = new StringBuilder();
        for (AssignmentSubmission submission : previousSubmissions) {
            if (submission != null && submission.getExtractedText() != null) {
                previousText.append(submission.getExtractedText()).append(" ");
            }
        }

        String pattern = "student";
        if (previousText.length() > 0) {
            pattern = previousText.toString().length() > 6 ? previousText.toString().substring(0, Math.min(6, previousText.length())) : previousText.toString();
        }

        NaiveMatcher.countOccurrences(currentText, pattern);
        KMP.findAllOccurrences(currentText, pattern);
        ZFunction.findAllOccurrences(currentText, pattern);
        RabinKarp.findAllOccurrences(currentText, pattern);

        List<String> phrases = new ArrayList<>();
        for (String token : currentText.split("\\s+")) {
            if (token.length() >= 4) {
                phrases.add(token);
            }
        }
        if (!phrases.isEmpty()) {
            AhoCorasick ac = new AhoCorasick(phrases);
            ac.findPatterns(currentText);
        }

        int[] suffixArray = SuffixArray.buildSuffixArray(currentText);
        LCPKasai.buildLCP(currentText, suffixArray);

        String compareText = previousText.length() > 0 ? previousText.toString() : currentText;
        Levenshtein.distance(currentText, compareText);
        DamerauLevenshtein.distance(currentText, compareText);
    }
}
