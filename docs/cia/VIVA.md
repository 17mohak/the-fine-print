# Viva preparation — A1 KMP and A2 Bloom filter

Answers refer to the actual code in `cia/cia-algorithms` and the demo in `cia/cia-demo`. Practise the manual
walkthroughs on paper; an examiner may change the inputs.

## A. Product framing
1. **What problem do your algorithms solve in the product?**
   KMP proves a quoted passage really occurs in a user's document before the product shows it as "the document
   says" (the provenance gate). The Bloom filter is a cheap "have we seen this fingerprint?" pre-screen whose
   "possibly" answers are always confirmed exactly.
2. **Is this code used in production?**
   No. It lives in the isolated `cia-algorithms` module. The platform's build check (`verifyBoundaries`) and
   ArchUnit fail if any platform module depends on it. Production uses standard adapters behind the same
   `core-api` interfaces. Promotion would need a measured need, benchmarks, shadow mode and review (seven-point
   process).
3. **Why exact matching and not fuzzy matching?**
   Evidence must be verifiable. A fuzzy hit could "verify" a paraphrase the document never contains. Fuzzy
   search may suggest where to look, but it can never establish a document fact.

## B. KMP
4. **Define the prefix function.**
   `pi[i]` is the length of the longest proper prefix of `P[0..i]` that is also a suffix of it.
5. **Compute π for `aabaaab`.** `[0, 1, 0, 1, 2, 2, 3]` (tested).
6. **Compute π for `abab`; then trace `ababcabab`.** `π = [0, 0, 1, 2]`; matches at 0 and 5, 10 comparisons
   (see the trace in A1_KMP.md).
7. **Why is the search linear?**
   `q` rises by at most 1 per text symbol, and each fallback strictly lowers it, so there are at most n
   fallbacks over the whole run. The total is at most 2n comparisons. That is asserted in tests and shown in the
   demo.
8. **What is the auxiliary space?**
   O(m) for π. The result array is O(occ); the code-point conversion in the adapter is O(n + m).
9. **How do you find overlapping matches?**
   After a full match, `q ← pi[m−1]` instead of 0. For example, `aa` in `aaaa` gives 0, 1, 2.
10. **What happens with an empty pattern? An empty text?**
    An empty pattern throws `IllegalArgumentException` (the SPI contract). An empty text returns no matches.
    A pattern longer than the text returns no matches immediately.
11. **Why code points rather than `char`s?**
    The platform's anchors are code-point offsets, and a supplementary character (emoji) is two `char`s. Test:
    `😀a😀a` / `😀a` gives offsets 0 and 2.
12. **How do you know it is correct?**
    The invariant argument (A1 doc), plus 5,000 seeded randomized comparisons against the naive reference over
    tiny alphabets, plus hand-computed cases.
13. **Did KMP beat `String.indexOf`?**
    See BENCHMARKS.md and answer with the measured numbers. `indexOf` is a vectorised JVM intrinsic and wins on
    ordinary text, which we report honestly. KMP's advantage is its worst-case guarantee: on adversarial input
    the naive method degrades by roughly a factor of m.
14. **Can a match start in the middle of a Devanagari conjunct?**
    Yes. KMP matches code points. Grapheme-boundary enforcement is the caller's responsibility in the platform
    design. This is a known limitation, stated in the docs.
15. **Show where the comparison counter is incremented and why it is there.**
    Once per fallback comparison inside the `while`, and once per text symbol. It is the measurable evidence of
    the 2n bound.

## C. Bloom filter
16. **What can a Bloom filter tell you for certain?**
    Only absence: one clear bit proves the key was never added. "All bits set" means *possibly* present.
17. **Derive the false-positive probability.**
    A given bit stays 0 after kn insertions with probability `(1 − 1/m)^{kn} ≈ e^{−kn/m}`. A non-member is
    accepted if all k of its bits are 1: `(1 − e^{−kn/m})^k`.
18. **Derive the optimal k and m.**
    Minimising over k gives `k = (m/n) ln 2`; substituting gives `p = 2^{−k}`, so `m = −n ln p / (ln 2)²`.
    Example: n = 1000, p = 0.01 gives m = 9586 and k = 7 (tested).
19. **What is double hashing and why use it?**
    `g_i = (h1 + i·h2) mod m`: k positions from two hashes, with the same asymptotic false-positive rate as k
    independent hashes (Kirsch–Mitzenmacher). It saves k − 2 hash passes.
20. **Why force `h2` odd?**
    If `h2` shared a factor with m (for example both even), the probe sequence would cycle through fewer distinct
    positions.
21. **Describe your hash function.**
    A seeded 64-bit block hash: little-endian 8-byte blocks, each multiplied, rotated and multiplied by odd
    constants, folded into the state, the tail handled the same way, the length mixed in, and the fmix64
    finaliser. Tests: determinism, seed sensitivity, distinct tails, about 32 of 64 output bits flipping per
    input bit (avalanche), and a chi-square uniformity check across 64 buckets.
22. **Is it cryptographic? Does that matter?**
    No. An attacker who knows the seed could craft collisions. That is acceptable because a hit is never trusted
    without the exact check.
23. **What happens when you insert more than n keys?**
    Saturation: the fill ratio goes to 1 and the false-positive rate to 1. At 10× capacity the test measures more
    than 90 % false positives. The demo flags `overCapacity`.
24. **Can you delete?**
    No. Clearing a bit could erase other keys. A counting Bloom filter or a cuckoo filter would be needed.
25. **How much memory against a HashSet?**
    Quote the measured CSV in BENCHMARKS.md. A Bloom filter at 10 bits per element is 1.25 bytes per key; a
    `HashSet<Long>` is tens of bytes per key (boxed `Long`, entry, table).
26. **Why your own bit array instead of `java.util.BitSet`?**
    The assessed structure must be our own implementation. An ArchUnit rule forbids `BitSet`, `HashSet` and
    JDK string search in the assessed module, and a fixture test proves the rule fires.
27. **How is the false-positive test made reproducible?**
    Fixed seeds for the filter and fixed key ranges for inserted and probe keys, so the measured rate is
    identical on every run. The tolerance band covers the double-hashing approximation.

## D. Edge-case challenges (be ready to run them live in the demo)
- Quote with different case or curly quotes → not found (no folding).
- Overlapping quote (`aa` in `aaaa`).
- An emoji in the text → offsets in code points; the highlight is still correct.
- Bloom with a 30 % target → visible false positives, each caught by the exact check.
- Bloom with capacity 2 and 10 fingerprints → saturation warning.
- Empty probe list or a 0.9 target → a clear validation error (an RFC 9457 problem response).

## E. Implementation-choice questions
- **Why are the services separate from the controllers?** So the request flow is traceable (controller →
  service → algorithm), and the business logic is unit-tested without HTTP.
- **Why is the demo stateless?** No storage to secure, and every request is reproducible.
- **Why plain HTML, CSS and JS?** No build toolchain for a two-panel demo. The frontend only calls the Java API;
  no algorithm runs in the browser.
- **Why are inputs limited (100k text, 5,000 fingerprints, 500 probes)?** They bound the server's work for a
  public demo endpoint.
