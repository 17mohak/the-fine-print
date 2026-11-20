package com.thefineprint.cia.algorithms.membership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class BloomFilterTest {

    private static final long SEED = 20261120L;

    /** Key {@code i} as 8 big-endian bytes; distinct longs give distinct keys. */
    private static byte[] key(long i) {
        return ByteBuffer.allocate(8).putLong(i).array();
    }

    @Test
    void optimalParametersMatchTheStandardFormulas() {
        BloomParameters p = BloomParameters.optimal(1_000, 0.01);
        // m = ceil(-1000 ln 0.01 / ln^2 2) = ceil(9585.06) = 9586; k = round(9.586 * ln 2) = round(6.64) = 7
        assertThat(p.bitCount()).isEqualTo(9_586);
        assertThat(p.hashCount()).isEqualTo(7);
        assertThat(p.theoreticalFalsePositiveRate(1_000)).isBetween(0.0095, 0.0105);
    }

    @Test
    void bitsPerElementSizing() {
        BloomParameters p = BloomParameters.withBitsPerElement(10_000, 10);
        assertThat(p.bitCount()).isEqualTo(100_000);
        assertThat(p.hashCount()).isEqualTo(7); // round(10 ln 2) = round(6.93)
        assertThat(p.bitsPerElement()).isEqualTo(10.0);
    }

    @Test
    void rejectsInvalidParameters() {
        assertThatThrownBy(() -> BloomParameters.optimal(0, 0.01)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BloomParameters.optimal(100, 0.0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BloomParameters.optimal(100, 1.0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BloomParameters.optimal(100, -0.5)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BloomParameters.optimal(100, Double.NaN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BloomParameters.optimal(Long.MAX_VALUE / 2, 1e-12))
                .as("would exceed the maximum bit count").isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BloomParameters(10, 0.01, 100, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BloomParameters(10, 0.01, 0, 3)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void boundaryParameters() {
        BloomParameters single = BloomParameters.optimal(1, 0.5);
        assertThat(single.bitCount()).isGreaterThanOrEqualTo(1);
        assertThat(single.hashCount()).isEqualTo(1);
        BloomFilter filter = new BloomFilter(single, SEED);
        filter.add(key(42));
        assertThat(filter.mightContain(key(42))).isTrue();
    }

    @Test
    void emptyFilterReportsEverythingAbsent() {
        BloomFilter filter = new BloomFilter(BloomParameters.optimal(1_000, 0.01), SEED);
        for (long i = 0; i < 10_000; i++) {
            assertThat(filter.mightContain(key(i))).isFalse();
        }
        assertThat(filter.fillRatio()).isZero();
        assertThat(filter.expectedFalsePositiveRate()).isZero();
    }

    @Test
    void neverReturnsAFalseNegative() {
        for (long seed : new long[] {1L, SEED, -7L}) {
            BloomFilter filter = new BloomFilter(BloomParameters.optimal(20_000, 0.01), seed);
            for (long i = 0; i < 20_000; i++) {
                filter.add(key(i * 7919));
            }
            for (long i = 0; i < 20_000; i++) {
                assertThat(filter.mightContain(key(i * 7919))).as("seed %d key %d", seed, i).isTrue();
            }
        }
    }

    @Test
    void measuredFalsePositiveRateIsWithinToleranceOfTarget() {
        int n = 10_000;
        double target = 0.01;
        BloomFilter filter = new BloomFilter(BloomParameters.optimal(n, target), SEED);
        for (long i = 0; i < n; i++) {
            filter.add(key(i));
        }
        int probes = 200_000;
        int falsePositives = 0;
        for (long i = 0; i < probes; i++) {
            if (filter.mightContain(key(1_000_000_000L + i))) { // never inserted
                falsePositives++;
            }
        }
        double measured = (double) falsePositives / probes;
        // Binomial standard error at p=0.01, 200k probes is about 0.00022; the band allows for double hashing.
        assertThat(measured).isBetween(0.007, 0.013);
        assertThat(filter.fillRatio()).isBetween(0.45, 0.55); // about 1/2 at capacity with optimal k
    }

    @Test
    void saturationDegradesTheFalsePositiveRate() {
        BloomFilter filter = new BloomFilter(BloomParameters.optimal(1_000, 0.01), SEED);
        for (long i = 0; i < 10_000; i++) { // 10x the design capacity
            filter.add(key(i));
        }
        int falsePositives = 0;
        for (long i = 0; i < 20_000; i++) {
            if (filter.mightContain(key(5_000_000L + i))) {
                falsePositives++;
            }
        }
        assertThat(filter.fillRatio()).isGreaterThan(0.99);
        assertThat(falsePositives / 20_000.0).isGreaterThan(0.9);
        assertThat(filter.expectedFalsePositiveRate()).isGreaterThan(0.9);
    }

    @Test
    void sameSeedGivesSamePositionsAndDifferentSeedsDiffer() {
        BloomParameters p = BloomParameters.optimal(1_000, 0.01);
        BloomFilter a = new BloomFilter(p, SEED);
        BloomFilter b = new BloomFilter(p, SEED);
        BloomFilter c = new BloomFilter(p, SEED + 1);
        byte[] k = "policy-HP-2026-0001".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(a.positionsOf(k)).containsExactly(b.positionsOf(k)).hasSize(7);
        assertThat(a.positionsOf(k)).isNotEqualTo(c.positionsOf(k));
        for (long pos : a.positionsOf(k)) {
            assertThat(pos).isBetween(0L, p.bitCount() - 1);
        }
    }

    @Test
    void addSetsExactlyThePositionsThatAreProbed() {
        BloomFilter filter = new BloomFilter(BloomParameters.optimal(100, 0.01), SEED);
        byte[] k = key(123);
        filter.add(k);
        for (long pos : filter.positionsOf(k)) {
            assertThat(filter.isBitSet(pos)).isTrue();
        }
        assertThat(filter.insertionCount()).isEqualTo(1);
    }

    @Test
    void rejectsNullKeys() {
        BloomFilter filter = new BloomFilter(BloomParameters.optimal(10, 0.01), SEED);
        assertThatThrownBy(() -> filter.add(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> filter.mightContain(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void memoryIsTheBitArrayPayload() {
        BloomFilter filter = new BloomFilter(new BloomParameters(1_000, 0.01, 640, 4), SEED);
        assertThat(filter.memoryBytes()).isEqualTo(80); // 640 bits = 10 longs
    }
}
