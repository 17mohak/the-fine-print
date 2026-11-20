# Document 5 — Technical Feasibility Assessment (Gate 1B)

**Nothing here was benchmarked. All performance is UNMEASURED.** No code was written. This is conceptual.

## 1. Shared vs specialised (hypothesis test)

| Capability | Shared across insurance / rental / loan? | Domain-specific part | External data needed | Main failure modes |
|---|---|---|---|---|
| Ingestion, OCR, layout | Shared | Regional-language/handwritten rental papers hardest | — | Misread amounts/dates; skipped pages |
| Document classification | Shared | Product type/version recognition (insurer product UIN, KFS format) | Product catalogue | Wrong type → wrong rules |
| Clause segmentation & cross-reference | Mostly shared | Insurance: schedule/endorsement/definition cross-refs; loan: schedule of charges | — | Missed rider overriding base clause |
| Entity/date/amount extraction | Shared | Domain units (sum insured, sub-limit, EMI, rent) | — | Wrong date base (start vs renewal) |
| Obligation/right/exception structure | Shared schema, domain vocabulary | Waiting-period semantics, PED definitions, lock-in/penalty | Standard definitions (IRDAI) | Exceptions to exceptions mishandled |
| Regulatory baseline comparison | **Not shared** | Per-regulator rule libraries with effective dates | IRDAI/RBI/state texts (versioned) | Stale or wrong rule version; conflicting secondaries (seen in WS03) |
| Scenario reasoning ("what if I exit early") | Partly | Depends on state law (rental), on rule version | Rules, user facts | False confidence |
| Deadline computation | Shared mechanism | Rules differ (R2 for Ombudsman; 2-yr commission; cooling-off) | Verified rules | Wrong trigger date |
| Grievance routing | **Not shared** | Per sector/state/forum limits (₹50L ceiling, parallel-forum bar) | Forum tables | Misrouting |
| Evidence/chronology/case layer | Shared | Case types | — | Provenance loss |

Assessment: **extraction and provenance are genuinely shared; interpretation, regulatory baseline and routing are not.** The shared-engine hypothesis is credible for the lower half of the stack and unsupported for the upper half. Value (and defensibility) sit in the specialised upper layers, which weakens "one universal engine" as a positioning.

## 2. Key design questions → provisional answers
- *Grounding:* every claim carries page+span citation; no claim without source; evidence chips Document / Regulation / Interpretation / Assumption / Unknown (from charter + WS03).
- *Missing/contradictory info:* abstain explicitly; "silent document" ≠ "not covered".
- *Effective dates:* regulatory knowledge as versioned records (instrument, section, effective date, verification status, reviewer). WS03 found multiple 2025–26 changes — the dominant maintenance cost.
- *Reliability threshold:* requires measured, not assumed. Proposed: deterministic checks for dates/amounts with human-visible source; LLM-generated explanation only on top of verified extractions; human review for high-value claims.
- *Cost:* unknown; per-document LLM cost likely small relative to case value but maintenance of rule libraries and human review dominate (INF).

## 3. Architecture directions (conceptual, trade-offs)
| Option | Pros | Cons |
|---|---|---|
| Plain RAG over uploaded doc | Fast, cheap | = general AI; poor on cross-reference and dates; weak abstention |
| Structured agreement representation + RAG + citations | Auditable; enables deadlines | Extraction errors propagate |
| + deterministic rules layer (versioned) | Reliable on checkable facts | Rule maintenance burden; legal review needed |
| + human review for high-stakes | Safety, trust | Cost, scale, legal-practice questions |
Direction: structured representation + citations + versioned deterministic rules for checkable facts, LLM only for explanation, abstention by default, human review tier for consequential outputs. Stack not chosen.

## 4. Benchmark proposal (to run before Gate 4)
- Corpus: ~20 health/motor policies with denial letters (real, consented, redacted or public Commission/Ombudsman case files), ~15 loan packs (KFS + sanction + schedule), ~15 rental agreements (varied states/languages), plus scans of varying quality.
- Gold labels by a domain reviewer for: clause boundaries, entities/dates/amounts, obligations/exceptions, cross-references, QA with citations, and "unanswerable" items.
- Metrics: field-level extraction accuracy; citation precision/recall; QA correctness; **abstention precision**; deadline computation correctness; contradiction detection; cross-doc reasoning.
- Baselines: ≥2 general AI assistants given identical docs/questions (same prompt protocol), plus a naive-RAG baseline.
- Safety suite: prompt injection inside documents, OCR corruption, truncated pages, conflicting endorsements, outdated-rule traps, wrong-forum routing.
- Pre-registered success thresholds to be set at Gate 4 with decision owner.

## 5. Grievance-submission feasibility
Prepare: feasible. Guide to portal: feasible. Official integration: **no public API evidence found** (R13–R16: Bima Bharosa none stated; NCH convergence is company-side; e-Jagriti and RBI CMS unreadable). Submit on user's behalf: **not supported** — Ombudsman rules name only complainant/legal heirs/nominee/assignee (R5); portals use OTP to the complainant (R15); terms on automation unknown. Default: user submits; product never states "submitted" unless confirmed.

## 6. Critical risks
Wrong deadline; stale regulation; false confidence on denial analysis; OCR errors on amounts; prompt injection via uploads; sensitive health/financial data (DPDP, Rules commencement unverified); misrouting.
