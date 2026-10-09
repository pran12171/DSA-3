# STUDENT ASSIGNMENT SIMILARITY ANALYZER

## Project Title
STUDENT ASSIGNMENT SIMILARITY ANALYZER

## Project Description
This project is a Java Swing desktop application for detecting plagiarism or similarity between student assignments. It supports both TXT and PDF submissions, extracts text, preprocesses content, and then evaluates similarity using a document-level TF-IDF + Cosine Similarity metric. Supporting DSA algorithms are used as internal analysis techniques to measure repeated phrases, prefix matches, edit distance, and suspicious lexical overlap.

## Features
- Submit assignment as TXT or PDF
- Extract text from plain text and PDF documents
- Store previous submissions in a local data folder
- Evaluate current assignment against all previous submissions as one aggregate comparison
- Use TF-IDF + Cosine Similarity as the main similarity percentage
- Use supporting DSA algorithms internally for plagiarism analysis
- View previous submission history
- View algorithm reference documentation
- Show original/copied status relative to the 10% threshold
- Graceful error handling for invalid input and corrupt documents

## Technologies
- Java 17+
- Java Swing
- Apache PDFBox
- Manual DSA implementations without external string/DP libraries

## DSA Algorithms Used
- Naive String Matching
- KMP
- Z-Function
- Rabin-Karp (double hashing)
- Trie and inverted text index
- Aho-Corasick
- Suffix Array
- LCP / Kasai
- Levenshtein Distance
- Damerau-Levenshtein Distance
- TF-IDF
- Cosine Similarity
- Document relationship network: Ford-Fulkerson, Edmonds-Karp, Dinic, min-cut, and bipartite matching

## Algorithm Complexities
- Naive String Matching: O(n * m)
- KMP: O(n + m)
- Z-Function: O(n)
- Rabin-Karp: O(n + m) average with hashing; worst-case O(n * m)
- Aho-Corasick: O(n + totalMatches + m)
- Suffix Array: O(n log^2 n) practical rank-doubling implementation
- LCP / Kasai: O(n)
- Levenshtein: O(m * n)
- Damerau-Levenshtein: O(m * n)
- TF-IDF: O(totalTokens)
- Cosine Similarity: O(vocabularySize)
- Inverted index query: O(queryTerms + matching postings)
- Ford-Fulkerson: O(E * maxFlow) for integral capacities
- Edmonds-Karp: O(V * E^2)
- Dinic: O(V^2 * E) general bound

## System Architecture
The project follows a modular architecture:
- GUI layer for Swing forms and result panels
- Model layer for assignments and metadata
- Extraction layer for TXT/PDF reading
- Preprocessing layer for normalization and tokenization
- Similarity layer for analytical engine and threshold comparison
- Storage layer for local submission repository
- Algorithms package for all DSA implementations

## How Similarity is Calculated
The main metric is built from the complete set of previous assignments. The system builds a term-frequency / inverse-document-frequency vocabulary across the collection of previous documents plus the current document. It then creates an aggregate previous-document representation and computes cosine similarity between the current document vector and that aggregate vector. The resulting cosine value is converted to a percentage:

similarityPercentage = cosineSimilarity * 100

This is the displayed OVERALL SIMILARITY. The application does not show pairwise percentages as the primary result. Supporting algorithms are used internally to detect repeated phrases, suspicious patterns, edit operations, and textual overlap.

## 10% Threshold Rule
- OVERALL SIMILARITY <= 10% => ORIGINAL
- OVERALL SIMILARITY > 10% => COPIED

## Project Structure
```text
StudentAssignmentSimilarityAnalyzer/
├── src/
│   ├── Main.java
│   ├── algorithms/
│   │   ├── NaiveMatcher.java
│   │   ├── Trie.java
│   │   ├── TextIndex.java
│   │   ├── NetworkFlow.java
│   │   ├── KMP.java
│   │   ├── ZFunction.java
│   │   ├── RabinKarp.java
│   │   ├── AhoCorasick.java
│   │   ├── SuffixArray.java
│   │   ├── LCPKasai.java
│   │   ├── Levenshtein.java
│   │   ├── DamerauLevenshtein.java
│   │   ├── TFIDF.java
│   │   └── CosineSimilarity.java
│   ├── model/
│   │   └── AssignmentSubmission.java
│   ├── extraction/
│   │   └── TextExtractor.java
│   ├── preprocessing/
│   │   └── TextPreprocessor.java
│   ├── similarity/
│   │   └── SimilarityEngine.java
│   ├── storage/
│   │   └── SubmissionRepository.java
│   └── gui/
│       ├── MainFrame.java
│       ├── AnalysisPanel.java
│       ├── HistoryPanel.java
│       └── AlgorithmPanel.java
├── data/
│   └── submissions/
├── lib/
│   └── PDFBox JAR files if required
├── README.md
└── .gitignore
```

## How to Install Apache PDFBox
Download the PDFBox JAR files from the official Apache PDFBox website or Maven Central and place them in the lib folder.

Example:
```bash
curl -L -o lib/pdfbox-app-3.0.2.jar https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox-app/3.0.2/pdfbox-app-3.0.2.jar
```

## How to Compile
From the project root:
```bash
javac -cp "lib/*" -d out $(find src -name "*.java")
```

## How to Run
```bash
java -cp "out:lib/*" Main
```

## Example Output
```text
==================================================
STUDENT ASSIGNMENT SIMILARITY ANALYZER
==================================================

Student          : Shaik
Current File     : Assignment_11.pdf
Previous Docs    : 10
Files Compared   : 10

--------------------------------------------------

OVERALL SIMILARITY : 16.73%
THRESHOLD           : 10.00%
FINAL STATUS        : COPIED

==================================================
```

## Future Enhancements
- Add database-backed storage
- Add multi-file bulk comparison
- Add visual similarity heatmaps
- Add export of reports to PDF/CSV
- Support more document types
- Improve algorithm tuning and threshold analysis

---
Built for DSA-based plagiarism and similarity detection in Java desktop environments.
