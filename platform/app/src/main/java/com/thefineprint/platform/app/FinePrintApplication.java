package com.thefineprint.platform.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Composition root of THE FINE PRINT platform.
 *
 * <p>P0 scaffolding only: the application starts and exposes the actuator health endpoint. It has no data
 * source, no security configuration and no business endpoints; those arrive in authorised later phases.
 */
@SpringBootApplication
public class FinePrintApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinePrintApplication.class, args);
    }
}
