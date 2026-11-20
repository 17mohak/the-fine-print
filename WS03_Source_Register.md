# WS03 — Regulatory Source Register (Deliverable B)

**Date compiled:** 2026-11-20
**Status of this register: DRAFT. No entry is verified against the primary official text.**

## Verification-status key (read this first)

| Code | Meaning |
|---|---|
| **S2** | Content corroborated by 2+ independent secondary sources (law-firm notes, press, PRS) found in this session. Official text **not** read. |
| **S1** | One secondary source only, or sources conflict. Treat as a lead. |
| **K** | From model background knowledge. Not checked this session. Highest risk of being stale (my knowledge may lag developments in 2026). |
| **P-TODO** | Official URL given is the *landing/portal* to start from; exact document must be located and read. |

**Rule for the team:** nothing below may drive product logic until promoted to **P-OK** (official text read, section numbers, effective date and amendments recorded, reviewed by counsel). I did not fetch any primary document in this pass; web search returned secondary material. Deep links are deliberately not fabricated — only root portals are listed.

Official portals: IRDAI https://irdai.gov.in/ · RBI https://www.rbi.org.in/ · India Code https://www.indiacode.nic.in/ · DoCA https://consumeraffairs.nic.in/ · Insurance Ombudsman (CIO) https://www.cioins.co.in/ · Consumer commissions https://confonet.nic.in/ (portal has been changing — verify current e-filing system).

---

## 1. Insurance

