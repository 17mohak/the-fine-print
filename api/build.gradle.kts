// OpenAPI skeleton (api/openapi.yaml) and a test that validates it.
plugins {
    id("fineprint.java-conventions")
}

dependencies {
    testImplementation(libs.swagger.parser.v3)
}

tasks.test {
    val spec = layout.projectDirectory.file("openapi.yaml")
    inputs.file(spec)
    systemProperty("openapi.spec", spec.asFile.absolutePath)
}
