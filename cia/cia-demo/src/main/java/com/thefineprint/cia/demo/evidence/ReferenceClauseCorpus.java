package com.thefineprint.cia.demo.evidence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A deterministic, <b>synthetic</b> corpus of reference clause wordings, generated from invented templates.
 *
 * <p>No wording is copied from any real policy, loan, tenancy, subscription or warranty document, and no entry is
 * presented as a real standard clause or as approved by any regulator. The corpus exists so that the Bloom-filter
 * stage screens realistic clause text rather than identifiers.
 *
 * <p>Each template is expanded over every combination of its parameters (the cartesian product, in order).
 * Combination {@code i} of a template is <b>held out</b> of the library when {@code i % 5 == 3}. Held-out
 * variants are natural-looking wordings that are known <i>not</i> to be in the library, which is useful for
 * demonstrating "definitely absent" answers and false positives. Documented in {@code docs/cia/DATA.md}.
 */
final class ReferenceClauseCorpus {

    /** One reference entry. {@code wording} is exactly what a clause must equal, symbol for symbol, to match. */
    record ReferenceClause(String id, String domain, String title, String wording) {
    }

    /** The generated corpus: library entries and held-out (never inserted) variants. */
    record Generated(List<ReferenceClause> library, List<ReferenceClause> heldOut) {
    }

    private record Template(String code, String domain, String title, String pattern, List<List<String>> params) {
    }

    private static final List<String> DAYS = List.of("7", "10", "14", "15", "21", "30", "45", "60", "90", "120");
    private static final List<String> PCT = List.of("0", "1", "2", "2.5", "3", "4", "5");
    private static final List<String> HOLDERS = List.of("The Policyholder", "The Insured Person", "The Proposer");
    private static final List<String> BORROWERS = List.of("The Borrower", "The Borrowers");

