// THE FINE PRINT — Gradle multi-module build (Track P, P0 scaffolding; D-21).
pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "fineprint"

// Shared interfaces and DTOs (no logic).
include(":core-api")

// OpenAPI skeleton and its validation test.
include(":api")

// Platform modular monolith. Every module except :platform:app is an empty placeholder in P0.
val platformModules = listOf(
    "app",
    "identity-access",
    "crypto",
    "documents",
    "workerbroker",
    "canonical",
    "provenance",
    "knowledge",
    "understand",
    "modelgateway",
    "commitments",
    "cases",
    "integrations",
    "audit",
)
platformModules.forEach { include(":platform:$it") }

val domainModules = listOf(
    "generic-agreement",
    "lending",
    "insurance",
    "tenancy",
    "subscriptions",
    "warranties",
)
domainModules.forEach { include(":platform:domains:$it") }

// Mechanical architecture checks (ArchUnit) over the platform classpath.
include(":architecture-tests")

// Isolated academic track (Track C, authorised by D-31). The platform must never depend on these modules:
// verifyBoundaries (build-logic) and ArchUnit enforce it.
include(":cia:cia-algorithms", ":cia:cia-bench", ":cia:cia-demo")
