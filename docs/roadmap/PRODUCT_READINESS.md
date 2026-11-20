# Product readiness — shared-engine assessment and validation backlog (PLANNING ONLY)

- **Prepared:** 2026-11-20 on branch `prep/p1-readiness`. Desk review of existing project documents only. **No
  outreach, interviews, form submissions or external accounts were used.**
- **Product (unchanged, D-13):** *THE FINE PRINT — your personal consumer intelligence platform.* Three pillars:
  **Understand · Protect · Act.** Domain breadth is preserved: insurance, housing/tenancy, lending,
  subscriptions/consumer services, purchases/warranties. **No domain is selected; nothing here narrows the
  product.**
- **Validation status (unchanged, D-10):** consumer validation **WAIVED AS A DEVELOPMENT GATE, NOT PASSED.**
  Demand, willingness to pay and product-market fit are **UNVALIDATED**; the general-AI benchmark is **NOT
  COMPLETED**.
- **Sources:** PROJECT_FOUNDATION; Gate1B/05 section 1 (shared versus specialised); Gate1B/06 (gaps); Gate1B/11
  (C1 vs C2: "no evidence makes either candidate the winner"); RESEARCH_BACKLOG; SYSTEM_DESIGN sections 3, 7–10;
  PRODUCT_EXPERIENCE_SPEC.

## 1. Shared-engine assessment

| Capability | Genuinely shared? | Domain-specific part | Status of evidence | Untested assumptions |
|---|---|---|---|---|
| Document ingestion | **Yes** | Capture quality varies (phone photos of rental papers) | Design only | Users will upload sensitive documents (trust, A2 in Gate1B/06) |
| OCR and layout | **Yes (engine)** | Regional-language and handwritten documents are hardest; tables (schedules of charges, sub-limits) | Unbenchmarked (A-5) | Tesseract-class OCR is adequate for Indian English and Hindi documents |
| Clause segmentation and cross-reference | **Mostly** | Insurance: schedule, endorsement, definition references; lending: schedule of charges | Unbenchmarked | Numbering and heading rules generalise across issuers |
| Structured extraction (entities, dates, amounts) | **Yes (mechanism)**, domain vocabulary | Sum insured, sub-limits, EMI, rent, notice periods, lock-ins | Unbenchmarked | Schema-constrained extraction plus Java verification reaches owner-set thresholds (set at G2) |
| Cross-document relationships | **Yes** | What counts as a linking identifier differs (policy number vs loan account vs registration) | Design only (DocumentLinker) | Identifier-based linking does not over-merge on shared identifiers |
| Source-grounded Q&A | **Yes (mechanism)** | What may be answered without crossing into advice differs by sector | Design only | Users accept abstention ("we can't tell"); Hindi quotation matching (ZWJ/ZWNJ) failure rate is acceptable |
| Comparison and scenario analysis | **Partly** | Scenarios depend on rules (state tenancy law, rule versions) and user facts | Design only | Scenario outputs stay information, not advice |
| Ambiguity and missing-information detection | **Yes (mechanism)** | What "missing" means is domain knowledge (e.g. a KFS field) | Design only | "Silent document" can be distinguished from "not covered" reliably |
| Versioned regulatory knowledge | **Mechanism shared; content not** | Per-regulator rule libraries with effective dates (IRDAI, RBI, state acts, CCPA) | **No rule is V3** (SYSTEM_DESIGN section 7) | Rule maintenance cost is sustainable (Gate1B/05 calls it the dominant maintenance cost) |
| Commitments and evidence management | **Yes** | Deadline rules differ by forum and sector | Design only | Confirmed-only reminders are enough to avoid false reliance |
| Grievance routing | **No** | Per sector, state and forum (ceilings, parallel-forum bars) | Partly verified facts; mirror sources | Routing tables can be kept current |

**Reading (consistent with Gate1B/05):** the lower stack is credibly shared: ingestion, OCR, extraction,
provenance, linking, the evidence and case layer, and the commitment mechanism. Interpretation, the regulatory
baseline and routing are domain-specific, and that is where value and risk concentrate. This supports the
architecture of a shared foundation plus independent domain modules. It does **not** support marketing "one
universal engine" as the differentiator.

## 2. Domain-specific dependencies (what each domain module would need before it is useful)

