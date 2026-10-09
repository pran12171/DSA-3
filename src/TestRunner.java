import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import extraction.TextExtractor;
import model.AssignmentSubmission;
import similarity.SimilarityEngine;
import storage.SubmissionRepository;

public class TestRunner {
    public static void main(String[] args) throws Exception {
        runAllTests();
        runEndToEndWorkflow();
        System.out.println("All validation tests passed.");
    }

    private static void runAllTests() throws Exception {
        testNoPreviousDocuments();
        testIdenticalDocuments();
        testUnrelatedDocuments();
        testCopiedPhrasesWithModifications();
        testMultiplePreviousDocuments();
        testTxtExtraction();
        testPdfExtraction();
        testEmptyDocument();
        testInvalidPdf();
        testBoundaryThreshold();
        testSubmissionRepositoryStorage();
        testSessionIsolationWithPersistentHistory();
        testMixedTxtAndPdfReferenceSession();
        testGuiFreshStartupCounts();
        testGuiCheckSimilarityTxtWorkflow();
        testTextIndex();
        testTrieAndFuzzySearch();
        testNetworkFlowAlgorithms();
        testAdvancedAnalysisIntegration();
    }

    private static void testTextIndex() {
        algorithms.TextIndex index = new algorithms.TextIndex();
        index.addDocument("D1", "Algorithms and data structures");
        index.addDocument("D2", "Data structures support search");
        assertEquals(1, index.search("algorithms structures").size(), "Index should intersect query terms");
        assertEquals("D1", index.search("algorithms structures").get(0), "Index should return the matching document");
        assertTrue(index.search("").isEmpty(), "Empty query should return no matches");
    }

    private static void testTrieAndFuzzySearch() {
        algorithms.Trie trie = new algorithms.Trie();
        trie.insert("data");
        trie.insert("database");
        assertTrue(trie.contains("data"), "Trie should find inserted words");
        assertTrue(trie.startsWith("dat"), "Trie should support prefix checks");
        assertEquals(2, trie.wordsWithPrefix("dat").size(), "Trie should enumerate a prefix");
        algorithms.TextIndex index = new algorithms.TextIndex();
        index.addDocument("D1", "algorithm algorithms");
        assertTrue(index.fuzzySearch("algoritm", 1).contains("algorithm"), "Index should support fuzzy queries");
    }

    private static void testNetworkFlowAlgorithms() {
        algorithms.NetworkFlow.FlowNetwork network = new algorithms.NetworkFlow.FlowNetwork(4);
        network.addEdge(0, 1, 3);
        network.addEdge(0, 2, 2);
        network.addEdge(1, 2, 1);
        network.addEdge(1, 3, 2);
        network.addEdge(2, 3, 3);
        assertEquals(5, algorithms.NetworkFlow.fordFulkerson(network, 0, 3), "Ford-Fulkerson max flow");
        assertEquals(5, algorithms.NetworkFlow.edmondsKarp(network, 0, 3), "Edmonds-Karp max flow");
        assertEquals(5, algorithms.NetworkFlow.dinic(network, 0, 3), "Dinic max flow");
        assertEquals(5, algorithms.NetworkFlow.minCut(network, 0, 3).getMaxFlow(), "Min-cut max-flow value");
        assertEquals(2, algorithms.NetworkFlow.bipartiteMatching(2, 2, new int[][] {{0, 0}, {0, 1}, {1, 1}}), "Bipartite matching");

        algorithms.NetworkFlow.FlowNetwork costs = new algorithms.NetworkFlow.FlowNetwork(4);
        costs.addEdge(0, 1, 2, 1);
        costs.addEdge(1, 3, 2, 1);
        costs.addEdge(0, 2, 1, 2);
        costs.addEdge(2, 3, 1, 1);
        int[] minCost = algorithms.NetworkFlow.minCostMaxFlow(costs, 0, 3);
        assertEquals(3, minCost[0], "Min-cost max-flow amount");
        assertEquals(7, minCost[1], "Min-cost max-flow cost");

        algorithms.NetworkFlow.FlowNetwork disconnected = new algorithms.NetworkFlow.FlowNetwork(3);
        assertEquals(0, algorithms.NetworkFlow.edmondsKarp(disconnected, 0, 2), "Disconnected graph has zero flow");
    }

