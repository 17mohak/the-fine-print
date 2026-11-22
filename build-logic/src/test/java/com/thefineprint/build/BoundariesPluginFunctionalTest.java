package com.thefineprint.build;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Builds a throwaway multi-project build with Gradle TestKit to prove that {@code verifyBoundaries}
 * really fails on forbidden dependencies. The {@code :cia:cia-placeholder} project here exists only inside
 * the temporary test build; the real repository contains no cia-* modules (D-21).
 */
class BoundariesPluginFunctionalTest {

    @TempDir
    Path dir;

    @BeforeEach
    void setUp() throws IOException {
        write("settings.gradle.kts", """
                rootProject.name = "boundary-fixture"
                include(":platform:app", ":platform:shared",
                        ":platform:domains:alpha", ":platform:domains:beta", ":cia:cia-placeholder")
                """);
        for (String p : new String[] {"platform/app", "platform/shared", "platform/domains/alpha",
                "platform/domains/beta", "cia/cia-placeholder"}) {
            module(p, "");
        }
    }

    @Test
    void passesWhenNoForbiddenDependencies() throws IOException {
        module("platform/app", "implementation(project(\":platform:domains:alpha\"))\n"
                + "implementation(project(\":platform:domains:beta\"))");
        BuildResult result = runner().build();
        assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
    }

    @Test
    void failsWhenPlatformDependsOnCiaDirectly() throws IOException {
        module("platform/app", "implementation(project(\":cia:cia-placeholder\"))");
        BuildResult result = runner().buildAndFail();
        assertThat(result.getOutput()).contains("resolves CIA project :cia:cia-placeholder");
    }

    @Test
    void failsWhenPlatformReachesCiaTransitively() throws IOException {
        module("platform/shared", "implementation(project(\":cia:cia-placeholder\"))");
        module("platform/app", "implementation(project(\":platform:shared\"))");
        BuildResult result = runner().buildAndFail();
        assertThat(result.getOutput()).contains(":platform:app resolves CIA project :cia:cia-placeholder");
    }

    @Test
    void failsWhenPlatformTestClasspathContainsCia() throws IOException {
        module("platform/app", "testImplementation(project(\":cia:cia-placeholder\"))");
        BuildResult result = runner().buildAndFail();
        assertThat(result.getOutput()).contains("[testRuntimeClasspath]");
    }

    @Test
    void failsWhenDomainDependsOnAnotherDomain() throws IOException {
        module("platform/domains/alpha", "implementation(project(\":platform:domains:beta\"))");
        BuildResult result = runner().buildAndFail();
        assertThat(result.getOutput()).contains("resolves another domain module :platform:domains:beta");
    }

    @Test
    void failsWhenDomainReachesAnotherDomainTransitively() throws IOException {
        module("platform/shared", "api(project(\":platform:domains:beta\"))");
        module("platform/domains/alpha", "implementation(project(\":platform:shared\"))");
        BuildResult result = runner().buildAndFail();
        assertThat(result.getOutput())
                .contains(":platform:domains:alpha resolves another domain module :platform:domains:beta");
    }

    @Test
    void ciaProjectsAreNotGivenTheCheck() throws IOException {
        BuildResult result = runner().withArguments(":cia:cia-placeholder:tasks", "--all").build();
        assertThat(result.getOutput()).doesNotContain("verifyBoundaries");
    }

    private GradleRunner runner() {
        return GradleRunner.create()
                .withProjectDir(dir.toFile())
                .withPluginClasspath()
                .withArguments("verifyBoundaries", "--stacktrace");
    }

    private void module(String path, String dependencies) throws IOException {
        write(path + "/build.gradle.kts", """
                plugins {
                    `java-library`
                    id("fineprint.boundaries")
                }
                dependencies {
                %s
                }
                """.formatted(dependencies));
    }

    private void write(String relative, String content) throws IOException {
        Path file = dir.resolve(relative);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content, StandardCharsets.UTF_8);
    }
}
