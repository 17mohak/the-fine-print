# DECISION_LOG.md

Consolidates `Gate1B/12` (L1–L9, retained there as history) and adds architecture-transition decisions. Dates are actual. Format per significant decision: decision · rationale · evidence · alternatives · trade-offs · confidence · reversibility · dependencies.

## Carried forward (from Gate1B/12)
L1 Ombudsman ceiling ₹50L; cooling-off ≥1 day; RB-IOS 2026 figures (official-host for first two; RB-IOS = **mirror**). L2 Health moratorium cited to IRDAI circular (mirror), not s.45; s.45 life-only (legal interpretation required). L3 "1,37,361" = G&H combined; ≈56% figure withdrawn. L4 Submission-on-behalf is sector-specific. L5 Samadhan fee = ₹999 + 20% + GST (vendor page). L6 C1/C2 remain candidates (now **non-gating**). L7 amended stop rule — retained as a **deferred research programme**, no longer a development gate (see D-10). L8 official-host vs mirror distinction in register. L9 demand / WTP / technical differentiation / regulatory feasibility kept separate.

## New decisions
### D-10 · 2026-11-20 · Consumer validation waived as a development gate — **owner decision**
- **Decision:** Consumer validation is **WAIVED AS A DEVELOPMENT GATE**. It is not passed or completed.
- **Rationale (as stated by owner; none added):** the owner wishes to proceed with the original product vision rather than delay development until consumer validation is complete.
- **Evidence:** none for demand; this is a sequencing choice.
- **Alternatives:** validate first (Gate1B/10 plan); concierge-first; narrow MVP. Not chosen by owner.
- **Consequences:** development may be *prepared* now; architecture proposed; implementation reserved for a separate Opus session. Interviews NOT CONDUCTED; concierge NOT CONDUCTED; WTP UNVALIDATED; PMF UNVALIDATED; general-AI benchmark NOT COMPLETED; competitor testing INCOMPLETE.
- **Risks:** building unwanted features; trust/upload barriers unexamined; general-AI parity unknown; episodic usage; regulatory exposure; sunk engineering cost.
- **Confidence:** n/a (owner's call). **Reversibility:** high — protocols retained and activatable. **Dependencies:** none; validation assets stay in `Gate1B/`.
- **Mitigation in design:** modular architecture so domain modules can be added/dropped; validation hooks (RESEARCH_BACKLOG §D); no real users/sensitive documents until production-critical items close.

### D-11 · Architecture and build preparation authorised (owner) — status: AUTHORISED; architecture **PROPOSED, not approved**.
### D-12 · Implementation reserved for a separate Opus session; not begun; **build not authorised** until the owner approves the architecture.
### D-13 · Original vision, pillars and domain breadth preserved. No narrowing without explicit approval.
### D-14 · TECHNICAL RECOMMENDATION — modular monolith in Java as the platform core; Python worker only for OCR/layout/ML inference behind a versioned contract. Rationale: CIA requires Java business logic; complexity of microservices not justified. Alternatives: microservices; Python-core. Confidence medium-high. Reversible (module boundaries kept). Depends on owner approval.
### D-15 · TECHNICAL RECOMMENDATION — provenance-first design: no user-visible "document says" statement without a Java-side verified source anchor; six-kind statement taxonomy. Confidence high. Hard to reverse once data exists → decide now.
### D-16 · TECHNICAL RECOMMENDATION — regulatory knowledge is versioned, effective-dated, reviewed data with a verification status including host class; never static prompt text. Confidence high.
### D-17 · TECHNICAL RECOMMENDATION — external submission is a separately controlled capability, default OFF, per-channel, requiring recorded authorisation evidence and legal sign-off. Confidence high.
### D-18 · TECHNICAL RECOMMENDATION — commitments: extracted dates are CANDIDATES; only user-confirmed commitments generate reminders. Confidence high.
### D-19 · CIA algorithm placement (proposal, see CIA_REQUIREMENTS_MATRIX): KMP/trie/suffix-array for quote verification and term search (Unit 4); Bloom filter prescreen with exact confirmation (CO5/Unit 6); heap + DSU for commitment scheduling and document-family clustering (Unit 2). Status: PROPOSED; needs faculty scope approval. Confidence medium.
### D-20 · OPEN QUESTION (owner/faculty) — **academic-integrity boundary**: the CIA guide requires "your own Java implementations" of assessed algorithms and an individual viva. Whether AI-generated code for those modules is permitted is **not stated in the guide**. Recommendation: Opus implements platform infrastructure; assessed algorithm bodies are written by the students (Opus may supply interfaces, tests scaffolding, review) unless faculty confirms AI-assisted authorship is allowed. **Owner must decide before the Opus session starts.**

### D-21 · RESERVED — Architecture approval and build authorisation · **NOT RECORDED**
The owner records approval here (with phase scope, e.g. "build authorised for Phase 0–1"). Until this entry exists, the Opus handover must not be executed.

## Pending approvals (owner)
Architecture (D-14–D-19) · D-20 policy · team size/faculty scope approval for CIA slice · LLM provider and data-residency stance · hosting region · whether any real documents may be used in development.

## Risks (summary; full in OPUS_BUILD_HANDOVER §23)
R-A Demand unvalidated. R-B General-AI parity unknown. R-C Legal-practice/insurance-advice boundary unknown. R-D Mirror-sourced regulatory data. R-E CIA/product scope tension (a broad platform vs algorithm-centric CIA project; "generic chatbot" excluded by the guide). R-F Sensitive data before security review. R-G Academic-integrity ambiguity (D-20).
