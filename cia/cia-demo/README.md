# cia-demo — Evidence Integrity Lab

A runnable demonstration of the assessed algorithms (Track C, D-31). The browser UI calls the Java backend; the
algorithms run on the server.

```bash
./gradlew :cia:cia-demo:bootRun
```

Then open http://127.0.0.1:8090/. (If port 8090 is taken: `./gradlew :cia:cia-demo:bootRun --args=--server.port=8095`.)

## The primary experience: one evidence check

The page opens on a single workflow that answers *"does the document really say that?"*:

1. **Verify the quotation.** The claimed quotation is matched against the source through the `core-api`
   `QuoteVerifier` contract, implemented by the assessed KMP matcher (`KmpQuoteVerifier`). Every exact occurrence is
   then checked so that it does not start or end inside a character (see *Boundary check* below). The outcome is a
   `core-api` `MatchClass`: `EXACT_UNIQUE`, `AMBIGUOUS` (more than one accepted occurrence) or `MISALIGNED` (code
   points occur, but no occurrence has established boundaries); or not found.
2. **Screen the clause wording.** The source is split into clauses (one per non-blank line; a leading number such as
   `4.2` becomes the id). Each clause's exact wording is fingerprinted (SHA-256) and screened with the assessed Bloom
   filter against a synthetic library of reference clause wordings ([DATA.md](../../docs/cia/DATA.md)). Every
   "possibly present" answer is confirmed by comparing the wording itself. "Definitely absent" needs no lookup.
3. **State the evidence**, in three parts:
   - **Verified:** whether and where the quotation occurs in the source (code-point offsets and clause).
   - **Screened:** whether the quoted clause's wording is identical to a reference entry, after exact confirmation.
   - **Not established:** meaning, applicability, legal validity, authenticity and currency of the document; and,
     when a reference match exists, that identical wording is not semantic or legal equivalence.

Synthetic examples on the page: a verified quotation, a clause that *looks* standard but is a Bloom false positive
(17 days, where the library holds 15 and 21), a quotation that appears twice, a verified Hindi quotation, and a Hindi
quotation that stops inside a syllable.

Below the workflow, **How it works** keeps the two algorithm panels for technical explanation: KMP with its prefix
function and comparison count, and the Bloom filter with its bit array.

| Endpoint | Purpose |
|---|---|
| `POST /algo/evidence-check` `{sourceText, claimedQuotation}` | The workflow above: verification, clause screening, three-part statement |
| `POST /algo/quote-match` `{text, quote}` | KMP explanation panel: every code-point occurrence, comparisons against the 2n bound, the prefix function |
| `POST /algo/document-links` `{documents: [{id, identifiers[]}]}` | A3 panel: families of documents that share identifiers (disjoint-set forest), the identifier behind every link, broad-identifier warnings |
| `POST /algo/membership` `{fingerprints, probes, targetFalsePositiveRate, expectedInsertions?, seed?}` | Bloom explanation panel: screening, then exact confirmation per probe; bit pattern (≤ 4,096 bits) |

- Loopback only; no database; no outbound calls; synthetic input only. The reference library is built once at
  start-up and only read afterwards; nothing from a request is stored.
- Invalid input returns `application/problem+json` (400) without echoing the submitted text.
- Code walk-through: `evidence/EvidenceCheckController` → `EvidenceCheckService` → `QuoteVerifier`
  (`KmpQuoteVerifier`, wired in `DemoConfiguration`) and `BoundaryCheck`; then `ClauseSegmenter` and
  `ReferenceClauseLibrary` (`BloomFilter`, then exact comparison).

## Boundary check (conservative, not full grapheme segmentation)

Exact matching works on code points, so a match can begin or end inside what a reader sees as one character: for
example `न` matches inside `नि` (न + vowel sign ि). `BoundaryCheck` rejects an occurrence when a boundary would:

- separate a combining mark (vowel signs, nukta, accents, variation selectors) from its base;
- split a virama conjunct in the nine covered Indic scripts (virama followed by a letter);
- split a ZWJ/ZWNJ, emoji-modifier or tag sequence, or a CR LF pair.

Boundaries next to text in scripts it does not cover (for example Thai or Hangul), and adjacent flag
(regional-indicator) characters, are answered **"cannot establish"**, which is treated exactly like a split. This is
**not** Unicode UAX #29 or Indic syllable segmentation, and no claim of full grapheme correctness is made. The
platform specification requires a pinned segmenter for that (SYSTEM_DESIGN 2.1, D-26).

In the source view, a rejected occurrence is drawn widened to the browser's grapheme boundaries so the text stays
readable. The offsets listed are always the server's.

## Duplicate members in the Bloom panel

`/algo/membership` reports `insertionAttempts` (submitted, repeats included), `distinctFingerprints` and
`duplicateAttempts`. Every attempt is passed to the filter (a repeat sets no new bits). The default capacity, the
expected false-positive rate and the over-capacity flag all use the **distinct** count. (The assessed
`BloomFilter.insertionCount()` counts attempts, so it is deliberately not used for these figures. The assessed class
is unchanged.)

## Testing
- `./gradlew :cia:cia-demo:test` runs `EvidenceCheckServiceTest` (13), `BoundaryCheckTest` (8),
  `ReferenceClauseLibraryTest` (7), `DemoServicesTest` (9), `DocumentLinkServiceTest` (9) and `DemoHttpTest` (8).
- They cover: the discovered sub-syllable defect and virama, ZWJ/ZWNJ, nukta, Tamil, Bengali and uncovered-script
  cases; NFC and case near-miss hints; ambiguity; clauses spanning a quotation; every reference wording screening
  as identical (no false negatives); held-out variants never matching; a resolved Bloom false positive; duplicate
  attempts versus distinct members; all three endpoints over HTTP; problem responses.
- **Gap:** there is no automated browser test (the repository has no browser-automation tooling, and none was
  added). Rendering, the responsive layout at 375 px and keyboard operation were checked manually in a browser.