| Domain | Rule and knowledge dependency | Current verification state | Main external blocker |
|---|---|---|---|
| Insurance (health/motor/life/general) | IRDAI regulations and circulars; Ombudsman Rules | Ombudsman Rules: official read, no second review; Health Master Circular: **mirror (V2)**; Policyholder Regs 2024: **not read** | Official-host copies; second reviewer; insurance-advice boundary (counsel) |
| Lending | RBI KFS, cooling-off and prepayment directions; RB-IOS 2026 | Bank directions read (bank-only scope); RB-IOS: **mirror (V2)**; NBFC rules **not read** | Never generalise bank rules to NBFCs |
| Housing/tenancy | State tenancy laws; Model Tenancy Act adoption | Not started (B-7) | State-by-state matrix |
| Subscriptions/consumer services | CCPA dark-pattern guidelines; e-mandate rules; Consumer Protection Act | Secondary only (B-8) | Primary reads |
| Purchases/warranties | Consumer Protection Act; e-Jagriti channel facts | Secondary only | Channel terms (B-9) |

## 3. Features with disproportionate privacy or legal risk

| Feature | Risk | Why | Existing control or open item |
|---|---|---|---|
| Complaint and grievance drafting (Act) | **Legal-practice boundary**; misleading drafts | Q-P1 unresolved; drafts could be taken as legal advice | Information-only, user-submits; counsel needed (A-4, C-7) |
| Any portal interaction (Connected tier) | Unauthorised filing; automation terms; CAPTCHA | Q-P4; sector-specific representation rules | Default-off; no automation code (D-17) |
| Regulatory deadline computation | Liability for a wrong deadline | Rules are not V3; commencement unclear (SBSR Act) | Deterministic only at V3+; informational otherwise (D-27) |
| Comparison and "which is better" | Insurance-advice or distribution boundary; steering | IRDAI advice rules unread | No steering or commissions; counsel |
| LLM processing of documents | Data leaving the system to a provider | Provider, region and terms undecided | Gateway, redaction; Owner + LEGAL (A-2) |
| Health-insurance documents | Health data under DPDP; heightened sensitivity | DPDP duties unverified (C-1) | Synthetic only until P7 |
| Reminders by email/SMS/WhatsApp | Consent; third-party processors | Channels undecided | Deferred (TECHNOLOGY_DECISIONS #25) |
| Vault-wide search | Plaintext residency; cross-agreement exposure | D-25 deferred it | Agreement-scoped only |

## 4. Product claims that need evidence before public use

| Claim (or implication) | Evidence required | Status |
|---|---|---|
| "Understand what you agree to": explanations are accurate | Golden-corpus metrics against owner thresholds (G2) | Not measured |
| "Better or safer than asking a general AI assistant" | The H1 benchmark (Gate1B/09; RESEARCH_BACKLOG D-3) | **Not completed** |
| "Know what you're entitled to" | Legal-information framing reviewed by counsel; V3+ rules | No V3 rules; counsel pending |
| "Your documents are private / secure / encrypted" | G0-SEC, the P7 security review and pen-test, DPDP review | **Must not be claimed** |
| Any market-size or pain statistic (e.g. IRDAI grievance counts) | The exact population and source, host class preserved | 1,37,361 = General **and** Health combined; the ~56% figure is **withdrawn**; repudiation share ≠ demand |
| "Never miss a deadline" | Deadline correctness metric; confirmed-only reminders; liability review | Not measured; C-6 pending |
| Any government or regulator affiliation | — | **Prohibited** (PROJECT_FOUNDATION section 4) |

## 5. Product-validation backlog (deferred research assets; not development gates)

IDs reference existing items where they exist (RESEARCH_BACKLOG A–D; Gate1B/06 D1–D11, H1–H3). Nothing here has
been executed.

### 5.1 Desk research (no consumers)
| # | Question | Ref |
|---|---|---|
| DR1 | Competitor teardown by direct inspection (apps, sites, pricing, privacy policies) | Gate1B/06 D8; RESEARCH_BACKLOG D-4 |
| DR2 | Comprehension and reading-behaviour studies, not self-report (Understand-pillar demand) | Gate1B/06 D11 |
| DR3 | Rental state matrix (3–5 states), MTA adoption | B-7; Gate1B/06 D10 |
| DR4 | Subscriptions and warranties: CCPA dark patterns, e-mandate, CPA jurisdiction | B-8 |
| DR5 | International pattern references (design learning only) | Gate1B/06 D9 |

### 5.2 Primary-source verification (official host; host class recorded)
| # | Item | Ref |
|---|---|---|
| PS1 | IRDAI Policyholder Protection Regs 2024 + master circular | B-1 |
| PS2 | Health Master Circular from the IRDAI official host (currently a mirror) | B-2, B-12 |
| PS3 | RB-IOS 2026 from RBI (manual browser download; no human-verification bypass) | B-3, B-12 |
| PS4 | NBFC KFS and prepayment directions | B-4 |
| PS5 | SBSR Act per-section commencement (plus counsel on s.45 and moratorium) | B-5 |
| PS6 | CIO FY25 report (image-only; manual or OCR extraction) | B-6 |
| PS7 | Second-reviewer reads to reach V3 (Ombudsman Rules; RBI bank directions) | B-13 |
| PS8 | Official channel terms (e-Jagriti, RBI CMS, NCH): representation and automation | B-9 |
| PS9 | DPDP Rules notification and commencement | C-1 |

### 5.3 Consumer interview questions (protocol: Gate1B/10; RESEARCH_BACKLOG D-1)
Neutral and behavioural, not hypothetical:
- When did you last read an agreement before accepting it, and what did you do?
- Tell me about the last time a policy, loan, rental or subscription term surprised you. What did you do next,
  and who helped?
- Which documents do you keep, where, and how did you find one when you needed it?
- Have you ever missed a renewal, notice period or deadline? What did it cost you?
- Have you ever filed a complaint or claim dispute? Walk me through every step and every person involved.
- What have you paid an agent, lawyer or service to help with documents or disputes? (Revealed willingness to pay.)
- What would stop you from uploading a policy or loan document to an app?
- When an assistant says "we can't tell from your documents", what do you do next?

Segments (from WS02/Gate1B/06): recent claimants (smooth, rejected, partial), recent buyers, renters,
borrowers, grievance users, delegators (family members acting for others), vernacular and non-metro users.

### 5.4 Concierge experiments (RESEARCH_BACKLOG D-2; pause rule per Gate1B/10)
| # | Experiment | Measures | Constraint |
|---|---|---|---|
| CE1 | Concierge document explanation for 5 users per candidate domain | Action taken afterwards; time saved; trust | Consent, privacy review and counsel first; **real documents remain prohibited in the platform** (D-21). Any concierge work happens outside the system |
| CE2 | Concierge commitment tracking for 1 month | Reminders acted on; corrections needed | Same |
| CE3 | Concierge complaint preparation (Guided/Assisted only) | Filed, outcome, time; legal-boundary incidents | Needs counsel (Q-P1) first |

### 5.5 Technical feasibility tests (synthetic or lawfully sourced data only)
| # | Test | Ref |
|---|---|---|
| TF1 | H1: the same scenarios to at least two general AI assistants and Indian explainers, blind-scored for grounded correctness and abstention | Gate1B/09; RESEARCH_BACKLOG D-3 |
| TF2 | OCR and layout accuracy on Indian English and Hindi documents (Tesseract vs one cloud option, only with LEGAL approval) | A-5 |
| TF3 | Retrieval: FTS plus structure vs embeddings, on the golden corpus | A-6 |
| TF4 | Hindi quotation exact-match failure rate (ZWJ/ZWNJ) | SYSTEM_DESIGN 16 |
| TF5 | Loan-pack reconciliation on KFS-bearing synthetic documents | Gate1B/06 H3 |
| TF6 | Cost per document (OCR + model calls) | SYSTEM_DESIGN 15 |
| TF7 | Ephemeral-index latency and memory at realistic vault sizes | B-14 |

### 5.6 Willingness-to-pay experiments (RESEARCH_BACKLOG D-5)
| # | Experiment | Signal |
|---|---|---|
| WTP1 | Revealed spend: what users already paid agents, lawyers or claim services (Samadhan is vendor-stated at ₹999 + 20% + GST) | Actual payments, not stated intent |
| WTP2 | A landing page or pre-order for a concierge offer (after counsel and privacy review) | Conversion at claim or dispute time |
| WTP3 | A delegator-as-buyer test (a family member paying on someone's behalf) | Who pays |

## 6. What remains explicitly unvalidated
Demand per domain; willingness to pay; trust to upload; acceptance of abstention; comprehension of the five-kind
UI; parity with general AI; episodic versus recurring usage; the legal-practice and advice boundaries; regulatory
maintenance cost. **Building under the D-10 waiver is a sequencing choice, not evidence.**
