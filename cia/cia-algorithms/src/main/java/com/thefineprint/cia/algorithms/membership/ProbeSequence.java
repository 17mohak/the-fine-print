package com.thefineprint.cia.algorithms.membership;

/**
 * Turns a key's two 64-bit hashes into its {@code k} Bloom-filter bit positions (double hashing).
 *
 * <p><b>Strategy.</b> For a filter of {@code m} bits:
 * <ol>
 *   <li>start {@code a = h1 mod m} (hash read as unsigned), so {@code 0 <= a < m};</li>
 *   <li>step {@code b = 1 + (h2 mod (m - 1))}, so {@code 1 <= b <= m - 1}; then, while {@code gcd(b, m) != 1},
 *       increase {@code b} by one. This stops at {@code m - 1} at the latest, because consecutive integers are
 *       always coprime ({@code gcd(m - 1, m) = 1}), so the step stays in {@code [1, m - 1]};</li>
 *   <li>positions {@code g_0 = a} and {@code g_(i+1) = (g_i + b) mod m}, i.e. {@code g_i = (a + i*b) mod m}.</li>
 * </ol>
 *
 * <p><b>Why the step must be coprime with m.</b> The map {@code i -> (a + i*b) mod m} visits
 * {@code m / gcd(b, m)} distinct positions before repeating. If {@code b} shares a factor with {@code m} the
 * sequence collapses onto a smaller cycle and a key may set fewer than {@code k} distinct bits. Forcing
 * {@code b} odd only helps when the shared factor is 2: with {@code m = 15} and {@code b = 5}, for example, only
 * 3 positions are reachable. With {@code gcd(b, m) = 1} the map is a bijection on {@code 0..m-1}, so
 * <b>the first {@code min(k, m)} positions are always distinct</b>.
 *
 * <p><b>No overflow.</b> Both hashes are reduced modulo {@code m} (or {@code m - 1}) before any arithmetic.
 * Afterwards every value is below {@code m <= 64 * (2^31 - 1) < 2^37}, so {@code g_i + b < 2^38}. The sequence is
 * advanced by one addition and one conditional subtraction, so there is no multiplication that could wrap.
 *
 * <p><b>m = 1.</b> The only position is 0; the step is 0.
 */
final class ProbeSequence {

    private ProbeSequence() {
    }

    /** First position: {@code h1 mod m}, treating {@code h1} as unsigned. */
    static long start(long h1, long m) {
        return Long.remainderUnsigned(h1, m);
    }

    /** Step in {@code [1, m - 1]} with {@code gcd(step, m) = 1}; 0 when {@code m == 1}. Deterministic in its inputs. */
    static long step(long h2, long m) {
        if (m == 1) {
            return 0;
        }
        long b = 1 + Long.remainderUnsigned(h2, m - 1);
        while (gcd(b, m) != 1) {
            b++; // stops by m - 1 at the latest: gcd(m - 1, m) = 1
        }
        return b;
    }

    /** The position after {@code position}: {@code (position + step) mod m}, for {@code position, step < m}. */
    static long next(long position, long step, long m) {
        long sum = position + step;
        return sum >= m ? sum - m : sum;
    }

    /** Fills {@code out} with the first {@code out.length} positions for {@code (h1, h2)}. */
    static void positions(long h1, long h2, long m, long[] out) {
        long position = start(h1, m);
        long step = step(h2, m);
        for (int i = 0; i < out.length; i++) {
            out[i] = position;
            position = next(position, step, m);
        }
    }

    /** Euclid's algorithm, for non-negative arguments. */
    static long gcd(long a, long b) {
        while (b != 0) {
            long r = a % b;
            a = b;
            b = r;
        }
        return a;
    }
}
