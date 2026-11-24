# A3 — Linking related documents with a disjoint-set forest (Unit 2)

**Code:** `cia/cia-algorithms/src/main/java/com/thefineprint/cia/algorithms/linking/DisjointSet.java`.
**Tests:** `DisjointSetTest` (10). **Demo:** `POST /algo/document-links` (`cia-demo/.../linking`) and the third panel of
the UI. **Status:** assessed implementation (Track C, D-31). Never a production dependency.

## 30-second explanation
A person's documents arrive one at a time: a policy, an endorsement, a renewal notice, a claim letter. Two of them
that quote the same policy number belong together, and so do a third that shares a renewal number with the second.
A disjoint-set forest keeps "which documents are in the same family" up to date as links are discovered, and can answer
"are these two documents in the same family?" in almost constant time, without ever re-scanning the graph.

## Problem statement
Input: `n` documents and a stream of links (pairs of documents that share an identifier). Output: the partition of the
documents into families, i.e. the **connected components** of the graph whose edges are the links, plus the reason (the
shared identifier) for each link. Queries ("same family?") can be interleaved with new links.

## Why a disjoint-set forest is appropriate
- The question is exactly dynamic connectivity under insertions only, which union–find solves in O(α(n)) amortised per
  operation: a pass of BFS would be O(n + m) for *every* new link or query.
- It needs only two `int` arrays; there is no adjacency structure to build or keep in sync.
- Honest limit: at product scale (tens of documents per person) BFS would be instant. The advantage is for large or
  growing collections and for repeated queries, and it is measured in [BENCHMARKS.md](BENCHMARKS.md).

## Contract (as implemented)

| Method | Contract |
|---|---|
| `new DisjointSet(n)` | `n` singleton sets over `0 … n-1`. `n < 0` → `IllegalArgumentException`; `n = 0` is valid |
| `find(x)` | The representative of `x`'s set; halves the path as it goes. Index outside `0 … n-1` → `IndexOutOfBoundsException` |
| `union(a, b)` | Merges the two sets; `true` if a merge happened, `false` if already together (including `a = b`) |
| `connected(a, b)`, `componentSize(x)`, `componentCount()` | As named |
| `componentLabels()` | A canonical label per element (`0, 1, 2, …` in order of first appearance): depends only on *who is together*, not on the order of unions |
| `depthOf(x)`, `parentHops()` | Explanation and test helpers: steps to the root **without** compressing; total parent steps taken so far |

## Pseudocode (mirrors the code)
```
make(n):            parent[i] = i;  size[i] = 1;  components = n
find(x):            while parent[x] != x:
                        parent[x] = parent[parent[x]]        // path halving
                        x = parent[x]
                    return x
union(a, b):        ra = find(a);  rb = find(b)
                    if ra == rb: return false
                    if size[ra] < size[rb]: swap(ra, rb)      // attach the smaller tree under the larger
                    parent[rb] = ra;  size[ra] += size[rb];  components -= 1
                    return true
```

In the demo, the service maps each identifier to the documents holding it and calls `union(first, other)` for every
additional holder, so a family forms through shared identifiers transitively. Identifiers shared by four or more
documents are flagged **broad**: they are the likely cause of a false merge.

## Invariants and correctness argument
1. **Partition:** the sets always partition `0 … n-1`; every element has exactly one root, reached by following
   `parent`. `union` only redirects one root to another, so no element is lost or duplicated.
2. **Same tree ⇔ same family:** initially every element is alone, which is the connected-components partition of the
   empty graph. `union(a, b)` joins the trees of `a` and `b`, which is exactly what adding the edge `{a, b}` does to the
   components. By induction after any sequence of unions, `find(x) = find(y)` iff `x` and `y` are connected by the
   edges added so far.
3. **Path halving does not change membership:** it only sets `parent[x]` to an ancestor of `x`, which has the same root.
   Test `findNeverChangesWhichElementsAreTogether`.
4. **Size is exact at roots:** `size[root]` equals the number of elements in the tree (a merge adds the two sizes).
- *Tested against a reference:* `agreesWithBreadthFirstSearchOnRandomGraphs` compares `componentLabels()` with BFS
  components on 2,000 seeded random graphs (up to 40 nodes, up to 80 edges, including multi-edges and self-loops).

