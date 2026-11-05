package experiment;

import algorithms.Counters;
import algorithms.EditDistance;
import algorithms.StringSearch;
import algorithms.RecursiveEditDistance;
import algorithms.TableEditDistance;
import algorithms.TwoRowsEditDistance;
import algorithms.NaiveSearch;
import algorithms.KMPSearch;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public final class Experiment {
    private static final String HEADER = "algorithm,input,n,m,trial,time_ms,comparisons,recursive_calls,"
            + "cell_updates,lps_comparisons,allocated_cells,seed,case_seed,repetitions,result,result_hash";
    private static volatile int sink;

    private Experiment() { }

    public static void main(String[] args) throws IOException {
        if (args.length != 0) throw new IllegalArgumentException("Usage: java -jar <jar>");
        Path output = Path.of("results/results.csv");
        run(output, new int[]{3, 6, 9, 12}, new int[]{100, 200, 400, 800},
                new int[]{1000, 2000, 4000, 8000, 16000});
    }

    static void run(Path output, int[] smallSizes, int[] largeSizes, int[] searchSizes) throws IOException {
        warmUp();
        List<String> rows = new ArrayList<>();
        InputGenerator generator = new InputGenerator();
        for (String type : List.of("random", "mismatch", "identical")) {
            for (int n : smallSizes) measureEdit(rows, type, generator.edit(type, n), true);
            for (int n : largeSizes) measureEdit(rows, type, generator.edit(type, n), false);
        }
        for (String type : List.of("random", "adversarial")) {
            for (int n : searchSizes) {
                InputGenerator.Input input = generator.search(type, n);
                List<StringSearch> algorithms = List.of(new NaiveSearch(), new KMPSearch());
                List<Integer> expected = algorithms.getFirst().search(input.first(), input.second());
                for (StringSearch algorithm : algorithms) {
                    requireEqual(expected, algorithm.search(input.first(), input.second()));
                }
                String[] names = {"NaiveSearch", "KMPSearch"};
                for (int i = 0; i < algorithms.size(); i++) {
                    StringSearch algorithm = algorithms.get(i);
                    measure(rows, names[i], type, input, algorithm.counters(),
                            () -> algorithm.search(input.first(), input.second()), expected, 100);
                }
            }
        }

        Path destination = output.toAbsolutePath();
        Files.createDirectories(destination.getParent());
        Path temporary = Files.createTempFile(destination.getParent(), "experiment-", ".csv.tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                writer.write(HEADER);
                writer.newLine();
                for (String row : rows) {
                    writer.write(row);
                    writer.newLine();
                }
            }
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
        System.out.println("Saved " + rows.size() + " measured trials to " + destination);
    }

    private static void measureEdit(List<String> rows, String type, InputGenerator.Input input, boolean recursive) {
        List<EditDistance> algorithms = new ArrayList<>(List.of(new TableEditDistance(), new TwoRowsEditDistance()));
        List<String> names = new ArrayList<>(List.of("TableEditDistance", "TwoRowsEditDistance"));
        if (recursive) {
            algorithms.addFirst(new RecursiveEditDistance());
            names.addFirst("RecursiveEditDistance");
        }
        int expected = new TableEditDistance().distance(input.first(), input.second());
        for (EditDistance algorithm : algorithms) {
            requireEqual(expected, algorithm.distance(input.first(), input.second()));
        }
        for (int i = 0; i < algorithms.size(); i++) {
            EditDistance algorithm = algorithms.get(i);
            int n = input.first().length();
            int repetitions;
            if (algorithm instanceof RecursiveEditDistance && !type.equals("identical")) {
                repetitions = n <= 3 ? 500 : n <= 6 ? 20 : 1;
            } else {
                repetitions = n <= 12 ? 1000 : n <= 200 ? 20 : 5;
            }
            measure(rows, names.get(i), type, input, algorithm.counters(),
                    () -> algorithm.distance(input.first(), input.second()), expected, repetitions);
        }
    }

    private static void measure(List<String> rows, String algorithm, String type, InputGenerator.Input input,
                                Counters counters, Supplier<?> operation, Object expected, int repetitions) {
        Object[] answers = new Object[repetitions];
        long[] referenceCounters = snapshot(counters);
        double[] times = new double[5];
        for (int trial = -2; trial < 5; trial++) {
            long start = System.nanoTime();
            for (int r = 0; r < repetitions; r++) answers[r] = operation.get();
            long elapsed = System.nanoTime() - start;
            for (Object answer : answers) requireEqual(expected, answer);
            if (!Arrays.equals(referenceCounters, snapshot(counters))) {
                throw new IllegalStateException("Counters changed for identical input: " + algorithm);
            }
            sink = answers[repetitions - 1].hashCode();
            if (trial < 0) continue;
            double timeMs = elapsed / 1_000_000.0 / repetitions;
            times[trial] = timeMs;
            int result = expected instanceof Integer value ? value : ((List<?>) expected).size();
            rows.add(String.format(Locale.ROOT,
                    "%s,%s,%d,%d,%d,%.9f,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d",
                    algorithm, type, input.first().length(), input.second().length(), trial + 1, timeMs,
                    counters.comparisons(), counters.recursiveCalls(), counters.cellUpdates(),
                    counters.lpsComparisons(), counters.allocatedCells(), InputGenerator.SEED,
                    input.seed(), repetitions, result, expected.hashCode()));
        }
        Arrays.sort(times);
        System.out.printf(Locale.ROOT, "%s %-11s n=%-5d median=%.6f ms%n",
                algorithm, type, input.first().length(), times[2]);
    }

    static void requireEqual(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new IllegalStateException("Algorithm mismatch: expected " + expected + ", got " + actual);
        }
    }

    private static long[] snapshot(Counters c) {
        return new long[]{c.comparisons(), c.recursiveCalls(), c.cellUpdates(), c.lpsComparisons(), c.allocatedCells()};
    }

    private static void warmUp() {
        List<EditDistance> edits = List.of(new RecursiveEditDistance(), new TableEditDistance(), new TwoRowsEditDistance());
        List<StringSearch> searches = List.of(new NaiveSearch(), new KMPSearch());
        InputGenerator generator = new InputGenerator();
        InputGenerator.Input randomSearch = generator.search("random", 512);
        InputGenerator.Input hardSearch = generator.search("adversarial", 512);
        for (int i = 0; i < 500; i++) {
            for (EditDistance edit : edits) {
                sink = edit.distance("AAAAAA", "BBBBBB");
                sink = edit.distance("ABCABC", "ABCDAB");
                sink = edit.distance("AAAAAA", "AAAAAA");
            }
            for (StringSearch search : searches) {
                sink = search.search(randomSearch.first(), randomSearch.second()).hashCode();
                sink = search.search(hardSearch.first(), hardSearch.second()).hashCode();
            }
        }
    }
}
