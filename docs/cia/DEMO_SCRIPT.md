# Demonstration script (about 8 minutes)

For the CIA demonstration of `cia-demo` (Track C, D-31). Everything shown is synthetic. Start with
`./gradlew :cia:cia-demo:bootRun` and open http://127.0.0.1:8090/.

## 0:00 — The problem (1 minute)
THE FINE PRINT helps people **understand** what their documents say. Any statement of the form "your policy says …"
must be backed by text that really is in the document. The question on screen, *does the document really say
that?*, is the trust problem this slice solves. The check runs on the Java server; the page only displays results.

## 1:00 — One evidence check (3 minutes)
1. **Verified quotation.** Load it and run the check.
   - Stage 1: KMP, called through the `QuoteVerifier` contract, finds exactly one occurrence. Point out the offsets
     (code points) and the outlined clause 4.2. Match class `EXACT_UNIQUE`.
   - Stage 2: every clause is screened. The Bloom filter says "definitely absent" for three clauses, so no lookup is
     needed. For the "possibly present" ones, the wording is compared exactly: 4.2 and 4.3 are identical to
     reference entries.
   - Stage 3: read the three parts aloud. Stress **Not established**: identical wording is not identical meaning,
     and nothing here says the clause applies, is valid, or comes from a genuine, current document.
2. **Looks standard, but is not.** Clause 4.1 says 17 days; the library has 15 and 21. The Bloom filter says
   "possibly present", and the exact comparison rejects it. This is why a Bloom hit is never reported as a match.
3. **Appears twice.** "renewal" occurs in two clauses: present, but `AMBIGUOUS`, so not a uniquely verified quote.

## 4:00 — India-first text (1.5 minutes)
4. **हिन्दी: verified.** A Devanagari quotation is verified and its clause matches a Hindi reference entry.
5. **हिन्दी: part of a syllable.** `दाव` occurs only inside `दावे`; accepting it would cut the consonant from its
   vowel sign. The result is `MISALIGNED`, not verified, and the reason names U+0947. Say plainly that this is a
   conservative boundary check, not full Unicode grapheme segmentation; where it cannot be sure, it refuses.

## 5:30 — How it works (2 minutes)
Scroll to **How it works**.
- KMP panel: type a quote such as `aab` into a text of `a`s. Show the prefix function, and the comparison count
  staying under the 2n bound. Mention the benchmark result: `String.indexOf` is faster on ordinary text, while KMP's
  bound holds on adversarial input ([BENCHMARKS.md](BENCHMARKS.md)).
- Bloom panel: load the example (target 30 %, so false positives appear). Select a probe to outline its k bit
  positions. Point out that one member was submitted twice: the panel reports submissions and distinct members
  separately, and the expected rate uses the distinct count.

## 7:30 — Limits and close (30 seconds)
- Exact matching only: no normalisation, so a differently encoded accent is "not found", with a hint.
- The reference library is synthetic and small; at this size an exact set alone would do. A Bloom filter pays off
  when the reference collection is large and the exact store is remote ([DATA.md](DATA.md)).
- The assessed algorithms are isolated from the production platform, which uses standard-library adapters behind
  the same contracts.

## Likely examiner changes to inputs
- Edit one character of clause 4.2 and re-run: stage 2 turns "definitely absent" (or a rejected false positive).
- Quote across two lines: the occurrence is attributed to both clauses.
- Paste text in another script (for example Thai): mid-word boundaries are "cannot establish", so not verified.
