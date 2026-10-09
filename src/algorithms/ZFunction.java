package algorithms;

import java.util.ArrayList;
import java.util.List;

public class ZFunction {

    public static int[] buildZ(String text) {
        if (text == null || text.isEmpty()) {
            return new int[0];
        }

        int[] z = new int[text.length()];
        int left = 0;
        int right = 0;

        for (int i = 1; i < text.length(); i++) {
            if (i <= right) {
                z[i] = Math.min(right - i + 1, z[i - left]);
            }
            while (i + z[i] < text.length() && text.charAt(z[i]) == text.charAt(i + z[i])) {
                z[i]++;
            }
            if (i + z[i] - 1 > right) {
                left = i;
                right = i + z[i] - 1;
            }
        }
        return z;
    }

    public static List<Integer> findAllOccurrences(String text, String pattern) {
        List<Integer> positions = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty()) {
            return positions;
        }

        String combined = pattern + "#" + text;
        int[] z = buildZ(combined);
        for (int i = 0; i < z.length; i++) {
            if (z[i] >= pattern.length()) {
                positions.add(i - pattern.length() - 1);
            }
        }
        return positions;
    }
}
