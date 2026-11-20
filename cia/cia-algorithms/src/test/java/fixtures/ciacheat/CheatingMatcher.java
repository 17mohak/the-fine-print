package fixtures.ciacheat;

import java.util.HashSet;
import java.util.Set;

/** Deliberately breaks the isolation rules, so the rules can be shown to fail. Not assessed code. */
public class CheatingMatcher {

    private final Set<String> seen = new HashSet<>();

    public int find(String text, String pattern) {
        seen.add(pattern);
        return text.indexOf(pattern);
    }
}
