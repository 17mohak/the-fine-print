package com.thefineprint.cia.algorithms.membership;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * The probe sequence must give {@code min(k, m)} distinct, in-range positions for every modulus {@code m},
 * including moduli that share factors with the raw step, and must never overflow.
 */
class ProbeSequenceTest {

    private static long distinctCount(long[] positions) {
        return Arrays.stream(positions).distinct().count();
    }

    private static long[] positions(long h1, long h2, long m, int k) {
        long[] out = new long[k];
        ProbeSequence.positions(h1, h2, m, out);
        return out;
    }

    /** What a step of b visits from 0 over k probes, with no coprimality fix (to show the failure mode). */
    private static long distinctWithRawStep(long b, long m, int k) {
        long[] out = new long[k];
        long p = 0;
        for (int i = 0; i < k; i++) {
            out[i] = p;
            p = (p + b) % m;
        }
        return distinctCount(out);
    }

    @Test
    void evenStepSharingAFactorWithAnEvenModulusIsAdjusted() {
        long m = 12;
        long h2 = 3; // raw step = 1 + (3 mod 11) = 4, and gcd(4, 12) = 4
        assertThat(distinctWithRawStep(4, m, 12)).as("raw step 4 reaches only 12/4 positions").isEqualTo(3);
        long step = ProbeSequence.step(h2, m);
        assertThat(step).isEqualTo(5); // 4 -> 5, the next value coprime with 12
        assertThat(distinctCount(positions(0, h2, m, 12))).isEqualTo(12);
    }

    @Test
    void oddStepSharingAFactorWithAnOddModulusIsAdjusted() {
        // The flaw in "force the step odd": 5 is odd but shares the factor 5 with m = 15.
        long m = 15;
        long h2 = 4; // raw step = 1 + (4 mod 14) = 5
        assertThat(distinctWithRawStep(5, m, 15)).as("odd raw step 5 reaches only 3 positions").isEqualTo(3);
        assertThat(ProbeSequence.step(h2, m)).isEqualTo(7); // 5 (gcd 5), 6 (gcd 3), 7 (coprime)
        assertThat(distinctCount(positions(9, h2, m, 15))).isEqualTo(15);
    }

    @Test
    void coprimeSearchStaysWithinOneToMMinusOne() {
        assertThat(ProbeSequence.step(3, 6)).isEqualTo(5);   // raw 4 (gcd 2) -> 5
        assertThat(ProbeSequence.step(27, 30)).isEqualTo(29); // raw 28 (gcd 2) -> 29 = m - 1, always coprime
        assertThat(ProbeSequence.step(12345, 2)).isEqualTo(1); // m = 2: the only possible step
        for (long m = 2; m <= 2_000; m++) {
            // h2 = m - 2 gives raw step 1 + ((m - 2) mod (m - 1)) = m - 1, which is coprime and kept.
            assertThat(ProbeSequence.step(m - 2, m)).as("m=%d", m).isEqualTo(m - 1);
        }
    }

    @Test
    void everyModulusGivesMinKMDistinctInRangePositions() {
        Random random = new Random(20261121L);
        for (long m = 1; m <= 400; m++) {
            for (int trial = 0; trial < 40; trial++) {
                long h1 = random.nextLong();
                long h2 = random.nextLong();
                long step = ProbeSequence.step(h2, m);
                if (m > 1) {
                    assertThat(step).isBetween(1L, m - 1);
                    assertThat(ProbeSequence.gcd(step, m)).as("m=%d step=%d", m, step).isEqualTo(1L);
                }
                for (int k : new int[] {1, 2, 7, (int) Math.min(m, 64), (int) m + 3}) {
                    long[] p = positions(h1, h2, m, k);
                    assertThat(distinctCount(p)).as("m=%d k=%d h1=%d h2=%d", m, k, h1, h2)
                            .isEqualTo(Math.min(k, m));
                    for (long pos : p) {
                        assertThat(pos).isBetween(0L, m - 1);
                    }
                }
            }
        }
    }

    @Test
    void positionsEqualTheReferenceRecurrenceComputedWithoutOverflow() {
        Random random = new Random(7L);
        long[] moduli = {1, 2, 3, 97, 1_000, 65_536, 999_983, 10_000_000, (1L << 36) + 7,
                BloomParameters.MAX_BITS - 1, BloomParameters.MAX_BITS};
        long[] extremeHashes = {0L, 1L, -1L, Long.MAX_VALUE, Long.MIN_VALUE};
        for (long m : moduli) {
            for (int trial = 0; trial < 60; trial++) {
                long h1 = trial < extremeHashes.length ? extremeHashes[trial] : random.nextLong();
                long h2 = trial < extremeHashes.length ? extremeHashes[extremeHashes.length - 1 - trial] : random.nextLong();
                long[] p = positions(h1, h2, m, 64);
                BigInteger mod = BigInteger.valueOf(m);
                BigInteger a = new BigInteger(Long.toUnsignedString(h1)).mod(mod);
                BigInteger b = BigInteger.valueOf(ProbeSequence.step(h2, m));
                for (int i = 0; i < p.length; i++) {
                    long expected = a.add(b.multiply(BigInteger.valueOf(i))).mod(mod).longValueExact();
                    assertThat(p[i]).as("m=%d i=%d", m, i).isEqualTo(expected);
                }
                assertThat(distinctCount(p)).isEqualTo(Math.min(64, m));
            }
        }
    }

    @Test
    void singleBitFilterHasOnePosition() {
        assertThat(ProbeSequence.step(42, 1)).isZero();
        assertThat(positions(-1, -1, 1, 3)).containsExactly(0, 0, 0);
    }

    @Test
    void isDeterministic() {
        assertThat(positions(123, 456, 1_000, 10)).containsExactly(positions(123, 456, 1_000, 10));
        assertThat(ProbeSequence.step(456, 1_000)).isEqualTo(ProbeSequence.step(456, 1_000));
    }

    @Test
    void filterPositionsAreDistinctForNonPowerOfTwoAndPrimeModuli() {
        for (long m : new long[] {1_000, 999, 997, 1_024, 3 * 5 * 7 * 11}) {
            BloomFilter filter = new BloomFilter(new BloomParameters(100, 0.01, m, 7), 20261121L);
            for (int key = 0; key < 2_000; key++) {
                long[] p = filter.positionsOf(java.nio.ByteBuffer.allocate(4).putInt(key).array());
                assertThat(distinctCount(p)).as("m=%d key=%d", m, key).isEqualTo(7);
                for (long pos : p) {
                    assertThat(pos).isBetween(0L, m - 1);
                }
            }
        }
    }
}
