# Deliverable D — demonstration evidence

Everything here was produced by running the application from this repository (JDK 21.0.4, Windows 11,
`./gradlew :cia:cia-demo:bootRun`, Chrome for the screenshots). All input is synthetic.

## 1. Working application
`./gradlew :cia:cia-demo:bootRun`, then http://127.0.0.1:8090/ (instructions: [README](../../../README.md)).

| Screenshot | Shows |
|---|---|
| [01-evidence-check-verified.png](screenshots/01-evidence-check-verified.png) | Full page after the *Verified quotation* example: KMP verdict `EXACT_UNIQUE` at code points [153, 223) in clause 4.2; Bloom screening of 6 clauses (3 definitely absent, so no lookup; 3 exact comparisons: 2 identical, **1 false positive rejected**); the three-part evidence statement |
| [02-hindi-boundary-rejected.png](screenshots/02-hindi-boundary-rejected.png) | Quotation `दाव` is rejected (`MISALIGNED`): its only occurrence would cut the consonant from the vowel sign े |
| [03-input-validation-message.png](screenshots/03-input-validation-message.png) | Browser-side message for an empty quotation (the server independently rejects it with HTTP 400, see section 3) |
| [04-bloom-panel.png](screenshots/04-bloom-panel.png) | Bloom panel at target p = 30 %: possibly-present probes resolved to *Confirmed present* or *False positive*; repeat submission reported; bit array |
| [05-kmp-panel.png](screenshots/05-kmp-panel.png) | KMP panel with the prefix function and the comparison count against the 2n bound |

## 2. Example inputs and outputs
[`data/samples/requests`](../../../data/samples/requests) (input, description, expected status) and
[`data/samples/responses`](../../../data/samples/responses) (recorded server output). Reproduce and verify with
`python ops/scripts/replay_samples.py --check` (demo running): **21 of 21 match**.

| Sample | Input | Output (abridged) |
|---|---|---|
| 01 verified | quotation inside clause 4.2 | `VERIFIED`, `EXACT_UNIQUE`, [153, 223), clause 4.2 identical to reference `INS-CLAIM-NOTICE-025` |
| 02 Bloom false positive | "A grace period of 17 days…" | `VERIFIED`; clause 4.1 Bloom *possibly present*, exact comparison **false positive** (library holds 15 and 21 days) |
| 03 ambiguous | `renewal` | `AMBIGUOUS`, two accepted occurrences (clauses 4.1 and 4.3) |
| 04 not found | a paraphrase | `NOT_FOUND` |
| 05 case near-miss | lower-case quotation | `NOT_FOUND` with a hint that it would match if case were ignored |
| 06 / 07 Hindi | `दावे की सूचना 30 दिनों के भीतर` / `दाव` | `VERIFIED` [56, 86) / `BOUNDARY_NOT_ESTABLISHED` (`MISALIGNED`) |
| 08 empty source | empty string | `NOT_FOUND` (valid input, not an error) |
| 10 / 11 / 12 KMP | `aa` in `aaaa`; a²⁰b in a²⁰⁰; `fee` | matches at 0, 1, 2 (4 comparisons, bound 8); no match, **380 comparisons, bound 2n = 400**; matches at 0, 10, 15 |
| 20 / 21 / 22 Bloom | 10 submissions (1 repeat), 12 probes | p = 30 %: m = 23, k = 2, fill 47.8 %; p = 1 %: m = 87, k = 7, fill 55.2 %; capacity 2: m = 20, fill 100 %, `overCapacity: true`, expected FPR 73.6 % |

## 3. Error handling
Samples 90–96 each return **HTTP 400** with `application/problem+json`; the submitted text is never echoed back
(asserted by `DemoHttpTest`). Examples: empty quotation → `claimedQuotation must not be empty`; malformed JSON →
`The request body is not valid JSON for this endpoint.`; p = 0.9 → `targetFalsePositiveRate must be between 0.0001 and 0.5`;
501 probes → `between 1 and 500 probes`; a quotation of 2,001 code points; a missing field; an empty pattern for KMP.
Unit-level failure handling (null, empty pattern, invalid Bloom parameters, out-of-range bits, oversized filters) is
in the [test report](test-report.md). Cases that must **not** be silently wrong are surfaced explicitly: sub-character
matches (`MISALIGNED`), repeated quotes (`AMBIGUOUS`), Bloom hits (always confirmed exactly), saturation
(`overCapacity`), duplicate members (counted separately).

## 4. Tests
[test-report.md](test-report.md): **101 tests, 0 failures** (regenerate with `ops/scripts/summarise_tests.py`).

## 5. Performance
- Algorithm benchmarks (JMH, three input sizes, baselines, five seeds for the false-positive rate):
  [BENCHMARKS.md](../BENCHMARKS.md); raw data and charts in [`cia/cia-bench/results`](../../../cia/cia-bench/results);
  report figures in [`docs/cia/submission/figures`](../submission/figures).
- End-to-end HTTP latency, from `ops/scripts/measure_api_latency.py` (200 runs after 30 warm-ups, a new connection per
  request, loopback, Windows 11 laptop): [api-latency.csv](api-latency.csv).

| Scenario | median ms | p95 ms | max ms |
|---|---|---|---|
| evidence-check, 6-clause policy | 4.24 | 27.79 | 32.98 |
| quote-match, n = 200 adversarial | 3.89 | 24.72 | 27.40 |
| membership, 10 members / 12 probes | 2.95 | 25.67 | 26.98 |
| evidence-check, 400 clauses (limit) | 16.45 | 30.92 | 33.19 |
| quote-match, n = 100,000 (limit) | 14.74 | 27.46 | 32.19 |

*Caveat:* the p95 values cluster around 25–30 ms for every scenario regardless of the work done, which points to
connection or OS-scheduling overhead on this machine rather than server compute (the server reports its own compute
time as `serverMicros`, for example 32 µs for the n = 200 adversarial search). Treat the medians as the indicative
figure; this is not an algorithm benchmark.
