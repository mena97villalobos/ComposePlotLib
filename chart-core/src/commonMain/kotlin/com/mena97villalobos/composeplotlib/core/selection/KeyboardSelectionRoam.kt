package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.BarChartPlan
import com.mena97villalobos.composeplotlib.core.layout.CombinedCartesianChartPlan
import com.mena97villalobos.composeplotlib.core.layout.CombinedHitTestLayer
import com.mena97villalobos.composeplotlib.core.layout.PlannedBarSegment
import com.mena97villalobos.composeplotlib.core.layout.forHitTestFromDrawOrder
import com.mena97villalobos.composeplotlib.core.model.toXyPoints
import com.mena97villalobos.composeplotlib.core.spec.BarChartOrientation
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianLayer
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianYAxisBinding
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PieChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import com.mena97villalobos.composeplotlib.core.viewport.normalizedNonDegenerate
import kotlin.math.max

/**
 * Pure selection-step helpers for **keyboard roam** (Shift+arrows on the chart surface). Uses full
 * series point lists in domain index order (not decimated render indices) so each key moves to an adjacent
 * data point.
 */
object KeyboardSelectionRoam {
    fun lineChartRoam(
        spec: LineChartSpec,
        viewport: ChartViewport,
        plotBounds: RectD,
        current: ChartSelection?,
        deltaPoint: Int,
        deltaSeries: Int,
    ): ChartSelection? {
        val seriesList = spec.series.filter { it.points.isNotEmpty() }
        if (seriesList.isEmpty()) return null
        if (deltaPoint == 0 && deltaSeries == 0) return current

        var sIdx = seriesList.indexOfFirst { it.id == current?.target?.seriesId }
        if (sIdx < 0) sIdx = 0

        if (deltaSeries != 0) {
            sIdx = (sIdx + deltaSeries).coerceIn(0, seriesList.lastIndex)
        }

        val series = seriesList[sIdx]
        val points = series.toXyPoints()
        if (points.isEmpty()) return null

        var pIdx =
            if (current != null && current.target.seriesId == series.id) {
                current.target.pointIndex
            } else {
                -1
            }

        if (deltaPoint != 0) {
            if (pIdx < 0) {
                pIdx = if (deltaPoint > 0) 0 else points.lastIndex
            } else {
                pIdx = (pIdx + deltaPoint).coerceIn(0, points.lastIndex)
            }
        } else if (pIdx < 0) {
            pIdx = 0
        }

        val point = points[pIdx]
        val transform = ChartTransform.forPlotBounds(viewport.normalizedNonDegenerate(), plotBounds)
        val pixel = transform.toPixel(point)
        return ChartSelection(
            target =
                HighlightTarget(
                    seriesId = series.id,
                    pointIndex = pIdx,
                    point = point,
                ),
            pixelOffset = pixel,
        )
    }

    fun scatterChartRoam(
        spec: ScatterChartSpec,
        viewport: ChartViewport,
        plotBounds: RectD,
        current: ChartSelection?,
        deltaPoint: Int,
        deltaSeries: Int,
    ): ChartSelection? {
        val seriesList = spec.series.filter { it.points.isNotEmpty() }
        if (seriesList.isEmpty()) return null
        if (deltaPoint == 0 && deltaSeries == 0) return current

        var sIdx = seriesList.indexOfFirst { it.id == current?.target?.seriesId }
        if (sIdx < 0) sIdx = 0

        if (deltaSeries != 0) {
            sIdx = (sIdx + deltaSeries).coerceIn(0, seriesList.lastIndex)
        }

        val series = seriesList[sIdx]
        val points = series.toXyPoints()
        if (points.isEmpty()) return null

        var pIdx =
            if (current != null && current.target.seriesId == series.id) {
                current.target.pointIndex
            } else {
                -1
            }

        if (deltaPoint != 0) {
            if (pIdx < 0) {
                pIdx = if (deltaPoint > 0) 0 else points.lastIndex
            } else {
                pIdx = (pIdx + deltaPoint).coerceIn(0, points.lastIndex)
            }
        } else if (pIdx < 0) {
            pIdx = 0
        }

        val point = points[pIdx]
        val transform = ChartTransform.forPlotBounds(viewport.normalizedNonDegenerate(), plotBounds)
        val pixel = transform.toPixel(point)
        return ChartSelection(
            target =
                HighlightTarget(
                    seriesId = series.id,
                    pointIndex = pIdx,
                    point = point,
                ),
            pixelOffset = pixel,
        )
    }

    fun barChartRoam(
        plan: BarChartPlan,
        current: ChartSelection?,
        delta: Int,
    ): ChartSelection? {
        if (delta == 0) return current
        val ordered =
            plan.barSegmentsForHitTest()
                .sortedBy { seg ->
                    when (plan.orientation) {
                        BarChartOrientation.Vertical -> (seg.bounds.left + seg.bounds.right) * 0.5
                        BarChartOrientation.Horizontal -> (seg.bounds.top + seg.bounds.bottom) * 0.5
                    }
                }
        if (ordered.isEmpty()) return null

        val currentIdx =
            current?.let { sel ->
                ordered.indexOfFirst {
                    it.groupId == sel.target.seriesId && it.categoryIndex == sel.target.pointIndex
                }
            } ?: -1

        val nextIdx =
            when {
                currentIdx < 0 && delta > 0 -> 0
                currentIdx < 0 && delta < 0 -> ordered.lastIndex
                else -> (currentIdx + delta).coerceIn(0, ordered.lastIndex)
            }
        return chartSelectionForBarSegment(ordered[nextIdx])
    }

