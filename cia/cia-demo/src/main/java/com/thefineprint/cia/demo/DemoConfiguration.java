package com.thefineprint.cia.demo;

import com.thefineprint.cia.algorithms.text.KmpQuoteVerifier;
import com.thefineprint.core.api.spi.QuoteVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the assessed adapters behind their {@code core-api} contracts. Demo services depend on the
 * {@link QuoteVerifier} interface, not on the KMP classes, exactly as a platform caller would; the platform itself
 * never sees this module (ARCHITECTURE_OVERVIEW section 2).
 */
@Configuration
class DemoConfiguration {

    @Bean
    QuoteVerifier quoteVerifier() {
        return new KmpQuoteVerifier();
    }
}
