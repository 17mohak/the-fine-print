# ADR-0004: What `core-api` contains in P0

- **Status:** Accepted (engineering) — owner review at G0 (interface shapes also bind Track C; disclose under D-20)
- **Date:** 2026-11-20
- **Phase / authorisation:** Track P, P0 (D-21: "`core-api` SPI interfaces/DTOs (no logic)")
- **Decision owner:** Engineering; OWNER/FACULTY review of the SPI shapes before any Track C work
- **Related:** ARCHITECTURE_OVERVIEW section 2; SYSTEM_DESIGN sections 2.3, 4, 5; CIA_REQUIREMENTS_MATRIX section 0 and A1–A4

## Context
The roadmap lists `core-api` as "SPIs: `QuoteVerifier`, `ApproximateMembership`, `ReminderIndex`,
`DocumentLinker`, DTOs". Only `QuoteVerifier.find(l1, normalisedQuote) → List<Span>` has a specified signature.
Full `Assertion`/`EvidenceAnchor` records depend on the P1 provenance schema (encryption, key versions, anchor
lifecycle), which may not be designed under P0. The SPI shapes are also the contract that student-authored
assessed implementations would target, so they affect Track C.

## Decision
`core-api` contains only:
- the four SPIs (`com.thefineprint.core.api.spi`), whose Javadoc contracts are derived from the governing
  documents:
  - `QuoteVerifier.find` returns all exact (including overlapping) code-point occurrences in ascending order.
    Normalisation, grapheme alignment and scope rules belong to the caller (SYSTEM_DESIGN 2.3, 5.3).
  - `ApproximateMembership.add` / `mightContain` over `byte[]`: no false negatives; `true` means
    "possibly present" and must be confirmed exactly.
  - `ReminderIndex.schedule` (insert or reschedule) / `cancel` / `pollDue(now, limit)`: ordered by `fireAt`,
    then by `commitmentId`; confirmed commitments only; tenant dispatch is outside the SPI.
  - `DocumentLinker.group(documents, edges)`: returns connected components together with their reason edges;
    a reason code never carries an identifier value.
- the data types those SPIs need: `Span`, `ScheduledReminder`, `DocumentEdge`, `DocumentGroup`.
- two vocabulary enums whose values the approved specification already fixes: `StatementKind` (six kinds) and
  `MatchClass`.

Records validate structural invariants only (non-null, non-negative, non-empty) and make defensive copies.
There is no other behaviour, and no implementation of any SPI exists.

**Deferred to P1:** `Assertion`, `EvidenceAnchor`, `RegulatoryCitation`, `Commitment` records and any
tenant-context type.

## Options considered
| Option | Pros | Cons |
|---|---|---|
| SPIs + required types + spec-fixed enums (chosen) | Minimal; nothing speculative | P1 adds more types |
| Also full Assertion/Anchor DTOs | Matches the roadmap wording more literally | Pre-empts the P1 schema (encrypted fields, key versions), so speculative |
| SPIs only | Smallest | `QuoteVerifier` needs `Span` anyway |

## Consequences
- Students, if authorised, implement against these interfaces. Interface authorship is disclosed here
  (CIA_REQUIREMENTS_MATRIX section 0: "SPI interfaces … allowed … to be disclosed in the report").
- Changing an SPI after Track C begins affects student work, so freeze the shapes at GC.

## Reversibility and revisit triggers
High until Track C or P1 code exists. Revisit at G0, and when P1 designs the provenance schema.

## Deviation from the approved architecture?
No.
