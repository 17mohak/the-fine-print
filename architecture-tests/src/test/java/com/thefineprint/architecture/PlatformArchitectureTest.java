package com.thefineprint.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Runs every boundary rule against the real production classes of core-api and all platform modules. */
class PlatformArchitectureTest {

    private static final String ROOT = "com.thefineprint";

    private static JavaClasses classes;

    @BeforeAll
    static void importProductionClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(ROOT);
    }

    @Test
    void importsTheRealCodeBase() {
        // Guards against a silently empty import making every rule pass vacuously.
        assertThat(classes.contain("com.thefineprint.platform.app.FinePrintApplication")).isTrue();
        assertThat(classes.contain("com.thefineprint.core.api.spi.QuoteVerifier")).isTrue();
    }

    @Test
    void platformDoesNotDependOnCia() {
        BoundaryRules.platformMustNotDependOnCia(ROOT).check(classes);
    }

    @Test
    void domainModulesAreIndependent() {
        BoundaryRules.domainsMustNotDependOnEachOther(ROOT).check(classes);
    }

    @Test
    void onlyModelGatewayCallsProviders() {
        BoundaryRules.onlyModelGatewayUsesProviders(ROOT, BoundaryRules.PROVIDER_SDK_PACKAGES).check(classes);
    }

    @Test
    void outboundCallsOnlyFromGatewayAndIntegrations() {
        BoundaryRules.onlyGatewayAndIntegrationsMakeOutboundCalls(ROOT).check(classes);
    }

    @Test
    void coreApiIsFrameworkFree() {
        BoundaryRules.coreApiDependsOnlyOnJdk(ROOT).check(classes);
    }

    @Test
    void noUntrustedParsingInJavaCore() {
        BoundaryRules.noUntrustedParsingInJavaCore(ROOT).check(classes);
    }

    @Test
    void noCiaClassesOnThePlatformClasspath() {
        assertThat(classes.stream().map(c -> c.getPackageName()))
                .noneMatch(p -> p.startsWith(ROOT + ".cia"));
    }
}
