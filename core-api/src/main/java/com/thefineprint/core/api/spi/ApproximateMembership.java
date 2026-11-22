package com.thefineprint.core.api.spi;

/**
 * A membership pre-screen over opaque byte keys (ARCHITECTURE_OVERVIEW section 2; CIA matrix A2).
 *
 * <p><b>Contract</b>
 * <ul>
 *   <li>No false negatives: after {@code add(k)}, {@code mightContain(k)} must return {@code true}.</li>
 *   <li>{@code true} means <i>possibly present</i> only. Callers must confirm with an authoritative exact check
 *       before relying on membership, and must never use this result for security or legal decisions.</li>
 *   <li>Keys are compared by content (array contents, not identity). Callers must not mutate a key after
 *       passing it. {@code null} keys are rejected with {@link NullPointerException}.</li>
 * </ul>
 *
 * <p><b>Implementations:</b> none in P0. The production default is an exact set (P1+); assessed
 * implementations are Track C only.
 */
public interface ApproximateMembership {

    /** Records {@code key} as a member. */
    void add(byte[] key);

    /** Returns {@code false} only if {@code key} was definitely never added. */
    boolean mightContain(byte[] key);
}
