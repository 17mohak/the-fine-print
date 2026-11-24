#!/usr/bin/env python3
"""Build the two Word documents for the CIA submission from the repository's own records and measured results.

    python docs/cia/submission/build_documents.py

Writes A_Project_Proposal.docx and C_Technical_Report.docx next to this file. Requires python-docx. The figures
come from make_figures.py. PDF copies were produced by opening each .docx in Microsoft Word (Save as PDF).
Yellow-highlighted text marks values only the student can supply or must confirm.
"""
from pathlib import Path

from docx_helpers import (bullets, code_block, figure, footer, heading, new_document, numbered, para, rule, table)

HERE = Path(__file__).resolve().parent
FIG = HERE / "figures"
REPO = "https://github.com/17mohak/the-fine-print"
TITLE = "The Fine Print: Evidence Integrity Lab"


# --------------------------------------------------------------------------------------------- A. proposal
def proposal():
    d = new_document(body_pt=11, margin_in=0.75)
    footer(d, "CIA-3 Advanced Algorithm Project: Project Proposal")
    d.add_heading("Project Proposal — " + TITLE, level=0)
    para(d, "_Verifying what a document really says, with Knuth–Morris–Pratt matching and Bloom-filter screening (Java server, JavaScript client)_",
         color=None, after=2)
    rule(d)
    table(d, [
        ["Student / team", "Mohak Mandwani, [[<REGISTER-NO>]] (individual project; faculty approval of individual treatment is stated by the student, see Report section 9)"],
        ["Programme", "B.Tech Computer Science (AI & ML), Semester V, ATLAS–uGDX School of Technology · CIA-3 Advanced Algorithm Project · Faculty: [[<FACULTY NAME>]]"],
        ["Repository", REPO],
    ], [1.25, 5.7], header=False, size=9.5, first_col_bold=True)

    heading(d, "1. Real-world problem")
    para(d, "People sign long, dense documents (insurance policies, loan sanction letters, tenancy agreements, subscription terms) and increasingly rely on AI "
            "assistants to summarise them. Language models state things like “your policy says claims must be notified within 30 days” and attach a quotation, "
            "but they sometimes invent or alter the quotation. A statement about a document is only trustworthy if the quoted words can be shown to occur in it, "
            "and the person should also know whether a clause is ordinary wording or unusual. This project builds the checking step: "
            "_given a source text and a claimed quotation, prove on the server whether, where and how often the quotation occurs, and say honestly what that does not prove._")

    heading(d, "2. Target user")
    bullets(d, [
        "**Primary:** a consumer, or a consumer-helpline volunteer or paralegal assisting one, who holds an AI-generated claim about a document (including Hindi or mixed-script text) and needs a quick, trustworthy check against the original.",
        "**Secondary:** developers of document-assistant products who need a provenance gate that rejects fabricated quotations before they are shown.",
    ])

    heading(d, "3. Proposed features")
    bullets(d, [
        "**Evidence check:** paste a source and a quotation; the Java server returns a verdict (`VERIFIED`, `AMBIGUOUS`, `BOUNDARY_NOT_ESTABLISHED`, `NOT_FOUND`) with code-point offsets and the clause.",
        "**Clause screening:** every clause is screened against a reference library of standard wordings; each “possibly present” answer is confirmed by exact comparison.",
        "**Three-part statement:** what is *verified*, what is *screened*, and what is *not established* (meaning, validity, authenticity, currency).",
        "**Safe handling of Indian-language text:** a match that would cut a character (for example a Devanagari vowel sign) is rejected, not reported as verified.",
        "**Explanation panels** that expose each algorithm (prefix function, comparison count, bit array); a documented JSON API; input validation with clear 400 errors; tests, reproducible benchmarks and sample data.",
    ])

    heading(d, "4. Two core techniques and why each is appropriate")
    table(d, [
        ["Technique (syllabus)", "Role in the system", "Why it fits"],
        ["**Knuth–Morris–Pratt** exact matching (Unit 4)",
         "Finds every occurrence of the quotation in the source, including overlapping ones.",
         "Provenance needs *exact* proof of presence, not similarity. KMP has a worst-case O(n + m) guarantee even for adversarial or model-generated text where naive search is O(n·m), and reporting all occurrences is what exposes ambiguous quotations."],
        ["**Bloom filter** (Unit 6 / CO5, probabilistic structure)",
         "Screens each clause’s fingerprint against the reference library before any exact lookup.",
         "No false negatives and a tunable false-positive rate in a few bits per entry: “definitely absent” skips the exact lookup; “possibly present” is always confirmed, so a false positive can never be reported as a match."],
    ], [1.55, 2.1, 3.3], size=9)
    para(d, "Both are implemented from scratch in Java (no library search, set or bit-set class inside the assessed classes; enforced by an ArchUnit test). "
            "Standard-library search and `HashSet` are used **only as baselines** in tests and benchmarks. "
            "The project states honestly that `String.indexOf` is faster than KMP on ordinary text, and says where KMP and the Bloom filter do pay off.", size=10)

    heading(d, "5. Data source")
    para(d, "**All data is synthetic**; no real policy, loan or personal document is used. A reference library of 477 clause wordings (plus 118 held-out look-alikes) is generated "
            "deterministically in code from 20 invented templates covering insurance, lending, tenancy, subscriptions and warranties, in English and Hindi. "
            "Example policies, benchmark text (seeded prose) and benchmark keys (seeded integers) are generated the same way and documented in `docs/cia/DATA.md`.", size=10)

    heading(d, "6. Measurable success criteria")
    table(d, [
        ["#", "Criterion", "Target", "Measured at submission"],
        ["1", "Correctness tests per algorithm (incl. boundary cases)", "≥ 5 each", "KMP 17, Bloom 31; 101 tests in total, 0 failing"],
        ["2", "KMP agrees with a naive reference", "0 mismatches", "0 in 5,000 random cases"],
        ["3", "KMP worst-case work", "≤ 2n comparisons", "380 ≤ 400 on n = 200 adversarial input; asserted at n = 100,000"],
        ["4", "Bloom filter false negatives", "0", "0 in 20,000 keys × 3 seeds"],
        ["5", "Bloom false-positive rate vs design", "within ±5 % (relative)", "within ±1 % at n = 10⁴…10⁶ (9 settings × 5 seeds × 10⁶ probes)"],
        ["6", "Bloom memory vs `HashSet<Long>`", "≥ 10× less", "50–62× less at 10 bits per element"],
        ["7", "Benchmarks", "≥ 3 sizes, ≥ 1 baseline", "n = 10⁴, 10⁵, 10⁶ × 3 pattern lengths; naive and `String.indexOf`"],
        ["8", "Invalid input handled, never silently wrong", "100 % → HTTP 400, input not echoed", "7 of 7 error samples; sub-character and repeated quotations flagged"],
        ["9", "End-to-end latency, 400-clause document", "median ≤ 100 ms", "16.5 ms median (loopback)"],
        ["10", "Reproducible from the repository", "clone, build, run from README", "`./gradlew build`; `bootRun`; 21-sample replay check"],
    ], [0.3, 2.45, 1.6, 2.6], size=8.8)
    para(d, "**Scope note.** This is a self-contained, runnable slice of a larger product idea; it is not a chatbot and no language model is in the loop. "
            "Java does all core logic on the server; the browser only collects input and displays results.", size=10, after=0)
    return d


