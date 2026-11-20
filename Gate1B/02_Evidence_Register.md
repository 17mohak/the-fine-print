# Document 2 — Evidence Register (Gate 1B)

Compiled 2026-11-20. Classification: **VP** verified primary (official text read this session) · **VS** verified secondary · **SU** secondary, unverified · **INF** inference · **HYP** hypothesis · **UNK** unknown.
Confidence: H / M / L. "Source IDs" S# refer to WS01 register, W# to WS03 register, R# to this register.

## Part A — Claims verified or corrected in this pass (primary reads)

| ID | Area | Claim (original → status) | Source actually read | Status | Conf. | Limitation | Strategic implication |
|---|---|---|---|---|---|---|---|
| R1 | Reg | **Insurance Ombudsman award ceiling is ₹50 lakh** (WS03 had ₹30L vs ₹50L conflict → **resolved**) | Insurance Ombudsman Rules 2017 as amended till 09.11.2023, rule 17(3) proviso (ii) "rupees [fifty] lakhs (including relevant expenses)" — PDF at cioins.co.in/notification/…; CIO FAQ Q9 agrees | VP | H | Later amendments after Nov 2023 not checked | Ombudsman covers most individual health/motor disputes by value |
| R2 | Reg | Complaint must first go to insurer; 1 year limitation; wait 1 month if no reply | Rules 2017 r.14(3)(a)–(b) (amended) | VP | H | — | Deadline tracking is a real, statute-based feature |
| R3 | Reg | Award within 3 months of receiving all requirements; insurer must comply within 30 days; award binding on insurer; mediation recommendation within 1 month, complainant accepts within 15 days | Rules r.16–17 | VP | H | WS03 said "accept award within 30 days" — not what rule 16/17 says (15 days applies to mediation recommendation). **WS03 corrected.** | Timeline logic must separate mediation vs award |
| R4 | Reg | Grounds Ombudsman can hear include partial/total repudiation, claim delay, **legal construction of policies re claims**, misrepresentation of terms, policy-servicing | Rules r.13(1)(a)–(i) | VP | H | — | Document-vs-denial analysis maps directly to Ombudsman jurisdiction |
| R5 | Reg | Who may complain: "himself or through his legal heirs, nominee or assignee"; complaint may be by email or online via CIO site | Rules r.14(1)–(2) | VP | H | Silent on authorised agents/third-party tools; CIO online form not read | **Third-party submission is NOT clearly authorised.** Default design: user files, product prepares |
| R6 | Reg | Cooling-off for digital loans: **minimum 1 day** (WS03 1 vs 3 conflict → resolved; "3" is KFS validity of ≥3 working days) | RBI Commercial Banks – Credit Facilities Directions 2025 para 11(1), para 348(2) (rbi.org.in BS_ViewMasDirections id=13156 / 13140); NBFC equivalent id=12957 para 11(1) | VP | H | Directions are "updated as on 1 Oct 2026"; standalone "Digital Lending Directions 2025" not located — rules sit in consolidated Directions | Deterministic regulatory checks exist for lending |
| R7 | Reg | KFS mandatory for retail/MSME term loans sanctioned on/after 1 Oct 2024 (banks); fees not in KFS cannot be charged without explicit consent | RBI Responsible Business Conduct Directions 2025 (banks) para 348(1),(5) | VP | H | NBFC RBC Directions not read | **Document-vs-regulatory-baseline checking is feasible and testable for loans** |
| R8 | Reg | No prepayment charges on floating-rate loans to individuals (non-business, and business to individuals/MSEs) for loans sanctioned/renewed on/after 1 Jan 2026 (banks, excl. SFB/payments/LAB) | RBC Directions paras 352–353 | VP | H (banks) | NBFC/SFB not verified; WS03 "verify date" → resolved for banks | Effective-date logic is mandatory |
| R9 | Reg | RBI Integrated Ombudsman Scheme 2026 **exists** (RBI lists "RB-IOS 2026"); CMS homepage shows it | rbi.org.in Schemes.aspx; RBIOOS01072026OB_E.pdf listed | VP (existence only) | M | PDF blocked by CAPTCHA; window, caps, effective date, transition **unread**. Banks' Directions still cite 2021 scheme and 30-day wait | Treat WS03's 90-day/₹30L figures as UNVERIFIED leads |
| R10 | Reg | Sabka Bima Sabki Raksha (Amendment of Insurance Laws) Act 2025 exists; listed on IRDAI 21-12-2025; IRDAI consolidated Act updated 05-02-2026 | irdai.gov.in/acts | VP (existence) | M | Text unread; s.45 effect unknown | WS03 I2 stays S-level for content |
| R11 | Data | CIO FY2023-24: 52,575 complaints received, 49,705 disposed, 12,855 non-entertainable, 36,850 entertainable disposed, 15,528 by mediation, ~87% within 90 days, ~40% registered online | CIO Annual Report 2023-24 (cioins.co.in/annualreports/AnnualReport2023-2024.pdf), Chairman's review | VP | H | | Anchors volume; 24% of disposals were non-entertainable → **intake quality is a real problem a preparation tool could address (HYP)** |
| R12 | Data | CIO health insurance FY24: company-wise table total 31,362 received (34,203 incl. opening); of 29,276 disposed: 10,135 recommendations, 6,233 awards for complainant, 4,122 for insurer, 2,087 withdrawals, 6,699 non-entertainable (my reading of table columns) | Same report, Health company-wise statement | VP (derived by me; column mapping not independently confirmed) | M | WS02 cited 31,490; small gap unexplained. Recommendation ≠ full relief | Complainant-favourable outcome (mediation+award) ≈ 56% of disposals **by my arithmetic**; WS01's "63/75/54%" remains unreliable |
| R13 | Portal | Bima Bharosa: online registration with OTP, token tracking, "attended within 14 days", paper/email/155255 routes; no API or representative-filing statement found | bimabharosa.irdai.gov.in + /Home/FAQ | VP | H (for what's stated) | User manual not read; absence of API statement ≠ proof none exists | Guide-to-portal feasible; integration unknown |
| R14 | Portal | NCH convergence programme: voluntary, free, "2000+ companies" joined; 30-day response expected | consumerhelpline.gov.in/public/convergenceprogram | VP | M | WS01 had 1,142 (Sept 2025) — **date difference, not resolvable**; NCH third-party/API terms not found; disclaimer requires permission to reproduce portal content | |
| R15 | Portal | CIO online filing requires OTP to complainant's registered mobile; tracking same | cioins.co.in FAQ | VP | H | CIO homepage contained placeholder text (lorem ipsum) — site-quality signal | OTP-bound flows make delegated automation impractical (INF) |
| R16 | Portal | e-Jagriti: eligibility, representation, API, automation terms — **could not be read** (JS-rendered) | e-jagriti.gov.in | UNK | — | Needs browser session | Open |
| R17 | Reg | IRDAI Policyholder Regs 2024, Health Master Circular 29 May 2024: **not located/read** (free-look, cashless 1h/3h, 5-yr moratorium, PED cap all remain secondary) | irdai.gov.in listings | UNK | — | 609 circulars, 182+ regs unread | Highest-priority desk task |
| R18 | Reg | IRDAI FY25 and DPDP Rules commencement: **not read** (PDF >10MB; MeitY returns 403) | — | UNK | — | Can be downloaded manually (my CIO download worked via curl) | Desk task |

## Part B — Original WS01/WS02/WS03 claims: disposition

| Orig. claim | Where | Disposition | Conf. |
|---|---|---|---|
| 2,57,790 Bima Bharosa complaints FY25; health-claim complaints 47,658→64,365; 53,102 to Ombudsman FY25 | WS01 S9 | SU. Not re-verified (IRDAI AR 24-25 unreadable here). Note WS02 separately cites 1,37,361 health grievances FY25 — **different measure/source, unresolved** | L–M |
| 31,490 health complaints FY24; ~95% are denials | WS02 | Partly: CIO table gives 31,362; "95%" **not verified** (rule r.13 grounds include non-denial; I did not locate the nature-wise table) | M / L |
| ~11% health claims repudiated FY24; 12.9% by value | WS02 | SU | L–M |
| LocalCircles 50% rejected/partial, 67% subscription trap, 35% understand policy | WS02 | SU; self-selected/commercial sponsor. Do not use as prevalence | L |
| Insurance Samadhan 18%+GST fee | WS01 S6 | Conflicting sources (15%/18%/₹500); unresolved | L |
| 5+ Indian AI policy explainers exist (₹0–199) | WS01 S14 | SU vendor/Product Hunt; existence plausible, quality unknown | L–M |
| "Bima Bharosa not integrated with Ombudsman" | WS01 S9 | SU (parliamentary reply cited). Consistent with Ombudsman rules requiring separate filing (VP R2) but exact statement not re-read | M |
| No cross-category contract tracker exists in India | WS01 | **Not established** — no app-store sweep done | L |
| Stanford: legal AI hallucinates 17–33% | WS01 S16 | SU, different task; context only | M |
| FTC v DoNotPay $193k, final Feb 2025 | WS01 S15 | SU (FTC page cited); not re-opened | M |
| Free look, cashless 1h/3h, 5-yr moratorium, PED 3 yrs | WS03 | Still S1/S2 — see R17 | L–M |
| Cooling-off min "1 vs 3 days" | WS03 | **Resolved: 1 day** (R6) | H |
| Ombudsman ceiling ₹30L/₹50L | WS03 | **Resolved: ₹50L** (R1) | H |
| "Complainant accepts award within 30 days" | WS03 | **Corrected** (R3) | H |
| Insurer grievance response 14 vs 15 days | WS03 | Bima Bharosa page itself says 14 days for IRDAI-routed complaints (R13); insurer's own TAT remains unread | M |
| RBI 2026 scheme: 90-day window, 15-day RE reply, ₹30L/₹3L | WS03 | Unverified leads (R9) | L |
| Jurisdiction by consideration paid; Rutu Mihir Panchal 2025 | WS03 | S2; not re-read | M |
| Model Tenancy Act adoption (UP, TN, Assam, AP) | WS03 | Stale 2022; unresolved | L |

## Part C — Source access log (for honesty)
Read successfully: Ombudsman Rules 2017 (as amended 9.11.2023) PDF; CIO annual report 2023-24 PDF; CIO FAQ; Bima Bharosa home+FAQ; NCH home+convergence+disclaimer; RBI Credit Facilities and RBC Directions (banks), NBFC Credit Facilities para 1–15; IRDAI Acts listing.
Blocked/failed: indiacode.nic.in (403); MeitY (403); RBI 2026 scheme PDF (CAPTCHA); e-Jagriti (JS-only); BCI site (no content); IRDAI AR 24-25 via fetch tool (size).
Not attempted: competitor product pages, app stores, hands-on tests of any product or general AI.

---
## v2 change notes (2026-11-20, follow-up round 1) — see Doc 08 for detail
| Change | Evidence |
|---|---|
| R9 upgraded: RB-IOS 2026 text read (bank-hosted copy): in force 1 Jul 2026; 30-day bank wait; 90-day filing; ₹30L + ₹3L; non-advocate authorised representative allowed; forum bar; 2021 complaints stay under 2021. Previously "unverified leads" now primary-text supported (provenance caveat: non-RBI host). | R23 |
| R10 upgraded: SBSR Act 2025 read; assent 20 Dec 2025; commencement by notification (many provisions w.e.f. 5 Feb 2026); **s.45 not amended**; s.45 is life-only, 3 years. | R24 |
| R12 **withdrawn** as a measure of consumer relief (≈56% combined recommendations and awards). | R27 |
| R17 partly resolved: Health Master Circular 29.05.2024 read (mirror): 30-day free look (≥1yr), 60-month moratorium, 1h/3h cashless, specific-terms repudiation, ₹5,000/day award non-compliance. Policyholder Regs 2024 still unread. | R19–R22 |
| WS01 "health-claim complaints 47,658→64,365" not found in IRDAI AR 24-25 → unverified. WS02 "health grievances 1,37,361" = General & Health combined (IRDAI Table II.15/II.17). | R25 |
| New: FY25 health claims (G&H insurers): 372.58 lakh claims; 87.50% paid by number; 7.92% repudiated per terms by number (my column reading). | R26 |
| Samadhan fees: ₹999 + 20% + GST (self-stated), supersedes 15/18% conflict. | Doc 09 |

## Host-class convention (v3, per decision L8)
VP-official = read from issuing authority's host (Ombudsman Rules and CIO FY24 via cioins.co.in; IRDAI AR 2024-25, SBSR Act and consolidated Act via irdai.gov.in; RBI Directions via rbi.org.in; Bima Bharosa/NCH pages).
**VP-mirror (re-read from official host still needed):** R23 RB-IOS 2026 (bank-hosted copy; RBI PDF blocked by human-verification); R19 Health Master Circular 29.05.2024 (caalley.com mirror). Findings built on R19/R23 carry this caveat.
