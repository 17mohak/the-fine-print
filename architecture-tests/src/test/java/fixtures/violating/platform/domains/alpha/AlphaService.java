package fixtures.violating.platform.domains.alpha;

import fixtures.violating.platform.domains.beta.BetaService;

/** Violates rule (b): one domain depends on another. */
public class AlphaService {
    BetaService beta;
}
