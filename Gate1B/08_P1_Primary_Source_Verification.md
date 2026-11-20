# 08 — P1 Primary-Source Verification (D1–D4)

Date 2026-11-20. Baseline: Docs 01–07. Method: official PDFs downloaded and text-extracted (pdftotext), then read. Evidence IDs continue from Doc 02 (R19+).
Provenance grading used here: **VP** = official text read from an official host · **VP-mirror** = text of the official instrument read from a non-official host (provenance caveat) · **VS** secondary · **UNK**.

## Summary table
| Item | Done | Verified | Still unverified | Not accessible |
|---|---|---|---|---|
| D1 IRDAI | Health Master Circular 29.05.2024 read in full (mirror) | Free look, cooling, cashless 1h/3h, 60-month moratorium, repudiation approval, award compliance + ₹5,000/day | Policyholder Protection Regs 2024 + its master circular (claim-settlement timelines, interest, grievance TAT); Insurance Products Regs 2024 (PED cap 36 months) | Final regs PDF not found on irdai.gov.in listings or by search |
| D2 RBI | RB-IOS 2026 read in full (bank-hosted copy of the scheme) | Commencement, scope, eligibility, 90-day limit, caps, representation rule, forum bar, repeal/transition | That copy equals RBI's official PDF byte-for-byte | RBI's own PDF is behind a human-verification page; **I did not attempt to bypass it** |
| D3 Act | SBSR Act 2025 and IRDAI consolidated Insurance Act (updated 5 Feb 2026) read | Assent, commencement mechanism, s.45 text and that the Act does not amend it | Which Act sections are notified in force beyond those marked w.e.f. 5-2-2026 | — |
| D4 Data | IRDAI Annual Report 2024-25 read (grievance + claim tables) | Bima Bharosa totals, claim-count tables | CIO FY25 report (PDF is image-only; no OCR available here); nature-wise Ombudsman tables FY24/FY25 | CIO FY25 text layer |

---
## D1 — IRDAI instruments

**R19 (VP-mirror)** *Master Circular on Health Insurance Business*, Ref IRDAI/HLT/CIR/PRO/84/5/2024, 29.05.2024, issued under s.14(2)(e) IRDA Act 1999, s.34 Insurance Act 1938, Reg 7 Sch. III of IRDAI (Insurance Products) Regs 2024. Read from caalley.com mirror (digitally signed IRDAI document; official portal copy not retrieved).
- **Applicability:** addressed to general, health **and life** insurers; health products. Effective immediately except where dated; non-compliant existing products to be modified by **30.09.2024**; accrued waiting/moratorium credits must be carried forward (Effective-date ¶(2)).
- **Free look (para 5):** 30 days from receipt of policy document, for policies of **term of one year or more**. This resolves WS03 "life-only vs life+health": **in this circular it applies to health policies ≥1 year.** Not stated for renewals in this text (earlier claim "not applicable to renewals" unverified here).
- **Cancellation (para 6):** policyholder may cancel an indemnity health policy anytime on 7 days' notice, with proportionate refund rules.
- **Moratorium (para 13):** no health policy or claim contestable for non-disclosure/misrepresentation **except established fraud**, after **60 months of continuous coverage**; accrued credits under porting/migration count. *Not* s.45 — the Insurance Act s.45 as read is life-only (see D3). **WS03 row "Insurance Act s.45; moratorium (5 yrs)" for health claims is corrected: health moratorium rests on this circular.**
- **Cashless (para 15):** decision on cashless authorisation "immediately but not more than one hour"; systems by 31 July 2024. **Final discharge authorisation within 3 hours** (para 16); delay beyond 3 hours: additional hospital charges borne by insurer from shareholders' funds. These are obligations on insurers; "regulator expects" framing in WS03 is too weak — it is mandatory text — but remedy for a breach is not specified in the circular.
- **Claims (para 17):** no claim repudiated without approval of the PMC or a three-member Claims Review Committee; partial/total repudiation must be conveyed **with full details referencing the specific policy terms**; insurer collects documents from hospitals. (Strong hook for C1: the denial letter must cite specific terms.)
- **Grievance (para 19):** insurer's response must include Ombudsman contact details.
- **Ombudsman award compliance (para 20):** within 30 days of receipt; non-compliance → **₹5,000/day penalty to complainant** plus penal interest under Rules. (Matches Rules r.17(6); the penalty figure is new vs Doc 02.)
- **Not in this circular:** PED waiting-period cap and claim-settlement timelines/interest. The circular says detailed provisions of paras 4, 5, 7, 12, 17, 18, 19, 20 are given in the master circular under the Policyholder Protection Regulations 2024 — **not read**.
- **PED cap (R20, VS):** IRDAI health pages/press: maximum PED waiting 36 months; PED look-back 36 months; specific waiting up to 36 months. Official Schedule text not read. Status VS, medium.
- **Interest on delay (R21, VS):** secondary (Business Today quoting IRDAI, Sept 2024): bank rate + 2% from intimation. Regulation text not read. Status VS; do not encode.
- **Policyholder Protection Regs 2024 (R22):** issued 5 Sept 2024 per JSA compendium (VS). Final PDF not located. Secondary reports free-look 30 days for life and health. **Open.**

