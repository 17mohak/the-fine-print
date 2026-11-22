// Assessed algorithm implementations (Track C, D-31). JDK + core-api only: no Spring, database or network.
plugins {
    id("fineprint.java-conventions")
}

dependencies {
    api(project(":core-api"))
    testImplementation(libs.archunit)
}
