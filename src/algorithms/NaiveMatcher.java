package algorithms;

import java.util.ArrayList;
import java.util.List;

public class NaiveMatcher {

    public static List<Integer> findAllOccurrences(String text, String pattern) {
        List<Integer> positions = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty()) {
            return positions;
        }

        int n = text.length();
        int m = pattern.length();
        for (int i = 0; i <= n - m; i++) {
            int j;
            for (j = 0; j < m; j++) {
                if (text.charAt(i + j) != pattern.charAt(j)) {
                    break;
                }
            }
            if (j == m) {
                positions.add(i);
            }
        }
        return positions;
    }

    public static int countOccurrences(String text, String pattern) {
        return findAllOccurrences(text, pattern).size();
    }
}
