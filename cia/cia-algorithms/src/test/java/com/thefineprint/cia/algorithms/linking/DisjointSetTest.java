package com.thefineprint.cia.algorithms.linking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class DisjointSetTest {

    @Test
    void startsAsSingletons() {
        DisjointSet d = new DisjointSet(5);
        assertThat(d.size()).isEqualTo(5);
        assertThat(d.componentCount()).isEqualTo(5);
        for (int i = 0; i < 5; i++) {
            assertThat(d.find(i)).isEqualTo(i);
            assertThat(d.componentSize(i)).isEqualTo(1);
        }
        assertThat(d.connected(0, 1)).isFalse();
    }

    @Test
    void unionMergesAndReportsWhetherAnythingChanged() {
        DisjointSet d = new DisjointSet(4);
        assertThat(d.union(0, 1)).isTrue();
        assertThat(d.union(1, 0)).isFalse(); // already together
        assertThat(d.union(2, 2)).isFalse(); // an element with itself
        assertThat(d.componentCount()).isEqualTo(3);
        assertThat(d.connected(0, 1)).isTrue();
        assertThat(d.componentSize(0)).isEqualTo(2);
    }

    @Test
    void connectivityIsTransitive() {
        DisjointSet d = new DisjointSet(6);
        d.union(0, 1);
        d.union(2, 3);
        assertThat(d.connected(0, 3)).isFalse();
        d.union(1, 2);
        assertThat(d.connected(0, 3)).isTrue();
        assertThat(d.componentSize(3)).isEqualTo(4);
        assertThat(d.componentCount()).isEqualTo(3);
    }

    @Test
    void boundaryCases() {
        DisjointSet empty = new DisjointSet(0);
        assertThat(empty.componentCount()).isZero();
        assertThat(empty.componentLabels()).isEmpty();

        DisjointSet one = new DisjointSet(1);
        assertThat(one.find(0)).isZero();
        assertThat(one.union(0, 0)).isFalse();
        assertThat(one.componentLabels()).containsExactly(0);
    }

    @Test
    void rejectsInvalidInput() {
        assertThatThrownBy(() -> new DisjointSet(-1)).isInstanceOf(IllegalArgumentException.class);
        DisjointSet d = new DisjointSet(3);
        assertThatThrownBy(() -> d.find(-1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> d.find(3)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> d.union(0, 3)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThat(d.componentCount()).isEqualTo(3); // a rejected call changes nothing
    }

    @Test
    void labelsAreCanonicalWhateverTheOrderOfUnions() {
        DisjointSet a = new DisjointSet(6);
        a.union(0, 3);
        a.union(3, 5);
        a.union(1, 4);
        DisjointSet b = new DisjointSet(6);
        b.union(5, 0);
        b.union(4, 1);
        b.union(3, 5);
        assertThat(a.componentLabels()).containsExactly(0, 1, 2, 0, 1, 0);
        assertThat(b.componentLabels()).containsExactly(a.componentLabels());
    }

    @Test
    void heightIsBoundedByLogTwoOfNEvenForTheWorstUnionOrder() {
        int n = 1 << 12; // 4096
        DisjointSet d = new DisjointSet(n);
        // Merge equal-sized trees pairwise, then pairs of pairs, and so on: the worst case for union by size.
        for (int step = 1; step < n; step *= 2) {
            for (int i = 0; i + step < n; i += 2 * step) {
                d.union(i, i + step);
            }
        }
        int deepest = 0;
        for (int i = 0; i < n; i++) {
            deepest = Math.max(deepest, d.depthOf(i)); // depthOf does not compress
        }
        assertThat(d.componentCount()).isEqualTo(1);
        assertThat(deepest).isLessThanOrEqualTo(12); // log2(4096)
    }

    @Test
    void aLongChainDoesNotOverflowTheStack() {
        int n = 1_000_000;
        DisjointSet d = new DisjointSet(n);
        for (int i = 0; i + 1 < n; i++) {
            d.union(i, i + 1);
        }
        assertThat(d.componentCount()).isEqualTo(1);
        assertThat(d.connected(0, n - 1)).isTrue();
        assertThat(d.componentSize(n / 2)).isEqualTo(n);
    }

    @Test
    void findNeverChangesWhichElementsAreTogether() {
        DisjointSet d = new DisjointSet(50);
        Random random = new Random(3);
        for (int i = 0; i < 40; i++) {
            d.union(random.nextInt(50), random.nextInt(50));
        }
        int[] before = d.componentLabels();
        for (int i = 0; i < 50; i++) {
            d.find(i); // compresses paths
        }
        assertThat(d.componentLabels()).containsExactly(before);
    }

    @Test
    void agreesWithBreadthFirstSearchOnRandomGraphs() {
        Random random = new Random(11);
        for (int round = 0; round < 2_000; round++) {
            int n = 1 + random.nextInt(40);
            int edges = random.nextInt(2 * n + 1);
            DisjointSet d = new DisjointSet(n);
            List<List<Integer>> adjacency = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                adjacency.add(new ArrayList<>());
            }
            for (int e = 0; e < edges; e++) {
                int a = random.nextInt(n);
                int b = random.nextInt(n);
                d.union(a, b);
                adjacency.get(a).add(b);
                adjacency.get(b).add(a);
            }
            int[] reference = bfsLabels(adjacency);
            assertThat(d.componentLabels()).as("n=%d edges=%d round=%d", n, edges, round).containsExactly(reference);
        }
    }

    /** Reference: connected components by BFS, labelled in order of first element, like the assessed class. */
    private static int[] bfsLabels(List<List<Integer>> adjacency) {
        int n = adjacency.size();
        int[] label = new int[n];
        java.util.Arrays.fill(label, -1);
        int next = 0;
        for (int start = 0; start < n; start++) {
            if (label[start] >= 0) {
                continue;
            }
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            queue.add(start);
            label[start] = next;
            while (!queue.isEmpty()) {
                int u = queue.poll();
                for (int v : adjacency.get(u)) {
                    if (label[v] < 0) {
                        label[v] = next;
                        queue.add(v);
                    }
                }
            }
            next++;
        }
        return label;
    }
}
