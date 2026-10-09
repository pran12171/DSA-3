package algorithms;

public class LCPKasai {

    public static int[] buildLCP(String text, int[] suffixArray) {
        if (text == null || text.isEmpty() || suffixArray == null || suffixArray.length == 0) {
            return new int[0];
        }

        int n = text.length();
        int[] rank = new int[n];
        int[] lcp = new int[n];

        for (int i = 0; i < n; i++) {
            rank[suffixArray[i]] = i;
        }

        int h = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] > 0) {
                int j = suffixArray[rank[i] - 1];
                while (i + h < n && j + h < n && text.charAt(i + h) == text.charAt(j + h)) {
                    h++;
                }
                lcp[rank[i]] = h;
                if (h > 0) {
                    h--;
                }
            }
        }
        return lcp;
    }

    public static int longestCommonPrefix(String a, String b) {
        if (a == null || b == null) {
            return 0;
        }

        int max = 0;
        int n = Math.min(a.length(), b.length());
        for (int i = 0; i < n; i++) {
            if (a.charAt(i) == b.charAt(i)) {
                max++;
            } else {
                break;
            }
        }
        return max;
    }
}
