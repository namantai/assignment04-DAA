package algorithms;

import java.util.Objects;

public final class TwoRowsEditDistance implements EditDistance {
    private final Counters counters = new Counters();

    @Override
    public int distance(String a, String b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        counters.reset();
        int m = b.length();
        int[] previous = new int[m + 1];
        counters.allocatedCells += previous.length;
        int[] current = new int[m + 1];
        counters.allocatedCells += current.length;
        for (int j = 0; j <= m; j++) previous[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= m; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(previous[j] + 1,
                        Math.min(current[j - 1] + 1, previous[j - 1] + cost));
                counters.cellUpdates++;
            }
            int[] temporary = previous;
            previous = current;
            current = temporary;
        }
        return previous[m];
    }

    @Override
    public Counters counters() { return counters; }
}
