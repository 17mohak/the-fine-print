package com.thefineprint.cia.demo.evidence;

/**
 * @param sourceText synthetic source document, treated as already canonical (matched exactly as given); one clause
 *     per line
 * @param claimedQuotation the passage someone claims the document contains; must not be empty
 */
public record EvidenceCheckRequest(String sourceText, String claimedQuotation) {
}
