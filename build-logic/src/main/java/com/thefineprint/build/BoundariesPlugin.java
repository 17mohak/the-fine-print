package com.thefineprint.build;

import java.util.List;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

/**
 * Registers {@code verifyBoundaries} on every Java project outside {@code :cia} and wires it into
 * {@code check}. Run all of them with {@code ./gradlew verifyBoundaries}.
 */
public final class BoundariesPlugin implements Plugin<Project> {

    static final String TASK_NAME = "verifyBoundaries";

    private static final List<String> CHECKED_CONFIGURATIONS = List.of(
            JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME,
            JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME,
            JavaPlugin.TEST_COMPILE_CLASSPATH_CONFIGURATION_NAME,
            JavaPlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME);

    @Override
    public void apply(Project project) {
        if (BoundaryPolicy.isCiaProject(project.getPath())) {
            return; // the isolated track is never checked as a platform consumer
        }
        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> register(project));
    }

    private static void register(Project project) {
        TaskProvider<VerifyBoundariesTask> task = project.getTasks().register(TASK_NAME,
                VerifyBoundariesTask.class, t -> {
                    t.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
                    t.setDescription("Fails if this project resolves a cia-* module or (for domain modules) "
                            + "another domain module.");
                    t.getOwnerPath().set(project.getPath());
                    for (String name : CHECKED_CONFIGURATIONS) {
                        t.getClasspathRoots().put(name, project.getConfigurations().named(name)
                                .flatMap(c -> c.getIncoming().getResolutionResult().getRootComponent()));
                    }
                });
        project.getTasks().named(LifecycleBasePlugin.CHECK_TASK_NAME, check -> check.dependsOn(task));
    }
}
