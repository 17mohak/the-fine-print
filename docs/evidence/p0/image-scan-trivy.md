# Container image scan — Trivy (informational, 2026-11-20)

- **Tool:** Trivy 0.76.2 (`aquasec/trivy@sha256:8f6bb947…9768fb`), run locally and read-only. Settings:
  `--scanners vuln --severity HIGH,CRITICAL`, vulnerability database as of 2026-11-20.
- **Status:** informational. Trivy is **not** part of the approved CI pipeline (ADR-0007 scans dependencies and
  secrets only). Image scanning was flagged in ADR-0007 as a future revisit item. Adding it to CI would be a
  pipeline change, proposed in `docs/g0/TOOLCHAIN_INVENTORY.md`.
- **Context:** these images run only in the loopback-only, synthetic-data development stack. None is a
  production image.

| Image (pinned digest) | HIGH | CRITICAL | Where | Fix available? | Assessment |
|---|---|---|---|---|---|
| `postgres:18.7-alpine3.23@sha256:f3ee138d…` | 26 | 1 | All 27 in Go `stdlib` v1.24.6 inside **`/usr/local/bin/gosu`**, the privilege-drop helper used by the image entrypoint. None is in PostgreSQL or Alpine packages | Yes, in newer Go; needs an upstream image rebuild | gosu runs once at container start to drop root and then `exec`s postgres. The upstream gosu project has long stated that most Go stdlib CVEs are not reachable through gosu. Low risk for a loopback dev stub. Monitor upstream rebuilds of the 18.7 tag |
| `chrislusf/seaweedfs:4.54@sha256:4b42c212…` | 0 | 0 | — | — | Clean |
| `quay.io/keycloak/keycloak:26.8.2@sha256:7e66a96e…` | 6 | 0 | RHEL 9.9 base: `sqlite-libs` ×4 (CVE-2026-119373/119374/119379/119586) and `java-21-openjdk-headless` (CVE-2026-91403), all with **no fixed version yet**; Keycloak's bundled `guava` 33.6.0-jre (CVE-2026-102554, fixed in 33.7.2) | OS: not yet. Guava: only via a new Keycloak release (26.8.2 is the latest) | Dev-only IdP on loopback with one synthetic user. Track the next Keycloak patch. Note: the Guava CVE fixed in our own build also exists inside Keycloak's distribution |
| `busybox:1.37.0-musl@sha256:f6a95d61…` | 0 | 0 | — | — | Clean. A newer `1.38.0` exists (not adopted; upgrading needs owner approval of a pin change) |

**No action was taken.** Changing any image pin is a version change (G0-1 / ADR-0002) and needs owner approval.
