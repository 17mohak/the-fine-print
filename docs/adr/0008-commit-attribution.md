# ADR-0008: Commit-attribution rules and enforcement

- **Status:** Accepted — owner directive (P0 master directive, 2026-11-20)
- **Date:** 2026-11-20
- **Phase / authorisation:** Track P, P0 (D-21: "commit-attribution rules")
- **Decision owner:** OWNER
- **Related:** CONTRIBUTING.md#commit-attribution; D-20 (academic AI-assistance policy, OPEN)

## Context
The owner directed that AI tools are development assistants, not git co-authors. Every commit must use the actual
human contributor's configured identity. No AI co-author trailer, AI git identity or AI attribution may appear in
commit metadata, README credits, package metadata or ownership declarations. Academic AI-assistance disclosure
must follow the actual faculty policy, which is still unknown (D-20).

## Decision
1. The rules are written in `CONTRIBUTING.md#commit-attribution`.
2. Enforcement:
   - CI job `attribution` runs `ops/scripts/check-commit-attribution.sh` over the full history.
   - The optional local hook `ops/git-hooks/commit-msg` is enabled with
     `git config core.hooksPath ops/git-hooks`.

   Both reject `Co-authored-by` trailers, and author/committer identities, that name Anthropic or a Claude
   product (Claude Code / Opus / Sonnet / Haiku / Fable / AI). The pattern deliberately does not match human
   names such as "Claude Martin".
3. Existing human attribution is never rewritten (no history rewriting to change authors).
4. **Not decided here:** how AI assistance is *disclosed* for academic purposes. That follows the faculty
   policy once it is supplied and recorded (D-20). Nothing in this repository asserts a disclosure position yet.

## Consequences
- **Residual gap:** a trailer naming a bare "Claude" with a non-Anthropic e-mail is not detected
  automatically. Code review covers it.
- The rule constrains metadata only. It says nothing about who wrote what, which remains a disclosure question
  under D-20.

## Reversibility and revisit triggers
Owner-controlled. Revisit when the faculty policy is recorded.

## Deviation from the approved architecture?
No.
