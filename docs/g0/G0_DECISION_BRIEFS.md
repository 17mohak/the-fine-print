# Gate G0 — decision briefs for the owner

- **Prepared:** 2026-11-20, at the end of Track P P0 (D-21). **Decision holder: the owner.** Nothing here is decided.
- **Purpose:** each brief states the decision, the facts with their sources, the options, and a recommendation.
  Record your answers in `DECISION_LOG.md`; the matching ADR statuses are then updated.
- **Technical state:** every P0 acceptance criterion is verified ([VERIFICATION.md](../evidence/p0/VERIFICATION.md)).
  P0 closes when G0-1 to G0-3 are answered. G0-SEC is a separate precondition for P1.

---

## G0-1 — Toolchain versions (ADR-0002, TECHNOLOGY_DECISIONS #2)

**Decision needed:** confirm the exact pinned versions. The only real choice is the **Spring Boot line**.

**Proposed pins (everything else is uncontested):**
- Java 21 LTS (`--release 21`)
- Gradle 9.9.2
- JUnit 6.1.3, AssertJ 3.27.7, ArchUnit 1.5.1, swagger-parser 2.1.49, CycloneDX plugin 3.6.1
- PostgreSQL 18.7 (19 is RC only), Keycloak 26.8.2, SeaweedFS 4.54 (see G0-3), busybox 1.37.0
- gitleaks 8.30.1, OSV-Scanner 2.7.1
- build-time Guava 33.7.2-jre (security constraint)

**Spring Boot facts (checked 2026-11-20):**

| | 4.2.0 (currently pinned) | 4.1.3 |
|---|---|---|
| Git tag / Maven Central | `v4.2.0`, published 2026-11-19 | `v4.1.3`, published 2026-11-19 |
| Milestone | closed 2026-11-17 | closed 2026-11-17 |
| spring.io project API | still shows `4.2.0-RC2` as the newest 4.2 entry and **4.1.2 as "current"** (metadata not yet updated; the 4.2.x generation is listed with initial date 2026-11-30) | listed as SNAPSHOT (same lag) |
| Maturity | a `.0` release, about one day old; no patch releases yet | 4th patch of a line released 2026-06-30 |
| OSS support ends (spring.io generations) | 2027-12-31 | 2027-07-31 |
| Brings | Spring Framework 7.1.0, JUnit 6.1.3, Micrometer 1.18, Logback 1.6 | Spring Framework 7.0.11, JUnit 6.0.3, Micrometer 1.17, Logback 1.5 |
| Effect on this repository | none (current state, all green) | Changes the catalogue (Boot 4.1.3, JUnit 6.0.3), the locks and the verification metadata; all checks re-run; about an hour of work |

**What depends on it in P0:** very little. The app is a health endpoint, so either line carries negligible risk now.
The choice matters for P1, which is security-critical.

**Recommendation:** **confirm 4.2.0 as the provisional baseline**, on one condition: before P1 starts, move to the
first 4.2.x patch release, or to 4.1.x if a 4.2 regression appears. Reasons:
- P1 cannot start before G0-SEC, so 4.2.x patches will exist by then.
- 4.2 has five more months of open-source support.
- Reverting is cheap.

*Alternative:* choose **4.1.3** now if you prefer the most mature line at all times. The cost is re-pinning, plus an
earlier forced upgrade (OSS support ends 2027-07-31).

**Also confirm:** the local JDK should be updated (Oracle 21.0.4 from July 2024 lacks security patches; CI already uses
current Temurin 21). Recommended, not blocking.

---

## G0-2 — Faculty AI-assistance policy path (D-20; D-21 clause 8; CIA_REQUIREMENTS_MATRIX section 0)

**Current documented state:**
- D-20 is **OPEN**.
- The CIA guide requires "your own Java implementations" and an individual viva, and it is **silent on AI
  assistance**.
- Rule in force: no AI-generated assessed algorithm bodies, tests, baselines, benchmarks, analysis, report or viva
  material until the faculty's **written** policy exists **and** the owner records it in DECISION_LOG.
- Track C (C0–C3) is not authorised.

