package fixtures.violating.platform.audit;

import java.net.HttpURLConnection;

/** Violates the outbound-call rule via a JDK connection class. */
public class LegacyFetcher {
    HttpURLConnection connection;
}
