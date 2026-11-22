# G0-SEC — Security-specialist review package

- **Revision 2:** 2026-11-20. Revision 1 is in git history at `7960257`; the changelog is at the end.
- **Review status: NOT STARTED. No reviewer engaged. No finding or sign-off exists.** The owner engages the
  reviewer (RESEARCH_BACKLOG A-13).
- **Mandate:** D-21 clause 6; IMPLEMENTATION_ROADMAP gate G0-SEC; SECURITY_AND_PRIVACY section 9a. A specialist
  reviews three designs **before P1 implements them**.
- **This review is not:** a production-security approval, a penetration test, or a DPDP or other legal opinion.
  Those remain P7 and LEGAL items (SECURITY_AND_PRIVACY section 10).

---

## 1. Executive summary

THE FINE PRINT is an India-first consumer platform that will hold people's agreements (insurance policies, loan
papers, tenancy agreements, subscriptions, warranties). It explains, tracks and helps users act on them, with
source-grounded provenance. That is highly sensitive personal, financial and health data.

The approved architecture (v0.2.1) is:
- a **Java modular monolith** (Spring Boot) with PostgreSQL (RLS) and an S3-compatible store holding
  **ciphertext only**;
- a **sandboxed Python worker** that alone parses untrusted files, reachable only through a Java **worker
  broker** over mTLS;
- a Java **model gateway** as the only path to language models.

**What we need from you:** a judgement on whether three designs are sound enough to implement in P1, and what
must change first:

| # | Design | Why it is security-critical |
|---|---|---|
| **S1** | Key hierarchy and envelope encryption, rotation, crypto-shredding, backups | It decides whether stolen databases or backups expose user documents, and whether deletion is real |
| **S2** | Tenant context and PostgreSQL RLS, including background jobs | It decides whether one user can ever see another user's data |
| **S3** | Document-worker isolation and the broker protocol | Hostile uploaded files are parsed here; a compromise must not reach other data |

**Nothing of S1–S3 is implemented** (P0 is scaffolding only). You are reviewing designs, so changes are cheap now.

## 2. System boundaries and trust boundaries

```
 [Browser SPA] --T1 HTTPS--> [fineprint-api: Java modular monolith]
                                |-- identity-access (OIDC; IdP undecided)
                                |-- crypto ----------T7----> [KMS/HSM: product undecided]
                                |-- documents / canonical / provenance / understand / commitments / cases
                                |-- workerbroker <==T2 mTLS== [doc-worker: Python sandbox, no internet, no storage]
                                |-- modelgateway ----T3----> [LLM provider(s): undecided]
                                |-- integrations ----T4----> [official portals: guided/assisted only]
                                |-- audit
                     [PostgreSQL: RLS, encrypted columns] [Object store: ciphertext only]  <--T6 backups
                     [Operators / staff] --T5--> (no standing content access)
```

| Boundary | Between | Primary concern | In G0-SEC scope? |
|---|---|---|---|
| T1 | Internet ↔ API | AuthN/Z, IDOR, input handling | Partly (S2 authorisation context) |
| T2 | Broker ↔ worker | Hostile bytes, sandbox escape, broker as chokepoint | **Yes (S3)** |
| T3 | Gateway ↔ model provider | Plaintext excerpts leave the system | No (P2; flagged) |
| T4 | Integrations ↔ portals | No automation; no credential storage | No |
| T5 | Staff/ops ↔ data | Insider access, break-glass | Partly (S1 key access) |
| T6 | Storage/backups | Ciphertext at rest, restore resurrection | **Yes (S1)** |
| T7 | App ↔ KMS | Key custody and use | **Yes (S1)** |

## 3. Data-flow inventory (P1+ design; architecture level)

| # | Flow | Data | Protection by design | Plaintext where |
|---|---|---|---|---|
| DF1 | Upload: browser → API | Original file | TLS. The API checks magic bytes and size only; no parsing in Java | API memory (request) |
| DF2 | API → object store | Original | Encrypted under a per-object DEK (AES-256-GCM), wrapped by the UMK; AAD-bound | API memory during encryption |
| DF3 | Broker → worker | Original bytes for one job | Decrypted in Java memory and streamed over mTLS; single-use job token; the worker gets an opaque job id, no user id | Broker and worker memory, worker tmpfs |
| DF4 | Worker → broker | Tokens, boxes, page images | Schema, size and bounds validated by the broker | Broker memory |
| DF5 | Java → DB/object store | L0/L1 text, edit maps, assertions, quotes | Encrypted blobs and columns (field keys via HKDF); ids, timestamps and statuses in plaintext | App memory |
| DF6 | Q&A retrieval | Decrypted L1 text | **Ephemeral in-memory index**, per user and agreement, TTL/LRU, never persisted (D-25) | **App heap for minutes** |
| DF7 | Gateway → LLM | Selected verified excerpts | Redaction profile, schema-constrained, no tools (P2) | Provider side (out of scope) |
| DF8 | Scheduler | Reminder ids and due dates | `system_scheduler` role reads a column-limited view; payloads encrypted | Due dates are plaintext metadata |
| DF9 | Backup/restore | Ciphertext + wrapped keys | Deletion-ledger replay before serving traffic | — |
| DF10 | Deletion | All of a user's objects and keys | Rows and objects deleted; UMK/DEKs destroyed (crypto-shredding); ledger entry | — |
| DF11 | Logs/metrics/traces | Operational data only | No content; scrubber; canary tests | Must never contain content |

