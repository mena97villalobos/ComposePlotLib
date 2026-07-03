package com.mena97villalobos.composeplotlib.core.layout.planning

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.AxisTick
import com.mena97villalobos.composeplotlib.core.layout.CartesianFrame
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutInput
import com.mena97villalobos.composeplotlib.core.layout.ChartTextMeasurementInput
import com.mena97villalobos.composeplotlib.core.layout.TICK_LABEL_CANVAS_EDGE_MARGIN
import com.mena97villalobos.composeplotlib.core.layout.X_TICK_LABEL_MIN_HORIZONTAL_GAP
import com.mena97villalobos.composeplotlib.core.layout.X_TICK_LABEL_TOP_INSET
import com.mena97villalobos.composeplotlib.core.layout.X_TICK_STAGGER_ROW_GAP
import com.mena97villalobos.composeplotlib.core.layout.Y_TICK_LABEL_AXIS_PADDING
import com.mena97villalobos.composeplotlib.core.layout.Y_TICK_LABEL_MIN_VERTICAL_GAP
import com.mena97villalobos.composeplotlib.core.layout.buildLegendEntries
import com.mena97villalobos.composeplotlib.core.layout.buildReservedRegions
import com.mena97villalobos.composeplotlib.core.math.FpMath
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.LegendSpec
import com.mena97villalobos.composeplotlib.core.spec.XTickLabelOverlapStrategy
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private fun Double.normalizedLabelLayoutScale(): Double = coerceIn(0.5, 4.0)

/**
 * Cartesian **final layout** pass: bounds, tick values, reserved chrome, density-reduced ticks, overlap/clip, min-visible soft targets.
 *
 * Ordering matches [com.mena97villalobos.composeplotlib.core.layout.ChartPlanner] file-level KDoc: PlanningBounds → [TickValuePass] → reserved regions → plot bounds + transform → density + positioned ticks + overlap.
 */
