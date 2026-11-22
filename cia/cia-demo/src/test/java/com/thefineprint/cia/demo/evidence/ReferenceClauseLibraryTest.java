package com.thefineprint.cia.demo.evidence;

import static org.assertj.core.api.Assertions.assertThat;

import com.thefineprint.cia.demo.evidence.ReferenceClauseCorpus.ReferenceClause;
import com.thefineprint.cia.demo.evidence.ReferenceClauseLibrary.Screen;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ReferenceClauseLibraryTest {

    private static final ReferenceClauseLibrary LIBRARY = new ReferenceClauseLibrary();

    @Test
    void corpusIsDeterministicDistinctAndDisjointFromItsHeldOutVariants() {
        ReferenceClauseCorpus.Generated again = ReferenceClauseCorpus.generate();
        assertThat(again.library()).isEqualTo(LIBRARY.entries());
        Set<String> wordings = new HashSet<>();
        for (ReferenceClause entry : LIBRARY.entries()) {
            assertThat(wordings.add(entry.wording())).as(entry.id()).isTrue();
        }
        assertThat(LIBRARY.heldOut()).noneMatch(h -> wordings.contains(h.wording()));
        // Sizes are documented in docs/cia/DATA.md; update both together.
        assertThat(LIBRARY.entries()).hasSize(EXPECTED_LIBRARY_SIZE);
        assertThat(LIBRARY.heldOut()).hasSize(EXPECTED_HELD_OUT_SIZE);
    }

    static final int EXPECTED_LIBRARY_SIZE = 477;
    static final int EXPECTED_HELD_OUT_SIZE = 118;

    @Test
    void corpusContainsClauseWordingNotIdentifiers() {
        assertThat(LIBRARY.entries()).allSatisfy(e -> {
            assertThat(e.wording()).doesNotMatch(".*[A-Z]{2,}-\\d.*"); // no policy-number-like identifiers
            assertThat(e.wording().codePointCount(0, e.wording().length())).isGreaterThan(20);
        });
        assertThat(LIBRARY.entries()).extracting(ReferenceClause::domain)
                .contains("Insurance", "Lending", "Tenancy", "Subscriptions", "Warranties", "Insurance (Hindi)");
    }

    @Test
    void everyReferenceWordingScreensAsAnIdenticalMatchNoFalseNegatives() {
        for (ReferenceClause entry : LIBRARY.entries()) {
            ReferenceClauseLibrary.Outcome outcome = LIBRARY.screen(entry.wording());
            assertThat(outcome.screen()).as(entry.id()).isEqualTo(Screen.EXACT_MATCH_CONFIRMED);
            assertThat(outcome.reference()).contains(entry);
        }
    }

    @Test
    void heldOutVariantsAreNeverReportedAsMatches() {
        for (ReferenceClause variant : LIBRARY.heldOut()) {
            assertThat(LIBRARY.screen(variant.wording()).screen()).as(variant.id())
                    .isNotEqualTo(Screen.EXACT_MATCH_CONFIRMED);
        }
    }

    @Test
    void aOneCharacterDifferenceIsNotAMatch() {
        String wording = LIBRARY.entries().get(0).wording();
        for (String variant : List.of(wording + " ", wording.replace('.', ','), wording.toUpperCase(java.util.Locale.ROOT),
                wording.substring(0, wording.length() - 1))) {
            assertThat(LIBRARY.screen(variant).screen()).isNotEqualTo(Screen.EXACT_MATCH_CONFIRMED);
        }
    }

    @Test
    void bloomPossibleHitsThatAreNotMembersAreRejectedByExactComparison() {
        // Deterministic search for non-members the filter answers "possibly present" for (seed 7, target 1 %).
        int rejected = 0;
        for (int i = 0; i < 5_000; i++) {
            ReferenceClauseLibrary.Outcome outcome = LIBRARY.screen("Synthetic non-member clause number " + i + ".");
            assertThat(outcome.screen()).isNotEqualTo(Screen.EXACT_MATCH_CONFIRMED);
            if (outcome.screen() == Screen.FALSE_POSITIVE_REJECTED) {
                assertThat(outcome.reference()).isEmpty();
                rejected++;
            }
        }
        assertThat(rejected).as("about 1 %% of 5,000 probes should be Bloom false positives").isBetween(15, 100);
    }

    @Test
    void filterIsSizedForTheDistinctLibrary() {
        assertThat(LIBRARY.parameters().expectedInsertions()).isEqualTo(LIBRARY.entries().size());
        assertThat(LIBRARY.expectedFalsePositiveRate()).isBetween(0.005, 0.015);
        assertThat(LIBRARY.fillRatio()).isBetween(0.4, 0.6);
    }
}
