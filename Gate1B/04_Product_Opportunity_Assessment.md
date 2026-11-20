# Document 4 — Product Opportunity Assessment (Gate 1B)

Not an approved MVP. Ratings are qualitative (H/M/L) with stated weights; no numeric scores because evidence does not support precision.

## Criteria and weights
Higher weight (×3): severity of pain, willingness to pay, differentiation vs general AI, regulatory/liability tolerability.
Medium (×2): frequency/repeat use, technical feasibility, trust barrier, distribution feasibility.
Lower (×1): strategic fit with platform. Rationale: a startup dies from lack of payer and defensibility before it dies from lack of fit.

| Candidate | Pain | Freq/repeat | WTP | vs General AI | Tech | Reg/liability | Trust | Distribution | Platform fit | Evidence depth |
|---|---|---|---|---|---|---|---|---|---|---|
| C1 Denied/short-paid health or motor claim → policy-vs-denial analysis + complaint pack for free channels (Act) | H | L (episodic) | M? (unproven) | M (workflow, forms, deadlines, evidence; reading alone is not a moat) | M | M–H risk (advice/legal-practice unclear) | Hard | M (claimant forums, search) | H | Strongest |
| C2 Loan documents reconciled to KFS/RBI baseline (fees not in KFS, prepayment, cooling-off, exit) (Understand/Protect) | M | M | UNK | M–H (deterministic regulatory rule library + date logic) | M–H (structured KFS) | M (RBI rules readable; no advice) | Hard | L–M | H | Rules strong, demand UNK |
| C3 Pre-purchase/renewal policy understanding (Understand) | M | M (annual) | L (₹0–199 competitors) | L | M | M | M | M (but incumbents own it) | H | Medium, commercially biased |
| C4 Recurring-charge/subscription evidence & cancel (Protect/Act) | L–M | H | L–M | L–M | M | L–M | M | M (bank/UPI incumbents) | M | Survey only |
| C5 Rental exit pack (deposit, move-in evidence) | M | L | L | L | M–H | H (state) | M | M | M | Anecdote only |
| C6 Warranty/purchase | L–M | L | L | L | M | M | M | L | L | None |
| C7 Cross-category personal vault/tracker | Unproven | Potentially high | L (storage commoditised) | L | M | M (privacy) | Very hard | cold start | Defines platform | None — **no demand evidence** |

## Candidate detail (top three)

### C1 — Claim denial → analysis + complaint preparation
- **User:** policyholder (or family member) with a rejected/partly paid/delayed claim. Possibly a delegator (HYP).
- **Problem:** denial letter cites clause/non-disclosure; user cannot tell if defensible, what evidence matters, which forum. Primary evidence: Ombudsman jurisdiction explicitly covers repudiation, delay and construction of policy terms (R4); ≈31k health complaints FY24 (R12); 24% of CIO disposals non-entertainable (R11), consistent with intake confusion (HYP).
- **Workaround:** agents/advisors, Samadhan-type firms (success fee, opaque), self-filing (free), lawyers.
- **Intervention:** extract policy clauses + denial reasons, show quote-level mapping, list missing documents, compute statutory deadlines (1-yr/1-month, R2), assemble complaint draft; user submits.
- **Demand evidence:** pain strong; **payment and behavioural demand unproven**; Ombudsman is free so paid value must be quality/speed/confidence.
- **Alternatives:** general AI, claim-assist firms, insurer desks, advisors.
- **Feasibility:** two source documents of known type; groundable. Hard parts: scanned denial letters, riders, correspondence.
- **Regulatory:** cannot present outcome predictions as legal determinations; legal-practice (Q-P1), DPDP (health data), third-party filing unclear (R5). Insurance-advice licensing boundary UNK.
- **Monetisation:** per-case fee (flat, not success-share, to avoid conflict) HYP; B2B2C unlikely given conflict. Episodic so subscription weak.
- **Uncertainties:** do users reach for software at that moment? does preparation change outcomes? general-AI parity.

### C2 — Loan document vs regulatory baseline
- **User:** first-time/education/personal borrowers; co-borrowing parents.
- **Problem (verified part):** rules exist that a document can violate (R7: fees not in KFS cannot be charged; R8: prepayment charges barred in scope; R6 cooling-off). **Problem frequency of violations/misunderstanding is UNKNOWN.**
- **Intervention:** reconcile KFS, sanction letter, schedule of charges; flag discrepancies; deadline tracker (cooling-off, document-return).
- **Why interesting:** differentiation is a maintained, effective-dated rule library plus structured KFS — something a generic chat does not provide reliably. Also gives the cleanest test of the shared-engine hypothesis (structured doc, deterministic checks).
- **Risks:** demand unknown; bank/NBFC split (NBFC texts unread); users rarely seek checks post-sanction; free regulator channels.
- **Monetisation:** unknown; B2B2C (education-loan platforms) may conflict.

### C3/C4–C7: see matrix. C3 is commoditised; C4 is crowded and low-stakes; C5 is blocked by state law; C7 has no demand evidence and should not be a launch concept — a vault is plausibly a *by-product* of C1/C2 (documents already collected) rather than a product.

## Recommendation (provisional, for Gate 2 review)
1. **Do not** select the three-pillar, five-domain platform as a first product.
2. Shortlist **C1** (strongest pain/pathway) and **C2** (strongest verified rule base, best engine test) for a time-boxed validation sprint: interviews + general-AI head-to-head + wizard-of-oz case handling. Insurance must win on evidence, not on data availability.
3. Treat C3, C4 as **entry surfaces/feeders** for C1/C2, not standalone products. Park C5, C6, C7.
4. Gate 2 decision criteria (proposed, to be approved): ≥N real cases where preparation changed the user's action; stated and revealed WTP at the moment of need; general-AI baseline beaten on grounded accuracy for a defined test set; counsel opinion that product boundary is safe.
