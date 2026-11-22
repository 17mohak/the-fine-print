package com.thefineprint.cia.demo.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.thefineprint.cia.algorithms.text.KmpQuoteVerifier;
import com.thefineprint.cia.demo.evidence.EvidenceCheckResponse.BloomVerdict;
import com.thefineprint.cia.demo.evidence.EvidenceCheckResponse.ClauseScreen;
import com.thefineprint.cia.demo.evidence.EvidenceCheckResponse.ExactVerdict;
import com.thefineprint.cia.demo.evidence.EvidenceCheckResponse.Occurrence;
import com.thefineprint.cia.demo.evidence.EvidenceCheckResponse.QuoteVerdict;
import com.thefineprint.core.api.model.MatchClass;
import com.thefineprint.core.api.spi.QuoteVerifier;
import com.thefineprint.core.api.text.Span;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EvidenceCheckServiceTest {

    static final String POLICY = String.join("\n",
            "SCHEDULE OF BENEFITS (synthetic example)",
            "4.1 Grace period. A grace period of 30 days is allowed for payment of the renewal premium.",
            "4.2 Notice of claim. The Policyholder shall give written notice of any claim within 30 days of discharge.",
            "4.3 Renewal. The Insurer may decline renewal only on grounds of fraud or non-disclosure.",
            "4.4 Waiting period. Pre-existing conditions are covered after 36 months of continuous coverage.");

    static final String HINDI = String.join("\n",
            "नियम और शर्तें (synthetic example)",
            "5.1 बीमित व्यक्ति को दावे की सूचना 30 दिनों के भीतर देनी होगी।",
            "5.2 पॉलिसी का नवीनीकरण केवल धोखाधड़ी के आधार पर अस्वीकार किया जा सकता है।");

    private static final ReferenceClauseLibrary LIBRARY = new ReferenceClauseLibrary();

    /** Records every SPI call, to prove the workflow goes through the contract. */
    private static final class RecordingVerifier implements QuoteVerifier {
        final List<String> quotes = new ArrayList<>();
        private final QuoteVerifier delegate = new KmpQuoteVerifier();

        @Override
        public List<Span> find(String l1, String normalisedQuote) {
            quotes.add(normalisedQuote);
            return delegate.find(l1, normalisedQuote);
        }
    }

    private final RecordingVerifier verifier = new RecordingVerifier();
    private final EvidenceCheckService service = new EvidenceCheckService(verifier, LIBRARY);

    private EvidenceCheckResponse check(String source, String quote) {
        return service.check(new EvidenceCheckRequest(source, quote));
    }

    private static ClauseScreen clause(EvidenceCheckResponse r, String id) {
        return r.screening().clauses().stream().filter(c -> c.clauseId().equals(id)).findFirst().orElseThrow();
    }

    @Test
    void verifiedQuotationIsLocatedAndItsClauseScreenedAgainstTheLibrary() {
        EvidenceCheckResponse r = check(POLICY, "The Policyholder shall give written notice of any claim within 30 days");

        assertThat(r.verification().verdict()).isEqualTo(QuoteVerdict.VERIFIED);
        assertThat(r.verification().matchClass()).isEqualTo(MatchClass.EXACT_UNIQUE);
        Occurrence o = r.verification().occurrences().get(0);
        assertThat(o.clauseIds()).containsExactly("4.2");
        int[] cps = POLICY.codePoints().toArray();
        assertThat(new String(cps, o.start(), o.end() - o.start()))
                .isEqualTo("The Policyholder shall give written notice of any claim within 30 days");

        ClauseScreen quoted = clause(r, "4.2");
        assertThat(quoted.containsQuotation()).isTrue();
        assertThat(quoted.bloomVerdict()).isEqualTo(BloomVerdict.POSSIBLY_PRESENT);
        assertThat(quoted.exactVerdict()).isEqualTo(ExactVerdict.IDENTICAL_WORDING);
        assertThat(quoted.referenceId()).isEqualTo("INS-CLAIM-NOTICE-025");

        assertThat(r.evidence().verified().status()).isEqualTo("Occurs exactly once");
        assertThat(r.evidence().verified().statements().get(0)).contains("exactly once", "clause 4.2");
        assertThat(r.evidence().screened().status()).isEqualTo("Identical reference wording");
        assertThat(r.evidence().screened().statements().get(0)).contains("INS-CLAIM-NOTICE-025", "exact comparison");
        assertThat(r.evidence().notEstablished())
                .contains(EvidenceCheckService.EQUIVALENCE_CAVEAT)
                .anyMatch(s -> s.contains("legally valid"))
                .anyMatch(s -> s.contains("authentic"))
                .anyMatch(s -> s.contains("current version"))
                .anyMatch(s -> s.contains("means"))
                .anyMatch(s -> s.contains("applies"));
        assertThat(verifier.quotes).as("stage 1 went through the QuoteVerifier SPI")
                .contains("The Policyholder shall give written notice of any claim within 30 days");
    }

    @Test
    void everyClauseIsScreenedAndPossibleHitsAreAlwaysExactlyCompared() {
        EvidenceCheckResponse r = check(POLICY, "Renewal.");
        assertThat(r.screening().clauses()).extracting(ClauseScreen::clauseId)
                .containsExactly("line 1", "4.1", "4.2", "4.3", "4.4");
        assertThat(clause(r, "4.1").exactVerdict()).isEqualTo(ExactVerdict.IDENTICAL_WORDING);
        assertThat(clause(r, "4.3").exactVerdict()).isEqualTo(ExactVerdict.IDENTICAL_WORDING);
        assertThat(clause(r, "4.4").exactVerdict()).as("held-out variant, not in the library")
                .isNotEqualTo(ExactVerdict.IDENTICAL_WORDING);
        assertThat(r.screening().clauses()).allSatisfy(c -> {
            if (c.bloomVerdict() == BloomVerdict.DEFINITELY_ABSENT) {
                assertThat(c.exactVerdict()).isEqualTo(ExactVerdict.NOT_NEEDED);
            } else {
                assertThat(c.exactVerdict()).isIn(ExactVerdict.IDENTICAL_WORDING, ExactVerdict.FALSE_POSITIVE);
            }
        });
        assertThat(r.screening().exactLookups() + r.screening().definitelyAbsent()).isEqualTo(5);
        assertThat(r.screening().exactLookups())
                .isEqualTo(r.screening().confirmedMatches() + r.screening().falsePositivesRejected());
    }

    @Test
    void aBloomFalsePositiveOnTheQuotedClauseIsReportedAsNoMatch() {
        String wording = firstFalsePositiveWording();
        EvidenceCheckResponse r = check("7.1 " + wording, wording);
        assertThat(r.verification().verdict()).isEqualTo(QuoteVerdict.VERIFIED);
        ClauseScreen screen = clause(r, "7.1");
        assertThat(screen.bloomVerdict()).isEqualTo(BloomVerdict.POSSIBLY_PRESENT);
        assertThat(screen.exactVerdict()).isEqualTo(ExactVerdict.FALSE_POSITIVE);
        assertThat(screen.referenceId()).isNull();
        assertThat(r.evidence().screened().status()).isEqualTo("No identical reference wording");
        assertThat(r.evidence().screened().statements().get(0)).contains("false positive", "not a match");
        assertThat(r.evidence().notEstablished()).doesNotContain(EvidenceCheckService.EQUIVALENCE_CAVEAT);
    }

    /**
     * Pins the UI's "looks standard, but is not" example: the library holds the 15- and 21-day grace-period wordings,
     * not 17 days, and with seed 7 the Bloom filter answers "possibly present" for the 17-day wording. If the corpus or
     * seed changes, this test fails and the example in app.js must be updated with it.
     */
    @Test
    void uiNearVariantExampleIsAResolvedFalsePositive() {
        String wording = "Grace period. A grace period of 17 days is allowed for payment of the renewal premium.";
        EvidenceCheckResponse r = check("4.1 " + wording, "A grace period of 17 days");
        assertThat(r.verification().verdict()).isEqualTo(QuoteVerdict.VERIFIED);
        assertThat(clause(r, "4.1").bloomVerdict()).isEqualTo(BloomVerdict.POSSIBLY_PRESENT);
        assertThat(clause(r, "4.1").exactVerdict()).isEqualTo(ExactVerdict.FALSE_POSITIVE);
        assertThat(LIBRARY.screen(wording.replace("17", "15")).screen())
                .isEqualTo(ReferenceClauseLibrary.Screen.EXACT_MATCH_CONFIRMED);
    }

    /** The discovered defect: "न" matched inside "नि" and was reported as a verified occurrence. */
    @Test
    void subSyllableOccurrenceIsRejectedAndOnlyTheWholeSyllableCounts() {
        EvidenceCheckResponse r = check("नियम न", "न");
        assertThat(r.verification().occurrences()).hasSize(2);
        Occurrence inside = r.verification().occurrences().get(0);
        assertThat(inside.start()).isZero();
        assertThat(inside.boundariesEstablished()).isFalse();
        assertThat(inside.boundaryIssues()).singleElement().asString().contains("U+093F");
        Occurrence standalone = r.verification().occurrences().get(1);
        assertThat(standalone.start()).isEqualTo(5);
        assertThat(standalone.boundariesEstablished()).isTrue();
        assertThat(r.verification().matchClass()).isEqualTo(MatchClass.EXACT_UNIQUE);
        assertThat(r.evidence().verified().statements()).anyMatch(s -> s.contains("1 further occurrence was rejected"));
    }

    @Test
    void quotationThatOnlyOccursInsideSyllablesIsNotVerified() {
        EvidenceCheckResponse r = check("नियम", "न");
        assertThat(r.verification().verdict()).isEqualTo(QuoteVerdict.BOUNDARY_NOT_ESTABLISHED);
        assertThat(r.verification().matchClass()).isEqualTo(MatchClass.MISALIGNED);
        assertThat(r.evidence().verified().status()).isEqualTo("Not verified");
        assertThat(r.evidence().screened().status()).isEqualTo("Not applicable");
        assertThat(r.screening().clauses()).noneMatch(ClauseScreen::containsQuotation);
    }

    @Test
    void viramaConjunctAndVowelSignSplitsAreNotVerified() {
        assertThat(check("क्षमा", "क").verification().matchClass()).as("before the virama").isEqualTo(MatchClass.MISALIGNED);
        assertThat(check("क्षमा", "क्").verification().matchClass()).as("after the virama").isEqualTo(MatchClass.MISALIGNED);
        assertThat(check("क्षमा", "षमा").verification().matchClass()).as("consonant after virama")
                .isEqualTo(MatchClass.MISALIGNED);
        assertThat(check(HINDI, "दाव").verification().matchClass()).as("vowel sign dropped").isEqualTo(MatchClass.MISALIGNED);
        assertThat(check(HINDI, "शर्त").verification().matchClass()).as("conjunct word without its vowel sign")
                .isEqualTo(MatchClass.MISALIGNED);
        assertThat(check("क्‍ष", "क्").verification().matchClass()).as("ZWJ").isEqualTo(MatchClass.MISALIGNED);
    }

    @Test
    void wholeDevanagariQuotationIsVerifiedAndScreened() {
        EvidenceCheckResponse r = check(HINDI, "दावे की सूचना 30 दिनों के भीतर");
        assertThat(r.verification().verdict()).isEqualTo(QuoteVerdict.VERIFIED);
        assertThat(r.verification().occurrences().get(0).clauseIds()).containsExactly("5.1");
        assertThat(clause(r, "5.1").exactVerdict()).isEqualTo(ExactVerdict.IDENTICAL_WORDING);
        assertThat(clause(r, "5.1").referenceId()).startsWith("HI-INS-CLAIM-NOTICE-");
        assertThat(clause(r, "5.2").exactVerdict()).isEqualTo(ExactVerdict.IDENTICAL_WORDING);
    }

    @Test
    void uncoveredScriptBoundaryIsNotPresentedAsVerified() {
        EvidenceCheckResponse r = check("กา", "ก");
        assertThat(r.verification().verdict()).isEqualTo(QuoteVerdict.BOUNDARY_NOT_ESTABLISHED);
        assertThat(r.verification().occurrences().get(0).boundaryIssues().get(0)).contains("does not cover");
    }

    @Test
    void repeatedQuotationIsAmbiguousNotVerified() {
        EvidenceCheckResponse r = check(POLICY, "30 days");
        assertThat(r.verification().verdict()).isEqualTo(QuoteVerdict.AMBIGUOUS);
        assertThat(r.verification().matchClass()).isEqualTo(MatchClass.AMBIGUOUS);
        assertThat(r.verification().occurrences()).hasSize(2);
        assertThat(r.evidence().verified().statements().get(0)).contains("2 times", "4.1", "4.2", "cannot be established");
        assertThat(r.screening().clauses()).filteredOn(ClauseScreen::containsQuotation)
                .extracting(ClauseScreen::clauseId).containsExactly("4.1", "4.2");
    }

    @Test
    void notFoundExplainsNormalisationAndCaseNearMissesWithoutVerifying() {
        EvidenceCheckResponse nfd = check("café terms", "café");
        assertThat(nfd.verification().verdict()).isEqualTo(QuoteVerdict.NOT_FOUND);
        assertThat(nfd.verification().matchClass()).isNull();
        assertThat(nfd.verification().nearMisses()).singleElement().asString().contains("NFC");
        assertThat(nfd.evidence().verified().status()).isEqualTo("Not found");

        EvidenceCheckResponse cased = check(POLICY, "the policyholder shall give");
        assertThat(cased.verification().verdict()).isEqualTo(QuoteVerdict.NOT_FOUND);
        assertThat(cased.verification().nearMisses()).anyMatch(s -> s.contains("case"));

        assertThat(check(POLICY, "absent wording").verification().nearMisses()).isEmpty();
    }

    @Test
    void quotationSpanningTwoClausesBelongsToBoth() {
        EvidenceCheckResponse r = check(POLICY, "of discharge.\n4.3 Renewal.");
        assertThat(r.verification().verdict()).isEqualTo(QuoteVerdict.VERIFIED);
        assertThat(r.verification().occurrences().get(0).clauseIds()).containsExactly("4.2", "4.3");
    }

    @Test
    void rejectsInvalidInput() {
        assertThatThrownBy(() -> check(POLICY, "")).hasMessageContaining("must not be empty");
        assertThatThrownBy(() -> check(null, "q")).hasMessageContaining("required");
        assertThatThrownBy(() -> check("x".repeat(100_001), "x")).hasMessageContaining("limited");
        assertThatThrownBy(() -> check("x", "x".repeat(2_001))).hasMessageContaining("limited");
        assertThatThrownBy(() -> check("a\n".repeat(401), "a")).hasMessageContaining("clauses");
        assertThat(check("", "x").verification().verdict()).isEqualTo(QuoteVerdict.NOT_FOUND);
    }

    /** A natural-looking wording that is not in the library but that the Bloom filter answers "possibly" for. */
    static String firstFalsePositiveWording() {
        for (int i = 0; i < 10_000; i++) {
            String candidate = "Synthetic non-member clause number " + i + ".";
            if (LIBRARY.screen(candidate).screen() == ReferenceClauseLibrary.Screen.FALSE_POSITIVE_REJECTED) {
                return candidate;
            }
        }
        throw new AssertionError("no false positive found");
    }
}
