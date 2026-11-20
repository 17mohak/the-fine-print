package com.thefineprint.cia.algorithms.membership;

import com.thefineprint.core.api.spi.ApproximateMembership;
import java.util.Objects;

/**
 * A Bloom filter: a compact, probabilistic set that answers "definitely not present" or "possibly present".
 *
 * <p><b>Guarantees.</b> No false negatives: every key that was added is reported as possibly present. False
 * positives are possible, at roughly {@link BloomParameters#theoreticalFalsePositiveRate} once {@code n} keys are
 * in. A positive answer is <b>never proof of membership</b>; callers must confirm it with an exact check.
 *
 * <p><b>Bit positions (double hashing, Kirsch–Mitzenmacher).</b> Two independent 64-bit hashes {@code h1} and
 * {@code h2} are computed once per key. The {@code i}-th position is {@code (h1 + i * h2) mod m} for
 * {@code i = 0 .. k-1}. {@code h2} is forced odd so the sequence does not collapse when it shares a factor with
 * {@code m}. This gives the asymptotic false-positive rate of {@code k} independent hashes for the cost of two.
 *
 * <p><b>Limitations.</b> No deletion (clearing a bit could erase other keys). The false-positive rate degrades
 * once insertions exceed the design capacity: saturation, reported by {@link #fillRatio()}. Duplicate additions
 * are counted in {@link #insertionCount()}. The filter is not thread-safe.
 */
public final class BloomFilter implements ApproximateMembership {

    /** Golden-ratio constant used to derive the second seed from the first. */
    private static final long SECOND_SEED_SALT = 0x9E3779B97F4A7C15L;

    private final BloomParameters parameters;
    private final BitArray bits;
    private final long seed1;
    private final long seed2;
    private long insertions;

    /**
     * @param parameters sizing ({@code m}, {@code k})
     * @param seed hash seed; the same seed and inputs always give the same bit pattern (reproducible tests)
     */
    public BloomFilter(BloomParameters parameters, long seed) {
        this.parameters = Objects.requireNonNull(parameters, "parameters");
        this.bits = new BitArray(parameters.bitCount());
        this.seed1 = seed;
        this.seed2 = seed ^ SECOND_SEED_SALT;
    }

    @Override
    public void add(byte[] key) {
        Objects.requireNonNull(key, "key");
        long h1 = SeededHash64.hash(key, seed1);
        long h2 = SeededHash64.hash(key, seed2) | 1L;
        for (int i = 0; i < parameters.hashCount(); i++) {
            bits.set(position(h1, h2, i));
        }
        insertions++;
    }

    @Override
    public boolean mightContain(byte[] key) {
        Objects.requireNonNull(key, "key");
        long h1 = SeededHash64.hash(key, seed1);
        long h2 = SeededHash64.hash(key, seed2) | 1L;
        for (int i = 0; i < parameters.hashCount(); i++) {
            if (!bits.get(position(h1, h2, i))) {
                return false; // one clear bit proves the key was never added
            }
        }
        return true;
    }

    /** The {@code k} bit positions for {@code key}, in probe order (for explanation and tests). */
    public long[] positionsOf(byte[] key) {
        Objects.requireNonNull(key, "key");
        long h1 = SeededHash64.hash(key, seed1);
        long h2 = SeededHash64.hash(key, seed2) | 1L;
        long[] positions = new long[parameters.hashCount()];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = position(h1, h2, i);
        }
        return positions;
    }

    /** {@code (h1 + i * h2) mod m}, computed on unsigned 64-bit values. */
    private long position(long h1, long h2, int i) {
        return Long.remainderUnsigned(h1 + i * h2, parameters.bitCount());
    }

    public BloomParameters parameters() {
        return parameters;
    }

    /** Number of {@link #add} calls, including duplicates. */
    public long insertionCount() {
        return insertions;
    }

    /** Fraction of bits set. Near 0.5 at design capacity with optimal {@code k}; approaching 1 means saturated. */
    public double fillRatio() {
        return (double) bits.bitsSet() / bits.size();
    }

    /** Theoretical false-positive probability at the current insertion count. */
    public double expectedFalsePositiveRate() {
        return parameters.theoreticalFalsePositiveRate(insertions);
    }

    /** Whether bit {@code index} is set (for visualising the filter). */
    public boolean isBitSet(long index) {
        return bits.get(index);
    }

    /** Payload memory of the bit array, in bytes. */
    public long memoryBytes() {
        return bits.payloadBytes();
    }
}
