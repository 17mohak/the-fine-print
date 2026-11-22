package com.thefineprint.core.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.thefineprint.core.api.model.MatchClass;
import com.thefineprint.core.api.model.StatementKind;
import com.thefineprint.core.api.spi.DocumentEdge;
import com.thefineprint.core.api.spi.DocumentGroup;
import com.thefineprint.core.api.text.Span;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Structural checks of the shared types only. There is no behaviour to test in P0. */
class CoreApiTypesTest {

    @Test
    void spanRejectsImpossibleRanges() {
        assertThat(new Span(0, 0)).isEqualTo(new Span(0, 0));
        assertThatThrownBy(() -> new Span(-1, 2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Span(5, 4)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void documentGroupIsImmutableAndNonEmpty() {
        UUID a = UUID.randomUUID();
        List<DocumentEdge> reasons = new ArrayList<>();
        DocumentGroup group = new DocumentGroup(Set.of(a), reasons);
        reasons.add(new DocumentEdge(a, a, "TEST"));
        assertThat(group.reasons()).isEmpty();
        assertThatThrownBy(() -> group.documentIds().add(UUID.randomUUID()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> new DocumentGroup(Set.of(), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void vocabularyMatchesTheApprovedSpecification() {
        assertThat(StatementKind.values()).extracting(Enum::name).containsExactly(
                "DOCUMENT_FACT", "EXTRACTED_VALUE", "REGULATORY_FACT", "INTERPRETATION", "ASSUMPTION", "UNKNOWN");
        assertThat(MatchClass.values()).extracting(Enum::name).containsExactly(
                "EXACT_UNIQUE", "EXACT_MULTI_DECLARED", "AMBIGUOUS", "FUZZY_UNVERIFIED", "MISALIGNED",
                "LOW_CONFIDENCE");
    }
}
