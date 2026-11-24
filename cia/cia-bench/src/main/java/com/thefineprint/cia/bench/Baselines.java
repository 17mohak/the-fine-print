package com.thefineprint.cia.bench;

import java.util.Arrays;

/**
 * Baselines that the assessed KMP is compared against. They live here, outside the assessed module, on purpose:
 * they are reference points, not the subject of assessment.
 */
public final class Baselines {

    private Baselines() {
    }

    /** Naive matching: try every alignment, compare left to right. O(n * m) in the worst case. */
    public static int[] naiveAllStarts(int[] text, int[] pattern) {
        int[] out = new int[8];
        int found = 0;
        for (int i = 0; i + pattern.length <= text.length; i++) {
            int j = 0;
            while (j < pattern.length && text[i + j] == pattern[j]) {
                j++;
            }
            if (j == pattern.length) {
                if (found == out.length) {
                    out = Arrays.copyOf(out, found * 2);
                }
                out[found++] = i;
            }
        }
        return Arrays.copyOf(out, found);
    }

    /**
     * All (overlapping) occurrences using the JDK's {@code String.indexOf}, which HotSpot implements with a
     * vectorised intrinsic. Offsets are UTF-16 indices; the benchmark inputs are ASCII, so they equal code points.
     */
    public static int[] jdkIndexOfAllStarts(String text, String pattern) {
        int[] out = new int[8];
        int found = 0;
        int from = text.indexOf(pattern);
        while (from >= 0) {
            if (found == out.length) {
                out = Arrays.copyOf(out, found * 2);
            }
            out[found++] = from;
            from = text.indexOf(pattern, from + 1);
        }
        return Arrays.copyOf(out, found);
    }
    /**
     * Baseline for A3: number of connected components of the graph on {@code nodes} nodes with the first
     * {@code edgeCount} edges {@code (from[i], to[i])}, by breadth-first search over a compressed adjacency
     * structure built here (so the build cost is part of the comparison).
     */
    public static int bfsComponentCount(int nodes, int[] from, int[] to, int edgeCount) {
        int[][] graph = buildAdjacency(nodes, from, to, edgeCount);
        int[] start = graph[0];
        int[] neighbours = graph[1];
        boolean[] seen = new boolean[nodes];
        int[] queue = new int[nodes];
        int components = 0;
        for (int s = 0; s < nodes; s++) {
            if (seen[s]) {
                continue;
            }
            components++;
            int head = 0;
            int tail = 0;
            queue[tail++] = s;
            seen[s] = true;
            while (head < tail) {
                int u = queue[head++];
                for (int k = start[u]; k < start[u + 1]; k++) {
                    int v = neighbours[k];
                    if (!seen[v]) {
                        seen[v] = true;
                        queue[tail++] = v;
                    }
                }
            }
        }
        return components;
    }

    /** Baseline for A3: are {@code a} and {@code b} connected using the first {@code edgeCount} edges? (BFS from a.) */
    public static boolean bfsConnected(int nodes, int[] from, int[] to, int edgeCount, int a, int b) {
        int[][] graph = buildAdjacency(nodes, from, to, edgeCount);
        int[] start = graph[0];
        int[] neighbours = graph[1];
        boolean[] seen = new boolean[nodes];
        int[] queue = new int[nodes];
        int head = 0;
        int tail = 0;
        queue[tail++] = a;
        seen[a] = true;
        while (head < tail) {
            int u = queue[head++];
            if (u == b) {
                return true;
            }
            for (int k = start[u]; k < start[u + 1]; k++) {
                int v = neighbours[k];
                if (!seen[v]) {
                    seen[v] = true;
                    queue[tail++] = v;
                }
            }
        }
        return false;
    }

    private static int[][] buildAdjacency(int nodes, int[] from, int[] to, int edgeCount) {
        int[] start = new int[nodes + 1];
        for (int i = 0; i < edgeCount; i++) {
            start[from[i] + 1]++;
            start[to[i] + 1]++;
        }
        for (int i = 0; i < nodes; i++) {
            start[i + 1] += start[i];
        }
        int[] fill = Arrays.copyOf(start, nodes);
        int[] neighbours = new int[2 * edgeCount];
        for (int i = 0; i < edgeCount; i++) {
            neighbours[fill[from[i]]++] = to[i];
            neighbours[fill[to[i]]++] = from[i];
        }
        return new int[][] {start, neighbours};
    }
}
