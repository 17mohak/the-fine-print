# A2 — Probabilistic membership screening with a Bloom filter (Unit 6 / CO5)

**Code:** `cia/cia-algorithms/src/main/java/com/thefineprint/cia/algorithms/membership/`
(`BloomFilter`, `BloomParameters`, `BitArray`, `SeededHash64`). **Tests:** `BloomFilterTest` (12),
`BitArrayAndHashTest` (6). **Demo confirmation step:** `cia-demo/.../membership/ExactFingerprintRegistry`.
**Status:** assessed implementation (Track C, D-31). Never a production dependency.

## 30-second explanation
A Bloom filter is a bit array plus k hash functions. Adding a key sets k bits; looking a key up checks those k
bits. If any bit is 0, the key was **definitely never added**. If all are 1, it was **possibly added**, because
other keys may have set those bits. It uses a few bits per key instead of tens of bytes, never gives a false
negative, and is wrong in the "possibly" direction at a rate you choose. That is why every "possibly" answer must
be confirmed by an exact lookup.

## Problem statement
THE FINE PRINT may need to ask "have we seen this fingerprint before?" (a document or clause hash, or a known
standard-clause wording) many times, against a set that could outgrow memory. A Bloom filter answers cheaply and
lets the system skip the exact lookup whenever the answer is "definitely not". **In the platform the production
default is an exact set** (ARCHITECTURE_OVERVIEW section 2). This assessed implementation is demonstrated in
`cia-demo`, not used in production.

## Contracts (as implemented)

| API | Contract |
|---|---|
| `BloomParameters.optimal(n, p)` | `m = ceil(−n·ln p / (ln 2)²)`, `k = max(1, round(m/n · ln 2))`. Rejects `n < 1`, `p` not in (0, 1) (including NaN), and filters over 64·(2³¹−1) bits |
| `BloomParameters.withBitsPerElement(n, b)` | `m = n·b`, `k = round(b · ln 2)` (used by the benchmarks) |
| `new BloomFilter(params, seed)` | The same seed and inputs always give identical bit patterns (reproducible tests) |
| `add(byte[])` / `mightContain(byte[])` | The `core-api` `ApproximateMembership` SPI. No false negatives; `true` means *possibly present*; `null` → `NullPointerException` |
| `positionsOf(byte[])`, `fillRatio()`, `expectedFalsePositiveRate()`, `memoryBytes()`, `isBitSet(i)` | Explanation and measurement helpers |

## How it works
- **Bit array (`BitArray`):** `m` bits packed into `long` words. Bit `i` is word `i >>> 6`, bit `i & 63`. It
  keeps a running count of set bits, so `fillRatio()` is O(1).
- **Hash (`SeededHash64`, our own implementation):** reads the bytes in 8-byte little-endian blocks; mixes each
  block with multiply–rotate–multiply by odd constants; folds into the state with rotate–multiply–add; mixes the
  tail bytes the same way; xors in the length; and finishes with the MurmurHash3 `fmix64` avalanche. **It is not
  cryptographic.**
- **Probe positions (double hashing with a coprime step, `ProbeSequence`):** compute `h1 = H(key, seed)` and
  `h2 = H(key, seed ⊕ φ)` once per key. Then:
  1. **start** `a = h1 mod m` (unsigned), so `0 ≤ a < m`;
  2. **step** `b = 1 + (h2 mod (m − 1))`, so `1 ≤ b ≤ m − 1`, then increase `b` by one while `gcd(b, m) ≠ 1`.
     The search stops at `m − 1` at the latest, because `gcd(m − 1, m) = 1`;
  3. **positions** `g_0 = a` and `g_{i+1} = (g_i + b) mod m`, so `g_i = (a + i·b) mod m`.

  If `m = 1` the only position is 0. This costs two hash passes instead of k.

  *Why the step must be coprime with m.* Starting from `a`, the sequence `a + i·b (mod m)` revisits a position
  after `m / gcd(b, m)` steps. If `b` shares a factor with `m`, the key reaches only `m / gcd(b, m)` positions
  and may set fewer than k distinct bits. The earlier version forced `b` odd, which only removes a shared factor
  of 2. With `m = 15`, the odd step 5 reaches just 3 positions. With `gcd(b, m) = 1`, `i ↦ a + i·b (mod m)` is a
  bijection on `{0, …, m − 1}`, so **every key's first `min(k, m)` positions are distinct, for every m**.

  *Why it cannot overflow.* The hashes are reduced modulo `m` (and `m − 1`) first. After that every value is
  `< m ≤ 64·(2³¹ − 1) < 2³⁷`, and each step is one addition (`< 2³⁸`) plus a conditional subtraction. Nothing
  is multiplied, so no value wraps. The test `positionsEqualTheReferenceRecurrenceComputedWithoutOverflow`
  compares against `BigInteger` arithmetic up to the maximum `m`, with extreme hash values.

