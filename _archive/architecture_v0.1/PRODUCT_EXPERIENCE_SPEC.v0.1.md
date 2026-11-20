# PRODUCT_EXPERIENCE_SPEC.md

Status: PROPOSED design direction and experience specification · 2026-11-20. **Not implementation.** Built on unvalidated assumptions about users (consumer validation waived as a gate); screens are hypotheses to be tested when validation assets are activated.

## 1. Design intent
Clarity, trust, control, intelligence. Not a chat wrapper, not an enterprise dashboard. The product feels like a **careful reader beside you**: calm, precise, evidence-forward, never theatrical.
**Distinctive direction (TECHNICAL RECOMMENDATION):** "Annotated paper." Documents are the hero — pages stay visible with a margin-note layer, the way a good lawyer's friend annotates a contract. Warm paper-toned surfaces, ink-dark text, one restrained accent colour for *evidence* (marginalia highlight), and a separate, consistent colour-and-shape language for statement kinds (below). Typography: a readable serif for document text/quotes, a clean sans for interface; numerals tabular for amounts/dates; Indic script support (Devanagari first) with matched metrics.

## 2. Mental model and information architecture
User's mental model: *"my stuff, what it says, what's coming, what to do."*
Primary navigation (4): **Home** (what needs attention) · **Agreements** (documents grouped into agreements; Understand) · **Commitments** (Protect: timeline/calendar) · **Cases** (Act). Persistent: **Ask** (document-grounded questions scoped to an agreement), **Search**, **Account/Privacy**. Pillars appear as *stages on each agreement* — "Before you sign · While you're committed · If something goes wrong" — so the lifecycle is one coherent object, not three tools.

## 3. The five-kind language (core UI contract)
| Kind | Meaning | Visual | Label | Click shows |
|---|---|---|---|---|
| Document says | verified quote | serif quote block with page tag, margin rule in ink | "Says (p.12)" | page with highlighted span |
| Extracted | value read from document, verified | monospaced/tabular chip with small anchor icon | "Read from document" | anchor + extraction confidence |
| Rule indicates | external regulatory text | bracketed card, authority + date | "Rule: RBI, effective 1 Jan 2026 · official/mirror/secondary" | provision text, source, retrieved date, verification status |
| Our interpretation | inference | italic with dotted underline | "Our reading" + confidence | supporting statements; "this is not legal advice" |
| Unknown | missing/insufficient | hatched empty card | "We can't tell" | what's missing + how to find out |
Assumption (user-supplied or defaulted) = outlined chip "You told us". **Never mix kinds in one sentence block**; a block has one kind. Colour is never the only signal (icon + label + shape).

## 4. Key surfaces
**Agreement overview:** header (type, parties, document family with reasons), "Key terms" grid (each cell shows kind + source tap), "Watch-outs" (fees, penalties, lock-ins, renewal/cancellation, exclusions) each with quote + plain-language gloss, "Unknowns" list, "Commitments found" (candidates awaiting confirm), related rules.
**Document workspace:** two-pane — page viewer (zoom, search, thumbnails) + margin panel. Clicking any statement scrolls and highlights its anchor; clicking a highlighted span shows all statements built on it. OCR low-confidence regions hatched.
**Clause explanation:** clause text (verbatim) → plain-language gloss (interpretation) → what it means in practice for scenarios the user enters (assumptions visible) → related clauses/cross-references → rule indicators.
**Ask:** scoped to an agreement; answer blocks each tagged by kind with citations; **abstention is a designed state** ("We can't answer this from your documents. Missing: policy start date. Add it or upload the schedule.").
**Compare:** side-by-side table of aligned terms with differences highlighted and per-cell provenance; unaligned/missing terms shown as Unknown, not blank.
**Commitments:** timeline + list; candidate vs confirmed visually distinct (dashed vs solid); confirming asks only for the trigger date/amount to be checked; regulatory deadlines show rule source and "confirm start date".
**Case workspace:** left timeline (events with certainty badges: documented vs your recollection), centre evidence (attach from vault/photos), right "relevant provisions" and checklist of missing evidence; draft complaint with per-sentence kind chips; escalation ladder shows conditions and windows; **status of any external step is only what the user reports** ("Filed? Add reference number").
**Action preparation:** three clearly named tiers — *Guide me* · *Prepare for me* · *(Connect — not available)*; external steps show "You submit this yourself on the official site" + copy/download pack.

## 5. States
**Empty:** explain with an example (synthetic sample agreement they can open) rather than blank. **Processing:** stage-wise progress (Received → Reading pages → Finding clauses → Checking sources); results appear progressively (document viewer usable first). **Extraction failure:** say which pages failed and why (unreadable scan, password-protected), offer retake tips/upload again/mark pages manually; never show partial output as complete. **Uncertainty:** confidence expressed in words + icon (not percentages alone). **Abstention:** see above. **Conflict:** "These two documents disagree (clause 4 vs endorsement 2)" with both quotes, no auto-resolution. **Quarantine:** "This file was blocked for safety." **Prompt-injection flag:** banner explained in §SECURITY.

## 6. Mobile and responsive
Mobile-first for capture (camera → crop → page order → quality check) and for Commitments/Home; document workspace collapses to single pane with a bottom sheet for margin notes; tap targets ≥44 px; offline-tolerant upload queue; slow-network aware (progress, resumable upload).

## 7. Accessibility
WCAG 2.2 AA target; keyboard-complete; screen-reader order = document order; kind conveyed by text + icon; reduced motion respected; contrast tokens verified; Indic fonts and line-height; plain-language reading level target; localisation architecture from day one (English first; Hindi next; others later) [ASSUMPTION on language priority — unvalidated].

## 8. Language and safety copy rules
Say "appears inconsistent with…", "your policy says…", "the rule indicates… (effective…)" — never "illegal", "wrongful", "you will win", "guaranteed". Always separate *what the document says* from *what a rule says* from *what we infer*. Every regulatory statement shows its verification status; mirror/secondary sources carry "not verified against the official source". Persistent, non-nagging "information, not legal advice" note and "talk to a professional" triggers for high-stakes/uncertain cases. No logos or wording implying government/regulator affiliation. No steering to products, no commissions, no recovery-share pricing.

## 9. Interaction and motion
Purposeful, short (≤200 ms), used to **maintain spatial continuity** (page↔margin note linking, anchor highlight sweeps), never to decorate. Hover/focus reveal source; "show your work" is one tap away everywhere. Skeletons mirror final layout. No auto-playing animation on sensitive screens.

## 10. Distinctive-but-safe details (candidate signature elements)
Margin-note layer with evidence "threads" linking statement→quote; the "Unknown" hatched card as a visible, respected state; a timeline "ledger" for commitments; subtle paper texture/grain on document surfaces only; a restrained **sealed-stamp** motif for *verified against official source* vs *mirror/secondary* (clearly different).

## 11. UX risks / hypotheses to test later [ASSUMPTION]
Users will accept an explicit "we can't tell"; users will tolerate upload effort; the lifecycle framing resonates; five-kind labels do not overwhelm; kind chips improve trust rather than adding noise. All unvalidated.