# ------------------------------------------------------------------------------------------- C. technical report
def report():
    d = new_document(body_pt=11, margin_in=0.85)
    footer(d, "CIA-3 Technical Report: " + TITLE)
    d.add_heading("Technical Report — " + TITLE, level=0)
    para(d, "_Exact quotation verification (Knuth–Morris–Pratt) and probabilistic clause screening (Bloom filter) as a Java service_", after=2)
    rule(d)
    para(d, "**Student:** Mohak Mandwani · **ID:** [[<REGISTER-NO>]] · **Programme:** B.Tech CS (AI & ML), Semester V · **CIA-3 Advanced Algorithm Project** · "
            f"**Repository:** {REPO}", size=9.5)

    # 1
    heading(d, "1. Problem statement and constraints")
    para(d, "**Problem.** Given a source text *T* of *n* symbols and a claimed quotation *P* of *m* symbols, decide whether *P* occurs in *T*, where, and how often; "
            "then tell the user whether the clause that holds it is ordinary wording from a reference library. The motivating failure is a language model "
            "“quoting” a clause that is not in the document. The output must say what is verified and what is **not** established.")
    para(d, "**Constraints.** (i) All core algorithms and business logic in Java, executed on the server; the JavaScript client only calls the API. "
            "(ii) At least two syllabus techniques, one from Units 4–6, and a probabilistic structure for CO5; implemented originally, with frameworks supplying infrastructure only. "
            "(iii) Synthetic data only. (iv) At least five correctness tests per algorithm including a boundary test; benchmarks at three or more sizes against at least one baseline; reproducible from the repository. "
            "(v) Design constraints chosen here: exact matching only (a paraphrase is “not found”); no silent wrong answers; a Bloom “hit” is never a result by itself. "
            "Input limits: source ≤ 100,000 code points and ≤ 400 clauses, quotation 1–2,000 code points, ≤ 5,000 members and 1–500 probes, p ∈ [0.0001, 0.5].")

    # 2
    heading(d, "2. System design and API")
    figure(d, FIG / "fig_architecture.png", 6.35, "Figure 1. System design: the browser UI calls three JSON endpoints of the Spring Boot server; both assessed algorithms run server-side in `cia-algorithms`.")
    para(d, "The slice consists of three Gradle modules: `cia-algorithms` (the assessed code; depends only on the JDK and the small `core-api` contracts), "
            "`cia-demo` (Spring Boot 4.2 on Java 21, controllers, services and the static UI) and `cia-bench` (JMH benchmarks). Assessed classes implement the `QuoteVerifier` and "
            "`ApproximateMembership` interfaces of `core-api`. The server binds to loopback, keeps no state, has no database and makes no outbound calls.")
    table(d, [
        ["Endpoint (POST, JSON)", "Request", "Response (abridged)"],
        ["`/algo/evidence-check`", "`sourceText`, `claimedQuotation`", "`verification` (verdict, match class, occurrences with code-point offsets, clause ids, near-miss hints); `screening` (per-clause Bloom and exact verdicts, lookups avoided, false positives rejected, filter parameters); `evidence` (verified / screened / notEstablished)"],
        ["`/algo/quote-match`", "`text`, `quote`", "all occurrences, prefix function, comparison count vs the 2n bound, server microseconds"],
        ["`/algo/membership`", "`fingerprints`, `probes`, `targetFalsePositiveRate`, optional `expectedInsertions`, `seed`", "m, k, fill ratio, per-probe Bloom verdict then exact confirmation, bit pattern, duplicate accounting, `overCapacity`"],
    ], [1.45, 1.75, 3.75], size=8.6)
    para(d, "**Evidence-check flow.** (1) Validate input. (2) KMP finds all occurrences (code points). (3) A conservative `BoundaryCheck` rejects an occurrence whose start or end would separate a "
            "combining mark from its base, split a virama conjunct (nine Indic scripts), a ZWJ/ZWNJ or emoji sequence, or a CR LF pair; boundaries it cannot judge are treated as rejected. "
            "(4) Classify: one accepted occurrence → `VERIFIED`; several → `AMBIGUOUS`; only rejected ones → `BOUNDARY_NOT_ESTABLISHED`; none → `NOT_FOUND`. "
            "(5) Split the source into clauses, fingerprint each (SHA-256), screen with the Bloom filter, confirm every possible hit by exact comparison. "
            "(6) Return the three-part statement. Errors are returned as `application/problem+json` (HTTP 400) and never echo the submitted text.")

    # 3
    heading(d, "3. Algorithms and pseudocode")
    heading(d, "3.1 A1: Knuth–Morris–Pratt (Unit 4)", 2)
    para(d, "Symbols are Unicode **code points** (`int`), so offsets match the platform’s anchors and a supplementary character is one symbol. Overlapping occurrences are reported.", after=2)
    code_block(d, """prefixFunction(P):                         // m = |P| >= 1
    pi[0] = 0; k = 0
    for i = 1 .. m-1:
        while k > 0 and P[i] != P[k]: k = pi[k-1]
        if P[i] == P[k]: k = k + 1
        pi[i] = k
    return pi

search(T, P):                               // all start offsets, ascending
    pi = prefixFunction(P);  q = 0;  out = []
    for i = 0 .. n-1:
        while q > 0 and T[i] != P[q]: q = pi[q-1]
        if T[i] == P[q]: q = q + 1
        if q == m: out.add(i-m+1);  q = pi[q-1]     // keep overlaps
    return out""")
    heading(d, "3.2 A2: Bloom filter (Unit 6 / CO5)", 2)
    para(d, "Parameters for *n* expected members and target rate *p*: `m = ceil(-n ln p / (ln 2)^2)` bits, `k = max(1, round((m/n) ln 2))`. The bit array and the 64-bit hash are the project’s own code "
            "(multiply–rotate block mixing and the public MurmurHash3 `fmix64` finaliser). Positions use **double hashing with a coprime step**: two hashes give a start `a` and a step `b`; "
            "`b` is increased until `gcd(b, m) = 1`, which makes the first `min(k, m)` positions of every key **distinct** for every `m`. All arithmetic stays below 2^38, so nothing overflows.", after=2)
    code_block(d, """positions(key):  a = H(key, s1) mod m;   b = 1 + (H(key, s2) mod (m-1))    // b = 0 if m = 1
                 while gcd(b, m) != 1: b = b + 1           // terminates: gcd(m-1, m) = 1
                 g = a;  repeat k times: yield g;  g = g + b;  if g >= m: g = g - m
add(key):          set every bit in positions(key)
mightContain(key): false at the first clear bit in positions(key), otherwise true   // "possibly"
screen(clauseWording):  h = SHA-256(utf8(wording))
    if not bloom.mightContain(h):  return DEFINITELY_ABSENT          // no lookup needed
    if exact.get(h) has identical wording:  return IDENTICAL         // Bloom hit confirmed
    return FALSE_POSITIVE_REJECTED                                   // never reported as a match""")

    # 4
    heading(d, "4. Correctness")
    para(d, "**KMP.** *Invariant 1:* at the top of iteration *i* of `prefixFunction`, `k = pi[i-1]`, the longest border of `P[0..i-1]`; the fallback `k = pi[k-1]` enumerates all borders in decreasing length "
            "(a border of a border is a border), so the loop stops at the longest one that `P[i]` extends. *Invariant 2:* after processing `T[i]`, `q` is the length of the longest prefix of `P` that is a suffix of `T[0..i]`. "
            "When `q = m`, `P` is a suffix of `T[0..i]` (**soundness**); if `P` ends at `i`, the longest such prefix is `P` itself so `q = m` (**completeness**); resetting to `pi[m-1]` rather than 0 keeps the invariant, so overlapping matches are not lost.")
    para(d, "**Bloom filter.** *No false negatives:* `add(x)` sets exactly the bits that `mightContain(x)` reads (same seeds and probe sequence) and bits are never cleared, so a member always finds all its bits set. "
            "*Distinct positions:* if `gcd(b, m) = 1` then `i → a + i·b (mod m)` is a bijection on `{0..m-1}`, so a key’s first `min(k, m)` positions are pairwise distinct. "
            "*Rate:* the textbook estimate `p ≈ (1 - e^{-kn/m})^k` assumes independent uniform positions, which this scheme only approximates (positions lie on one arithmetic progression; the step is uniform only for prime `m`), "
            "so **measured rates, not the formula, are the evidence** (section 6).", after=2)
    table(d, [
        ["Algorithm", "Tests", "What they establish (all pass)"],
        ["KMP", "17", "hand-computed prefix function; single, multiple and overlapping matches; no match; empty text, pattern longer than text, pattern = text, last position (boundary); invalid input; surrogate pairs and Devanagari code points; ≤ 2n comparisons on a^100000 vs a^999·b; agreement with a naive reference on 5,000 random inputs; defensive copies; SPI adapter"],
        ["Bloom filter", "31", "formula sizing and bits-per-element sizing; invalid n, p, k, m; boundary parameters; empty filter; **no false negatives** (20,000 keys × 3 seeds); measured rate within a band of the target; saturation at 10× capacity; seed reproducibility; add/probe positions identical; distinct positions for all m ≤ 400 and the maximum m; `BigInteger` cross-check for overflow; bit-array word boundaries; hash tails 0–17 bytes"],
        ["Isolation (ArchUnit)", "5", "assessed classes depend only on the JDK and `core-api`, call no JDK set/bit-set/string-search methods; the rule is proven to fire on a deliberately cheating fixture"],
        ["Demo service and HTTP", "44", "workflow verdicts, boundary rules (Devanagari, Tamil, Bengali, ZWJ, uncovered scripts), every reference wording screens as identical, held-out variants never match, a pinned resolved Bloom false positive, three endpoints over HTTP, 400 responses"],
    ], [1.2, 0.5, 5.25], size=8.6)

    # 5
    heading(d, "5. Complexity")
    table(d, [
        ["Operation", "Time", "Space", "Argument"],
        ["KMP prefix function", "O(m)", "O(m)", "`k` rises ≤ 1 per step and each fallback lowers it: ≤ m fallbacks in total"],
        ["KMP search", "O(n + m); ≤ 2n comparisons", "O(m) auxiliary (+ O(occ) output)", "same potential argument on `q`; asserted in tests and shown in the UI"],
        ["Bloom add / query", "O(k + |key|) + one gcd per key, O(log m)", "m bits (1.25 B/key at 10 bits)", "two hash passes; the coprime search is bounded by the largest gap between integers coprime to m (≤ 1 for powers of two, 0 for primes)"],
        ["Bloom build", "O(n·k)", "m bits", "n insertions"],
        ["Evidence check", "O(n + m + occ·C + Σ|clause|)", "O(n + m + C)", "KMP, clause attribution for C clauses, SHA-256 and exact comparison per clause; bounded by the input limits"],
    ], [1.3, 1.85, 1.45, 2.35], size=8.6)

    # 6
    heading(d, "6. Benchmark methodology and results")
    para(d, "**Setup.** AMD Ryzen 9 8945HS (8 cores/16 threads), 15.3 GB RAM, Windows 11, Oracle HotSpot JDK 21.0.4, JMH 1.37 (KMP: 1 fork, 2 × 1 s warm-up, 3 × 1 s measurements; Bloom: 2 forks, 3 × 1 s warm-up, 5 × 1 s measurements), "
            "nothing else running (a run contaminated by a concurrent test run was discarded). Inputs are seeded synthetic text and keys. **Sizes:** n = 10⁴, 10⁵, 10⁶; pattern length m = 8, 64, 512; "
            "Bloom 6, 10, 14 bits per element. **Baselines:** naive O(nm) search and `String.indexOf` (KMP); `HashSet<Long>` (Bloom). Raw JSON/CSV and one-command reproduction are in the repository (README section 9).")
    table(d, [
        ["Input, m", "n", "KMP µs", "naive µs", "`indexOf` µs"],
        ["Ordinary, 64", "10⁴ / 10⁵ / 10⁶", "16.1 / 190 / 2,918", "31.0 / 386 / 3,546", "**1.6 / 15.5** / 2,716"],
        ["Adversarial, 512", "10⁴ / 10⁵ / 10⁶", "**39.1 / 361 / 3,389**", "1,833 / 19,291 / 185,511", "2,908 / 32,485 / 347,890"],
    ], [1.2, 1.2, 1.5, 1.55, 1.5], size=8.6)
    figure(d, FIG / "fig_kmp.png", 6.2, "Figure 2. KMP vs naive vs `String.indexOf` (log–log, m = 512). On ordinary text the intrinsic wins; on adversarial input only KMP stays linear.")
    figure(d, FIG / "fig_bloom.png", 6.2, "Figure 3. Bloom filter: measured false-positive rate (mean of 5 seeds, 10⁶ probes each) against the textbook formula, and memory against `HashSet<Long>`.")
    table(d, [
        ["n", "bits/elem (k)", "theory FPR", "measured FPR (5 seeds)", "Bloom bytes", "HashSet bytes", "query ops/µs: Bloom vs HashSet"],
        ["10⁴", "10 (7)", "0.819 %", "0.813 %", "12,504", "625,616", "8.11 vs 188.9"],
        ["10⁵", "10 (7)", "0.819 %", "0.814 %", "125,000", "7,759,160", "7.10 vs 195.5"],
        ["10⁶", "10 (7)", "0.819 %", "0.825 %", "1,250,000", "65,465,048", "6.07 vs 173.0"],
        ["10⁶", "6 (4) / 14 (10)", "5.606 % / 0.120 %", "5.594 % / 0.120 %", "750,000 / 1,750,000", "n/a", "5.48 vs 178.9 / 4.30 vs 173.2"],
    ], [0.45, 1.0, 0.95, 1.15, 1.05, 0.95, 1.4], size=8.3)
    para(d, "**Findings.** (1) **The baseline wins on ordinary text:** `String.indexOf` (a vectorised HotSpot intrinsic) is 9–20× faster than our KMP for n ≤ 10⁵; KMP is only about 1–2× faster than naive there. "
            "(2) **KMP’s guarantee is real:** on adversarial input its time depends on n alone (≈ 3.4–3.9 ms at n = 10⁶ for every m), so at m = 512 it is about 55× faster than naive and 100× faster than `indexOf`. "
            "(3) The Bloom filter’s measured rate is within ±1 % (relative) of theory in all nine settings, and it uses **50–62× less memory** than a `HashSet` at 10 bits per element, "
            "but a `HashSet` is 23–40× faster per query: the filter is a memory tool, not a speed tool, which is why it only pre-screens. "
            "(4) A bug found during the work (odd-step probing could give a key fewer than k distinct bits when m has odd factors) was fixed with the coprime step; it did not change the average rate at these sizes but "
            "slowed Bloom queries by 3–4×, an inference from the code change that was not profiled. "
            "(5) End-to-end over HTTP (loopback), the median round trip is 3–4 ms for small inputs and 15–16 ms at the input limits (400 clauses; n = 100,000); p95 ≈ 25–30 ms in every case, which points to connection overhead on this machine rather than compute.")

    # 7
    heading(d, "7. Demonstration and error handling")
    para(d, "The UI (screenshots in `docs/cia/evidence`) opens on one workflow with five synthetic examples: verified, “looks standard but is not” (a Bloom false positive that exact comparison rejects), appears twice (`AMBIGUOUS`), "
            "Hindi verified, and Hindi “part of a syllable” (`दाव` inside `दावे`, rejected as `MISALIGNED`). `data/samples` holds 21 request/response pairs; `replay_samples.py --check` re-runs all of them (21 of 21 match). "
            "Seven invalid-input samples return HTTP 400 problem responses (empty or over-long quotation, missing field, malformed JSON, p = 0.9, 501 probes, empty KMP pattern); case near-misses return a hint but never a match; "
            "duplicate members are counted separately from distinct ones; a filter pushed past capacity reports `overCapacity` (fill 100 %, expected rate 73.6 %).")

    # 8
    heading(d, "8. Limitations")
    bullets(d, [
        "**Exact matching only:** one changed character (case, curly quotes, OCR noise, a different Unicode encoding) means “not found”; the UI hints but never matches. Paraphrase is not verification.",
        "**A verified quotation proves presence, not meaning**, applicability, validity, authenticity or currency of the document; identical wording is not semantic or legal equivalence.",
        "**The boundary check is conservative, not full Unicode grapheme segmentation** (UAX #29); uncovered scripts are rejected rather than guessed.",
        "**The reference library is synthetic and small** (477 entries): an exact set alone would be adequate at this size; the Bloom filter is shown for the memory case, where a large remote exact store makes “definitely absent” valuable.",
        "**Bloom filter:** no deletion; rate rises beyond design capacity; the hash is not cryptographic (acceptable only because hits are always confirmed); not thread-safe; the textbook formula is approximate for this probe scheme.",
        "**Benchmarks:** one laptop, short JMH runs (KMP confidence intervals are wide: differences under about 2× are not established); `HashSet` memory is measured by heap delta; synthetic ASCII text only; one key distribution for the Bloom experiment.",
        "**Not covered:** a suffix-array variant for many quotes per document; an automated browser test (the UI was checked manually); production use (the wider platform uses standard-library adapters).",
    ], size=10)

    # 9
    heading(d, "9. Individual contributions and AI-assistance disclosure")
    para(d, "**Project type:** individual. **Student:** Mohak Mandwani ([[<REGISTER-NO>]]), sole student contributor and owner of all decisions. The student states that the faculty approved individual treatment and AI assistance "
            "(DECISION_LOG D-31); **no written faculty confirmation is held in the repository** and the disclosure form the faculty may require has not been specified.")
    bullets(d, [
        "**Student:** chose the problem and the scope of this graded slice within a wider product idea; selected the two algorithms and the exact-confirmation design; made and recorded scope and authorisation decisions (D-10 to D-32); reviewed and accepted each stage (P0 verification, pull requests 1 and 2); ran the builds, tests and benchmarks on a personal laptop; prepares the viva.",
        "**AI assistance (Claude, Anthropic, via Claude Code, directed by the student):** drafted the research and architecture documents; assisted in implementing the assessed algorithms, their tests, the benchmark harness, the demo backend and UI, the sample data and scripts, and drafted this report and the proposal from the repository’s own records and measured results.",
        "**Attribution:** every git commit carries the student’s identity only; AI tools are not listed as co-authors (project rule, ADR-0008). That rule concerns metadata and is **not** a statement of who wrote each line; this section and `docs/cia/CONTRIBUTIONS.md` are the disclosure.",
    ], size=10)
    para(d, "[[Student to confirm before submission: edit this section so it states exactly what you personally designed, wrote and ran, and use the faculty’s disclosure wording if one exists.]]", size=9.5)
    para(d, "**Third-party material:** Spring Boot, JUnit, AssertJ, ArchUnit and JMH (infrastructure, tests and measurement only; manifest in `docs/cia/DEPENDENCIES.md`); public-domain MurmurHash3 finaliser constants. No real data.")

    heading(d, "10. Reproducibility")
    para(d, f"Everything is reproducible from {REPO} with JDK 21 and, optionally, Python 3 (no database, no secrets, no environment variables required). "
            "From a clean clone: `./gradlew build` (all modules, tests and boundary checks), "
            "`./gradlew :cia:cia-demo:bootRun` then http://127.0.0.1:8090/ for the application, "
            "`python ops/scripts/replay_samples.py --check` to replay the 21 samples, and the three `./gradlew :cia:cia-bench:…` commands in README section 9 to regenerate the benchmarks. "
            "The README lists prerequisites, optional overrides (`SERVER_PORT`), the API with curl and PowerShell examples, and troubleshooting; "
            "`docs/cia/SUBMISSION_CHECKLIST.md` maps each deliverable and rubric item to its evidence.", size=10.5)

    heading(d, "References")
    para(d, "[1] D. Knuth, J. Morris, V. Pratt, “Fast pattern matching in strings”, SIAM J. Computing 6(2), 1977. "
            "[2] B. H. Bloom, “Space/time trade-offs in hash coding with allowable errors”, CACM 13(7), 1970. "
            "[3] A. Kirsch, M. Mitzenmacher, “Less hashing, same performance: building a better Bloom filter”, Random Structures & Algorithms 33(2), 2008. "
            "[4] T. Cormen et al., Introduction to Algorithms, ch. 32 (string matching). [5] A. Appleby, MurmurHash3 (public domain). [6] CIA-3 Advanced Algorithm Project Guidelines 2026–27, ATLAS–uGDX.", size=9, after=0)
    return d


if __name__ == "__main__":
    proposal().save(HERE / "A_Project_Proposal.docx")
    report().save(HERE / "C_Technical_Report.docx")
    print("wrote A_Project_Proposal.docx and C_Technical_Report.docx")
