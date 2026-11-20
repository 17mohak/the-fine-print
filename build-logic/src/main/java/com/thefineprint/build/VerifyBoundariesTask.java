package com.thefineprint.build;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.component.ProjectComponentIdentifier;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

/**
 * Walks the fully resolved dependency graphs of a project's classpaths and fails on any boundary violation
 * defined by {@link BoundaryPolicy}. Checking the resolved graph (not only declarations) catches transitive
 * violations.
 */
public abstract class VerifyBoundariesTask extends DefaultTask {

    /** Gradle path of the project being checked. */
    @Input
    public abstract Property<String> getOwnerPath();

    /** Root components of the classpaths to check, keyed by configuration name. */
    @Internal
    public abstract MapProperty<String, ResolvedComponentResult> getClasspathRoots();

    @TaskAction
    public void verify() {
        String owner = getOwnerPath().get();
        Set<String> violations = new TreeSet<>();
        List<String> checked = new ArrayList<>();
        for (Map.Entry<String, ResolvedComponentResult> entry : getClasspathRoots().get().entrySet()) {
            checked.add(entry.getKey());
            for (String violation : collect(owner, entry.getValue())) {
                violations.add("[" + entry.getKey() + "] " + violation);
            }
        }
        if (!violations.isEmpty()) {
            throw new GradleException("Dependency boundary violations in " + owner + ":\n  - "
                    + String.join("\n  - ", violations));
        }
        getLogger().info("Boundaries OK for {} (checked {})", owner, checked);
    }

    private static List<String> collect(String owner, ResolvedComponentResult root) {
        List<String> found = new ArrayList<>();
        Set<ComponentIdentifier> seen = new HashSet<>();
        Deque<ResolvedComponentResult> queue = new ArrayDeque<>();
        seen.add(root.getId());
        queue.add(root);
        while (!queue.isEmpty()) {
            ResolvedComponentResult current = queue.removeFirst();
            for (DependencyResult dependency : current.getDependencies()) {
                if (!(dependency instanceof ResolvedDependencyResult resolved)) {
                    continue; // unresolved dependencies already fail resolution elsewhere
                }
                ResolvedComponentResult selected = resolved.getSelected();
                if (!seen.add(selected.getId())) {
                    continue;
                }
                ComponentIdentifier id = selected.getId();
                if (id instanceof ProjectComponentIdentifier project) {
                    BoundaryPolicy.projectViolation(owner, project.getProjectPath()).ifPresent(found::add);
                } else if (id instanceof ModuleComponentIdentifier module) {
                    BoundaryPolicy.moduleViolation(owner, module.getGroup(), module.getModule(),
                            module.getVersion()).ifPresent(found::add);
                }
                queue.add(selected);
            }
        }
        return found;
    }
}