internal object CartesianLayoutPlanner {
    fun resolveCartesianFrame(
        input: ChartLayoutInput,
        xAxis: AxisSpec,
        yAxis: AxisSpec,
        legend: LegendSpec,
        contentPadding: InsetsD,
        legendLabelColors: List<Pair<String, ChartColor>>,
        rightAxisTickLabels: List<String> = emptyList(),
        reserveRightYAxisSpace: Boolean = false,
        tickValuePass: TickValuePass? = null,
    ): CartesianFrame {
        val labelScale = input.labelLayoutScale.normalizedLabelLayoutScale()
        val bounds = input.toPlanningBounds()
        val tickPass = tickValuePass ?: TickValuePlanner.compute(bounds.viewport, xAxis, yAxis)
        val legendEntries =
            buildLegendEntries(
                labels = legendLabelColors,
                canvasBounds = bounds.canvasBounds,
                minimumInsets = contentPadding,
                textMeasurements = input.textMeasurements,
                legendSpec = legend,
                labelLayoutScale = labelScale,
            )
        val xLabelsForReserve = tickPass.xLabels(xAxis)
        val maxXTickLabelHeight =
            xLabelsForReserve.maxOfOrNull { lbl ->
                input.textMeasurements.tickLabelMetrics(lbl).height
            } ?: 0.0
        val xStaggerExtraBelow =
            if (xAxis.enabled && xAxis.xTickLabelOverlapStrategy == XTickLabelOverlapStrategy.StaggerAdjacent) {
                maxXTickLabelHeight + X_TICK_STAGGER_ROW_GAP * labelScale
            } else {
                0.0
            }
        val reservedRegions =
            buildReservedRegions(
                canvasBounds = bounds.canvasBounds,
                minimumInsets = contentPadding,
                xTickLabels = xLabelsForReserve,
                yTickLabels = tickPass.yLabels(yAxis),
                legendEntries = legendEntries,
                textMeasurements = input.textMeasurements,
                xAxisEnabled = xAxis.enabled,
                yAxisEnabled = yAxis.enabled,
                labelLayoutScale = labelScale,
                rightAxisTickLabels = rightAxisTickLabels,
                reserveRightYAxisSpace = reserveRightYAxisSpace,
                xStaggerExtraBelowPlot = xStaggerExtraBelow,
            )
        val plotBounds = bounds.canvasBounds.inset(reservedRegions.contentInsets)
        val transform =
            ChartTransform.forPlotBounds(bounds.viewport, plotBounds, xScale = xAxis.scale, yScale = yAxis.scale)
        val xGap = (xAxis.minHorizontalTickLabelGapPx ?: X_TICK_LABEL_MIN_HORIZONTAL_GAP) * labelScale
        val yGap = (yAxis.minVerticalTickLabelGapPx ?: Y_TICK_LABEL_MIN_VERTICAL_GAP) * labelScale
        val xMinDensityCap =
            minimumCapFromMeasuredLabelWidth(
                axis = xAxis,
                axisSpanPx = plotBounds.width,
                labels = tickPass.xLabels(xAxis),
                textMeasurements = input.textMeasurements,
                labelGapPx = xGap,
            )
        val yMinDensityCap =
            minimumCapFromMeasuredLabelWidth(
                axis = yAxis,
                axisSpanPx = plotBounds.height,
                labels = tickPass.yLabels(yAxis),
                textMeasurements = input.textMeasurements,
                labelGapPx = yGap,
            )
        val xValsDense =
            TickDensityReducer.reduce(
                tickPass.xValues,
                plotBounds.width,
                xAxis,
                minimumCapFromMeasuredLabelWidth = xMinDensityCap,
            )
        val yValsDense =
            TickDensityReducer.reduce(
                tickPass.yValues,
                plotBounds.height,
                yAxis,
                minimumCapFromMeasuredLabelWidth = yMinDensityCap,
            )
        val xTicksRaw =
            buildXTicks(
                plotBounds,
                bounds.viewport,
                xValsDense,
                input.textMeasurements,
                xAxis,
                labelLayoutScale = labelScale,
            )
        val yTicksRaw =
            buildYTicks(
                plotBounds,
                bounds.viewport,
                yValsDense,
                input.textMeasurements,
                yAxis,
                labelLayoutScale = labelScale,
            )
        val edgeMargin = TICK_LABEL_CANVAS_EDGE_MARGIN * labelScale
        val xTicks =
            enforceMinimumVisibleXTickLabels(
                hideTickLabelsOutsideCanvasX(
                    resolveXTickLabelOverlap(xTicksRaw, xGap, xAxis.xTickLabelOverlapStrategy, labelScale),
                    bounds.canvasBounds,
                    edgeMargin,
                ),
                min = xAxis.minVisibleXTickLabels,
                minGap = xGap,
                strategy = xAxis.xTickLabelOverlapStrategy,
                canvas = bounds.canvasBounds,
                canvasMargin = edgeMargin,
            )
        val yTicks =
            enforceMinimumVisibleYTickLabels(
                hideTickLabelsOutsideCanvasY(
                    suppressOverlappingYTickLabels(yTicksRaw, yGap),
                    bounds.canvasBounds,
                    edgeMargin,
                ),
                min = yAxis.minVisibleYTickLabels,
                minGap = yGap,
                canvas = bounds.canvasBounds,
                canvasMargin = edgeMargin,
            )
        return CartesianFrame(
            canvasBounds = bounds.canvasBounds,
            viewport = bounds.viewport,
            plotBounds = plotBounds,
            transform = transform,
            reservedRegions = reservedRegions,
            xTicks = xTicks,
            yTicks = yTicks,
            legendEntries = legendEntries,
        )
    }
}

private fun buildXTicks(
    plotBounds: RectD,
    viewport: ChartViewport,
    tickValues: List<Double>,
    textMeasurements: ChartTextMeasurementInput,
    axisSpec: AxisSpec,
    labelLayoutScale: Double,
): List<AxisTick> {
    val xLabelTopInset = X_TICK_LABEL_TOP_INSET * labelLayoutScale
    return tickValues.map { value ->
        val label = axisTickLabel(axisSpec, value)
        val metrics = textMeasurements.tickLabelMetrics(label)
        val fraction = if (viewport.xRange.span == 0.0) 0.0 else (value - viewport.xRange.start) / viewport.xRange.span
        val x = plotBounds.left + (plotBounds.width * fraction)
        val labelTopY = plotBounds.bottom + xLabelTopInset
        val labelLeftX = x - metrics.width / 2.0
        AxisTick(
            value = value,
            label = label,
            axisPosition = OffsetD(x, plotBounds.bottom),
            position = OffsetD(labelLeftX, labelTopY),
            labelMetrics = metrics,
        )
    }
}

