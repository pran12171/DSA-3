package algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trie {
    private static class Node {
        private final Map<Character, Node> children = new HashMap<>();
        private boolean word;
    }

    private final Node root = new Node();
    private int wordCount;

    public void insert(String value) {
        if (value == null || value.isEmpty()) return;
        Node current = root;
        for (char character : value.toCharArray()) current = current.children.computeIfAbsent(character, ignored -> new Node());
        if (!current.word) {
            current.word = true;
            wordCount++;
        }
    }

    public boolean contains(String value) {
        Node node = find(value);
        return node != null && node.word;
    }

    public boolean startsWith(String prefix) { return find(prefix) != null; }

    public List<String> wordsWithPrefix(String prefix) {
        Node node = find(prefix);
        if (node == null) return new ArrayList<>();
        List<String> words = new ArrayList<>();
        collect(node, prefix == null ? "" : prefix, words);
        Collections.sort(words);
        return words;
    }

    public int size() { return wordCount; }

    private Node find(String value) {
        if (value == null) return null;
        Node current = root;
        for (char character : value.toCharArray()) {
            current = current.children.get(character);
            if (current == null) return null;
        }
        return current;
    }

    private void collect(Node node, String prefix, List<String> words) {
        if (node.word) words.add(prefix);
        for (Map.Entry<Character, Node> entry : node.children.entrySet()) collect(entry.getValue(), prefix + entry.getKey(), words);
    }
}