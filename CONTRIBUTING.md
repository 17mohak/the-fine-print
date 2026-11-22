# Contributing to THE FINE PRINT

## Scope and authorisation

Only work authorised in [`DECISION_LOG.md`](DECISION_LOG.md) may be implemented. As of D-21 (2026-11-20), that is
**Track P, P0 only**. P1–P7 and Track C (C0–C3) each need their own recorded authorisation.

- Do not implement encryption, key management, RLS or tenant context, the worker or broker, canonicalisation or
  provenance logic, search indexes, production SPI adapters, or any assessed algorithm, unless it has been
  authorised.
- Nothing goes into `cia/` except placeholder documentation ([`cia/README.md`](cia/README.md)).
- **No real personal documents or sensitive data, anywhere.** Synthetic data only.
- Decisions follow [ADR-0001](docs/adr/0001-decision-record-conventions.md). OWNER, FACULTY, LEGAL and SEC
  decisions are proposed, never assumed.

## Workflow

- Branch from `main`. Use small, focused commits with imperative subjects.
- Run `./gradlew build` before pushing. CI must be green.
- Dependency changes follow [docs/runbooks/local-setup.md](docs/runbooks/local-setup.md#updating-dependencies).

## Commit attribution

Ownership and authorship belong to the human project owner and authorised human contributors. AI tools are
development assistants, not git co-authors ([ADR-0008](docs/adr/0008-commit-attribution.md)).

1. Every commit uses the **actual human contributor's** configured git identity (`user.name`, `user.email`).
2. **No AI co-author trailers.** That means no `Co-authored-by: Claude…`, `Co-authored-by: Anthropic…`, or any
   `noreply@anthropic.com` trailer or equivalent.
3. Do not configure git with an AI identity or an AI-branded e-mail address.
4. Do not add an AI tool as author, contributor, maintainer or collaborator in README credits, package metadata
   or ownership declarations.
5. Never rewrite existing human attribution.
6. **Academic disclosure** of AI assistance follows the actual faculty policy, which is still unconfirmed (D-20).
   This repository does not yet state a disclosure position. Do not invent one.

Enforcement: CI job `attribution` (`ops/scripts/check-commit-attribution.sh`) and an optional local
`commit-msg` hook (`git config core.hooksPath ops/git-hooks`).
