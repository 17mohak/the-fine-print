# PROJECT_FOUNDATION.md — THE FINE PRINT

**Version v3 · 2026-11-20 · Authoritative.** Supersedes `Gate1B/07_PROJECT_FOUNDATION.md` (v2; snapshot in `_archive/`, stub left in place). Historical research is retained unchanged in `Gate1B/`. Labels used: **[VERIFIED FACT] [RESEARCH FINDING] [TECHNICAL RECOMMENDATION] [ASSUMPTION] [OPEN QUESTION] [DEFERRED DECISION]**.

## 1. Vision (unchanged)
**THE FINE PRINT — your personal consumer intelligence platform.** *Understand what you agree to. Know what you're entitled to. Know what to do next.* India-first. A comprehensive platform, not an insurance app, PDF summariser, grievance chatbot or generic AI assistant.

## 2. Pillars
- **UNDERSTAND** — clause-level explanation, obligations/exclusions/conditions/exceptions, fees and penalties, renewal/cancellation terms, ambiguity and missing-information detection, scenarios, cross-clause and cross-document reasoning, comparison, questions grounded in the user's own documents.
- **PROTECT** — commitment tracking, payment obligations, renewals, notice periods, deadlines, entitlements and eligibility conditions, document organisation, version history, reminders, obligation→source-provision links.
- **ACT** — evidence organisation, timelines, relevant clauses, remedies and escalation routes, complaint preparation, checklists, guided access to official channels, assisted form preparation where permitted, action/outcome tracking.
Grievance capability tiers: **Guided** (explain, direct) · **Assisted** (organise evidence, prepare content) · **Connected** (interact with an official system only via a verified, authorised integration). No official API is assumed; authentication, CAPTCHA, anti-bot and portal safeguards are never bypassed.

## 3. Domain breadth (preserved; domains under investigation, not launch commitments)
Insurance (health, motor, life, general) · Housing/tenancy · Lending (education, personal, vehicle) · Subscriptions/consumer services · Purchases/warranties. Architecture = shared foundation + independently developed domain modules; workflows are not forced identical. No domain is selected or discarded by data availability.

## 4. Principles
Evidence before conclusions; original document = source of truth; every important statement carries provenance and one of six kinds (document fact, extraction, external legal/regulatory, interpretation, assumption, unknown); abstain when evidence is inadequate; never fabricate provisions; legal information ≠ legal advice; jurisdiction- and effective-date-aware; users control consequential external actions; no undisclosed commissions/conflicts; no promises of claim success; no implied government/regulator affiliation; uploaded documents are untrusted data, never instructions; privacy and security are foundational.