private fun buildYTicks(
    plotBounds: RectD,
    viewport: ChartViewport,
    tickValues: List<Double>,
    textMeasurements: ChartTextMeasurementInput,
    axisSpec: AxisSpec,
    labelLayoutScale: Double,
): List<AxisTick> {
    val yAxisPad = Y_TICK_LABEL_AXIS_PADDING * labelLayoutScale
    return tickValues.reversed().map { value ->
        val label = axisTickLabel(axisSpec, value)
        val metrics = textMeasurements.tickLabelMetrics(label)
        val fraction = if (viewport.yRange.span == 0.0) 0.0 else (viewport.yRange.endInclusive - value) / viewport.yRange.span
        val y = plotBounds.top + (plotBounds.height * fraction)
        val ascentApprox = metrics.firstBaselineFromTop.coerceAtMost(metrics.height * 0.5)
        val boxMidToBaseline = metrics.firstBaselineFromTop - metrics.height / 2.0
        val opticalShift = (boxMidToBaseline - ascentApprox * 0.35).coerceIn(-metrics.height * 0.2, metrics.height * 0.2)
        val labelTopY = y - metrics.height / 2.0 - opticalShift
        AxisTick(
            value = value,
            label = label,
            axisPosition = OffsetD(plotBounds.left, y),
            position =
                OffsetD(
                    plotBounds.left - metrics.width - yAxisPad,
                    labelTopY,
                ),
            labelMetrics = metrics,
        )
    }
}

private fun suppressOverlappingXTickLabels(
    ticks: List<AxisTick>,
    minGap: Double,
): List<AxisTick> {
    if (ticks.size <= 1) return ticks
    val order = ticks.indices.sortedBy { ticks[it].axisPosition.x }
    val show = BooleanArray(ticks.size) { true }
    var lastRight = Double.NEGATIVE_INFINITY
    for (i in order) {
        val t = ticks[i]
        val left = t.position.x
        val right = left + t.labelMetrics.width
        if (left < lastRight + minGap) {
            show[i] = false
        } else {
            lastRight = right
        }
    }
    return ticks.mapIndexed { idx, t -> if (show[idx]) t else t.copy(showLabel = false) }
}

private fun resolveXTickLabelOverlap(
    ticks: List<AxisTick>,
    minGap: Double,
    strategy: XTickLabelOverlapStrategy,
    labelLayoutScale: Double,
): List<AxisTick> =
    when (strategy) {
        XTickLabelOverlapStrategy.HideOverlapping -> suppressOverlappingXTickLabels(ticks, minGap)
        XTickLabelOverlapStrategy.StaggerAdjacent -> staggerOverlappingXTickLabels(ticks, minGap, labelLayoutScale)
    }

private fun staggerOverlappingXTickLabels(
    ticks: List<AxisTick>,
    minGap: Double,
    labelLayoutScale: Double,
): List<AxisTick> {
    if (ticks.size <= 1) return ticks
    val rowGap = X_TICK_STAGGER_ROW_GAP * labelLayoutScale
    val rowVerticalOffset = ticks.maxOf { it.labelMetrics.height } + rowGap
    val order = ticks.indices.sortedBy { ticks[it].axisPosition.x }
    val row = IntArray(ticks.size) { 0 }
    val lastRight = doubleArrayOf(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)
    for (i in order) {
        val t = ticks[i]
        val left = t.position.x
        val right = left + t.labelMetrics.width
        var r = 0
        if (left < lastRight[0] + minGap) {
            r = 1
        }
        if (r == 1 && left < lastRight[1] + minGap) {
            row[i] = -1
            continue
        }
        row[i] = r
        lastRight[r] = max(lastRight[r], right)
    }
    return ticks.mapIndexed { idx, t ->
        when (row[idx]) {
            -1 -> t.copy(showLabel = false)
            1 -> t.copy(position = OffsetD(t.position.x, t.position.y + rowVerticalOffset))
            else -> t
        }
    }
}

private fun hideTickLabelsOutsideCanvasX(
    ticks: List<AxisTick>,
    canvas: RectD,
    margin: Double,
): List<AxisTick> {
    if (ticks.isEmpty()) return ticks
    val leftBound = canvas.left + margin
    val rightBound = canvas.right - margin
    return ticks.map { t ->
        if (!t.showLabel) return@map t
        val left = t.position.x
        val right = left + t.labelMetrics.width
        if (left < leftBound || right > rightBound) t.copy(showLabel = false) else t
    }
}

