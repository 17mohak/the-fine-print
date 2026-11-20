# PROJECT_FOUNDATION.md — THE FINE PRINT

Version: Gate 1B draft · 2026-11-20 · Canonical; changes require explicit approval.

## Identity
**THE FINE PRINT** — a personal consumer intelligence platform that helps people understand what they agree to, keep track of what they owe or are entitled to, and take informed action when something goes wrong. India-first.

## Three pillars (unchanged)
1. **Understand** — before signing/committing.
2. **Protect** — while committed (vault, dates, obligations, entitlements).
3. **Act** — when something goes wrong (timeline, evidence, grievance preparation, routing).
Distinguish: prepare a grievance / guide to a platform / integrate with a platform / submit on user's behalf. Only the first two are currently supportable.

## Domains in scope for investigation (not launch commitments)
Insurance; housing/tenancy; lending; subscriptions & consumer services; warranties & purchases.

## Product principles
- Never present probabilistic interpretation as legal determination; separate Document fact / Regulatory fact / Interpretation / Assumption / Unknown.
- Every material statement carries source and page; abstain when unsupported.
- Effective-date and jurisdiction aware; fail closed on state-dependent topics.
- Human confirmation before consequential actions; never say "submitted" unless confirmed.
- No commission or recovery-share conflicts by default; transparent funding.
- Privacy-minimising handling of sensitive documents.

## Non-goals
Generic PDF chatbot; legal advice or outcome guarantees; auto-submission to government portals (unsupported by evidence); an insurance-only, legal-only or grievance-only identity; implementation before Gate 5.

## Strategic hypotheses (all unvalidated)
H1 Shared extraction/provenance foundation serves multiple domains (credible for lower stack; upper stack is domain-specific).
H2 Lifecycle (Understand→Protect→Act) is useful to consumers as one journey (no consumer evidence yet).
H3 Trust/provenance/reliability can differentiate against free general AI (untested).
H4 A paid moment exists (claim time is leading candidate; unproven).
H5 A personal vault has standalone value (no evidence; likely a by-product).

## Decisions established
Name, vision, pillars, principles, domain list, India-first, evidence standards, gated process.

## Decisions intentionally open
Initial opportunity (shortlist C1 claim-denial prep, C2 loan-KFS reconciliation); architecture option (A/B/C/D — evidence currently favours B with single-domain first experience); monetisation; launch states; human-review tier.

## Research findings affecting direction (with confidence)
- Insurance Ombudsman: ₹50L ceiling, 1-year limit, insurer-first, 3-month award, covers repudiation/delay/policy construction — **High** (primary).
- CIO FY24: 52,575 complaints, ~24% of disposals non-entertainable — **High**.
- Bank lending rules are explicit and checkable (KFS, prepayment, cooling-off ≥1 day) — **High** (banks).
- Third-party grievance submission is not authorised by the Ombudsman rules text; no public APIs found — **Medium**.
- Explainer tools are cheap and plentiful; general AI parity untested — **Low–Medium**.
- Earlier drift to health-insurance-only was a scope error; insurance was favoured by data availability.

## Evidence standards
Classes: verified primary / verified secondary / secondary unverified / inference / hypothesis / unknown. Confidence with reason. Primary official text required before any regulatory fact drives product logic. Contradictions shown, not silently resolved.

## Decision rules
Gate transitions need explicit approval. Evidence beats narrative. Insurance competes on the same framework as other domains. Negative evidence is recorded.

## Current stage
**Gate 1B — integrated discovery, incomplete** (no interviews, no hands-on competitor/AI tests, no benchmark, partial primary regulatory reads).

## Conditions for Gate 2
Primary reads D1–D4; competitor/general-AI head-to-head H1; ≥24 interviews plus concierge/WTP evidence; counsel view on Q-P1/Q-P4; explicit scoring review of C1 vs C2 (and any challenger from interviews).

---
## Version history
- **v1** (Gate 1B draft, 2026-11-20).
- **v2** (2026-11-20, follow-up round 1) — changes limited to verified evidence; vision, pillars, principles unchanged:
  - Findings: added RB-IOS 2026 (primary text, non-RBI host), IRDAI Health Master Circular (mirror), SBSR Act 2025 (s.45 unamended, life-only), IRDAI FY25 grievance data; added correction that submission-on-behalf is **sector-specific** (Insurance Ombudsman Rules do not clearly authorise agents; RBI scheme permits non-advocate authorised representatives) — the "Non-goals" line on auto-submission stands (portal automation terms still unknown).
  - Withdrawn: the "≈56% complainant-favourable" figure; the s.45/health-moratorium conflation.
  - Current stage: Gate 1B incomplete; stop rule and minimum evidence set proposed in Doc 12 (awaiting approval).
  - C1/C2 remain open candidates; no selection.
