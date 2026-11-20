package com.thefineprint.cia.demo.quote;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** HTTP entry point for quote matching. Transport only; the work is in {@link QuoteMatchService}. */
@RestController
public class QuoteMatchController {

    private final QuoteMatchService service;

    public QuoteMatchController(QuoteMatchService service) {
        this.service = service;
    }

    @PostMapping("/algo/quote-match")
    public QuoteMatchResponse match(@RequestBody QuoteMatchRequest request) {
        return service.match(request);
    }
}
