package com.thefineprint.cia.demo.evidence;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A <b>conservative</b> check that a matched span does not start or end inside a user-perceived character.
 *
 * <p>Exact matching works on code points. A code-point match can still begin or end in the middle of what a reader
 * sees as one character: between a Devanagari consonant and its vowel sign, inside a virama conjunct, or inside a
 * joiner sequence. Such a match must not be presented as a verified quotation.
 *
 * <p><b>What this is not.</b> It is <i>not</i> Unicode UAX #29 grapheme segmentation, and it does not implement
 * Indic syllable rules. The platform specification requires a pinned segmenter for that (SYSTEM_DESIGN 2.1,
 * D-26); this demo has none and adds no dependency. Instead each boundary gets one of three answers:
 * <ul>
 *   <li>{@link Status#SAFE}: both neighbours are in scripts this check covers and no joining rule applies;</li>
 *   <li>{@link Status#SPLITS_CHARACTER}: a rule shows the boundary falls inside a character sequence;</li>
 *   <li>{@link Status#CANNOT_ESTABLISH}: a neighbour is outside the covered scripts, or the case needs
 *       context this check does not model. This is treated exactly like a split: not verified.</li>
 * </ul>
 * Covered scripts: Latin, Greek, Cyrillic, Common, Inherited, and the nine major Indic scripts (Devanagari,
 * Bengali, Gurmukhi, Gujarati, Oriya, Tamil, Telugu, Kannada, Malayalam). Everything else is "cannot establish".
 */
final class BoundaryCheck {

    /** Outcome for one boundary. */
    enum Status {
        SAFE,
        SPLITS_CHARACTER,
        CANNOT_ESTABLISH
    }

    /** One boundary's outcome with a plain-language reason (empty when safe). */
    record Finding(Status status, String reason) {
    }

    private static final int ZWNJ = 0x200C;
    private static final int ZWJ = 0x200D;

    /** Viramas (halants) of the covered Indic scripts: they join the consonants on either side into a conjunct. */
    private static final Set<Integer> VIRAMAS = Set.of(
            0x094D, 0x09CD, 0x0A4D, 0x0ACD, 0x0B4D, 0x0BCD, 0x0C4D, 0x0CCD, 0x0D4D);

    /** Prepend characters in covered scripts (they attach to the character that follows). */
    private static final Set<Integer> PREPEND = Set.of(0x0D4E);

    private static final Set<Character.UnicodeScript> COVERED = Set.of(
            Character.UnicodeScript.COMMON, Character.UnicodeScript.INHERITED,
            Character.UnicodeScript.LATIN, Character.UnicodeScript.GREEK, Character.UnicodeScript.CYRILLIC,
            Character.UnicodeScript.DEVANAGARI, Character.UnicodeScript.BENGALI, Character.UnicodeScript.GURMUKHI,
            Character.UnicodeScript.GUJARATI, Character.UnicodeScript.ORIYA, Character.UnicodeScript.TAMIL,
            Character.UnicodeScript.TELUGU, Character.UnicodeScript.KANNADA, Character.UnicodeScript.MALAYALAM);

    private BoundaryCheck() {
    }

    /**
     * Problems with the boundaries of span {@code [start, end)} of {@code text}; empty when both are safe.
     * Each problem names the boundary, its code-point offset and the reason.
     */
    static List<String> issues(int[] text, int start, int end) {
        List<String> issues = new ArrayList<>(2);
        Finding atStart = at(text, start);
        if (atStart.status() != Status.SAFE) {
            issues.add("start (offset " + start + "): " + atStart.reason());
        }
        Finding atEnd = at(text, end);
        if (atEnd.status() != Status.SAFE) {
            issues.add("end (offset " + end + "): " + atEnd.reason());
        }
        return List.copyOf(issues);
    }

    /** Classifies the boundary between {@code text[b - 1]} and {@code text[b]}. */
    static Finding at(int[] text, int b) {
        if (b <= 0 || b >= text.length) {
            return new Finding(Status.SAFE, "");
        }
        int before = text[b - 1];
        int after = text[b];
        if (before == '\r' && after == '\n') {
            return split("it would split a CR LF line break");
        }
        if (isCombiningMark(after)) {
            return split("it would separate " + describe(after) + " from the character it combines with");
        }
        if (after == ZWJ || after == ZWNJ || isEmojiModifier(after) || isTag(after)) {
            return split("it would split a joiner or modifier sequence at " + describe(after));
        }
        if (before == ZWJ) {
            return split("it would split a joiner sequence after " + describe(before));
        }
        if (VIRAMAS.contains(before) && Character.isLetter(after)) {
            return split("it would split a conjunct: " + describe(before) + " joins it to " + describe(after));
        }
        if (PREPEND.contains(before)) {
            return split("it would separate " + describe(before) + " from the character it attaches to");
        }
        if (isRegionalIndicator(before) && isRegionalIndicator(after)) {
            return new Finding(Status.CANNOT_ESTABLISH,
                    "flag (regional-indicator) pairing is not determined by this check");
        }
        Character.UnicodeScript scriptBefore = Character.UnicodeScript.of(before);
        Character.UnicodeScript scriptAfter = Character.UnicodeScript.of(after);
        if (!COVERED.contains(scriptBefore) || !COVERED.contains(scriptAfter)) {
            Character.UnicodeScript uncovered = COVERED.contains(scriptBefore) ? scriptAfter : scriptBefore;
            return new Finding(Status.CANNOT_ESTABLISH, "the neighbouring text is in a script ("
                    + uncovered.name().toLowerCase(Locale.ROOT) + ") whose character boundaries this check does not cover");
        }
        return new Finding(Status.SAFE, "");
    }

    private static Finding split(String reason) {
        return new Finding(Status.SPLITS_CHARACTER, reason);
    }

    private static boolean isCombiningMark(int cp) {
        int type = Character.getType(cp);
        return type == Character.NON_SPACING_MARK || type == Character.COMBINING_SPACING_MARK
                || type == Character.ENCLOSING_MARK;
    }

    private static boolean isEmojiModifier(int cp) {
        return cp >= 0x1F3FB && cp <= 0x1F3FF;
    }

    private static boolean isTag(int cp) {
        return cp >= 0xE0020 && cp <= 0xE007F;
    }

    private static boolean isRegionalIndicator(int cp) {
        return cp >= 0x1F1E6 && cp <= 0x1F1FF;
    }

    /** "U+093F DEVANAGARI VOWEL SIGN I". */
    static String describe(int cp) {
        String name = Character.getName(cp);
        return String.format(Locale.ROOT, "U+%04X%s", cp, name == null ? "" : " " + name);
    }
}
