package algorithms;

import java.util.Arrays;

public class SuffixArray {

    public static int[] buildSuffixArray(String text) {
        if (text == null || text.isEmpty()) {
            return new int[0];
        }

        int n = text.length();
        Integer[] sa = new Integer[n];
        int[] rank = new int[n];
        int[] tmp = new int[n];

        for (int i = 0; i < n; i++) {
            sa[i] = i;
            rank[i] = text.charAt(i);
        }

        int k = 1;
        while (k < n) {
            final int finalK = k;
            Arrays.sort(sa, (a, b) -> {
                if (rank[a] != rank[b]) {
                    return Integer.compare(rank[a], rank[b]);
                }
                int ra = (a + finalK < n) ? rank[a + finalK] : -1;
                int rb = (b + finalK < n) ? rank[b + finalK] : -1;
                return Integer.compare(ra, rb);
            });

            tmp[sa[0]] = 0;
            for (int i = 1; i < n; i++) {
                int a = sa[i - 1];
                int b = sa[i];
                int rankA = rank[a];
                int rankB = rank[b];
                int nextA = (a + k < n) ? rank[a + k] : -1;
                int nextB = (b + k < n) ? rank[b + k] : -1;

                boolean same = rankA == rankB && nextA == nextB;
                tmp[b] = same ? tmp[a] : i;
            }

            System.arraycopy(tmp, 0, rank, 0, n);
            if (rank[sa[n - 1]] == n - 1) {
                break;
            }
            k <<= 1;
        }

        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = sa[i];
        }
        return result;
    }
}
