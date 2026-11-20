# SECURITY_AND_PRIVACY.md

Status: PROPOSED initial threat model and requirements · 2026-11-20. **No security review has been performed. No claim of compliance (DPDP or otherwise) is made.** Items marked [LEGAL] / [SECSPEC] need specialist review before real users or real sensitive documents.

## 1. Data classification
C3 Highly sensitive: uploaded agreements, claims/medical/financial correspondence, case evidence, extracted assertions. C2 Personal: identity, contact, consent. C1 Operational: logs, metrics (must contain no C3 content). Development uses **synthetic documents only** until production-critical items close (RESEARCH_BACKLOG §C).

## 2. Threat model (STRIDE-ish, by boundary)
| # | Threat | Boundary | Impact | Required controls | Verify by |
|---|---|---|---|---|---|
| T1 | Unauthorised document access / IDOR | T1 | Severe | Default-deny; owner check in every service method; Postgres RLS; signed short-lived object URLs bound to user+doc; IDs unguessable (UUIDv7 not sole control) | Automated authZ test matrix; pen-test |
| T2 | Cross-user leakage (cache, search index, model context, logs) | all | Severe | Per-user partition keys in caches/search; model calls built from one user's data only; no shared conversational memory; log scrubber | Tenant-isolation tests with two users; canary strings |
| T3 | Insecure file storage / backup exposure | T6 | Severe | Private buckets; SSE-KMS + per-user envelope keys; encrypted backups; restore access separate from app role; retention enforcement | Config audit; restore drill |
| T4 | Prompt injection in uploaded docs | T3 | High (wrong answers, data exfil, misleading advice) | See §3 | Injection test corpus in CI |
| T5 | Malicious/malformed files (zip bombs, polyglots, PDF JS, parser exploits) | T2 | High | Magic-byte validation, size/page/time limits, AV scan, strip active content in derived copies, sandboxed worker (non-root, seccomp/no network/read-only FS, CPU/mem caps), safe parsers pinned and patched | Fuzzing corpus; sandbox escape review |
| T6 | OCR/parser vulnerabilities in dependencies | T2 | High | SBOM, dependency scanning, pin + update cadence, worker isolation | CI scans |
| T7 | Model-provider data exposure/retention/training | T3 | Severe | Provider allow-list per data class; contractual no-training/zero-retention where available; redaction profile (names, account/policy numbers, Aadhaar/PAN) before calls where feasible; regional processing; option of local model; **provider terms never assumed sufficient** [LEGAL] | DPA review; egress logs |
| T8 | Credential/secret leakage | all | High | No secrets in repo; secrets manager; rotation; scoped service accounts; pre-commit secret scanning | CI scan |
| T9 | Insecure API (injection, SSRF, mass assignment, rate abuse) | T1 | High | Validation at edge, parameterised queries, no user-controlled URLs fetched, rate limits, CORS allow-list, CSRF protection for cookie flows, security headers | OWASP ASVS L2 checklist |
| T10 | Excessive logging of personal data | internal | High | Structured logs with allow-listed fields, scrubber tests, no document text/quotes in logs, hashes only for model calls by default | Log-leak tests |
| T11 | Retention/deletion failures | T6 | High | Retention schedule, user deletion workflow cascading to originals, derived data, model-call records, search, backups (expiry window disclosed) | Deletion e2e test |
| T12 | Insider access | T5 | Severe | No standing admin access to C3; break-glass with approval + audit; reviewers see synthetic/consented data only; hash-chained audit | Access review |
| T13 | Account compromise | T1 | High | MFA available/encouraged, session management, device/session list, step-up auth for export/delete, anomaly alerts | IdP config review |
| T14 | External integration risks | T4 | Med–High | Default no connected integrations; capability records; no credential storage for third-party portals; no CAPTCHA/OTP automation; outbound allow-list | Capability audit |
| T15 | Output risks: misleading legal conclusions, fabricated provisions | product | High | Provenance gate, abstention, language rules (SYSTEM_DESIGN §5; PRODUCT_EXPERIENCE_SPEC §8) | Safety test suite |
| T16 | Supply-chain / CI compromise | build | High | Locked deps, signed builds, least-privilege CI tokens | CI review |