**What P0 did that touches the boundary (for disclosure):**
- An AI assistant authored the `core-api` SPI interfaces (`QuoteVerifier`, `ApproximateMembership`, `ReminderIndex`,
  `DocumentLinker`) and their Javadoc contracts (ADR-0004). Students would implement against them.
- An AI assistant authored the `cia/README.md` placeholder.
- Nothing else in `cia/` exists.
- The CIA matrix already says SPI authorship must be **disclosed in the report**.

**Exactly what the owner must confirm at G0 (the "D-20 path"):**
1. **Who obtains the policy, and by when.** Will you request the faculty's written AI-assistance policy (and the other
   C0 inputs: CO1–CO5 definitions, unit-to-CO mapping, actual dates, team size, scope approval for the slice)?
2. **The interim rule until the policy arrives.** Confirm that the current rule stands: AI may build platform code
   and the infrastructure listed in CIA matrix section 0, and nothing assessed. This is the recommended default.
3. **SPI shapes.** Should the `core-api` SPI contracts (ADR-0004) be shown to the faculty as part of the scope
   approval, given that students would implement against interfaces they did not write?
4. **Disclosure.** Disclose AI assistance wherever the faculty policy requires it (owner directive). Confirm that no
   disclosure wording is drafted until that policy is known.

**Recommendation:**
- Answer (2) and (4) as above now, which needs no faculty input.
- For (1), request the written policy together with the C0 inputs in one message.
- For (3), include the SPI contracts in the scope-approval request.
- Track C stays blocked until the faculty's answer is recorded. **This does not block P1 planning**, because Track P
  never depends on Track C.

---

## G0-3 — Development object store: MinIO → SeaweedFS (ADR-0005; a deviation)

**Decision needed:** approve or reject SeaweedFS as the **development-only** S3-compatible stub. This does not choose
the production store, which remains OWNER + LEGAL (TECHNOLOGY_DECISIONS #9).

**Facts (verified 2026-11-20):**
- MinIO's Docker Hub repository returns HTTP 404.
- MinIO's GitHub repository `minio/minio` is **archived**; its last release is `RELEASE.2025-10-15`.
- SeaweedFS 4.54 is Apache-2.0 and actively released (2026-11-16). In the dev stack it:
  - starts healthy;
  - enforces S3 credentials (anonymous and wrong-secret requests get 403);
  - publishes only `127.0.0.1:8333`;
  - passes the CI smoke test.
- Nothing in the P0 code talks to it.

**Implications:**
- S3 edge-case behaviour differs from AWS S3 and MinIO.
- P1 storage integration tests must run against whatever store is finally chosen, and should not rely on SeaweedFS
  quirks.
- Changing the stub later touches only one Compose service and the verification script.

**Options:**
- (a) **Approve SeaweedFS for development.** Recommended.
- (b) Garage (AGPL; needs a bootstrap step).
- (c) Pin an unmaintained MinIO image or fork. Not recommended: an unpatched or unofficial supply chain.
- (d) LocalStack S3 (an emulator, with licensing and feature drift).

**Recommendation:** **(a)**. Record it in DECISION_LOG and mark ADR-0005 *Accepted*. Revisit at the production
hosting decision.

---

## G0-SEC — Security-specialist review (mandatory before P1)

This is not a G0 owner decision, but it is the **next blocking item**. A handover package is ready:
[G0-SEC_HANDOVER.md](G0-SEC_HANDOVER.md). The owner needs to engage a specialist (RESEARCH_BACKLOG A-13). The review
is not a production-security approval, and P1 may not implement encryption, RLS, tenant context or worker isolation
until its findings are recorded and addressed or consciously accepted.

---

## Summary of answers requested

| # | Question | Recommended answer |
|---|---|---|
| G0-1 | Spring Boot 4.2.0 or 4.1.3 (other pins as listed)? | 4.2.0 provisional; move to the first 4.2.x patch before P1 |
| G0-2 | D-20 path: interim rule, who requests the policy, SPI disclosure | Keep the interim rule; request the written policy and C0 inputs; include the SPIs in the scope request |
| G0-3 | Approve SeaweedFS as the dev-only S3 stub? | Approve (dev only) |
| G0-SEC | Engage a security specialist? | Yes, before any P1 authorisation |
| — | Merge PR #1 into `main`? | After G0-1 to G0-3 are recorded |
