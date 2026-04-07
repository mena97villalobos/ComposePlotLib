package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.PieChartLayoutMath
import com.mena97villalobos.composeplotlib.core.layout.PieSweep
import com.mena97villalobos.composeplotlib.core.math.FpMath
import com.mena97villalobos.composeplotlib.core.model.PieChartSlice
import com.mena97villalobos.composeplotlib.core.spec.PieChartSpec
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Slice hit-testing in plot pixel space: angular sectors share geometry with [com.mena97villalobos.composeplotlib.core.layout.ChartPlanner.planPieChart].
 *
 * Only strictly positive slice values participate in angles (same as planning). Tap inside the hole, outside the outer
 * radius, or on a zero-value gap yields `null`.
 */
object PieHitResolver {
    /**
     * Moves selection among **positive** slices in **draw order** (same as [resolve] angular walk).
     *
     * @param delta +1 / −1 for next / previous contributing slice; [current] may be null to start at ends.
     */
    fun selectionForContributingOrdinal(
        spec: PieChartSpec,
        plotBounds: RectD,
        current: ChartSelection?,
        delta: Int,
    ): ChartSelection? {
        if (delta == 0) return current
        val radial = PieChartLayoutMath.layout(plotBounds, spec.holeRadiusRatio)
        if (radial.outerRadius <= 0.0) return null

        val contributing = spec.slices.mapIndexed { index, s -> index to s }.filter { it.second.value > 0.0 }
        if (contributing.isEmpty()) return null

        val currentIdx =
            current?.let { sel ->
                contributing.indexOfFirst { it.first == sel.target.pointIndex }
            } ?: -1

        val nextIdx =
            when {
                currentIdx < 0 && delta > 0 -> 0
                currentIdx < 0 && delta < 0 -> contributing.lastIndex
                else -> (currentIdx + delta).coerceIn(0, contributing.lastIndex)
            }
        val total = contributing.sumOf { it.second.value }
        if (total <= 0.0) return null
        val gapDeg = PieSweep.effectiveSliceGapDegrees(contributing.size, spec.sliceGapDegrees)
        val sweeps = PieSweep.sweepsDegrees(contributing, total, spec.sliceGapDegrees)
        if (sweeps.isEmpty()) return null

        var cursor = 0.0
        val inner = radial.innerRadius ?: 0.0
        for (i in contributing.indices) {
            val (sliceIndex, slice) = contributing[i]
            val sweep = sweeps[i]
            if (i == nextIdx) {
                return selectionForSlice(
                    spec = spec,
                    radialCenter = radial.center,
                    outerRadius = radial.outerRadius,
                    innerRadius = inner,
                    cursor = cursor,
                    sweep = sweep,
                    sliceIndex = sliceIndex,
                    slice = slice,
                )
            }
            cursor += sweep + gapDeg
        }
        return null
    }

    /** @param plotBounds same inset plot rect used by the planner (after legend insets). */
    fun resolve(
        touchPoint: OffsetD,
        spec: PieChartSpec,
        plotBounds: RectD,
    ): ChartSelection? {
        if (!touchPoint.x.isFinite() || !touchPoint.y.isFinite()) return null
        if (!plotBounds.contains(touchPoint)) return null
        val radial = PieChartLayoutMath.layout(plotBounds, spec.holeRadiusRatio)
        if (radial.outerRadius <= 0.0) return null

        val dx = touchPoint.x - radial.center.x
        val dy = touchPoint.y - radial.center.y
        val distSq = dx * dx + dy * dy
        val outerSq = radial.outerRadius * radial.outerRadius
        val inner = radial.innerRadius ?: 0.0
        val innerSq = inner * inner
        if (distSq < innerSq || distSq > outerSq + FpMath.EPS) return null

        val touchDeg = atan2(dy, dx) * 180.0 / PI
        var delta = touchDeg - spec.startAngleDegrees
        while (delta < 0.0) delta += 360.0
        while (delta >= 360.0) delta -= 360.0

        val contributing = spec.slices.mapIndexed { index, s -> index to s }.filter { it.second.value > 0.0 }
        val total = contributing.sumOf { it.second.value }
        if (total <= 0.0) return null
        val gapDeg = PieSweep.effectiveSliceGapDegrees(contributing.size, spec.sliceGapDegrees)
        val sweeps = PieSweep.sweepsDegrees(contributing, total, spec.sliceGapDegrees)
        if (sweeps.isEmpty()) return null

        var cursor = 0.0
        for ((i, pair) in contributing.withIndex()) {
            val (sliceIndex, slice) = pair
            val sweep = sweeps[i]
            val end = cursor + sweep
            val hit = delta + FpMath.EPS >= cursor && delta < end - FpMath.EPS
            if (hit) {
                return selectionForSlice(
                    spec = spec,
                    radialCenter = radial.center,
                    outerRadius = radial.outerRadius,
                    innerRadius = inner,
                    cursor = cursor,
                    sweep = sweep,
                    sliceIndex = sliceIndex,
                    slice = slice,
                )
            }
            cursor = end + gapDeg
        }
        return null
    }

    private fun selectionForSlice(
        spec: PieChartSpec,
        radialCenter: OffsetD,
        outerRadius: Double,
        innerRadius: Double,
        cursor: Double,
        sweep: Double,
        sliceIndex: Int,
        slice: PieChartSlice,
    ): ChartSelection {
        val midDeg = spec.startAngleDegrees + cursor + sweep / 2.0
        val midRad = midDeg * PI / 180.0
        val anchorR =
            if (innerRadius > 0.0) {
                (innerRadius + outerRadius) / 2.0
            } else {
                outerRadius * 0.65
            }
        val px = radialCenter.x + anchorR * cos(midRad)
        val py = radialCenter.y + anchorR * sin(midRad)
        val anchor = OffsetD(px, py)
        return ChartSelection(
            target =
                HighlightTarget(
                    seriesId = spec.id,
                    pointIndex = sliceIndex,
                    point = XyPoint(slice.value, sliceIndex.toDouble()),
                ),
            pixelOffset = anchor,
            markerAnchor = anchor,
        )
    }
}