| # | Instrument | Authority | Relevant provisions (to verify) | Dates | Applicability | Status |
|---|---|---|---|---|---|---|
| I1 | Insurance Act, 1938 (as amended) | Parliament / India Code | s.45 (limits on calling a policy into question; fraud carve-out), s.38/39 (assignment/nomination), s.64VB (premium before risk) | 1938; s.45 rewritten 2015 | All insurance; s.45 is the statutory root of non-disclosure disputes | K |
| I2 | Sabka Bima Sabki Raksha (Amendment of Insurance Laws) Act, 2025 | Parliament | Amends Insurance Act 1938, LIC Act 1956, IRDA Act 1999; 100% FDI; stronger IRDAI powers. Specific policyholder provisions **not confirmed** | Assent 20 Dec 2025 (S2); commencement of each section to be checked | Changes the base statute — any section references from I1 must be re-checked | S2 (existence/date); S1 (content) |
| I3 | IRDAI (Protection of Policyholders' Interests, Operations and Allied Matters of Insurers) Regulations, 2024 | IRDAI | Free-look (30 days reported); claim timelines; grievance handling; policy-document content | Reported notified ~1 Apr 2024 (S1: sources vary on scope — life-only vs life+health) | Life and general/health insurers | S1 |
| I4 | IRDAI (Protection of Policyholders' Interests) Regulations, 2017 | IRDAI | Free-look 15/30 days (s.10(1) per IRDAI page), claim settlement timelines, policy-contents regs | 2017; presumed superseded by I3 — **confirm** | Historical wording still appears in many older policies | S1 |
| I5 | IRDAI Master Circular on Health Insurance Business (Products) | IRDAI | Cashless: decision in 1 h, discharge authorisation in 3 h; moratorium cut 8→5 yrs; claim repudiation needs internal committee approval; replaces ~55 earlier circulars | 29 May 2024 (S2) | Health insurers; many provisions have phased effective dates | S2 (headline items); title varies by source → verify exact title |
| I6 | IRDAI standardisation of health-policy terms / exclusions (2019–2020 circulars; consolidated into I5) | IRDAI | Standard definitions of illnesses; list of permissible/non-payable items; PED waiting-period cap (reported reduced to 3 yrs in 2024 — S1) | 2019–2024 | Health | K / S1 |
| I7 | IRDAI Health Insurance Regulations 2016 (as amended) | IRDAI | Portability, renewability, waiting periods, TPA | 2016 | Health; may be superseded by I5/I3 | K |
| I8 | Insurance Ombudsman Rules, 2017 | Ministry of Finance | Complaint after insurer rejection/non-reply; 1-year limitation (S1); award in ~3 months; no fee; **monetary ceiling disputed (₹30 lakh vs ₹50 lakh in secondary sources — unresolved)**; insurer penalty for non-compliance reported but unverified | In force 27 Apr 2017 (S2) | Individual policyholders of insurers/intermediaries | S1 |
| I9 | IRDAI grievance infrastructure (Bima Bharosa; legacy IGMS) | IRDAI | Insurer GRO → IRDAI. Insurer response window: IRDAI handbook says 2 weeks (S1); other sources say 14/15 days | Handbook c.2021 | All insurers | S1 |
| I10 | Motor Vehicles Act, 1988 (Ch. XI) & MACT | Parliament / state | Third-party liability; claims tribunals | 1988, amended 2019 | Motor insurance | K |
| I11 | DPDP Act, 2023 and DPDP Rules | MeitY | Consent, purpose limitation, children's data, breach notice | Act 2023; rules/phased commencement — verify | **The product itself** (handling health/financial documents) | K / P-TODO |

## 2. Residential tenancy

| # | Instrument | Authority | Relevant provisions | Dates | Applicability | Status |
|---|---|---|---|---|---|---|
| T1 | Transfer of Property Act, 1882, Ch. V (ss.105–117) | Parliament | Lease definition, duration, notice to quit (s.106 — default periods vary by state), repairs, deposit-related rights, forfeiture | 1882 | Residential leases except where state rent law overrides | K |
| T2 | Registration Act, 1908 s.17(1)(d), s.49 | Parliament | Leases >1 year must be registered; unregistered lease has limited evidentiary use | 1908 | Pan-India but state registration practice varies | K |
| T3 | Indian Stamp Act, 1899 + each state's stamp law | State | Stamp duty on leases/leave-and-licence — **rates and rules are state-specific** | Varies | All | K / P-TODO per state |
| T4 | State rent control / tenancy statutes (e.g. Maharashtra Rent Control Act 1999; Delhi Rent Control Act 1958; Karnataka Rent Act 1999; Tamil Nadu Regulation of Rights and Responsibilities of Landlords and Tenants Act 2017; West Bengal Premises Tenancy Act 1997; UP urban premises tenancy law) | State | Rent caps, deposit limits, eviction grounds, forum | Varies | State-specific | K — **must be verified individually** |
| T5 | Model Tenancy Act, 2021 | MoHUA | Deposit cap 2 months (residential); tenancy reported to Rent Authority within 2 months; Rent Authority/Court/Tribunal structure | Cabinet approval 2 Jun 2021 (S2) | **Advisory only** until a state enacts/aligns. 2022 Rajya Sabha reply: UP, TN, Assam, AP aligned (S1; dated; Assam used its own Act) | S2 (content); S1 (adoption status — stale) |
| T6 | Police-verification / tenant-registration orders | State/city police | Local orders | Varies | Local | K |
| T7 | Society/RWA bye-laws, state apartment-ownership acts | State / society | Maintenance, restrictions on tenants | Varies | Local | K |

## 3. Consumer lending

| # | Instrument | Authority | Relevant provisions | Dates | Applicability | Status |
|---|---|---|---|---|---|---|
| L1 | RBI (Digital Lending) Directions, 2025 | RBI | KFS before agreement execution; cooling-off exit (**min. 1 day vs 3 days — sources conflict**); reasonable processing fee on exit disclosed in KFS; LSP/DLA rules | Effective 8 May 2025; some parts 15 Jun & 1 Nov 2025 (S1) | Regulated Entities' digital loans | S1 |
| L2 | RBI directions on Key Fact Statement for retail/MSME loans (2024) | RBI | Standardised KFS, APR disclosure | Reported effective 1 Oct 2024 | Retail loans by banks/NBFCs | K |
| L3 | RBI Fair Practices Code (banks; NBFC Master Directions) | RBI | Loan terms communication, penal charges, recovery conduct | Various; consolidated directions 2025 — check | Banks & NBFCs | K / P-TODO |
| L4 | RBI circulars: reset of floating-rate EMI loans (2023); return of original property documents within 30 days with per-day compensation (2023); CIC update-delay compensation (2023); penal charges not capitalised (2023); prepayment-charge restrictions on floating-rate retail loans (reported effective 2026) | RBI | See each | 2023–2026 | Banks & NBFCs | K — **dates and figures must be confirmed** |
| L5 | RBI Integrated Ombudsman Scheme, 2021 → **replaced by Integrated Ombudsman Scheme, 2026** | RBI | 2021: 30-day RE wait, 1-year limit, ₹20 lakh / ₹1 lakh caps (S2). 2026: reported 90-day filing window, ₹30 lakh / ₹3 lakh caps, RE reply in 15 days (S1). Pre-1 Jul 2026 complaints stay under 2021 scheme | 2026 scheme reported in force 1 Jul 2026 (S2) | Banks, NBFCs, PSPs, CICs | S1 — **high-impact change; read RBI text** |
| L6 | RBI Master Direction on Credit Card/Debit Card issuance | RBI | Most Important T&Cs, charge disclosures, billing | 2022 | Cards | K |
| L7 | Recovery: RBI guidelines on recovery agents; SARFAESI Act 2002; DRT Act 1993 | RBI / Parliament | Secured-loan enforcement | — | Secured loans | K |
| L8 | Credit Information Companies (Regulation) Act, 2005 | Parliament/RBI | Credit-report accuracy and disputes | 2005 | Credit reports | K |
| L9 | Unregulated-lending legislation (Banning of Unregulated Lending Activities bill) and state money-lender Acts | Centre/State | **Status unknown to me — check whether enacted** | — | Informal lenders | K / unresolved |
| L10 | RBI e-mandate (recurring payments) framework | RBI | Additional-factor authentication above threshold; pre-debit notification | 2021 onwards; thresholds raised for some categories 2023 — verify | EMIs, subscriptions, insurance premium | K |

## 4. Warranties, subscriptions, services

| # | Instrument | Authority | Relevant provisions | Dates | Applicability | Status |
|---|---|---|---|---|---|---|
| C1 | Consumer Protection Act, 2019 | Parliament | Definitions (defect, deficiency, unfair contract s.2(46), unfair trade practice s.2(47)), product liability (Ch. VI), CCPA, commission jurisdiction, limitation 2 years (s.69), mediation | 2019; in force 20 Jul 2020 | All consumer goods/services (insurance and banking services included; tenancy generally not — legal question Q-T1) | K |
| C2 | Consumer Protection (Jurisdiction of the District Commission, the State Commission and the National Commission) Rules, 2021 | DoCA | District ≤ ₹50 lakh, State >₹50 lakh–₹2 crore, National >₹2 crore, **by consideration paid** | Notified 30 Dec 2021 (S2) | Forum selection | S2 |
| C3 | *Rutu Mihir Panchal v. Union of India* (SC) | Supreme Court | Upholds consideration-based pecuniary jurisdiction | 29 Apr 2025 (S2) | Forum selection | S2 |
| C4 | Consumer Protection (E-Commerce) Rules, 2020 | DoCA | Seller/platform disclosures, grievance officer, return/refund | 2020 (amended 2021) | Online sales/services | K |
| C5 | Guidelines for Prevention and Regulation of Dark Patterns, 2023 | CCPA | 13 specified dark patterns (incl. subscription traps, drip pricing) | 30 Nov 2023 (S2) | Platforms offering goods/services | S2 |
| C6 | Guidelines for Prevention of Misleading Advertisements, 2022 | CCPA | Ad claims | 2022 | Marketing | K |
| C7 | Sale of Goods Act, 1930 (ss.14–17 implied terms) & Indian Contract Act, 1872 (ss.23, 28, 73–74) | Parliament | Implied conditions; void restraint of legal proceedings; liquidated damages vs penalty | — | Goods; any contract | K |
| C8 | Right-to-repair framework / DoCA portals; National Consumer Helpline (1915) | DoCA | Pre-litigation facilitation | — | Goods | K / P-TODO |
| C9 | RBI/NPCI e-mandate rules | see L10 | Auto-debit subscriptions | — | Subscriptions | K |
| C10 | RBI dark-pattern requirements for regulated entities | RBI | Reported effective 1 Jan 2027 | S1 | Financial products marketing | S1 |

## Gaps in this register

- No primary-source text has been read for any row.
- Sectoral rules not yet researched: telecom (TRAI), electricity, airlines (DGCA), education, real estate (RERA) — outside the four requested domains.
- State-wise rent/stamp tables not built (see report §E).
