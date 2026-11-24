package com.thefineprint.cia.algorithms.linking;

/**
 * Disjoint-set forest over the elements {@code 0 .. n-1} (union–find), with <b>union by size</b> and
 * <b>path halving</b>. Elements start as singletons; {@link #union} merges two sets; {@link #find} names a set's
 * representative.
 *
 * <p>Complexity: any sequence of {@code m} operations on {@code n} elements costs O(m · α(n)) in total
 * (α is the inverse Ackermann function, below 5 for every practical n). Space is two {@code int} arrays, O(n).
 * Union by size alone bounds every tree's height by log₂ n.
 *
 * <p>Implemented on {@code int} arrays only, with iterative loops (no recursion, so a million-element chain
 * cannot overflow the stack). Not thread-safe. {@link #find} mutates the structure (it compresses paths) but never
 * changes which elements are in the same set.
 */
public final class DisjointSet {

    private final int[] parent;
    private final int[] size; // valid at roots only: number of elements in the tree
    private int components;
    private long parentHops; // parent-pointer steps taken by find, for the explanation panel and tests

    /** Creates {@code n} singleton sets. */
    public DisjointSet(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must not be negative");
        }
        parent = new int[n];
        size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
        components = n;
    }

    /** The number of elements. */
    public int size() {
        return parent.length;
    }

    /** The number of disjoint sets. */
    public int componentCount() {
        return components;
    }

    /** The representative of {@code x}'s set. Halves the path on the way: each visited node skips its parent. */
    public int find(int x) {
        check(x);
        while (parent[x] != x) {
            parent[x] = parent[parent[x]]; // path halving
            x = parent[x];
            parentHops++;
        }
        return x;
    }

    /**
     * Merges the sets of {@code a} and {@code b}.
     *
     * @return {@code true} if they were different sets (a merge happened), {@code false} if already together
     */
    public boolean union(int a, int b) {
        int ra = find(a);
        int rb = find(b);
        if (ra == rb) {
            return false;
        }
        if (size[ra] < size[rb]) { // attach the smaller tree under the larger
            int t = ra;
            ra = rb;
            rb = t;
        }
        parent[rb] = ra;
        size[ra] += size[rb];
        components--;
        return true;
    }

    /** Whether {@code a} and {@code b} are in the same set. */
    public boolean connected(int a, int b) {
        return find(a) == find(b);
    }

    /** The number of elements in {@code x}'s set. */
    public int componentSize(int x) {
        return size[find(x)];
    }

    /**
     * The number of parent-pointer steps from {@code x} to its root, <b>without</b> changing the structure. For
     * explanation and tests of the height bound.
     */
    public int depthOf(int x) {
        check(x);
        int depth = 0;
        while (parent[x] != x) {
            x = parent[x];
            depth++;
        }
        return depth;
    }

    /** The total parent-pointer steps taken by {@link #find} so far (and so by {@link #union}). */
    public long parentHops() {
        return parentHops;
    }

    /**
     * A label for every element: elements in the same set share a label, and labels are numbered {@code 0, 1, 2, …}
     * in order of the first element that carries them. So the result is canonical: it depends only on which
     * elements are together, not on the order of unions.
     */
    public int[] componentLabels() {
        int n = parent.length;
        int[] labelOfRoot = new int[n];
        for (int i = 0; i < n; i++) {
            labelOfRoot[i] = -1;
        }
        int[] labels = new int[n];
        int next = 0;
        for (int i = 0; i < n; i++) {
            int root = find(i);
            if (labelOfRoot[root] < 0) {
                labelOfRoot[root] = next++;
            }
            labels[i] = labelOfRoot[root];
        }
        return labels;
    }

    private void check(int x) {
        if (x < 0 || x >= parent.length) {
            throw new IndexOutOfBoundsException("element " + x + " is outside 0.." + (parent.length - 1));
        }
    }
}
