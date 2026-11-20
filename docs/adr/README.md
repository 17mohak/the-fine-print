# Architecture decision records

Conventions: [ADR-0001](0001-decision-record-conventions.md). Template: [0000-template.md](0000-template.md).
Owner decisions and authorisations live in [`DECISION_LOG.md`](../../DECISION_LOG.md); ADRs never override it.

| ADR | Title | Status |
|---|---|---|
| [0001](0001-decision-record-conventions.md) | Decision-record conventions | Accepted (engineering), review at G0 |
| [0002](0002-p0-toolchain-and-versions.md) | P0 toolchain and exact versions | **Spring Boot line decided (D-30); other pins Proposed** |
| [0003](0003-module-layout-and-boundary-enforcement.md) | Module layout and mechanical boundary enforcement | Accepted (engineering), review at G0 |
| [0004](0004-core-api-p0-scope.md) | What `core-api` contains in P0 | Accepted (engineering), review at G0 |
| [0005](0005-dev-object-store-minio-unavailable.md) | Dev object store: MinIO unavailable; SeaweedFS proposed | **Proposed — owner decision at G0 (deviation)** |
| [0006](0006-dev-stack-stubs.md) | P0 development stack and stubs | Accepted (engineering); verified 2026-11-20 |
| [0007](0007-supply-chain-scanning-and-sbom.md) | Supply-chain controls, SBOM, scanning | Accepted (engineering), review at G0 |
| [0008](0008-commit-attribution.md) | Commit-attribution rules and enforcement | Accepted (owner directive) |
| [0009](0009-track-c-modules-and-benchmark-tooling.md) | Track C modules, benchmark tooling and demo stack | Accepted (engineering), under D-31 |
