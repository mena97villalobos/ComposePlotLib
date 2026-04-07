package com.mena97villalobos.composeplotlib.core.spec

/**
 * Controls **domain-visible** scatter markers and optional downsampling for planning and hit-testing.
 *
 * Unlike [LineRenderSpec], there is no continuity padding: scatter uses
 * [com.mena97villalobos.composeplotlib.core.visible.VisibleRangeCalculator.scatterPointsInDomainViewport]
 * (per-point domain box test), then the same decimation strategies as line rendering when
 * [maxPointsPerSeries] caps the visible subset.
 *
 * @param maxPointsPerSeries Max markers per series after domain filtering. `0` or negative means no cap unless
 * [usePlotWidthPixelBudget] is applied in Compose (see [LineRenderSpec.autoMaxPointsForPlotWidthPx]).
 * @param usePlotWidthPixelBudget When `true` and `maxPointsPerSeries <= 0`, the Compose layer may supply a cap
 * from laid-out plot width (same policy as line charts).
 */
data class ScatterRenderSpec(
    val maxPointsPerSeries: Int = 0,
    val decimation: LineDecimationStrategy = LineDecimationStrategy.Uniform,
    val usePlotWidthPixelBudget: Boolean = false,
)
