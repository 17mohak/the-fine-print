# `doc-worker/` — sandboxed Python document worker — placeholder

**No worker code exists.** The worker and the Java worker broker are excluded from P0 (D-21 clause 2). They are
P1 work, and P1 requires its own authorisation plus the G0-SEC security-specialist review of the
worker-isolation design (ARCHITECTURE_OVERVIEW section 4).

For P0, the development stack contains only an **inert stub container** (`doc-worker-stub` in
`ops/compose/compose.yaml`). It prints a banner and sleeps: it has no network, parses nothing and speaks no
protocol. Its hardening flags are conservative defaults for an inert container, **not** the P1 isolation design.
