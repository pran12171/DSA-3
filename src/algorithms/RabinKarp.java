package algorithms;

import java.util.ArrayList;
import java.util.List;

public class RabinKarp {
    private static final long MOD1 = 1_000_000_007L;
    private static final long MOD2 = 1_000_000_009L;
    private static final long BASE = 911382629L;

    public static List<Integer> findAllOccurrences(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty() || pattern.length() > text.length()) {
            return matches;
        }

        long patternHash1 = 0;
        long patternHash2 = 0;
        long textHash1 = 0;
        long textHash2 = 0;
        long basePow1 = 1;
        long basePow2 = 1;

        int m = pattern.length();
        int n = text.length();

        for (int i = 0; i < m - 1; i++) {
            basePow1 = (basePow1 * BASE) % MOD1;
            basePow2 = (basePow2 * BASE) % MOD2;
        }

        for (int i = 0; i < m; i++) {
            char ch = pattern.charAt(i);
            patternHash1 = (patternHash1 * BASE + ch) % MOD1;
            patternHash2 = (patternHash2 * BASE + ch) % MOD2;

            ch = text.charAt(i);
            textHash1 = (textHash1 * BASE + ch) % MOD1;
            textHash2 = (textHash2 * BASE + ch) % MOD2;
        }

        for (int i = 0; i <= n - m; i++) {
            if (textHash1 == patternHash1 && textHash2 == patternHash2 && text.substring(i, i + m).equals(pattern)) {
                matches.add(i);
            }

            if (i < n - m) {
                char outgoing = text.charAt(i);
                char incoming = text.charAt(i + m);
                textHash1 = (textHash1 - outgoing * basePow1) % MOD1;
                textHash2 = (textHash2 - outgoing * basePow2) % MOD2;
                textHash1 = (textHash1 * BASE + incoming) % MOD1;
                textHash2 = (textHash2 * BASE + incoming) % MOD2;
                if (textHash1 < 0) {
                    textHash1 += MOD1;
                }
                if (textHash2 < 0) {
                    textHash2 += MOD2;
                }
            }
        }

        return matches;
    }
}
