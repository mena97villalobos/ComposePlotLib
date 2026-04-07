package com.mena97villalobos.composeplotlib.benchmarks;

import com.mena97villalobos.composeplotlib.core.domain.XyPoint;
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD;
import com.mena97villalobos.composeplotlib.core.spec.LineDecimationStrategy;
import com.mena97villalobos.composeplotlib.core.spec.LineRenderSpec;
import com.mena97villalobos.composeplotlib.core.spec.ScatterRenderSpec;
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport;
import com.mena97villalobos.composeplotlib.core.visible.LineRenderPipeline;
import com.mena97villalobos.composeplotlib.core.visible.ScatterRenderPipeline;
import com.mena97villalobos.composeplotlib.core.visible.VisibleRangeCalculator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * JVM microbenchmarks for hot paths in {@code chart-core} visible-range and render pipelines.
 * Run: {@code ./gradlew :chart-core-benchmarks:jmh}
 */
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CoreVisibleBenchmark {

    @State(Scope.Benchmark)
    public static class LargeLineSeries {
        public List<XyPoint> points;
        public ChartViewport viewport;

        @Setup
        public void setup() {
            int n = 50_000;
            ArrayList<XyPoint> list = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                double x = i * 0.01;
                list.add(new XyPoint(x, Math.sin(x)));
            }
            points = list;
            viewport = new ChartViewport(
                    new ClosedRangeD(10.0, 30.0),
                    new ClosedRangeD(-1.5, 1.5)
            );
        }
    }

    @Benchmark
    public void visiblePointRange_lineSeries(LargeLineSeries state, Blackhole bh) {
        bh.consume(VisibleRangeCalculator.INSTANCE.visiblePointRange(state.points, state.viewport));
    }

    @Benchmark
    public void visiblePointRangeWithContinuity_lineSeries(LargeLineSeries state, Blackhole bh) {
        bh.consume(VisibleRangeCalculator.INSTANCE.visiblePointRangeWithContinuity(
                state.points,
                state.viewport,
                1
        ));
    }

    @Benchmark
    public void linePrepareIndexed_uniformDecimation(LargeLineSeries state, Blackhole bh) {
        LineRenderSpec spec = new LineRenderSpec(
                2048,
                LineDecimationStrategy.Uniform,
                1,
                false
        );
        bh.consume(LineRenderPipeline.INSTANCE.prepareIndexed(state.points, state.viewport, spec));
    }

    @Benchmark
    public void scatterPrepareIndexed_uniformDecimation(LargeLineSeries state, Blackhole bh) {
        ScatterRenderSpec spec = new ScatterRenderSpec(2048, LineDecimationStrategy.Uniform, false);
        bh.consume(ScatterRenderPipeline.INSTANCE.prepareIndexed(state.points, state.viewport, spec));
    }
}
