package com.thefineprint.cia.demo.linking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.thefineprint.cia.demo.linking.DocumentLinkRequest.Document;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DocumentLinkServiceTest {

    private final DocumentLinkService service = new DocumentLinkService();

    private static Document doc(String id, String... identifiers) {
        return new Document(id, List.of(identifiers));
    }

    private DocumentLinkResponse link(Document... documents) {
        return service.link(new DocumentLinkRequest(List.of(documents)));
    }

    @Test
    void linksDocumentsTransitivelyThroughSharedIdentifiers() {
        // A and B share P-1; B and C share L-9: all three are one family, though A and C share nothing directly.
        DocumentLinkResponse r = link(doc("A", "P-1"), doc("B", "P-1", "L-9"), doc("C", "L-9"), doc("D", "X-5"));
        assertThat(r.groups()).hasSize(2);
        assertThat(r.groups().get(0).documentIds()).containsExactly("A", "B", "C");
        assertThat(r.groups().get(1).documentIds()).containsExactly("D");
        assertThat(r.stats().families()).isEqualTo(2);
        assertThat(r.stats().merges()).isEqualTo(2);
    }

    @Test
    void explainsEveryLinkWithTheIdentifierThatCausedIt() {
        DocumentLinkResponse r = link(doc("A", "P-1"), doc("B", "P-1"), doc("C", "Q-2"));
        assertThat(r.links()).hasSize(1);
        assertThat(r.links().get(0).identifier()).isEqualTo("P-1");
        assertThat(r.links().get(0).documentIds()).containsExactly("A", "B");
        assertThat(r.links().get(0).group()).isEqualTo(1);
    }

    @Test
    void documentsWithNoSharedIdentifierStayAlone() {
        DocumentLinkResponse r = link(doc("A", "P-1"), doc("B", "P-2"), doc("C"));
        assertThat(r.groups()).hasSize(3);
        assertThat(r.links()).isEmpty();
        assertThat(r.stats().unionCalls()).isZero();
    }

    @Test
    void anIdentifierRepeatedWithinOneDocumentLinksNothing() {
        DocumentLinkResponse r = link(doc("A", "P-1", "P-1"), doc("B", "P-2"));
        assertThat(r.groups()).hasSize(2);
        assertThat(r.stats().identifiers()).isEqualTo(3); // occurrences are counted
    }

    @Test
    void aWidelySharedIdentifierIsFlaggedAsBroad() {
        DocumentLinkResponse r = link(doc("A", "INSURER"), doc("B", "INSURER"), doc("C", "INSURER"),
                doc("D", "INSURER"), doc("E", "PRIVATE"), doc("F", "PRIVATE"));
        assertThat(r.groups()).hasSize(2);
        assertThat(r.links()).extracting(l -> l.identifier() + ":" + l.broad())
                .containsExactly("INSURER:true", "PRIVATE:false");
    }

    @Test
    void identifiersAreComparedExactly() {
        DocumentLinkResponse r = link(doc("A", "pol-1"), doc("B", "POL-1"), doc("C", "POL-1 "));
        assertThat(r.groups()).hasSize(3); // case and trailing space differ: no folding
    }

    @Test
    void aSingleDocumentIsAValidBoundaryCase() {
        DocumentLinkResponse r = link(doc("only", "X"));
        assertThat(r.groups()).hasSize(1);
        assertThat(r.stats().families()).isEqualTo(1);
    }

    @Test
    void resultIsTheSameWhateverTheOrderOfIdentifiers() {
        DocumentLinkResponse a = link(doc("A", "P", "Q"), doc("B", "Q"), doc("C", "P"));
        DocumentLinkResponse b = link(doc("A", "Q", "P"), doc("B", "Q"), doc("C", "P"));
        assertThat(a.groups()).isEqualTo(b.groups());
    }

    @Test
    void rejectsInvalidInputWithoutEchoingIt() {
        assertThatThrownBy(() -> service.link(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.link(new DocumentLinkRequest(List.of())))
                .hasMessageContaining("between 1 and 200 documents");
        assertThatThrownBy(() -> link(doc("A"), doc("A"))).hasMessageContaining("unique");
        assertThatThrownBy(() -> link(doc(" ", "x"))).hasMessageContaining("id");
        assertThatThrownBy(() -> link(doc("A", ""))).hasMessageContaining("identifier");
        assertThatThrownBy(() -> link(doc("secret-id", "x".repeat(101)))).hasMessageNotContaining("secret-id");

        List<Document> tooMany = new ArrayList<>();
        for (int i = 0; i < 201; i++) {
            tooMany.add(doc("d" + i));
        }
        assertThatThrownBy(() -> service.link(new DocumentLinkRequest(tooMany))).hasMessageContaining("200");

        String[] many = new String[21];
        java.util.Arrays.fill(many, 0, 21, "i");
        assertThatThrownBy(() -> link(doc("A", many))).hasMessageContaining("at most 20");
    }
}
