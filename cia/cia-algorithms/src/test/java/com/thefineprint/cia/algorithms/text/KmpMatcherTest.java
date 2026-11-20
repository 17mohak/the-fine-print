package com.thefineprint.cia.algorithms.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

class KmpMatcherTest {

    private static int[] cp(String s) {
        return s.codePoints().toArray();
    }

    private static int[] starts(String text, String pattern) {
        return KmpMatcher.search(cp(text), cp(pattern)).matchStarts();
    }

    /** Reference: try every alignment and compare every symbol. Obviously correct, O(n * m). */
    static int[] naiveStarts(int[] text, int[] pattern) {
        int[] out = new int[Math.max(0, text.length - pattern.length + 1)];
        int found = 0;
        for (int i = 0; i + pattern.length <= text.length; i++) {
            int j = 0;
            while (j < pattern.length && text[i + j] == pattern[j]) {
                j++;
            }
            if (j == pattern.length) {
                out[found++] = i;
            }
        }
        return Arrays.copyOf(out, found);
    }

    @Test
    void prefixFunctionMatchesHandComputedValues() {
        assertThat(KmpMatcher.prefixFunction(cp("aabaaab"))).containsExactly(0, 1, 0, 1, 2, 2, 3);
        assertThat(KmpMatcher.prefixFunction(cp("abcd"))).containsExactly(0, 0, 0, 0);
        assertThat(KmpMatcher.prefixFunction(cp("aaaa"))).containsExactly(0, 1, 2, 3);
        assertThat(KmpMatcher.prefixFunction(cp("abab"))).containsExactly(0, 0, 1, 2);
        assertThat(KmpMatcher.prefixFunction(cp("a"))).containsExactly(0);
    }

    @Test
    void findsASingleOccurrence() {
        assertThat(starts("the waiting period is 30 days", "waiting period")).containsExactly(4);
    }

    @Test
    void findsEveryOccurrenceInOrder() {
        assertThat(starts("fee. late fee. fee", "fee")).containsExactly(0, 10, 15);
    }

    @Test
    void findsOverlappingOccurrences() {
        assertThat(starts("aaaa", "aa")).containsExactly(0, 1, 2);
        assertThat(starts("ababa", "aba")).containsExactly(0, 2);
        assertThat(starts("aabaaabaaab", "aabaaab")).containsExactly(0, 4);
    }

    @Test
    void reportsNoMatchWhenAbsent() {
        assertThat(starts("premium payable annually", "monthly")).isEmpty();
    }

    @Test
    void boundaryCases() {
        assertThat(starts("", "a")).as("empty text").isEmpty();
        assertThat(starts("abc", "abcd")).as("pattern longer than text").isEmpty();
        assertThat(starts("abc", "abc")).as("pattern equals text").containsExactly(0);
        assertThat(starts("a", "a")).as("single symbol").containsExactly(0);
        assertThat(starts("xyz", "z")).as("match at last position").containsExactly(2);
    }

    @Test
    void rejectsInvalidInput() {
        assertThatThrownBy(() -> KmpMatcher.search(cp("abc"), new int[0]))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> KmpMatcher.prefixFunction(new int[0]))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> KmpMatcher.search(null, cp("a"))).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> KmpMatcher.search(cp("a"), null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void offsetsAreCodePointsNotUtf16Units() {
        // U+1F600 is one code point but two Java chars; Devanagari "ा" (U+093E) is a combining vowel sign.
        assertThat(starts("😀a😀a", "😀a")).containsExactly(0, 2);
        assertThat(starts("नियम और शर्तें नियम", "नियम")).containsExactly(0, 15);
    }

    @Test
    void comparisonCountIsLinearEvenOnAdversarialInput() {
        int n = 100_000;
        int[] text = new int[n];
        Arrays.fill(text, 'a');
        int[] pattern = cp("a".repeat(999) + "b");
        KmpResult result = KmpMatcher.search(text, pattern);
        assertThat(result.matchCount()).isZero();
        assertThat(result.characterComparisons()).isLessThanOrEqualTo(2L * n);
    }

    @Test
    void agreesWithNaiveReferenceOnRandomInputs() {
        Random random = new Random(20261120L); // fixed seed: reproducible
        for (int trial = 0; trial < 5_000; trial++) {
            int alphabet = 1 + random.nextInt(3); // tiny alphabets force repeats and overlaps
            int[] text = randomSymbols(random, random.nextInt(60), alphabet);
            int[] pattern = randomSymbols(random, 1 + random.nextInt(6), alphabet);
            KmpResult result = KmpMatcher.search(text, pattern);
            assertThat(result.matchStarts())
                    .as("trial %d text=%s pattern=%s", trial, Arrays.toString(text), Arrays.toString(pattern))
                    .containsExactly(naiveStarts(text, pattern));
            assertThat(result.characterComparisons()).isLessThanOrEqualTo(2L * text.length);
        }
    }

    @Test
    void resultArraysAreDefensiveCopies() {
        KmpResult result = KmpMatcher.search(cp("abab"), cp("ab"));
        result.matchStarts()[0] = 99;
        assertThat(result.matchStarts()).containsExactly(0, 2);
    }

    private static int[] randomSymbols(Random random, int length, int alphabet) {
        int[] out = new int[length];
        for (int i = 0; i < length; i++) {
            out[i] = 'a' + random.nextInt(alphabet);
        }
        return out;
    }
}
