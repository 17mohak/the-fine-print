// ArchUnit boundary rules over the real platform classes (rules a-c of the P0 acceptance criteria, plus
// core-api purity and "no untrusted parsing in the Java core"). Not a platform module; test sources only.
plugins {
    id("fineprint.java-conventions")
}

dependencies {
    testImplementation(project(":core-api"))
    testImplementation(project(":platform:app"))
    testImplementation(libs.archunit)
}
