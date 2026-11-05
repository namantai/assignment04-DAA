package algorithms;

import java.util.Objects;

public final class TableEditDistance implements EditDistance {
    private final Counters counters = new Counters();

    @Override
    public int distance(String a, String b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        counters.reset();
        int n = a.length();
        int m = b.length();
        int[][] dp = new int[n + 1][];
        for (int i = 0; i <= n; i++) {
            dp[i] = new int[m + 1];
            counters.allocatedCells += dp[i].length;
            dp[i][0] = i;
        }
        for (int j = 0; j <= m; j++) dp[0][j] = j;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(dp[i - 1][j] + 1,
                        Math.min(dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost));
                counters.cellUpdates++;
            }
        }
        return dp[n][m];
    }

    @Override
    public Counters counters() { return counters; }
}
