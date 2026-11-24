package com.thefineprint.cia.demo.linking;

import java.util.List;

/**
 * @param documents 1–200 synthetic documents
 */
public record DocumentLinkRequest(List<Document> documents) {

    /**
     * @param id unique, 1–100 characters
     * @param identifiers up to 20 identifiers (for example a policy or loan number), each 1–100 characters. Two
     *     documents are linked when they share an identifier exactly; nothing is normalised or folded
     */
    public record Document(String id, List<String> identifiers) {
    }
}