## 4. Assets and sensitive-data categories

| Class | Examples | Sensitivity | Design protection |
|---|---|---|---|
| A1 Originals | Uploaded PDFs and images (policies, loan papers, medical bills) | Highest: may contain health, financial and identity data | Per-object DEK; ciphertext only at rest |
| A2 Derived text | OCR tokens, L0/L1 canonical text, edit maps, page images | Same as A1 | Per-object DEK |
| A3 Assertions | Statements, quotes, extracted values (amounts, dates) | High | Field keys (HKDF from UMK) |
| A4 Case evidence | Notes, events, drafts, attachments | High | Field keys / DEKs |
| A5 Keys | Root KEK (KMS), wrapped UMKs, wrapped DEKs, derived field and blind-index keys | Critical | Hierarchy (S1) |
| A6 Metadata | Ids, timestamps, statuses, sizes, counts, **commitment due dates** | Medium: inference risk accepted and documented | Plaintext, RLS-protected |
| A7 Identity | Account and contact data (through the IdP) | High | Encrypted columns; IdP holds the credentials |
| A8 Operational | Logs, metrics, audit events | Must contain no content | Scrubber; hash-chained audit |

**No real data exists anywhere** (D-21 clause 7). All environments are synthetic until P7 closes.

## 5. Threat scenarios (focused on S1–S3)

