package algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TFIDF {

    public static class DocumentVector {
        private final List<String> terms;
        private final Map<String, Double> tfidfVector;

        public DocumentVector(List<String> terms, Map<String, Double> tfidfVector) {
            this.terms = new ArrayList<>(terms);
            this.tfidfVector = new HashMap<>(tfidfVector);
        }

        public List<String> getTerms() {
            return Collections.unmodifiableList(terms);
        }

        public Map<String, Double> getTfidfVector() {
            return Collections.unmodifiableMap(tfidfVector);
        }
    }

    public static DocumentVector computeDocumentVector(List<String> documentTerms, Map<String, Integer> documentFrequency, int totalDocuments) {
        Map<String, Double> tfidf = new HashMap<>();
        Map<String, Integer> termCounts = new HashMap<>();

        for (String term : documentTerms) {
            if (term == null || term.isBlank()) {
                continue;
            }
            termCounts.merge(term, 1, Integer::sum);
        }

        double docLength = termCounts.size() == 0 ? 1.0 : termCounts.values().stream().mapToDouble(Integer::doubleValue).sum();
        for (Map.Entry<String, Integer> entry : termCounts.entrySet()) {
            String term = entry.getKey();
            int tf = entry.getValue();
            int df = documentFrequency.getOrDefault(term, 0);
            double idf = Math.log((totalDocuments + 1.0) / (df + 1.0)) + 1.0;
            double tfValue = tf / docLength;
            tfidf.put(term, tfValue * idf);
        }

        return new DocumentVector(documentTerms, tfidf);
    }

    public static Map<String, Integer> buildDocumentFrequency(List<List<String>> documents) {
        Map<String, Integer> df = new HashMap<>();
        for (List<String> document : documents) {
            if (document == null) {
                continue;
            }
            Set<String> uniqueTerms = new HashSet<>();
            for (String term : document) {
                if (term == null || term.isBlank()) {
                    continue;
                }
                uniqueTerms.add(term);
            }
            for (String term : uniqueTerms) {
                df.merge(term, 1, Integer::sum);
            }
        }
        return df;
    }
}
