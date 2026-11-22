package com.thefineprint.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.lang.ArchRule;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URLConnection;
import java.util.List;

/**
 * Architecture rules, parameterised by root package so the same rules run against the real code base
 * ({@code com.thefineprint}) and against deliberately violating fixtures.
 *
 * <p>Rules may legitimately match no classes while modules are placeholders, so empty "should" clauses are
 * allowed; {@code ArchitectureFixturesTest} proves each rule detects violations.
 */
final class BoundaryRules {

    /** Known model-provider SDK packages. Only {@code modelgateway} may use them (ARCHITECTURE_OVERVIEW 1.4). */
    static final List<String> PROVIDER_SDK_PACKAGES = List.of(
            "com.anthropic..", "com.openai..", "dev.langchain4j..", "org.springframework.ai..",
            "com.google.genai..", "com.google.cloud.vertexai..", "com.azure.ai..",
            "software.amazon.awssdk.services.bedrock..", "software.amazon.awssdk.services.bedrockruntime..",
            "ai.djl..", "de.kherud.llama..");

    /** Outbound network clients. Only {@code modelgateway} and {@code integrations} may make outbound calls. */
    static final List<String> OUTBOUND_HTTP_PACKAGES = List.of(
            "java.net.http..", "org.springframework.web.client..",
            "org.springframework.web.reactive.function.client..", "okhttp3..", "org.apache.hc..",
            "org.apache.http..", "retrofit2..", "feign..", "io.netty.handler.codec.http..");

    /** Untrusted-file parsers. All parsing of untrusted bytes belongs in the sandboxed worker (ARCH section 4). */
    static final List<String> UNTRUSTED_PARSER_PACKAGES = List.of(
            "org.apache.pdfbox..", "com.itextpdf..", "com.lowagie..", "org.apache.tika..",
            "org.apache.poi..", "net.sourceforge.tess4j..", "javax.imageio..", "org.openpdf..");

    private BoundaryRules() {
    }

    /** (a) Neither platform nor core-api may depend on the isolated CIA track. */
    static ArchRule platformMustNotDependOnCia(String root) {
        return noClasses().that().resideInAnyPackage(root + ".platform..", root + ".core..")
                .should().dependOnClassesThat().resideInAPackage(root + ".cia..")
                .because("assessed CIA code must never be a platform dependency (D-19, D-22)")
                .allowEmptyShould(true);
    }

    /** (b) Domain modules must not depend on each other. */
    static ArchRule domainsMustNotDependOnEachOther(String root) {
        return slices().matching(root + ".platform.domains.(*)..")
                .should().notDependOnEachOther()
                .because("domain modules are independently developed (SYSTEM_DESIGN section 8)")
                .allowEmptyShould(true);
    }

    /** (c) Only modelgateway may use model-provider SDKs. */
    static ArchRule onlyModelGatewayUsesProviders(String root, List<String> providerPackages) {
        return noClasses().that().resideOutsideOfPackage(root + ".platform.modelgateway..")
                .should().dependOnClassesThat().resideInAnyPackage(providerPackages.toArray(String[]::new))
                .because("language models are reached only through the Java model gateway (ARCH section 1)")
                .allowEmptyShould(true);
    }

    /** (c, continued) Only modelgateway and integrations may make outbound network calls. */
    static ArchRule onlyGatewayAndIntegrationsMakeOutboundCalls(String root) {
        return noClasses().that().resideOutsideOfPackages(
                        root + ".platform.modelgateway..", root + ".platform.integrations..")
                .should().dependOnClassesThat().resideInAnyPackage(OUTBOUND_HTTP_PACKAGES.toArray(String[]::new))
                .orShould().dependOnClassesThat().belongToAnyOf(
                        HttpURLConnection.class, URLConnection.class, Socket.class)
                .because("outbound calls are confined to modelgateway and integrations (handover section 7)")
                .allowEmptyShould(true);
    }

    /** core-api contains only interfaces and DTOs and depends on nothing but the JDK. */
    static ArchRule coreApiDependsOnlyOnJdk(String root) {
        return classes().that().resideInAPackage(root + ".core.api..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(root + ".core.api..", "java..")
                .because("core-api is shared by the platform and the CIA track and must stay framework-free")
                .allowEmptyShould(true);
    }

    /** No untrusted-file parsing in the Java core. */
    static ArchRule noUntrustedParsingInJavaCore(String root) {
        return noClasses().that().resideInAPackage(root + "..")
                .should().dependOnClassesThat().resideInAnyPackage(UNTRUSTED_PARSER_PACKAGES.toArray(String[]::new))
                .because("all parsing of untrusted bytes happens in the sandboxed worker (ARCH section 4, D-23)")
                .allowEmptyShould(true);
    }
}
