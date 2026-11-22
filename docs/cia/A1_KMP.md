# A1 — Exact quote matching with Knuth–Morris–Pratt (Unit 4)

**Code:** `cia/cia-algorithms/src/main/java/com/thefineprint/cia/algorithms/text/`
(`KmpMatcher`, `KmpResult`, `KmpQuoteVerifier`). **Tests:** `KmpMatcherTest` (11), `KmpQuoteVerifierTest` (6).
**Status:** assessed implementation (Track C, D-31). Never a production dependency of the platform.

## 30-second explanation
When THE FINE PRINT says "your document says X", it must prove that X really occurs in the document. KMP finds
every exact occurrence of a quote in a text in time proportional to their combined length. It never re-reads
text it has already matched, because it precomputes, for the quote alone, how far it can safely fall back after
a mismatch.

## Problem statement
Input: a text `T` of `n` symbols and a non-empty pattern `P` of `m` symbols. Output: every start position `s`
with `T[s .. s+m-1] = P[0 .. m-1]`, in ascending order, including overlapping ones. In the product this is the
check that stops a language model from inventing a "quote" that the document does not contain.

## Why KMP is appropriate
- Exact matching is the right model: provenance needs proof of presence, not similarity (SYSTEM_DESIGN 2.3).
- Its worst-case time is linear, O(n + m), even on adversarial input such as `aaaa…a` against `aa…ab`, where the
  naive algorithm is O(n·m).
- It reports every occurrence, so the caller can decide whether a quote is unique (`EXACT_UNIQUE`) or
  ambiguous (SYSTEM_DESIGN 5.3).

## Input and output contracts (as implemented)

| Method | Contract |
|---|---|
| `KmpMatcher.prefixFunction(int[] pattern)` | Returns `pi` with `pi[i]` = the length of the longest proper prefix of `pattern[0..i]` that is also its suffix. Empty pattern → `IllegalArgumentException`; `null` → `NullPointerException` |
| `KmpMatcher.search(int[] text, int[] pattern)` | Returns a `KmpResult` with all start offsets (ascending, overlapping included), the prefix function and the number of symbol comparisons. Empty text → no matches. `m > n` → no matches (and 0 comparisons). Empty pattern → `IllegalArgumentException` |
| `KmpQuoteVerifier.find(String l1, String quote)` | The `core-api` `QuoteVerifier` SPI. Converts both strings to **Unicode code points** and returns `Span[start, start+m)` per occurrence, as an immutable list. No case, whitespace or quote-mark folding: normalisation is the caller's job |

Symbols are `int` code points, not Java `char`s, so an emoji or other supplementary character counts as one
symbol, and offsets match the platform's code-point anchors (SYSTEM_DESIGN 2.2).

## Plain-language explanation
Suppose we have matched `abab` of the quote `ababc` and then see a mismatch. The naive method shifts by one and
starts comparing again. KMP notices that the `ab` we just matched is also the start of the quote, so it keeps
those two symbols as already matched and carries on from there. The prefix function records, for each length of
partial match, how much of it is still useful after a mismatch.

## Pseudocode (mirrors the code exactly)
```
prefixFunction(P):                      // m = |P| >= 1
    pi[0] = 0; k = 0
    for i = 1 .. m-1:
        while k > 0 and P[i] != P[k]: k = pi[k-1]      // fall back to a shorter border
        if P[i] == P[k]: k = k + 1
        pi[i] = k
    return pi

search(T, P):
    pi = prefixFunction(P); if m > n: return []
    q = 0                                               // symbols of P currently matched
    for i = 0 .. n-1:
        while q > 0 and T[i] != P[q]: q = pi[q-1]       // each step counts one comparison
        if T[i] == P[q]: q = q + 1                      // counts one comparison
        if q == m: report i-m+1; q = pi[q-1]            // continue: overlapping matches
```

## Important invariants
1. **Prefix function:** at the top of iteration `i`, `k = pi[i-1]`, the longest border of `P[0..i-1]`. The
   fallback `k ← pi[k-1]` enumerates *all* borders of that prefix in decreasing length, because a border of a
   border is a border.
2. **Search:** after processing `T[i]`, `q` is the length of the longest prefix of `P` that is a suffix of
   `T[0..i]` (and `q < m` at the start of the next iteration).

## Correctness argument
- *Prefix function.* Any border of `P[0..i]` other than the empty one has the form "border of `P[0..i-1]`" plus
  `P[i]`. The `while` loop tries the borders of `P[0..i-1]` from longest to shortest and stops at the first one
  that `P[i]` extends; that one is the longest. If none extends it, `k` is 0, and `P[i] == P[0]` decides between
  1 and 0.