## Pseudocode
```
positions(key):    a = H(key, s1) mod m                       // unsigned
                   b = 1 + (H(key, s2) mod (m-1))             // m >= 2; b = 0 when m == 1
                   while gcd(b, m) != 1: b = b + 1            // stops by m-1
                   g = a; repeat k times: yield g; g = g + b; if g >= m: g = g - m
add(key):          set every bit in positions(key)
mightContain(key): return false at the first clear bit in positions(key); otherwise true
```

## Important invariants
1. A bit, once set, is never cleared (there is no deletion).
2. `add(x)` sets exactly the bits that `mightContain(x)` checks: the same seeds and the same probe sequence
   (test `addSetsExactlyThePositionsThatAreProbed`).
3. A key's first `min(k, m)` probe positions are pairwise distinct and lie in `[0, m)`, for every supported `m`
   (`ProbeSequenceTest`: all `m` from 1 to 400, k up to m + 3, and large moduli up to the maximum).

## Correctness argument
- **No false negatives:** after `add(x)`, all `k` positions of `x` are 1 (invariant 2), and bits never go back
  to 0 (invariant 1). So `mightContain(x)` finds no clear bit and returns `true`. Tested with 20,000 keys under
  three seeds.
- **False-positive rate (theory, an approximation for this scheme):** the textbook derivation assumes each key's
  positions are independent and uniform. Then a bit stays 0 after n insertions with probability
  `(1 − 1/m)^{kn} ≈ e^{−kn/m}`, and a non-member is reported present with probability
  `p ≈ (1 − e^{−kn/m})^k`. Minimising over k gives `k = (m/n) ln 2` and `m = −n ln p / (ln 2)²`, the formulas in
  `BloomParameters`.

  **Qualification.** This implementation does not meet that assumption exactly:
  - A key's k positions are *distinct*, and lie on one arithmetic progression mod m.
  - The step is uniform over `[1, m − 1]` before the coprime adjustment. After it, coprime values that follow
    non-coprime ones are chosen more often, so the step is uniform over the allowed values **only when m is
    prime**.
  - Two keys with the same `(a, b)` collide on all k bits.

  Kirsch and Mitzenmacher showed that double hashing has the same *asymptotic* false-positive rate as k
  independent hashes (their analysis uses a prime modulus). For our composite `m`, the formula is an
  approximation, so **the measured rates in BENCHMARKS.md, not the formula, are the evidence**. Making a key's
  positions distinct (rather than sampled with replacement) changes the expected fill only by terms of order
  k²/m, which is negligible at the sizes measured.
  At the optimum about half the bits are set (test: fill ratio 0.45–0.55 at capacity).
- **Empirical check:** at `n = 10,000` and `p = 0.01`, 200,000 never-inserted probes give a measured rate within
  0.7–1.3 % (test band). The binomial standard error is about 0.02 %, so the band is wide enough for
  double-hashing effects. The experiment in [BENCHMARKS.md](BENCHMARKS.md) measures 1,000,000 probes for each of
  five seeds per configuration.

## Complexity
- `add` / `mightContain`: **O(k)** bit operations, plus O(|key|) to hash (two hash passes), plus choosing the
  step: one or more `gcd` computations of O(log m) each. The number of increments before reaching a coprime
  value is bounded by the largest gap between integers coprime to m. That gap is small for practical m (for
  example, at most 1 when m is a power of two, and 0 when m is prime) but is not a constant in general.
- Space: **m bits** (`ceil(m/64)·8` bytes). At 10 bits per element that is 1.25 bytes per key, against tens of
  bytes per key for `HashSet<Long>` (measured in [BENCHMARKS.md](BENCHMARKS.md)).
- Building: O(n·k).

## Edge cases and validation (tested)
- `n = 1`.
- A very high `p` (0.5, giving `k = 1`).
- An empty filter (everything absent; fill ratio 0).
- Invalid `n`, `p` (0, 1, negative, NaN), `k`, `m`.
- An oversized filter.
- Out-of-range bit indices.
- Word boundaries (bits 63/64, 127/128).
- Hash tails of 0–17 bytes.
- `null` keys.

