package com.thefineprint.cia.demo.evidence;

import com.thefineprint.core.api.model.MatchClass;
import java.util.List;

/**
 * Result of one evidence check: stage 1 (verify the quotation), stage 2 (screen clause wording against the reference
 * library) and the three-part evidence statement. All offsets are Unicode code points, half-open {@code [start, end)}.
 *
 * @param verification stage 1
 * @param screening stage 2
 * @param evidence what is verified, what is screened, and what is not established
 * @param clauses the clauses found in the source (one per non-blank line)
 * @param method how the result was produced, including its limitations
 */
public record EvidenceCheckResponse(Verification verification, Screening screening, Evidence evidence,
                                    List<Clause> clauses, List<String> method) {

    /** Overall outcome of stage 1. */
    public enum QuoteVerdict {
        /** Exactly one occurrence with established boundaries ({@link MatchClass#EXACT_UNIQUE}). */
        VERIFIED,
        /** More than one occurrence with established boundaries ({@link MatchClass#AMBIGUOUS}). */
        AMBIGUOUS,
        /** The code points occur, but no occurrence has established character boundaries
         *  ({@link MatchClass#MISALIGNED}). */
        BOUNDARY_NOT_ESTABLISHED,
        /** No exact occurrence. */
        NOT_FOUND
    }

    /**
     * @param verdict overall outcome
     * @param matchClass the {@code core-api} classification, or {@code null} when nothing was found
     * @param algorithm what produced the occurrences
     * @param sourceLength n, in code points
     * @param quotationLength m, in code points
     * @param occurrences every exact occurrence the {@code QuoteVerifier} returned, accepted or not
     * @param nearMisses hints when nothing was found (never evidence)
     */
    public record Verification(QuoteVerdict verdict, MatchClass matchClass, String algorithm, int sourceLength,
                               int quotationLength, List<Occurrence> occurrences, List<String> nearMisses) {
    }

    /**
     * @param boundariesEstablished whether both boundaries passed the conservative boundary check
     * @param boundaryIssues why not, when they did not
     * @param clauseIds clauses this occurrence overlaps
     */
    public record Occurrence(int start, int end, boolean boundariesEstablished, List<String> boundaryIssues,
                             List<String> clauseIds) {
    }

    /** Stage 2: every clause is screened; the ones holding an accepted occurrence are marked. */
    public record Screening(Library library, List<ClauseScreen> clauses, int definitelyAbsent, int exactLookups,
                            int confirmedMatches, int falsePositivesRejected) {
    }

    /** The reference library and its Bloom filter. */
    public record Library(int referenceClauses, long bitCount, int hashCount, double targetFalsePositiveRate,
                          double expectedFalsePositiveRate, double fillRatio, long bloomBytes, long seed) {
    }

    /** What the Bloom filter said about a clause's wording, and what the exact comparison found. */
    public enum BloomVerdict {
        DEFINITELY_ABSENT,
        POSSIBLY_PRESENT
    }

    /** Outcome of the exact comparison. */
    public enum ExactVerdict {
        /** The filter proved absence, so no exact lookup was needed. */
        NOT_NEEDED,
        /** The library holds a reference entry with identical wording. */
        IDENTICAL_WORDING,
        /** The filter said "possibly", but no reference wording is identical: a false positive. */
        FALSE_POSITIVE
    }

    /**
     * @param containsQuotation whether an accepted occurrence of the quotation lies in this clause
     * @param referenceId the matching reference entry, when the wording is identical
     */
    public record ClauseScreen(String clauseId, boolean containsQuotation, BloomVerdict bloomVerdict,
                               ExactVerdict exactVerdict, String referenceId, String referenceDomain,
                               String referenceTitle) {
    }

    /** A clause of the source. */
    public record Clause(String id, int start, int end, String wording) {
    }

    /** The three-part evidence statement. */
    public record Evidence(Part verified, Part screened, List<String> notEstablished) {
    }

    /** One part: a short status label and the plain-language statements behind it. */
    public record Part(String status, List<String> statements) {
    }
}