Corrections to WS03: (a) free-look scope resolved for health ≥1yr; (b) moratorium source corrected; (c) cashless timeline firm; (d) PED cap remains secondary.

## D2 — RB-IOS 2026 (R23, VP-mirror)
Text read: Ujjivan SFB-hosted PDF of "Reserve Bank – Integrated Ombudsman Scheme, 2026" (24 pages, "Public document"). RBI's own copy (rbidocs.rbi.org.in) returns a human-verification page to automated clients; I did not circumvent it. RBI's website lists the scheme and the CMS portal references it (R9).
- **Commencement/repeal (cl.1(2), cl.20):** in force **1 July 2026**; RB-IOS 2021 repealed 1 July 2026; **2021 complaints received before that date, their appeals and award execution stay under 2021.** 
- **Coverage (cl.1(3), cl.4):** commercial, RRB and cooperative banks (thresholds), NBFCs (with exclusions e.g. HFCs/CIC/IDF), prepaid instrument issuers, credit information companies — read carefully: some NBFC categories excluded; exact list needs counsel/second read.
- **Who may file (cl.9–10(1)(b), cl.3(1)(c)):** customer personally **or via an "authorised representative" — a person other than an advocate, appointed in writing**; advocate only if the advocate is the aggrieved person. Complaint must be addressed to the Ombudsman directly (cc is not enough). **This differs from the Insurance Ombudsman Rules (R5), which name only complainant/legal heirs/nominee/assignee.** For a lending product, a non-advocate agent may lawfully lodge a complaint with written authority; whether automated tooling is allowed remains a portal-terms question (UNK).
- **Maintainability (cl.10):** must first complain to the bank with proof; maintainable if no reply within **30 days** (or longer period specified by RBI/NPCI/card network) or user unsatisfied; **filed within 90 days** of that timeline's expiry or last bank communication, whichever is later (cl.10(1)(e)–(g)); not duplicative of a pending/settled matter before the Ombudsman, court, tribunal, arbitrator or other judicial/quasi-judicial forum (cl.10(1)(h)–(k)); criminal proceedings excepted.
- **Relief (cl.8(3)):** no cap on the dispute amount; **consequential loss up to ₹30 lakh plus up to ₹3 lakh** for time, expenses, harassment/mental anguish.
- **Annex:** complaint form requiring prior bank complaint proof; mandatory requirements listed.
- **Resolves earlier secondary leads:** 90 days ✔, ₹30L/₹3L ✔ (now primary-text supported), 15-day RE reply **not checked** in the portions I read (cl.14 timelines unread in detail), 2021 vs 2026 transition ✔.
- **Conflict with older RBI directions:** Banks' Credit Facilities Directions (Nov 2025, "updated 1 Oct 2026") para 12(4) still names the 2021 scheme and a 30-day trigger — those are legacy references; the 2026 scheme controls complaints from 1 July 2026. Treat bank directions' references as outdated text, not a different rule (my reading; counsel to confirm).
- **Unread:** cl.11–19 procedural details, appeal provisions, penalties on REs.

## D3 — Insurance Act amendment (R24, VP)
Sources: Sabka Bima Sabki Raksha (Amendment of Insurance Laws) Act, 2025 (Gazette 21 Dec 2025; assent **20 Dec 2025**; Bill No. 195-F of 2025; Act 40 of 2025); IRDAI consolidated "Insurance Act 1938 (updated with SBSR Act 2025)" listed 05-02-2026.
- **Commencement (s.1(2)):** on dates the Central Government appoints by notification. Consolidated footnotes show many amendments **w.e.f. 5 Feb 2026**; other provisions may be pending (a secondary source says s.32A not yet effective). **Do not assume the whole Act is in force.**
- **Section 45:** the Act's text contains **no amendment of s.45** (searched; only s.52A of that number). The consolidated s.45 is headed "Policy not to be called in question on ground of misstatement after two years" but its operative text says **three years**, applies to **life insurance policies only**, with fraud carve-out (Explanations I–II: fraud definition; mere silence not fraud unless duty to speak) and the rule that insurer cannot repudiate on fraud if insured proves statement true to best of knowledge or no deliberate intent (sub-s.(3)). Footnotes attribute s.45 to the 2015 Act only.
- **What this means for the product (my analysis, not legal advice):**
  1. **s.45 is not the legal basis of the health moratorium.** The 60-month rule is IRDAI circular text. A product explaining a *health* repudiation must cite the circular (and any regulations), not s.45. WS03/WS01 conflated these.
  2. The December 2025 Act did **not** change misstatement/non-disclosure law as far as the Act's own text shows; the headline relevant change is IRDAI/structure/FDI-type reform. For policy-interpretation logic the Act changes little; it does require **effective-date awareness** (many provisions w.e.f. 5-2-2026).
  3. Heading/text mismatch ("two years" vs "three years") means the product must quote operative text, not section headings.
