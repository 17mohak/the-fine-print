// Reproducible benchmarks and measurements for the assessed algorithms (Track C, D-31).
// JMH runs are opt-in tasks, never part of `build`; a small smoke test of the harness runs with the tests.
plugins {
    id("fineprint.java-conventions")
}

dependencies {
    implementation(project(":cia:cia-algorithms"))
    implementation(libs.jmh.core)
    annotationProcessor(libs.jmh.generator.annprocess)
}

val results = layout.projectDirectory.dir("results")

// Full JMH run. Override the configuration with -PjmhArgs="..." (passed to org.openjdk.jmh.Main) and the output
// file name with -PjmhResultFile=... (default jmh-results.json, written under results/).
tasks.register<JavaExec>("jmh") {
    group = "benchmark"
    description = "Runs the JMH benchmarks and writes results/<jmhResultFile> (default jmh-results.json)."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "org.openjdk.jmh.Main"
    val extra = providers.gradleProperty("jmhArgs").map { it.split(" ").filter(String::isNotBlank) }
    val resultFile = providers.gradleProperty("jmhResultFile").getOrElse("jmh-results.json")
    args(extra.getOrElse(listOf("-f", "1", "-wi", "3", "-w", "1s", "-i", "5", "-r", "1s")) +
            listOf("-rf", "json", "-rff", results.file(resultFile).asFile.absolutePath))
    doFirst { results.asFile.mkdirs() }
}

// False-positive-rate, memory and build-throughput experiment (deterministic, seeded).
tasks.register<JavaExec>("bloomExperiment") {
    group = "benchmark"
    description = "Measures Bloom filter FPR and memory versus HashSet; writes results/bloom-experiment.csv."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "com.thefineprint.cia.bench.BloomExperiment"
    jvmArgs("-Xmx2g")
    args(results.file("bloom-experiment.csv").asFile.absolutePath)
    doFirst { results.asFile.mkdirs() }
}
