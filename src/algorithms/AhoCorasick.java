package algorithms;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class AhoCorasick {

    public static class Node {
        Map<Character, Node> children = new HashMap<>();
        Node fail;
        Node output;
        List<String> patterns = new ArrayList<>();

        Node() {
            this.fail = null;
            this.output = null;
        }
    }

    private final Node root;

    public AhoCorasick(List<String> patterns) {
        this.root = new Node();
        buildTrie(patterns);
        buildFailureLinks();
    }

    private void buildTrie(List<String> patterns) {
        if (patterns == null) {
            return;
        }

        for (String pattern : patterns) {
            if (pattern == null || pattern.isEmpty()) {
                continue;
            }
            Node current = root;
            for (char c : pattern.toCharArray()) {
                current.children.putIfAbsent(c, new Node());
                current = current.children.get(c);
            }
            current.patterns.add(pattern);
        }
    }

    private void buildFailureLinks() {
        Queue<Node> queue = new ArrayDeque<>();

        for (Node child : root.children.values()) {
            child.fail = root;
            queue.add(child);
        }

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            for (Map.Entry<Character, Node> entry : current.children.entrySet()) {
                char ch = entry.getKey();
                Node child = entry.getValue();
                Node fail = current.fail;

                while (fail != null && !fail.children.containsKey(ch)) {
                    fail = fail.fail;
                }

                child.fail = fail == null ? root : fail.children.get(ch);
                child.output = child.fail.patterns.isEmpty() ? child.fail.output : child.fail;
                queue.add(child);
            }
        }
    }

    public List<String> findPatterns(String text) {
        List<String> matches = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return matches;
        }

        Node current = root;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            while (current != root && !current.children.containsKey(ch)) {
                current = current.fail;
            }

            if (current.children.containsKey(ch)) {
                current = current.children.get(ch);
            } else {
                current = root;
            }

            Node output = current;
            while (output != null && !output.patterns.isEmpty()) {
                for (String pattern : output.patterns) {
                    matches.add(pattern);
                }
                output = output.output;
            }
        }
        return matches;
    }
}
