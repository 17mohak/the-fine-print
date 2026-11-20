# OPUS_BUILD_HANDOVER.md — THE FINE PRINT

**Status (D-21 recorded 2026-11-20): Opus handover READY FOR P0 ONLY (v0.2.1); for P1+ and Track C it remains a draft that is NOT authorised.** Architecture: **APPROVED v0.2.1 direction**, subject to unresolved decisions, limitations and review gates. Implementation: **AUTHORISED FOR TRACK P, P0 ONLY.** Consumer validation: **WAIVED AS A DEVELOPMENT GATE, NOT PASSED.** Prepared by Claude Sonnet (planning session), 2026-11-20. (v0.1 in `_archive/architecture_v0.1/`.)
Labels: VERIFIED FACT · RESEARCH FINDING · TECHNICAL RECOMMENDATION · ASSUMPTION · OPEN QUESTION · DEFERRED DECISION.

## 0. Read first — hard rules for the Opus session
1. **Authorisation (D-21, recorded 2026-11-20): you may work on Track P, P0 ONLY.** **P0 scope (exact):** infrastructure only — repository skeleton, Gradle modules, CI (build, tests, ArchUnit, dependency/secret scan, SBOM), ADR template, `core-api` SPI interfaces/DTOs (no logic), OpenAPI skeleton, Docker Compose dev stack with stubs, commit-attribution rules; **empty placeholders only for `cia/`**. **P0 exclusions (exact):** P0 must **not** implement encryption/key management, RLS/tenant context, the worker or broker, canonicalisation/provenance logic, search indexes, production `QuoteVerifier` adapters, or any assessed algorithm. **P1–P7: NOT AUTHORISED. Track C C0–C3: NOT AUTHORISED for implementation.** **G0:** at the end of P0 the owner must confirm exact versions (#2) and the D-20 path before anything further is considered; **G0-SEC:** a security-specialist review of the encryption/key, RLS/tenant-context and worker-isolation designs is required **before P1 implements those controls**; P1 also needs its own recorded authorisation. No real personal documents or sensitive data anywhere. If asked to do anything outside P0, stop and ask the owner. Do not infer authorisation from this document.
2. **CIA authorship:** until the owner records faculty's written AI-assistance policy (D-20), **do not write, fix, complete, paste or "improve" the bodies of assessed algorithms** (KMP, suffix array/LCP, Bloom filter and its hash function, heap, DSU, matching) **or their tests, baselines, benchmarks, complexity/correctness text, report or viva material.** If asked, stop and raise the question. Infrastructure outside `cia-algorithms`/`cia-bench` is allowed after approval (CIA matrix §0).
3. **No silent architecture decisions.** Use the decision-rights table (§24). Anything marked OWNER/FACULTY/LEGAL/SEC is *not yours to decide*: ask, record the answer, continue.
4. Do not claim anything is complete without evidence. Do not describe demand as established. Do not use real personal documents. Do not bypass CAPTCHA/auth/anti-bot.
5. Missing information stays visible: **CO definitions, unit→CO mapping, actual dates, AI policy, team size, faculty scope approval** (CIA matrix §0) — never invent them.
Companion files give depth: PROJECT_FOUNDATION, ARCHITECTURE_OVERVIEW, SYSTEM_DESIGN, TECHNOLOGY_DECISIONS, CIA_REQUIREMENTS_MATRIX, SECURITY_AND_PRIVACY, PRODUCT_EXPERIENCE_SPEC, IMPLEMENTATION_ROADMAP, RESEARCH_BACKLOG, DECISION_LOG, `Gate1B/*`.

## 1. Product vision and approved principles
**THE FINE PRINT — your personal consumer intelligence platform.** *Understand what you agree to. Know what you're entitled to. Know what to do next.* India-first. **UNDERSTAND** (clause-level explanation, obligations/exclusions/exceptions, fees/penalties, renewal/cancellation, ambiguity/missing information, scenarios, cross-clause and cross-document reasoning, comparison, questions grounded in the user's documents) · **PROTECT** (commitment tracking, payments, renewals, notice periods, deadlines, entitlements, document organisation, version history, reminders, obligation→source links) · **ACT** (evidence organisation, timelines, relevant clauses, remedies/escalation routes, complaint preparation, checklists, guided access to official channels, assisted form prep where permitted, action/outcome tracking). Grievance tiers: **Guided / Assisted / Connected**; Connected requires a verified authorised integration — none exists.
Principles: evidence before conclusions; original document = source of truth; six statement kinds; abstain when evidence is inadequate; never fabricate provisions; legal information ≠ legal advice; jurisdiction + effective-date aware; user controls consequential external actions; no undisclosed commissions; no success promises; no implied government affiliation; **uploaded documents are untrusted data**; security/privacy foundational. Not an insurance app, PDF summariser, grievance chatbot or generic AI assistant.

