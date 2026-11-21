package com.thefineprint.cia.bench;

import com.thefineprint.cia.algorithms.membership.BloomFilter;
import com.thefineprint.cia.algorithms.membership.BloomParameters;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Deterministic measurement of false-positive rate and memory, Bloom filter vs {@code HashSet<Long>}.
 *
 * <p>For each size {@code n}, bits-per-element {@code b} and hash seed: insert keys {@code 0..n-1}, probe
 * 1,000,000 keys that were never inserted, and count false positives. Several seeds per configuration show the
 * seed-to-seed spread of the measured rate. Memory: the Bloom figure is the exact bit-array payload;
 * the HashSet figure is an <b>approximation</b> from the change in used heap after forced GCs (median of three
 * builds). It includes the boxed {@code Long}s and table entries, which is the real cost of exact membership in
 * Java.
 */
public final class BloomExperiment {

    static final int NEGATIVE_PROBES = 1_000_000;
    static final long[] SEEDS = {7L, 11L, 13L, 17L, 19L};

    /** One output row. */
    public record Row(int elements, int bitsPerElement, long seed, int hashCount, double theoreticalFpr,
                      double measuredFpr, long bloomBytes, long hashSetBytesApprox, double bloomBuildMillis) {
    }

    public static void main(String[] args) throws IOException {
        Path out = Path.of(args.length > 0 ? args[0] : "bloom-experiment.csv");
        List<Row> rows = run(new int[] {10_000, 100_000, 1_000_000}, new int[] {6, 10, 14}, SEEDS, NEGATIVE_PROBES);
        write(rows, out);
        System.out.println("Wrote " + rows.size() + " rows to " + out.toAbsolutePath());
    }

    public static List<Row> run(int[] sizes, int[] bitsPerElementValues, long[] seeds, int negativeProbes) {
        List<Row> rows = new ArrayList<>();
        for (int n : sizes) {
            long hashSetBytes = measureHashSetBytes(n);
            for (int b : bitsPerElementValues) {
                for (long seed : seeds) {
                    rows.add(measure(n, b, seed, negativeProbes, hashSetBytes));
                }
            }
        }
        return rows;
    }

    /** Builds one filter and counts false positives among {@code negativeProbes} never-inserted keys. */
    private static Row measure(int n, int bitsPerElement, long seed, int negativeProbes, long hashSetBytes) {
        BloomParameters params = BloomParameters.withBitsPerElement(n, bitsPerElement);
        long start = System.nanoTime();
        BloomFilter filter = new BloomFilter(params, seed);
        for (long i = 0; i < n; i++) {
            filter.add(BloomBenchmark.bytes(i));
        }
        double buildMillis = (System.nanoTime() - start) / 1e6;
        int falsePositives = 0;
        for (long i = 0; i < negativeProbes; i++) {
            if (filter.mightContain(BloomBenchmark.bytes(1_000_000_000L + i))) {
                falsePositives++;
            }
        }
        return new Row(n, bitsPerElement, seed, params.hashCount(), params.theoreticalFalsePositiveRate(n),
                (double) falsePositives / negativeProbes, filter.memoryBytes(), hashSetBytes, buildMillis);
    }

    /** Approximate retained size of a HashSet of {@code n} boxed longs (median of three measurements). */
    static long measureHashSetBytes(int n) {
        long[] samples = new long[3];
        for (int s = 0; s < samples.length; s++) {
            long before = usedHeapAfterGc();
            Set<Long> set = new HashSet<>();
            for (long i = 0; i < n; i++) {
                set.add(i + 1_000L); // avoid the small-Long cache (-128..127)
            }
            long after = usedHeapAfterGc();
            samples[s] = Math.max(0, after - before);
            if (set.size() != n) {
                throw new IllegalStateException("unexpected set size");
            }
        }
        java.util.Arrays.sort(samples);
        return samples[1];
    }

    private static long usedHeapAfterGc() {
        Runtime rt = Runtime.getRuntime();
        for (int i = 0; i < 3; i++) {
            System.gc();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return rt.totalMemory() - rt.freeMemory();
    }

    static void write(List<Row> rows, Path out) throws IOException {
        Files.createDirectories(out.toAbsolutePath().getParent());
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(out, StandardCharsets.UTF_8))) {
            w.println("# Bloom filter FPR and memory experiment; java=" + System.getProperty("java.version")
                    + " vm=" + System.getProperty("java.vm.name") + " os=" + System.getProperty("os.name")
                    + " cpus=" + Runtime.getRuntime().availableProcessors() + " negativeProbes=" + NEGATIVE_PROBES
                    + " probeStrategy=coprime-step");
            w.println("elements,bitsPerElement,seed,hashCount,theoreticalFpr,measuredFpr,bloomBytes,"
                    + "hashSetBytesApprox,bloomBuildMillis");
            for (Row r : rows) {
                w.println(String.format(Locale.ROOT, "%d,%d,%d,%d,%.6f,%.6f,%d,%d,%.2f", r.elements(),
                        r.bitsPerElement(), r.seed(), r.hashCount(), r.theoreticalFpr(), r.measuredFpr(), r.bloomBytes(),
                        r.hashSetBytesApprox(), r.bloomBuildMillis()));
            }
        }
    }
}
