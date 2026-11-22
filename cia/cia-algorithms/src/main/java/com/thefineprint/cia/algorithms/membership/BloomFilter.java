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
 * <p><b>Bit positions (double hashing).</b> Two independent 64-bit hashes {@code h1} and {@code h2} are computed
 * once per key and turned into {@code k} positions by {@code ProbeSequence}: start {@code h1 mod m}, a step
 * derived from {@code h2} that is <i>coprime with m</i>, and {@code g_(i+1) = (g_i + step) mod m}. Because the
 * step is coprime with {@code m}, a key's first {@code min(k, m)} positions are always distinct, for any
 * {@code m}, and the arithmetic is reduced modulo {@code m} before it can overflow. Two hash passes replace
 * {@code k}. The textbook false-positive formula assumes independent uniform positions; this scheme approximates
 * it, and the measured rates are reported in {@code docs/cia/BENCHMARKS.md}.
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
        long m = parameters.bitCount();
        long position = ProbeSequence.start(SeededHash64.hash(key, seed1), m);
        long step = ProbeSequence.step(SeededHash64.hash(key, seed2), m);
        for (int i = 0; i < parameters.hashCount(); i++) {
            bits.set(position);
            position = ProbeSequence.next(position, step, m);
        }
        insertions++;
    }

    @Override
    public boolean mightContain(byte[] key) {
        Objects.requireNonNull(key, "key");
        long m = parameters.bitCount();
        long position = ProbeSequence.start(SeededHash64.hash(key, seed1), m);
        long step = ProbeSequence.step(SeededHash64.hash(key, seed2), m);
        for (int i = 0; i < parameters.hashCount(); i++) {
            if (!bits.get(position)) {
                return false; // one clear bit proves the key was never added
            }
            position = ProbeSequence.next(position, step, m);
        }
        return true;
    }

    /** The {@code k} bit positions for {@code key}, in probe order (for explanation and tests). */
    public long[] positionsOf(byte[] key) {
        Objects.requireNonNull(key, "key");
        long[] positions = new long[parameters.hashCount()];
        ProbeSequence.positions(SeededHash64.hash(key, seed1), SeededHash64.hash(key, seed2),
                parameters.bitCount(), positions);
        return positions;
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
