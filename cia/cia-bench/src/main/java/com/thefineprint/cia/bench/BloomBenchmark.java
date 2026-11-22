package com.thefineprint.cia.bench;

import com.thefineprint.cia.algorithms.membership.BloomFilter;
import com.thefineprint.cia.algorithms.membership.BloomParameters;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Query and insertion throughput: assessed Bloom filter vs {@code HashSet<Long>} (exact membership).
 *
 * <p>Keys are 64-bit fingerprints. The Bloom filter hashes their 8-byte encodings; the HashSet stores boxed
 * {@code Long}s. Each query benchmark probes a fixed batch of 1,024 keys, half present and half absent.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class BloomBenchmark {

    static final int BATCH = 1_024;

    @Param({"10000", "100000", "1000000"})
    public int elements;

    @Param({"6", "10", "14"})
    public int bitsPerElement;

    private BloomFilter filter;
    private Set<Long> exact;
    private byte[][] probeBytes;
    private Long[] probeLongs;
    private byte[][] insertBytes;

    @Setup
    public void prepare() {
        filter = new BloomFilter(BloomParameters.withBitsPerElement(elements, bitsPerElement), 7L);
        exact = new HashSet<>(elements * 2);
        for (long i = 0; i < elements; i++) {
            filter.add(bytes(i));
            exact.add(i);
        }
        probeBytes = new byte[BATCH][];
        probeLongs = new Long[BATCH];
        insertBytes = new byte[BATCH][];
        for (int i = 0; i < BATCH; i++) {
            long key = (i % 2 == 0) ? i : elements + 10_000_000L + i; // even: present, odd: absent
            probeBytes[i] = bytes(key);
            probeLongs[i] = key;
            insertBytes[i] = bytes(50_000_000L + i);
        }
    }

    static byte[] bytes(long key) {
        return ByteBuffer.allocate(8).putLong(key).array();
    }

    @Benchmark
    @OperationsPerInvocation(BATCH)
    public void bloomQuery(Blackhole bh) {
        for (byte[] key : probeBytes) {
            bh.consume(filter.mightContain(key));
        }
    }

    @Benchmark
    @OperationsPerInvocation(BATCH)
    public void hashSetQuery(Blackhole bh) {
        for (Long key : probeLongs) {
            bh.consume(exact.contains(key));
        }
    }

    /** Inserts a batch into a fresh filter sized for the batch (measures per-insert cost). */
    @Benchmark
    @OperationsPerInvocation(BATCH)
    public BloomFilter bloomInsert() {
        BloomFilter fresh = new BloomFilter(BloomParameters.withBitsPerElement(BATCH, bitsPerElement), 7L);
        for (byte[] key : insertBytes) {
            fresh.add(key);
        }
        return fresh;
    }

    @Benchmark
    @OperationsPerInvocation(BATCH)
    public Set<Long> hashSetInsert() {
        Set<Long> fresh = new HashSet<>(BATCH * 2);
        for (int i = 0; i < BATCH; i++) {
            fresh.add(50_000_000L + i);
        }
        return fresh;
    }
}
