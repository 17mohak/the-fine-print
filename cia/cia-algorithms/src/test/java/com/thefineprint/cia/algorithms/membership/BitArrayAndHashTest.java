package com.thefineprint.cia.algorithms.membership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Random;
import org.junit.jupiter.api.Test;

class BitArrayAndHashTest {

    @Test
    void setAndGetAcrossWordBoundaries() {
        BitArray bits = new BitArray(130);
        for (long i : new long[] {0, 63, 64, 127, 128, 129}) {
            assertThat(bits.get(i)).isFalse();
            assertThat(bits.set(i)).isTrue();
            assertThat(bits.get(i)).isTrue();
            assertThat(bits.set(i)).as("second set reports already set").isFalse();
        }
        assertThat(bits.bitsSet()).isEqualTo(6);
        assertThat(bits.get(1)).isFalse();
        assertThat(bits.payloadBytes()).isEqualTo(24); // 130 bits -> 3 words
    }

    @Test
    void rejectsOutOfRangeIndexesAndSizes() {
        BitArray bits = new BitArray(10);
        assertThatThrownBy(() -> bits.get(10)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> bits.set(-1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> new BitArray(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BitArray(64L * Integer.MAX_VALUE + 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void hashIsDeterministicAndSeedSensitive() {
        byte[] data = "clause-fingerprint".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(SeededHash64.hash(data, 1)).isEqualTo(SeededHash64.hash(data.clone(), 1));
        assertThat(SeededHash64.hash(data, 1)).isNotEqualTo(SeededHash64.hash(data, 2));
        assertThatThrownBy(() -> SeededHash64.hash(null, 1)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void everyTailLengthAndTheEmptyInputHashDistinctly() {
        java.util.Set<Long> seen = new java.util.HashSet<>();
        byte[] base = new byte[17];
        for (int len = 0; len <= 17; len++) {
            assertThat(seen.add(SeededHash64.hash(java.util.Arrays.copyOf(base, len), 7))).as("len %d", len).isTrue();
        }
    }

    @Test
    void flippingOneInputBitChangesAboutHalfTheOutputBits() {
        Random random = new Random(42);
        long totalChanged = 0;
        int trials = 2_000;
        for (int t = 0; t < trials; t++) {
            byte[] data = new byte[12];
            random.nextBytes(data);
            long before = SeededHash64.hash(data, 99);
            int bit = random.nextInt(data.length * 8);
            data[bit / 8] ^= (byte) (1 << (bit % 8));
            totalChanged += Long.bitCount(before ^ SeededHash64.hash(data, 99));
        }
        double average = (double) totalChanged / trials;
        assertThat(average).isBetween(30.0, 34.0); // ideal avalanche is 32 of 64 bits
    }

    @Test
    void positionsAreRoughlyUniformAcrossBuckets() {
        int buckets = 64;
        int[] counts = new int[buckets];
        int samples = 64_000;
        for (int i = 0; i < samples; i++) {
            byte[] data = java.nio.ByteBuffer.allocate(4).putInt(i).array();
            counts[(int) Long.remainderUnsigned(SeededHash64.hash(data, 5), buckets)]++;
        }
        double expected = (double) samples / buckets;
        double chiSquare = 0;
        for (int c : counts) {
            chiSquare += (c - expected) * (c - expected) / expected;
        }
        // 63 degrees of freedom: the 99.9th percentile is about 103.4.
        assertThat(chiSquare).isLessThan(103.4);
    }
}