- *Search.* By the same argument applied to the text, invariant 2 is maintained. When `q` reaches `m`, `P` is a
  suffix of `T[0..i]`, so it occurs at `i - m + 1` (**soundness**). Conversely, if `P` occurs ending at `i`, then
  `P` is a suffix of `T[0..i]`, and since `q` is the *longest* such prefix, `q = m` at that step
  (**completeness**). Resetting `q` to `pi[m-1]` rather than 0 keeps invariant 2, so overlapping occurrences are
  not missed.
- *Tested against a reference:* `agreesWithNaiveReferenceOnRandomInputs` compares against the obviously correct
  O(n·m) algorithm on 5,000 seeded random cases over 1–3 letter alphabets, which maximise repeats and overlaps.

## Complexity
- **Time:** O(m) for the prefix function and O(n) for the search, so **O(n + m)**. In the search, every `while`
  iteration strictly decreases `q`, and `q` increases by at most 1 per text symbol, so the fallbacks total at most
  n. The code counts comparisons; the test `comparisonCountIsLinearEvenOnAdversarialInput` asserts
  comparisons ≤ 2n on `a^100000` against `a^999 b`.
- **Auxiliary space:** **O(m)** for `pi`. The output array is O(occ) (up to n − m + 1). The adapter's code-point
  conversion uses O(n + m), which belongs to the adapter, not the algorithm.

## Edge cases (all tested)
- Empty text → `[]`.
- Pattern longer than text → `[]`.
- Pattern equal to text → `[0]`.
- Single symbol.
- Match at the last position.
- Overlaps (`aa` in `aaaa` → 0, 1, 2).
- Surrogate pairs (`😀`).
- Devanagari combining signs (matched as code points).
- Empty pattern → exception (the SPI contract).
- `null` → exception.

## Known limitations
- Exact only: one changed character (OCR noise, curly versus straight quotes, case) means "not found". That is
  intentional for evidence, and documented in the demo UI.
- No grapheme alignment: a match may start inside a grapheme cluster (for example on a Devanagari vowel sign).
  The platform design leaves boundary enforcement to the caller (SYSTEM_DESIGN 2.3), and this implementation
  follows the SPI.
- One quote per call: for many quotes against one long document, a suffix array would amortise preprocessing
  (CIA matrix A1). It is not implemented in this slice.

## Baseline comparison
See [BENCHMARKS.md](BENCHMARKS.md). The expectation stated before measuring (CIA matrix A1 point 8) was that
JDK `String.indexOf`, a vectorised HotSpot intrinsic, would be faster on ordinary text. The measured results are
reported there as they are, including where KMP loses.

## Example walkthrough (actual trace)
Pattern `abab` → `pi = [0, 0, 1, 2]`. Text `ababcabab` (n = 9):

| i | T[i] | action | q after |
|---|---|---|---|
| 0 | a | a = P[0] | 1 |
| 1 | b | b = P[1] | 2 |
| 2 | a | a = P[2] | 3 |
| 3 | b | b = P[3] → **match at 0**; q ← pi[3] = 2 | 2 |
| 4 | c | c ≠ P[2] → q ← pi[1] = 0; c ≠ P[0] | 0 |
| 5 | a | a = P[0] | 1 |
| 6 | b | | 2 |
| 7 | a | | 3 |
| 8 | b | **match at 5**; q ← 2 | 2 |

Result: offsets `[0, 5]`, **10 comparisons** (one per symbol plus one fallback at i = 4) ≤ 2n = 18. The naive
method would also re-read positions 1–3 after the first alignment.

## 2-minute walkthrough (for the demo)
1. Open the Evidence Integrity Lab and load the synthetic example.
2. The server converts the text and quote to code points and runs `KmpMatcher.search`.
3. The UI marks every occurrence. The example finds one match, because clause 4.3 says "If **t**he
   Policyholder…" in lower case, and exact matching does not fold case.
4. Open "How KMP ran" and point at the π row: every value is a fallback length.
5. Compare the *Comparisons* figure with the *2n* bound.

## 5-minute technical explanation (decisions and alternatives)
- **Why `int[]` code points:** offsets must equal the platform's anchor offsets, and surrogate pairs must not be
  split.
- **Why the counter:** the comparison count is the evidence for linearity shown in the demo and asserted in tests.
  Its cost is one increment per comparison.
- **Why a separate result record:** it returns offsets, π and the count in one immutable value, with defensive
  copies of the arrays.
- **Alternatives:**
  - Naive: simple, but O(n·m) worst case.
  - `String.indexOf`: fast, but not our implementation, and it works on UTF-16 offsets.
  - Boyer–Moore: sublinear on average, but more complex shift tables, and it does not report overlaps as
    naturally.
  - Suffix array + LCP: better when one text receives many queries, at the cost of O(n log n) preprocessing.
