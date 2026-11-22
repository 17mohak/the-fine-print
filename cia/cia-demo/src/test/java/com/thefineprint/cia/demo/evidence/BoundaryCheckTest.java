package com.thefineprint.cia.demo.evidence;

import static org.assertj.core.api.Assertions.assertThat;

import com.thefineprint.cia.demo.evidence.BoundaryCheck.Status;
import org.junit.jupiter.api.Test;

/**
 * The boundary check must refuse every boundary that falls inside a character sequence it knows about, and must
 * answer "cannot establish" (never "safe") outside the scripts it covers.
 */
class BoundaryCheckTest {

    private static int[] cp(String s) {
        return s.codePoints().toArray();
    }

    /** Status of the boundary of the first occurrence of {@code quote} in {@code text}, at its start or end. */
    private static Status startOf(String text, String quote) {
        int start = text.codePointCount(0, text.indexOf(quote));
        return BoundaryCheck.at(cp(text), start).status();
    }

    private static Status endOf(String text, String quote) {
        int start = text.codePointCount(0, text.indexOf(quote));
        return BoundaryCheck.at(cp(text), start + quote.codePointCount(0, quote.length())).status();
    }

    @Test
    void textEdgesAndOrdinaryWordBoundariesAreSafe() {
        assertThat(BoundaryCheck.issues(cp("fee"), 0, 3)).isEmpty();
        assertThat(BoundaryCheck.issues(cp("late fee, fee"), 5, 8)).isEmpty();
        assertThat(BoundaryCheck.issues(cp("नियम और शर्तें"), 0, 4)).as("Devanagari word before a space").isEmpty();
    }

    @Test
    void consonantSeparatedFromItsVowelSignIsASplit() {
        // The discovered defect: "न" matched inside "नि" (न + U+093F DEVANAGARI VOWEL SIGN I).
        assertThat(endOf("नियम", "न")).isEqualTo(Status.SPLITS_CHARACTER);
        assertThat(BoundaryCheck.at(cp("नियम"), 1).reason()).contains("U+093F", "VOWEL SIGN I");
        assertThat(endOf("दावे", "दाव")).as("drops the vowel sign E").isEqualTo(Status.SPLITS_CHARACTER);
        assertThat(endOf("नियमों", "नियम")).as("inflected word").isEqualTo(Status.SPLITS_CHARACTER);
    }

    @Test
    void viramaConjunctsCannotBeSplitOnEitherSide() {
        String ksha = "क्ष"; // क + U+094D VIRAMA + ष
        assertThat(endOf(ksha, "क")).as("before the virama").isEqualTo(Status.SPLITS_CHARACTER);
        assertThat(endOf(ksha, "क्")).as("after the virama, before the consonant").isEqualTo(Status.SPLITS_CHARACTER);
        assertThat(startOf(ksha, "ष")).as("consonant after a virama").isEqualTo(Status.SPLITS_CHARACTER);
        assertThat(endOf("शर्तें", "शर्त")).as("conjunct word losing its vowel sign").isEqualTo(Status.SPLITS_CHARACTER);
        assertThat(startOf("शर्तें", "र्तें")).as("after श, a real syllable boundary")
                .isEqualTo(Status.SAFE);
        assertThat(endOf("शर्तें", "शर")).as("र followed by virama").isEqualTo(Status.SPLITS_CHARACTER);
    }

    @Test
    void wordFinalViramaBeforeASpaceIsNotAConjunct() {
        assertThat(endOf("क् ख", "क्")).isEqualTo(Status.SAFE);
    }

    @Test
    void joinersAndNuktaKeepTheirSequencesTogether() {
        assertThat(endOf("क्‍ष", "क्")).as("ZWJ after virama").isEqualTo(Status.SPLITS_CHARACTER);
        assertThat(endOf("क्‌ष", "क्")).as("ZWNJ after virama").isEqualTo(Status.SPLITS_CHARACTER);
        assertThat(startOf("क्‍ष", "ष")).as("consonant after ZWJ").isEqualTo(Status.SPLITS_CHARACTER);
        assertThat(endOf("ज़मीन", "ज")).as("nukta U+093C, written decomposed")
                .isEqualTo(Status.SPLITS_CHARACTER);
        assertThat(endOf("👩‍💻", "👩")).as("emoji ZWJ sequence").isEqualTo(Status.SPLITS_CHARACTER);
        assertThat(endOf("👍🏽", "👍")).as("emoji skin-tone modifier").isEqualTo(Status.SPLITS_CHARACTER);
    }

    @Test
    void latinCombiningAccentsAndCrLfAreSplits() {
        assertThat(endOf("café", "cafe")).isEqualTo(Status.SPLITS_CHARACTER);
        assertThat(BoundaryCheck.at(cp("a\r\nb"), 2).status()).isEqualTo(Status.SPLITS_CHARACTER);
        assertThat(BoundaryCheck.at(cp("a\r\nb"), 1).status()).isEqualTo(Status.SAFE);
    }

    @Test
    void otherIndicScriptsAreCovered() {
        assertThat(endOf("கா", "க")).as("Tamil vowel sign").isEqualTo(Status.SPLITS_CHARACTER);
        assertThat(endOf("ক্ষ", "ক্")).as("Bengali virama").isEqualTo(Status.SPLITS_CHARACTER);
    }

    @Test
    void uncoveredScriptsAreNeverReportedSafe() {
        // Thai SARA AA (U+0E32) is a letter, not a mark, so only the script rule stops it.
        assertThat(endOf("กา", "ก")).isEqualTo(Status.CANNOT_ESTABLISH);
        assertThat(endOf("한국", "한")).as("Hangul").isEqualTo(Status.CANNOT_ESTABLISH);
        assertThat(endOf("🇮🇳🇮🇳", "🇮")).as("regional indicators").isEqualTo(Status.CANNOT_ESTABLISH);
        assertThat(BoundaryCheck.at(cp("กา"), 1).reason()).contains("thai");
    }
}
