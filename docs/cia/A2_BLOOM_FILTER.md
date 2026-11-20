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
- **Double hashing (Kirsch–Mitzenmacher):** compute `h1 = H(key, seed)` and `h2 = H(key, seed ⊕ φ) | 1` once.
  The positions are `g_i = (h1 + i·h2) mod m` for `i = 0..k−1`, computed as unsigned 64-bit arithmetic. Forcing
  `h2` odd stops the sequence collapsing when `h2` shares a factor with an even `m`. This is known to give the
  same asymptotic false-positive rate as k independent hashes, at the cost of two hash computations.

## Pseudocode
```
add(key):          h1 = H(key, s1); h2 = H(key, s2) | 1
                   for i in 0..k-1: set bit (h1 + i*h2) mod m
mightContain(key): same positions; return false at the first clear bit; else true
```

## Important invariants
1. A bit, once set, is never cleared (there is no deletion).
2. `add(x)` sets exactly the bits that `mightContain(x)` checks: the same seeds and the same position formula
   (test `addSetsExactlyThePositionsThatAreProbed`).

## Correctness argument
- **No false negatives:** after `add(x)`, all `k` positions of `x` are 1 (invariant 2), and bits never go back
  to 0 (invariant 1). So `mightContain(x)` finds no clear bit and returns `true`. Tested with 20,000 keys under
  three seeds.
- **False-positive rate:** for a key never added, each of its positions is 1 with probability about
  `1 − (1 − 1/m)^{kn} ≈ 1 − e^{−kn/m}`. It is reported present only if all `k` are 1:
  `p ≈ (1 − e^{−kn/m})^k`. Minimising over `k` gives `k = (m/n) ln 2` and `m = −n ln p / (ln 2)²`, the formulas
  in `BloomParameters`. At the optimum, about half the bits are set (test: fill ratio 0.45–0.55 at capacity).
- **Empirical check:** at `n = 10,000` and `p = 0.01`, 200,000 never-inserted probes give a measured rate within
  0.7–1.3 % (test band). The binomial standard error is about 0.02 %, so the band is wide enough for
  double-hashing effects. The experiment in [BENCHMARKS.md](BENCHMARKS.md) measures 1,000,000 probes per
  configuration.

## Complexity
- `add` / `mightContain`: **O(k)** bit operations plus O(|key|) to hash (two hash passes).
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
- **Modulo reduction:** `x mod m` on 64-bit values has negligible bias for `m` far below 2⁶⁴.
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
  `k = round(2.6 · 0.693) = 2`, seed 7. After inserting 10 keys, 16 of 26 bits are set (61.5 %).
- **Probes:**
  - 3 known fingerprints → *possibly present* → **confirmed**.
  - 6 unknown probes hit a clear bit → *definitely absent*.
  - 3 unknown probes (`LN-PER-2026-7399`, `SUB-OTT-2026-9099`, `POL-HLT-2025-0001`) find both their bits set →
    *possibly present* → the exact check says **false positive**.

  For example, `LN-PER-2026-7399` maps to bits 8 and 17, both set by other fingerprints.

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
- **Why a seeded hash:** reproducible tests and benchmarks, and independent filters per seed.
- **Alternatives:**
  - Counting Bloom filter: supports deletion, at about 4× the memory.
  - Cuckoo filter: supports deletion and has better locality, but is more complex.
  - Exact `HashSet` or a database index: no false positives, but much more memory.
  - In this product, exactness matters, so the filter is only ever a pre-screen.
