# Contributions, authorship and AI-assistance disclosure

**Project type:** individual (one student). The owner states that the professors approved individual treatment (D-31).
**Owner and author of record:** Mohak Mandwani (all commits in `git log` carry this identity; AI tools are not git
co-authors by project rule, [ADR-0008](../adr/0008-commit-attribution.md)).

## AI-assistance disclosure

AI assistance **was used extensively** in this project, and this section says so plainly. The tool was Claude (Anthropic),
used through Claude Code sessions directed by the owner.

| Area | AI assistance | Human role |
|---|---|---|
| Research record, architecture and governing documents (`*.md` in the repository root, `Gate1B/`) | Drafted and revised in AI sessions | Owner directed scope, answered open questions, recorded decisions D-10…D-32 in [DECISION_LOG.md](../../DECISION_LOG.md) |
| Assessed algorithms (A1 KMP, A2 Bloom filter), their tests, benchmark code and write-ups | AI-assisted implementation under the owner's direction, after the owner recorded the faculty approval (D-31) | Owner specified the problem and algorithms, reviewed, accepted and takes responsibility for the code; the algorithms must be explained by the owner in the viva ([VIVA.md](VIVA.md)) |
| Demo backend, UI, sample data, scripts, evidence pack | AI-assisted | Owner directed and reviewed; ran the application and benchmarks on the owner's machine |
| Proposal and technical report | Drafted with AI assistance from the repository's own records and measured results | Owner reviews and submits |

**Faculty policy status (as recorded, not embellished).** DECISION_LOG D-31 records the owner's statement that the
professors approved (1) individual treatment, (2) AI assistance for the assessed Java algorithms, (3) AI-assisted
infrastructure, (4) the SPI contracts and (5) the project scope. **No written faculty confirmation is held in this
repository.** The disclosure form and wording the faculty may require (item F2) is **not yet specified**; this
document is offered in its place until it is.

> **Owner to confirm before submission:** the table above describes what the repository records. If a
> faculty-prescribed disclosure statement exists, use its wording. If any statement above overstates or understates what you
> personally did, edit it: the viva will test your own understanding of KMP, the Bloom filter, their correctness
> and their complexity.

## What the owner did (as recorded in the repository)
- Chose the problem (quote verification for consumer documents) and the scope of the graded slice versus the wider platform.
- Made and recorded the scope, authorisation and boundary decisions (D-10…D-32), including keeping assessed code isolated from the platform.
- Reviewed the work in stages (P0 verification, pull requests #1 and #2, G0 decisions) and ran builds, tests and benchmarks on a personal machine (hardware recorded in [BENCHMARKS.md](BENCHMARKS.md)).

## Third-party code and data
- Frameworks supply infrastructure only (Spring Boot for HTTP, JUnit/AssertJ/ArchUnit for tests, JMH for benchmarking); see [DEPENDENCIES.md](DEPENDENCIES.md).
- The assessed classes use **no** JDK search, set, bit-set or regex utilities for the algorithm itself (ArchUnit-enforced). `String.indexOf`, `HashSet` and a naive matcher appear only as baselines in benchmarks and test references.
- The MurmurHash3 `fmix64` finaliser constants are public-domain and are used inside the project's own hash function (documented in [A2_BLOOM_FILTER.md](A2_BLOOM_FILTER.md)).
- All data is synthetic; no real documents, personal data or scraped content.
