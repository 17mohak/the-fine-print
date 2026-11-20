package com.thefineprint.core.api.spi;

import java.util.Objects;
import java.util.UUID;

/**
 * An undirected link between two documents, used by {@link DocumentLinker}.
 *
 * @param from one document id
 * @param to the other document id
 * @param reasonCode opaque, non-sensitive code naming why they are linked (for example an identifier
 *     <i>type</i>). It must never contain the identifier value itself.
 */
public record DocumentEdge(UUID from, UUID to, String reasonCode) {

    public DocumentEdge {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(reasonCode, "reasonCode");
    }
}
