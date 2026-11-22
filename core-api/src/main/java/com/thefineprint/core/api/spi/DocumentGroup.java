package com.thefineprint.core.api.spi;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * One family of linked documents returned by {@link DocumentLinker}.
 *
 * @param documentIds members of the group; non-empty
 * @param reasons the edges connecting the members (empty for a singleton)
 */
public record DocumentGroup(Set<UUID> documentIds, List<DocumentEdge> reasons) {

    public DocumentGroup {
        documentIds = Set.copyOf(documentIds);
        reasons = List.copyOf(reasons);
        if (documentIds.isEmpty()) {
            throw new IllegalArgumentException("a document group has at least one member");
        }
    }
}
