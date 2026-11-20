# OPUS_BUILD_HANDOVER.md — THE FINE PRINT

**Audience:** a separate Claude Opus implementation session. **Prepared by:** Claude Sonnet (planning session), 2026-11-20.
**Authorisation state (read first):** Architecture is **PROPOSED — not reviewed, not approved**. Build is **NOT authorised** until the owner records approval in `DECISION_LOG.md` (a line "D-21 Architecture approved; build authorised for Phase X"). **If that line is absent, do not write code; ask the owner.** This document is intended to be self-contained; companion files give depth: PROJECT_FOUNDATION, ARCHITECTURE_OVERVIEW, SYSTEM_DESIGN, TECHNOLOGY_DECISIONS, CIA_REQUIREMENTS_MATRIX, SECURITY_AND_PRIVACY, PRODUCT_EXPERIENCE_SPEC, IMPLEMENTATION_ROADMAP, RESEARCH_BACKLOG, `Gate1B/*` (research history).
Labels: VERIFIED FACT · RESEARCH FINDING · TECHNICAL RECOMMENDATION · ASSUMPTION · OPEN QUESTION · DEFERRED DECISION.

## 1. Product vision and approved principles
**THE FINE PRINT — your personal consumer intelligence platform.** *Understand what you agree to. Know what you're entitled to. Know what to do next.* India-first. Three pillars: **UNDERSTAND** (clause-level explanation, obligations/exclusions/exceptions, fees/penalties, renewal/cancellation, ambiguity/missing info, scenarios, cross-clause & cross-document reasoning, comparison, document-grounded Q&A) · **PROTECT** (commitment tracking, payments, renewals, notice periods, deadlines, entitlements, document organisation, version history, reminders, obligation→source links) · **ACT** (evidence organisation, timelines, relevant clauses, remedies/escalation routes, complaint preparation, checklists, guided access to official channels, assisted form prep where permitted, action/outcome tracking). Grievance tiers: Guided / Assisted / Connected (Connected needs a verified authorised integration — none exists; never bypass auth/CAPTCHA/anti-bot).
Principles: evidence before conclusions; original document is source of truth; six statement kinds (document fact, extraction, regulatory, interpretation, assumption, unknown); abstain when evidence is inadequate; never fabricate provisions; legal information ≠ legal advice; jurisdiction + effective-date aware; user controls consequential external actions; no undisclosed commissions; no success promises; no implied government affiliation; **uploaded documents are untrusted data, not instructions**; security/privacy foundational.
It is **not** an insurance app, PDF summariser, grievance chatbot or generic AI assistant.

## 2. Scope and domain breadth
Domains (under investigation, none selected): insurance (health/motor/life/general), housing/tenancy, lending (education/personal/vehicle), subscriptions/consumer services, purchases/warranties. Architecture = shared foundation + independent domain modules; workflows not forced identical. Initial *architecture-proof* modules (Phase 4): `generic-agreement`, `lending`, `insurance`, + one thin module — chosen to stress different engine properties, **not** as a market choice. Do not hard-code any single domain into the foundation.

