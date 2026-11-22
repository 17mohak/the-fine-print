package com.thefineprint.cia.bench;

import java.util.Random;

/** Seeded synthetic inputs for the matching benchmarks. No real document text is used anywhere. */
public final class SyntheticText {

    /** A small invented vocabulary in the style of agreement wording. */
    private static final String[] WORDS = {
        "the", "insurer", "policy", "holder", "shall", "pay", "premium", "within", "days", "of", "notice",
        "renewal", "waiting", "period", "claim", "amount", "sum", "insured", "exclusion", "applies", "to", "any",
        "loan", "interest", "rate", "charges", "tenant", "deposit", "refund", "subscription", "cancel", "fee",
    };

    private SyntheticText() {
    }

    /** Ordinary prose-like text of exactly {@code length} ASCII characters. */
    public static String ordinary(int length, long seed) {
        Random random = new Random(seed);
        StringBuilder sb = new StringBuilder(length + 16);
        while (sb.length() < length) {
            sb.append(WORDS[random.nextInt(WORDS.length)]).append(random.nextInt(12) == 0 ? ". " : " ");
        }
        return sb.substring(0, length);
    }

    /** A substring of {@code text} of {@code length} characters taken from a seeded position (so it occurs). */
    public static String excerpt(String text, int length, long seed) {
        int start = new Random(seed).nextInt(text.length() - length);
        return text.substring(start, start + length);
    }

    /** Worst case for naive matching: a text of {@code n} 'a's. */
    public static String adversarialText(int n) {
        return "a".repeat(n);
    }

    /** Pattern of {@code m - 1} 'a's then 'b': every alignment almost matches, and none does. */
    public static String adversarialPattern(int m) {
        return "a".repeat(m - 1) + "b";
    }
}
