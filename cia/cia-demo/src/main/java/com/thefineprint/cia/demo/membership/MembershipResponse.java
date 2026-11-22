package com.thefineprint.cia.demo.membership;

import java.util.List;

/**
 * Result of one screen-then-confirm run.
 *
 * @param parameters the filter's sizing and seed
 * @param insertions number of fingerprints added (duplicates included)
 * @param distinctFingerprints number of distinct fingerprints in the exact registry
 * @param fillRatio fraction of bits set
 * @param expectedFalsePositiveRate theoretical rate at the current insertion count
 * @param overCapacity whether more fingerprints were added than the design capacity (saturation)
 * @param bits the bit pattern as "0"/"1", or {@code null} when the filter has more than 4,096 bits
 * @param results one verdict per probe, in request order
 */
public record MembershipResponse(Parameters parameters, long insertions, int distinctFingerprints,
                                 double fillRatio, double expectedFalsePositiveRate, boolean overCapacity,
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