    private static void testAdvancedAnalysisIntegration() {
        AssignmentSubmission current = new AssignmentSubmission("Current", "current.txt", "", "TXT", "graph algorithms and document topics");
        AssignmentSubmission previous = new AssignmentSubmission("Reference", "reference.txt", "", "TXT", "graph algorithms support document topics");
        String report = similarity.AdvancedAnalysis.analyze(current, java.util.List.of(previous));
        assertTrue(report.contains("relationshipFlow="), "Advanced report should include network flow");
        assertTrue(report.contains("minCutSourceSide="), "Advanced report should include minimum cut");
    }

    private static void testGuiFreshStartupCounts() throws Exception {
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            gui.MainFrame frame = new gui.MainFrame();
            frame.dispose();
        });
    }

    private static void testGuiCheckSimilarityTxtWorkflow() throws Exception {
        Path txtPath = Paths.get("test-data", "JAVAARRAYS.txt").toAbsolutePath();
        if (!Files.exists(txtPath)) {
            Files.writeString(txtPath, "Java arrays are fundamental data structures that store elements of the same type.", StandardCharsets.UTF_8);
        }

        gui.MainFrame[] holder = new gui.MainFrame[1];
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            try {
                gui.MainFrame frame = new gui.MainFrame();
                holder[0] = frame;
                java.lang.reflect.Field nameField = gui.MainFrame.class.getDeclaredField("studentNameField");
                nameField.setAccessible(true);
                ((javax.swing.JTextField) nameField.get(frame)).setText("StudentTest");

                java.lang.reflect.Field pathField = gui.MainFrame.class.getDeclaredField("filePathField");
                pathField.setAccessible(true);
                ((javax.swing.JTextField) pathField.get(frame)).setText(txtPath.toString());

                java.lang.reflect.Method checkMethod = gui.MainFrame.class.getDeclaredMethod("checkSimilarity");
                checkMethod.setAccessible(true);
                checkMethod.invoke(frame);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Thread.sleep(600);

        javax.swing.SwingUtilities.invokeAndWait(() -> {
            try {
                java.lang.reflect.Field finalStatusVal = gui.MainFrame.class.getDeclaredField("finalStatusValue");
                finalStatusVal.setAccessible(true);
                javax.swing.JLabel statusLabel = (javax.swing.JLabel) finalStatusVal.get(holder[0]);
                assertEquals("FIRST SUBMISSION", statusLabel.getText(), "First TXT submission in GUI must produce FIRST SUBMISSION");
                holder[0].dispose();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void clearSubmissionDirectory() throws IOException {
        Path repoDir = SubmissionRepository.REPOSITORY_PATH;
        if (Files.exists(repoDir)) {
            try (java.util.stream.Stream<Path> paths = Files.walk(repoDir)) {
                paths.filter(path -> !path.equals(repoDir)).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    private static void runEndToEndWorkflow() throws Exception {
        clearSubmissionDirectory();
        Path testDataDir = Files.createDirectories(Paths.get("test-data"));

        String assignment1Text = "Data structures are important concepts in computer science. Arrays, linked lists, stacks and queues are commonly used to organize data efficiently.";
        String assignment2Text = "Data structures are important concepts in computer science. Arrays, linked lists, stacks and queues are commonly used to organize data efficiently.";
        String assignment3Text = "Data structures are very important concepts in computer science. Arrays, linked lists, stacks and queues are widely used to organize information efficiently.";
        String assignment4Text = "Photosynthesis is the biological process by which green plants convert light energy into chemical energy. Chlorophyll absorbs sunlight and helps produce glucose.";

        Path assignment1 = testDataDir.resolve("assignment1.txt");
        Path assignment2 = testDataDir.resolve("assignment2.txt");
        Path assignment3 = testDataDir.resolve("assignment3.txt");
        Path assignment4 = testDataDir.resolve("assignment4.txt");
        Files.writeString(assignment1, assignment1Text, StandardCharsets.UTF_8);
        Files.writeString(assignment2, assignment2Text, StandardCharsets.UTF_8);
        Files.writeString(assignment3, assignment3Text, StandardCharsets.UTF_8);
        Files.writeString(assignment4, assignment4Text, StandardCharsets.UTF_8);

        SubmissionRepository repository = new SubmissionRepository();

        AssignmentSubmission first = new AssignmentSubmission("Shaik", assignment1.getFileName().toString(), assignment1.toString(), "TXT", assignment1Text);
        List<AssignmentSubmission> firstPrevious = new ArrayList<>();
        SimilarityEngine.AnalysisResult firstResult = SimilarityEngine.analyzeSubmission(first, firstPrevious);
        assertEquals("FIRST SUBMISSION", firstResult.getFinalStatus(), "Test 1 should be FIRST SUBMISSION");
        assertEquals(0.0, firstResult.getSimilarityPercentage(), 0.0001, "Test 1 should report zero similarity");
        assertTrue(repository.saveSubmission(first, assignment1), "Test 1 should store first submission in repository");
        System.out.println("Test 1: " + firstResult.getSimilarityPercentage() + "% | " + firstResult.getFinalStatus() + " | previous=" + firstResult.getPreviousDocuments() + " | compared=" + firstResult.getFilesCompared());

        AssignmentSubmission second = new AssignmentSubmission("Shaik", assignment2.getFileName().toString(), assignment2.toString(), "TXT", assignment2Text);
        List<AssignmentSubmission> secondPrevious = List.of(first);
        SimilarityEngine.AnalysisResult secondResult = SimilarityEngine.analyzeSubmission(second, secondPrevious);
        assertTrue(secondResult.getPreviousDocuments() == 1, "Test 2 should have 1 reference document");
        assertTrue(secondResult.getSimilarityPercentage() > 10.0, "Test 2 should be detected as copied");
        assertEquals("COPIED", secondResult.getFinalStatus(), "Test 2 should be COPIED");
        assertTrue(repository.saveSubmission(second, assignment2), "Test 2 should save the second submission");
        System.out.println("Test 2: " + secondResult.getSimilarityPercentage() + "% | " + secondResult.getFinalStatus() + " | previous=" + secondResult.getPreviousDocuments() + " | compared=" + secondResult.getFilesCompared());

        AssignmentSubmission third = new AssignmentSubmission("Shaik", assignment3.getFileName().toString(), assignment3.toString(), "TXT", assignment3Text);
        List<AssignmentSubmission> thirdPrevious = List.of(first, second);
        SimilarityEngine.AnalysisResult thirdResult = SimilarityEngine.analyzeSubmission(third, thirdPrevious);
        assertTrue(thirdResult.getPreviousDocuments() == 2, "Test 3 should have 2 reference documents");
        assertTrue(thirdResult.getSimilarityPercentage() > 10.0, "Test 3 should retain substantial overlap");
        assertTrue(repository.saveSubmission(third, assignment3), "Test 3 should save the third submission");
        System.out.println("Test 3: " + thirdResult.getSimilarityPercentage() + "% | " + thirdResult.getFinalStatus() + " | previous=" + thirdResult.getPreviousDocuments() + " | compared=" + thirdResult.getFilesCompared());

        AssignmentSubmission fourth = new AssignmentSubmission("Shaik", assignment4.getFileName().toString(), assignment4.toString(), "TXT", assignment4Text);
        List<AssignmentSubmission> fourthPrevious = List.of(first, second, third);
        SimilarityEngine.AnalysisResult fourthResult = SimilarityEngine.analyzeSubmission(fourth, fourthPrevious);
        assertTrue(fourthResult.getPreviousDocuments() == 3, "Test 4 should have 3 reference documents");
        assertTrue(fourthResult.getSimilarityPercentage() < secondResult.getSimilarityPercentage(), "Test 4 should be lower than the identical-document test");
        assertTrue(repository.saveSubmission(fourth, assignment4), "Test 4 should save the fourth submission");
        System.out.println("Test 4: " + fourthResult.getSimilarityPercentage() + "% | " + fourthResult.getFinalStatus() + " | previous=" + fourthResult.getPreviousDocuments() + " | compared=" + fourthResult.getFilesCompared());

        Path pdfOutput = testDataDir.resolve("assignment5.pdf");
        createPdf(pdfOutput, assignment1Text);
        AssignmentSubmission fifth = new AssignmentSubmission("Shaik", pdfOutput.getFileName().toString(), pdfOutput.toString(), "PDF", assignment1Text);
        List<AssignmentSubmission> fifthPrevious = List.of(first, second, third, fourth);
        SimilarityEngine.AnalysisResult fifthResult = SimilarityEngine.analyzeSubmission(fifth, fifthPrevious);
        assertTrue(fifthResult.getPreviousDocuments() == 4, "Test 5 should have 4 reference documents");
        assertTrue(fifthResult.getSimilarityPercentage() > 10.0, "Test 5 PDF should show substantial similarity");
        assertTrue(repository.saveSubmission(fifth, pdfOutput), "Test 5 should save the PDF submission");
        System.out.println("Test 5: " + fifthResult.getSimilarityPercentage() + "% | " + fifthResult.getFinalStatus() + " | previous=" + fifthResult.getPreviousDocuments() + " | compared=" + fifthResult.getFilesCompared());
    }

    private static void testNoPreviousDocuments() {
        AssignmentSubmission current = new AssignmentSubmission("Shaik", "assignment_01.pdf", "C:/temp/assignment_01.pdf", "PDF",
                "The student studies data structures and algorithms.");
        SimilarityEngine.AnalysisResult result = SimilarityEngine.analyzeSubmission(current, new ArrayList<>());
        assertEquals("FIRST SUBMISSION", result.getFinalStatus(), "No previous documents should produce FIRST SUBMISSION");
        assertEquals(0.0, result.getSimilarityPercentage(), 0.0001, "First submission score must be 0.0");
    }

    private static void testIdenticalDocuments() {
        String text = "The student studies data structures and algorithms each week with careful practice.";
        AssignmentSubmission previous = new AssignmentSubmission("Shaik", "prev_same.txt", "C:/temp/prev_same.txt", "TXT", text);
        AssignmentSubmission current = new AssignmentSubmission("Shaik", "current_same.txt", "C:/temp/current_same.txt", "TXT", text);

        SimilarityEngine.AnalysisResult result = SimilarityEngine.analyzeSubmission(current, List.of(previous));
        assertTrue(result.getSimilarityPercentage() > 80.0, "Identical documents should have very high similarity");
        assertTrue(result.getFinalStatus().equals("COPIED") || result.getFinalStatus().equals("ORIGINAL"), "Status should be valid");
    }

    private static void testUnrelatedDocuments() {
        String previousText = "The moon orbits Earth and quantum physics explains subatomic motion in the laboratory.";
        String currentText = "The kitchen table is made of wood and the recipe uses tomatoes and basil for dinner.";
        AssignmentSubmission previous = new AssignmentSubmission("StudentA", "previous_unrelated.txt", "C:/temp/previous_unrelated.txt", "TXT", previousText);
        AssignmentSubmission current = new AssignmentSubmission("StudentA", "current_unrelated.txt", "C:/temp/current_unrelated.txt", "TXT", currentText);

        SimilarityEngine.AnalysisResult result = SimilarityEngine.analyzeSubmission(current, List.of(previous));
        assertTrue(result.getSimilarityPercentage() < 10.0, "Unrelated documents should be low similarity");
    }

    private static void testCopiedPhrasesWithModifications() {
        String previousText = "Data structures and algorithms are important for efficient software engineering and problem solving.";
        String currentText = "Data structures and algorithms are very important for effective software engineering and problem solving tasks.";
        AssignmentSubmission previous = new AssignmentSubmission("StudentB", "previous_modified.txt", "C:/temp/previous_modified.txt", "TXT", previousText);
        AssignmentSubmission current = new AssignmentSubmission("StudentB", "current_modified.txt", "C:/temp/current_modified.txt", "TXT", currentText);

        SimilarityEngine.AnalysisResult result = SimilarityEngine.analyzeSubmission(current, List.of(previous));
        assertTrue(result.getSimilarityPercentage() > 10.0, "Copied content with edits should increase document similarity");
    }

    private static void testMultiplePreviousDocuments() {
        List<AssignmentSubmission> previousDocs = new ArrayList<>();
        previousDocs.add(new AssignmentSubmission("StudentC", "doc1.txt", "C:/temp/doc1.txt", "TXT",
                "The student studies sorting algorithms and binary trees in data structures."));
        previousDocs.add(new AssignmentSubmission("StudentC", "doc2.txt", "C:/temp/doc2.txt", "TXT",
                "Graphs and heaps are learned in the data structures course."));
        previousDocs.add(new AssignmentSubmission("StudentC", "doc3.txt", "C:/temp/doc3.txt", "TXT",
                "The class discusses recursion and balanced trees for efficient coding."));

        AssignmentSubmission current = new AssignmentSubmission("StudentC", "current_multi.txt", "C:/temp/current_multi.txt", "TXT",
                "The student studies sorting algorithms and binary trees in data structures and also reviews graphs and heaps.");

        SimilarityEngine.AnalysisResult result = SimilarityEngine.analyzeSubmission(current, previousDocs);
        assertTrue(result.getPreviousDocuments() == 3, "All previous documents should be counted");
        assertTrue(result.getSimilarityPercentage() > 10.0, "Aggregate similarity across multiple previous documents should be computed");
    }

    private static void testTxtExtraction() throws IOException {
        Path tempFile = Files.createTempFile("assignment_txt_", ".txt");
        Files.writeString(tempFile, "This is a test assignment for text extraction.", StandardCharsets.UTF_8);
        String extracted = TextExtractor.extractText(tempFile);
        assertTrue(extracted.contains("test assignment"), "TXT extraction must return readable text");
        Files.deleteIfExists(tempFile);
    }

    private static void testPdfExtraction() throws Exception {
        Path tempFile = Files.createTempFile("assignment_pdf_", ".pdf");
        createPdf(tempFile, "This is a PDF assignment created for verification.");
        String extracted = TextExtractor.extractText(tempFile);
        assertTrue(extracted.contains("PDF assignment"), "PDF extraction must return readable text");
        Files.deleteIfExists(tempFile);
    }

    private static void testEmptyDocument() throws IOException {
        Path tempFile = Files.createTempFile("empty_assignment_", ".txt");
        Files.writeString(tempFile, "", StandardCharsets.UTF_8);
        try {
            TextExtractor.extractText(tempFile);
            throw new AssertionError("Empty document should be rejected");
        } catch (IOException expected) {
            // expected
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static void testInvalidPdf() throws IOException {
        Path tempFile = Files.createTempFile("corrupt_pdf_", ".pdf");
        Files.writeString(tempFile, "This is not a valid PDF file.", StandardCharsets.UTF_8);
        try {
            TextExtractor.extractText(tempFile);
            throw new AssertionError("Corrupt PDF should throw IOException");
        } catch (IOException expected) {
            // expected
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static void testBoundaryThreshold() {
        assertEquals("ORIGINAL", SimilarityEngine.classifySimilarity(10.00), "10.00 should be ORIGINAL");
        assertEquals("COPIED", SimilarityEngine.classifySimilarity(10.01), "10.01 should be COPIED");
    }

    private static void testSubmissionRepositoryStorage() throws IOException {
        SubmissionRepository repository = new SubmissionRepository();
        Path sourceFile = Files.createTempFile("repo_assignment_", ".txt");
        Files.writeString(sourceFile, "Repository storage verification text.", StandardCharsets.UTF_8);

        AssignmentSubmission submission = new AssignmentSubmission("Alice", sourceFile.getFileName().toString(), sourceFile.toString(), "TXT",
                "Repository storage verification text.");
        submission.setStatus("COPIED");
        submission.setSimilarityPercentage(16.50);

        assertTrue(repository.saveSubmission(submission, sourceFile), "Submission should be saved to storage");
        List<AssignmentSubmission> loaded = repository.loadPreviousSubmissions();
        assertTrue(loaded.stream().anyMatch(item -> item.getFileName().equals(sourceFile.getFileName().toString())),
                "Previously stored submission should be loadable");
        Files.deleteIfExists(sourceFile);
    }

    private static void testSessionIsolationWithPersistentHistory() throws IOException {
        SubmissionRepository repository = new SubmissionRepository();
        Path sourceFile = Files.createTempFile("history_doc_", ".txt");
        Files.writeString(sourceFile, "This is old stored history text in persistent storage.", StandardCharsets.UTF_8);
        AssignmentSubmission historical = new AssignmentSubmission("HistoricalStudent", sourceFile.getFileName().toString(), sourceFile.toString(), "TXT", "This is old stored history text in persistent storage.");
        repository.saveSubmission(historical, sourceFile);

        // A new session without reference files must have 0 comparison files and produce FIRST SUBMISSION
        AssignmentSubmission current = new AssignmentSubmission("CurrentStudent", "new_doc.txt", "C:/temp/new_doc.txt", "TXT", "This is old stored history text in persistent storage.");
        List<AssignmentSubmission> emptySessionReferences = new ArrayList<>();
        SimilarityEngine.AnalysisResult sessionResult = SimilarityEngine.analyzeSubmission(current, emptySessionReferences);
        assertEquals("FIRST SUBMISSION", sessionResult.getFinalStatus(), "Session with 0 reference documents must be FIRST SUBMISSION");
        assertEquals(0, sessionResult.getPreviousDocuments(), "Previous documents count must be 0 for empty session");
        assertEquals(0, sessionResult.getFilesCompared(), "Files compared count must be 0 for empty session");
        Files.deleteIfExists(sourceFile);
    }

    private static void testMixedTxtAndPdfReferenceSession() throws Exception {
        String sharedTopic = "Binary search trees maintain sorted order for logarithmic search time.";
        Path txtRef = Files.createTempFile("ref_txt_", ".txt");
        Files.writeString(txtRef, sharedTopic, StandardCharsets.UTF_8);

        Path pdfRef = Files.createTempFile("ref_pdf_", ".pdf");
        createPdf(pdfRef, "Balanced AVL trees and Red-Black trees prevent worst case height skew in BST.");

        String currentText = "Binary search trees maintain sorted order for logarithmic search time and AVL trees prevent height skew.";
        AssignmentSubmission current = new AssignmentSubmission("Alice", "current.txt", "C:/temp/current.txt", "TXT", currentText);

        List<AssignmentSubmission> sessionReferences = new ArrayList<>();
        sessionReferences.add(new AssignmentSubmission("Ref1", txtRef.getFileName().toString(), txtRef.toString(), "TXT", TextExtractor.extractText(txtRef)));
        sessionReferences.add(new AssignmentSubmission("Ref2", pdfRef.getFileName().toString(), pdfRef.toString(), "PDF", TextExtractor.extractText(pdfRef)));

        SimilarityEngine.AnalysisResult result = SimilarityEngine.analyzeSubmission(current, sessionReferences);
        assertEquals(2, result.getPreviousDocuments(), "Must compare against exactly 2 session reference documents");
        assertEquals(2, result.getFilesCompared(), "Files compared must be 2");
        assertTrue(result.getSimilarityPercentage() > 10.0, "Substantial overlap across mixed TXT and PDF should yield >10% similarity");
        assertEquals("COPIED", result.getFinalStatus(), "Status must be COPIED");

        Files.deleteIfExists(txtRef);
        Files.deleteIfExists(pdfRef);
    }

    private static void createPdf(Path filePath, String content) throws IOException {
        Files.deleteIfExists(filePath);
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                stream.setFont(font, 12);
                stream.beginText();
                stream.newLineAtOffset(100, 700);
                stream.showText(content);
                stream.endText();
            }
            document.save(filePath.toFile());
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(String expected, String actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " Expected: " + expected + " but got: " + actual);
        }
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " Expected: " + expected + " but got: " + actual);
        }
    }

    private static void assertEquals(double expected, double actual, double tolerance, String message) {
        if (Math.abs(expected - actual) > tolerance) {
            throw new AssertionError(message + " Expected: " + expected + " but got: " + actual);
        }
    }
}
