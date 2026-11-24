package com.thefineprint.cia.demo.linking;

import com.thefineprint.cia.algorithms.linking.DisjointSet;
import com.thefineprint.cia.demo.linking.DocumentLinkRequest.Document;
import com.thefineprint.cia.demo.linking.DocumentLinkResponse.Group;
import com.thefineprint.cia.demo.linking.DocumentLinkResponse.SharedIdentifier;
import com.thefineprint.cia.demo.linking.DocumentLinkResponse.Stats;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Groups documents into families: two documents are linked when they share an identifier, and a family is a set of
 * documents connected through such links (transitively). The grouping itself is the assessed
 * {@link DisjointSet}; this service only maps identifiers to document numbers (infrastructure) and explains the
 * result. Groupings are suggestions: nothing is applied or stored.
 */
@Service
public class DocumentLinkService {

    static final int MAX_DOCUMENTS = 200;
    static final int MAX_IDENTIFIERS_PER_DOCUMENT = 20;
    static final int MAX_TEXT = 100;
    static final int BROAD_THRESHOLD = 4;

    static final List<String> CAVEATS = List.of(
            "A shared identifier is a hint, not proof: transitive linking can merge unrelated documents if an "
                    + "identifier is common (an insurer name, a shared phone number). Identifiers shared by "
                    + BROAD_THRESHOLD + " or more documents are flagged as broad.",
            "Identifiers are compared exactly as entered (no case, whitespace or Unicode folding), so a differently "
                    + "written identifier does not link.",
            "This groups documents only. It does not establish that they belong to one agreement or what they mean, and "
                    + "nothing is merged or stored.");

    public DocumentLinkResponse link(DocumentLinkRequest request) {
        long started = System.nanoTime();
        List<Document> documents = validate(request);
        int n = documents.size();

        DisjointSet forest = new DisjointSet(n);
        Map<String, List<Integer>> holders = new LinkedHashMap<>(); // identifier -> documents holding it, in order
        int identifierCount = 0;
        for (int i = 0; i < n; i++) {
            Set<String> seen = new HashSet<>();
            for (String identifier : documents.get(i).identifiers()) {
                identifierCount++;
                if (seen.add(identifier)) { // a repeat within one document adds no link
                    holders.computeIfAbsent(identifier, k -> new ArrayList<>()).add(i);
                }
            }
        }
        int unionCalls = 0;
        int merges = 0;
        for (List<Integer> docs : holders.values()) {
            for (int k = 1; k < docs.size(); k++) {
                unionCalls++;
                if (forest.union(docs.get(0), docs.get(k))) {
                    merges++;
                }
            }
        }

        int[] label = forest.componentLabels();
        List<List<String>> members = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (label[i] == members.size()) {
                members.add(new ArrayList<>());
            }
            members.get(label[i]).add(documents.get(i).id());
        }
        List<Group> groups = new ArrayList<>();
        for (int g = 0; g < members.size(); g++) {
            groups.add(new Group(g + 1, List.copyOf(members.get(g))));
        }
        List<SharedIdentifier> links = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> e : holders.entrySet()) {
            List<Integer> docs = e.getValue();
            if (docs.size() > 1) {
                List<String> ids = docs.stream().map(i -> documents.get(i).id()).toList();
                links.add(new SharedIdentifier(e.getKey(), ids, label[docs.get(0)] + 1, docs.size() >= BROAD_THRESHOLD));
            }
        }
        long micros = (System.nanoTime() - started) / 1_000;
        return new DocumentLinkResponse("Disjoint-set forest (union by size, path halving)",
                new Stats(n, identifierCount, unionCalls, merges, forest.componentCount(), forest.parentHops()),
                List.copyOf(groups), List.copyOf(links), CAVEATS, micros);
    }

    private static List<Document> validate(DocumentLinkRequest request) {
        if (request == null || request.documents() == null) {
            throw new IllegalArgumentException("documents are required");
        }
        List<Document> documents = request.documents();
        if (documents.isEmpty() || documents.size() > MAX_DOCUMENTS) {
            throw new IllegalArgumentException("between 1 and " + MAX_DOCUMENTS + " documents");
        }
        Set<String> ids = new HashSet<>();
        for (Document d : documents) {
            if (d == null || d.id() == null || d.id().isBlank() || d.id().codePointCount(0, d.id().length()) > MAX_TEXT) {
                throw new IllegalArgumentException("each document needs an id of 1 to " + MAX_TEXT + " characters");
            }
            if (!ids.add(d.id())) {
                throw new IllegalArgumentException("document ids must be unique");
            }
            List<String> identifiers = d.identifiers() == null ? List.of() : d.identifiers();
            if (identifiers.size() > MAX_IDENTIFIERS_PER_DOCUMENT) {
                throw new IllegalArgumentException("at most " + MAX_IDENTIFIERS_PER_DOCUMENT + " identifiers per document");
            }
            for (String identifier : identifiers) {
                if (identifier == null || identifier.isEmpty()
                        || identifier.codePointCount(0, identifier.length()) > MAX_TEXT) {
                    throw new IllegalArgumentException("each identifier needs 1 to " + MAX_TEXT + " characters");
                }
            }
        }
        return documents.stream()
                .map(d -> new Document(d.id(), d.identifiers() == null ? List.of() : List.copyOf(d.identifiers())))
                .toList();
    }
}
