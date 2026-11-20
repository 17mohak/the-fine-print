package fixtures.violating.platform.understand;

import fixtures.vendor.llm.FakeProviderClient;

/** Violates rule (c): provider SDK used outside modelgateway. */
public class Summariser {
    FakeProviderClient client;
}
