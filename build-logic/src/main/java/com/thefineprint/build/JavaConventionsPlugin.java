package com.thefineprint.build;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.VersionCatalog;
import org.gradle.api.artifacts.dsl.LockMode;
import org.gradle.api.artifacts.VersionCatalogsExtension;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

/**
 * Shared conventions for every Java module: Java 21 toolchain, strict compilation, JUnit Platform,
 * dependency locking, and boundary enforcement.
 */
public final class JavaConventionsPlugin implements Plugin<Project> {

    static final int JAVA_VERSION = 21;

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(JavaLibraryPlugin.class);
        project.getPluginManager().apply(BoundariesPlugin.class);

        project.getExtensions().configure(JavaPluginExtension.class, java ->
                java.getToolchain().getLanguageVersion().set(JavaLanguageVersion.of(JAVA_VERSION)));

        project.getTasks().withType(JavaCompile.class).configureEach(compile -> {
            compile.getOptions().getRelease().set(JAVA_VERSION);
            compile.getOptions().setEncoding("UTF-8");
            compile.getOptions().getCompilerArgs().addAll(
                    java.util.List.of("-Xlint:all,-processing", "-Werror", "-parameters"));
        });

        // STRICT: a configuration without lock state fails resolution, so nothing resolves unpinned.
        project.getDependencyLocking().lockAllConfigurations();
        project.getDependencyLocking().getLockMode().set(LockMode.STRICT);

        VersionCatalog libs = project.getExtensions().getByType(VersionCatalogsExtension.class).named("libs");
        var deps = project.getDependencies();
        deps.add("testImplementation", deps.platform(libs.findLibrary("junit-bom").orElseThrow().get()));
        deps.add("testImplementation", libs.findLibrary("junit-jupiter").orElseThrow().get());
        deps.add("testImplementation", libs.findLibrary("assertj-core").orElseThrow().get());
        deps.add("testRuntimeOnly", libs.findLibrary("junit-platform-launcher").orElseThrow().get());

        project.getTasks().withType(Test.class).configureEach(test -> {
            test.useJUnitPlatform();
            test.getSystemProperties().put("user.timezone", "UTC");
        });
    }
}
