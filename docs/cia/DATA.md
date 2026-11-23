# Data used by the CIA demonstration and benchmarks

Everything here is **synthetic**. No real policy, loan, tenancy, subscription, warranty or personal document is used
anywhere in Track C. No wording is presented as a real standard clause or as approved by any regulator or issuer.

## 1. Reference clause library (`cia-demo`, evidence check stage 2)

**Source:** `cia/cia-demo/src/main/java/com/thefineprint/cia/demo/evidence/ReferenceClauseCorpus.java`, generated
deterministically at start-up (no file, no randomness).

**Construction.** 20 invented templates, each a clause wording with placeholders, are expanded over every
combination of their parameter values (cartesian product, in a fixed order):

| Domain | Templates | Example wording (one combination) |
|---|---|---|
| Insurance | Notice of claim; Grace period; Waiting period; Free look period; Renewal; Cashless facility | `Notice of claim. The Policyholder shall give written notice of any claim within 30 days of discharge.` |
| Lending | Prepayment; Late payment; Cooling-off period | `Late payment. A penal charge of 2 per cent per month is levied on any instalment overdue by more than 30 days.` |
| Tenancy | Security deposit; Notice to vacate; Rent revision | `Notice to vacate. Either party may terminate this agreement by giving 2 months' written notice.` |
| Subscriptions | Cancellation; Auto-renewal; Refunds | `Auto-renewal. The subscription renews automatically unless cancelled at least 7 days before the renewal date.` |
| Warranties | Warranty period; Warranty claims | `Claims. A warranty claim must be made within 30 days of the defect becoming apparent.` |
| Insurance (Hindi) | दावे की सूचना; नवीनीकरण | `बीमित व्यक्ति को दावे की सूचना 30 दिनों के भीतर देनी होगी।` |
| Tenancy (Hindi) | जमानत राशि | `किरायेदार के परिसर खाली करने के 30 दिनों के भीतर 2 महीने के किराये की जमानत राशि लौटाई जाएगी।` |

- **Ids:** `<TEMPLATE-CODE>-<combination index>`, for example `INS-CLAIM-NOTICE-025`.
- **Held-out variants:** combination `i` of each template is **left out of the library** when `i % 5 == 3`. These
  are natural-looking wordings known *not* to be members. They are used to show "definitely absent" answers and to
  test that a non-member never matches.
- **Sizes:** 595 combinations; **477 library entries** and **118 held-out variants**. All wordings are distinct.
  `ReferenceClauseLibraryTest` asserts both sizes, so this document and the code must change together.
- **Matching rule:** a clause matches a reference entry only if its wording is identical code point for code point
  (no case, whitespace, punctuation or Unicode normalisation folding).

**The Bloom filter over the library.** Each wording's SHA-256 digest (of its UTF-8 bytes) is added to the
assessed `BloomFilter`, sized with `BloomParameters.optimal(477, 0.01)` and seed 7: m = 4,573 bits (576 bytes),
k = 7, theoretical false-positive rate at 477 distinct members of about 1 %. The exact store maps digests to
entries and the wording itself is compared on every possible hit, so even a digest collision could not produce a
false match.

**Honest scale note.** 477 entries is small: an exact in-memory set alone would be entirely adequate at this size.
The Bloom prescreen earns its place when the reference collection is large and the exact store is remote (a
database or service), because every "definitely absent" answer avoids a lookup. The demo shows that effect as a
count of exact lookups performed versus avoided. The memory comparison at larger sizes is in
[BENCHMARKS.md](BENCHMARKS.md).

**A pinned false positive.** With this library and seed, the wording
`Grace period. A grace period of 17 days is allowed for payment of the renewal premium.` (not in the library: it
holds 15 and 21 days) gets a Bloom "possibly present", and exact comparison rejects it. The UI's "Looks standard,
but is not" example uses it. `EvidenceCheckServiceTest.uiNearVariantExampleIsAResolvedFalsePositive` pins this.

## 2. Evidence-check examples (`cia-demo` UI)

Defined in `cia/cia-demo/src/main/resources/static/app.js` (`EVIDENCE_EXAMPLES`):

| Example | Source | Quotation | Expected result |
|---|---|---|---|
| Verified quotation | English schedule, clauses 4.1–4.5 | `The Policyholder shall give written notice of any claim within 30 days` | `EXACT_UNIQUE` in 4.2; 4.2's wording identical to `INS-CLAIM-NOTICE-025` |
| Looks standard, but is not | same | `A grace period of 17 days is allowed` | `EXACT_UNIQUE` in 4.1; Bloom "possibly", exact comparison: false positive |
| Appears twice | same | `renewal` | `AMBIGUOUS` (clauses 4.1 and 4.3) |
| हिन्दी: verified | Hindi schedule, clauses 5.1–5.2 | `दावे की सूचना 30 दिनों के भीतर` | `EXACT_UNIQUE` in 5.1; wording identical to a `HI-INS-CLAIM-NOTICE-*` entry |
| हिन्दी: part of a syllable | same | `दाव` (without the vowel sign े) | `MISALIGNED`: the only occurrence ends before U+0947 |

Clause 4.4 of the English schedule is a held-out variant (36 months); 4.5 (cashless treatment "listed in the
Annexure") matches no template.

## 3. Benchmark and experiment inputs (`cia-bench`)

- **KMP:** `SyntheticText` — seeded prose from a 32-word invented vocabulary in agreement style (seed 1); patterns
  are excerpts at a seeded position (seed 2). Adversarial input: text `a…a`, pattern `a…ab`.
- **Bloom:** keys are 64-bit integers encoded as 8 big-endian bytes; inserted keys `0..n−1`, negative probes from
  `1,000,000,000` upward; seeds 7, 11, 13, 17 and 19.

Details and limitations: [BENCHMARKS.md](BENCHMARKS.md).

## 4. Validation of inputs (`cia-demo`)

| Endpoint | Limits | Rejected with 400 (problem+json) |
|---|---|---|
| `/algo/evidence-check` | source ≤ 100,000 code points and ≤ 400 clauses (non-blank lines); quotation 1–2,000 code points | missing fields, empty quotation, over-limit input, malformed JSON |
| `/algo/quote-match` | text ≤ 100,000; quote 1–2,000 code points | as above |
| `/algo/membership` | ≤ 5,000 members, 1–500 probes, each 1–200 characters; p in [0.0001, 0.5]; capacity 1–1,000,000 | as above |

Error messages are our own validation texts; submitted text is never echoed back.