- **Counsel questions:** (a) which provisions are notified and when; (b) interaction of s.45(1)–(4) with IRDAI moratorium for health and for motor/general (s.45 life-only); (c) whether the circular's moratorium binds pre-2024 policies (retrospectivity; circular requires carry-forward of accrued credits).

## D4 — FY25 data (R25–R28)
**R25 (VP)** IRDAI Annual Report 2024-25 (listed 30-12-2025), Table II.15/II.17, paras II.6.7–II.6.9:
- **2,57,790** grievances on Bima Bharosa in FY25 (Life **1,20,429**; General & Health **1,37,361**). FY24: 2,15,569 (life 1,20,726; G&H 94,843). So G&H grew ~45% (94,843→1,37,361) — **computed by me.**
- *(Correction 2026-11-20: IRDAI states the ~69% for grievances under **general insurance business**, not G&H combined.)* G&H grievances: ~**69% claims-related**; **88,841** claims-related G&H grievances against **11.26 crore** claims (≈0.08% of claims, my arithmetic). Life: 33,280 claim grievances vs 26.68 lakh claims.
- Life UFBP (unfair business practice) grievances: 26,667 FY25 (23,335 FY24); resolved 26,495: 9,613 in favour, 1,797 partly, 15,085 against.
- Pending at year-end: 10,160 overall (G&H 9,606). Reported attended: 2,47,630.
- **Correction:** WS02's "IRDAI-reported **health** grievances rose 41% to 1,37,361" is mislabelled — 1,37,361 is **General & Health combined** (and the rise is ~45% by my arithmetic from the table, not 41%; the 41% may use a different base). WS01's "health-claim complaints 47,658→64,365" is **not found** in this report; unverified, do not use.
- **R26 (VP, derived)** Table I.29 health claims of general & health insurers FY25 (excl. PA/travel; my column reading): total 372.58 lakh claims (₹1,32,487.81 cr); paid 326.01 lakh (**87.50%** by number; 71.14% by amount); disallowed ₹18,521.02 cr (13.98% of amount); **repudiated per terms 29.51 lakh (7.92% by number; 8.61% by amount)**; outstanding 17.07 lakh (4.58%). WS02's FY24 "~11% repudiated / 12.9% value disallowed" is a different year and a different measure; **not reconcilable without FY24 table** — I did not verify it. Claim-level rates ≠ grievance rates ≠ Ombudsman complaints.
- **R27 (VP, from earlier read)** CIO FY24: 52,575 received, 49,705 disposed, 12,855 non-entertainable, 36,850 entertainable, 15,528 by recommendation (mediation). **A recommendation is a settlement proposal the complainant must accept (rule 16(2), 15 days); it is not evidence of relief obtained.** Doc 02 R12's "≈56% complainant-favourable" combined recommendations and awards — **withdrawn as a measure of consumer relief.** What can be said: of health complaints disposed (my reading) 6,233 awards favoured complainant vs 4,122 favoured insurer; recommendation outcomes unknown.
- **R28 (UNK)** CIO FY25: PDF is image-only (text layer ~1 KB; no OCR available in this environment). Needs OCR/manual transcription. Nature-wise tables FY24 exist in the FY24 PDF ("Nature wise classification" statements 12–18) but I did not extract them in this pass.
- **Reconciliation of measures:** claims (372.58 lakh health claims) → insurer grievances/IRDAI Bima Bharosa G&H (1,37,361, FY25) → Ombudsman complaints (~52.6k all lines FY24) → awards/recommendations. Each step is a different population; no single "consumer relief rate" can be computed from these tables.

## Decisions affected
- C1: strengthened (denial must cite specific terms; Ombudsman award; 1h/3h; moratorium) but **volume statements revised downward in certainty**; repudiation about 8% of health claims by number (FY25, derived) is a baseline more reliable than WS02 survey figures.
- C2: RBI 2026 scheme now primary-supported, incl. **representative filing allowed** (non-advocate).
- Product boundary: submission-on-behalf is **sector-dependent** (insurance: not clearly allowed; RBI: allowed via written authority) — changes Doc 01 §6 generalisation.
- Rule library needs mandatory effective-date metadata (confirmed).
