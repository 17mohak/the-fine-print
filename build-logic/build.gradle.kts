plugins {
    `java-gradle-plugin`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
}

dependencyLocking {
    lockAllConfigurations()
}

gradlePlugin {
    plugins {
        create("javaConventions") {
            id = "fineprint.java-conventions"
            implementationClass = "com.thefineprint.build.JavaConventionsPlugin"
        }
        create("boundaries") {
            id = "fineprint.boundaries"
            implementationClass = "com.thefineprint.build.BoundariesPlugin"
        }
    }
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
