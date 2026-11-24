package com.thefineprint.cia.bench;

import com.thefineprint.cia.algorithms.linking.DisjointSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * A3: connected components of a random graph with {@code n} nodes and {@code n} edges (documents and shared
 * identifiers): assessed disjoint-set forest vs breadth-first search.
 *
 * <p>Two workloads. <b>batch</b>: count the components once (BFS must first build an adjacency structure, DSU
 * needs none; both are inside the timing). <b>incremental</b>: add the edges in 20 batches and ask "are these two
 * documents connected?" after each batch, as a growing document vault would (BFS must rescan after every batch).
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class LinkingBenchmark {

    private static final int QUERIES = 20;

    @Param({"10000", "100000", "1000000"})
    public int nodes;

    private int[] from;
    private int[] to;
    private int[] queryA;
    private int[] queryB;

    @Setup
    public void prepare() {
        Random random = new Random(5);
        from = new int[nodes];
        to = new int[nodes];
        for (int i = 0; i < nodes; i++) {
            from[i] = random.nextInt(nodes);
            to[i] = random.nextInt(nodes);
        }
        queryA = new int[QUERIES];
        queryB = new int[QUERIES];
        for (int q = 0; q < QUERIES; q++) {
            queryA[q] = random.nextInt(nodes);
            queryB[q] = random.nextInt(nodes);
        }
    }

    @Benchmark
    public int batchDsu() {
        DisjointSet d = new DisjointSet(nodes);
        for (int i = 0; i < nodes; i++) {
            d.union(from[i], to[i]);
        }
        return d.componentCount();
    }

    @Benchmark
    public int batchBfs() {
        return Baselines.bfsComponentCount(nodes, from, to, nodes);
    }

    @Benchmark
    public int incrementalDsu() {
        DisjointSet d = new DisjointSet(nodes);
        int connected = 0;
        int step = nodes / QUERIES;
        for (int q = 0; q < QUERIES; q++) {
            for (int i = q * step; i < (q + 1) * step; i++) {
                d.union(from[i], to[i]);
            }
            if (d.connected(queryA[q], queryB[q])) {
                connected++;
            }
        }
        return connected;
    }

    @Benchmark
    public int incrementalBfs() {
        int connected = 0;
        int step = nodes / QUERIES;
        for (int q = 0; q < QUERIES; q++) {
            if (Baselines.bfsConnected(nodes, from, to, (q + 1) * step, queryA[q], queryB[q])) {
                connected++;
            }
        }
        return connected;
    }
}
