package com.thefineprint.cia.demo.membership;

import com.thefineprint.cia.algorithms.membership.BloomFilter;
import com.thefineprint.cia.algorithms.membership.BloomParameters;
import com.thefineprint.cia.demo.membership.MembershipResponse.BloomVerdict;
import com.thefineprint.cia.demo.membership.MembershipResponse.ExactVerdict;
import com.thefineprint.cia.demo.membership.MembershipResponse.ProbeResult;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * "Screen, then confirm" for synthetic fingerprints.
 *
 * <ol>
 *   <li>Build a Bloom filter sized from the requested capacity and false-positive target, and add every
 *       fingerprint (UTF-8 bytes).</li>
 *   <li>For each probe, ask the filter. "Definitely absent" needs no further work.</li>
 *   <li>Confirm each "possibly present" answer against {@link ExactFingerprintRegistry}. Only then is a
 *       probe shown as present; otherwise it is reported as a false positive.</li>
 * </ol>
 * Stateless: the filter lives only for the duration of one request.
 */
@Service
public class MembershipScreeningService {

    static final int MAX_FINGERPRINTS = 5_000;
    static final int MAX_PROBES = 500;
    static final int MAX_FINGERPRINT_LENGTH = 200;
    static final long MAX_VISUALISED_BITS = 4_096;
    static final long DEFAULT_SEED = 7L;

    public MembershipResponse screen(MembershipRequest request) {
        validate(request);
        List<String> fingerprints = request.fingerprints();
        long capacity = request.expectedInsertions() != null
                ? request.expectedInsertions()
                : Math.max(1, fingerprints.size());
        BloomParameters parameters = BloomParameters.optimal(capacity, request.targetFalsePositiveRate());
        long seed = request.seed() != null ? request.seed() : DEFAULT_SEED;

        BloomFilter filter = new BloomFilter(parameters, seed);
        for (String fingerprint : fingerprints) {
            filter.add(bytes(fingerprint));
        }
        ExactFingerprintRegistry registry = new ExactFingerprintRegistry(fingerprints);

        List<ProbeResult> results = new ArrayList<>();
        for (String probe : request.probes()) {
            long[] positions = filter.positionsOf(bytes(probe));
            if (!filter.mightContain(bytes(probe))) {
                results.add(new ProbeResult(probe, BloomVerdict.DEFINITELY_ABSENT, ExactVerdict.NOT_NEEDED,
                        toList(positions)));
            } else {
                ExactVerdict exact = registry.contains(probe)
                        ? ExactVerdict.CONFIRMED_PRESENT
                        : ExactVerdict.FALSE_POSITIVE;
                results.add(new ProbeResult(probe, BloomVerdict.POSSIBLY_PRESENT, exact, toList(positions)));
            }
        }

        return new MembershipResponse(
                new MembershipResponse.Parameters(parameters.expectedInsertions(),
                        parameters.targetFalsePositiveRate(), parameters.bitCount(), parameters.hashCount(),
                        parameters.bitsPerElement(), seed),
                filter.insertionCount(),
                registry.size(),
                filter.fillRatio(),
                filter.expectedFalsePositiveRate(),
                filter.insertionCount() > parameters.expectedInsertions(),
                bitString(filter, parameters.bitCount()),
                List.copyOf(results));
    }

    private static void validate(MembershipRequest request) {
        if (request == null || request.fingerprints() == null || request.probes() == null
                || request.targetFalsePositiveRate() == null) {
            throw new IllegalArgumentException("fingerprints, probes and targetFalsePositiveRate are required");
        }
        if (request.fingerprints().size() > MAX_FINGERPRINTS) {
            throw new IllegalArgumentException("at most " + MAX_FINGERPRINTS + " fingerprints");
        }
        if (request.probes().isEmpty() || request.probes().size() > MAX_PROBES) {
            throw new IllegalArgumentException("between 1 and " + MAX_PROBES + " probes");
        }
        double p = request.targetFalsePositiveRate();
        if (!(p >= 0.0001 && p <= 0.5)) {
            throw new IllegalArgumentException("targetFalsePositiveRate must be between 0.0001 and 0.5");
        }
        if (request.expectedInsertions() != null
                && (request.expectedInsertions() < 1 || request.expectedInsertions() > 1_000_000)) {
            throw new IllegalArgumentException("expectedInsertions must be between 1 and 1,000,000");
        }
        for (List<String> list : List.of(request.fingerprints(), request.probes())) {
            for (String value : list) {
                if (value == null || value.isEmpty() || value.length() > MAX_FINGERPRINT_LENGTH) {
                    throw new IllegalArgumentException(
                            "each fingerprint and probe must be 1 to " + MAX_FINGERPRINT_LENGTH + " characters");
                }
            }
        }
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private static List<Long> toList(long[] positions) {
        return Arrays.stream(positions).boxed().toList();
    }

    /** The bit pattern as a "0"/"1" string, only for filters small enough to draw. */
    private static String bitString(BloomFilter filter, long bitCount) {
        if (bitCount > MAX_VISUALISED_BITS) {
            return null;
        }
        StringBuilder sb = new StringBuilder((int) bitCount);
        for (long i = 0; i < bitCount; i++) {
            sb.append(filter.isBitSet(i) ? '1' : '0');
        }
        return sb.toString();
    }
}
