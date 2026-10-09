package similarity;

import algorithms.NetworkFlow;
import algorithms.TextIndex;
import model.AssignmentSubmission;
import preprocessing.TextPreprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AdvancedAnalysis {
    private AdvancedAnalysis() { }

    public static String analyze(AssignmentSubmission current, List<AssignmentSubmission> previous) {
        if (current == null || previous == null || previous.isEmpty()) return "Advanced analysis: no reference documents.";

        List<AssignmentSubmission> references = new ArrayList<>();
        for (AssignmentSubmission submission : previous) {
            if (submission != null && submission.getExtractedText() != null && !submission.getExtractedText().isBlank()) references.add(submission);
        }
        if (references.isEmpty()) return "Advanced analysis: no readable reference documents.";

        TextIndex index = new TextIndex();
        index.addDocument(current.getFileName(), current.getExtractedText());
        for (AssignmentSubmission reference : references) index.addDocument(reference.getFileName(), reference.getExtractedText());
        List<String> queryMatches = index.search(current.getExtractedText());

        List<Set<String>> tokenSets = new ArrayList<>();
        Set<String> currentTopics = new HashSet<>(TextPreprocessor.normalizeAndTokenize(current.getExtractedText()));
        for (AssignmentSubmission reference : references) tokenSets.add(new HashSet<>(TextPreprocessor.normalizeAndTokenize(reference.getExtractedText())));

        int currentNode = references.size();
        int sink = currentNode + 1;
        NetworkFlow.FlowNetwork graph = new NetworkFlow.FlowNetwork(references.size() + 2);
        Map<String, Set<String>> documentTopics = new HashMap<>();
        for (int i = 0; i < references.size(); i++) {
            Set<String> overlap = new HashSet<>(currentTopics);
            overlap.retainAll(tokenSets.get(i));
            int capacity = Math.max(1, overlap.size());
            graph.addEdge(currentNode, i, capacity);
            graph.addEdge(i, sink, capacity);
            documentTopics.put(references.get(i).getFileName(), tokenSets.get(i));
        }
        long flowStart = System.nanoTime();
        NetworkFlow.FlowResult cut = NetworkFlow.minCut(graph, currentNode, sink);
        long flowNanos = System.nanoTime() - flowStart;

        return String.format("Advanced analysis: indexMatches=%d, vocabulary=%d, relationshipFlow=%d, minCutSourceSide=%s, "
                + "relatedDocuments=%d, timingsNanos(flow=%d)",
                queryMatches.size(), index.vocabularySize(), cut.getMaxFlow(), cut.getSourceSideOfMinCut(),
            Math.max(0, cut.getSourceSideOfMinCut().size() - 1), flowNanos);
    }
}