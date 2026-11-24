# THE FINE PRINT

*Understand what you agree to. Know what you're entitled to. Know what to do next.*
An India-first personal consumer-intelligence platform ([PROJECT_FOUNDATION.md](../PROJECT_FOUNDATION.md)).

## Status

| Item | Status |
|---|---|
| Architecture | **APPROVED v0.2.1 direction** (D-21, 2026-11-20), subject to unresolved decisions, limitations and review gates |
| Implementation | **Track P, P0** (repository, build, CI, SPIs, dev stack). **Track C (C1–C2) authorised by D-31** in isolated `cia-*` modules. P1–P7 **not authorised** |
| This code base | **P0 scaffolding.** No product functionality. Not secure, not production-ready, no compliance claim. **No real documents or personal data.** |
| P0 acceptance | All criteria demonstrated locally and in CI ([verification record](evidence/p0/VERIFICATION.md)); **closes at G0** with owner confirmation |
| Next gates | **G0**: decisions recorded in D-30 and D-32 (pins retained; SeaweedFS for development only); the faculty-policy item (G0-2) is resolved by D-31 (owner statement; no written faculty evidence held) ([briefs](g0/G0_DECISION_BRIEFS.md)). **G0-SEC**: security-specialist review before P1 ([package](g0/G0-SEC_HANDOVER.md)) |

## Repository map

```
core-api/            SPI interfaces + DTOs only (QuoteVerifier, ApproximateMembership, ReminderIndex, DocumentLinker)
platform/app/        Spring Boot composition root (health endpoint only in P0)
platform/<module>/   modular-monolith modules: empty placeholders in P0
platform/domains/    domain modules: empty placeholders; must not depend on each other
api/                 OpenAPI 3.1 skeleton (/api/v1) + validation test
architecture-tests/  ArchUnit boundary rules + fixture proofs
build-logic/         Gradle conventions + verifyBoundaries plugin (+ TestKit tests)
cia/                 isolated academic track (D-31): cia-algorithms (KMP, Bloom), cia-bench, cia-demo
doc-worker/ web/ db/ data/   placeholders for later phases
ops/compose/         development stack (Postgres, S3 stub, dev IdP, inert worker stub)
ops/scripts/         stack verification, commit-attribution check
docs/adr/            architecture decision records
docs/runbooks/       local setup
docs/evidence/p0/    P0 verification record
docs/g0/             G0 decision briefs and G0-SEC security-review handover
*.md (root)          governing documents (v0.2.1) — authoritative specifications
Gate1B/, WS0*.md     historical research (unchanged); _archive/ = superseded v0.1
```

## Quick start

```bash
./gradlew build                 # JDK 21 required; Gradle is fetched by the wrapper
```

Full instructions, including the Docker development stack:
[docs/runbooks/local-setup.md](runbooks/local-setup.md).

## Governing documents

[PROJECT_FOUNDATION](../PROJECT_FOUNDATION.md) · [ARCHITECTURE_OVERVIEW](../ARCHITECTURE_OVERVIEW.md) ·
[SYSTEM_DESIGN](../SYSTEM_DESIGN.md) · [TECHNOLOGY_DECISIONS](../TECHNOLOGY_DECISIONS.md) ·
[CIA_REQUIREMENTS_MATRIX](../CIA_REQUIREMENTS_MATRIX.md) · [SECURITY_AND_PRIVACY](../SECURITY_AND_PRIVACY.md) ·
[PRODUCT_EXPERIENCE_SPEC](../PRODUCT_EXPERIENCE_SPEC.md) · [IMPLEMENTATION_ROADMAP](../IMPLEMENTATION_ROADMAP.md) ·
[RESEARCH_BACKLOG](../RESEARCH_BACKLOG.md) · [DECISION_LOG](../DECISION_LOG.md) (authoritative for authorisations) ·
[OPUS_BUILD_HANDOVER](../OPUS_BUILD_HANDOVER.md)

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md), including the commit-attribution rules.
