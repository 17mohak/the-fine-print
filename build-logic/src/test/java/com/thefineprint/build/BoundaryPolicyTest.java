package com.thefineprint.build;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BoundaryPolicyTest {

    @Test
    void recognisesCiaProjectPaths() {
        assertThat(BoundaryPolicy.isCiaProject(":cia")).isTrue();
        assertThat(BoundaryPolicy.isCiaProject(":cia:cia-algorithms")).isTrue();
        assertThat(BoundaryPolicy.isCiaProject(":cia-demo")).isTrue();
        assertThat(BoundaryPolicy.isCiaProject(":platform:app")).isFalse();
        assertThat(BoundaryPolicy.isCiaProject(":platform:social")).isFalse();
        assertThat(BoundaryPolicy.isCiaProject(":core-api")).isFalse();
    }

    @Test
    void recognisesCiaExternalModules() {
        assertThat(BoundaryPolicy.isCiaModule("com.thefineprint.cia", "anything")).isTrue();
        assertThat(BoundaryPolicy.isCiaModule("com.thefineprint.cia.bench", "x")).isTrue();
        assertThat(BoundaryPolicy.isCiaModule("org.example", "cia-algorithms")).isTrue();
        assertThat(BoundaryPolicy.isCiaModule("com.thefineprint.ciao", "x")).isFalse();
        assertThat(BoundaryPolicy.isCiaModule("org.springframework", "spring-core")).isFalse();
    }

    @Test
    void resolvesDomainOfProjectPath() {
        assertThat(BoundaryPolicy.domainOf(":platform:domains:lending")).contains(":platform:domains:lending");
        assertThat(BoundaryPolicy.domainOf(":platform:domains:lending:fixtures"))
                .contains(":platform:domains:lending");
        assertThat(BoundaryPolicy.domainOf(":platform:domains:")).isEmpty();
        assertThat(BoundaryPolicy.domainOf(":platform:documents")).isEmpty();
    }

    @Test
    void forbidsCrossDomainButAllowsSameDomainAndSharedModules() {
        assertThat(BoundaryPolicy.projectViolation(":platform:domains:lending", ":platform:domains:insurance"))
                .isPresent();
        assertThat(BoundaryPolicy.projectViolation(":platform:domains:lending", ":platform:domains:lending:x"))
                .isEmpty();
        assertThat(BoundaryPolicy.projectViolation(":platform:domains:lending", ":core-api")).isEmpty();
        assertThat(BoundaryPolicy.projectViolation(":platform:app", ":platform:domains:lending")).isEmpty();
    }

    @Test
    void forbidsCiaFromEveryOwner() {
        assertThat(BoundaryPolicy.projectViolation(":platform:app", ":cia:cia-demo")).isPresent();
        assertThat(BoundaryPolicy.projectViolation(":core-api", ":cia:cia-algorithms")).isPresent();
        assertThat(BoundaryPolicy.moduleViolation(":platform:app", "com.thefineprint.cia", "cia-algorithms", "1"))
                .isPresent();
        assertThat(BoundaryPolicy.moduleViolation(":platform:app", "org.junit", "junit-bom", "6")).isEmpty();
    }
}
