package com.thefineprint.core.api.model;

/**
 * The six kinds every user-visible statement must carry (PROJECT_FOUNDATION section 4; SYSTEM_DESIGN 5.1).
 * Values are fixed by the approved specification.
 */
public enum StatementKind {
    /** Verbatim text of the user's document, backed by a verified unique anchor. */
    DOCUMENT_FACT,
    /** A value read from the document (amount, date, percentage). */
    EXTRACTED_VALUE,
    /** External legal or regulatory text, with host class and verification state. */
    REGULATORY_FACT,
    /** An inference; never the sole support for another statement. */
    INTERPRETATION,
    /** Supplied by the user or a declared default. */
    ASSUMPTION,
    /** Missing or insufficient information. */
    UNKNOWN
}
