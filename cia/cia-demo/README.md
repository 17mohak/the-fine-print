# cia-demo — Evidence Integrity Lab

A runnable demonstration of the assessed algorithms (Track C, D-31). The browser UI calls the Java backend; the
algorithms run on the server.

```bash
./gradlew :cia:cia-demo:bootRun
```

Then open http://127.0.0.1:8090/ and use **Load synthetic example** in each panel.

| Endpoint | Purpose |
|---|---|
| `POST /algo/quote-match` `{text, quote}` | KMP: every occurrence (code-point offsets), comparisons against the 2n bound, the prefix function |
| `POST /algo/membership` `{fingerprints, probes, targetFalsePositiveRate, expectedInsertions?, seed?}` | Bloom screening, then exact confirmation per probe; parameters, fill ratio, bit pattern (≤ 4,096 bits) |

- Loopback only; stateless; no database; no outbound calls; synthetic input only.
- Invalid input returns `application/problem+json` (400) without echoing the submitted text.
- Code walk-through: `quote/QuoteMatchController` → `QuoteMatchService` → `KmpMatcher`; and
  `membership/MembershipController` → `MembershipScreeningService` → `BloomFilter`, then
  `ExactFingerprintRegistry`.

## Testing
- `DemoServicesTest` (7) and `DemoHttpTest` (4) run with `./gradlew build`. They cover both services, both
  endpoints over HTTP, the problem responses, and that the static UI (`index.html`, `app.js`) is served and
  calls `/algo/*`.
- **Gap:** there is no automated browser test. The repository has no browser-automation tooling, and none was
  added for this demo. Rendering, highlighting and bit-array drawing were checked manually in a browser
  (2026-11-20). A Playwright suite would be the natural addition when the product UI (`web/`) introduces a
  frontend toolchain.
