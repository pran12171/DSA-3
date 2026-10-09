package algorithms;

import java.util.HashMap;
import java.util.Map;

public class DamerauLevenshtein {

    public static int distance(String source, String target) {
        if (source == null || target == null) {
            return 0;
        }

        if (source.equals(target)) {
            return 0;
        }

        if (source.length() == 0) {
            return target.length();
        }
        if (target.length() == 0) {
            return source.length();
        }

        int maxDist = source.length() + target.length();
        Map<Character, Integer> lastRow = new HashMap<>();
        Map<Character, Integer> lastCol = new HashMap<>();

        int[][] dp = new int[source.length() + 2][target.length() + 2];
        dp[0][0] = maxDist;

        for (int i = 0; i <= source.length(); i++) {
            dp[i + 1][0] = maxDist;
            dp[i + 1][1] = i;
        }
        for (int j = 0; j <= target.length(); j++) {
            dp[0][j + 1] = maxDist;
            dp[1][j + 1] = j;
        }

        for (int i = 1; i <= source.length(); i++) {
            int db = 0;
            for (int j = 1; j <= target.length(); j++) {
                int i1 = lastRow.getOrDefault(target.charAt(j - 1), 0);
                int j1 = db;

                if (source.charAt(i - 1) == target.charAt(j - 1)) {
                    dp[i + 1][j + 1] = dp[i][j];
                    db = j;
                } else {
                    dp[i + 1][j + 1] = Math.min(
                            dp[i][j] + 1,
                            Math.min(dp[i + 1][j] + 1, Math.min(dp[i][j + 1] + 1,
                                    dp[i1][j1] + (i - i1 - 1) + 1 + (j - j1 - 1)))
                    );
                }

                lastRow.put(target.charAt(j - 1), i);
                lastCol.put(source.charAt(i - 1), j);
            }
        }

        return dp[source.length() + 1][target.length() + 1];
    }
}
