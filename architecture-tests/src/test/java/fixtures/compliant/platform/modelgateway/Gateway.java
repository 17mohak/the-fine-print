package fixtures.compliant.platform.modelgateway;

import fixtures.vendor.llm.FakeProviderClient;
import java.net.http.HttpClient;

/** Allowed: the gateway may use provider SDKs and outbound HTTP. */
public class Gateway {
    FakeProviderClient provider;
    HttpClient http;
}
