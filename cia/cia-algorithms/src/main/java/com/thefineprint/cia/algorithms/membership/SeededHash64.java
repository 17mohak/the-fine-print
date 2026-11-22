package com.thefineprint.cia.algorithms.membership;

import java.util.Objects;

/**
 * A seeded, non-cryptographic 64-bit hash of a byte array, used to derive Bloom-filter bit positions.
 *
 * <p>Construction (our own implementation of a well-known design): bytes are consumed in little-endian 8-byte
 * blocks. Each block is multiplied, rotated and multiplied again by fixed odd constants, then folded into the
 * running state, as in MurmurHash3's x64 block mixing. A final avalanche step (the MurmurHash3 {@code fmix64}
 * finaliser) makes every input bit affect every output bit. Different seeds give practically independent
 * functions, which double hashing relies on.
 *
 * <p><b>Not cryptographic.</b> An adversary who knows the seed can craft collisions. That is acceptable for a
 * pre-screen whose positives are always confirmed exactly, and is why a hit is never treated as proof.
 */
public final class SeededHash64 {

    private static final long C1 = 0x87c37b91114253d5L;
    private static final long C2 = 0x4cf5ad432745937fL;

    private SeededHash64() {
    }

    /** Hashes all of {@code data} with {@code seed}. */
    public static long hash(byte[] data, long seed) {
        Objects.requireNonNull(data, "data");
        long h = seed ^ (data.length * 0x9E3779B97F4A7C15L);
        int blocks = data.length / 8;
        for (int b = 0; b < blocks; b++) {
            h ^= mixBlock(readLittleEndian(data, b * 8, 8));
            h = Long.rotateLeft(h, 27) * 5 + 0x52dce729L;
        }
        int tail = data.length - blocks * 8;
        if (tail > 0) {
            h ^= mixBlock(readLittleEndian(data, blocks * 8, tail));
        }
        h ^= data.length;
        return finalise(h);
    }

    private static long mixBlock(long k) {
        k *= C1;
        k = Long.rotateLeft(k, 31);
        k *= C2;
        return k;
    }

    /** Reads {@code length} (1–8) bytes starting at {@code offset} as an unsigned little-endian value. */
    private static long readLittleEndian(byte[] data, int offset, int length) {
        long value = 0;
        for (int i = length - 1; i >= 0; i--) {
            value = (value << 8) | (data[offset + i] & 0xFFL);
        }
        return value;
    }

    /** The MurmurHash3 64-bit finaliser: an invertible bit mixer with good avalanche. */
    static long finalise(long h) {
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return h;
    }
}
