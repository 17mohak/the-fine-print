package com.thefineprint.cia.demo.evidence;

import com.thefineprint.cia.demo.evidence.EvidenceCheckResponse.BloomVerdict;
import com.thefineprint.cia.demo.evidence.EvidenceCheckResponse.ClauseScreen;
import com.thefineprint.cia.demo.evidence.EvidenceCheckResponse.Evidence;
import com.thefineprint.cia.demo.evidence.EvidenceCheckResponse.ExactVerdict;
import com.thefineprint.cia.demo.evidence.EvidenceCheckResponse.Occurrence;
import com.thefineprint.cia.demo.evidence.EvidenceCheckResponse.Part;
import com.thefineprint.cia.demo.evidence.EvidenceCheckResponse.QuoteVerdict;
import com.thefineprint.cia.demo.evidence.EvidenceCheckResponse.Screening;
import com.thefineprint.cia.demo.evidence.EvidenceCheckResponse.Verification;
import com.thefineprint.cia.demo.evidence.ReferenceClauseCorpus.ReferenceClause;
import com.thefineprint.core.api.model.MatchClass;
import com.thefineprint.core.api.spi.QuoteVerifier;
import com.thefineprint.core.api.text.Span;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * The evidence check: one workflow in two stages.
 *
 * <ol>
 *   <li><b>Verify.</b> Find every exact occurrence of the claimed quotation through the {@link QuoteVerifier} SPI
 *       (backed by the assessed KMP matcher). Accept an occurrence only if both of its boundaries pass the
 *       conservative {@link BoundaryCheck}. Classify the result with the {@code core-api} {@link MatchClass}.</li>
 *   <li><b>Screen.</b> Segment the source into clauses and screen each clause's exact wording against the
 *       {@link ReferenceClauseLibrary}: Bloom filter first, then exact comparison for every possible hit.</li>
 * </ol>
 * The response ends with a three-part statement: what is verified, what is screened, and what is not established.
 * Stateless: nothing from a request is kept.
 */
@Service
public class EvidenceCheckService {

    static final int MAX_SOURCE_CODE_POINTS = 100_000;
    static final int MAX_QUOTATION_CODE_POINTS = 2_000;
    static final int MAX_CLAUSES = 400;

    static final List<String> NOT_ESTABLISHED = List.of(
            "What the quoted text means, or how it should be interpreted.",
            "Whether it applies to any particular person, claim or situation.",
            "Whether the clause is legally valid or enforceable.",
            "Whether the source document is authentic and unaltered, or who issued it.",
            "Whether the source is the current version in force.");

    static final String EQUIVALENCE_CAVEAT = "That identical reference wording has the same meaning or effect in "
            + "this document: identical wording is not semantic or legal equivalence, and the reference library is "
            + "synthetic, not an approved standard.";

    static final List<String> METHOD = List.of(
            "Stage 1 calls the QuoteVerifier contract, implemented here by the assessed KMP matcher "
                    + "(KmpQuoteVerifier). Text is matched exactly as entered: no Unicode normalisation, case folding "
                    + "or whitespace folding.",
            "Each occurrence's start and end are checked conservatively for combining marks, viramas (conjuncts), "
                    + "joiners and CR LF, in Latin, Greek, Cyrillic and nine Indic scripts. This is not full Unicode "
                    + "grapheme segmentation. An occurrence whose boundaries cannot be established is not accepted.",
            "Clauses are the non-blank lines of the source; a leading number such as 4.2 becomes the clause id "
                    + "(a simple heuristic).",
            "Stage 2 fingerprints each clause's exact wording (SHA-256), screens the fingerprint with the assessed "
                    + "Bloom filter, and confirms every possible hit by comparing the wording itself.",
            "The reference library is synthetic and generated from invented templates (docs/cia/DATA.md).");

    private final QuoteVerifier quoteVerifier;
    private final ReferenceClauseLibrary library;

    public EvidenceCheckService(QuoteVerifier quoteVerifier, ReferenceClauseLibrary library) {
        this.quoteVerifier = Objects.requireNonNull(quoteVerifier, "quoteVerifier");
        this.library = Objects.requireNonNull(library, "library");
    }

