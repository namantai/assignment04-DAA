package algorithms;

import java.util.Objects;

public final class TwoRowsEditDistance implements EditDistance {

    // State: previous[j] = d(i-1,j), current[j] = d(i,j) for the current row i.
    // Transition: min(previous[j]+1, current[j-1]+1, previous[j-1]+substitution cost); swap rows.
    // Base case: previous[j] = j for i = 0; current[0] = i on each new row.
    @Override
    public int distance(String a, String b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        int m = b.length();
        int[] previous = new int[m + 1];
        int[] current = new int[m + 1];
        for (int j = 0; j <= m; j++) previous[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= m; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(previous[j] + 1,
                        Math.min(current[j - 1] + 1, previous[j - 1] + cost));
            }
            int[] temporary = previous;
            previous = current;
            current = temporary;
        }
        return previous[m];
    }

}
