package experiment;

import java.util.Random;

public final class InputGenerator {
    public static final long SEED = 20261105L;
    public static final int PATTERN_LENGTH = 32;
    private static final String ALPHABET = "ABCD";

    public static final class Input {
        private final String first;
        private final String second;
        private final long seed;

        public Input(String first, String second, long seed) {
            this.first = first;
            this.second = second;
            this.seed = seed;
        }

        public String first() { return first; }
        public String second() { return second; }
        public long seed() { return seed; }
    }

    public Input edit(String type, int n) {
        if (n < 0) throw new IllegalArgumentException("Negative length");
        long seed = caseSeed(1, type, n);
        Random random = new Random(seed);
        return switch (type) {
            case "random" -> new Input(randomString(random, n), randomString(random, n), seed);
            case "mismatch" -> new Input("A".repeat(n), "B".repeat(n), seed);
            case "identical" -> new Input("A".repeat(n), "A".repeat(n), seed);
            default -> throw new IllegalArgumentException("Unknown edit input: " + type);
        };
    }

    public Input search(String type, int n) {
        if (n < 0) throw new IllegalArgumentException("Negative length");
        long seed = caseSeed(2, type, n);
        Random random = new Random(seed);
        return switch (type) {
            case "random" -> new Input(randomString(random, n), randomString(random, PATTERN_LENGTH), seed);
            case "adversarial" -> new Input("A".repeat(n), "A".repeat(PATTERN_LENGTH - 1) + "B", seed);
            default -> throw new IllegalArgumentException("Unknown search input: " + type);
        };
    }

    private long caseSeed(int family, String type, int n) {
        return SEED + 1_000_003L * family + 31L * type.hashCode() + 1009L * n;
    }

    private String randomString(Random random, int length) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) result.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        return result.toString();
    }
}