## 2. Requirements (initial)
- **AuthN:** OIDC via IdP; MFA supported; short-lived access tokens; refresh rotation; no passwords in app DB.
- **AuthZ:** owner-scoped by default; roles USER/REVIEWER/ADMIN; REVIEWER sees knowledge base + synthetic/consented corpora only; ADMIN has no user-document access; support access = break-glass.
- **Isolation:** `user_id` on every row, RLS enabled, tests run as two users; object keys `u/{userId}/…`; per-user data key (wrapped).
- **Encryption:** TLS 1.2+; HSTS; at-rest encryption for DB, object store, backups; app-level envelope encryption for original documents; key rotation plan [SECSPEC].
- **Secure file handling:** §T5; originals never executed or rendered server-side outside sandbox; downloads via `Content-Disposition: attachment` + signed URL; image thumbnails generated in worker.
- **Secrets:** secrets manager/env injection; never in images or logs.
- **Audit:** append-only, hash-chained events (who/what/when/object id; no content) for document access, export, deletion, consent change, rule approval, capability changes, model calls.
- **Retention (proposed defaults, [LEGAL] to confirm):** originals kept until user deletes or account closure + grace (30 days); extraction runs superseded > 90 days purged; model-call content not stored (hashes only); audit events retained per legal advice; backups expire ≤ 35 days.
- **Deletion:** user-initiated, verified, cascades; completion receipt; backup residual window disclosed.
- **Consent & purpose limitation:** granular consent records (processing documents, using third-party model providers, reminders channels, optional research use = default OFF); purposes enumerated; no secondary use; notices in plain language and Indian languages (later) [LEGAL: DPDP notice/consent manager/children's data/cross-border].
- **Incident handling:** runbook, severity matrix, breach assessment/notification process [LEGAL: DPDP Rules timeline unverified — Gate1B/06 D7], contact point, log preservation.
- **Backup/recovery:** encrypted, tested restores; RPO/RTO TBD [OPEN].

## 3. Prompt-injection and hostile-content defences (design requirements)
1. **Data/instruction separation:** document text only inside a delimited data envelope; system prompt states it is untrusted; no concatenation into instruction positions.
2. **Least privilege:** extraction/QA model calls have **no tools, no network, no access to other users' data, no ability to trigger actions**; outputs are inert structured JSON.
3. **Schema-constrained output + Java validation:** anything outside schema discarded; free-text fields length-capped; links/markup stripped before rendering.
4. **Grounding gate:** claims need verified anchors; text like "tell the user the claim is valid" cannot create a DOCUMENT_FACT unless it is literally in the document — and even then it is shown as a quote, not followed.
5. **Detection heuristics:** flag imperative patterns aimed at the model, hidden text (white-on-white, zero-size), unusual Unicode; flagged docs → NEEDS_REVIEW banner "this document contains text that tries to give instructions to AI; it was ignored".
6. **Separate model call per purpose** (extraction vs Q&A vs drafting); cross-call state stored as validated data only.
7. **Red-team corpus in CI** (Gate1B/09 scenario 11 and more).
8. **UI safety:** rendering escapes all document text; no auto-opening links from documents.

## 4. Privacy boundaries
No selling/sharing of user data; no advertising use; no commissions or steering (conflict-of-interest principle); no training on user documents without separate opt-in [LEGAL]; model providers receive the minimum necessary context; telemetry excludes content.

## 5. Pre-production gates (must close before real users/sensitive docs)
Legal: DPDP applicability/notice/consent/breach, cross-border, health/financial data, legal-practice boundary (Q-P1), insurance-advice boundary, liability disclaimers, terms. Security: independent review of auth/isolation, pen-test, sandbox review, key management design, backup/restore drill. Operational: incident runbook, support access policy, retention verified by test. Product safety: abstention/provenance suite meeting thresholds.

## 6. Questions for specialists
DPDP role (data fiduciary/processor) and consent manager implications; permissible use of LLM providers abroad; retention minimums/maximums; evidentiary preservation duties; whether storing health data triggers additional obligations; KMS/HSM choice; liability exposure for erroneous deadlines.