    private static final List<Template> TEMPLATES = List.of(
            new Template("INS-CLAIM-NOTICE", "Insurance", "Notice of claim",
                    "Notice of claim. {0} shall give written notice of any claim within {1} days of {2}.",
                    List.of(HOLDERS, DAYS, List.of("discharge", "the loss", "the incident", "the date of admission",
                            "the event giving rise to the claim"))),
            new Template("INS-GRACE", "Insurance", "Grace period",
                    "Grace period. A grace period of {0} days is allowed for payment of the {1} premium.",
                    List.of(DAYS, List.of("renewal", "instalment", "annual", "monthly"))),
            new Template("INS-WAITING", "Insurance", "Waiting period",
                    "Waiting period. {0} are covered after {1} months of continuous coverage.",
                    List.of(List.of("Pre-existing conditions", "Pre-existing diseases", "Named ailments",
                            "Maternity expenses"), List.of("9", "12", "24", "36", "48"))),
            new Template("INS-FREE-LOOK", "Insurance", "Free look period",
                    "Free look period. {0} may cancel this policy within {1} days of receipt of the policy document.",
                    List.of(HOLDERS, DAYS)),
            new Template("INS-RENEWAL", "Insurance", "Renewal",
                    "Renewal. {0} may decline renewal only on grounds of {1}.",
                    List.of(List.of("The Insurer", "The Company"), List.of("fraud or non-disclosure",
                            "fraud, misrepresentation or non-disclosure", "fraud",
                            "non-co-operation by the Insured Person"))),
            new Template("INS-CASHLESS", "Insurance", "Cashless facility",
                    "Cashless facility. Cashless treatment is available only at {0} listed on {1}.",
                    List.of(List.of("network hospitals", "empanelled hospitals"),
                            List.of("the Insurer's website", "the Company's website", "the policy schedule"))),
            new Template("LN-PREPAYMENT", "Lending", "Prepayment",
                    "Prepayment. {0} may prepay the loan after {1} months subject to a charge of {2} per cent of "
                            + "the principal outstanding.",
                    List.of(BORROWERS, List.of("6", "12", "18", "24", "36"), PCT)),
            new Template("LN-LATE-PAYMENT", "Lending", "Late payment",
                    "Late payment. A penal charge of {0} per cent per month is levied on any instalment overdue by "
                            + "more than {1} days.",
                    List.of(List.of("1", "1.5", "2", "2.5", "3"), DAYS)),
            new Template("LN-COOLING-OFF", "Lending", "Cooling-off period",
                    "Cooling-off period. {0} may exit the loan within {1} days of disbursement by repaying the "
                            + "principal and proportionate interest.",
                    List.of(BORROWERS, List.of("1", "3", "5", "7", "10", "14", "15"))),
            new Template("TEN-DEPOSIT", "Tenancy", "Security deposit",
                    "Security deposit. The security deposit of {0} months' rent shall be refunded within {1} days of "
                            + "the Tenant vacating the premises.",
                    List.of(List.of("1", "2", "3", "6", "10"), DAYS)),
            new Template("TEN-NOTICE", "Tenancy", "Notice to vacate",
                    "Notice to vacate. Either party may terminate this agreement by giving {0} months' written notice.",
                    List.of(List.of("1", "2", "3", "6"))),
            new Template("TEN-RENT-REVISION", "Tenancy", "Rent revision",
                    "Rent revision. The rent shall be revised by {0} per cent on each renewal of this agreement.",
                    List.of(List.of("3", "4", "5", "6", "7.5", "8", "10", "12"))),
            new Template("SUB-CANCELLATION", "Subscriptions", "Cancellation",
                    "Cancellation. The Subscriber may cancel at any time; cancellation takes effect at the end of the "
                            + "current {0} billing cycle.",
                    List.of(List.of("weekly", "monthly", "quarterly", "annual"))),
            new Template("SUB-AUTO-RENEWAL", "Subscriptions", "Auto-renewal",
                    "Auto-renewal. The subscription renews automatically unless cancelled at least {0} {1} before the "
                            + "renewal date.",
                    List.of(List.of("1", "2", "3", "5", "7", "14", "30"), List.of("days", "working days"))),
            new Template("SUB-REFUND", "Subscriptions", "Refunds",
                    "Refunds. Fees paid for a {0} billing cycle are refundable within {1} days of payment.",
                    List.of(List.of("weekly", "monthly", "quarterly", "annual"), DAYS)),
            new Template("WAR-PERIOD", "Warranties", "Warranty period",
                    "Warranty period. The product is warranted against manufacturing defects for {0} months from the "
                            + "date of {1}.",
                    List.of(List.of("6", "12", "18", "24", "36", "60"),
                            List.of("purchase", "delivery", "installation", "invoice"))),
            new Template("WAR-CLAIM", "Warranties", "Warranty claims",
                    "Claims. A warranty claim must be made within {0} days of the defect becoming apparent.",
                    List.of(DAYS)),
            new Template("HI-INS-CLAIM-NOTICE", "Insurance (Hindi)", "दावे की सूचना",
                    "{0} को दावे की सूचना {1} दिनों के भीतर देनी होगी।",
                    List.of(List.of("बीमित व्यक्ति", "पॉलिसीधारक"), DAYS)),
            new Template("HI-INS-RENEWAL", "Insurance (Hindi)", "नवीनीकरण",
                    "पॉलिसी का नवीनीकरण केवल {0} के आधार पर अस्वीकार किया जा सकता है।",
                    List.of(List.of("धोखाधड़ी", "धोखाधड़ी या तथ्य छिपाने", "गलत जानकारी"))),
            new Template("HI-TEN-DEPOSIT", "Tenancy (Hindi)", "जमानत राशि",
                    "किरायेदार के परिसर खाली करने के {0} दिनों के भीतर {1} महीने के किराये की जमानत राशि लौटाई जाएगी।",
                    List.of(DAYS, List.of("1", "2", "3"))));

    private ReferenceClauseCorpus() {
    }

    static Generated generate() {
        Map<String, ReferenceClause> library = new LinkedHashMap<>();
        Map<String, ReferenceClause> heldOut = new LinkedHashMap<>();
        for (Template template : TEMPLATES) {
            List<List<String>> combinations = cartesian(template.params());
            for (int i = 0; i < combinations.size(); i++) {
                String wording = fill(template.pattern(), combinations.get(i));
                ReferenceClause clause = new ReferenceClause(
                        String.format(Locale.ROOT, "%s-%03d", template.code(), i), template.domain(),
                        template.title(), wording);
                (i % 5 == 3 ? heldOut : library).putIfAbsent(wording, clause);
            }
        }
        heldOut.keySet().removeAll(library.keySet());
        return new Generated(List.copyOf(library.values()), List.copyOf(heldOut.values()));
    }

    private static String fill(String pattern, List<String> values) {
        String out = pattern;
        for (int p = 0; p < values.size(); p++) {
            out = out.replace("{" + p + "}", values.get(p));
        }
        return out;
    }

    private static List<List<String>> cartesian(List<List<String>> params) {
        List<List<String>> result = new ArrayList<>();
        result.add(List.of());
        for (List<String> values : params) {
            List<List<String>> next = new ArrayList<>();
            for (List<String> prefix : result) {
                for (String value : values) {
                    List<String> combined = new ArrayList<>(prefix);
                    combined.add(value);
                    next.add(List.copyOf(combined));
                }
            }
            result = next;
        }
        return result;
    }
}
