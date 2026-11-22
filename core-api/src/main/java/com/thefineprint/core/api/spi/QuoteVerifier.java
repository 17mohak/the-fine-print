package com.thefineprint.core.api.spi;

import com.thefineprint.core.api.text.Span;
import java.util.List;

/**
 * Finds every exact occurrence of a quote in canonical L1 text (SYSTEM_DESIGN 2.3 and section 4).
 *
 * <p><b>Contract</b>
 * <ul>
 *   <li>{@code l1} is canonical L1 text; {@code normalisedQuote} has already been passed through the same
 *       deterministic L1 pipeline by the caller. Implementations perform no normalisation, folding, fuzzy or
 *       edit-distance matching, and never use L2.</li>
 *   <li>Returns <b>all</b> exact code-point occurrences, including overlapping ones, as {@link Span}s in
 *       code-point offsets, in ascending order of {@code start}. Returns an empty list when there is none.</li>
 *   <li>An empty {@code normalisedQuote} is rejected with {@link IllegalArgumentException}; {@code null}
 *       arguments with {@link NullPointerException}.</li>
 *   <li>Grapheme-boundary alignment, clause scope and uniqueness rules (SYSTEM_DESIGN 5.3) are applied by the
 *       caller, not by this interface.</li>
 * </ul>
 *
 * <p><b>Implementations:</b> none in P0. The production adapter is P1 work; assessed implementations belong
 * to Track C and must never be imported by the platform (ARCHITECTURE_OVERVIEW section 2).
 */
public interface QuoteVerifier {

    /**
     * @param l1 canonical L1 text to search
     * @param normalisedQuote quote already normalised with the L1 pipeline; non-empty
     * @return all exact occurrences, ascending by start; never {@code null}
     */
    List<Span> find(String l1, String normalisedQuote);
}
