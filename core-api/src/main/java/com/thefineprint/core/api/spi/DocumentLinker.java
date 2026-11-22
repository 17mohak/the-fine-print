package com.thefineprint.core.api.spi;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Groups one user's documents into families (one agreement) from identifier links
 * (SYSTEM_DESIGN section 3, cross-document relations; CIA matrix A4).
 *
 * <p><b>Contract</b>
 * <ul>
 *   <li>The result is exactly the connected components of the undirected graph whose nodes are
 *       {@code documents} and whose edges are {@code edges}. Every document appears in exactly one group;
 *       a document with no edges forms a singleton group.</li>
 *   <li>Each group lists the edges that connect its members, so the grouping can be explained to the user.</li>
 *   <li>An edge that names a document not in {@code documents} is rejected with
 *       {@link IllegalArgumentException}.</li>
 *   <li>Groupings are <i>suggestions</i>: the user may unlink, and nothing consequential is applied silently.
 *       Callers pass a single user's documents only; tenant scoping is outside this interface.</li>
 * </ul>
 *
 * <p><b>Implementations:</b> none in P0. The production default is simple connected components (P1+);
 * assessed implementations are Track C only.
 */
public interface DocumentLinker {

    /**
     * @param documents all document ids to group; no duplicates
     * @param edges identifier links between those documents
     * @return the groups; never {@code null}
     */
    List<DocumentGroup> group(Collection<UUID> documents, Collection<DocumentEdge> edges);
}
