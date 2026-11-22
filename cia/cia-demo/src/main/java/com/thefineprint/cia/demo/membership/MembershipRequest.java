package com.thefineprint.cia.demo.membership;

import java.util.List;

/**
 * @param fingerprints synthetic identifiers to insert (each 1–200 characters; up to 5,000)
 * @param probes values to look up (1–500)
 * @param targetFalsePositiveRate design probability p, between 0.0001 and 0.5
 * @param expectedInsertions design capacity n; defaults to the number of fingerprints. Set it lower to see
 *     saturation
 * @param seed hash seed; defaults to 7 so runs are reproducible
 */
public record MembershipRequest(List<String> fingerprints, List<String> probes, Double targetFalsePositiveRate,
                                Long expectedInsertions, Long seed) {
}
