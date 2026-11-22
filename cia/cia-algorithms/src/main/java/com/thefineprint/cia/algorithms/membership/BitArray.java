package com.thefineprint.cia.algorithms.membership;

/**
 * A fixed-size array of bits packed into {@code long} words (64 bits per word).
 *
 * <p>Bit {@code i} lives in word {@code i >>> 6} at position {@code i & 63}. Memory is
 * {@code ceil(size / 64) * 8} bytes plus the array header, so 10 million bits take about 1.2 MB.
 */
public final class BitArray {

    private final long[] words;
    private final long size;
    private long bitsSet;

    /**
     * @param size number of bits; 1 to {@code 64 * Integer.MAX_VALUE}
     */
    public BitArray(long size) {
        if (size < 1 || size > 64L * Integer.MAX_VALUE) {
            throw new IllegalArgumentException("bit array size out of range: " + size);
        }
        this.size = size;
        this.words = new long[(int) ((size + 63) >>> 6)];
    }

    /** Sets bit {@code index}; returns {@code true} if it was previously clear. */
    public boolean set(long index) {
        checkIndex(index);
        int word = (int) (index >>> 6);
        long mask = 1L << (index & 63);
        if ((words[word] & mask) != 0) {
            return false;
        }
        words[word] |= mask;
        bitsSet++;
        return true;
    }

    /** Returns whether bit {@code index} is set. */
    public boolean get(long index) {
        checkIndex(index);
        return (words[(int) (index >>> 6)] & (1L << (index & 63))) != 0;
    }

    /** Number of bits. */
    public long size() {
        return size;
    }

    /** Number of bits currently set (maintained incrementally; O(1)). */
    public long bitsSet() {
        return bitsSet;
    }

    /** Payload memory in bytes (the word array only, excluding object headers). */
    public long payloadBytes() {
        return words.length * 8L;
    }

    private void checkIndex(long index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("bit index " + index + " outside [0, " + size + ")");
        }
    }
}
