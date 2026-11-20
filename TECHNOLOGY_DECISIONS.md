# TECHNOLOGY_DECISIONS.md

v0.2.1 — Architecture **APPROVED v0.2.1 direction** (D-21, 2026-11-20, subject to unresolved decisions, limitations and review gates) · Implementation **AUTHORISED FOR TRACK P, P0 ONLY** (P1–P7 and Track C C0–C3 NOT AUTHORISED) · Opus handover **READY FOR P0 ONLY** · Consumer validation **WAIVED AS A DEVELOPMENT GATE, NOT PASSED**. Individual technology recommendations remain PROVISIONAL (decision #8 adopted by owner direction; **G0 requires owner confirmation of versions (#2)**); nothing here is a made decision unless §A says "decidable now".** (v0.1 in `_archive/architecture_v0.1/`.) 2026-11-20. Versions are ASSUMPTIONS to confirm on build start.

## A. Decision readiness (who must decide)
Legend: **NOW** = can be settled by owner approval of this architecture (low regret, reversible) · **OWNER** = needs owner input · **FACULTY** = needs faculty input · **LEGAL** = needs legal review · **SEC** = needs security specialist · **ADR** = Opus may decide within approved architecture via ADR (no silent architecture change).
| # | Decision | Recommendation (provisional) | Readiness | Blocked by |
|---|---|---|---|---|
| 1 | Backend language | Java (CIA-mandated) | **NOW** (fact from CIA guide) | — |
| 2 | Framework / build | Spring Boot (pinned current GA) + Gradle Kotlin DSL; Java 21 LTS | **NOW** provisional; exact versions **ADR** | confirm availability |
| 3 | Architecture style | Modular monolith + Python worker + broker | **NOW** on approval | owner approval |
| 4 | CIA/production separation | `core-api` SPIs; isolated `cia-*` modules | **NOW** on approval | — |
| 5 | CIA algorithm authorship | students write assessed bodies | **FACULTY** (policy) + **OWNER** | AI policy unknown |
| 6 | Frontend | React + TypeScript (Vite) | **NOW** (CIA allows any JS framework) | — |
| 7 | Primary DB | PostgreSQL 16+ | **NOW** | — |
| 8 | User-document search/retrieval | **ADOPTED (owner direction, D-25):** ephemeral, lazily built, agreement-scoped in-memory index from decrypted L1; no persisted plaintext index; cross-vault search deferred; bounds/TTL tuned by ADR | **DECIDED (direction)**; bounds via **ADR**; security review of residency **SEC** | benchmark triggers for broader search |
| 9 | Object storage | S3-compatible; ciphertext only | **NOW** (interface); product/region **OWNER+LEGAL** | hosting |
| 10 | Encryption design | envelope hierarchy (SECURITY §3) | **NOW** as design; implementation details **SEC** | review |
| 11 | KMS/HSM product and custody | undecided | **OWNER + LEGAL + SEC** | residency, cost |
| 12 | Hosting provider/region/data residency | undecided | **OWNER + LEGAL** | DPDP review |
| 13 | IdP / authentication product | OIDC; Keycloak in dev; prod undecided | **OWNER + SEC** | hosting, cost |
| 14 | LLM providers / model access | provider-agnostic Java gateway; providers undecided | **OWNER + LEGAL** | DPAs, residency |
| 15 | OCR engine | pluggable port; Tesseract baseline; cloud OCR only if benchmark + LEGAL approve | **OWNER + LEGAL** (cloud); benchmark **ADR** | A-5 |
| 16 | Worker sandbox level | child-process + container hardening (ARCH §4); per-job ephemeral containers deferred | **SEC** | review |
| 17 | Job queue | Postgres-backed; JobRunr vs custom | **ADR** | — |
| 18 | Vector retrieval | none until benchmarked | **NOW** (defer) | A-6 |
| 19 | Observability | OpenTelemetry + Prometheus; JSON logs with scrubber | **NOW** | — |
| 20 | Testing stack | JUnit5, AssertJ, jqwik, Testcontainers, ArchUnit, JMH, Playwright | **NOW** | — |
| 21 | Migrations | Flyway | **NOW** | — |
| 22 | Deployment | Docker Compose now; K8s deferred | **NOW** | — |
| 23 | Rule representation | declarative versioned rules + Java evaluator | **NOW** | — |
| 24 | Retention periods | proposed defaults | **LEGAL + OWNER** | — |
| 25 | Email/WhatsApp/SMS reminders | deferred | **OWNER + LEGAL** | consent/provider |
| 26 | UI languages (v1) | English first; Hindi next (ASSUMPTION) | **OWNER** | — |
| 27 | Team size / CIA scope | — | **FACULTY + OWNER** | — |

## B. Owner-input list (must be answered before the phases that need them)
Before P0: confirm versions (#2), approve architecture (#3–#4), authorship policy path (#5, #27). Before P1: search architecture (#8), hosting/KMS direction (#9, #11, #12) at least for dev defaults. Before P2: LLM provider stance (#14), OCR path (#15). Before any real data: #24 and all production gates.

## C. Evaluations (options · recommendation · rationale · trade-offs · cost · reversibility · evidence)
| Area | Options | Recommendation | Rationale / trade-offs | Complexity | Reversibility | Evidence/assumptions |
|---|---|---|---|---|---|---|
| Backend | Spring Boot; Javalin; Quarkus; Micronaut | Spring Boot | CIA recommends Spring Boot; mature security/data/testing | Low–med | Med | CIA guide (VERIFIED) |
| Style | microservices; modular monolith; serverless | modular monolith | small team; single DB; ArchUnit boundaries | Low | High | principle |
| Frontend | React/Next/Vue/Angular | React+TS SPA | all logic server-side; accessible primitives | Low | High | CIA §2 |
| DB | PostgreSQL; MySQL; Mongo | PostgreSQL | RLS, JSONB, transactional queue | Low | Med | — |
| Search | PG FTS; OpenSearch; embedded Lucene/in-process index; persisted encrypted index | **ephemeral agreement-scoped in-memory index (D-25)**; PG FTS for public knowledge only; persisted/cross-vault alternatives deferred, each needing SEC review + ADR | encrypted text can't use PG FTS (v0.1 recommended FTS — **revised**) | Med | High | SECURITY §5 |
| Queue | PG queue; RabbitMQ; Kafka | PG queue | transactional with data | Low | High | — |
| OCR | Tesseract; Paddle; cloud | port + Tesseract baseline | local vs accuracy/privacy | Med | High | unbenchmarked |
| Untrusted parsing | Java PDFBox in core; worker | **worker only** (v0.1 reversed) | blast-radius | Med | Med | threat model |
| Auth | Keycloak; managed; Spring AS | OIDC, IdP undecided | no passwords in app | Med | High | — |
| Encryption | TDE only; envelope | envelope (SECURITY §3) | per-user shredding; residual insider risk | High | Med | [SEC] |
| Observability | OTel+Prom; vendor | OTel+Prom | vendor-neutral | Low | High | — |
| Deployment | Compose; VM; K8s | Compose | CIA accepts local demo | Low | High | CIA §10 |
| AI access | single; multi; local | gateway; ≥2 providers or local | data-exposure | Med | High | undecided |
| Extraction | single LLM call; hybrid | hybrid pipeline with Java verifiers | auditability | Med–high | High | unbenchmarked |
| Production algorithms | assessed CIA code; standard libs | **standard libs** | reliability; academic code isolated | Low | High | ARCH §2 |

## D. Revisit triggers
Broader search (vault-wide or persisted index): validated need + benchmark showing agreement-scoped retrieval insufficient or vault-wide ephemeral build exceeding owner-set latency/memory SLOs (SECURITY section 5). Split a service when it needs independent scaling/security; add search engine when in-process index fails benchmarks; vector store on measured recall gain; K8s on multi-node need; managed OCR if local accuracy insufficient and LEGAL approves; promote any assessed algorithm only via the seven-point process.

## E. Deferred (explicitly not decided)
KMS/HSM, hosting/region, IdP, LLM providers, OCR vendor, queue library, Indic tokenisation, public-site framework, retention schedule, reminder channels.