## Complexity
- **Height bound (union by size):** a node's depth grows by 1 only when its tree is attached under a tree at least as
  large, so the size of its tree at least doubles each time. A tree of height `h` therefore has at least `2^h`
  elements, hence `h ≤ log₂ n`. Test `heightIsBoundedByLogTwoOfNEvenForTheWorstUnionOrder` builds the worst case
  (pairwise merges of equal trees) for `n = 4096` and finds depth ≤ 12.
- **Amortised cost:** with union by size and path halving, any sequence of `m` operations on `n` elements costs
  `O(m · α(n))` (Tarjan and van Leeuwen), where α is the inverse Ackermann function (at most 4 for any `n` that fits
  in memory). Without compression, one operation is `O(log n)` worst case by the height bound. This is a cited result;
  the repository proves only the height bound and measures the rest.
- **Space:** `2n` ints, O(n). `componentLabels()` allocates two more arrays of `n`.

## Edge cases (all tested)
Empty set (`n = 0`); one element; union of an element with itself; repeated union (idempotent); indices `-1` and `n`;
a rejected call changes nothing; a 1,000,000-element chain (iterative, no stack overflow). Service level: no documents,
more than 200 documents, duplicate document ids, blank ids, empty or over-long identifiers, more than 20 identifiers per
document, a repeated identifier inside one document, identifiers that differ only in case or a trailing space (they do
**not** link), a single document.

## Known limitations
- **Transitive over-merging:** one shared common identifier (an insurer name, a phone number) merges unrelated
  documents. The demo flags broad identifiers; production linking would need an identifier-type allow-list and weights.
- **No split:** a disjoint-set forest cannot undo a union. "Unlink" means rebuilding the affected family from the
  retained reason edges (the demo reports them, so that is possible).
- **Exact identifier equality only:** no normalisation or fuzzy matching.
- Grouping is a **suggestion**. It does not establish that the documents belong to one agreement, and nothing is stored.

## Baseline comparison
BFS connected components over a compressed adjacency structure (built inside the timing), for a one-shot count and for
interleaved "connected?" queries. See [BENCHMARKS.md](BENCHMARKS.md).

## Example walkthrough (the demo's synthetic example)
Eight documents; `POL-8841` is held by `policy-2026`, `endorsement-1` and `renewal-notice`; `RENEWAL-2027-17` by
`renewal-notice` and `claim-letter`; `LN-5520` by `loan-sanction` and `emi-schedule`; the rest share nothing.
Union calls: 2 (POL-8841) + 1 (RENEWAL-2027-17) + 1 (LN-5520) = 4, all merging. Result: 4 families:
`{policy-2026, endorsement-1, renewal-notice, claim-letter}` (linked transitively; `policy-2026` and `claim-letter` share
nothing directly), `{loan-sanction, emi-schedule}`, `{rent-agreement}`, `{gym-invoice}`.

## 2-minute walkthrough (demo)
1. Open *Which documents belong together?* and load the synthetic example; group.
2. Read the family table: the first family has four documents, linked by two identifiers. Point at `claim-letter`:
   it shares nothing with `policy-2026`, yet is in its family, through `renewal-notice`.
3. Add a document `unrelated: POL-8841` and regroup; then give four documents the same identifier to see the
   *broad identifier* warning.
4. Say what it does not show: that the documents are truly one agreement.

## 5-minute technical explanation (decisions and alternatives)
- **Why union by size *and* path halving:** either alone gives O(log n) worst case; together, near-constant amortised.
  Halving is single-pass and iterative (no recursion), which is why a million-element chain is safe.
- **Why `int` arrays only:** the assessed class must be our own structure; ArchUnit forbids `HashMap`, `HashSet` and
  `BitSet` in assessed code. The mapping from identifier text to document number is infrastructure and lives in the demo.
- **Why canonical labels:** the response must not depend on the order in which links were found.
- **Alternatives:** BFS/DFS (simple, but O(n + m) per query); pairwise comparison of identifier sets, O(n²); union by
  rank (equivalent bound; size is also useful as an output); a Euler-tour or link-cut structure (supports deletion, far
  more complex than this problem warrants).
