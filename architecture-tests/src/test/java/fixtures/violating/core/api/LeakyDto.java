package fixtures.violating.core.api;

import fixtures.violating.platform.audit.LegacyFetcher;

/** Violates core-api purity: depends on non-JDK code. */
public class LeakyDto {
    LegacyFetcher fetcher;
}
