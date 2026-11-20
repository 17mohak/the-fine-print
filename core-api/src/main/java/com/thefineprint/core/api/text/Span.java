package com.thefineprint.core.api.text;

/**
 * A half-open range {@code [start, end)} of <b>Unicode code-point offsets</b> into a text (SYSTEM_DESIGN 2.2).
 * Offsets are code points, not UTF-16 {@code char} indices.
 *
 * <p>The constructor rejects structurally impossible ranges only; it carries no other behaviour.
 *
 * @param start inclusive code-point offset, {@code >= 0}
 * @param end exclusive code-point offset, {@code >= start}
 */
public record Span(int start, int end) {

    public Span {
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("invalid span [" + start + ", " + end + ")");
        }
    }
}
