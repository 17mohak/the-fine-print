package com.thefineprint.cia.demo.linking;

import java.util.List;

/** Document families found with the assessed disjoint-set forest, with the reasons for every link. */
public record DocumentLinkResponse(String algorithm, Stats stats, List<Group> groups, List<SharedIdentifier> links,
                                   List<String> caveats, long serverMicros) {

    /**
     * @param documents documents submitted
     * @param identifiers identifier occurrences submitted
     * @param unionCalls union operations performed (one per additional document sharing an identifier)
     * @param merges unions that actually merged two families
     * @param families resulting number of groups (singletons included)
     * @param parentHops parent-pointer steps taken by find, summed over all operations
     */
    public record Stats(int documents, int identifiers, int unionCalls, int merges, int families, long parentHops) {
    }

    /** One family: members in submission order. A family of one is a document that shares nothing. */
    public record Group(int number, List<String> documentIds) {
    }

    /**
     * An identifier held by two or more documents: the reason those documents are linked.
     *
     * @param broad true if four or more documents share it; a widely shared identifier may over-merge unrelated
     *     documents and should be reviewed
     */
    public record SharedIdentifier(String identifier, List<String> documentIds, int group, boolean broad) {
    }
}