## 2. Scope and domain breadth
Domains under investigation (none selected): insurance (health/motor/life/general), housing/tenancy, lending, subscriptions/consumer services, purchases/warranties. Shared foundation + independent domain modules. Architecture-proof modules at P3: `generic-agreement`, `lending`, `insurance`, + one thin module — chosen to stress different engine properties, **not** a market choice.

## 3. Formal consumer-validation waiver (D-10, 2026-11-20, owner)
**WAIVED AS A DEVELOPMENT GATE — not passed/completed.** Interviews NOT CONDUCTED; concierge NOT CONDUCTED; WTP UNVALIDATED; PMF UNVALIDATED; general-AI benchmark NOT COMPLETED; competitor testing INCOMPLETE; regulatory research PARTIALLY VERIFIED; technical feasibility PRELIMINARY; vision PRESERVED. Rationale (owner's): proceed with the original vision rather than delay development. Protocols retained in `Gate1B/`.

## 4. Research findings and source references (host class preserved; do not upgrade)
OFFICIAL / MIRROR / SECONDARY.
- **OFFICIAL** Insurance Ombudsman Rules 2017 (amended to 9 Nov 2023; cioins.co.in): ₹50 lakh ceiling (r.17(3)); insurer-first (r.14(3)); 1-year limit; award in 3 months; compliance in 30 days; mediation recommendation 1 month + acceptance 15 days; complainant = person/legal heirs/nominee/assignee (agents not named); grounds include repudiation, delay, legal construction (r.13). *Read once; no second reviewer yet → not V3.*
- **OFFICIAL** IRDAI Annual Report 2024-25: 2,57,790 Bima Bharosa grievances; **1,37,361 = General & Health combined**; ~69% of grievances under *general insurance business* were claims-related; 88,841 claim-related G&H grievances vs 11.26 crore claims; FY25 health claims (my column reading) 87.50% paid / 7.92% repudiated by number. **Distinct populations (claims, insurer grievances, IRDAI grievances, Ombudsman complaints, awards, recommendations) must never be combined; repudiation share ≠ demand.**
- **OFFICIAL** CIO FY24: 52,575 complaints; 24% of disposals non-entertainable — **does not establish need for preparation assistance.** The ≈56% favourable figure is **withdrawn; never reuse.** CIO FY25 PDF image-only: not extracted.
- **OFFICIAL** RBI bank Directions (Nov 2025, upd. 1 Oct 2026): KFS for retail/MSME loans sanctioned on/after 1 Oct 2024 (banks); cooling-off ≥1 day; no prepayment charge on in-scope floating-rate loans from 1 Jan 2026 (commercial banks excl. SFB/payments/LAB). **NBFC rules unread — never generalise.**
- **OFFICIAL** SBSR Act 2025 (assent 20 Dec 2025; commencement by notification; many provisions w.e.f. 5 Feb 2026); Act text doesn't amend s.45; consolidated s.45 life-only, operative text three years. **Per-section commencement and s.45/moratorium interplay need legal interpretation.**
- **MIRROR** RB-IOS 2026 (bank-hosted copy; RBI's PDF behind human verification, not bypassed; equivalence **not confirmed**): in force 1 Jul 2026; bank first, 30-day wait; 90 days to file; ₹30L + ₹3L; non-advocate authorised representative; forum bar. → **V2**.
- **MIRROR** IRDAI Health Master Circular 29.05.2024 (caalley.com): 30-day free look (≥1yr), 60-month moratorium, cashless 1h/3h, repudiation must cite specific terms, ₹5,000/day for award non-compliance. → **V2**.
- **SECONDARY/OPEN** Policyholder Protection Regs 2024 (not read); PED 36-month cap; claim-delay interest.
- Competitors: pages inspected (PolicyPlain, Insurance Samadhan ₹999 + 20% + GST vendor-stated, Ditto); no product used; Coverfox 403; EzPolicy/PolicyDr unchecked.
- Corrections: ceiling ₹50L; cooling-off ≥1 day; health moratorium rests on IRDAI circular not s.45; third-party submission is sector-specific.
**Seed status:** no rule is approved for deterministic evaluation yet (SYSTEM_DESIGN §7). That is correct.

## 5. Known uncertainties — see RESEARCH_BACKLOG A/B/C and DECISION_LOG pending approvals.

## 6. Approved architecture
**Architecture direction APPROVED (v0.2.1, D-21), subject to unresolved decisions, limitations and review gates; only P0 is authorised.** v0.2.1 summary: Java modular monolith (Spring Boot) + sandboxed Python doc-worker behind a Java **worker broker** (mTLS, no storage URLs, no internet); PostgreSQL with RLS and encrypted payload columns; S3-compatible object store (ciphertext only); Postgres-backed jobs; provider-agnostic Java model gateway; React+TS SPA; provenance-first assertions; versioned knowledge with V0–V4 verification ladder; default-off external submission; **assessed CIA algorithms isolated from production** behind `core-api` SPIs.

## 7. Component responsibilities
ARCHITECTURE_OVERVIEW §5. Boundaries enforced by ArchUnit/Gradle: `platform/*` must not depend on `cia-*`; `cia-algorithms` has no Spring/DB/network; domain modules don't import each other; `cases` cannot call external systems; only `modelgateway` calls providers; only `integrations` makes outbound channel calls; only `workerbroker` talks to the worker; untrusted file parsing never in the Java core.

## 8. Repository structure (proposal)
```
fineprint/
├─ settings.gradle.kts, build.gradle.kts, gradle/libs.versions.toml
├─ core-api/                # SPIs (QuoteVerifier, ApproximateMembership, ReminderIndex, DocumentLinker) + DTOs; no logic
├─ platform/
│  ├─ app/                  # Spring Boot main, config, security
│  ├─ identity-access/ crypto/ documents/ workerbroker/ canonical/ provenance/ knowledge/
│  ├─ understand/ modelgateway/ commitments/ cases/ integrations/ audit/
│  └─ domains/ {generic-agreement, lending, insurance, tenancy, subscriptions, warranties}/
├─ doc-worker/              # Python sandboxed OCR/layout + contract tests
├─ web/                     # React + TS
├─ cia/                     # ISOLATED academic track (student-authored bodies)
│  ├─ cia-algorithms/ (text/, prob/, sched/, graph/)   # depends only on core-api
│  ├─ cia-bench/ (JMH, generators, results/)
│  └─ cia-demo/ (runnable app + UI; no DB server/network/LLM/worker)
├─ db/migrations/ (Flyway) · data/ (synthetic corpora, golden anchors, rule seeds with source+host class)
├─ docs/ {adr/, cia/, runbooks/} · ops/ (compose, CI, scripts) · README.md, CONTRIBUTING.md
```

## 9. Technology choices
TECHNOLOGY_DECISIONS §A lists readiness per decision. Opus may set exact versions and library picks only where marked **ADR**; everything marked OWNER/FACULTY/LEGAL/SEC is unresolved (KMS, hosting/region, IdP, LLM providers, OCR vendor for cloud, search architecture, retention).

## 10. Database and data model
SYSTEM_DESIGN §11 (entities) + §5 (provenance) + SECURITY §3/§6 (keys, RLS) are the spec. Conventions: `user_id` + **FORCE RLS** on user tables, non-owner role, `set_config('app.user_id', …, true)` per transaction; encrypted payload columns vs plaintext metadata as classified; append-only assertions/audit; UUIDv7; paise money; JSONB `schema_id@version`. Opus authors Flyway DDL, RLS policies, schema lint and a data dictionary at **P1 only, after G0-SEC**, and presents them at gate G1.

## 11. API contracts
SYSTEM_DESIGN §12: `/api/v1` REST (documents, agreements, understand, provenance, commitments, cases, knowledge, account); RFC 7807 errors with `traceId`; cursor pagination; `Idempotency-Key`; no content in errors; object bytes streamed through the API. **Worker broker protocol** (mTLS, single-use job token; lease/input/result/heartbeat/fail). `/algo/*` exists only in `cia-demo`.

## 12. Document-processing pipeline
SYSTEM_DESIGN §1–§3. Java core does magic-byte/size/hash only; the **worker** parses everything; Java builds canonical text (§2) and verifies.

## 13. AI and retrieval architecture
Gateway signatures indicative: `ModelGateway.structured(Purpose, PromptRef, SchemaRef, DataEnvelope, RedactionProfile) → ValidatedOutput`; no tools/network for extraction/QA; providers pluggable (**undecided**). Retrieval = **ephemeral, agreement-scoped, lazily built, byte-bounded in-memory index** over decrypted L1 text (D-25; SECURITY §5): TTL/LRU eviction, tenant-partitioned, heap-only, **never persisted**, rebuild on miss/invalidation, direct-scan fallback; broad cross-vault search is deferred until a benchmark and validated need justify it (revisit triggers in SECURITY §5). Embeddings only if benchmarked. Hybrid pipeline; never a single LLM call.

## 14. Provenance requirements (non-negotiable)
**L0 is the exact worker token stream and is never mutated**; grapheme segmentation (pinned segmenter, Unicode >= 15.1, full UAX #29 incl. Indic conjuncts) runs over derived S0 with mappings to all original token ids/boxes; code-point offsets over L1 with `canon_version` + `segmenter_id` + keyed `canonical_hash`; grapheme-aligned anchors; **production verification = exact matching on L1 with the same deterministic normalisation, plus L1 boundary enforcement; L2 is candidate retrieval only and can never establish DOCUMENT_FACT**; DOCUMENT_FACT only with `EXACT_UNIQUE` (or declared `EXACT_MULTI`) anchors verified by the **production** `QuoteVerifier`; fuzzy/ambiguous/misaligned/low-confidence matches never become DOCUMENT_FACT; anchor invalidation and re-anchoring per SYSTEM_DESIGN §2.6; regulatory statements carry host class + verification state; interpretations never sole support; UNKNOWN first-class; corrections append; model/prompt versions recorded. **Test catalogue SYSTEM_DESIGN §2.7 must exist and pass before G1.**

## 15. Security and privacy requirements
SECURITY_AND_PRIVACY v0.2 is binding: key hierarchy and envelope encryption covering originals, derived text, assertions, case evidence; rotation; crypto-shredding with restore-time deletion-ledger replay; explicit TenantContext parameter; FORCE RLS; tenant-scoped jobs and a column-limited system scheduler role; hardened worker; injection defences; scrubbed logs. **Production security is not solved; synthetic data only until P7.**

## 16. CIA traceability matrix
CIA_REQUIREMENTS_MATRIX is binding for Track C. Source = faculty guide only; gaps listed in §0 above.

## 17. Algorithm specifications and benchmarks (Track C — student-authored)
Specs in CIA matrix §4 (A1 KMP + suffix array/LCP; A2 Bloom; A3 heap; A4 DSU; A5 optional). **Opus does not implement these bodies** (§0 rule 2). Opus may provide (post-approval, disclosed): SPI interfaces, `cia-demo` scaffolding without algorithm logic, build config, CI. Production uses boring adapters; promotion criteria in ARCHITECTURE_OVERVIEW §2; default = no promotion.

## 18. Testing strategy
Unit + property (jqwik) vs reference for **platform** code; canonicalisation/anchor catalogue (§2.7, incl. G1–G10, P1–P2, L0 immutability and pinned-segmenter checks); ephemeral-index tests (two-user partition canaries, byte bound, TTL/LRU eviction, rebuild, no plaintext index on disk, budget-exhaustion fallback); contract tests (OpenAPI, worker broker); two-user isolation + RLS schema lint + pooled-connection test; key-rotation and crypto-shred tests; restore-replay test; golden-corpus metrics (extraction, citation precision/recall, abstention precision, deadline correctness) with owner-set thresholds; injection red-team; rule-effective-date and verification-ladder tests (no sub-V3 deterministic output); ArchUnit; Playwright e2e incl. failure/abstention; axe accessibility; network test for worker; load tests before P7. CIA tests/benchmarks are student-authored (§0).

## 19. Deployment and environment assumptions
Docker Compose for dev and `cia-demo`; one-command setup; env-injected config; Linux containers; user-facing Asia/Kolkata, storage UTC; production hosting/region/KMS/IdP **undecided** (OWNER/LEGAL/SEC); k8s deferred.

## 20. Design and experience specifications
PRODUCT_EXPERIENCE_SPEC: IA (Home/Agreements/Commitments/Cases + Ask/Search/Account), five-kind UI language, margin-note provenance, designed abstention/conflict/failure states, **host-class badges for regulatory statements** (official vs mirror/secondary; V-state wording), WCAG 2.2 AA, safety copy rules. "Annotated paper" direction is a recommendation; not a generic chat UI or admin dashboard.

## 21. Phased roadmap
IMPLEMENTATION_ROADMAP v0.2: **Track P** P0–P7 (gates G0–G7) and **Track C** C0–C3 (gate GC). v0.1 phase numbers are stale. D-21 authorises **P0 only**; start nothing else.

## 22. Milestone acceptance criteria
Per phase in the roadmap with evidence artefacts; no milestone is done without evidence.

## 23. Known risks and prohibited assumptions
**Prohibited:** assuming demand/PMF/WTP; assuming an official API or automation permission; bypassing CAPTCHA/auth/anti-bot/human-verification; treating mirror/secondary sources as official or letting V0–V2 rules drive deterministic output; reusing the ≈56% figure; equating 1,37,361 with health only; combining claims/grievance/Ombudsman/award populations; generalising bank KFS/prepayment rules to NBFCs; treating s.45 as the health-moratorium source; using real personal documents; sending data to unapproved providers; plaintext document text in logs or (pending owner decision) in Postgres; parsing untrusted files in the Java core; giving the worker internet/storage access/credentials; importing `cia-*` into the platform; writing assessed algorithm bodies without faculty-confirmed policy; silently creating reminders from extracted dates; presenting interpretation as determination; adding infrastructure without a recorded justification; changing approved architecture without owner review; claiming CIA compliance without the missing source materials.
**Risks:** unvalidated demand; general-AI parity; legal boundaries (practice of law, insurance advice); mirror-sourced rules; Indic OCR quality; cost per document; CIA/product scope tension; academic-integrity ambiguity; insider-with-key-access and plaintext-in-memory residual risks; small-team capacity; search-architecture trade-off.

## 24. Decision-rights table and questions
| Category | Opus may decide (record an ADR) | Opus must **propose and wait** | **Blocked — ask owner/faculty/legal/security** |
|---|---|---|---|
| Libraries/versions | exact versions within pinned stack; test libs; queue library (#17) | swapping framework/DB | — |
| Internal design | class/package design inside approved module boundaries; DDL details; API field naming | new module, changed boundary, new service, new datastore | — |
| Crypto | use a vetted library per SECURITY §3 | any change to key hierarchy/algorithms | KMS/HSM product, custody, residency (OWNER+LEGAL+SEC) |
| Search | implement the ephemeral agreement-scoped index per SECURITY §5; tune bounds/TTL by ADR | any persisted index, vault-wide search, or external search engine | choosing any plaintext-at-rest or persisted-index alternative (OWNER + SEC) |
| Worker | implement ARCH §4 as specified | any added egress, credentials or provider call | sandbox level beyond spec (SEC) |
| Providers/OCR | gateway/port implementations | adding a provider | which providers/regions/cloud OCR (OWNER+LEGAL) |
| Knowledge | build workflow and evaluator | adding rules at V3+ | approving a rule, upgrading a source state, legal interpretation (reviewer/counsel) |
| CIA | `core-api` SPIs, scaffolding (post-approval, disclosed) | — | **anything inside assessed algorithms/tests/benchmarks/report** until faculty policy confirmed (FACULTY+OWNER) |
| Scope | — | adding/removing a domain module | narrowing the vision (OWNER) |
**Questions to raise (not decide):** (1) authorship policy for assessed code (D-20); (2) confirmed Java/Spring/Postgres versions; (3) IdP, KMS, hosting region, LLM providers; (4) OCR path and cloud-OCR permission; (5) golden-corpus sourcing and any consented real documents; (6) owner thresholds for extraction/citation/abstention/deadline metrics (G2); (7) Phase-3 module set and the thin module; (8) v1 UI/OCR languages; (9) reminder channels; (10) search bounds/TTL values and any proposal to broaden search (architecture itself decided: D-25); (11) any contradiction with the CIA guide, principles or security spec; (12) any fact in this handover found wrong — record with host class and propose a DECISION_LOG entry.

## 25. How to work
Read this file and DECISION_LOG; confirm D-21 and authorised phases; propose a plan for those phases; ADR for every non-trivial decision; small attributable commits; keep docs updated; produce evidence artefacts; stop at gates; never expand scope silently.
