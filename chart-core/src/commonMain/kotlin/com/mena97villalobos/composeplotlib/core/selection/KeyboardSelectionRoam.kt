package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.BarChartPlan
import com.mena97villalobos.composeplotlib.core.layout.BoxPlotChartPlan
import com.mena97villalobos.composeplotlib.core.layout.CandlestickChartPlan
import com.mena97villalobos.composeplotlib.core.layout.CombinedCartesianChartPlan
import com.mena97villalobos.composeplotlib.core.layout.CombinedHitTestLayer
import com.mena97villalobos.composeplotlib.core.layout.HeatmapChartPlan
import com.mena97villalobos.composeplotlib.core.layout.PlannedBarSegment
import com.mena97villalobos.composeplotlib.core.layout.PlannedCandle
import com.mena97villalobos.composeplotlib.core.layout.PlannedHeatmapCell
import com.mena97villalobos.composeplotlib.core.layout.WaterfallChartPlan
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
 * Pure selection-step helpers for **keyboard roam** (Shift+arrows on the chart surface). Line and scatter roam
 * over full series point lists in domain index order; bar and candlestick roam over the **planned** (possibly
 * decimated) geometry, relocating the current selection by its stable global point index.
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
        // The axis scales and inversions have to come along: `forPlotBounds` defaults them to
        // Linear and false, so roaming a log or inverted axis with the arrow keys otherwise
        // recorded a pixel from a straight-line mapping the chart is not using.
        val transform =
            ChartTransform.forPlotBounds(
                viewport.normalizedNonDegenerate(),
                plotBounds,
                xScale = spec.xAxis.scale,
                yScale = spec.yAxis.scale,
                invertX = spec.xAxis.inverted,
                invertY = spec.yAxis.inverted,
            )
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
        // The axis scales and inversions have to come along: `forPlotBounds` defaults them to
        // Linear and false, so roaming a log or inverted axis with the arrow keys otherwise
        // recorded a pixel from a straight-line mapping the chart is not using.
        val transform =
            ChartTransform.forPlotBounds(
                viewport.normalizedNonDegenerate(),
                plotBounds,
                xScale = spec.xAxis.scale,
                yScale = spec.yAxis.scale,
                invertX = spec.xAxis.inverted,
                invertY = spec.yAxis.inverted,
            )
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
            plan
                .barSegmentsForHitTest()
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

    /**
     * Keyboard selection-roam over **planned** candles: Shift+Left/Right step [deltaPoint] within a series
     * along the x (time) axis; Shift+Up/Down switch series via [deltaSeries]. Both axes **clamp** (no wrap).
     * Builds a [ChartSelection] identical to [CandlestickHitResolver.resolvePlanned] so roam and tap selections
     * are interchangeable. Steps over `plan.candles` (possibly decimated), like [barChartRoam] over bar segments.
     */
    fun candlestickChartRoam(
        plan: CandlestickChartPlan,
        current: ChartSelection?,
        deltaPoint: Int,
        deltaSeries: Int,
    ): ChartSelection? = candlestickChartRoam(plan.candles, current, deltaPoint, deltaSeries)

    internal fun candlestickChartRoam(
        candles: List<PlannedCandle>,
        current: ChartSelection?,
        deltaPoint: Int,
        deltaSeries: Int,
    ): ChartSelection? {
        if (deltaPoint == 0 && deltaSeries == 0) return current
        if (candles.isEmpty()) return null

        // Series order = first-seen order in the draw-order candle list (mirrors spec.series order).
        val seriesIds = candles.map { it.seriesId }.distinct()

        var sIdx = seriesIds.indexOfFirst { it == current?.target?.seriesId }
        if (sIdx < 0) sIdx = 0
        if (deltaSeries != 0) {
            sIdx = (sIdx + deltaSeries).coerceIn(0, seriesIds.lastIndex)
        }
        val seriesId = seriesIds[sIdx]

        // Walk along the x (time) axis within the resolved series.
        val ordered = candles.filter { it.seriesId == seriesId }.sortedBy { it.point.x }
        if (ordered.isEmpty()) return null

        // PlannedCandle.index is the GLOBAL point index (decimation-stable) — relocate by it, not positionally.
        var pIdx =
            if (current != null && current.target.seriesId == seriesId) {
                ordered.indexOfFirst { it.index == current.target.pointIndex }
            } else {
                -1
            }
        if (deltaPoint != 0) {
            pIdx =
                if (pIdx < 0) {
                    if (deltaPoint > 0) 0 else ordered.lastIndex
                } else {
                    (pIdx + deltaPoint).coerceIn(0, ordered.lastIndex)
                }
        } else if (pIdx < 0) {
            pIdx = 0
        }
        return chartSelectionForCandle(ordered[pIdx])
    }

    /**
     * Keyboard selection-roam over **planned** boxes: Shift+Left/Right step [deltaPoint] within a series along the
     * x (category) axis; Shift+Up/Down switch series via [deltaSeries]. Both axes **clamp** (no wrap). Builds a
     * [ChartSelection] identical to [BoxPlotHitResolver.resolvePlanned] so roam and tap selections are interchangeable.
     */
    fun boxPlotChartRoam(
        plan: BoxPlotChartPlan,
        current: ChartSelection?,
        deltaPoint: Int,
        deltaSeries: Int,
    ): ChartSelection? {
        if (deltaPoint == 0 && deltaSeries == 0) return current
        val boxes = plan.boxes
        if (boxes.isEmpty()) return null

        val seriesIds = boxes.map { it.seriesId }.distinct()
        var sIdx = seriesIds.indexOfFirst { it == current?.target?.seriesId }
        if (sIdx < 0) sIdx = 0
        if (deltaSeries != 0) {
            sIdx = (sIdx + deltaSeries).coerceIn(0, seriesIds.lastIndex)
        }
        val seriesId = seriesIds[sIdx]

        val ordered = boxes.filter { it.seriesId == seriesId }.sortedBy { it.entry.x }
        if (ordered.isEmpty()) return null

        var pIdx =
            if (current != null && current.target.seriesId == seriesId) {
                ordered.indexOfFirst { it.index == current.target.pointIndex }
            } else {
                -1
            }
        if (deltaPoint != 0) {
            pIdx =
                if (pIdx < 0) {
                    if (deltaPoint > 0) 0 else ordered.lastIndex
                } else {
                    (pIdx + deltaPoint).coerceIn(0, ordered.lastIndex)
                }
        } else if (pIdx < 0) {
            pIdx = 0
        }
        return BoxPlotHitResolver.selectionFor(ordered[pIdx])
    }

    /**
     * Keyboard selection-roam over **planned** waterfall bars: Shift+Left/Right step [delta] along the sequence
     * (single logical series). Clamps at both ends (no wrap). Builds a [ChartSelection] identical to
     * [WaterfallHitResolver.resolvePlanned] so roam and tap selections are interchangeable.
     */
    fun waterfallChartRoam(
        plan: WaterfallChartPlan,
        current: ChartSelection?,
        delta: Int,
    ): ChartSelection? {
        if (delta == 0) return current
        val ordered = plan.bars.sortedBy { (it.bounds.left + it.bounds.right) * 0.5 }
        if (ordered.isEmpty()) return null

        val currentIdx =
            current?.let { sel -> ordered.indexOfFirst { it.index == sel.target.pointIndex } } ?: -1
        val nextIdx =
            when {
                currentIdx < 0 && delta > 0 -> 0
                currentIdx < 0 && delta < 0 -> ordered.lastIndex
                else -> (currentIdx + delta).coerceIn(0, ordered.lastIndex)
            }
        return WaterfallHitResolver.selectionFor(ordered[nextIdx])
    }

    /**
     * Keyboard selection-roam over a heatmap grid: [deltaCol] steps columns (Left/Right), [deltaRow] steps rows
     * (Up increases the row index since row 0 is at the bottom). Clamps at the grid edges (no wrap); when the target
     * coordinate has no cell (sparse grid) it keeps stepping in the same direction until a cell is found or the edge
     * is reached. From no selection, selects the bottom-left-most cell. Builds a [ChartSelection] identical to
     * [HeatmapHitResolver.resolve].
     */
    fun heatmapRoam(
        plan: HeatmapChartPlan,
        specId: String,
        current: ChartSelection?,
        deltaCol: Int,
        deltaRow: Int,
    ): ChartSelection? {
        if (deltaCol == 0 && deltaRow == 0) return current
        val cells = plan.heatmapCells
        if (cells.isEmpty()) return null

        val byCoord = HashMap<Pair<Int, Int>, PlannedHeatmapCell>(cells.size)
        var maxCol = 0
        var maxRow = 0
        for (c in cells) {
            byCoord[c.col to c.row] = c
            if (c.col > maxCol) maxCol = c.col
            if (c.row > maxRow) maxRow = c.row
        }

        if (current == null || current.target.seriesId != specId) {
            val start = cells.minWith(compareBy({ it.row }, { it.col }))
            return HeatmapHitResolver.selectionFor(start, specId)
        }

        val point = current.target.point
        var col = point.x.toInt()
        var row = point.y.toInt()
        while (true) {
            val nextCol = col + deltaCol
            val nextRow = row + deltaRow
            if (nextCol < 0 || nextCol > maxCol || nextRow < 0 || nextRow > maxRow) {
                return current
            }
            col = nextCol
            row = nextRow
            val cell = byCoord[col to row]
            if (cell != null) return HeatmapHitResolver.selectionFor(cell, specId)
        }
    }

    private fun chartSelectionForCandle(hit: PlannedCandle): ChartSelection {
        val topCenter = OffsetD((hit.bodyBounds.left + hit.bodyBounds.right) / 2.0, hit.bodyBounds.top)
        return ChartSelection(
            target =
                HighlightTarget(
                    seriesId = hit.seriesId,
                    pointIndex = hit.index,
                    point = XyPoint(hit.point.x, hit.point.close),
                ),
            pixelOffset = topCenter,
            markerAnchor = hit.bodyBounds.center,
            hitBounds = hit.hitBounds,
        )
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
                        payload.segmentsDrawOrder
                            .forHitTestFromDrawOrder()
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
