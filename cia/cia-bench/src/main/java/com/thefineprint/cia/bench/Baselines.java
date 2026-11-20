package com.thefineprint.cia.bench;

import java.util.Arrays;

/**
 * Baselines that the assessed KMP is compared against. They live here, outside the assessed module, on purpose:
 * they are reference points, not the subject of assessment.
 */
public final class Baselines {

    private Baselines() {
    }

    /** Naive matching: try every alignment, compare left to right. O(n * m) in the worst case. */
    public static int[] naiveAllStarts(int[] text, int[] pattern) {
        int[] out = new int[8];
        int found = 0;
        for (int i = 0; i + pattern.length <= text.length; i++) {
            int j = 0;
            while (j < pattern.length && text[i + j] == pattern[j]) {
                j++;
            }
            if (j == pattern.length) {
                if (found == out.length) {
                    out = Arrays.copyOf(out, found * 2);
                }
                out[found++] = i;
            }
        }
        return Arrays.copyOf(out, found);
    }

    /**
     * All (overlapping) occurrences using the JDK's {@code String.indexOf}, which HotSpot implements with a
     * vectorised intrinsic. Offsets are UTF-16 indices; the benchmark inputs are ASCII, so they equal code points.
     */
    public static int[] jdkIndexOfAllStarts(String text, String pattern) {
        int[] out = new int[8];
        int found = 0;
        int from = text.indexOf(pattern);
        while (from >= 0) {
            if (found == out.length) {
                out = Arrays.copyOf(out, found * 2);
            }
            out[found++] = from;
            from = text.indexOf(pattern, from + 1);
        }
        return Arrays.copyOf(out, found);
    }
}
