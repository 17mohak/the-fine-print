package com.thefineprint.cia.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/** End-to-end over HTTP: the browser UI is served, and both endpoints compute on the server. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoHttpTest {

    @Value("${local.server.port}")
    int port;

    @Test
    void servesTheBrowserInterface() throws Exception {
        HttpResponse<String> page = get("/");
        assertThat(page.statusCode()).isEqualTo(200);
        assertThat(page.body()).contains("Evidence Integrity Lab", "app.js", "Evidence check", "Not established",
                "How it works");
        assertThat(get("/app.js").body()).contains("/algo/evidence-check", "/algo/quote-match", "/algo/membership");
    }

    @Test
    void quoteMatchEndpointComputesOffsets() throws Exception {
        HttpResponse<String> r = post("/algo/quote-match", "{\"text\":\"fee, late fee, fee\",\"quote\":\"fee\"}");
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.body()).contains("\"start\":0", "\"start\":10", "\"start\":15", "Knuth-Morris-Pratt");
    }

    @Test
    void membershipEndpointScreensThenConfirms() throws Exception {
        HttpResponse<String> r = post("/algo/membership",
                "{\"fingerprints\":[\"POL-1\",\"POL-2\"],\"probes\":[\"POL-1\"],\"targetFalsePositiveRate\":0.01}");
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.body()).contains("\"bloomVerdict\":\"POSSIBLY_PRESENT\"", "\"exactVerdict\":\"CONFIRMED_PRESENT\"");
    }

    @Test
    void evidenceCheckEndpointRunsBothStagesOnTheServer() throws Exception {
        String json = "{\"sourceText\":\"4.2 Notice of claim. The Policyholder shall give written notice of any claim "
                + "within 30 days of discharge.\",\"claimedQuotation\":\"written notice of any claim\"}";
        HttpResponse<String> r = post("/algo/evidence-check", json);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.body()).contains("\"verdict\":\"VERIFIED\"", "\"matchClass\":\"EXACT_UNIQUE\"",
                "\"exactVerdict\":\"IDENTICAL_WORDING\"", "\"referenceId\":\"INS-CLAIM-NOTICE-025\"",
                "\"notEstablished\"", "QuoteVerifier SPI");
    }

    @Test
    void evidenceCheckRejectsASubSyllableQuotationOverHttp() throws Exception {
        // JSON escapes keep the request independent of the platform's default charset.
        HttpResponse<String> r = post("/algo/evidence-check",
                "{\"sourceText\":\"\\u0928\\u093F\\u092F\\u092E\",\"claimedQuotation\":\"\\u0928\"}");
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(r.body()).contains("\"verdict\":\"BOUNDARY_NOT_ESTABLISHED\"", "\"matchClass\":\"MISALIGNED\"",
                "\"boundariesEstablished\":false");
    }

    @Test
    void evidenceCheckInvalidInputIsAProblemResponse() throws Exception {
        HttpResponse<String> r = post("/algo/evidence-check",
                "{\"sourceText\":\"secret-synthetic-text\",\"claimedQuotation\":\"\"}");
        assertThat(r.statusCode()).isEqualTo(400);
        assertThat(r.headers().firstValue("Content-Type")).hasValueSatisfying(v -> assertThat(v).contains("problem+json"));
        assertThat(r.body()).contains("must not be empty").doesNotContain("secret-synthetic-text");
    }

    @Test
    void invalidInputGivesAProblemResponseWithoutEchoingInput() throws Exception {
        HttpResponse<String> r = post("/algo/quote-match", "{\"text\":\"secret-synthetic-text\",\"quote\":\"\"}");
        assertThat(r.statusCode()).isEqualTo(400);
        assertThat(r.headers().firstValue("Content-Type")).hasValueSatisfying(v -> assertThat(v).contains("problem+json"));
        assertThat(r.body()).contains("quote must not be empty").doesNotContain("secret-synthetic-text");

        HttpResponse<String> malformed = post("/algo/membership", "{not json");
        assertThat(malformed.statusCode()).isEqualTo(400);
    }

    private HttpResponse<String> get(String path) throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
        }
    }

    private HttpResponse<String> post(String path, String json) throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder(uri(path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }

    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + port + path);
    }
}
