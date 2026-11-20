// Runnable demonstration of the assessed algorithms (Track C, D-31): a Spring Boot API under /algo/* and a
// browser UI served from static resources. Separate from the platform application; never imported by it.
plugins {
    id("fineprint.java-conventions")
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))
    implementation(project(":cia:cia-algorithms"))
    implementation(libs.spring.boot.starter.webmvc)

    testImplementation(platform(libs.spring.boot.dependencies))
    testImplementation(libs.spring.boot.starter.test)
}
