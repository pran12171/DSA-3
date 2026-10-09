package algorithms;

import preprocessing.TextPreprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TextIndex {
    private final Map<String, Set<String>> postings = new HashMap<>();
    private final Trie vocabulary = new Trie();

    public void addDocument(String documentId, String text) {
        if (documentId == null || documentId.isBlank()) return;
        for (String token : TextPreprocessor.normalizeAndTokenize(text)) {
            vocabulary.insert(token);
            postings.computeIfAbsent(token, ignored -> new HashSet<>()).add(documentId);
        }
    }

    public List<String> search(String query) {
        List<String> terms = TextPreprocessor.normalizeAndTokenize(query);
        if (terms.isEmpty()) return new ArrayList<>();
        Set<String> matches = new HashSet<>(postings.getOrDefault(terms.get(0), Set.of()));
        for (int i = 1; i < terms.size(); i++) {
            matches.retainAll(postings.getOrDefault(terms.get(i), Set.of()));
        }
        List<String> result = new ArrayList<>(matches);
        result.sort(String::compareTo);
        return result;
    }

    public int vocabularySize() { return postings.size(); }

    public List<String> fuzzySearch(String query, int maxDistance) {
        if (query == null || query.isBlank() || maxDistance < 0) return new ArrayList<>();
        List<String> matches = new ArrayList<>();
        for (String word : vocabulary.wordsWithPrefix("") ) {
            if (Levenshtein.distance(query.toLowerCase(), word) <= maxDistance) matches.add(word);
        }
        return matches;
    }
}