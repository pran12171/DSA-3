package algorithms;

import java.util.ArrayList;
import java.util.List;

public class KMP {

    public static int[] buildLPS(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return new int[0];
        }

        int[] lps = new int[pattern.length()];
        int len = 0;
        int i = 1;

        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else if (len > 0) {
                len = lps[len - 1];
            } else {
                lps[i] = 0;
                i++;
            }
        }
        return lps;
    }

    public static List<Integer> findAllOccurrences(String text, String pattern) {
        List<Integer> positions = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty() || pattern.length() > text.length()) {
            return positions;
        }

        int[] lps = buildLPS(pattern);
        int i = 0;
        int j = 0;

        while (i < text.length()) {
            while (j > 0 && text.charAt(i) != pattern.charAt(j)) {
                j = lps[j - 1];
            }
            if (text.charAt(i) == pattern.charAt(j)) {
                j++;
            }
            if (j == pattern.length()) {
                positions.add(i - pattern.length() + 1);
                j = lps[j - 1];
            }
            i++;
        }

        return positions;
    }
}
