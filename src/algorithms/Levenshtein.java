package algorithms;

public class Levenshtein {

    public static int distance(String source, String target) {
        if (source == null || target == null) {
            return 0;
        }

        if (source.equals(target)) {
            return 0;
        }

        int rows = source.length() + 1;
        int cols = target.length() + 1;
        int[][] dp = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j < cols; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i < rows; i++) {
            for (int j = 1; j < cols; j++) {
                int cost = source.charAt(i - 1) == target.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[rows - 1][cols - 1];
    }
}
