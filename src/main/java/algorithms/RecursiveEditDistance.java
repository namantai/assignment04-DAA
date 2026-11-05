package algorithms;

import java.util.Objects;

public final class RecursiveEditDistance implements EditDistance {

    // State: d(n,m) is the distance between the first n and m characters.
    // Transition: equal last characters use d(n-1,m-1); otherwise 1 + min(delete, insert, replace).
    // Base case: d(0,m) = m and d(n,0) = n.
    @Override
    public int distance(String a, String b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        return solve(a, b, a.length(), b.length());
    }

    private int solve(String a, String b, int n, int m) {
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

}
