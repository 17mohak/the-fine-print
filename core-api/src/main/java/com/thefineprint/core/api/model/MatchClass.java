package com.thefineprint.core.api.model;

/**
 * Classification of an evidence anchor's match (SYSTEM_DESIGN 5.2 and 5.3). Values are fixed by the approved
 * specification. Only {@link #EXACT_UNIQUE} and {@link #EXACT_MULTI_DECLARED} can support a
 * {@link StatementKind#DOCUMENT_FACT}; the rule itself is enforced in P1, not here.
 */
public enum MatchClass {
    EXACT_UNIQUE,
    EXACT_MULTI_DECLARED,
    AMBIGUOUS,
    FUZZY_UNVERIFIED,
    MISALIGNED,
    LOW_CONFIDENCE
}
