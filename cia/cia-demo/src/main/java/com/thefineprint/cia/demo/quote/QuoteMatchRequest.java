package com.thefineprint.cia.demo.quote;

/**
 * @param text synthetic source text (treated as already canonical)
 * @param quote the passage to look for; must not be empty
 */
public record QuoteMatchRequest(String text, String quote) {
}
