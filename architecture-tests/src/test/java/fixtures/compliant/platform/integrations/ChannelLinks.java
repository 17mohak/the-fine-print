package fixtures.compliant.platform.integrations;

import java.net.http.HttpClient;

/** Allowed: integrations may make outbound calls. */
public class ChannelLinks {
    HttpClient http;
}