## 5. Current status (accurate record)
| Item | Status |
|---|---|
| Consumer validation | **WAIVED AS A DEVELOPMENT GATE** (owner decision D-10, 2026-11-20). Not passed, not completed. |
| Consumer interviews | **NOT CONDUCTED** |
| Concierge testing | **NOT CONDUCTED** |
| Willingness to pay | **UNVALIDATED** |
| Product-market fit | **UNVALIDATED** |
| General-AI competitive benchmark | **NOT COMPLETED** (protocol prepared, Doc 09) |
| Hands-on competitor testing | **INCOMPLETE** (three sites' pages inspected; no product used) |
| Regulatory research | **PARTIALLY VERIFIED** (see §7) |
| Technical feasibility | **PRELIMINARY, NOT FULLY BENCHMARKED** |
| Original vision | **PRESERVED** |
| Architecture & build preparation | **AUTHORISED** (owner) |
| Architecture | **PROPOSED — not reviewed, not approved** |
| Build handover | **PREPARED (draft) — build NOT authorised** |
| Implementation | **RESERVED for a separate Claude Opus session; not begun** |
Gate position: Gate 1B incomplete on opportunity evidence; Gates 2–3 not formally convened (superseded by owner waiver for sequencing only); **Gate 4 (architecture): proposed, awaiting owner review/approval; Gate 5 (build): not authorised.**

## 6. Validation protocols — retained as deferred research assets
`Gate1B/09`, `10`, `06`, `11` and the amended stop rule (L7) remain **intact and activatable**; they no longer gate development. Opportunity-validation items live in RESEARCH_BACKLOG.md §D.

## 7. Research findings that bind the design (with provenance preserved)
- **[VERIFIED FACT, official host]** Insurance Ombudsman Rules 2017 (amended to 9 Nov 2023, cioins.co.in): ceiling ₹50 lakh; insurer-first; 1-year limit; award within 3 months; insurer complies in 30 days; complainants named as person/legal heirs/nominee/assignee — agents not named. Jurisdiction includes repudiation, delay, legal construction of policies for claims.
- **[VERIFIED FACT, official host]** IRDAI Annual Report 2024-25: 2,57,790 Bima Bharosa grievances; **1,37,361 are General and Health combined (not health alone)**; 88,841 claims-related G&H grievances vs 11.26 crore claims. Different populations — claims, insurer grievances, Ombudsman complaints, awards, recommendations — **must not be conflated**.
- **[VERIFIED FACT, official host]** RBI bank Directions (Nov 2025, updated 1 Oct 2026): KFS for retail/MSME loans sanctioned on/after 1 Oct 2024 (**banks**); cooling-off ≥1 day; no prepayment charge on in-scope floating-rate loans sanctioned/renewed from 1 Jan 2026 (**commercial banks excl. SFB/payments/LAB**). **NBFC rules not read; do not generalise.**
- **[VERIFIED FACT, mirror host]** RB-IOS 2026 read from a **bank-hosted copy**; equivalence with RBI's official PDF **not confirmed** (RBI's copy sits behind human verification; not bypassed). Content: in force 1 Jul 2026; bank first, 30-day wait; 90-day filing; ₹30L + ₹3L; non-advocate authorised representative; forum bar.
- **[VERIFIED FACT, mirror host]** IRDAI Health Master Circular 29.05.2024 read from a **mirror (caalley.com)**: 30-day free look (≥1yr), 60-month moratorium, cashless 1h/3h, repudiation must cite specific terms, ₹5,000/day non-compliance with Ombudsman award.
- **[OPEN QUESTION]** IRDAI Policyholder Protection Regulations 2024 and their master circular: **not read**. PED cap and claim-delay interest remain secondary.
- **[VERIFIED FACT, official host] / [OPEN QUESTION]** SBSR Act 2025: assent 20 Dec 2025; commencement by notification (many provisions w.e.f. 5 Feb 2026); the Act text does not amend s.45; consolidated s.45 is life-only. Commencement of every provision and s.45/moratorium interplay **need legal interpretation**.
- **[RESEARCH FINDING]** The ~24% non-entertainable Ombudsman disposals (FY24) **does not establish that complaint-preparation assistance is needed**. The ~56% favourable-outcome figure is **withdrawn** and must not be reused. Health-claim repudiation share (≈7.9% by number FY25, derived) is **not a proxy for demand**.
- **[VERIFIED FACT]** CIO FY25 report is image-only; contents not extracted. Competitor pages were inspected; **functionality untested**. General-AI benchmark **not executed**.
- **[RESEARCH FINDING]** Third-party submission is sector-dependent: Insurance Ombudsman Rules do not clearly authorise agents; RBI scheme permits non-advocate authorised representatives with written authority; portal automation terms unknown.

## 8. Scope boundaries and non-goals
Not: legal advice; outcome predictions; commission-funded steering; automated portal submission without a verified authorised integration; reliance on an LLM answer without evidence; narrowing to one domain without approval. Internal foundations may be built now; **real users, real sensitive documents and consequential workflows require the production-critical items in RESEARCH_BACKLOG.md §C (legal, security, privacy) first.**

## 9. Key decisions → DECISION_LOG.md. Outstanding risks → DECISION_LOG.md §Risks and OPUS_BUILD_HANDOVER.md §23.

## 10. Document map
Root: PROJECT_FOUNDATION, ARCHITECTURE_OVERVIEW, SYSTEM_DESIGN, TECHNOLOGY_DECISIONS, CIA_REQUIREMENTS_MATRIX, SECURITY_AND_PRIVACY, PRODUCT_EXPERIENCE_SPEC, IMPLEMENTATION_ROADMAP, OPUS_BUILD_HANDOVER, DECISION_LOG, RESEARCH_BACKLOG. History: `Gate1B/` (WS01–WS03 and Docs 01–12; unmodified except dated change notes).
