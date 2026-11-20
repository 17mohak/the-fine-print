# G0-SEC — security-specialist review handover package

- **Prepared:** 2026-11-20. **Status: NOT STARTED.** No reviewer has been engaged; the owner is to engage one
  (RESEARCH_BACKLOG A-13).
- **Why it exists:** D-21 clause 6, IMPLEMENTATION_ROADMAP gate G0-SEC, SECURITY_AND_PRIVACY section 9a. A
  specialist must review three designs **before P1 implements them**.
- **What the review is not:** a production-security approval, a DPDP or other compliance opinion, or a
  penetration test. Those remain P7 and legal items (SECURITY_AND_PRIVACY section 10).

## 1. Scope of the review

| # | Design under review | Primary specification | Key decision records |
|---|---|---|---|
| S1 | Key hierarchy and envelope encryption, rotation, crypto-shredding, backups, deletion-ledger replay | SECURITY_AND_PRIVACY sections 3–4 | D-24, TECHNOLOGY_DECISIONS #10, #11 |
| S2 | Tenant context propagation, Postgres RLS (FORCE RLS, transaction-local `app.user_id`, roles), background-job isolation, the `system_scheduler` role | SECURITY_AND_PRIVACY section 6 | OPUS_BUILD_HANDOVER section 10 |
| S3 | Document-worker isolation: no internet or storage access, mTLS to the broker only, single-use job tokens, child-process sandbox, broker validation | ARCHITECTURE_OVERVIEW section 4; SYSTEM_DESIGN section 12 (broker protocol) | D-23, TECHNOLOGY_DECISIONS #16 |

Related context to read: the threat model (SECURITY_AND_PRIVACY section 2, T1–T21); the search residency design
(section 5, D-25), since the ephemeral index holds decrypted text; and the residual risks (section 11).

## 2. Current implementation state (so the reviewer knows what exists)

**None of S1–S3 is implemented.** P0 contains only scaffolding. What exists and is relevant:
- `core-api` SPIs only. No tenant-context type exists.
- ArchUnit rules that ban untrusted-file parsers in the Java core, and that confine outbound network clients to
  `modelgateway` and `integrations` (`architecture-tests/`). These enforce part of S3's "no parsing in core"
  principle.
- The dev stack contains an **inert** worker stub (`network_mode: none`, read-only, uid 65534, no capabilities).
  It is a placeholder, not the S3 design.
- Postgres has no schema, roles or RLS. Keycloak is dev-only. SeaweedFS is a dev-only S3 stub with no encryption.
- Evidence: `docs/evidence/p0/` (including `dev-stack-inspection.txt`).

## 3. Questions for the reviewer

**S1 — Cryptography and keys**
1. Is the hierarchy (Root KEK in KMS → per-user UMK → per-object DEK, plus HKDF field and blind-index keys) sound
   for per-user isolation and crypto-shredding?
2. Review the AES-256-GCM nonce strategy (random 96-bit with per-key limits), the AAD binding
   `(user_id, object_id, key_version, purpose)`, and the use limits per key.
3. Is HMAC-SHA256 under a per-user key an adequate blind index for dedup and `canonical_hash`? What are the
   leakage and confirmation-attack considerations?
4. Rotation: is online UMK re-wrap without data re-encryption, with a versioned field-key migration, adequate?
5. Deletion: are crypto-shredding plus restore-time deletion-ledger replay sufficient? How should the backup
   residual window be handled?
6. What KMS/HSM properties are required, independent of product (custody, audit, resurrection prevention)? The
   product choice is OWNER + LEGAL.
7. Which vetted library should be used (Tink or JCA), with which configuration constraints?
8. What are the accepted residual risks: operators with key-service access (T12/T18) and plaintext in memory (T21)?

**S2 — Tenant context and RLS**
1. Is the RLS policy pattern `user_id = nullif(current_setting('app.user_id', true), '')::uuid` with FORCE RLS
   and a non-owner role safe under connection pooling (transaction-local `set_config`; PgBouncer in transaction
   mode)?
2. Are the "refuse transaction without context", explicit-parameter (no thread-local) and async-handoff rules
   sufficient? What misuse cases are missing?
3. Is the `system_scheduler` role design (a column-limited view, then per-reminder tenant switch) correct?
4. What should the required CI test matrix contain (two-user matrix, pool-leak test, schema lint, superuser and
   BYPASSRLS startup refusal)? What is missing?

**S3 — Worker isolation**
1. Is child-process sandboxing with seccomp/AppArmor, cgroups, a no-network namespace per invocation, a read-only
   root and per-job tmpfs adequate, compared with ephemeral containers per job (deferred)?
2. Broker protocol: mTLS identity plus a single-use, job-bound, short-lived token. Are there replay or
   confused-deputy risks? What validation must the broker enforce?
3. Is the residency of decrypted bytes in broker and worker memory acceptable, and what mitigations apply?
4. What tests must prove isolation (a network-egress test, a resource-exhaustion test, malformed-output fuzzing)?

## 4. Deliverables requested from the reviewer

1. Findings per design (S1–S3), each with severity and a recommended change.
2. Explicit sign-off, or the conditions for one, per design, so that P1 may implement it.
3. Required tests and evidence that P1 must produce for each control.
4. The residual risks they consider acceptable, with rationale, for the owner to accept or reject.
5. Any change that would alter the approved architecture. Each needs an ADR and owner approval in DECISION_LOG.

## 5. Process after the review

1. Record the findings in DECISION_LOG (a new D-entry) and link the report.
2. The owner addresses each finding, or consciously accepts it.
3. Only then may P1 be authorised (its own recorded authorisation), and the P1 implementation follows the reviewed
   designs.
4. P7 still requires an independent security review and penetration test before any real users or real documents.

## 6. Constraints the reviewer should know

- **Synthetic data only.** No real personal documents in any environment (D-21 clause 7).
- **Undecided items** (owner, legal or security): KMS/HSM product, hosting region and data residency, IdP, LLM
  providers, retention periods.
- **Private repository:** https://github.com/17mohak/the-fine-print. The reviewer needs read access, granted by
  the owner.
