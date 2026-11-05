package algorithms;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class StringSearchTest {
    static List<StringSearch> algorithms() {
        return List.of(new NaiveSearch(), new KMPSearch());
    }

    static List<Arguments> examples() {
        Object[][] cases = {
                {"ABABDABACDABABCABAB", "ABABCABAB", List.of(10)},
                {"AAAAA", "AA", List.of(0, 1, 2, 3)},
                {"ABABABA", "ABA", List.of(0, 2, 4)},
                {"abcxyz", "abc", List.of(0)}, {"xyzabc", "abc", List.of(3)},
                {"abcdef", "zzz", List.of()}, {"ab", "abc", List.of()},
                {"", "a", List.of()}, {"abc", "abc", List.of(0)},
                {"a", "a", List.of(0)}, {"abaca", "a", List.of(0, 2, 4)},
                {"AAAAAAAAB", "AAAAB", List.of(4)}
        };
        List<Arguments> examples = new ArrayList<>();
        for (StringSearch algorithm : algorithms()) {
            for (Object[] c : cases) examples.add(Arguments.of(algorithm, c[0], c[1], c[2]));
        }
        return examples;
    }

    @ParameterizedTest
    @MethodSource("examples")
    void referenceAndBoundaryCases(StringSearch algorithm, String text, String pattern, List<Integer> expected) {
        assertEquals(expected, algorithm.search(text, pattern));
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void emptyPatternIsRejected(StringSearch algorithm) {
        assertThrows(IllegalArgumentException.class, () -> algorithm.search("abc", ""));
        assertThrows(IllegalArgumentException.class, () -> algorithm.search("", ""));
    }

    static List<Arguments> lpsExamples() {
        return List.of(
                Arguments.of("AAACAAAA", new int[]{0, 1, 2, 0, 1, 2, 3, 3}),
                Arguments.of("ABABCABAB", new int[]{0, 0, 1, 2, 0, 1, 2, 3, 4}),
                Arguments.of("A", new int[]{0}), Arguments.of("ABCD", new int[]{0, 0, 0, 0}),
                Arguments.of("AAAA", new int[]{0, 1, 2, 3}));
    }

    @ParameterizedTest
    @MethodSource("lpsExamples")
    void lpsReferenceArrays(String pattern, int[] expected) {
        assertArrayEquals(expected, new KMPSearch().lps(pattern));
    }

    @Test
    void lpsRejectsEmptyPattern() {
        assertThrows(IllegalArgumentException.class, () -> new KMPSearch().lps(""));
    }

    @TestFactory
    List<DynamicTest> randomTextsAgreeWithIndependentOracle() {
        Random random = new Random(1142026L);
        List<DynamicTest> tests = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            String text = randomString(random, random.nextInt(81));
            String pattern = randomString(random, 1 + random.nextInt(12));

            if (i % 2 == 0 && !text.isEmpty()) {
                int start = random.nextInt(text.length());
                pattern = text.substring(start, Math.min(text.length(), start + 1 + random.nextInt(8)));
            }
            String p = pattern;
            tests.add(DynamicTest.dynamicTest("random search " + i, () -> {
                List<Integer> expected = new ArrayList<>();
                for (int pos = text.indexOf(p); pos >= 0; pos = text.indexOf(p, pos + 1)) expected.add(pos);
                assertEquals(expected, new NaiveSearch().search(text, p));
                assertEquals(expected, new KMPSearch().search(text, p));
            }));
        }
        return tests;
    }

    private static String randomString(Random random, int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) result.append("ABC".charAt(random.nextInt(3)));
        return result.toString();
    }

    @Test
    void handCountedSearchAndLpsComparisons() {
        NaiveSearch naive = new NaiveSearch();
        KMPSearch kmp = new KMPSearch();
        assertEquals(List.of(), naive.search("AAAAA", "AAB"));
        assertEquals(9, naive.counters().comparisons());
        assertEquals(List.of(), kmp.search("AAAAA", "AAB"));
        assertEquals(8, kmp.counters().comparisons());
        assertEquals(3, kmp.counters().lpsComparisons());
        assertEquals(3, kmp.counters().allocatedCells());
        assertArrayEquals(new int[]{0}, kmp.lps("X"));
        assertEquals(0, kmp.counters().comparisons());
        assertEquals(0, kmp.counters().lpsComparisons());
        naive.search("a", "a");
        assertEquals(1, naive.counters().comparisons());
        kmp.search("a", "ab");
        assertEquals(0, kmp.counters().comparisons());
        assertEquals(0, kmp.counters().allocatedCells());
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void nullInputsAreRejected(StringSearch algorithm) {
        assertThrows(NullPointerException.class, () -> algorithm.search(null, "a"));
        assertThrows(NullPointerException.class, () -> algorithm.search("a", null));
    }
}
