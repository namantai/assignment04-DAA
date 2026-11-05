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

class EditDistanceTest {
    static List<EditDistance> algorithms() {
        return List.of(new RecursiveEditDistance(), new TableEditDistance(), new TwoRowsEditDistance());
    }

    static List<Arguments> examples() {
        Object[][] cases = {
                {"kitten", "sitting", 3}, {"flaw", "lawn", 2}, {"intention", "execution", 5},
                {"", "abc", 3}, {"abc", "abc", 0}, {"", "", 0}, {"abc", "", 3},
                {"a", "b", 1}, {"a", "a", 0}, {"", "x", 1}, {"x", "", 1},
                {"ab", "abc", 1}, {"abc", "ac", 1}, {"cat", "cut", 1},
                {"aaaa", "aa", 2}, {"aaa", "bbb", 3}, {"a", "abcdef", 5}
        };
        List<Arguments> examples = new ArrayList<>();
        for (EditDistance algorithm : algorithms()) {
            for (Object[] c : cases) examples.add(Arguments.of(algorithm, c[0], c[1], c[2]));
        }
        return examples;
    }

    @ParameterizedTest(name = "{0}: distance({1},{2}) = {3}")
    @MethodSource("examples")
    void referenceAndBoundaryCases(EditDistance algorithm, String a, String b, int expected) {
        assertEquals(expected, algorithm.distance(a, b));
    }

    @TestFactory
    List<DynamicTest> randomPairsAgree() {
        Random random = new Random(20261105L);
        List<DynamicTest> tests = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            String a = randomString(random, random.nextInt(7));
            String b = randomString(random, random.nextInt(7));
            tests.add(DynamicTest.dynamicTest("random pair " + i + ": " + a + "/" + b, () -> {
                int expected = new TableEditDistance().distance(a, b);
                for (EditDistance algorithm : algorithms()) {
                    assertEquals(expected, algorithm.distance(a, b));
                    assertEquals(expected, algorithm.distance(b, a), "Symmetry");
                    assertTrue(expected >= Math.abs(a.length() - b.length()));
                    assertTrue(expected <= Math.max(a.length(), b.length()));
                }
            }));
        }
        return tests;
    }

    private static String randomString(Random random, int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) result.append("abc".charAt(random.nextInt(3)));
        return result.toString();
    }

    @Test
    void recursiveCounterCountsBaseCallsAndResets() {
        RecursiveEditDistance algorithm = new RecursiveEditDistance();
        assertEquals(1, algorithm.distance("a", "b"));
        assertEquals(4, algorithm.counters().recursiveCalls());
        assertEquals(0, algorithm.distance("abc", "abc"));
        assertEquals(4, algorithm.counters().recursiveCalls());
        assertEquals(0, algorithm.distance("", ""));
        assertEquals(1, algorithm.counters().recursiveCalls());
    }

    @ParameterizedTest
    @MethodSource("dpAlgorithms")
    void dpCountersCountOnlyInteriorCellsAndActualStorage(EditDistance algorithm) {
        assertEquals(3, algorithm.distance("ab", "cde"));
        assertEquals(6, algorithm.counters().cellUpdates());
        assertEquals(algorithm instanceof TableEditDistance ? 12 : 8, algorithm.counters().allocatedCells());
        assertEquals(0, algorithm.distance("", ""));
        assertEquals(0, algorithm.counters().cellUpdates());
        assertEquals(algorithm instanceof TableEditDistance ? 1 : 2, algorithm.counters().allocatedCells());
    }

    static List<EditDistance> dpAlgorithms() {
        return List.of(new TableEditDistance(), new TwoRowsEditDistance());
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void nullInputsAreRejected(EditDistance algorithm) {
        assertThrows(NullPointerException.class, () -> algorithm.distance(null, "a"));
        assertThrows(NullPointerException.class, () -> algorithm.distance("a", null));
    }
}
