package algorithms;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CosineSimilarity {

    public static double similarity(Map<String, Double> vectorA, Map<String, Double> vectorB) {
        if (vectorA == null || vectorB == null) {
            return 0.0;
        }

        Set<String> terms = new HashSet<>();
        terms.addAll(vectorA.keySet());
        terms.addAll(vectorB.keySet());
        if (terms.isEmpty()) {
            return 0.0;
        }

        double numerator = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        for (String term : terms) {
            double a = vectorA.getOrDefault(term, 0.0);
            double b = vectorB.getOrDefault(term, 0.0);
            numerator += a * b;
            magnitudeA += a * a;
            magnitudeB += b * b;
        }

        if (magnitudeA == 0.0 || magnitudeB == 0.0) {
            return 0.0;
        }

        return numerator / (Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB));
    }
}