    public EvidenceCheckResponse check(EvidenceCheckRequest request) {
        validate(request);
        String source = request.sourceText();
        String quotation = request.claimedQuotation();
        int[] text = source.codePoints().toArray();
        int quotationLength = quotation.codePointCount(0, quotation.length());
        List<ClauseSegmenter.Clause> clauses = ClauseSegmenter.segment(text);
        if (clauses.size() > MAX_CLAUSES) {
            throw new IllegalArgumentException("the source is limited to " + MAX_CLAUSES + " clauses (non-blank lines)");
        }

        // Stage 1: verify.
        List<Occurrence> occurrences = new ArrayList<>();
        List<Occurrence> accepted = new ArrayList<>();
        for (Span span : quoteVerifier.find(source, quotation)) {
            List<String> issues = BoundaryCheck.issues(text, span.start(), span.end());
            List<String> clauseIds = clauses.stream().filter(c -> c.overlaps(span.start(), span.end()))
                    .map(ClauseSegmenter.Clause::id).toList();
            Occurrence occurrence = new Occurrence(span.start(), span.end(), issues.isEmpty(), issues, clauseIds);
            occurrences.add(occurrence);
            if (issues.isEmpty()) {
                accepted.add(occurrence);
            }
        }
        QuoteVerdict verdict;
        MatchClass matchClass;
        if (accepted.size() == 1) {
            verdict = QuoteVerdict.VERIFIED;
            matchClass = MatchClass.EXACT_UNIQUE;
        } else if (accepted.size() > 1) {
            verdict = QuoteVerdict.AMBIGUOUS;
            matchClass = MatchClass.AMBIGUOUS;
        } else if (!occurrences.isEmpty()) {
            verdict = QuoteVerdict.BOUNDARY_NOT_ESTABLISHED;
            matchClass = MatchClass.MISALIGNED;
        } else {
            verdict = QuoteVerdict.NOT_FOUND;
            matchClass = null;
        }
        List<String> nearMisses = occurrences.isEmpty() ? nearMisses(source, quotation) : List.of();
        Verification verification = new Verification(verdict, matchClass, "Knuth-Morris-Pratt (QuoteVerifier SPI)",
                text.length, quotationLength, List.copyOf(occurrences), nearMisses);

        // Stage 2: screen every clause; mark those holding an accepted occurrence.
        List<ClauseScreen> screens = new ArrayList<>();
        int absent = 0;
        int confirmed = 0;
        int falsePositives = 0;
        for (ClauseSegmenter.Clause clause : clauses) {
            boolean quoted = accepted.stream().anyMatch(o -> o.clauseIds().contains(clause.id())
                    && clause.overlaps(o.start(), o.end()));
            ReferenceClauseLibrary.Outcome outcome = library.screen(clause.wording());
            ClauseScreen screen = switch (outcome.screen()) {
                case DEFINITELY_ABSENT -> {
                    absent++;
                    yield new ClauseScreen(clause.id(), quoted, BloomVerdict.DEFINITELY_ABSENT,
                            ExactVerdict.NOT_NEEDED, null, null, null);
                }
                case EXACT_MATCH_CONFIRMED -> {
                    confirmed++;
                    ReferenceClause ref = outcome.reference().orElseThrow();
                    yield new ClauseScreen(clause.id(), quoted, BloomVerdict.POSSIBLY_PRESENT,
                            ExactVerdict.IDENTICAL_WORDING, ref.id(), ref.domain(), ref.title());
                }
                case FALSE_POSITIVE_REJECTED -> {
                    falsePositives++;
                    yield new ClauseScreen(clause.id(), quoted, BloomVerdict.POSSIBLY_PRESENT,
                            ExactVerdict.FALSE_POSITIVE, null, null, null);
                }
            };
            screens.add(screen);
        }
        Screening screening = new Screening(libraryInfo(), List.copyOf(screens), absent, confirmed + falsePositives,
                confirmed, falsePositives);

        List<EvidenceCheckResponse.Clause> clauseViews = clauses.stream()
                .map(c -> new EvidenceCheckResponse.Clause(c.id(), c.start(), c.end(), c.wording())).toList();
        return new EvidenceCheckResponse(verification, screening, evidence(verification, accepted, screens),
                clauseViews, METHOD);
    }

    private static void validate(EvidenceCheckRequest request) {
        if (request == null || request.sourceText() == null || request.claimedQuotation() == null) {
            throw new IllegalArgumentException("sourceText and claimedQuotation are required");
        }
        if (request.claimedQuotation().isEmpty()) {
            throw new IllegalArgumentException("claimedQuotation must not be empty");
        }
        String source = request.sourceText();
        if (source.codePointCount(0, source.length()) > MAX_SOURCE_CODE_POINTS) {
            throw new IllegalArgumentException("sourceText is limited to " + MAX_SOURCE_CODE_POINTS + " characters");
        }
        String quotation = request.claimedQuotation();
        if (quotation.codePointCount(0, quotation.length()) > MAX_QUOTATION_CODE_POINTS) {
            throw new IllegalArgumentException(
                    "claimedQuotation is limited to " + MAX_QUOTATION_CODE_POINTS + " characters");
        }
    }

    /** Hints only. Each re-runs the same SPI on transformed copies; neither result is ever evidence. */
    private List<String> nearMisses(String source, String quotation) {
        List<String> hints = new ArrayList<>();
        String nfcSource = Normalizer.normalize(source, Normalizer.Form.NFC);
        String nfcQuotation = Normalizer.normalize(quotation, Normalizer.Form.NFC);
        if (!nfcQuotation.isEmpty() && !quoteVerifier.find(nfcSource, nfcQuotation).isEmpty()) {
            hints.add("The quotation would match after Unicode NFC normalisation: the source and the quotation "
                    + "encode some characters differently (for example a precomposed letter versus a letter plus a "
                    + "combining accent). This check does not normalise, so it is not a match.");
        }
        String lowerSource = source.toLowerCase(Locale.ROOT);
        String lowerQuotation = quotation.toLowerCase(Locale.ROOT);
        if (!lowerQuotation.equals(quotation) || !lowerSource.equals(source)) {
            if (!lowerQuotation.isEmpty() && !quoteVerifier.find(lowerSource, lowerQuotation).isEmpty()) {
                hints.add("The quotation would match if letter case were ignored. Exact matching is case-sensitive, "
                        + "so it is not a match.");
            }
        }
        return List.copyOf(hints);
    }

