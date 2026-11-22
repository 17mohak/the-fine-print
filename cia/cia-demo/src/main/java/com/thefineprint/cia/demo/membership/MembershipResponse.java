package com.thefineprint.cia.demo.membership;

import java.util.List;

/**
 * Result of one screen-then-confirm run.
 *
 * @param parameters the filter's sizing and seed
 * @param insertionAttempts fingerprints submitted, repeats included; each attempt is passed to the filter, and a
 *     repeat sets no new bits
 * @param distinctFingerprints distinct fingerprints: the effective membership of both the filter and the registry
 * @param duplicateAttempts {@code insertionAttempts - distinctFingerprints}
 * @param fillRatio fraction of bits set
 * @param expectedFalsePositiveRate theoretical rate at the number of <b>distinct</b> fingerprints
 * @param overCapacity whether there are more distinct fingerprints than the design capacity (saturation)
 * @param bits the bit pattern as "0"/"1", or {@code null} when the filter has more than 4,096 bits
 * @param results one verdict per probe, in request order
 */
public record MembershipResponse(Parameters parameters, int insertionAttempts, int distinctFingerprints,
                                 int duplicateAttempts, double fillRatio, double expectedFalsePositiveRate, boolean overCapacity,
                                 String bits, List<ProbeResult> results) {

    /** Filter sizing: {@code n}, {@code p}, {@code m}, {@code k}, {@code m/n} and the seed. */
    public record Parameters(long expectedInsertions, double targetFalsePositiveRate, long bitCount,
                             int hashCount, double bitsPerElement, long seed) {
    }

    /** What the filter said about one probe, and what the exact check found. */
    public record ProbeResult(String probe, BloomVerdict bloomVerdict, ExactVerdict exactVerdict,
                              List<Long> positions) {
    }

    /** The only two answers a Bloom filter can give. */
    public enum BloomVerdict {
        DEFINITELY_ABSENT,
        POSSIBLY_PRESENT
    }

    /** Outcome of the authoritative confirmation step. */
    public enum ExactVerdict {
        /** The filter already proved absence; no exact check was needed. */
        NOT_NEEDED,
        /** The filter said "possibly", and the exact registry confirms the fingerprint exists. */
        CONFIRMED_PRESENT,
        /** The filter said "possibly", but the fingerprint does not exist. */
        FALSE_POSITIVE
    }
}
