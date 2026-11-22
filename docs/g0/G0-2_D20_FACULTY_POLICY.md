# G0-2 — D-20 faculty AI-assistance policy: outstanding-information checklist

- **Prepared:** 2026-11-20. **Status: OPEN.** Nothing here states or infers the institution's policy.
- **Sources:** DECISION_LOG D-20 and D-21 clause 8; CIA_REQUIREMENTS_MATRIX sections 0 and 1 (missing-information
  list); OPUS_BUILD_HANDOVER section 0 rule 2.
- **Owner action:** review the draft request in section 4, edit it, and send it yourself. **It has not been sent.**

## 1. What is known (from the documents, not assumed)

- The faculty guide (`CIA3_Advanced_Algorithm_Project_Guidelines_2026-27.pdf`, as summarised in the CIA matrix)
  requires "your own Java implementations" of the assessed algorithms and an individual viva. **It says nothing
  about AI assistance.**
- The owner-stated rule in force (D-20): no AI-generated assessed algorithm bodies, tests, baselines, benchmarks,
  correctness or complexity analysis, report or viva material until the faculty's **written** policy exists **and**
  the owner records it in DECISION_LOG.
- Track C (C0–C3) is not authorised (D-21 clause 4).

## 2. Inventory of AI-assisted work so far (facts, for any future disclosure)

| Category | What exists | Authored by | Assessed? |
|---|---|---|---|
| **Architecture and planning documents** | The root governing documents (v0.2.1), including the CIA matrix and its algorithm specifications A1–A5 (problem statements, complexity targets, benchmark plans) | Prepared in AI-assisted planning sessions (the handover header says "Prepared by Claude Sonnet (planning session)"), under owner direction | Not code. **But the CIA matrix's algorithm specifications and benchmark plans overlap with what a student proposal or report would contain.** Ask the faculty (Q5) |
| **Platform infrastructure** | Gradle build, build-logic plugins, CI, Compose stack, scripts, ADRs, runbooks, the Spring Boot app shell, platform tests | AI assistant, under owner direction | No |
| **SPI interfaces** | `core-api`: `QuoteVerifier`, `ApproximateMembership`, `ReminderIndex`, `DocumentLinker`, their DTOs and Javadoc contracts (ADR-0004) | AI assistant | Not assessed code, **but students would implement assessed algorithms against these contracts** (Q3) |
| **CIA placeholder** | `cia/README.md` only | AI assistant | No |
| **Assessed implementation work** | **None exists.** No algorithm body, test, baseline, benchmark, analysis, report or viva material | — | — |

## 3. Outstanding information (CIA matrix section 1 "MISSING", restated as a checklist)

| # | Item | Needed for | Status |
|---|---|---|---|
| F1 | **Written faculty policy on AI-assisted work**: what "your own implementations" permits, for code, tests, benchmarks, analysis and the report | D-20; any Track C authorship model | OPEN |
| F2 | Required **disclosure** form, wording and placement, if any | All AI-assisted material in the submission | OPEN |
| F3 | Whether students may build on AI-authored **SPI interfaces and infrastructure** in the same repository | C1 (students implement behind the SPIs) | OPEN |
| F4 | Official **CO1–CO5 definitions** and the authoritative **unit-to-CO mapping** (the guide only illustrates U3→CO2, U4→CO3, U5→CO4, U6→CO5) | Traceability matrix; the CO5 claim | OPEN |
| F5 | Actual **dates** (proposal, design review, prototype, benchmarking, final); the guide gives weeks 11–15 only | Track C schedule | OPEN |
| F6 | **Team composition and size** (2–3 required; individual work needs approval) | Algorithm ownership for the viva | OPEN |
| F7 | **Scope approval**: is a self-contained slice of a larger product acceptable? | C0 sign-off | OPEN |
| F8 | Full syllabus beyond the technique table | Technique eligibility | OPEN |
| F9 | Whether JUnit/JMH are expected or free choices; report format (5–8 pages) | C1–C3 | OPEN |

**Work that must remain entirely student-authored**, regardless of F3 (D-20, CIA matrix section 0), until the
written policy says otherwise:
- assessed algorithm bodies (A1 KMP and suffix array/LCP; A2 Bloom filter including its hash function; A3 heap;
  A4 DSU; optional A5 matching);
- their tests, reference baselines, benchmarks and benchmark analysis;
- correctness and complexity write-ups, the report text, and viva preparation.

**Not decided here, and not to be drafted yet:** any disclosure statement. It depends on F1 and F2.

## 4. Draft clarification request (for the owner to review and send — NOT SENT)

> **Subject:** CIA-3 Advanced Algorithms project — clarification on AI-assistance policy and scope
>
> Dear [faculty name],
>
> Our team is preparing the CIA-3 project proposal. Before we start implementation, we would be grateful for
> written guidance on the following points:
>
> 1. **AI assistance.** The guidelines require our own Java implementations of the assessed algorithms. May AI
>    tools be used at all, and if so, for which artefacts: algorithm code, tests, benchmarks, analysis, the
>    report, or none of these?
> 2. **Disclosure.** If any AI assistance is permitted, how should it be disclosed (form, wording, placement)?
> 3. **Supporting infrastructure.** Our project sits inside a larger application whose build setup, test harness
>    and Java interfaces (the method signatures our algorithms would implement) were written with AI assistance.
>    The assessed algorithm implementations, their tests and benchmarks would be written entirely by the students.
>    Is that arrangement acceptable, and must the interfaces be disclosed or written by us instead?
> 4. **Planning documents.** Our design documents, including the algorithm specifications and benchmark plans,
>    were prepared with AI assistance. May we use them as input to our proposal, and what must we disclose?
> 5. **Scope.** Is a self-contained, independently runnable slice of a larger product acceptable as the CIA
>    project?
> 6. **Course outcomes.** Could you share the official CO1–CO5 definitions and the unit-to-CO mapping?
> 7. **Dates and team.** Could you confirm the submission and review dates, and the permitted team size and
>    composition?
>
> Thank you,
> [names]

*Note for the owner:* adjust names, the course code and the team details. The request deliberately describes the
facts in section 2 neutrally, so the faculty can answer accurately.

## 5. What happens after the answer

1. The owner records the written policy in DECISION_LOG (new D-entry) and attaches or links the faculty's text.
2. Track C authorisation, if granted, follows that policy exactly; the CIA matrix section 0 table is updated.
3. Any disclosure text is drafted only then, to the faculty's required form.
4. **P1 (Track P) does not depend on this answer.**
