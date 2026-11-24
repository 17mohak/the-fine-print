package com.thefineprint.cia.demo.linking;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** HTTP entry point for document linking. Transport only; the work is in {@link DocumentLinkService}. */
@RestController
public class DocumentLinkController {

    private final DocumentLinkService service;

    public DocumentLinkController(DocumentLinkService service) {
        this.service = service;
    }

    @PostMapping("/algo/document-links")
    public DocumentLinkResponse link(@RequestBody DocumentLinkRequest request) {
        return service.link(request);
    }
}
