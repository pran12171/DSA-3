package preprocessing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TextPreprocessor {
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
            "has", "he", "her", "his", "i", "in", "is", "it", "its", "of", "on",
            "or", "that", "the", "their", "there", "they", "this", "to", "was",
            "were", "will", "with", "you", "your", "we", "our", "about", "after",
            "before", "into", "than", "then", "them", "these", "those", "through",
            "under", "up", "very", "when", "where", "while", "who", "why", "how",
            "which", "what", "some", "more", "most", "other", "over", "again", "against",
            "between", "because", "cannot", "could", "should", "would", "have", "had",
            "does", "did", "done", "each", "few", "many", "much", "not", "no", "nor",
            "only", "same", "such", "than", "too", "used", "using", "also"));

    private final String originalText;
    private final String normalizedText;
    private final List<String> tokens;

    public TextPreprocessor(String originalText) {
        this.originalText = originalText == null ? "" : originalText;
        this.normalizedText = normalize(this.originalText);
        this.tokens = tokenize(this.normalizedText);
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }

        String lower = text.toLowerCase();
        lower = lower.replaceAll("[\\p{Punct}]+", " ");
        lower = lower.replaceAll("\\s+", " ").trim();
        return lower;
    }

    public static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        String[] parts = text.split("\\s+");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            String token = part.trim();
            if (token.isBlank()) {
                continue;
            }
            if (STOP_WORDS.contains(token)) {
                continue;
            }
            result.add(token);
        }
        return result;
    }

    public static String[] splitCharacterWords(String text) {
        if (text == null || text.isBlank()) {
            return new String[0];
        }
        return normalize(text).split(" ");
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getNormalizedText() {
        return normalizedText;
    }

    public List<String> getTokens() {
        return new ArrayList<>(tokens);
    }

    public static String cleanForCharacters(String text) {
        if (text == null) {
            return "";
        }
        return normalize(text).replace(" ", "");
    }

    public static List<String> normalizeAndTokenize(String text) {
        return tokenize(normalize(text));
    }
}