    private EvidenceCheckResponse.Library libraryInfo() {
        var p = library.parameters();
        return new EvidenceCheckResponse.Library(library.entries().size(), p.bitCount(), p.hashCount(),
                p.targetFalsePositiveRate(), library.expectedFalsePositiveRate(), library.fillRatio(),
                library.bloomBytes(), ReferenceClauseLibrary.SEED);
    }

    private static Evidence evidence(Verification v, List<Occurrence> accepted, List<ClauseScreen> screens) {
        long rejected = v.occurrences().size() - accepted.size();
        String rejectedNote = rejected == 0 ? null : rejected + " further occurrence" + (rejected == 1 ? " was" : "s were")
                + " rejected because " + (rejected == 1 ? "its" : "their")
                + " character boundaries could not be established.";

        List<String> verifiedStatements = new ArrayList<>();
        String verifiedStatus;
        switch (v.verdict()) {
            case VERIFIED -> {
                Occurrence o = accepted.get(0);
                verifiedStatus = "Occurs exactly once";
                verifiedStatements.add("The quotation occurs exactly once in the source, at code points ["
                        + o.start() + ", " + o.end() + ")" + inClauses(o.clauseIds()) + ".");
            }
            case AMBIGUOUS -> {
                verifiedStatus = "Occurs more than once";
                verifiedStatements.add("The quotation occurs " + accepted.size() + " times"
                        + inClauses(accepted.stream().flatMap(o -> o.clauseIds().stream()).distinct().toList())
                        + ". Which occurrence is meant cannot be established, so it is not treated as a uniquely "
                        + "verified quotation.");
            }
            case BOUNDARY_NOT_ESTABLISHED -> {
                verifiedStatus = "Not verified";
                verifiedStatements.add("The quotation's code points occur in the source, but no occurrence starts and "
                        + "ends on established character boundaries (for example, it would cut a consonant from its "
                        + "vowel sign). It is not presented as verified.");
            }
            default -> {
                verifiedStatus = "Not found";
                verifiedStatements.add("The source does not contain this exact quotation.");
                verifiedStatements.addAll(v.nearMisses());
            }
        }
        if (rejectedNote != null && !accepted.isEmpty()) {
            verifiedStatements.add(rejectedNote);
        }

        List<ClauseScreen> quoted = screens.stream().filter(ClauseScreen::containsQuotation).toList();
        List<String> screenedStatements = new ArrayList<>();
        String screenedStatus;
        if (quoted.isEmpty()) {
            screenedStatus = "Not applicable";
            screenedStatements.add("No clause is attributed to the quotation, so no clause wording was screened on its "
                    + "behalf. Every clause is still screened in stage 2.");
        } else {
            for (ClauseScreen s : quoted) {
                screenedStatements.add(switch (s.exactVerdict()) {
                    case IDENTICAL_WORDING -> "Clause " + s.clauseId() + ": its wording is identical, character for "
                            + "character, to reference entry " + s.referenceId() + " (" + s.referenceDomain() + " · "
                            + s.referenceTitle() + "). The Bloom filter's \"possibly present\" was confirmed by exact "
                            + "comparison.";
                    case FALSE_POSITIVE -> "Clause " + s.clauseId() + ": the Bloom filter answered \"possibly present\", "
                            + "but exact comparison found no identical reference wording. That was a false positive "
                            + "and is not a match.";
                    case NOT_NEEDED -> "Clause " + s.clauseId() + ": the Bloom filter proved its wording is not in the "
                            + "reference library, so no exact lookup was needed.";
                });
            }
            long identical = quoted.stream().filter(s -> s.exactVerdict() == ExactVerdict.IDENTICAL_WORDING).count();
            screenedStatus = identical == quoted.size() ? "Identical reference wording"
                    : identical == 0 ? "No identical reference wording" : "Partly identical reference wording";
        }

        List<String> notEstablished = new ArrayList<>(NOT_ESTABLISHED);
        if (screens.stream().anyMatch(s -> s.exactVerdict() == ExactVerdict.IDENTICAL_WORDING)) {
            notEstablished.add(EQUIVALENCE_CAVEAT);
        }
        return new Evidence(new Part(verifiedStatus, List.copyOf(verifiedStatements)),
                new Part(screenedStatus, List.copyOf(screenedStatements)), List.copyOf(notEstablished));
    }

    private static String inClauses(List<String> ids) {
        if (ids.isEmpty()) {
            return "";
        }
        return (ids.size() == 1 ? ", in clause " : ", in clauses ") + String.join(", ", ids);
    }
}
