package com.mena97villalobos.composeplotlib.core.visible

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.spec.LineDecimationStrategy
import com.mena97villalobos.composeplotlib.core.spec.LineRenderSpec
import com.mena97villalobos.composeplotlib.core.spec.ScatterRenderSpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport

/**
 * Indexed scatter points for drawing and hit-testing: domain-visible window (scatter semantics), then optional
 * decimation matching [LineRenderPipeline] when a cap is set.
 */
object ScatterRenderPipeline {
    fun prepareIndexed(
        points: List<XyPoint>,
        viewport: ChartViewport,
        spec: ScatterRenderSpec,
    ): List<Pair<Int, XyPoint>> {
        if (points.isEmpty()) return emptyList()
        val visible = VisibleRangeCalculator.scatterPointsInDomainViewport(points, viewport)
        if (visible.isEmpty()) return emptyList()
        val cap = spec.maxPointsPerSeries
        if (cap <= 0 || visible.size <= cap) return visible
        val lineSpec =
            LineRenderSpec(
                maxPointsPerSeries = cap,
                decimation = spec.decimation,
                viewportContinuityPadding = 0,
                usePlotWidthPixelBudget = false,
            )
        var out = LineRenderPipeline.decimateIndexed(visible, lineSpec)
        // MinMax-style decimation can briefly exceed the cap before dedupe ordering; squeeze uniformly if needed.
        if (out.size > cap) {
            val squeeze =
                LineRenderSpec(
                    maxPointsPerSeries = cap,
                    decimation = LineDecimationStrategy.Uniform,
                    viewportContinuityPadding = 0,
                    usePlotWidthPixelBudget = false,
                )
            out = LineRenderPipeline.decimateIndexed(out, squeeze)
        }
        return out
    }
}