| # | Scenario | Design affected | Design response (to be judged) |
|---|---|---|---|
| TS1 | A database dump or backup is stolen | S1 | Ciphertext plus wrapped keys only; the KEK never leaves the KMS |
| TS2 | An attacker with app-server code execution and KMS permission | S1 | **Residual risk, accepted in the design**: active users' data is readable. Mitigated by KMS audit and anomaly alerts |
| TS3 | A deleted user's data is resurrected from a backup | S1 | Deletion-ledger replay before traffic; UMK blobs destroyed |
| TS4 | Nonce reuse or a key-wear-out under AES-GCM | S1 | Random 96-bit nonces with per-key limits (needs your judgement) |
| TS5 | A confirmation attack on content hashes (is document X in user Y's vault?) | S1 | Per-user HMAC blind indexes; no raw SHA-256 stored as queryable metadata |
| TS6 | A pooled connection leaks the previous tenant's `app.user_id` | S2 | Transaction-local `set_config`; pool-return test |
| TS7 | Missing context makes a query run as "no tenant", or as everyone | S2 | Unset setting ⇒ NULL ⇒ no rows; a transaction wrapper refuses to run without context |
| TS8 | A misconfigured role bypasses RLS (owner, superuser, BYPASSRLS) | S2 | Non-owner app role; startup refuses privileged roles; separate migration role |
| TS9 | A background job mixes tenants | S2 | One tenant per job; context rebuilt from the job row; separate scheduler role |
| TS10 | A hostile PDF exploits a parser, escapes the sandbox and pivots | S3 | Child process without network namespace, seccomp/AppArmor, cgroups, read-only root, tmpfs wipe, no credentials |
| TS11 | A compromised worker replays or steals job tokens to read other jobs | S3 | Single-use, job-bound, short-lived tokens plus mTLS identity |
| TS12 | A compromised worker returns malicious output (oversized, malformed, injected text) | S3 | Broker validates schema, sizes, coordinate bounds and text sanity; the Java side treats output as untrusted |
| TS13 | The broker as a high-value chokepoint (it holds decrypted bytes for many jobs) | S3 | Minimal surface, validation and memory hygiene (needs your judgement) |
| TS14 | Decrypted text lingers in heap dumps, swap or caches, including the ephemeral index | S1/S3 | No swap; dumps disabled or encrypted; TTL-bounded per-user caches; best-effort zeroisation (the JVM cannot guarantee erasure) |

## 6. Questions requiring specialist judgement

Revision 1 had 16 questions. They are kept (some merged), and gaps are added (**new**).

**S1 — Keys and encryption**
1. Is the hierarchy (Root KEK in KMS → per-user UMK → per-object DEK, plus HKDF field and blind-index keys) sound
   for per-user isolation and crypto-shredding? Is any level unnecessary or missing?
2. AES-256-GCM: is random 96-bit nonces with per-key usage limits acceptable, or should a deterministic or
   counter scheme, or AES-GCM-SIV, be used? What limits apply per DEK and per field key?
3. Is the AAD binding `(user_id, object_id, key_version, purpose)` complete? Should it include table and column
   for field encryption, and the document version?
4. Blind indexes: does HMAC-SHA256 under a per-user key for dedup and `canonical_hash` leak anything material?
   Should the dedup scope be narrowed?
5. Rotation: is online UMK re-wrap without data re-encryption, with versioned field-key migration, adequate?
   What triggers a mandatory re-encryption?
6. Deletion and backups: are crypto-shredding plus deletion-ledger replay at restore adequate? How should the
   backup residual window be bounded and disclosed?
7. What KMS/HSM properties are required, independent of product (custody separation, audit, prevention of key
   resurrection, regional constraints)? *The product is OWNER + LEGAL.*
8. Library: Tink or JCA? Which primitives and configurations should be mandated or banned?
9. **New:** UMK caching. What is an acceptable in-memory TTL for unwrapped UMKs and derived keys, and is
   zeroisation on expiry meaningful on the JVM?
10. **New:** should the hash-chained audit log be separately keyed or anchored (for example, periodic external
    anchoring) to resist an insider with database access?

**S2 — Tenant context and RLS**
11. Is the pattern `USING (user_id = nullif(current_setting('app.user_id', true), '')::uuid)` with FORCE RLS
    and a non-owner role safe under pooling (transaction-local `set_config`; PgBouncer transaction mode only)?
12. Are the explicit `TenantContext` parameter (no thread-locals in domain code), the
    "no transaction without context" wrapper and the async-handoff helper sufficient? Which misuse cases are
    missing?
13. Is the `system_scheduler` role (a column-limited view, then a per-item tenant switch) correct? Should the
    view live in a separate schema with its own grants?
14. What must the CI test matrix contain (a two-user matrix over every table, a pool-leak test, a schema lint
    that every `user_id` table has policies, startup refusal of privileged roles)? What is missing?
15. **New:** where do authorisation checks live: is RLS the only guard, or must service-layer owner checks also
    exist (defence in depth against RLS misconfiguration)? What is the minimum?
16. **New:** the ephemeral search index (D-25) is partitioned by `(userId, agreementId)` in heap. Are the
    tenant-partition and eviction rules adequate to prevent cross-tenant retrieval? It holds the largest volume
    of plaintext.

**S3 — Worker isolation and broker**
17. Is child-process sandboxing (seccomp/AppArmor, cgroups, no network namespace per invocation, read-only root,
    per-job tmpfs) adequate, or should ephemeral containers per job (deferred) be required before real documents?
18. Broker protocol: mTLS identity plus a single-use, job-bound, short-lived token. Are there replay,
    confused-deputy or token-theft risks? How should the token be bound (to the mTLS channel, to the job, by a
    time limit)?
19. Which validations must the broker enforce on worker output (schema, sizes, coordinate bounds, text sanity,
    page counts), and should it run in a separate process or service from the main API?
20. Is the residency of decrypted bytes in broker and worker memory acceptable? Which mitigations are mandatory
    (no swap, no core dumps, recycling after N jobs)?
21. **New:** how are the worker's mTLS identity and the broker's certificates issued, rotated and revoked in
    development and production?
22. **New:** what is the minimum isolation test evidence: an egress test (no route except the broker), a
    resource-exhaustion test, malformed-output fuzzing, and a sandbox-escape regression corpus?

**Cross-cutting (new)**
23. Which of S1–S3 must be in place before *any* real document can be handled, as opposed to before production
    launch? (Real documents are prohibited until P7 in any case.)
24. Is there any design element you would block outright as unsafe to implement?

*Redundancy removed:* the revision 1 S1 Q8 (residual risks) is now part of deliverable D4 rather than a separate
question.

## 7. Required evidence and test deliverables (what P1 must produce, subject to your review)

| Control | Evidence P1 must produce | Failure = |
|---|---|---|
| Envelope encryption | Unit and property tests: round trip; AAD mismatch rejected; wrong key rejected; nonce uniqueness under load | Any decrypt with the wrong AAD or key succeeds; a nonce repeats |
| Ciphertext at rest | A DB and object-store scan for planted canary plaintext after ingesting synthetic documents | A canary is found in any persisted store or log |
| Crypto-shredding | A deletion test: after deletion, objects, rows and wrapped keys are gone and the ledger entry exists | Any residual decryptable data |
| Restore replay | Restore a backup taken before a deletion; verify the ledger replay runs before traffic | A deleted user's data is readable after restore |
| Key rotation | Rotate the UMK and field keys; data stays readable; old versions retire | Unreadable data, or a retired key still in use |
| RLS | A two-user matrix over every user table; a pool-return test; a schema lint; refusal of privileged roles | Any cross-tenant row is visible or writable |
| Job isolation | A cross-tenant scheduler test; one tenant per job | A job touches more than one tenant |
| Ephemeral index | Two-user canaries; TTL/LRU eviction; no index on disk (filesystem scan) | A cross-user hit, or plaintext on disk |
| Worker egress | A network test: the worker reaches only the broker | Any other route |
| Worker resources | CPU, memory, pid and time limits enforced; recycle after N jobs | A limit is not enforced |
| Broker validation | Fuzzing malformed and oversized results | Invalid output accepted |
| Logging | A scrubber test with canary strings in all log paths | Content appears in logs or traces |

## 8. Sign-off criteria (proposed for the reviewer to accept or amend)

For each design S1, S2 and S3, the reviewer records one of the following:
- **Approved to implement as designed.**
- **Approved with conditions**: listed changes, each to be verified by the named evidence in section 7.
- **Not approved**: redesign required.

G0-SEC is satisfied for a design when its record is "approved" or "approved with conditions" **and** the owner
has recorded in DECISION_LOG that each condition and finding is addressed or consciously accepted. P1 may then
implement **only** the approved designs, and only after P1 has its own recorded authorisation.

## 9. Decisions the implementation agent must NOT make

| Decision | Holder |
|---|---|
| KMS/HSM product, key custody, region | OWNER + LEGAL + SEC |
| Any change to the key hierarchy, algorithms, AAD scheme or rotation model | SEC review + OWNER |
| Sandbox strength beyond the specification (for example ephemeral containers per job) | SEC |
| Any added worker egress, credential or provider call | SEC + OWNER |
| Any persisted or vault-wide search index, or searchable encryption | OWNER + SEC (ADR) |
| Accepting a residual risk the reviewer flags | OWNER |
| Retention periods, backup lifetime, legal holds | LEGAL + OWNER |
| Hosting provider, region, data residency | OWNER + LEGAL |
| IdP product and MFA policy | OWNER + SEC |
| LLM providers, data-sharing terms, redaction acceptability | OWNER + LEGAL |
| Use of any real document, even consented | OWNER (currently prohibited) |

## 10. Current implementation state (what exists to inspect)

- **None of S1–S3 is implemented.** P0 contains:
  - `core-api` SPIs only, with no tenant-context type;
  - ArchUnit rules that ban untrusted-file parsers in the Java core and confine outbound network clients to
    `modelgateway` and `integrations`;
  - a dev stack whose worker stub is **inert** (`network_mode: none`, read-only, uid 65534, no capabilities;
    a placeholder, not the S3 design);
  - Postgres with no schema, roles or RLS;
  - a Keycloak dev realm and a SeaweedFS dev S3 stub without encryption.
- Evidence: `docs/evidence/p0/`. That includes the dev-stack inspection and an informational image scan
  (`image-scan-trivy.md`: the dev Postgres and Keycloak images carry HIGH findings, recorded there).

## 11. Reading list (in order)

1. SECURITY_AND_PRIVACY.md, all of it (about 100 lines).
2. ARCHITECTURE_OVERVIEW.md sections 3–5 (containers, worker boundary, responsibilities).
3. SYSTEM_DESIGN.md sections 1, 11 and 12 (lifecycle, data classification, broker protocol).
4. DECISION_LOG.md D-21, D-23, D-24, D-25, and the "Review findings v0.2.1" section.
5. This repository's `docs/roadmap/P1_READINESS.md`, if provided: the planned P1 work packages and data lifecycle
   (on the preparation branch).

## 12. Deliverables requested from the reviewer

- **D1:** findings per design, with severity and the recommended change.
- **D2:** the sign-off record per design (section 8).
- **D3:** amendments to the section 7 evidence list.
- **D4:** residual risks they consider acceptable, with rationale, for the owner to accept or reject.
- **D5:** any change that would alter the approved architecture. Each needs an ADR and owner approval.

## 13. Logistics

- Repository: https://github.com/17mohak/the-fine-print (**private**; the owner grants read access).
- Synthetic data only.
- Undecided items that affect the review: KMS/HSM product, hosting region, IdP, LLM providers, retention periods.

## Changelog
- **Revision 2 (2026-11-20):** added the executive summary, boundary diagram, data-flow inventory, asset classes,
  threat scenarios, evidence-and-failure table, sign-off criteria, reserved-decision list and reading list.
  Questions reorganised (16 kept or merged, 8 added: Q9, Q10, Q15, Q16, Q21–Q24).
- **Revision 1 (2026-11-20, `7960257`):** initial scope, 16 questions, deliverables and process.
