package com.thefineprint.cia.demo.evidence;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** HTTP entry point for the evidence check. Transport only; the work is in {@link EvidenceCheckService}. */
@RestController
public class EvidenceCheckController {

    private final EvidenceCheckService service;

    public EvidenceCheckController(EvidenceCheckService service) {
        this.service = service;
    }

    @PostMapping("/algo/evidence-check")
    public EvidenceCheckResponse check(@RequestBody EvidenceCheckRequest request) {
        return service.check(request);
    }
}
