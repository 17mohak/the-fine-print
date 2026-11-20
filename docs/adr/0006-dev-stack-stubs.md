# ADR-0006: P0 development stack and stubs

- **Status:** Accepted (engineering) — owner review at G0. **Not yet run** (Docker engine unavailable during P0)
- **Date:** 2026-11-20
- **Phase / authorisation:** Track P, P0 (D-21: "Docker Compose dev stack with stubs")
- **Decision owner:** Engineering
- **Related:** TECHNOLOGY_DECISIONS #13, #22; OPUS_BUILD_HANDOVER section 19; ARCHITECTURE_OVERVIEW section 4; ADR-0005

## Context
P0 needs Postgres, an object store, a development IdP and a worker stub, but must not implement RLS, tenant
context, encryption, OIDC integration, the worker or the broker. Data must be synthetic.

## Decision
`ops/compose/compose.yaml` (project `fineprint-dev`):
- **postgres**: stock image with no init scripts, no roles beyond the bootstrap user, no RLS (P1).
- **object-store**: SeaweedFS S3 (ADR-0005), with a dev-only identity file.
- **idp**: Keycloak `start-dev` importing realm `fineprint-dev`, containing:
  - one public client `fineprint-web-dev` (authorisation code + PKCE S256; direct-access grants off);
  - one synthetic user (`synthetic.user@example.test`).

  The application is not wired to it; OIDC integration is P1.
- **doc-worker-stub**: an inert busybox container. It prints a banner and sleeps: `network_mode: none`,
  read-only root, tmpfs, non-root user, all capabilities dropped, `no-new-privileges`, pids and memory caps.
  These are conservative defaults for an inert container and are **not** the P1 worker-isolation design, which
  needs G0-SEC.
- Every published port binds to `127.0.0.1`. Every image is pinned by digest.
- **Development-only values** sit in `ops/compose/.env.example` (copied to a git-ignored `.env`), and in the
  realm and SeaweedFS files. Each value is labelled `dev-only-…-not-a-secret` and allow-listed by path in
  `.gitleaks.toml`.
- Compose requires its variables (`${VAR:?}`), so a missing `.env` fails loudly. Verified with
  `docker compose config`.
- `ops/scripts/verify-dev-stack.sh` checks each service's health, `pg_isready`, the absence of an application
  schema, an S3 response, OIDC discovery, that the worker stub has no network, the stub banner, and
  loopback-only ports. CI job `compose-smoke` runs the same script.

## Consequences
- The stack carries no protection beyond loopback binding. It must never hold real documents.
- **Not verified:** none of the containers has been started on this machine. The healthchecks (notably
  SeaweedFS `wget` and Keycloak `/dev/tcp`) and the SeaweedFS command flags still need proof on a running
  engine; it is outstanding.

## Reversibility and revisit triggers
High. Revisit at P1, when real integration (OIDC, Flyway, object storage) begins.

## Deviation from the approved architecture?
No (see ADR-0005 for the object-store product).
