package com.thefineprint.cia.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.thefineprint.cia.demo.membership.MembershipRequest;
import com.thefineprint.cia.demo.membership.MembershipResponse;
import com.thefineprint.cia.demo.membership.MembershipResponse.BloomVerdict;
import com.thefineprint.cia.demo.membership.MembershipResponse.ExactVerdict;
import com.thefineprint.cia.demo.membership.MembershipScreeningService;
import com.thefineprint.cia.demo.quote.QuoteMatchRequest;
import com.thefineprint.cia.demo.quote.QuoteMatchResponse;
import com.thefineprint.cia.demo.quote.QuoteMatchService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DemoServicesTest {

    private final QuoteMatchService quotes = new QuoteMatchService();
    private final MembershipScreeningService membership = new MembershipScreeningService();

    @Test
    void quoteMatchReturnsEveryOccurrenceAndExplanation() {
        QuoteMatchResponse r = quotes.match(new QuoteMatchRequest("abab ab abab", "abab"));
        assertThat(r.matches()).containsExactly(new QuoteMatchResponse.Match(0, 4),
                new QuoteMatchResponse.Match(8, 12));
        assertThat(r.prefixFunction()).containsExactly(0, 0, 1, 2);
        assertThat(r.quoteSymbols()).containsExactly("a", "b", "a", "b");
        assertThat(r.characterComparisons()).isLessThanOrEqualTo(r.comparisonBound());
        assertThat(r.prefixFunctionTruncated()).isFalse();
    }

    @Test
    void quoteMatchRejectsInvalidInput() {
        assertThatThrownBy(() -> quotes.match(new QuoteMatchRequest("text", ""))).hasMessageContaining("empty");
        assertThatThrownBy(() -> quotes.match(new QuoteMatchRequest(null, "q"))).hasMessageContaining("required");
        assertThatThrownBy(() -> quotes.match(new QuoteMatchRequest("x".repeat(100_001), "x")))
                .hasMessageContaining("limited");
    }

    @Test
    void longQuotesExplainOnlyTheFirstSymbols() {
        QuoteMatchResponse r = quotes.match(new QuoteMatchRequest("a".repeat(200), "a".repeat(100)));
        assertThat(r.quoteSymbols()).hasSize(64);
        assertThat(r.prefixFunctionTruncated()).isTrue();
        assertThat(r.matches()).hasSize(101);
    }

    @Test
    void everyInsertedFingerprintIsConfirmedPresent() {
        List<String> known = List.of("POL-1", "POL-2", "LN-3");
        MembershipResponse r = membership.screen(new MembershipRequest(known, known, 0.01, null, null));
        assertThat(r.results()).allSatisfy(result -> {
            assertThat(result.bloomVerdict()).isEqualTo(BloomVerdict.POSSIBLY_PRESENT);
            assertThat(result.exactVerdict()).isEqualTo(ExactVerdict.CONFIRMED_PRESENT);
        });
        assertThat(r.parameters().seed()).isEqualTo(7);
    }

    @Test
    void possibleHitsOnUnknownProbesAreReportedAsFalsePositivesNeverAsPresent() {
        List<String> known = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            known.add("KNOWN-" + i);
        }
        List<String> probes = new ArrayList<>();
        for (int i = 0; i < 400; i++) {
            probes.add("UNKNOWN-" + i);
        }
        MembershipResponse r = membership.screen(new MembershipRequest(known, probes, 0.3, null, 7L));
        long falsePositives = r.results().stream()
                .filter(x -> x.exactVerdict() == ExactVerdict.FALSE_POSITIVE).count();
        assertThat(falsePositives).as("a 30%% filter must produce some false positives").isPositive();
        assertThat(r.results()).noneMatch(x -> x.exactVerdict() == ExactVerdict.CONFIRMED_PRESENT);
        assertThat(r.results()).filteredOn(x -> x.bloomVerdict() == BloomVerdict.DEFINITELY_ABSENT)
                .allMatch(x -> x.exactVerdict() == ExactVerdict.NOT_NEEDED);
        assertThat(r.bits()).hasSize((int) r.parameters().bitCount());
    }

    @Test
    void reportsSaturationWhenOverCapacity() {
        List<String> known = List.of("A", "B", "C", "D", "E");
        MembershipResponse r = membership.screen(new MembershipRequest(known, List.of("Z"), 0.01, 2L, null));
        assertThat(r.overCapacity()).isTrue();
        assertThat(r.expectedFalsePositiveRate()).isGreaterThan(0.01);
    }

    @Test
    void duplicateFingerprintsAreCountedAsAttemptsNotMembers() {
        List<String> submitted = List.of("CLAUSE-A", "CLAUSE-A", "CLAUSE-A", "CLAUSE-B");
        MembershipResponse r = membership.screen(new MembershipRequest(submitted, List.of("CLAUSE-A"), 0.01, null, null));
        assertThat(r.insertionAttempts()).isEqualTo(4);
        assertThat(r.distinctFingerprints()).isEqualTo(2);
        assertThat(r.duplicateAttempts()).isEqualTo(2);
        assertThat(r.parameters().expectedInsertions()).as("default capacity is the distinct count").isEqualTo(2);
        assertThat(r.overCapacity()).isFalse();
        com.thefineprint.cia.algorithms.membership.BloomParameters p =
                com.thefineprint.cia.algorithms.membership.BloomParameters.optimal(2, 0.01);
        assertThat(r.expectedFalsePositiveRate()).as("theory at 2 distinct members, not 4 attempts")
                .isEqualTo(p.theoreticalFalsePositiveRate(2));
        assertThat(r.expectedFalsePositiveRate()).isLessThan(p.theoreticalFalsePositiveRate(4));
    }

    @Test
    void repeatsAloneNeverMakeTheFilterOverCapacity() {
        List<String> submitted = java.util.Collections.nCopies(50, "SAME-CLAUSE");
        MembershipResponse r = membership.screen(new MembershipRequest(submitted, List.of("SAME-CLAUSE"), 0.01, 1L, null));
        assertThat(r.distinctFingerprints()).isEqualTo(1);
        assertThat(r.insertionAttempts()).isEqualTo(50);
        assertThat(r.overCapacity()).as("1 distinct member in a filter sized for 1").isFalse();
        assertThat(r.expectedFalsePositiveRate()).isLessThan(0.02);

        MembershipResponse over = membership.screen(new MembershipRequest(List.of("A", "B", "B"), List.of("A"), 0.01,
                1L, null));
        assertThat(over.overCapacity()).as("2 distinct members in a filter sized for 1").isTrue();
    }

    @Test
    void membershipRejectsInvalidInput() {
        assertThatThrownBy(() -> membership.screen(new MembershipRequest(List.of("a"), List.of(), 0.01, null, null)))
                .hasMessageContaining("probes");
        assertThatThrownBy(() -> membership.screen(new MembershipRequest(List.of("a"), List.of("a"), 0.9, null, null)))
                .hasMessageContaining("targetFalsePositiveRate");
        assertThatThrownBy(() -> membership.screen(new MembershipRequest(List.of(""), List.of("a"), 0.01, null, null)))
                .hasMessageContaining("characters");
        assertThatThrownBy(() -> membership.screen(new MembershipRequest(List.of("a"), List.of("a"), 0.01, 0L, null)))
                .hasMessageContaining("expectedInsertions");
    }
}
