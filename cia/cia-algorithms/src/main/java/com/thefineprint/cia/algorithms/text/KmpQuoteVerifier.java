package com.thefineprint.cia.algorithms.text;

import com.thefineprint.core.api.spi.QuoteVerifier;
import com.thefineprint.core.api.text.Span;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * {@link QuoteVerifier} backed by the assessed KMP implementation.
 *
 * <p>This is the <b>assessed</b> adapter (Track C). It is used only by {@code cia-demo} and {@code cia-bench};
 * the production platform never depends on it (ARCHITECTURE_OVERVIEW section 2).
 *
 * <p>Per the SPI contract the caller has already normalised both strings with the same L1 pipeline; this class
 * performs no normalisation or folding. It only converts the strings to code points, which makes the returned
 * spans code-point offsets.
 */
public final class KmpQuoteVerifier implements QuoteVerifier {

    @Override
    public List<Span> find(String l1, String normalisedQuote) {
        Objects.requireNonNull(l1, "l1");
        Objects.requireNonNull(normalisedQuote, "normalisedQuote");
        if (normalisedQuote.isEmpty()) {
            throw new IllegalArgumentException("normalisedQuote must not be empty");
        }
        int[] pattern = normalisedQuote.codePoints().toArray();
        KmpResult result = KmpMatcher.search(l1.codePoints().toArray(), pattern);

        List<Span> spans = new ArrayList<>(result.matchCount());
        for (int start : result.matchStarts()) {
            spans.add(new Span(start, start + pattern.length));
        }
        return List.copyOf(spans);
    }
}
