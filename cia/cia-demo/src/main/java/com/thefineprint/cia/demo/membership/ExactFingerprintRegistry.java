package com.thefineprint.cia.demo.membership;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * The authoritative, exact record of which fingerprints exist.
 *
 * <p>A Bloom filter can only say "possibly present". Every possible hit is resolved here before anything is
 * shown as present. This class deliberately uses a plain {@code HashSet}: it is not assessed code, and in the
 * platform its role would be played by a database lookup.
 */
final class ExactFingerprintRegistry {

    private final Set<String> fingerprints;

    ExactFingerprintRegistry(Collection<String> fingerprints) {
        this.fingerprints = new HashSet<>(fingerprints);
    }

    boolean contains(String fingerprint) {
        return fingerprints.contains(fingerprint);
    }

    int size() {
        return fingerprints.size();
    }
}
