# Document 6 — Research Gaps and Validation Plan (Gate 1B)

Priority: P1 blocks Gate 2 · P2 blocks Gate 3 · P3 before Gate 4.

## A. Desk research (can be done without consumers)
| # | Question | Why it matters | How | Resolving evidence | Decision | P |
|---|---|---|---|---|---|---|
| D1 | Read IRDAI Policyholder Regs 2024 + Health Master Circular 2024 (free-look, claim timelines/interest, cashless, moratorium, PED) | Rule library for C1 | Download from irdai.gov.in manually (listings scanned this pass; docs not located) | Section-level extracts with effective dates | C1 viability, deadlines | P1 |
| D2 | Read RB-IOS 2026 (CAPTCHA blocked) and transitional rules | Lending escalation logic | Browser download of RBIOOS01072026OB_E.pdf | Window, caps, pre-condition | C2 | P1 |
| D3 | Read Dec-2025 Insurance Act amendment s.45 effect | Non-disclosure disputes | IRDAI consolidated Act (5 Feb 2026) | Text | C1 | P1 |
| D4 | CIO nature-wise table and FY25 report; IRDAI AR 24-25 | Verify "95% denials", FY25 growth, upheld rates | Download PDFs (>10MB) and extract with pdftotext (worked for FY24 CIO) | Figures | Size/pain claims | P1 |
| D5 | NBFC KFS/prepayment directions | C2 scope | RBI Master Directions | Text | C2 | P2 |
| D6 | e-Jagriti/CMS/NCH terms of use, representative filing | Integration boundary | Browser session | Terms | Feature scope | P2 |
| D7 | DPDP Rules notification and commencement | Compliance | Gazette | Dates | Gate 4 | P2 |
| D8 | Competitor teardown via app stores/sites; pricing; privacy policies | Replace WS01 snippet-based findings | Direct inspection | Screens/test logs | Differentiation | P1 |
| D9 | International design references (Resolver, Which?, Lemonade, Spellbook) | Pattern learning only | Docs | — | — | P3 |
| D10 | Rental state matrix (3–5 states), MTA adoption | Park/unpark C5 | State acts | Matrix | Rental scope | P3 |
| D11 | Reading/comprehension behaviour studies (not self-report) | Understand-pillar demand | Literature | Studies | C3 | P2 |

## B. Hands-on testing
| # | Test | Resolves |
|---|---|---|
| H1 | Same 20 policy+denial scenarios to ≥2 general AI assistants and 3 Indian explainers; blind-score | "General AI parity" — the central threat |
| H2 | File a dummy/real complaint walk-through on Bima Bharosa, CIO, e-Jagriti, CMS | Real friction, fields, evidence requirements |
| H3 | Loan pack reconciliation test on KFS-bearing documents | Engine/rule-library feasibility for C2 |

## C. Consumer research (cannot be done by desk)
Interview programme per WS02 protocol (24–30; segments: recent claimants incl. smooth/rejected/partial, recent buyers, renters, borrowers/co-borrowers, grievance users, delegators; vernacular/non-metro). Add:
- Behavioural proof tests: a landing page/concierge offer measuring unprompted willingness to upload documents and pay at claim time.
- Concierge pilot of 10–20 real denied-claim cases, tracking: action taken, filing, outcome, time.
- WTP via revealed behaviour (what they already paid agents/firms), not hypotheticals.
Resolves: A1 users act with software at claim time; A2 upload trust; A3 payment; A4 effect vs general AI; delegator-as-buyer.

## D. Professional advice
Counsel on WS03 §G (Q-P1 legal practice, Q-P2 DPDP, Q-P3 liability, Q-P4 portal interaction, Q-I1–I6, Q-L1–L5), plus IRDAI advice/distribution boundary and Ombudsman representative filing (R5). Blocks Gate 3 scope.

## E. Coverage matrix
| Area | Status |
|---|---|
| Consumer problem (A) | Desk secondary only; interviews **not done** |
| Competitors (B) | Snippet-level from WS01; **no hands-on**; no general-AI test |
| Regulatory (C) | Partial primary: Ombudsman Rules, CIO report, RBI lending directions (banks); rest unread |
| Technical (D) | Conceptual only; no benchmark |
| Business (E) | No WTP data; no unit economics |
| Cross-domain | Reasoned from above; rental/warranty thin |
