package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class AlgorithmPanel {
    public static void open() {
        JFrame frame = new JFrame("Algorithms Overview");
        frame.setSize(900, 560);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("Naive String Matching", "Purpose: Basic exact pattern search.\nComplexity: O(n * m)\nRole: Detects repeated exact phrases.");
        entries.put("KMP", "Purpose: Efficient exact pattern matching.\nComplexity: O(n + m)\nRole: Detects repeated/copied phrases efficiently.");
        entries.put("Z-Function", "Purpose: Pattern matching through prefix values.\nComplexity: O(n)\nRole: Measures repeated prefix overlap across text.");
        entries.put("Rabin-Karp", "Purpose: Hash-based pattern scanning.\nComplexity: O(n + m) average\nRole: Identifies suspicious duplicate blocks quickly.");
        entries.put("Aho-Corasick", "Purpose: Simultaneous multi-pattern matching.\nComplexity: O(n + totalMatches + m)\nRole: Finds multiple repeated phrases in one pass.");
        entries.put("Suffix Array", "Purpose: Organizes all suffixes for substring analysis.\nComplexity: O(n log n) or O(n log^2 n)\nRole: Reveals repeated document segments.");
        entries.put("LCP / Kasai", "Purpose: Measures longest common prefix.\nComplexity: O(n)\nRole: Highlights shared text sequences.");
        entries.put("Levenshtein / Wagner-Fischer", "Purpose: Edit distance between strings.\nComplexity: O(m * n)\nRole: Detects insertions, deletions, and replacements.");
        entries.put("Damerau-Levenshtein", "Purpose: Edit distance with adjacent transpositions.\nComplexity: O(m * n)\nRole: Captures copied text with small shifts or edits.");
        entries.put("TF-IDF", "Purpose: Weights meaningful terms by importance.\nComplexity: O(totalTokens)\nRole: Primary document similarity feature in this project.");
        entries.put("Cosine Similarity", "Purpose: Measures vector alignment between documents.\nComplexity: O(vocabularySize)\nRole: Produces the overall similarity percentage used in the result.");

        StringBuilder text = new StringBuilder();
        text.append("MODULE 2 — STRING ALGORITHMS\n\n");
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            if (entry.getKey().equals("TF-IDF") || entry.getKey().equals("Cosine Similarity") ||
                    entry.getKey().equals("Levenshtein / Wagner-Fischer") || entry.getKey().equals("Damerau-Levenshtein")) {
                continue;
            }
            text.append(entry.getKey()).append("\n");
            text.append(entry.getValue()).append("\n\n");
        }

        text.append("MODULE 3 — DYNAMIC PROGRAMMING\n\n");
        text.append("Levenshtein / Wagner-Fischer\n");
        text.append("Purpose: Edit distance between strings.\nComplexity: O(m * n)\nRole: Detects insertions, deletions, and replacements.\n\n");
        text.append("Damerau-Levenshtein\n");
        text.append("Purpose: Edit distance accounting for adjacent transpositions.\nComplexity: O(m * n)\nRole: Detects copied text with minor rearrangements.\n\n");

        text.append("DOCUMENT SIMILARITY\n\n");
        text.append("TF-IDF\n");
        text.append("Purpose: Weights meaningful terms by importance.\nComplexity: O(totalTokens)\nRole: Primary document similarity feature in this project.\n\n");
        text.append("Cosine Similarity\n");
        text.append("Purpose: Measures vector alignment between documents.\nComplexity: O(vocabularySize)\nRole: Produces the overall similarity percentage used in the result.\n");

        text.append("\nMODULE 4 - NETWORK FLOW\n\n");
        text.append("Document Relationship Network\n");
        text.append("Purpose: Models documents as nodes and token-overlap relationships as directed capacity edges.\n");
        text.append("Algorithms: Ford-Fulkerson, Edmonds-Karp, Dinic, maximum flow, minimum cut, and bipartite matching.\n");
        text.append("Role: Identifies connected reference documents and the source-side cut of the relationship network.\n");

        JTextArea area = new JTextArea(text.toString());
        area.setEditable(false);
        area.setLineWrap(false);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        area.setBackground(new Color(248, 250, 252));

        JScrollPane scrollPane = new JScrollPane(area);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        frame.add(panel);
        frame.setVisible(true);
    }
}
