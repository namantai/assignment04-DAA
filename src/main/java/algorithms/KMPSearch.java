package algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class KMPSearch implements StringSearch {

        public int[] lps(String p) {
        Objects.requireNonNull(p, "p");
        if (p.isEmpty()) throw new IllegalArgumentException("Pattern must not be empty");
        return buildLps(p);
    }

    private int[] buildLps(String p) {
        int[] prefix = new int[p.length()];
        int length = 0;
        int i = 1;
        while (i < p.length()) {
            if (p.charAt(i) == p.charAt(length)) {
                prefix[i++] = ++length;
            } else if (length > 0) {
                length = prefix[length - 1];
            } else {
                prefix[i++] = 0;
            }
        }
        return prefix;
    }

    @Override
    public List<Integer> search(String text, String pattern) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(pattern, "pattern");
        if (pattern.isEmpty()) throw new IllegalArgumentException("Pattern must not be empty");
        List<Integer> matches = new ArrayList<>();
        if (pattern.length() > text.length()) return matches;
        int[] prefix = buildLps(pattern);
        int i = 0;
        int j = 0;
        while (i < text.length()) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == pattern.length()) {
                    matches.add(i - j);
                    j = prefix[j - 1];
                }
            } else if (j > 0) {
                j = prefix[j - 1];
            } else {
                i++;
            }
        }
        return matches;
    }

}
