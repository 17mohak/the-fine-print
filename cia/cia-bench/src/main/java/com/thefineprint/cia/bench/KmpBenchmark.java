package com.thefineprint.cia.bench;

import com.thefineprint.cia.algorithms.text.KmpMatcher;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * Time to find all occurrences of one pattern in one text: assessed KMP vs naive vs JDK {@code indexOf}.
 *
 * <p>Inputs are prepared once per trial, so code-point conversion is excluded from the timing. KMP and naive
 * work on {@code int[]} code points; {@code indexOf} works on the {@code String}. That difference is part of
 * the honest comparison.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class KmpBenchmark {

    @Param({"10000", "100000", "1000000"})
    public int textLength;

    @Param({"8", "64", "512"})
    public int patternLength;

    /** ORDINARY: prose-like text, pattern excerpted from it. ADVERSARIAL: "aaaa…" text, "aa…ab" pattern. */
    @Param({"ORDINARY", "ADVERSARIAL"})
    public String input;

    private String text;
    private String pattern;
    private int[] textCodePoints;
    private int[] patternCodePoints;

    @Setup
    public void prepare() {
        if (input.equals("ADVERSARIAL")) {
            text = SyntheticText.adversarialText(textLength);
            pattern = SyntheticText.adversarialPattern(patternLength);
        } else {
            text = SyntheticText.ordinary(textLength, 1L);
            pattern = SyntheticText.excerpt(text, patternLength, 2L);
        }
        textCodePoints = text.codePoints().toArray();
        patternCodePoints = pattern.codePoints().toArray();
    }

    @Benchmark
    public int[] kmp() {
        return KmpMatcher.search(textCodePoints, patternCodePoints).matchStarts();
    }

    @Benchmark
    public int[] naive() {
        return Baselines.naiveAllStarts(textCodePoints, patternCodePoints);
    }

    @Benchmark
    public int[] jdkIndexOf() {
        return Baselines.jdkIndexOfAllStarts(text, pattern);
    }
}
