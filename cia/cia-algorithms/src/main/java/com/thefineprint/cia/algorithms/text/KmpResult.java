package com.thefineprint.cia.algorithms.text;

import java.util.Arrays;

/**
 * Outcome of one {@link KmpMatcher#search} call.
 *
 * <p>Arrays are copied on the way in and out, so a result can be shared safely.
 *
 * @param matchStarts code-point offsets of every occurrence, ascending
 * @param prefixFunction the pattern's prefix function (useful for explaining the run)
 * @param characterComparisons how many text-versus-pattern symbol comparisons the search made; bounded by 2n
 */
public record KmpResult(int[] matchStarts, int[] prefixFunction, long characterComparisons) {

    public KmpResult {
        matchStarts = matchStarts.clone();
        prefixFunction = prefixFunction.clone();
    }

    @Override
    public int[] matchStarts() {
        return matchStarts.clone();
    }

    @Override
    public int[] prefixFunction() {
        return prefixFunction.clone();
    }

    /** Number of occurrences found. */
    public int matchCount() {
        return matchStarts.length;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof KmpResult that
                && Arrays.equals(matchStarts, that.matchStarts)
                && Arrays.equals(prefixFunction, that.prefixFunction)
                && characterComparisons == that.characterComparisons;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * Arrays.hashCode(matchStarts) + Arrays.hashCode(prefixFunction))
                + Long.hashCode(characterComparisons);
    }

    @Override
    public String toString() {
        return "KmpResult[matchStarts=" + Arrays.toString(matchStarts) + ", prefixFunction="
                + Arrays.toString(prefixFunction) + ", characterComparisons=" + characterComparisons + "]";
    }
}
