package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.CombinedCartesianChartPlan
import com.mena97villalobos.composeplotlib.core.layout.CombinedHitTestLayer
import com.mena97villalobos.composeplotlib.core.layout.forHitTestFromDrawOrder
import com.mena97villalobos.composeplotlib.core.model.toXyPoints
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianLayer
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianYAxisBinding
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import com.mena97villalobos.composeplotlib.core.viewport.normalizedNonDegenerate
import com.mena97villalobos.composeplotlib.core.visible.LineRenderPipeline
import com.mena97villalobos.composeplotlib.core.visible.ScatterPlotPoints
import com.mena97villalobos.composeplotlib.core.visible.ScatterRenderPipeline

/**
 * Resolves taps for combined cartesian charts: layers are tested **top-first** (last in [CombinedCartesianChartSpec.layers] wins).
 *
 * Line and scatter hits use the same indexed point lists as rendering ([LineRenderPipeline.prepareIndexed],
 * [ScatterRenderPipeline] / [ScatterPlotPoints]) so tap targets stay aligned with drawn points when caps apply.
 */
object CombinedCartesianHitResolver {
    fun resolve(
        spec: CombinedCartesianChartSpec,
        plan: CombinedCartesianChartPlan,
        primaryViewport: ChartViewport,
        touchPx: OffsetD,
        plotBounds: RectD,
        lineMaxDistancePx: Double = 28.0,
    ): ChartSelection? {
        if (!touchPx.x.isFinite() || !touchPx.y.isFinite()) return null
        val vpPrimary = primaryViewport.normalizedNonDegenerate()
        val vpSecondary = plan.secondaryViewport?.normalizedNonDegenerate()

        // Mirror the render transform (CartesianLayoutPlanner) — the linear/non-inverted defaults would
        // mis-hit every log, symlog, broken or inverted axis.
        fun transformOn(
            vp: ChartViewport,
            yAxis: AxisSpec,
        ): ChartTransform =
            ChartTransform.forPlotBounds(
                vp,
                plotBounds,
                xScale = spec.xAxis.scale,
                yScale = yAxis.scale,
                invertX = spec.xAxis.inverted,
                invertY = yAxis.inverted,
            )

        val transformPrimary = transformOn(vpPrimary, spec.yAxis)
        val transformSecondary = vpSecondary?.let { transformOn(it, spec.yAxisSecondary ?: spec.yAxis) }

        fun transformFor(binding: CombinedCartesianYAxisBinding): ChartTransform =
            when (binding) {
                CombinedCartesianYAxisBinding.SharedY -> transformPrimary
                CombinedCartesianYAxisBinding.SecondaryY ->
                    transformSecondary ?: transformPrimary
            }

        fun viewportFor(binding: CombinedCartesianYAxisBinding): ChartViewport =
            when (binding) {
                CombinedCartesianYAxisBinding.SharedY -> vpPrimary
                CombinedCartesianYAxisBinding.SecondaryY ->
                    vpSecondary ?: vpPrimary
            }

        val hitById = plan.hitTestLayers.associateBy { it.layerId }

        for (layer in spec.layers.asReversed()) {
            when (layer) {
                is CombinedCartesianLayer.Bar -> {
                    val payload = hitById[layer.id] as? CombinedHitTestLayer.Bars ?: continue
                    val sel =
                        BarHitResolver.resolvePlanned(
                            segmentsHitTestFirst = payload.segmentsDrawOrder.forHitTestFromDrawOrder(),
                            touchPoint = touchPx,
                        ) ?: continue
                    return sel
                }
                is CombinedCartesianLayer.Line -> {
                    if (hitById[layer.id] !is CombinedHitTestLayer.Lines) continue
                    val t = transformFor(layer.yAxisBinding)
                    val candidates =
                        layer.series.mapNotNull { series ->
                            val indexed =
                                LineRenderPipeline.prepareIndexed(
                                    series.toXyPoints(),
                                    viewportFor(layer.yAxisBinding),
                                    spec.lineRender,
                                )
                            if (indexed.isEmpty()) return@mapNotNull null
                            NearestPointResolver.resolveIndexed(
                                seriesId = series.id,
                                indexedPoints = indexed,
                                touchPoint = touchPx,
                                transform = t,
                                maxDistancePx = lineMaxDistancePx,
                                layerId = layer.id,
                            )
                        }
                    val best = candidates.minByOrNull { selectionDistance(it.pixelOffset, touchPx) }
                    if (best != null) return best
                }
                is CombinedCartesianLayer.Scatter -> {
                    if (hitById[layer.id] !is CombinedHitTestLayer.Scatters) continue
                    val t = transformFor(layer.yAxisBinding)
                    val vp = viewportFor(layer.yAxisBinding)
                    val candidates =
                        layer.series.mapNotNull { series ->
                            val rPx = series.pointStyle.radius.coerceAtLeast(0.0)
                            val indexed =
                                if (spec.scatterRender.maxPointsPerSeries <= 0) {
                                    ScatterPlotPoints.indexedInPlotBounds(
                                        series.points,
                                        t,
                                        plotBounds,
                                        radiusPx = rPx,
                                    )
                                } else {
                                    val prepared =
                                        ScatterRenderPipeline.prepareIndexed(
                                            series.toXyPoints(),
                                            vp,
                                            spec.scatterRender,
                                        )
                                    ScatterPlotPoints.filterPreparedToPlotDisc(
                                        prepared,
                                        t,
                                        plotBounds,
                                        radiusPx = rPx,
                                    )
                                }
                            if (indexed.isEmpty()) return@mapNotNull null
                            NearestPointResolver
                                .resolveIndexed(
                                    seriesId = series.id,
                                    indexedPoints = indexed,
                                    touchPoint = touchPx,
                                    transform = t,
                                    maxDistancePx = lineMaxDistancePx,
                                )?.let { sel ->
                                    ChartSelection(
                                        target = sel.target.copy(layerId = layer.id),
                                        pixelOffset = sel.pixelOffset,
                                        markerAnchor = sel.markerAnchor,
                                        hitBounds = sel.hitBounds,
                                    )
                                }
                        }
                    val best = candidates.minByOrNull { selectionDistance(it.pixelOffset, touchPx) }
                    if (best != null) return best
                }
            }
        }
        return null
    }
}

private fun selectionDistance(
    a: OffsetD,
    touch: OffsetD,
): Double {
    val dx = a.x - touch.x
    val dy = a.y - touch.y
    return dx * dx + dy * dy
}
