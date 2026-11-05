package algorithms;

import java.util.Objects;

public final class RecursiveEditDistance implements EditDistance {
    private final Counters counters = new Counters();

    @Override
    public int distance(String a, String b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        counters.reset();
        return solve(a, b, a.length(), b.length());
    }

    private int solve(String a, String b, int n, int m) {
        counters.recursiveCalls++;
        if (n == 0) return m;
        if (m == 0) return n;
        if (a.charAt(n - 1) == b.charAt(m - 1)) {
            return solve(a, b, n - 1, m - 1);
        }
        int delete = solve(a, b, n - 1, m);
        int insert = solve(a, b, n, m - 1);
        int replace = solve(a, b, n - 1, m - 1);
        return 1 + Math.min(delete, Math.min(insert, replace));
    }

    @Override
    public Counters counters() { return counters; }
}
