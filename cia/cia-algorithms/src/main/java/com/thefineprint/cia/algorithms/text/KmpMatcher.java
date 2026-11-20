package com.thefineprint.cia.algorithms.text;

import java.util.Arrays;
import java.util.Objects;

/**
 * Knuth–Morris–Pratt exact matching over arrays of Unicode code points.
 *
 * <p>Finds <b>every</b> occurrence of a pattern in a text, including overlapping ones, in O(n + m) time and
 * O(m) auxiliary space, where n is the text length and m the pattern length.
 *
 * <p>Working on {@code int[]} code points (not Java {@code char}s) means offsets are code-point offsets, which is
 * what the platform's anchors use (SYSTEM_DESIGN 2.2), and a supplementary character such as an emoji is a
 * single symbol rather than a surrogate pair.
 */
public final class KmpMatcher {

    private KmpMatcher() {
    }

    /**
     * Computes the prefix function (failure function) of {@code pattern}.
     *
     * <p>{@code pi[i]} is the length of the longest <i>proper</i> prefix of {@code pattern[0..i]} that is also a
     * suffix of it. Example: for {@code "aabaaab"} the result is {@code [0, 1, 0, 1, 2, 2, 3]}.
     *
     * <p>Invariant at the top of each iteration: {@code k == pi[i - 1]}, the length of the longest border of the
     * prefix ending at {@code i - 1}. When {@code pattern[i]} cannot extend that border we fall back to the next
     * shorter border {@code pi[k - 1]}; every border of a border is itself a border, so no candidate is skipped.
     *
     * @throws IllegalArgumentException if {@code pattern} is empty (an empty pattern has no prefix function)
     */
    public static int[] prefixFunction(int[] pattern) {
        Objects.requireNonNull(pattern, "pattern");
        if (pattern.length == 0) {
            throw new IllegalArgumentException("pattern must not be empty");
        }
        int[] pi = new int[pattern.length];
        int k = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (k > 0 && pattern[i] != pattern[k]) {
                k = pi[k - 1];
            }
            if (pattern[i] == pattern[k]) {
                k++;
            }
            pi[i] = k;
        }
        return pi;
    }

    /**
     * Returns the start offsets of every occurrence of {@code pattern} in {@code text}, in ascending order.
     *
     * <p>Invariant: after reading {@code text[i]}, {@code q} is the length of the longest prefix of the pattern
     * that ends exactly at {@code text[i]}. On a full match ({@code q == m}) we report {@code i - m + 1} and continue
     * from {@code pi[m - 1]}, so overlapping occurrences (e.g. "aa" in "aaaa" at 0, 1, 2) are all found.
     *
     * <p>Complexity: each loop iteration either advances {@code i} or strictly decreases {@code q}, and {@code q}
     * increases at most once per text character, so the total work is O(n). With the prefix function, O(n + m).
     *
     * @param text the text to search; may be empty (then there are no matches)
     * @param pattern the pattern; must not be empty
     * @return the result, including the number of character comparisons performed
     * @throws IllegalArgumentException if {@code pattern} is empty
     */
    public static KmpResult search(int[] text, int[] pattern) {
        Objects.requireNonNull(text, "text");
        int[] pi = prefixFunction(pattern);
        int m = pattern.length;
        if (m > text.length) {
            return new KmpResult(new int[0], pi, 0);
        }

        int[] starts = new int[8];
        int found = 0;
        long comparisons = 0;
        int q = 0;
        for (int i = 0; i < text.length; i++) {
            while (q > 0 && text[i] != pattern[q]) {
                comparisons++;
                q = pi[q - 1];
            }
            comparisons++;
            if (text[i] == pattern[q]) {
                q++;
            }
            if (q == m) {
                if (found == starts.length) {
                    starts = Arrays.copyOf(starts, found * 2);
                }
                starts[found++] = i - m + 1;
                q = pi[q - 1];
            }
        }
        return new KmpResult(Arrays.copyOf(starts, found), pi, comparisons);
    }
}
