package experiment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExperimentTest {
    @TempDir Path directory;

    @Test
    void generatorIsRepeatableAndProducesIntendedShapes() {
        InputGenerator generator = new InputGenerator();
        var first = generator.edit("random", 12);
        generator.edit("random", 99);
        assertSameInput(first, new InputGenerator().edit("random", 12));
        assertEquals(12, first.first().length());
        assertEquals(12, first.second().length());
        assertTrue(first.first().matches("[ABCD]+"));
        var mismatch = generator.edit("mismatch", 4);
        assertEquals("AAAA", mismatch.first());
        assertEquals("BBBB", mismatch.second());
        var identical = generator.edit("identical", 4);
        assertEquals(identical.first(), identical.second());
        var adversarial = generator.search("adversarial", 100);
        assertEquals("A".repeat(100), adversarial.first());
        assertEquals("A".repeat(31) + "B", adversarial.second());
        assertSameInput(generator.search("random", 100), new InputGenerator().search("random", 100));
        assertThrows(IllegalArgumentException.class, () -> generator.edit("random", -1));
        assertThrows(IllegalArgumentException.class, () -> generator.search("unknown", 1));
    }

    @Test
    void disagreementStopsExperiment() {
        assertThrows(IllegalStateException.class, () -> Experiment.requireEqual(2, 3));
        assertThrows(IllegalStateException.class, () -> Experiment.requireEqual(List.of(1, 2), List.of(1)));
    }

    private void assertSameInput(InputGenerator.Input expected, InputGenerator.Input actual) {
        assertEquals(expected.first(), actual.first());
        assertEquals(expected.second(), actual.second());
        assertEquals(expected.seed(), actual.seed());
    }

    @Test
    void csvHasFiveTrialsAndReproducibleNonTimingColumns() throws Exception {
        Path output = directory.resolve("results.csv");
        Experiment.run(output, new int[]{2}, new int[]{4}, new int[]{64});
        List<String> a = Files.readAllLines(output);
        Experiment.run(output, new int[]{2}, new int[]{4}, new int[]{64});
        List<String> b = Files.readAllLines(output);
        assertEquals(96, a.size());
        assertEquals(a.size(), b.size());
        assertEquals(a.getFirst(), b.getFirst());
        for (int row = 1; row < a.size(); row++) {
            String[] left = a.get(row).split(",");
            String[] right = b.get(row).split(",");
            assertEquals(16, left.length);
            assertTrue(Double.parseDouble(left[5]) > 0);
            assertTrue(Double.parseDouble(right[5]) > 0);
            assertEquals((row - 1) % 5 + 1, Integer.parseInt(left[4]));
            left[5] = right[5] = "time varies";
            assertArrayEquals(left, right, "Non-timing data: " + Arrays.toString(left));
        }
    }
}
