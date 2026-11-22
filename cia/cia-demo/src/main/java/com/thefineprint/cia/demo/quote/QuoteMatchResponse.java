package com.thefineprint.cia.demo.quote;

import java.util.List;

/**
 * Result of one quote-matching run. All offsets and lengths are in Unicode code points.
 *
 * @param algorithm the algorithm that ran (always KMP here)
 * @param textLength n
 * @param quoteLength m
 * @param matches every occurrence, as half-open {@code [start, end)} ranges, ascending
 * @param characterComparisons symbol comparisons the search made
 * @param comparisonBound the proven upper bound for the search phase, {@code 2n}
 * @param quoteSymbols the first quote symbols (up to 64), for the prefix-function table
 * @param prefixFunction the prefix-function values for those symbols
 * @param prefixFunctionTruncated whether the quote was longer than the explained portion
 * @param serverMicros wall time of the search on the server, in microseconds (indicative only)
 */
public record QuoteMatchResponse(
        String algorithm,
        int textLength,
        int quoteLength,
        List<Match> matches,
        long characterComparisons,
        long comparisonBound,
        List<String> quoteSymbols,
        List<Integer> prefixFunction,
        boolean prefixFunctionTruncated,
        long serverMicros) {

    /** One occurrence. */
    public record Match(int start, int end) {
    }
}
