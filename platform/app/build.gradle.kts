// Spring Boot composition root of the modular monolith. P0: health endpoint only.
plugins {
    id("fineprint.java-conventions")
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.actuator)

    // Composition: the app's runtime classpath is "the platform runtime classpath" checked by verifyBoundaries.
    implementation(project(":core-api"))
    listOf(
        "identity-access", "crypto", "documents", "workerbroker", "canonical", "provenance", "knowledge",
        "understand", "modelgateway", "commitments", "cases", "integrations", "audit",
    ).forEach { implementation(project(":platform:$it")) }
    listOf("generic-agreement", "lending", "insurance", "tenancy", "subscriptions", "warranties")
        .forEach { implementation(project(":platform:domains:$it")) }

    testImplementation(platform(libs.spring.boot.dependencies))
    testImplementation(libs.spring.boot.starter.test)
}
