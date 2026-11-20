package fixtures.violating.platform.cases;

import java.net.http.HttpClient;

/** Violates the outbound-call rule: cases must not call external systems. */
public class CaseNotifier {
    HttpClient client;
}