## 3. Formal consumer-validation waiver (DECISION D-10, 2026-11-20, owner)
Consumer validation: **WAIVED AS A DEVELOPMENT GATE** (not passed/completed). Interviews NOT CONDUCTED. Concierge NOT CONDUCTED. Willingness to pay UNVALIDATED. Product-market fit UNVALIDATED. General-AI benchmark NOT COMPLETED. Competitor testing INCOMPLETE. Regulatory research PARTIALLY VERIFIED. Technical feasibility PRELIMINARY. Vision PRESERVED. Rationale (owner's, nothing added): proceed with the original vision rather than delay development for validation. **Do not describe demand as established anywhere in code comments, UI copy, docs or tests.** Validation protocols are retained in `Gate1B/` as deferred assets.

## 4. Research findings and source references (with provenance — do not upgrade)
Host class: OFFICIAL (issuing authority's host) · MIRROR (third-party copy of official text) · SECONDARY.
- **OFFICIAL** — Insurance Ombudsman Rules 2017 as amended to 9 Nov 2023 (cioins.co.in): ₹50 lakh ceiling (r.17(3) proviso), insurer-first (r.14(3)), 1-year limit, award in 3 months (r.17(4)), compliance in 30 days (r.17(6)), mediation recommendation in 1 month + complainant acceptance in 15 days (r.16), complaint by person/legal heirs/nominee/assignee (r.14(1)) — agents not named; grounds incl. repudiation, delay, legal construction (r.13).
- **OFFICIAL** — IRDAI Annual Report 2024-25: 2,57,790 Bima Bharosa grievances; **1,37,361 = General & Health combined**; ~69% of grievances under *general insurance business* were claims-related (IRDAI wording; not a health-only figure); 88,841 claim-related G&H grievances vs 11.26 crore claims; health claims table (my column reading: 87.50% paid, 7.92% repudiated by number). **Populations (claims, insurer grievances, IRDAI grievances, Ombudsman complaints, awards, recommendations) are distinct; never combine or derive a "success rate".** Repudiation share ≠ demand.
- **OFFICIAL** — CIO FY24 report: 52,575 complaints; 12,855 non-entertainable (24%). **This does not establish that complaint-preparation help is needed.** The ≈56% favourable figure was **withdrawn; never reuse.** CIO FY25 PDF is image-only: not extracted.
- **OFFICIAL** — RBI bank Directions (Nov 2025, updated 1 Oct 2026): KFS for retail/MSME term loans sanctioned on/after 1 Oct 2024 (banks); cooling-off ≥ 1 day; no prepayment charge on in-scope floating-rate loans sanctioned/renewed from 1 Jan 2026 (commercial banks, excl. SFB/payments/LAB). **NBFC rules unread — never generalise.**
- **OFFICIAL** — SBSR Act 2025 (assent 20 Dec 2025; commencement by notification; many provisions w.e.f. 5 Feb 2026 per consolidated Act); Act text does not amend s.45; consolidated s.45 is life-only, operative text three years (heading says two). **Commencement per section and s.45/moratorium interplay need legal interpretation.**
- **MIRROR** — RB-IOS 2026 from a **bank-hosted copy** (RBI's own PDF is behind human verification; not bypassed; equivalence **not confirmed**): in force 1 Jul 2026; bank first, 30-day wait; 90 days to file; ₹30L consequential + ₹3L; non-advocate authorised representative allowed; forum-parallelism bar; 2021 complaints stay under 2021.
- **MIRROR** — IRDAI Health Master Circular 29.05.2024 from caalley.com: 30-day free look (≥1yr policies), 60-month moratorium (fraud excepted), cashless 1h / discharge 3h, repudiation must cite specific terms, ₹5,000/day for Ombudsman-award non-compliance.
- **SECONDARY / OPEN** — IRDAI Policyholder Protection Regs 2024 and detailed master circular (**not read**); PED 36-month cap and claim-delay interest (secondary only).
- Competitors: pages of PolicyPlain/"Policy Proxy", Insurance Samadhan (₹999 + 20% + GST, vendor-stated), Ditto inspected; **no product used**; Coverfox 403; EzPolicy/PolicyDr not checked. Full history in `Gate1B/`.
- Corrections to remember: ceiling ₹50L; cooling-off ≥1 day ("3" is KFS validity); health moratorium rests on IRDAI circular, **not s.45**; third-party submission is sector-specific.

## 5. Known uncertainties / unresolved decisions
See RESEARCH_BACKLOG A/B/C; DECISION_LOG pending approvals; §24 below.

## 6. Approved architecture
**Nothing is approved yet.** The *proposed* architecture (ARCHITECTURE_OVERVIEW) is: Java modular monolith (Spring Boot) + sandboxed Python doc-worker; PostgreSQL; S3-compatible object store; Postgres-backed jobs; provider-agnostic Java model gateway; React+TS SPA; provenance-first assertions; versioned knowledge layer; default-off external submission. Implement only what the owner approves; deviations require ADR + owner review.

## 7. Component responsibilities
(Condensed from ARCHITECTURE_OVERVIEW §3.) `identity-access`, `documents`, `canonical`, `provenance`, `algo-engine` (pure Java), `knowledge`, `domains/*` (SPI), `understand`, `modelgateway`, `commitments`, `cases`, `integrations`, `audit`, `web`; `doc-worker` (Python). Boundaries enforced by ArchUnit: `algo-engine` has no Spring/DB deps; domain modules don't import each other; `cases` cannot call external systems; only `modelgateway` may call providers; only `integrations` may make outbound calls to channels.

## 8. Repository structure (proposal)
```
fineprint/
├─ build.gradle.kts, settings.gradle.kts, gradle/libs.versions.toml
├─ algo-engine/            # pure Java: text/ (KMP, SuffixArray, Trie), prob/ (BloomFilter, hashing), sched/ (IndexedMinHeap), graph/ (UnionFind[, Matching])
├─ algo-bench/             # JMH harness, datasets generators, scripts, results/ (committed CSV/JSON)
├─ platform/
│  ├─ app/                 # Spring Boot main, config, security
│  ├─ identity-access/  documents/  canonical/  provenance/  knowledge/
│  ├─ understand/  modelgateway/  commitments/  cases/  integrations/  audit/
│  └─ domains/ {generic-agreement, lending, insurance, tenancy, subscriptions, warranties}/
├─ doc-worker/             # Python (OCR/layout) + contract tests
├─ web/                    # React + TS (Vite)
├─ db/migrations/          # Flyway
├─ data/                   # synthetic corpora, golden anchors, rule seeds (with source+host class)
├─ docs/ {adr/, cia/, runbooks/}
├─ ops/                    # docker-compose, CI, scripts
└─ README.md, CONTRIBUTING.md (attribution rules)
```

## 9. Technology choices (see TECHNOLOGY_DECISIONS for options/trade-offs)
Java 21 LTS, Spring Boot (pinned current GA), Gradle Kotlin DSL; PostgreSQL 16+ (FTS, RLS, JSONB); S3-compatible storage; Postgres-backed jobs; React+TS+Vite; OIDC (Keycloak dev; prod IdP deferred); Flyway; JUnit5/AssertJ/jqwik/Testcontainers/ArchUnit/JMH/Playwright; OpenTelemetry; Docker Compose. **Versions are assumptions — confirm and pin on day one; record in an ADR.** Deferred: IdP, KMS, hosting, LLM providers, OCR vendor, queue library.

## 10. Database and data model
Use SYSTEM_DESIGN §10 entity list and §4 provenance model as the specification. Mandatory conventions: `user_id` on every user-owned table + RLS; UUIDv7 ids; `timestamptz` UTC; money in paise `bigint`; JSONB payloads carry `schema_id@version`; assertions and audit rows append-only; `evidence_anchor` stores `canonical_hash`, offsets, page, bbox, quote; `instrument_version.host_class` ∈ {OFFICIAL_HOST, MIRROR, SECONDARY} and `verification_status` enumerated as in SYSTEM_DESIGN §6; no content in logs. Opus authors Flyway DDL, indices, RLS policies, and a data dictionary, then presents them for review before Phase 2 closes.

## 11. API contracts
OpenAPI-first under `/api/v1` per SYSTEM_DESIGN §12 (documents, agreements, understand, provenance, commitments, cases, knowledge, account, algo diagnostics). RFC 7807 errors with `traceId`; cursor pagination; `Idempotency-Key` on uploads/jobs; no document content in errors. Worker contract: signed-URL job protocol with HMAC callback (SYSTEM_DESIGN §12). Generate typed clients for `web`; contract tests mandatory.

## 12. Document-processing pipeline
SYSTEM_DESIGN §1–§3: validate → store immutable original → job → text-layer or OCR → layout/tables → canonical text + offset map → classify → segment → extract (schema-constrained, quote-required) → Java verification → cross-references → cross-document relations → knowledge join → interpretation. States and failure behaviours as specified. Originals never mutated; derived artefacts versioned by `extraction_run_id`; reprocessing creates a new run.

## 13. AI and retrieval architecture
Model gateway interface (indicative signatures only): `ModelGateway.structured(Purpose, PromptRef, SchemaRef, DataEnvelope, RedactionProfile) -> ValidatedOutput`; providers pluggable; prompts/schemas versioned in repo and recorded per call. No tools/network for extraction/QA calls. Retrieval v1 = structure-aware + Postgres FTS; embeddings only if benchmark (RESEARCH_BACKLOG A-6) justifies, behind an interface. Q&A algorithm per SYSTEM_DESIGN §5 (verified spans only, citation re-verification, abstention triggers). Hybrid pipeline — **never a single LLM call as the architecture**.

## 14. Provenance requirements (non-negotiable)
1. No user-visible "document says" without a Java-verified anchor. 2. Regulatory statements display host class + verification status + effective dates. 3. Interpretations list supporting assertions and are never sole support for anything. 4. UNKNOWN is a stored first-class record. 5. Corrections/overrides create new rows (`supersedes_id`), history retained. 6. Every answer's assertion set is stored; a "why" view reproduces it. 7. Model and prompt versions recorded per call. 8. Anchors rebuilt (not reused) when canonicalisation changes.

## 15. Security and privacy requirements
SECURITY_AND_PRIVACY.md is binding: default-deny authZ + RLS; per-user keys; hardened sandboxed worker; prompt-injection defences (data envelope, no-tools, schema validation, grounding gate, detection, red-team CI); scrubbed logs; retention/deletion; audit chain; **synthetic data only** until production gates close; no real sensitive documents in dev/test. Never assume provider terms satisfy obligations.

## 16. CIA traceability matrix
CIA_REQUIREMENTS_MATRIX.md §5 is binding for `algo-engine` and the CIA slice. **Source: the faculty guide only; CO definitions, dates, AI-policy are missing** (matrix §1) — do not invent them.

## 17. Algorithm specifications and benchmark requirements
Implement per matrix §4: A1 KMP + suffix array/LCP; A2 Bloom filter (own hash, double hashing; `mightContain` only a prescreen; exact confirmation path; no deletion); A3 indexed binary min-heap; A4 DSU with reason edges; A5 optional. Assessed classes must not call JDK search/heap/union-find/bloom equivalents internally (JDK/Guava allowed only in baselines/tests). Benchmarks: ≥3 sizes per algorithm, baselines as listed, JMH, fixed seeds, committed raw results + one-command reproduction; report honest outcomes. Production wrappers add safety (e.g. `indexOf` cross-check) outside the assessed classes. **Academic-authorship question → §24 Q1.**

## 18. Testing strategy
Unit + property (jqwik) tests vs reference; ≥5 deterministic tests per algorithm incl. boundary; statistical tests with seeds/tolerances for Bloom; contract tests (OpenAPI, worker); tenant-isolation suite (two users; IDOR; search; object URLs; logs canaries); golden-corpus evaluation (extraction accuracy, citation precision/recall, abstention precision, deadline correctness) with recorded thresholds; injection red-team suite; rule effective-date boundary tests; ArchUnit; Playwright e2e for key flows incl. failure/abstention states; accessibility checks (axe) in CI; deletion e2e; load tests before Phase 8. CI blocks on failures; flaky tests quarantined with tickets, never skipped silently.

## 19. Deployment and environment assumptions
Docker Compose for dev and CIA demo (Postgres, MinIO, worker, IdP dev, app, web); one-command setup; env vars documented, no secrets committed; Linux containers; local-first (CIA accepts local demo); prod hosting/region/KMS/IdP **undecided**; k8s deferred. Time zone Asia/Kolkata for user-facing dates, UTC storage.

## 20. Design and experience specifications
PRODUCT_EXPERIENCE_SPEC.md is binding on structure: IA (Home/Agreements/Commitments/Cases + Ask/Search/Account), the five-kind UI language, document workspace with margin-note provenance, designed abstention/conflict/failure states, mobile capture, WCAG 2.2 AA, safety copy rules (§8). Visual direction "annotated paper" is a recommendation — Opus may propose refinements (using any design guidance available in its session); do not substitute a generic chat UI or admin-dashboard look.

## 21. Phased roadmap
IMPLEMENTATION_ROADMAP.md Phases 0–8 with gates G0–G8. Start only at the phase the owner authorises (expected: P0 then P1).

## 22. Milestone acceptance criteria
As specified per phase in the roadmap; each requires evidence artefacts (test reports, benchmark CSVs, metrics) and a short deviations list. A milestone is not "done" without evidence.

## 23. Known risks and prohibited assumptions
**Prohibited:** assuming demand/PMF/WTP; assuming an official API or permission for portal automation; bypassing CAPTCHA/auth/anti-bot; treating mirror/secondary sources as official; using the ≈56% figure; equating 1,37,361 with health only; combining claims/grievance/Ombudsman/award populations; generalising bank KFS/prepayment rules to NBFCs; treating s.45 as the health moratorium source; using real personal documents; sending data to LLM providers not approved; storing document text in logs; silently creating reminders from extracted dates; presenting interpretation as determination; adding infrastructure (Kafka, K8s, vector DB, graph DB, microservices) without a recorded justification; changing architecture without owner review; claiming CIA compliance without the missing source materials; marking anything complete without evidence.
**Risks:** unvalidated demand; general-AI parity; legal boundary (practice of law, insurance advice); mirror-sourced rules; Indic OCR quality; cost per document; CIA vs product scope tension; academic-integrity ambiguity; solo/small-team capacity; sensitive-data handling.

## 24. Questions Opus must raise (do not decide silently)
1. **Who authors the assessed algorithm bodies?** CIA requires "your own Java implementations" + individual viva; guide silent on AI assistance. Ask the owner; default recommendation: students write A1–A4 bodies, Opus builds interfaces, tests scaffolding, benchmark harness, platform. (DECISION_LOG D-20.)
2. Confirmed Java/Spring/Postgres versions and any deviations.
3. IdP, KMS, hosting region, LLM providers (A-2, A-3) — need owner/legal answers before Phase 3.
4. Which OCR path for Phase 3 and whether any cloud OCR is permitted.
5. Golden-corpus sourcing and whether any consented real documents may ever be used.
6. Thresholds for extraction/citation/abstention/deadline metrics (owner to set at G3).
7. Whether `generic-agreement` + `lending` + `insurance` + one thin module is the right Phase-4 set, and which thin module.
8. Language scope for v1 UI and OCR (English only vs Hindi).
9. Whether email/WhatsApp reminders are in scope for Phase 5 (consent and provider review).
10. Any requirement that seems to contradict the CIA guide, the principles, or the security spec — raise it with a proposed resolution.
11. Anything discovered that makes a documented fact wrong (e.g. a regulatory text differs) — record it, cite the host class, propose a DECISION_LOG entry.

## 25. How to work (operating rules for the Opus session)
Read this file and DECISION_LOG first; confirm authorisation line; propose a plan for the authorised phase; use ADRs for every non-trivial decision; commit in small, attributable commits; keep docs updated; produce evidence artefacts; stop and ask at gates; never expand scope silently.
