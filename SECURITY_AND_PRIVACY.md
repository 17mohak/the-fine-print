# SECURITY_AND_PRIVACY.md

**v0.2.1 — design requirements. Architecture **APPROVED v0.2.1 direction** (D-21, 2026-11-20, subject to unresolved decisions, limitations and review gates) · Implementation **AUTHORISED FOR TRACK P, P0 ONLY** (P1–P7 and Track C C0–C3 NOT AUTHORISED) · Opus handover **READY FOR P0 ONLY** · Consumer validation **WAIVED AS A DEVELOPMENT GATE, NOT PASSED**. None of these controls is implemented or authorised for implementation in P0; G0-SEC is required before P1. No security review has been performed. Production security is NOT claimed to be solved; no DPDP or other compliance claim is made.** [LEGAL] / [SECSPEC] = needs specialist review. v0.1 in `_archive/architecture_v0.1/`. 2026-11-20.

## 1. What is protected (scope expanded in v0.2)
| Data | Examples | Storage protection |
|---|---|---|
| Originals | uploaded PDFs/images | ciphertext in object store; per-object DEK |
| **Derived artefacts** | page images, OCR token JSON, L0/L1 canonical text, edit maps | ciphertext blobs; per-object DEK |
| **Extracted assertions** | statement text, structured values, quotes (anchors' `quote`) | encrypted columns (field keys); offsets/ids plaintext |
| **Case evidence** | case notes, event descriptions, communications, drafts, attachments | encrypted columns/blobs |
| Commitments | description, amounts encrypted; **due dates plaintext** (needed for scheduling) | mixed (see §3.4) |
| Account/consent | contact data | encrypted columns; email/hash for login via IdP |
| Operational | logs, metrics, traces | **no content**; scrubber enforced |
Minimal plaintext-queryable metadata: ids, timestamps, status/type enums, key versions, sizes, due dates, rule/knowledge data (public regulatory text). Metadata inference risk (e.g. document count, dates) is accepted and documented.

## 2. Threat model (updates marked ★)
| # | Threat | Boundary | Required controls |
|---|---|---|---|
| T1 | Unauthorised access / IDOR | T1 | Default-deny; TenantContext on every service call; RLS (§6); owner checks; object bytes are streamed through the API under TenantContext (no direct object-store URLs handed to browsers in v0.2) |
| T2 | Cross-user leakage (caches, search index, model context, logs, jobs) | all | per-user key + index partitioning; single-tenant model calls and jobs; log scrubber; canary tests |
| T3 | Storage/backup exposure | T6 | ciphertext only at rest (§3); restore-time deletion replay (§4) |
| T4 | Prompt injection | T3 | §8 |
| T5 | Malicious/malformed files | T2 | worker sandbox + broker (ARCHITECTURE §4); no untrusted parsing in core |
| T6 | Parser/OCR vulnerabilities | T2 | SBOM, pinned/patched deps, per-job child process, fuzz corpus |
| T7 | Model-provider data exposure | T3 | allow-list per data class; redaction; DPAs/no-training/zero-retention where obtainable; regional constraint; local-model option; **provider terms never assumed sufficient** [LEGAL][OWNER] |
| T8 | Credential/secret leakage | all | secrets manager, rotation, scanning |
| T9 | Insecure API | T1 | validation, parameterised queries, rate limits, CORS allow-list, CSRF, headers (OWASP ASVS L2) |
| T10 | Excess logging of personal data | internal | allow-listed fields; scrubber tests; no document text in logs/traces/error messages |
| T11 | Retention/deletion failure | T6 | §4 |
| T12 | Insider access | T5 | no standing admin access to content; break-glass; hash-chained audit; but **application operators with key-service access can in principle decrypt** — residual risk |
| T13 | Account compromise | T1 | MFA via IdP, session mgmt, step-up for export/delete |
| T14 | Integration risks | T4 | default no CONNECTED; capability records; no stored third-party credentials; no OTP/CAPTCHA automation |
| T15 | Misleading output | product | provenance gate, abstention, copy rules |
| T16 | Supply chain/CI | build | locked deps, signed builds, least-privilege tokens |
| ★T17 | Worker/broker compromise (hostile file → worker escape → pivot) | T2 | sandbox, no network except broker mTLS, no creds, broker validates, short job tokens; accepted residual: plaintext of the job's file is in worker memory during processing |
| ★T18 | Key compromise (KEK, UMK, DEK) | T7 | hierarchy, rotation, KMS access policy, audit of key use, anomaly alerts [SECSPEC] |
| ★T19 | RLS bypass (missing SET, pooled-connection leak, superuser role) | DB | §6 controls + tests |
| ★T20 | Background-job cross-tenant mix-up | internal | §6.3 |
| ★T21 | Decrypted text residency (heap dumps, swap, caches) | app | in-memory caches TTL-bounded and per-user; no swap for app containers; crash-dump disabled/encrypted |

## 3. Key hierarchy and envelope encryption [TECHNICAL RECOMMENDATION; KMS/HSM product = OWNER/LEGAL/SECSPEC decision, not made]
**3.1 Hierarchy**
1. **Root KEK** — held in a KMS/HSM (provider/region undecided). Never leaves KMS; used to wrap/unwrap UMKs. One per environment; versioned.
2. **User Master Key (UMK)** — random 256-bit per user (and `umk_version`), generated at account creation, stored **wrapped by Root KEK** in `user_key`. Unwrapped in memory on demand, cached per user for a short TTL, zeroised on expiry.
3. **Data Encryption Keys (DEK)** — random 256-bit **per object** (each original, each derived blob, each attachment), AES-256-GCM, wrapped by the user's UMK; wrapped DEK stored in `object_key` next to the object reference. Unique nonces; AAD binds `(user_id, object_id, key_version, purpose)` so ciphertext can't be replayed across users/objects.
4. **Blind-index key** — a per-user key derived by HKDF from the UMK (`info = blind-index`) used for HMAC-SHA256 blind indexes (document dedup hash, `canonical_hash`) so equality can be tested server-side without storing raw content hashes.
5. **Field keys** — for encrypted DB columns: per-user, per-purpose subkeys derived with HKDF-SHA256 from the UMK (`info = table|column|key_version`); AES-GCM with row-bound AAD (`row_id`, `user_id`).
**3.2 What this achieves/doesn't:** per-user compromise isolation, crypto-shredding granularity, protection of backups and DB-admin reads of ciphertext. **It is not end-to-end/zero-knowledge:** the server must decrypt to process (OCR results, LLM calls). An attacker with application-level access plus key-service permissions can read active users' data; this residual risk is documented, not hidden.
**3.3 Algorithms** (ASSUMPTION; confirm with specialist): AES-256-GCM, HKDF-SHA256, random 96-bit nonces with per-key counters/limits, KMS-supplied wrap. Use a vetted library (e.g. Google Tink or JCA with standard providers) — **no custom cryptography**.
**3.3.1 Transport and in-memory:** TLS 1.2+ everywhere incl. broker mTLS; decrypted bytes only in process memory.
**3.4 Plaintext metadata decision:** commitment `due_at` is plaintext for scheduling; description/amount/notes encrypted. Document titles encrypted (shown after decrypt). Counts/sizes/timestamps plaintext.

## 4. Rotation, deletion, backups
**Rotation:** *Root KEK* rotate by KMS key version → re-wrap UMKs (cheap, online). *UMK* rotate per policy or on suspicion: new `umk_version`; re-wrap all DEKs (no data re-encrypt); field-key rotation requires re-encrypting affected columns by a background migration (versioned columns decrypt with older versions until done). *DEK* rotation: on reprocess or scheduled re-encrypt of blobs. Every key use/rotation logs a `key_event` (no key material).
**Deletion:** user deletion → (1) mark account `DELETING`; (2) delete objects and rows (cascades incl. derived artefacts, assertions, case evidence, search index); (3) **crypto-shred**: destroy the wrapped UMK and all wrapped DEKs, write a `deletion_ledger` entry `{user_id, deleted_at, key_destroyed_at}`. Individual object/document deletion destroys its DEK.
**Backups:** backups contain only ciphertext plus wrapped keys. Because a restored backup would re-introduce a deleted user's wrapped UMK, the **restore procedure must replay the deletion ledger before the system serves traffic**; the ledger itself is retained separately and minimal (id + dates). Backups expire ≤ 35 days (proposed; [LEGAL]). KMS must not allow resurrection of destroyed UMK material (UMKs are *our* wrapped blobs; destroying the blob in primary and ledger replay in restores is the mechanism). Residual window (backup lifetime) disclosed.
**Not claimed:** that deletion is instantly total in all replicas/backups; legal retention duties may conflict [LEGAL].

## 5. Search and retrieval — D-25 ADOPTED (owner direction, 2026-11-20): ephemeral per-user index
Encrypted text cannot be indexed by Postgres FTS, and **no plaintext user-document text or plaintext search index is ever persisted.**
**Design:**
- **Scope:** *agreement-scoped* retrieval first (Q&A over one agreement, in-document find, clause lookup). Agreement list filtering uses a small in-memory metadata index built from decrypted titles/metadata. **Broad cross-vault full-text search is deferred** until the benchmark trigger below is met.
- **Build:** lazily on first need, from decrypted L1 text + structure nodes, inside a tenant-scoped build task (TenantContext explicit). Single-flight per `(userId, agreementId)`; bounded build concurrency; admission control.
- **Storage:** heap memory only. No disk spill, no memory-mapped files, no snapshots, no serialisation to cache servers; heap/core dumps disabled or encrypted; no swap for app containers.
- **Tenant partitioning:** `TenantIndexRegistry` keyed by `(userId, agreementId)`; every access requires a matching TenantContext; no shared mutable analyser/dictionary state that can carry content between users; cross-tenant query is impossible by construction; tests use two-user canaries.
- **Bounds:** global byte budget, per-user and per-index caps (defaults ASSUMPTION, set by ADR after measurement); LRU eviction under pressure; **short idle TTL** (default ASSUMPTION 5–10 minutes) plus absolute maximum lifetime; eviction drops references and zeroises large buffers best-effort (the JVM cannot guarantee erasure — stated, not hidden).
- **Invalidation/rebuild:** index key includes `(agreementId, canonical_hash, canon_version, segmenter_id)`; any document version, canon revision or rule change evicts it; a miss rebuilds. If the budget is exhausted, return a controlled "preparing your documents" state or fall back to a direct linear scan of the decrypted L1 for that agreement; never to a plaintext persisted index.
- **Multi-instance:** indexes are per instance (optional user-affinity routing); no shared index store.
- **Observability:** build time, hit rate, resident bytes, evictions, rebuilds — no content.
**Trade-offs accepted:** cold-start latency on first question; memory per active user; index lost on restart/eviction (rebuild); repeated decryption (more key operations); no cross-agreement ranking; extra engineering. **Benefits:** no plaintext at rest, no database-side content exposure, no cross-user query path.
**Revisit triggers for broader search:** (a) validated user need for cross-vault search (opportunity-validation research, RESEARCH_BACKLOG D); (b) benchmark showing agreement-scoped retrieval cannot meet the use case, or building a vault-wide ephemeral index exceeds latency/memory SLOs at realistic p95 vault sizes (SLO values set by owner); (c) any alternative (persisted encrypted index, searchable encryption, plaintext-in-Postgres) requires its own security review and ADR — **none is pre-approved.**
Public regulatory knowledge (not user data) may use Postgres FTS.

## 6. Tenant context, RLS and job isolation
**6.1 TenantContext propagation:** an immutable `TenantContext{userId, requestId, scopes}` is created only by the security filter (or by a job runner from a job row). **Domain/service methods take `TenantContext` as an explicit parameter** — no ambient/static lookups, no thread-locals in domain code. Async/executor hand-offs copy the context explicitly; `@Async`/virtual-thread use is wrapped by a helper that cannot run without a context.
**6.2 RLS enforcement:** every user-owned table has `user_id uuid NOT NULL` and `ENABLE` + **`FORCE ROW LEVEL SECURITY`** with policies `USING (user_id = nullif(current_setting('app.user_id', true), '')::uuid) WITH CHECK (same)`; unset setting ⇒ NULL ⇒ no rows (default deny). The app connects with a **non-owner, non-superuser role without BYPASSRLS**; migrations run as a separate owner role. Each transaction begins with `SELECT set_config('app.user_id', :id, true)` (transaction-local, so pooled connections cannot leak a previous tenant; if using PgBouncer, transaction pooling mode only). A transaction wrapper refuses to open a DB transaction without a context. **Tests (CI):** two-user matrix over every table; assert zero cross-reads/writes; assert a connection returned to the pool has an empty setting; assert policies exist for every table carrying `user_id` (schema lint); attempt superuser/BYPASSRLS usage fails startup.
**6.3 Background-job isolation:** each job row carries `user_id` and job type; the executor builds a TenantContext from the row and sets `app.user_id` per job transaction; a job processes **one tenant only**. Jobs never reuse a connection with a previous tenant's setting. Per-tenant key material fetched via the `crypto` module under that context and discarded after the job. **Cross-tenant system tasks** (e.g. scanning due reminders) run as a separate DB role `system_scheduler` with column-limited access to a view of `reminder(id, user_id, fire_at, status)`; dispatch then switches to tenant context per reminder. No system task reads encrypted payloads. Worker-broker jobs are likewise tenant-scoped; the worker never learns `user_id` (it receives an opaque job id).
**6.4 Evidence of enforcement:** isolation test suite, schema lint, security review [SECSPEC].

## 7. Other requirements (carried from v0.1)
AuthN via OIDC (IdP undecided), MFA support; authZ roles USER/REVIEWER/ADMIN (ADMIN has no content access); audit hash-chain (events without content); retention defaults proposed (originals until user deletion + 30-day grace; superseded runs purged > 90 days; model-call content not stored; backups ≤ 35 days) [LEGAL]; granular consent records and purpose limitation (research use default OFF) [LEGAL]; incident runbook and breach process [LEGAL — DPDP Rules timelines unverified]; backup/restore drills; RPO/RTO TBD.

## 8. Prompt-injection and hostile-content defences
(unchanged from v0.1 §3) data/instruction separation; least privilege (no tools/network/other tenants' data); schema-constrained output + Java validation; grounding gate (injected text can appear only as a quoted document string); heuristics (imperatives, hidden text, odd Unicode) → `NEEDS_REVIEW` banner; one model call per purpose; red-team corpus in CI; UI escapes all document text. **Added:** quote anchors are verified in Java against canonical text; text that "tells the model" something cannot upgrade a statement's kind.

## 9. Privacy boundaries
No sale/sharing; no advertising use; no training on user documents without separate opt-in [LEGAL]; minimum necessary to providers; telemetry excludes content; no commissions/steering.

## 9a. Specialist-review precondition for P1 (owner condition, recorded in D-21)
**A security-specialist review of the encryption design (section 3-4), RLS/tenant-context design (section 6) and worker-isolation design (ARCHITECTURE_OVERVIEW section 4) is required BEFORE P1 implements those controls.** P0 must not implement them. The review does not constitute production security approval.

## 10. Pre-production gates (all required before real users/sensitive documents)
Legal: DPDP applicability/notice/consent/breach/cross-border; health & financial data; legal-practice and insurance-advice boundaries; liability/terms. Security: independent review of key management, RLS and isolation tests, worker sandbox, pen-test. Operations: incident runbook, support-access policy, deletion + restore-replay verified, retention verified by test. Product safety: abstention/provenance suite thresholds. **Until closed: synthetic data only.**

## 11. Honest status of unsolved items
KMS/HSM choice, hosting region/residency, IdP, provider DPAs, RPO/RTO, retention law, ephemeral-index memory/latency behaviour (§5, unproven), sandbox hardening level, plaintext-in-memory exposure (T17/T21), insider-with-key-access risk (T12/T18), metadata inference, metadata of due dates. **None is resolved by this document.**

## 12. Questions for specialists
DPDP role/consent manager/cross-border; permissibility of LLM providers outside India; whether health-data storage triggers additional duties; deletion vs legal-hold; KMS/HSM design and custody; adequacy of crypto-shredding with backups; liability for erroneous deadlines.
