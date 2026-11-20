package com.thefineprint.cia.bench;

import static org.assertj.core.api.Assertions.assertThat;

import com.thefineprint.cia.algorithms.text.KmpMatcher;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Fast checks that the benchmark harness measures what it claims. Full runs use the jmh task. */
class HarnessSmokeTest {

    @Test
    void allThreeMatchersAgreeOnBenchmarkInputs() {
        for (String kind : new String[] {"ORDINARY", "ADVERSARIAL"}) {
            for (int m : new int[] {8, 64}) {
                KmpBenchmark b = new KmpBenchmark();
                b.textLength = 20_000;
                b.patternLength = m;
                b.input = kind;
                b.prepare();
                assertThat(b.naive()).as("%s m=%d naive", kind, m).containsExactly(b.kmp());
                assertThat(b.jdkIndexOf()).as("%s m=%d indexOf", kind, m).containsExactly(b.kmp());
            }
        }
    }

    @Test
    void ordinaryPatternsOccurAndAdversarialPatternsDoNot() {
        String text = SyntheticText.ordinary(5_000, 1L);
        assertThat(text).hasSize(5_000);
        String excerpt = SyntheticText.excerpt(text, 64, 2L);
        assertThat(KmpMatcher.search(text.codePoints().toArray(), excerpt.codePoints().toArray()).matchCount())
                .isPositive();
        assertThat(SyntheticText.adversarialText(100).codePoints().toArray()).hasSize(100);
        assertThat(KmpMatcher.search(SyntheticText.adversarialText(1_000).codePoints().toArray(),
                SyntheticText.adversarialPattern(16).codePoints().toArray()).matchCount()).isZero();
    }

    @Test
    void bloomExperimentProducesPlausibleRows() {
        List<BloomExperiment.Row> rows = BloomExperiment.run(new int[] {2_000}, new int[] {10}, 50_000);
        assertThat(rows).hasSize(1);
        BloomExperiment.Row row = rows.get(0);
        assertThat(row.measuredFpr()).isBetween(0.002, 0.02); // theory about 0.008 at 10 bits/element
        assertThat(row.bloomBytes()).isEqualTo(2_504); // 20,000 bits -> 313 longs
        assertThat(row.hashCount()).isEqualTo(7);
    }

    @Test
    void bloomBenchmarkSetupBuildsBothStructures() {
        BloomBenchmark b = new BloomBenchmark();
        b.elements = 1_000;
        b.bitsPerElement = 10;
        b.prepare();
        assertThat(b.bloomInsert().insertionCount()).isEqualTo(BloomBenchmark.BATCH);
        assertThat(b.hashSetInsert()).hasSize(BloomBenchmark.BATCH);
    }
}