private fun hideTickLabelsOutsideCanvasY(
    ticks: List<AxisTick>,
    canvas: RectD,
    margin: Double,
): List<AxisTick> {
    if (ticks.isEmpty()) return ticks
    val leftBound = canvas.left + margin
    val rightBound = canvas.right - margin
    val topBound = canvas.top + margin
    val bottomBound = canvas.bottom - margin
    return ticks.map { t ->
        if (!t.showLabel) return@map t
        val left = t.position.x
        val right = left + t.labelMetrics.width
        val top = t.position.y
        val bottom = top + t.labelMetrics.height
        if (left < leftBound || right > rightBound || top < topBound || bottom > bottomBound) {
            t.copy(showLabel = false)
        } else {
            t
        }
    }
}

private fun suppressOverlappingYTickLabels(
    ticks: List<AxisTick>,
    minGap: Double,
): List<AxisTick> {
    if (ticks.size <= 1) return ticks
    val order = ticks.indices.sortedBy { ticks[it].axisPosition.y }
    val show = BooleanArray(ticks.size) { true }
    var lastBottom = Double.NEGATIVE_INFINITY
    for (i in order) {
        val t = ticks[i]
        val top = t.position.y
        val bottom = top + t.labelMetrics.height
        if (top < lastBottom + minGap) {
            show[i] = false
        } else {
            lastBottom = bottom
        }
    }
    return ticks.mapIndexed { idx, t -> if (show[idx]) t else t.copy(showLabel = false) }
}

private fun minimumCapFromMeasuredLabelWidth(
    axis: AxisSpec,
    axisSpanPx: Double,
    labels: List<String>,
    textMeasurements: ChartTextMeasurementInput,
    labelGapPx: Double,
): Int? {
    if (!axis.raiseTickDensityCapFromMeasuredLabelWidth || !axis.enabled) return null
    if (labels.isEmpty() || !axisSpanPx.isFinite() || axisSpanPx <= FpMath.EPS_SPAN) return null
    val sumW = labels.sumOf { textMeasurements.tickLabelMetrics(it).width }
    val avgW = sumW / labels.size
    if (!avgW.isFinite() || avgW <= 0.0) return null
    val denom = avgW + labelGapPx.coerceAtLeast(0.0)
    if (denom <= 0.0) return null
    return max(2, floor(axisSpanPx / denom).toInt())
}

private fun enforceMinimumVisibleXTickLabels(
    ticks: List<AxisTick>,
    min: Int?,
    minGap: Double,
    strategy: XTickLabelOverlapStrategy,
    canvas: RectD,
    canvasMargin: Double,
): List<AxisTick> {
    if (min == null || min <= 0 || ticks.size <= 1) return ticks
    if (strategy != XTickLabelOverlapStrategy.HideOverlapping) return ticks
    val mutable = ticks.toMutableList()

    fun visible() = mutable.count { it.showLabel }
    if (visible() >= min) return ticks
    val spread = spreadTickIndicesForMinimum(mutable.size, min)
    for (idx in spread) {
        if (visible() >= min) break
        if (!mutable[idx].showLabel && canShowXTickWithoutOverlap(mutable, idx, minGap, canvas, canvasMargin)) {
            mutable[idx] = mutable[idx].copy(showLabel = true)
        }
    }
    while (visible() < min) {
        val hidden = mutable.indices.filter { !mutable[it].showLabel }.sortedBy { mutable[it].axisPosition.x }
        if (hidden.isEmpty()) break
        var progressed = false
        for (idx in hidden) {
            if (visible() >= min) break
            if (canShowXTickWithoutOverlap(mutable, idx, minGap, canvas, canvasMargin)) {
                mutable[idx] = mutable[idx].copy(showLabel = true)
                progressed = true
            }
        }
        if (!progressed) break
    }
    return mutable
}

