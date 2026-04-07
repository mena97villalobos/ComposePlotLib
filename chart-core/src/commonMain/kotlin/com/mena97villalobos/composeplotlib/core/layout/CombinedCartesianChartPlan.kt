package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.model.ScatterSeries
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianYAxisBinding
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport

/**
 * Hit-test payload per layer (draw order: `segmentsDrawOrder` is bottom → top for bars).
 */
sealed class CombinedHitTestLayer {
    abstract val layerId: String
    abstract val yAxisBinding: CombinedCartesianYAxisBinding

    data class Lines(
        override val layerId: String,
        override val yAxisBinding: CombinedCartesianYAxisBinding,
        val series: List<LineSeries>,
    ) : CombinedHitTestLayer()

    data class Scatters(
        override val layerId: String,
        override val yAxisBinding: CombinedCartesianYAxisBinding,
        val series: List<ScatterSeries>,
    ) : CombinedHitTestLayer()

    data class Bars(
        override val layerId: String,
        override val yAxisBinding: CombinedCartesianYAxisBinding,
        val segmentsDrawOrder: List<PlannedBarSegment>,
    ) : CombinedHitTestLayer()
}

/**
 * Plan for [com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianChartSpec].
 *
 * Secondary Y tick geometry lives in [yTicksSecondary] (drawn after primary overlay); `layout.yTicks` stays primary.
 */
data class CombinedCartesianChartPlan(
    val layout: ChartLayoutPlan,
    val selection: ChartSelection?,
    val yTicksSecondary: List<AxisTick>,
    val secondaryViewport: ChartViewport?,
    val allBarSegments: List<PlannedBarSegment>,
    val hitTestLayers: List<CombinedHitTestLayer>,
)
