package com.thefineprint.cia.algorithms;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Keeps the assessed implementations honest and isolated (ARCHITECTURE_OVERVIEW section 2; CIA matrix section 1:
 * original implementations, frameworks for infrastructure only).
 */
class AssessedCodeIsolationTest {

    private static final JavaClasses ASSESSED = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.thefineprint.cia.algorithms");

    private static final Set<String> FORBIDDEN_STRING_METHODS = Set.of(
            "indexOf", "lastIndexOf", "contains", "matches", "split", "regionMatches",
            "startsWith", "endsWith", "replace", "replaceAll", "replaceFirst");

    @Test
    void importsTheAssessedClasses() {
        assertThat(ASSESSED.contain("com.thefineprint.cia.algorithms.text.KmpMatcher")).isTrue();
        assertThat(ASSESSED.contain("com.thefineprint.cia.algorithms.membership.BloomFilter")).isTrue();
    }

    @Test
    void dependsOnlyOnTheJdkCoreAndCoreApi() {
        classes().should().onlyDependOnClassesThat().resideInAnyPackage(
                        "java.lang..", "java.util", "java.util.stream",
                        "com.thefineprint.core.api..", "com.thefineprint.cia.algorithms..")
                .because("assessed code has no Spring, database, network or I/O dependencies")
                .check(ASSESSED);
    }

    @Test
    void doesNotDelegateToJdkSetsOrBitSets() {
        noJdkCollections().check(ASSESSED);
    }

    @Test
    void doesNotCallJdkStringSearch() {
        noJdkStringSearch().check(ASSESSED);
    }

    @Test
    void rulesDetectAViolatingFixture() {
        JavaClasses cheat = new ClassFileImporter().importPackages("fixtures.ciacheat");
        assertThat(noJdkCollections().evaluate(cheat).hasViolation()).isTrue();
        assertThat(noJdkStringSearch().evaluate(cheat).hasViolation()).isTrue();
    }

    private static ArchRule noJdkCollections() {
        return noClasses().should().dependOnClassesThat().belongToAnyOf(
                        java.util.BitSet.class, java.util.HashSet.class, java.util.HashMap.class,
                        java.util.TreeSet.class, java.util.TreeMap.class, java.util.LinkedHashSet.class,
                        java.util.PriorityQueue.class)
                .because("the Bloom filter's bit storage and the matcher must be our own implementations");
    }

    private static ArchRule noJdkStringSearch() {
        return noClasses().should().callMethodWhere(new DescribedPredicate<JavaMethodCall>("a JDK String search method") {
                    @Override
                    public boolean test(JavaMethodCall call) {
                        return call.getTargetOwner().isEquivalentTo(String.class)
                                && FORBIDDEN_STRING_METHODS.contains(call.getName());
                    }
                })
                .because("KMP must not delegate matching to String.indexOf or similar");
    }
}
