package com.thefineprint.build;

import java.util.Optional;

/**
 * Pure policy describing which dependencies are forbidden between modules.
 *
 * <p>Rules (IMPLEMENTATION_ROADMAP P0 acceptance; ARCHITECTURE_OVERVIEW section 2):
 * <ol>
 *   <li>No project outside {@code :cia} may resolve any {@code cia} project or {@code cia-*} artefact on any
 *       classpath. Track C code must never reach the platform runtime classpath.</li>
 *   <li>A domain module ({@code :platform:domains:*}) may not resolve another domain module, directly or
 *       transitively.</li>
 * </ol>
 */
public final class BoundaryPolicy {

    /** Reserved Maven group for any future Track C artefacts. */
    public static final String CIA_GROUP = "com.thefineprint.cia";

    static final String DOMAINS_PREFIX = ":platform:domains:";

    private BoundaryPolicy() {
    }

    /** True if the Gradle project path is part of the isolated CIA track. */
    public static boolean isCiaProject(String projectPath) {
        for (String segment : projectPath.split(":")) {
            if (isCiaName(segment)) {
                return true;
            }
        }
        return false;
    }

    /** True if an external module coordinate belongs to the isolated CIA track. */
    public static boolean isCiaModule(String group, String name) {
        return group.equals(CIA_GROUP) || group.startsWith(CIA_GROUP + ".") || isCiaName(name);
    }

    /** Returns the domain-module path that contains {@code projectPath}, if any. */
    public static Optional<String> domainOf(String projectPath) {
        if (!projectPath.startsWith(DOMAINS_PREFIX)) {
            return Optional.empty();
        }
        String rest = projectPath.substring(DOMAINS_PREFIX.length());
        if (rest.isEmpty()) {
            return Optional.empty();
        }
        int next = rest.indexOf(':');
        return Optional.of(DOMAINS_PREFIX + (next < 0 ? rest : rest.substring(0, next)));
    }

    /**
     * Returns a violation message if {@code owner} may not depend on project {@code dependencyPath},
     * or empty if the dependency is allowed.
     */
    public static Optional<String> projectViolation(String owner, String dependencyPath) {
        if (isCiaProject(dependencyPath)) {
            return Optional.of(owner + " resolves CIA project " + dependencyPath
                    + " (platform must never depend on cia-*; D-19/D-22)");
        }
        Optional<String> ownerDomain = domainOf(owner);
        Optional<String> depDomain = domainOf(dependencyPath);
        if (ownerDomain.isPresent() && depDomain.isPresent() && !ownerDomain.equals(depDomain)) {
            return Optional.of(owner + " resolves another domain module " + dependencyPath
                    + " (domain modules must not depend on each other)");
        }
        return Optional.empty();
    }

    /** Returns a violation message if {@code owner} may not depend on the external module, else empty. */
    public static Optional<String> moduleViolation(String owner, String group, String name, String version) {
        if (isCiaModule(group, name)) {
            return Optional.of(owner + " resolves CIA artefact " + group + ":" + name + ":" + version
                    + " (platform must never depend on cia-*; D-19/D-22)");
        }
        return Optional.empty();
    }

    private static boolean isCiaName(String name) {
        return name.equals("cia") || name.startsWith("cia-");
    }
}