    fun pieChartRoam(
        spec: PieChartSpec,
        plotBounds: RectD,
        current: ChartSelection?,
        delta: Int,
    ): ChartSelection? {
        if (delta == 0) return current
        return PieHitResolver.selectionForContributingOrdinal(
            spec = spec,
            plotBounds = plotBounds,
            current = current,
            delta = delta,
        )
    }

    fun combinedCartesianRoam(
        spec: CombinedCartesianChartSpec,
        plan: CombinedCartesianChartPlan,
        primaryViewport: ChartViewport,
        plotBounds: RectD,
        current: ChartSelection?,
        delta: Int,
    ): ChartSelection? {
        if (delta == 0) return current
        val slots = buildCombinedRoamSelections(spec, plan, primaryViewport, plotBounds)
        if (slots.isEmpty()) return null

        val currentIdx =
            current?.let { sel ->
                slots.indexOfFirst { matchesCombinedSelection(it, sel) }
            } ?: -1

        val nextIdx =
            when {
                currentIdx < 0 && delta > 0 -> 0
                currentIdx < 0 && delta < 0 -> slots.lastIndex
                else -> (currentIdx + delta).coerceIn(0, slots.lastIndex)
            }
        return slots[nextIdx]
    }

    private fun matchesCombinedSelection(
        a: ChartSelection,
        b: ChartSelection,
    ): Boolean {
        val ta = a.target
        val tb = b.target
        return ta.seriesId == tb.seriesId &&
            ta.pointIndex == tb.pointIndex &&
            ta.layerId == tb.layerId
    }

    private fun buildCombinedRoamSelections(
        spec: CombinedCartesianChartSpec,
        plan: CombinedCartesianChartPlan,
        primaryViewport: ChartViewport,
        plotBounds: RectD,
    ): List<ChartSelection> {
        val vpPrimary = primaryViewport.normalizedNonDegenerate()
        val vpSecondary = plan.secondaryViewport?.normalizedNonDegenerate()

        fun transformFor(binding: CombinedCartesianYAxisBinding): ChartTransform =
            when (binding) {
                CombinedCartesianYAxisBinding.SharedY -> ChartTransform.forPlotBounds(vpPrimary, plotBounds)
                CombinedCartesianYAxisBinding.SecondaryY ->
                    ChartTransform.forPlotBounds(vpSecondary ?: vpPrimary, plotBounds)
            }
        val hitById = plan.hitTestLayers.associateBy { it.layerId }
        val out = ArrayList<ChartSelection>()

        for (layer in spec.layers) {
            when (layer) {
                is CombinedCartesianLayer.Line -> {
                    val t = transformFor(layer.yAxisBinding)
                    for (series in layer.series) {
                        val points = series.toXyPoints()
                        for (i in points.indices) {
                            val p = points[i]
                            val pixel = t.toPixel(p)
                            out.add(
                                ChartSelection(
                                    target =
                                        HighlightTarget(
                                            seriesId = series.id,
                                            pointIndex = i,
                                            point = p,
                                            layerId = layer.id,
                                        ),
                                    pixelOffset = pixel,
                                ),
                            )
                        }
                    }
                }
                is CombinedCartesianLayer.Scatter -> {
                    val t = transformFor(layer.yAxisBinding)
                    for (series in layer.series) {
                        val points = series.toXyPoints()
                        for (i in points.indices) {
                            val p = points[i]
                            val pixel = t.toPixel(p)
                            out.add(
                                ChartSelection(
                                    target =
                                        HighlightTarget(
                                            seriesId = series.id,
                                            pointIndex = i,
                                            point = p,
                                            layerId = layer.id,
                                        ),
                                    pixelOffset = pixel,
                                ),
                            )
                        }
                    }
                }
                is CombinedCartesianLayer.Bar -> {
                    val payload = hitById[layer.id] as? CombinedHitTestLayer.Bars ?: continue
                    val ordered =
                        payload.segmentsDrawOrder.forHitTestFromDrawOrder()
                            .sortedBy { seg ->
                                when (layer.orientation) {
                                    BarChartOrientation.Vertical -> (seg.bounds.left + seg.bounds.right) * 0.5
                                    BarChartOrientation.Horizontal -> (seg.bounds.top + seg.bounds.bottom) * 0.5
                                }
                            }
                    for (seg in ordered) {
                        val sel = chartSelectionForBarSegment(seg) ?: continue
                        out.add(
                            ChartSelection(
                                target = sel.target.copy(layerId = layer.id),
                                pixelOffset = sel.pixelOffset,
                                markerAnchor = sel.markerAnchor,
                                hitBounds = sel.hitBounds,
                            ),
                        )
                    }
                }
            }
        }
        return out
    }

    private fun chartSelectionForBarSegment(hit: PlannedBarSegment): ChartSelection? {
        if (!hit.bounds.left.isFinite() || !hit.bounds.top.isFinite()) return null
        val topCenter =
            when (hit.orientation) {
                BarChartOrientation.Vertical ->
                    OffsetD((hit.bounds.left + hit.bounds.right) * 0.5, hit.bounds.top)
                BarChartOrientation.Horizontal -> {
                    val outerX = max(hit.bounds.left, hit.bounds.right)
                    OffsetD(outerX, (hit.bounds.top + hit.bounds.bottom) * 0.5)
                }
            }
        return ChartSelection(
            target =
                HighlightTarget(
                    seriesId = hit.groupId,
                    pointIndex = hit.categoryIndex,
                    point = XyPoint(hit.entry.x, hit.entry.y),
                    layerId = hit.layerId,
                ),
            pixelOffset = topCenter,
            markerAnchor = hit.bounds.center,
            hitBounds = hit.bounds,
        )
    }
}
