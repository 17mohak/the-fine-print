import org.cyclonedx.gradle.CyclonedxDirectTask

buildscript {
    // Lock the build's own plugin classpath too, so it is pinned and visible to OSV-Scanner
    // (buildscript-gradle.lockfile). Build-time only: none of this reaches the application runtime.
    configurations.classpath {
        resolutionStrategy.activateDependencyLocking()
    }
    dependencies {
        constraints {
            classpath("com.google.guava:guava:33.7.2-jre") {
                because("GHSA-xxph-c9ww-hj94 / CVE-2026-102554 fixed in 33.7.2; pulled in by the CycloneDX plugin")
            }
        }
    }
}

plugins {
    base
    alias(libs.plugins.cyclonedx)
}

// The included build's own tests (boundary-plugin TestKit tests) are part of the root `check`.
tasks.named("check") {
    dependsOn(gradle.includedBuild("build-logic").task(":check"))
}

// SBOM: aggregate CycloneDX document at build/reports/cyclonedx/bom.{json,xml}.
// Only runtime classpaths are recorded: that is what ships. Test-only tooling is excluded.
allprojects {
    tasks.withType<CyclonedxDirectTask>().configureEach {
        includeConfigs = listOf("runtimeClasspath")
        includeBuildSystem = false
        // Pure container projects have no code or dependencies.
        enabled = path !in setOf(":platform:cyclonedxDirectBom", ":platform:domains:cyclonedxDirectBom")
    }
}

// Resolves and locks every configuration. Use: ./gradlew resolveAndLockAll --write-locks
tasks.register("resolveAndLockAll") {
    description = "Resolves all resolvable configurations in all projects (use with --write-locks)."
    notCompatibleWithConfigurationCache("Resolves configurations of all projects at execution time")
    doFirst {
        require(gradle.startParameter.isWriteDependencyLocks) { "Run with --write-locks" }
    }
    doLast {
        allprojects.forEach { p ->
            p.configurations.filter { it.isCanBeResolved }.forEach { it.resolve() }
        }
    }
}