private fun enforceMinimumVisibleYTickLabels(
    ticks: List<AxisTick>,
    min: Int?,
    minGap: Double,
    canvas: RectD,
    canvasMargin: Double,
): List<AxisTick> {
    if (min == null || min <= 0 || ticks.size <= 1) return ticks
    val mutable = ticks.toMutableList()

    fun visible() = mutable.count { it.showLabel }
    if (visible() >= min) return ticks
    val spread = spreadTickIndicesForMinimum(mutable.size, min)
    for (idx in spread) {
        if (visible() >= min) break
        if (!mutable[idx].showLabel && canShowYTickWithoutOverlap(mutable, idx, minGap, canvas, canvasMargin)) {
            mutable[idx] = mutable[idx].copy(showLabel = true)
        }
    }
    while (visible() < min) {
        val hidden = mutable.indices.filter { !mutable[it].showLabel }.sortedBy { mutable[it].axisPosition.y }
        if (hidden.isEmpty()) break
        var progressed = false
        for (idx in hidden) {
            if (visible() >= min) break
            if (canShowYTickWithoutOverlap(mutable, idx, minGap, canvas, canvasMargin)) {
                mutable[idx] = mutable[idx].copy(showLabel = true)
                progressed = true
            }
        }
        if (!progressed) break
    }
    return mutable
}

private fun spreadTickIndicesForMinimum(
    tickCount: Int,
    minVisible: Int,
): List<Int> {
    if (tickCount <= 0 || minVisible <= 0) return emptyList()
    val m = minVisible.coerceAtMost(tickCount)
    if (m == 1) return listOf(tickCount / 2)
    return (0 until m)
        .map { k ->
            (k * (tickCount - 1).toDouble() / (m - 1)).roundToInt().coerceIn(0, tickCount - 1)
        }.distinct()
        .sorted()
}

private fun canShowXTickWithoutOverlap(
    ticks: List<AxisTick>,
    index: Int,
    minGap: Double,
    canvas: RectD,
    canvasMargin: Double,
): Boolean {
    val t = ticks[index]
    val cx = t.axisPosition.x
    val left = t.position.x
    val right = left + t.labelMetrics.width
    if (left < canvas.left + canvasMargin || right > canvas.right - canvasMargin) return false
    var prevRight = Double.NEGATIVE_INFINITY
    var nextLeft = Double.POSITIVE_INFINITY
    for (i in ticks.indices) {
        if (i == index || !ticks[i].showLabel) continue
        val o = ticks[i]
        val ox = o.axisPosition.x
        if (ox < cx) {
            val pr = o.position.x + o.labelMetrics.width
            if (pr > prevRight) prevRight = pr
        } else if (ox > cx) {
            val nl = o.position.x
            if (nl < nextLeft) nextLeft = nl
        }
    }
    return left >= prevRight + minGap && right + minGap <= nextLeft
}

private fun canShowYTickWithoutOverlap(
    ticks: List<AxisTick>,
    index: Int,
    minGap: Double,
    canvas: RectD,
    canvasMargin: Double,
): Boolean {
    val t = ticks[index]
    val cy = t.axisPosition.y
    val top = t.position.y
    val bottom = top + t.labelMetrics.height
    if (top < canvas.top + canvasMargin || bottom > canvas.bottom - canvasMargin) return false
    var prevBottom = Double.NEGATIVE_INFINITY
    var nextTop = Double.POSITIVE_INFINITY
    for (i in ticks.indices) {
        if (i == index || !ticks[i].showLabel) continue
        val o = ticks[i]
        val oy = o.axisPosition.y
        if (oy < cy) {
            val pb = o.position.y + o.labelMetrics.height
            if (pb > prevBottom) prevBottom = pb
        } else if (oy > cy) {
            val nt = o.position.y
            if (nt < nextTop) nextTop = nt
        }
    }
    return top >= prevBottom + minGap && bottom + minGap <= nextTop
}

/** Exposed for [com.mena97villalobos.composeplotlib.core.layout.ChartPlanner.planCombinedCartesianChart] secondary-Y tick pipeline. */
internal fun plannerMinimumCapFromMeasuredLabelWidth(
    axis: AxisSpec,
    axisSpanPx: Double,
    labels: List<String>,
    textMeasurements: ChartTextMeasurementInput,
    labelGapPx: Double,
): Int? = minimumCapFromMeasuredLabelWidth(axis, axisSpanPx, labels, textMeasurements, labelGapPx)

/** Y overlap suppression + canvas clip (shared with cartesian frame and combined secondary axis). */
internal fun plannerFinalizeYTicksForCanvas(
    ticks: List<AxisTick>,
    yGap: Double,
    canvas: RectD,
    edgeMargin: Double,
): List<AxisTick> =
    hideTickLabelsOutsideCanvasY(
        suppressOverlappingYTickLabels(ticks, yGap),
        canvas,
        edgeMargin,
    )
