package com.thefineprint.cia.algorithms.membership;

/**
 * Sizing of a Bloom filter: expected insertions {@code n}, target false-positive probability {@code p}, the
 * resulting number of bits {@code m} and hash functions {@code k}.
 *
 * <p>Standard formulas (assuming independent, uniform hashing):
 * <ul>
 *   <li>optimal bits: {@code m = ceil(-n * ln(p) / (ln 2)^2)}</li>
 *   <li>optimal hashes: {@code k = max(1, round((m / n) * ln 2))}</li>
 *   <li>false-positive probability after {@code x} insertions: {@code (1 - e^(-k x / m))^k}</li>
 * </ul>
 *
 * @param expectedInsertions the design capacity {@code n}; at least 1
 * @param targetFalsePositiveRate the design false-positive probability {@code p}; strictly between 0 and 1
 * @param bitCount {@code m}; at least 1
 * @param hashCount {@code k}; 1 to 64
 */
public record BloomParameters(long expectedInsertions, double targetFalsePositiveRate, long bitCount,
                              int hashCount) {

    private static final double LN2 = Math.log(2);
    static final long MAX_BITS = 64L * Integer.MAX_VALUE;

    public BloomParameters {
        if (expectedInsertions < 1) {
            throw new IllegalArgumentException("expectedInsertions must be at least 1");
        }
        if (!(targetFalsePositiveRate > 0.0 && targetFalsePositiveRate < 1.0)) {
            throw new IllegalArgumentException("targetFalsePositiveRate must be strictly between 0 and 1");
        }
        if (bitCount < 1 || bitCount > MAX_BITS) {
            throw new IllegalArgumentException("bitCount out of range: " + bitCount);
        }
        if (hashCount < 1 || hashCount > 64) {
            throw new IllegalArgumentException("hashCount must be between 1 and 64");
        }
    }

    /** Optimal {@code m} and {@code k} for {@code n} expected insertions at target probability {@code p}. */
    public static BloomParameters optimal(long expectedInsertions, double targetFalsePositiveRate) {
        if (expectedInsertions < 1) {
            throw new IllegalArgumentException("expectedInsertions must be at least 1");
        }
        if (!(targetFalsePositiveRate > 0.0 && targetFalsePositiveRate < 1.0)) {
            throw new IllegalArgumentException("targetFalsePositiveRate must be strictly between 0 and 1");
        }
        double bits = Math.ceil(-expectedInsertions * Math.log(targetFalsePositiveRate) / (LN2 * LN2));
        if (bits > MAX_BITS) {
            throw new IllegalArgumentException("filter would need " + bits + " bits; too large");
        }
        long m = Math.max(1, (long) bits);
        int k = (int) Math.max(1, Math.min(64, Math.round((double) m / expectedInsertions * LN2)));
        return new BloomParameters(expectedInsertions, targetFalsePositiveRate, m, k);
    }

    /**
     * Sizing by bits per element (used by the benchmarks): {@code m = n * bitsPerElement} and the optimal
     * {@code k} for that ratio. The target probability recorded is the theoretical one at capacity.
     */
    public static BloomParameters withBitsPerElement(long expectedInsertions, int bitsPerElement) {
        if (expectedInsertions < 1 || bitsPerElement < 1) {
            throw new IllegalArgumentException("expectedInsertions and bitsPerElement must be at least 1");
        }
        long m = Math.multiplyExact(expectedInsertions, (long) bitsPerElement);
        int k = (int) Math.max(1, Math.min(64, Math.round(bitsPerElement * LN2)));
        double p = theoreticalRate(m, k, expectedInsertions);
        return new BloomParameters(expectedInsertions, Math.min(Math.max(p, Double.MIN_VALUE), 0.999999), m, k);
    }

    /** Theoretical false-positive probability after {@code insertions} distinct insertions. */
    public double theoreticalFalsePositiveRate(long insertions) {
        return theoreticalRate(bitCount, hashCount, insertions);
    }

    private static double theoreticalRate(long m, int k, long insertions) {
        if (insertions <= 0) {
            return 0.0;
        }
        return Math.pow(1.0 - Math.exp(-(double) k * insertions / m), k);
    }

    /** Bits allocated per expected element ({@code m / n}). */
    public double bitsPerElement() {
        return (double) bitCount / expectedInsertions;
    }
}
