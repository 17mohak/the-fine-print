package com.thefineprint.cia.demo.evidence;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Splits a synthetic source into clauses with a deliberately simple, labelled heuristic: <b>one clause per
 * non-blank line</b>. A line that starts with a number label such as {@code 4.2} takes that label as its id;
 * other lines are named by line number. The clause <i>wording</i> is the line without its label, with leading and
 * trailing whitespace removed; nothing else is changed (no case, quote-mark or whitespace folding).
 *
 * <p>This is demo scaffolding, not the platform's clause segmentation (P1+), and it is not assessed code.
 */
final class ClauseSegmenter {

    /** A clause: its id, its line as a code-point range of the source, and its exact wording. */
    record Clause(String id, int start, int end, String wording) {

        boolean overlaps(int spanStart, int spanEnd) {
            return start < spanEnd && spanStart < end;
        }
    }

    private static final Pattern LABEL = Pattern.compile("^\\s*(\\d+(?:\\.\\d+)*)[.)]?\\s+");

    private ClauseSegmenter() {
    }

    static List<Clause> segment(int[] text) {
        List<Clause> clauses = new ArrayList<>();
        int lineStart = 0;
        int lineNumber = 1;
        for (int i = 0; i <= text.length; i++) {
            if (i == text.length || text[i] == '\n') {
                int lineEnd = i;
                if (lineEnd > lineStart && text[lineEnd - 1] == '\r') {
                    lineEnd--;
                }
                String line = new String(text, lineStart, lineEnd - lineStart);
                if (!line.isBlank()) {
                    Matcher label = LABEL.matcher(line);
                    boolean labelled = label.find();
                    String id = labelled ? label.group(1) : "line " + lineNumber;
                    String wording = (labelled ? line.substring(label.end()) : line).strip();
                    if (!wording.isEmpty()) {
                        clauses.add(new Clause(id, lineStart, lineEnd, wording));
                    }
                }
                lineStart = i + 1;
                lineNumber++;
            }
        }
        return List.copyOf(clauses);
    }
}
