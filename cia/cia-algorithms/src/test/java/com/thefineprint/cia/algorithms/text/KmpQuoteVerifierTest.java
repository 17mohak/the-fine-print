package com.thefineprint.cia.algorithms.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.thefineprint.core.api.spi.QuoteVerifier;
import com.thefineprint.core.api.text.Span;
import org.junit.jupiter.api.Test;

/** Checks the adapter honours the {@link QuoteVerifier} SPI contract. */
class KmpQuoteVerifierTest {

    private final QuoteVerifier verifier = new KmpQuoteVerifier();

    @Test
    void returnsCodePointSpansForEveryOccurrence() {
        String l1 = "Clause 4. The insurer may cancel. The insurer may renew.";
        assertThat(verifier.find(l1, "The insurer may"))
                .containsExactly(new Span(10, 25), new Span(34, 49));
    }

    @Test
    void spansCoverTheQuotedTextExactly() {
        String l1 = "Notice period: 30 days. 😀 Notice period: 60 days.";
        int[] cps = l1.codePoints().toArray();
        for (Span span : verifier.find(l1, "Notice period")) {
            String quoted = new String(cps, span.start(), span.end() - span.start());
            assertThat(quoted).isEqualTo("Notice period");
        }
    }

    @Test
    void returnsEmptyListWhenQuoteAbsentOrTextEmpty() {
        assertThat(verifier.find("no match here", "absent")).isEmpty();
        assertThat(verifier.find("", "anything")).isEmpty();
    }

    @Test
    void performsNoFolding() {
        assertThat(verifier.find("Premium", "premium")).as("case is significant").isEmpty();
        assertThat(verifier.find("“quoted”", "\"quoted\"")).as("quote marks are not folded").isEmpty();
    }

    @Test
    void rejectsEmptyAndNullArguments() {
        assertThatThrownBy(() -> verifier.find("text", "")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> verifier.find(null, "q")).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> verifier.find("t", null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void resultIsImmutable() {
        assertThatThrownBy(() -> verifier.find("aa", "a").add(new Span(0, 1)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
