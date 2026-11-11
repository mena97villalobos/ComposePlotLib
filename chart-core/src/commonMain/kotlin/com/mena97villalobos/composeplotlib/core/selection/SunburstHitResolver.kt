package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.PlannedSunburstSegment
import com.mena97villalobos.composeplotlib.core.layout.SunburstHierarchyPlan
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Maps a point in chart pixels to a segment of a [SunburstHierarchyPlan]: the ring is chosen by distance from the
 * center (edges inclusive) and the segment by angle. Points in the gap between two rings, outside the outermost
 * ring, or outside a partial sweep resolve to nothing.
 */
object SunburstHitResolver {
    /** The segment containing [point], or `null`. */
    fun resolve(
        point: OffsetD,
        plan: SunburstHierarchyPlan,
    ): PlannedSunburstSegment? {
        if (!point.x.isFinite() || !point.y.isFinite()) return null
        val dx = point.x - plan.center.x
        val dy = point.y - plan.center.y
        val r = sqrt(dx * dx + dy * dy)
        val angle = atan2(dy, dx) * 180.0 / PI
        return plan.segments.firstOrNull { s ->
            r >= s.innerRadius && r <= s.outerRadius && (s.depth == 0 && s.sweepAngleDegrees >= 360.0 || angleWithin(angle, s))
        }
    }

    private fun angleWithin(
        angle: Double,
        segment: PlannedSunburstSegment,
    ): Boolean {
        if (segment.sweepAngleDegrees >= 360.0) return true
        val delta = ((angle - segment.startAngleDegrees) % 360.0 + 360.0) % 360.0
        return delta <= segment.sweepAngleDegrees
    }

    /** The callback payload for [segment] of [plan]; [HierarchyNodeHit.bounds] is the segment's bounding box. */
    fun hitFor(
        segment: PlannedSunburstSegment,
        plan: SunburstHierarchyPlan,
    ): HierarchyNodeHit =
        HierarchyNodeHit(
            nodeId = segment.nodeId,
            path = segment.path,
            label = segment.label,
            value = segment.value,
            valueText = null,
            depth = segment.depth,
            hasChildren = segment.hasChildren,
            isAggregate = false,
            memberIds = emptyList(),
            bounds = segmentBounds(segment, plan.center),
        )

    /** Bounding box of a ring sector: its four corners plus every axis extreme the sweep passes. */
    internal fun segmentBounds(
        segment: PlannedSunburstSegment,
        center: OffsetD,
    ): RectD {
        val xs = ArrayList<Double>()
        val ys = ArrayList<Double>()

        fun add(
            radius: Double,
            degrees: Double,
        ) {
            val rad = degrees * PI / 180.0
            xs += center.x + radius * cos(rad)
            ys += center.y + radius * sin(rad)
        }
        val start = segment.startAngleDegrees
        val end = start + segment.sweepAngleDegrees
        for (radius in listOf(segment.innerRadius, segment.outerRadius)) {
            add(radius, start)
            add(radius, end)
        }
        var axis = kotlin.math.ceil(start / 90.0) * 90.0
        while (axis <= end) {
            add(segment.outerRadius, axis)
            axis += 90.0
        }
        return RectD(xs.min(), ys.min(), xs.max(), ys.max())
    }
}
