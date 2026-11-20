# Document 1 — Executive Research Report (Gate 1B)

Date 2026-11-20. Companion files: 02 Evidence Register · 03 Domain Matrix · 04 Opportunity Assessment · 05 Technical Feasibility · 06 Gaps & Validation · 07 PROJECT_FOUNDATION.

## 0. Honest scope statement
Done: reviewed WS01/WS02/WS03 and register; read primary texts for the Insurance Ombudsman Rules 2017 (amended to 9 Nov 2023), CIO Annual Report 2023-24, RBI bank directions on KFS/cooling-off/prepayment, Bima Bharosa, NCH and CIO pages.
**Not done:** consumer interviews, hands-on testing of any competitor or general AI, a technical benchmark, e-Jagriti/RBI CMS terms, IRDAI 2024 regulations and Health Master Circular, RB-IOS 2026 text, DPDP Rules, financial modelling. Findings below are bounded by that. Gate 1B is **not complete**.

## 1. Executive summary
**Verified (primary):**
- Insurance Ombudsman: free, insurer-first, 1-year limit, ₹50 lakh ceiling, award in 3 months, binding on insurer; jurisdiction expressly covers claim repudiation, delay and policy construction. FY24: 52,575 complaints; ~24% of disposals non-entertainable. (R1–R4, R11)
- Banks must give a standard KFS for retail loans from 1 Oct 2024, may not charge fees outside it without consent, cooling-off is at least 1 day, and floating-rate prepayment charges are barred for in-scope loans from 1 Jan 2026. (R6–R8)
- The Ombudsman Rules do not clearly authorise third-party filers; portals use OTP to the complainant; no public API was found. (R5, R13–R15)
**Corrections to earlier work:** Ombudsman ceiling is ₹50L (not unresolved); cooling-off minimum is 1 day; "accept award in 30 days" was wrong.
**Hypotheses (unvalidated):** consumers will use software at claim time and pay; the lifecycle is one journey for users; trust/provenance beats free general AI; a vault has standalone value.
**Unknown:** willingness to pay, upload trust, general-AI parity, benchmark accuracy, legal boundary.

## 2. Product vision and scope — unchanged (see 07).

## 3. Methodology
Evidence register with six classes; primary-source reads via fetch and PDF extraction; contradictions kept visible; negative evidence sought. Limits in §0.

## 4. Consumer problem findings
Pain is best evidenced in insurance claims (regulator volumes, case reversals). Survey prevalence figures (35% understand, 50% rejected/partial, 67% trap) come from commercial or self-selected panels and **must not be used as rates**. Rental and lending comprehension have almost no evidence — absence, not disproof. Interview protocol exists (WS02); no interviews run.

## 5. Market and competitor findings
Indian explainers are cheap/commoditised (₹0–199) [SU]. Incumbents (Policybazaar, Ditto) own claim help with commission conflict. Paid dispute help (Insurance Samadhan) has conflicting fee data. Free official channels exist. **None of this was tested hands-on**, and general AI was not benchmarked — so "no moat in reading" is an inference, and "dedicated product beats general AI" is unproven either way.

## 6. Regulatory and grievance findings
See Doc 2. Key product boundaries: prepare and guide yes; submit-on-behalf not supported; effective-date logic mandatory (2024 circular, Dec 2025 Act, Jan 2026 prepayment rule, RB-IOS 2026); state-gating for tenancy. Legal-practice and DPDP questions remain with counsel.

## 7. Technical feasibility
Extraction/provenance shared; interpretation, regulatory baseline and routing domain-specific (Doc 5). Unmeasured. Benchmark proposed.

## 8. Business model
No WTP evidence. Plausible: flat per-case fee at the claim moment; weak: subscription (episodic use), B2B2C (conflicts with trust principle). No forecast produced because no credible inputs exist.

## 9–10. Cross-domain and shared-engine assessment
Doc 3. The shared engine is credible as infrastructure, not as the product's differentiator; value sits in domain modules and workflow. **Recommended structure: Option B (shared foundation + domain modules), with one domain-specific first experience.** Option A (universal experience) is unsupported by evidence; Option C remains a fallback if lifecycle linkage proves not to matter to users.

## 11. Strategic opportunities
C1 claim-denial analysis + complaint prep; C2 loan KFS reconciliation; C3/C4 as feeders; C5–C7 parked (Doc 4).

## 12. Disconfirming evidence
Explain-the-policy is cheap and crowded; understanding a policy may not change insurer decisions; free regulator channels; claim use is episodic; survey bias; courts slow; DoNotPay shows overclaiming risk; no evidence of user demand for a cross-category vault; regulation is already reducing some problems.

## 13. Risks and unknowns — Doc 6.

## 14. Recommendations
1. Do not approve the full three-pillar, multi-domain build. 2. Run the validation sprint (primary reads, general-AI head-to-head, interviews, concierge WTP) for C1 and C2. 3. Retain the broad identity in PROJECT_FOUNDATION; select starting point by evidence. 4. Brief counsel now.

## 15. Next-stage requirements — Doc 7 conditions.

## Part 8 — Final strategic answers
1. **Worth pursuing?** *Problem:* yes for claim disputes (evidenced), unproven elsewhere. *Solution:* the broad platform is not supported; a narrow, grounded workflow might be. *Business:* unproven — no payer evidence; episodic use is a structural concern.
2. **Shared engine credible?** Partly: ingestion, extraction, provenance yes; interpretation, rules, routing no. Untested by any benchmark.
3. **Most promising problems:** claim disputes (Act/insurance); loan-term reconciliation (Understand-Protect/lending, demand unknown); recurring-charge evidence is a feeder.
4. **Initial focus:** shortlist C1 and C2 for Gate 2 comparison. Not final.
5. **Avoid:** generic document chat; auto-submission; legal-conclusion outputs; success-fee models that bias advice; rental statutory conclusions without state data; vault-as-launch-product; outcome-probability claims.
6. **Differentiation (conditional):** grounded, abstaining, effective-date-aware outputs with maintained rule libraries and a prepared evidence/complaint pack — versus general AI (no rule versioning/workflow), lawyers (cost/access), intermediaries (conflict), storage apps (no understanding), grievance portals (no preparation). Each comparison is an inference until tested.
7. **Must be true:** users act at claim time with software; they will upload and pay; grounded output measurably beats general AI; preparation improves outcomes; legal boundary is workable.
8. **Before implementation:** approve gate decisions listed in Doc 6; complete P1 items; select opportunity (Gate 2); define product (Gate 3); design architecture (Gate 4).

## Critical self-review
- *Insurance over-weighted?* Yes risk remains; I forced C2 into the comparison and kept insurance's WTP as unproven. Insurance data still dominates because other domains lack data.
- *Lifecycle investigated?* Only at framework level; no consumer evidence for the lifecycle as one journey.
- *Vault overestimated?* Demoted.
- *General AI underestimated?* Possibly: untested. Treat as the primary risk.
- *Grievance integration overstated?* Reduced to prepare/guide.
- *Legal information vs advice?* Retained as open counsel question.
- *Secondary numbers?* Flagged, not used as rates. My own CIO derivation (≈56%) is column-mapped by me and medium confidence.

## Gate 1B assessment
**Ready for review:** foundation (07); ruling out the full-platform-first build; the prepare/guide boundary; C1/C2 shortlist as hypotheses; corrected regulatory facts.
**Unresolved:** initial opportunity; architecture option; monetisation; WTP; general-AI parity; legal boundary.
**Evidence still required:** Doc 6 P1 items (D1–D4, D8, H1) and consumer evidence. Gate 2 should not be convened until those exist.
