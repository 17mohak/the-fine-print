package com.thefineprint.cia.demo.quote;

import com.thefineprint.cia.algorithms.text.KmpMatcher;
import com.thefineprint.cia.algorithms.text.KmpResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Runs the assessed KMP matcher on one (text, quote) pair and packages what the UI needs to explain the run.
 *
 * <p>Input is taken as already-canonical text: the demo does not apply the platform's L1 normalisation, so what
 * you type is exactly what is matched (no case, whitespace or quote-mark folding).
 */
@Service
public class QuoteMatchService {

    static final int MAX_TEXT_CODE_POINTS = 100_000;
    static final int MAX_QUOTE_CODE_POINTS = 2_000;
    /** The prefix-function table is shown for at most this many pattern symbols. */
    static final int EXPLAINED_SYMBOLS = 64;

    public QuoteMatchResponse match(QuoteMatchRequest request) {
        if (request == null || request.text() == null || request.quote() == null) {
            throw new IllegalArgumentException("text and quote are required");
        }
        int[] text = request.text().codePoints().toArray();
        int[] quote = request.quote().codePoints().toArray();
        if (quote.length == 0) {
            throw new IllegalArgumentException("quote must not be empty");
        }
        if (text.length > MAX_TEXT_CODE_POINTS) {
            throw new IllegalArgumentException("text is limited to " + MAX_TEXT_CODE_POINTS + " characters");
        }
        if (quote.length > MAX_QUOTE_CODE_POINTS) {
            throw new IllegalArgumentException("quote is limited to " + MAX_QUOTE_CODE_POINTS + " characters");
        }

        long started = System.nanoTime();
        KmpResult result = KmpMatcher.search(text, quote);
        long elapsedMicros = (System.nanoTime() - started) / 1_000;

        List<QuoteMatchResponse.Match> matches = new ArrayList<>();
        for (int start : result.matchStarts()) {
            matches.add(new QuoteMatchResponse.Match(start, start + quote.length));
        }
        int shown = Math.min(quote.length, EXPLAINED_SYMBOLS);
        List<String> symbols = new ArrayList<>(shown);
        for (int i = 0; i < shown; i++) {
            symbols.add(Character.toString(quote[i]));
        }
        return new QuoteMatchResponse(
                "Knuth-Morris-Pratt",
                text.length,
                quote.length,
                List.copyOf(matches),
                result.characterComparisons(),
                2L * text.length,
                symbols,
                Arrays.stream(result.prefixFunction()).limit(shown).boxed().toList(),
                quote.length > shown,
                elapsedMicros);
    }
}
