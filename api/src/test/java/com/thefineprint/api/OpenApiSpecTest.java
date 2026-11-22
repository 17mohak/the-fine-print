package com.thefineprint.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OpenApiSpecTest {

    private static SwaggerParseResult result;

    @BeforeAll
    static void parse() {
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        // A file URI, so paths containing spaces resolve correctly.
        String location = Path.of(System.getProperty("openapi.spec")).toUri().toString();
        result = new OpenAPIV3Parser().readLocation(location, null, options);
    }

    @Test
    void specIsValidOpenApi31() {
        assertThat(result.getMessages()).isEmpty();
        assertThat(result.getOpenAPI()).isNotNull();
        assertThat(result.getOpenAPI().getOpenapi()).startsWith("3.1.");
    }

    @Test
    void usesTheVersionedBasePath() {
        assertThat(result.getOpenAPI().getServers()).extracting(s -> s.getUrl()).containsExactly("/api/v1");
    }

    @Test
    void definesTheCrossCuttingConventions() {
        OpenAPI api = result.getOpenAPI();
        assertThat(api.getComponents().getSchemas()).containsKeys("Problem", "PageInfo");
        List<?> required =api.getComponents().getSchemas().get("Problem").getRequired();
        assertThat(List.copyOf(required)).map(String::valueOf).contains("traceId");
        assertThat(api.getComponents().getParameters()).containsKeys("Cursor", "Limit", "IdempotencyKey");
    }

    @Test
    void definesNoOperationsInP0() {
        assertThat(result.getOpenAPI().getPaths() == null || result.getOpenAPI().getPaths().isEmpty()).isTrue();
    }
}
