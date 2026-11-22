package com.thefineprint.cia.demo.membership;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** HTTP entry point for membership screening. Transport only; the work is in {@link MembershipScreeningService}. */
@RestController
public class MembershipController {

    private final MembershipScreeningService service;

    public MembershipController(MembershipScreeningService service) {
        this.service = service;
    }

    @PostMapping("/algo/membership")
    public MembershipResponse screen(@RequestBody MembershipRequest request) {
        return service.screen(request);
    }
}
