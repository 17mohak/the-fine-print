package com.thefineprint.cia.demo.evidence;

import com.thefineprint.cia.algorithms.membership.BloomFilter;
import com.thefineprint.cia.algorithms.membership.BloomParameters;
import com.thefineprint.cia.demo.evidence.ReferenceClauseCorpus.ReferenceClause;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * The reference library the evidence check screens clauses against: the assessed Bloom filter as a prescreen,
 * and an exact store as the authority.
 *
 * <p><b>Roles.</b> Every reference wording is fingerprinted (SHA-256 of its UTF-8 bytes) and its fingerprint added
 * to the Bloom filter. The exact store maps fingerprints to entries; it stands in for the authoritative lookup that
 * would live in a database or remote store, which is the situation in which a Bloom prescreen earns its place:
 * a "definitely absent" answer avoids that lookup entirely. At this demo's size an exact in-memory set alone would
 * be perfectly adequate; the library is kept small so it can be generated and inspected.
 *
 * <p><b>Exact comparison.</b> A Bloom "possibly present" is never reported as a match. The exact step looks the
 * fingerprint up <i>and</i> compares the wording string itself, so even a fingerprint collision could not produce
 * a false match.
 *
 * <p>Built once at start-up and never modified afterwards; requests only read it. No request data is stored.
 */
@Component
public class ReferenceClauseLibrary {

    static final double TARGET_FALSE_POSITIVE_RATE = 0.01;
    static final long SEED = 7L;

    /** Result of screening one wording. */
    enum Screen {
        /** The Bloom filter proved the fingerprint was never added; no exact lookup was made. */
        DEFINITELY_ABSENT,
        /** Bloom said "possibly"; the exact store holds an identical wording. */
        EXACT_MATCH_CONFIRMED,
        /** Bloom said "possibly"; the exact store holds no identical wording. */
        FALSE_POSITIVE_REJECTED
    }

    /** Screening outcome and, when confirmed, the reference entry whose wording is identical. */
    record Outcome(Screen screen, Optional<ReferenceClause> reference) {
    }

    private final List<ReferenceClause> entries;
    private final List<ReferenceClause> heldOut;
    private final Map<String, ReferenceClause> exactStore = new HashMap<>();
    private final BloomFilter filter;

    public ReferenceClauseLibrary() {
        ReferenceClauseCorpus.Generated corpus = ReferenceClauseCorpus.generate();
        this.entries = corpus.library();
        this.heldOut = corpus.heldOut();
        this.filter = new BloomFilter(BloomParameters.optimal(entries.size(), TARGET_FALSE_POSITIVE_RATE), SEED);
        for (ReferenceClause entry : entries) {
            byte[] fingerprint = fingerprint(entry.wording());
            filter.add(fingerprint);
            exactStore.put(HexFormat.of().formatHex(fingerprint), entry);
        }
        if (exactStore.size() != entries.size()) {
            throw new IllegalStateException("reference wordings must be distinct");
        }
    }

    /** Screens {@code wording}: Bloom first, then an exact comparison for every possible hit. */
    Outcome screen(String wording) {
        byte[] fingerprint = fingerprint(wording);
        if (!filter.mightContain(fingerprint)) {
            return new Outcome(Screen.DEFINITELY_ABSENT, Optional.empty());
        }
        ReferenceClause candidate = exactStore.get(HexFormat.of().formatHex(fingerprint));
        if (candidate != null && candidate.wording().equals(wording)) {
            return new Outcome(Screen.EXACT_MATCH_CONFIRMED, Optional.of(candidate));
        }
        return new Outcome(Screen.FALSE_POSITIVE_REJECTED, Optional.empty());
    }

    /** SHA-256 of the UTF-8 bytes. The wording is used exactly as given: no normalisation or folding. */
    static byte[] fingerprint(String wording) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(wording.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required by every Java platform", e);
        }
    }

    List<ReferenceClause> entries() {
        return entries;
    }

    List<ReferenceClause> heldOut() {
        return heldOut;
    }

    BloomParameters parameters() {
        return filter.parameters();
    }

    double fillRatio() {
        return filter.fillRatio();
    }

    long bloomBytes() {
        return filter.memoryBytes();
    }

    /** Theoretical false-positive rate at the number of <b>distinct</b> entries (all entries are distinct). */
    double expectedFalsePositiveRate() {
        return filter.parameters().theoreticalFalsePositiveRate(entries.size());
    }
}
