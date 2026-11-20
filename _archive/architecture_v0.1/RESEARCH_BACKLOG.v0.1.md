# RESEARCH_BACKLOG.md

Status 2026-11-20. Research continues in parallel; it blocks only where stated. Host-class discipline: OFFICIAL / MIRROR / SECONDARY recorded for every source (Gate1B/02 convention).

## A. Architecture-critical (answer before architecture approval)
| ID | Question | Why | How | Status |
|---|---|---|---|---|
| A-1 | Owner/faculty: CO definitions, unit→CO mapping, week dates, **AI-assistance policy**, team size, scope approval | CIA compliance and D-20 | Request from faculty; owner answers | OPEN — blocks Phase 1 authorship model |
| A-2 | LLM provider stance: acceptable providers/regions, contractual terms, redaction acceptability | Model gateway design, T7 | Owner + legal | OPEN |
| A-3 | Hosting region/provider and data-residency requirement | Storage/KMS/IdP selection | Owner + legal | OPEN |
| A-4 | Legal-practice boundary (Q-P1) and insurance-advice boundary at *design* level: what outputs are permissible (draft complaint wording, "appears inconsistent") | UX copy, draft generator limits | Counsel | OPEN — design assumes information-only, user-submits |
| A-5 | OCR/layout feasibility on Indian documents (English + Hindi first): accuracy of Tesseract vs one cloud option | Worker design, privacy | Benchmark on synthetic + consented docs | NOT STARTED |
| A-6 | Retrieval: does FTS+structure suffice vs embeddings for clause retrieval? | Search/vector decision | Benchmark on golden corpus | NOT STARTED |
| A-7 | Indic FTS configuration (tokenisation, stemming) | Search quality | Spike | NOT STARTED |

## B. Implementation-critical (before the component is built)
| ID | Question | Component | Notes |
|---|---|---|---|
| B-1 | Read IRDAI Policyholder Protection Regs 2024 + master circular (claims timelines, interest, grievance TAT, free-look scope) from **official host**; PED cap in Insurance Products Regs 2024 | knowledge/insurance seeds | Not read; secondary only |
| B-2 | Re-read Health Master Circular 29.05.2024 from **IRDAI official host** (currently MIRROR) | insurance seeds | Upgrade host class |
| B-3 | Obtain RB-IOS 2026 from RBI official host (currently bank-hosted MIRROR; RBI PDF behind human-verification — do not bypass; use manual browser download) and read cl.11–19 | lending seeds | Equivalence unconfirmed |
| B-4 | NBFC KFS and prepayment directions; SFB scope; do not generalise bank rules | lending module | Not read |
| B-5 | SBSR Act 2025: notification status per section; s.45/moratorium interplay | knowledge + counsel | Legal interpretation required |
| B-6 | CIO FY25: OCR/manual extraction; FY24+FY25 nature-wise tables | reference data | PDF image-only |
| B-7 | Tenancy: state matrix for chosen launch states; MTA adoption; stamp/registration | tenancy module | Not started |
| B-8 | Subscriptions/warranties: CCPA dark-pattern guidelines, e-mandate rules, CPA jurisdiction (read primary) | modules | Secondary only so far |
| B-9 | Official channel facts: e-Jagriti, RBI CMS, NCH terms (representatives, automation) | channel catalogue | JS-only/blocked pages — manual browser |
| B-10 | Golden-corpus sourcing: public commission/Ombudsman case documents, insurer sample policies (licences), synthetic generation | eval harness | Lawful sourcing + privacy review |

## C. Production-critical (before real users, sensitive documents or consequential workflows)
C-1 DPDP Rules notification/commencement and obligations (MeitY blocked to fetch; manual/gazette). C-2 Notice/consent design [LEGAL]. C-3 Breach notification process [LEGAL]. C-4 Independent security review/pen-test. C-5 Sandbox and key-management review. C-6 Liability/disclaimer/terms. C-7 Legal-practice + advice licensing opinions (Q-P1, Q-P3). C-8 Retention/deletion verification incl. backups. C-9 Provider DPAs/zero-retention terms. C-10 Safety-suite thresholds agreed (abstention precision, injection resistance, deadline correctness). C-11 Third-party filing/automation permissibility (Q-P4) before any CONNECTED integration. C-12 Accessibility audit.

## D. Opportunity-validation (deferred assets — activate when owner chooses; not a development gate)
D-1 24 consumer interviews (Gate1B/10). D-2 Concierge cases (5 per candidate, expand to 10 on signal; pause rule). D-3 H1 general-AI benchmark (Gate1B/09; two assistants + explainers; blind scoring; reuse golden corpus). D-4 Hands-on competitor testing (Coverfox 403; EzPolicy/PolicyDr URLs; actual use of PolicyPlain/Samadhan/Ditto). D-5 Willingness-to-pay via revealed behaviour. D-6 Domain comparison/challenger discovery (rental, subscriptions, warranties). D-7 Product-market fit signals. **All remain UNVALIDATED/NOT CONDUCTED.** Findings here can reprioritise Phase 4/7 module order and feature emphasis.

## Triage rule
A, B items block only their stated architecture decision or component. C blocks real-world use, not engineering. D never blocks engineering but should inform what is emphasised.
