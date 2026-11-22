package com.thefineprint.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Proves each rule in {@link BoundaryRules} actually detects violations, using small fixture classes under
 * {@code fixtures.violating} (each breaks one rule) and {@code fixtures.compliant} (breaks none). Without this,
 * rules over placeholder modules could pass vacuously.
 */
class ArchitectureFixturesTest {

    private static final String BAD = "fixtures.violating";
    private static final String GOOD = "fixtures.compliant";
    private static final List<String> FAKE_PROVIDERS = List.of("fixtures.vendor..");

    private static final JavaClasses VIOLATING = new ClassFileImporter().importPackages(BAD, "fixtures.vendor");
    private static final JavaClasses COMPLIANT = new ClassFileImporter().importPackages(GOOD, "fixtures.vendor");

    @Test
    void detectsPlatformDependencyOnCia() {
        assertViolation(BoundaryRules.platformMustNotDependOnCia(BAD), "CiaImportingService");
        assertClean(BoundaryRules.platformMustNotDependOnCia(GOOD));
    }

    @Test
    void detectsDomainToDomainDependency() {
        assertViolation(BoundaryRules.domainsMustNotDependOnEachOther(BAD), "AlphaService");
        assertClean(BoundaryRules.domainsMustNotDependOnEachOther(GOOD));
    }

    @Test
    void detectsProviderUseOutsideModelGateway() {
        assertViolation(BoundaryRules.onlyModelGatewayUsesProviders(BAD, FAKE_PROVIDERS), "Summariser");
        assertClean(BoundaryRules.onlyModelGatewayUsesProviders(GOOD, FAKE_PROVIDERS));
    }

    @Test
    void detectsOutboundHttpOutsideGatewayAndIntegrations() {
        assertViolation(BoundaryRules.onlyGatewayAndIntegrationsMakeOutboundCalls(BAD), "CaseNotifier");
        assertViolation(BoundaryRules.onlyGatewayAndIntegrationsMakeOutboundCalls(BAD), "LegacyFetcher");
        assertClean(BoundaryRules.onlyGatewayAndIntegrationsMakeOutboundCalls(GOOD));
    }

    @Test
    void detectsCoreApiDependingOnNonJdkCode() {
        assertViolation(BoundaryRules.coreApiDependsOnlyOnJdk(BAD), "LeakyDto");
        assertClean(BoundaryRules.coreApiDependsOnlyOnJdk(GOOD));
    }

    @Test
    void detectsUntrustedParsingInJavaCore() {
        assertViolation(BoundaryRules.noUntrustedParsingInJavaCore(BAD), "ThumbnailReader");
        assertClean(BoundaryRules.noUntrustedParsingInJavaCore(GOOD));
    }

    private static void assertViolation(ArchRule rule, String offendingSimpleName) {
        EvaluationResult result = rule.evaluate(VIOLATING);
        assertThat(result.hasViolation()).as("rule should be violated: %s", rule.getDescription()).isTrue();
        assertThat(result.getFailureReport().getDetails()).anyMatch(d -> d.contains(offendingSimpleName));
    }

    private static void assertClean(ArchRule rule) {
        EvaluationResult result = rule.evaluate(COMPLIANT);
        assertThat(result.getFailureReport().getDetails()).as(rule.getDescription()).isEmpty();
    }
}