## Saturation behaviour and limitations
- **Saturation:** inserting beyond `n` drives the fill ratio towards 1 and the false-positive rate towards 1
  (test: at 10× capacity, fill > 0.99 and FPR > 0.9). The demo flags `overCapacity`.
- **No deletion:** clearing a bit could remove other keys. A counting variant is out of scope.
- **Duplicates** are counted in `insertionCount()`, which overstates the theoretical rate (it is conservative).
- **Not cryptographic:** an attacker who knows the seed can craft colliding keys. That is acceptable only
  because hits are never trusted without exact confirmation.
- **Modulo reduction:** `x mod m` on 64-bit values has negligible bias for `m` far below 2⁶⁴ (at most 2⁻²⁷ at
  the maximum `m`).
- **Step distribution:** uniform over the allowed steps only when m is prime (see "Qualification" above).
  Sizing to a prime m would remove this, at the cost of changing `BloomParameters`; it was not needed to meet
  the measured false-positive targets.
- **Not thread-safe.**

## The exact-confirmation step (never treat a hit as proof)
`MembershipScreeningService` (in `cia-demo`):
1. Builds the filter and adds every fingerprint.
2. For each probe, asks the filter.
   - **Definitely absent** → done (`NOT_NEEDED`).
   - **Possibly present** → check `ExactFingerprintRegistry`, giving `CONFIRMED_PRESENT` or `FALSE_POSITIVE`.

The UI shows both columns; only "Confirmed present" means present. Test:
`possibleHitsOnUnknownProbesAreReportedAsFalsePositivesNeverAsPresent`.

## Baseline comparison
`HashSet<Long>`: exact, with no false positives, but many times more memory. See [BENCHMARKS.md](BENCHMARKS.md)
for memory, false-positive rate and throughput.

## Example walkthrough (the demo's synthetic example)
- **Sizing:** 10 fingerprints at target p = 0.3 → `m = ceil(−10·ln 0.3 / 0.4805) = 26` bits,
  `k = round(2.6 · 0.693) = 2`, seed 7. `m = 26 = 2·13` is composite, so some raw steps are adjusted to the
  next coprime value. After inserting 10 keys, 15 of 26 bits are set (57.7 %).
- **Probes** (recomputed after the coprime-step fix, 2026-11-21):
  - 3 known fingerprints → *possibly present* → **confirmed**.
  - 7 unknown probes hit a clear bit → *definitely absent*.
  - 2 unknown probes (`POL-MTR-2026-0199`, `RENT-MH-2026-0045`) find both their bits set → *possibly present*
    → the exact check says **false positive**.

  For example, `POL-MTR-2026-0199` maps to bits 5 and 8, both set by other fingerprints.

## 2-minute walkthrough (demo)
1. Load the example; it uses 30 % to make false positives visible.
2. Point at m and k and explain the two formulas.
3. Run it. Read one "definitely absent" row (a clear bit proves absence) and one "false positive" row (select it
   to outline its bits on the bit array, both set by other keys).
4. Switch to 1 % and rerun: the false positives disappear and m grows.
5. Set the capacity to 2: the filter saturates and the warning appears.

## 5-minute technical explanation (decisions and alternatives)
- **Why our own `BitArray`:** `java.util.BitSet` would be the "framework doing the algorithm". ArchUnit forbids
  it, and `HashSet`, in assessed code.
- **Why two hashes and double hashing:** k independent strong hashes would cost k passes over the key; double
  hashing costs two.
- **Why a coprime step rather than an odd one:** "odd" only guarantees `gcd(b, m) = 1` when m is a power of two.
  Our `m = n × bits-per-element` is usually composite with odd factors (for example `100,000 = 2⁵·5⁵`), so the
  general fix is needed.
- **Alternatives considered for the probe sequence:**
  - Round m up to a prime: then every step in `[1, m − 1]` is coprime and uniform, but it changes the sizing.
  - Enhanced double hashing (`a + i·b + (i³ − i)/6`): avoids some collapse, but does not guarantee distinct
    positions for arbitrary m.
  - Rejection of repeated positions: correct, but more complex and variable-time.
- **Why a seeded hash:** reproducible tests and benchmarks, and independent filters per seed.
- **Alternatives:**
  - Counting Bloom filter: supports deletion, at about 4× the memory.
  - Cuckoo filter: supports deletion and has better locality, but is more complex.
  - Exact `HashSet` or a database index: no false positives, but much more memory.
  - In this product, exactness matters, so the filter is only ever a pre-screen.
