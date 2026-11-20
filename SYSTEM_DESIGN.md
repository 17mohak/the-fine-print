# SYSTEM_DESIGN.md

v0.2.1 — Architecture **APPROVED v0.2.1 direction** (D-21, 2026-11-20, subject to unresolved decisions, limitations and review gates) · Implementation **AUTHORISED FOR TRACK P, P0 ONLY** (P1–P7 and Track C C0–C3 NOT AUTHORISED) · Opus handover **READY FOR P0 ONLY** · Consumer validation **WAIVED AS A DEVELOPMENT GATE, NOT PASSED**. *This design describes P1+ components; none of them (encryption, RLS, worker/broker, canonicalisation, provenance, search index) may be implemented under the P0 authorisation.* (v0.1 in `_archive/architecture_v0.1/`.) 2026-11-20. Companion: ARCHITECTURE_OVERVIEW (structure, change summary), SECURITY_AND_PRIVACY (keys, RLS), CIA matrix.

## 1. Document lifecycle and states
`RECEIVED → VALIDATED → STORED → QUEUED → EXTRACTING → CANONICALISED → CLASSIFIED → SEGMENTED → ENRICHED → READY`; side states `QUARANTINED`, `FAILED_<STAGE>`, `NEEDS_REVIEW`, `SUPERSEDED`. Originals are immutable and identified by a **per-user keyed blind index** (HMAC-SHA256 under a per-user hash key derived from the UMK, SECURITY section 3); a raw SHA-256 of the plaintext is never stored as queryable metadata (it would let anyone with database access test whether a known document is in a user's vault) — an integrity SHA-256 may be kept inside encrypted metadata. Derived artefacts are encrypted, versioned by `extraction_run_id`, re-creatable. **Java core validation (no parsing of untrusted structure):** size cap (default 25 MB), magic-byte type check, extension/MIME consistency, page cap enforced by worker result, keyed blind-index hash, dedup per user via exact blind-index match. Everything that interprets file structure (PDF, images) happens in the sandboxed worker. Encrypted PDFs → ask user. Near-duplicate and family linking use the SPIs in §4 (production adapters).

## 2. Canonicalisation (exact specification) — v0.2.1
**2.1 Streams and layers**
- **L0 — the exact worker token stream (immutable).** Stored (encrypted) exactly as received from the worker after schema validation. Token = `{id, page, bbox[x0,y0,x1,y1] in PDF points (origin top-left, rotation normalised to 0), text, confidence 0..1, source TEXT_LAYER|OCR, readingOrder, charBoxes? (optional per-char boxes), cellRef?}`. **L0 is never merged, split, normalised, reordered, re-timed or edited by any component.** Validation may *flag* tokens (e.g. `startsWithExtend`, `crossTokenCluster`) in a separate side table; it never rewrites them. A content hash of L0 is recorded and re-checked at read time (test G9).
- **S0 — derived reading-order string (reversible).** S0 = concatenation of L0 token texts in `readingOrder`, with *synthetic separators* between tokens by layout rule (space between words on a line, `\n` between lines, `\n\n` at paragraph gaps). Separators belong to no token. **Separator policy:** no separator is inserted before a token whose first code point is Grapheme_Extend, ZWJ, ZWNJ or SpacingMark (treated as continuing the previous token's word; recorded as `joinedByPolicy` in the map) so a stray combining mark can never attach to a synthetic space. `s0_map` = ordered segments `{s0Start, s0End, tokenId | SYNTHETIC, tokenCharStart, tokenCharEnd}`; S0 and `s0_map` reconstruct L0 exactly (round-trip test).
- **G — grapheme segmentation over S0 (not over tokens).** Extended grapheme cluster boundaries are computed over the concatenated S0 string so clusters that span token boundaries are found. **Full UAX #29 coverage is required, not only combining marks and ZWJ/ZWNJ:** CR LF; Control; Hangul syllable sequences (L/V/T/LV/LVT); Extend (combining marks, ZWNJ); ZWJ with Extended_Pictographic (emoji ZWJ sequences, modifier sequences); SpacingMark; Prepend; Regional_Indicator pairs (flags); and the **Indic conjunct rule (GB9c, Unicode 15.1+)** so Devanagari/Bengali/Gujarati/Malayalam etc. consonant–virama–consonant conjuncts are single clusters. **Segmenter is pinned** (library + Unicode version stored as `segmenter_id`, e.g. ICU4J with Unicode >= 15.1). [ASSUMPTION — verify in P1: JDK 21 `\X`/`BreakIterator` implements Unicode 15.0 and lacks GB9c; do not rely on it for Indic conjuncts.] A segmenter change bumps `canon_version`. Each cluster record = `{s0Start, s0End, tokenSpans[(tokenId, charStart, charEnd)...], crossToken}`; **merged geometry is derived on demand** as the union of the participating tokens' boxes (a view, never stored back into L0).
- **L1 — canonical text (`canon_version = canon-1.0`).** Derived from S0 by deterministic, versioned, reversible transformations applied **per extended grapheme cluster of S0** (so a combining mark in a different OCR token is normalised together with its base):
  1. Unicode **NFC** per cluster. [ASSUMPTION — verify by property test P2 on the corpus: per-cluster NFC equals whole-stream NFC. If violated for any script, widen the normalisation unit and coarsen the edit-map runs for that script; record in an ADR.]
  2. **Enumerated presentation-ligature expansion** only: U+FB00 ff, FB01 fi, FB02 fl, FB03 ffi, FB04 ffl, FB05/FB06 st. (Not NFKC.)
  3. Remove U+00AD. **Preserve U+200D ZWJ and U+200C ZWNJ.**
  4. Whitespace: NBSP/tab/other Zs to U+0020; horizontal runs collapse to one U+0020; paragraph break = `\n\n`; soft wrap inside a paragraph = single U+0020.
  5. **End-of-line hyphenation:** line ending in `-` (U+002D/U+2010) followed by a lowercase-initial line in the same paragraph/column is joined unless the joined word is on the document's own hyphenated-compound list; each join recorded `HYPHEN_JOIN` with `ambiguous` flag.
  6. No case folding, quote/dash folding, digit-shape changes or OCR "corrections" in L1.
- **L2 — match-key (lossy; candidate retrieval only).** L1 + casefold + quote/dash/whitespace folds + compatibility folds. **L2 may suggest where to look. L2 can never independently establish a DOCUMENT_FACT.** An L2 candidate stays `FUZZY_UNVERIFIED` unless an exact L1 match (2.3) independently succeeds.
**2.2 Offsets and alignment.** Anchors use **Unicode code-point offsets** `[start, end)` into L1 plus `canon_version`, `segmenter_id` and `canonical_hash = HMAC-SHA256(per-user hash key, UTF-8(L1))`. L1 cluster boundaries are the images of S0 cluster boundaries (cached `l1ClusterBoundaries`); **both anchor ends must fall on L1 cluster boundaries**; otherwise the match is `MISALIGNED` (display may offer the expanded span as a suggestion, never as a verified anchor). UTF-16 conversion helpers (Java/JS) are round-trip tested including astral characters.
**2.3 Production quote verification = exact matching on L1.** Steps: (1) apply the *same deterministic L1 pipeline* to the quote (NFC per cluster, ligature expansion, soft-hyphen removal, whitespace rules; no folding); (2) find **all exact code-point occurrences** in L1 via the production `QuoteVerifier`; (3) **enforce L1 cluster-boundary alignment** of both ends; (4) apply scope/uniqueness rules (5.3). For ambiguous hyphen joins the verifier additionally checks the *alternate L1 variant* generated by flipping the ambiguous joins with the same pipeline; success on only the alternate variant is flagged `hyphen_ambiguous`. No case folding, no punctuation folding, no edit distance, no L2 in this path.
**2.4 Edit map (reversibility).** Runs `{canonStart, canonEnd, s0Start, s0End, editKind in {IDENTITY, NFC, LIGATURE, WS_COLLAPSE, PARA_BREAK, HYPHEN_JOIN, SOFT_HYPHEN_DROP}}`, sorted, non-overlapping, gap-free over L1. Canonical to S0 is a binary search; S0 to tokens is `s0_map`. Length-changing runs map whole-run to whole-run (sub-run precision not claimed). **Invariants (property-tested):** monotonic; gap-free; canon(canon(x)) = canon(x); every canonical range maps to at least one token span or a synthetic separator only; forward/backward mapping reproduces the same spans; L0 hash unchanged by the pipeline.
**2.5 Page and bounding-box mapping.** canonical range, then S0 range, then clusters, then `tokenSpans`, then token boxes; group by (page, line) and union per line into rect lists; ranges crossing pages give `page_from..page_to` with rect lists per page. Partial-token ranges use `charBoxes` if present, else a proportional split flagged `bbox_approx`. Cross-token clusters union the participating tokens' boxes and are flagged `cross_token`. Table cells: anchors list `cellRef`.
**2.6 Anchor lifecycle and invalidation.** An anchor is valid only against `(document_version_id, extraction_run_id, canon_version, segmenter_id, canonical_hash)`. New `document_version`: old anchors stay with the old version; a re-anchor job re-verifies quotes exactly on the new L1 (one aligned match: new anchor with `derived_from`; zero or many: assertion `STALE`) — **no silent re-pointing.** New `extraction_run`, `canon_version` or `segmenter_id`: same procedure. **Human OCR correction** creates a `canon_revision`; anchors are re-mapped through the correction script, with audit. `canonical_hash` mismatch at read: anchor refused (`ANCHOR_INVALID`). Repeated quotations: 5.3.
**2.7 Test catalogue (must exist and pass before gate G1; synthetic fixtures).**
| # | Case | Expected |
|---|---|---|
| D1 | Devanagari nukta: U+0958 vs U+0915 U+093C | equal after NFC; exact verify |
| D2 | Combining-mark order variants | NFC canonical order; verify |
| D3 | Quote begins at a dependent vowel sign (matra) | rejected `MISALIGNED` |
| D4 | Conjunct with ZWJ/ZWNJ | ZWJ/ZWNJ preserved; ZWJ-less quote does not verify exactly |
| D5 | Mixed Hindi/English; Devanagari vs ASCII digits | no folding in L1 |
| D6 | Astral character / surrogates; UTF-16 and code-point round trip | lossless |
| L1 | PDF ligatures "ﬁnal", "ofﬁce", "ﬃ" vs quote | verifies via enumerated map |
| L2 | Document genuinely containing "ﬁ" and quote containing "ﬁ" | verifies (both canonicalised identically) |
| H1 | "exclu-⏎sion" | joins |
| H2 | "pre-⏎existing" where hyphenated form attested elsewhere | hyphen kept |
| H3 | "co-⏎payment" unattested | ambiguous flagged; alternate variant checked; flagged |
| H4 | Soft hyphen U+00AD | removed |
| O1 | OCR noise "perlod", 0/O, rn/m | exact fails; L2 may suggest; result `FUZZY_UNVERIFIED`; **never DOCUMENT_FACT** |
| O2 | Amount "₹5,OOO" vs "₹5,000" | blocked; amounts need exact digits |
| O3 | Low-confidence tokens in span | `LOW_CONFIDENCE` below threshold |
| R1 | Quote appears 3 times | all returned; clause scope selects; unique then verified |
| R2 | Quote appears twice in the cited clause | `AMBIGUOUS` unless declared multi |
| R3 | Quote spans page break | `page_from != page_to`, rect lists per page |
| W1 | NBSP/tab/line wrap | collapse; verifies |
| I1 | New version, quote still unique | new anchor with `derived_from` |
| I2 | New version, quote now twice | `STALE` |
| I3 | `canon_version` or `segmenter_id` bump | all anchors re-verified; none reused |
| **G1** | OCR emits a matra as a separate token after its base consonant | L0 byte-identical; cluster `crossToken`; anchor over base+matra aligns; quote starting at the matra `MISALIGNED` |
| **G2** | Indic conjunct (consonant+virama+consonant) split across tokens; quote starts at the second consonant | `MISALIGNED` under pinned Unicode >= 15.1 segmenter; test fails if segmenter lacks GB9c |
| **G3** | Emoji ZWJ sequence split across tokens | single cluster; quote splitting it `MISALIGNED` |
| **G4** | Regional-indicator pair (flag) split across tokens | single cluster |
| **G5** | Hangul L+V+T jamo sequence across tokens | single cluster; NFC composition correct |
| **G6** | CR LF | single cluster; no split |
| **G7** | Prepend character followed by base | single cluster |
| **G8** | Combining-mark token following a line break or space-separated token | separator policy prevents attaching the mark to a synthetic separator; `joinedByPolicy` recorded |
| **G9** | L0 immutability: hash of stored L0 equals hash of the validated worker stream after the full pipeline | equal |
| **G10** | Segmenter or Unicode version change | `canon_version` bump; anchors invalidated and re-verified |
| P1 | Property: idempotence, monotonic map, gap-free coverage, round trip | pass on randomised inputs |
| **P2** | Property: per-cluster NFC equals whole-stream NFC on the corpus | pass, else widen unit (ADR) |

## 3. Understanding pipeline
| Stage | Technique | Where | On failure |
|---|---|---|---|
| Extraction | worker per page: text layer else OCR; layout/tables | worker | page flagged; `NEEDS_REVIEW` |
| Canonicalisation | §2 | Java | reject malformed worker output |
| Classification | rules + small model/LLM, top-k + confidence | Java/gateway | UNKNOWN → `generic-agreement` |
| Segmentation | numbering/heading rules + layout; LLM only for ambiguity, offsets re-verified | Java | one flagged block |
| Extraction (fields/obligations) | schema-constrained LLM; each field must carry a verbatim quote | gateway | field = UNKNOWN |
| Verification | `QuoteVerifier` production adapter + deterministic money/date/percent parsing + arithmetic sanity | Java | downgrade/drop |
| Cross-references | regex + structure tree lookup | Java | list unresolved |
| Cross-document relations | identifier match via `DocumentLinker`; override/conflict proposed to user | Java | unresolved conflict surfaced |
| Knowledge join | rules selected by jurisdiction/entity class/product/doc date; **deterministic evaluation only for rules at V3+** (§7) | Java | "rule status: informational" |
| Interpretation | LLM on verified spans + rule excerpts; kind=INTERPRETATION | gateway | abstain |
Human review queue is for reviewers (corpus building, rule approval), not for end-user legal review in v1 (OPEN: legal).

## 4. Production SPIs (replace v0.1's direct use of algorithms)
`QuoteVerifier.find(l1, normalisedQuote) → List<Span>` (production: exact code-point matching against **L1** using the same deterministic normalisation as L1, followed by L1 grapheme-boundary enforcement; **never L2**); `ApproximateMembership` (production: exact set); `ReminderIndex` (production: DB-backed due-time index with `SKIP LOCKED`); `DocumentLinker` (production: per-user connected components over identifier edges). SPIs live in `core-api`. Assessed implementations in `cia-algorithms` may be wired only into `cia-demo`, or (optional, off by default) into a **shadow cross-checker** that runs after the production result, compares, logs disagreements without content, and never affects output. Any other use needs the promotion process (ARCHITECTURE_OVERVIEW §2).

## 5. Provenance model
**5.1 Assertion** (immutable; corrections add a row with `supersedes_id`): `id, user_id, subject, kind ∈ {DOCUMENT_FACT, EXTRACTED_VALUE, REGULATORY_FACT, INTERPRETATION, ASSUMPTION, UNKNOWN}, statement (encrypted), structured_value (encrypted JSONB), confidence, status ∈ {VERIFIED, UNVERIFIED, USER_CONFIRMED, USER_REJECTED, STALE, SUPERSEDED}, produced_by (extraction_run_id | rule_version_id | model_call_id | user_id), created_at`.
**5.2 EvidenceAnchor:** `assertion_id, document_version_id, extraction_run_id, canon_version, canonical_hash, start, end (code points), page_from, page_to, rects[] per page, quote (encrypted), match_class ∈ {EXACT_UNIQUE, EXACT_MULTI_DECLARED, AMBIGUOUS, FUZZY_UNVERIFIED, MISALIGNED, LOW_CONFIDENCE}, verified_by ∈ {PRODUCTION_VERIFIER, USER}, verifier_impl+version, verified_at, bbox_approx, derived_from?`.
**5.3 Verification rules.** (1) Normalise the quote with the same deterministic L1 pipeline (2.3). (2) The production verifier returns *all* exact L1 occurrences; apply clause/context scope (declared clause id or preceding <= 40 chars). (3) Both ends must be on L1 cluster boundaries (else `MISALIGNED`). (4) One aligned match in scope is `EXACT_UNIQUE` and eligible for DOCUMENT_FACT. (5) Multiple matches are `AMBIGUOUS` unless extraction explicitly declares `multi` with all occurrences (`EXACT_MULTI_DECLARED`). (6) Alternate hyphen-join variant success is flagged `hyphen_ambiguous`. (7) Amounts, dates and percentages require exact digit match on high-confidence tokens. (8) **L2 is candidate retrieval only: an L2/fuzzy hit yields `FUZZY_UNVERIFIED`, `LOW_CONFIDENCE` or `MISALIGNED` and only EXTRACTED_VALUE/UNVERIFIED; it can never independently establish a DOCUMENT_FACT.**
**5.4 RegulatoryCitation:** `assertion_id, provision_id, rule_version_id, retrieved_at, source_url, host_class, verification_state (V0–V4), text_hash, effective_from/to, reviewer ids`. **ModelCall:** purpose, provider, model id, prompt@version, schema@version, input/output hashes, redaction profile, injection flags (content not stored by default).
**5.5 Rules:** DOCUMENT_FACT needs a verified unique/declared anchor; REGULATORY_FACT shows host class + verification state; INTERPRETATION lists supporting assertions and cannot be sole support; ASSUMPTION only from user/declared default; UNKNOWN is first-class; every answer stores its assertion set.

## 6. Q&A / reasoning
Retrieval from the **ephemeral, agreement-scoped, in-memory index** built lazily from decrypted L1 text and structure nodes (decided: D-25; SECURITY §5; never persisted), structure-aware; context = verified spans; model call through gateway with data envelope and no tools; output schema with `supports` ids; Java re-verifies ids and quotes; unsupported blocks removed; if none remains → **abstain** with unknowns. Abstention triggers: no support; unresolved conflict; missing required fact; rule applicability unknown or **rule below V3**; state/jurisdiction missing for state-dependent topics; low OCR confidence on cited span; request for a legal determination. Scenario facts become ASSUMPTION assertions.

## 7. Knowledge layer — verification ladder (requirement 5)
Entities: `instrument → instrument_version → provision → rule` with `source` record `{url, host_class ∈ {OFFICIAL_HOST, MIRROR_HOST, SECONDARY}, retrieved_at, retrieval_method, text_hash, read_by}`.
**Verification states:**
| State | Meaning | Allowed uses |
|---|---|---|
| **V0 UNVERIFIED** | claim noted; no source text read | reviewers only; never shown to end users |
| **V1 SECONDARY_ONLY** | law-firm/press/aggregator only | informational lead with banner "not verified"; no computation |
| **V2 MIRROR_READ** | text read from non-official host (e.g. bank-hosted RB-IOS 2026; caalley.com circular) | show provision text with **"mirror — not verified against official host"** badge; **no deterministic evaluation, no deadline computation presented as authoritative**; may appear as *guidance with warning* |
| **V3 OFFICIAL_READ_REVIEWED** | official-host text read **and** second-person review of text, effective dates, commencement, applicability predicate; mirror copies upgraded only after hash/diff equivalence to the official text is recorded | **may drive DETERMINISTIC rules** (e.g. date computations) — results still labelled with source and effective date |
| **V4 COUNSEL_CONFIRMED** | qualified counsel confirmed interpretation | allows INTERPRETIVE rules to be displayed as "reviewed reading"; still never a verdict |
**Rule type × state:** DETERMINISTIC rules require V3+ (else not evaluated; informational display only); REFERENCE rules allowed at V2+ with badge, V1 as lead; INTERPRETIVE rules never auto-evaluated — displayed at V4 as reviewed reading, otherwise "seek qualified advice". **Additional preconditions for any rule:** commencement confirmed (e.g. SBSR Act per-section notification), effective dates recorded, applicability (entity class, product, dates) reviewed.
**Mirror→official upgrade:** obtain official copy (manual browser download if the host blocks automation — **no CAPTCHA/human-check bypass**), compare normalised text hash/diff; identical → record equivalence and allow V3 review; different → conflict ticket, rule frozen. **End-to-end provenance:** assertion → rule_version → provision → instrument_version → source (url, host class, retrieved_at, hash, reader, reviewer); UI shows the host badge at every hop.
**Current seed status (from Gate1B/02, /08; no upgrades yet):** Insurance Ombudsman Rules 2017 (amended 9 Nov 2023) official read, awaiting second review → V2-equivalent until reviewed (cannot yet drive deterministic output); IRDAI AR 2024-25 data = reference data (official read) ; RBI bank Directions (KFS, cooling-off, prepayment) official read, bank-only scope, awaiting review; RB-IOS 2026 = **V2** (bank-hosted mirror); IRDAI Health Master Circular = **V2** (mirror); IRDAI Policyholder Regs 2024 = **V0/V1** (not read); PED cap/claim interest = **V1**; SBSR Act = official read, commencement unresolved, interpretive → informational only. **Result: no rule is currently approved for deterministic evaluation; that is the correct current state.**
Approval workflow DRAFT→IN_REVIEW→APPROVED; APPROVED requires state ≥ requirement for its type; changes create new versions; `DEPRECATED` retained.

## 8. Domain-module SPI
As v0.1: `DomainModule {id; supportedDocumentTypes; classify; extractionSchemas; validators; ruleSelectors; workflows; explanationTemplates}`; data packs for taxonomy/schemas/prompts/rule seeds; modules `generic-agreement` (required), `lending`, `insurance`, `tenancy`, `subscriptions`, `warranties`; each declares jurisdictions, state-dependence, deadline types, channel catalogue, safety gates.

## 9. Commitments
Candidates (from extraction or `RULE_DERIVED`) → user confirms/edits/dismisses → `commitment`. **Only CONFIRMED create reminders.** `RULE_DERIVED` candidates exist only from rules at V3+; otherwise the system shows an informational note and asks the user to enter dates themselves. Scheduling via `ReminderIndex` (production = DB-backed index; assessed heap is not used). Reminders tick under per-user tenant context (§SECURITY 6). Changed agreement versions mark linked commitments `NEEDS_REVIEW`.

## 10. Cases / grievances
As v0.1 (case, event, evidence, communication, draft, submission_prep, outcome; drafts per-sentence kind chips; timeline ordering by `occurred_at` then recorded order with stable sort). All case text/evidence encrypted per SECURITY §3. `external_submission` path absent/flag-disabled.

## 11. Data model (logical)
v0.1 entity list plus: `user_key`, `object_key`, `key_event`, `canon_revision`, `edit_map_run` (stored as encrypted blob), `worker_job`, `worker_lease`, `deletion_ledger`, `source` (knowledge provenance), `rule_review`. Columns classified **plaintext-queryable** (ids, timestamps, statuses, types, due dates, key versions, byte sizes) vs **encrypted payload** (document text, statements, quotes, notes, draft text, event descriptions, structured values, commitment descriptions/amounts). Conventions: UUIDv7; `user_id` + RLS on user tables; `timestamptz`; money as paise; JSONB with `schema_id@version`; append-only assertions/audit.

## 12. API overview (`/api/v1`, OpenAPI-first)
Documents/agreements/understand/provenance/commitments/cases/knowledge/account as v0.1. Changes: `/algo/*` endpoints exist **only in `cia-demo`**, not in the platform API. **Worker broker protocol (internal, mTLS):** `POST /broker/v1/lease` (worker→broker: capacity; response: job spec + single-use token) · `GET /broker/v1/jobs/{id}/input` (streamed bytes; bound to token) · `PUT /broker/v1/jobs/{id}/result` (streamed result JSON + page images if requested) · `POST /broker/v1/jobs/{id}/heartbeat|fail`. The broker validates schema, size, coordinate bounds, text sanity, then hands to `canonical`. No storage URLs ever reach the worker.

## 13. Operations
Config via env + secrets manager; feature flags; metrics (extraction latency, OCR confidence, verification-failure rate, abstention rate, MISALIGNED/AMBIGUOUS rates, queue depth, key-operation errors, RLS-violation attempts); alerts on spikes; backup/restore with **deletion-ledger replay** (SECURITY §4); RPO/RTO TBD.

## 14. Evaluation harness
Golden corpus per domain (synthetic first), gold anchors/values; the §2.7 catalogue is part of CI; metrics for extraction/citation/abstention/deadline correctness; thresholds set by the owner at gate P2.

## 15. Known limits [OPEN]
Indic OCR quality; table extraction; legal reviewer capacity; provider data stance; cost per document; latency targets (proposed p95 < 90 s ingest→READY for ≤20 pages; ASSUMPTION).

## 16. Review additions (v0.2, revised v0.2.1)
v0.2 review fixes: raw plaintext hashes as queryable metadata (confirmation-attack risk) replaced by keyed HMAC blind indexes. **v0.2.1 correction:** the v0.2 rule that the broker "merges tokens that begin with a combining mark" is **withdrawn** — it mutated L0. L0 is now the exact worker stream; grapheme segmentation runs over derived S0 with mappings to all original token ids and boxes (2.1). **Known usability risk (unresolved):** LLM-produced Hindi quotations often omit or alter ZWJ/ZWNJ; exact verification then fails and the result is `FUZZY_UNVERIFIED` (deliberately conservative). The P1/P2 golden corpus must measure the rate. **Unverified assumptions:** JDK segmentation lacks GB9c (use pinned ICU4J); per-cluster NFC equals whole-stream NFC (P2).
