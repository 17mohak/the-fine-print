# ADR-0001: Decision-record conventions

- **Status:** Accepted (engineering) — owner review at G0
- **Date:** 2026-11-20
- **Phase / authorisation:** Track P, P0 (D-21: "ADR template")
- **Decision owner:** Engineering
- **Related:** DECISION_LOG.md; OPUS_BUILD_HANDOVER sections 24–25; TECHNOLOGY_DECISIONS section A

## Context
The handover requires "ADR for every non-trivial decision" and forbids silent architecture changes. Owner,
faculty, legal and security decisions are already recorded in `DECISION_LOG.md` (D-numbers). Engineering
decisions made inside the approved architecture need a lighter, durable record.

## Decision
1. **Two registers, no overlap.**
   - `DECISION_LOG.md` (root) stays the authoritative record of owner decisions and authorisations
     (D-numbers). ADRs never override it.
   - `docs/adr/NNNN-kebab-title.md` records engineering decisions within the approved architecture, using
     `0000-template.md`.
2. **Numbering.** Four digits, sequential, never reused. `0000` is the template.
3. **Status values.**
   - *Proposed:* awaiting a decision-holder; not in effect if it is a deviation.
   - *Accepted (engineering):* within the engineer's decision rights, subject to review at the named gate.
   - *Accepted:* confirmed by the owner, with a DECISION_LOG reference.
   - *Superseded by ADR-XXXX.*
   - *Rejected.*
4. **Decision rights.** Anything that TECHNOLOGY_DECISIONS section A or handover section 24 marks
   OWNER/FACULTY/LEGAL/SEC can only be *Proposed* in an ADR. It becomes *Accepted* only when the decision-holder's
   answer is recorded in DECISION_LOG.
5. **Deviations.** Any change to the approved v0.2.1 architecture (new module, boundary, service or datastore;
   a different product) must say "Deviation: yes" and is not in effect until owner approval is recorded.
6. **Immutability.** Accepted ADRs are not rewritten. A change of mind is a new ADR that supersedes the old one.
   Typo fixes are allowed.
7. **Evidence.** Facts carry how and when they were verified, using the governing documents' labels
   ([VERIFIED FACT], [ASSUMPTION], [OPEN QUESTION]…).

## Consequences
Two files to consult, but owner authority stays unambiguous. Reviewers can see at a glance which decisions
still wait on someone else (status *Proposed*).

## Reversibility and revisit triggers
High. Revisit if the team adopts a different ADR tool.

## Deviation from the approved architecture?
No.
