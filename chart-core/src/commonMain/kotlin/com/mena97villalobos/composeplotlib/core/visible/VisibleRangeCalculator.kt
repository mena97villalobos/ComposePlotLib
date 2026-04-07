package com.mena97villalobos.composeplotlib.core.visible

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.math.FpMath
import com.mena97villalobos.composeplotlib.core.range.IndexRange
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport

/**
 * Finds which points participate in the visible portion of a line series inside the viewport’s
 * **domain rectangle** ([ChartViewport.xRange] × [ChartViewport.yRange]).
 *
 * A segment between consecutive samples is visible if it intersects that axis-aligned rectangle
 * (including chords that cross when both endpoints lie outside). This matches pixel-space clipping
 * after an axis-aligned domain→plot mapping more closely than x-only filtering.
 *
 * Monotonic x in domain space is typical for line charts; indices returned remain a single contiguous
 * span suitable for polylines.
 */
object VisibleRangeCalculator {
    /**
     * Small horizontal slack (px) when culling bar columns against [RectD] plot bounds so wide bars
     * sitting on viewport edges are not dropped by float / half-pixel error.
     */
    const val BAR_COLUMN_PLOT_EDGE_SLACK_PX: Double = 0.5

    /** Smallest [IndexRange] covering all points that belong to at least one visible segment, or `null` if none. */
    fun visiblePointRange(
        points: List<XyPoint>,
        viewport: ChartViewport,
    ): IndexRange? {
        if (points.isEmpty()) {
            return null
        }

        if (points.size == 1) {
            val p = points[0]
            return if (pointInsideViewport(p.x, p.y, viewport)) {
                IndexRange(0, 0)
            } else {
                null
            }
        }

        var firstVisible = -1
        var lastVisible = -1

        for (i in 0 until points.lastIndex) {
            val a = points[i]
            val b = points[i + 1]
            if (segmentIntersectsViewportDomain(a.x, a.y, b.x, b.y, viewport)) {
                if (firstVisible == -1) {
                    firstVisible = i
                }
                lastVisible = i + 1
            }
        }

        if (firstVisible == -1 || lastVisible == -1) {
            return null
        }

        return IndexRange(firstVisible, lastVisible)
    }

    /**
     * Like [visiblePointRange] but expands the span by up to [paddingEachSide] indices so the polyline
     * still has endpoints to intersect plot edges after pixel-space clipping.
     */
    fun visiblePointRangeWithContinuity(
        points: List<XyPoint>,
        viewport: ChartViewport,
        paddingEachSide: Int = 1,
    ): IndexRange? {
        val base = visiblePointRange(points, viewport) ?: return null
        if (paddingEachSide <= 0) return base
        val start = (base.start - paddingEachSide).coerceAtLeast(0)
        val end = (base.endInclusive + paddingEachSide).coerceAtMost(points.lastIndex)
        return IndexRange(start, end)
    }

    /** Sublist view of [points] restricted to indices from [visiblePointRange], or empty if none visible. */
    fun visiblePoints(
        points: List<XyPoint>,
        viewport: ChartViewport,
    ): List<XyPoint> {
        val range = visiblePointRange(points, viewport) ?: return emptyList()
        return points.subList(range.start, range.endInclusive + 1)
    }

    /**
     * Scatter charts: every point whose domain coordinates lie inside the viewport **domain** rectangle
     * ([ChartViewport.xRange] × [ChartViewport.yRange]). Preserves series order; no line-segment visibility
     * assumption (unlike [visiblePointRange]).
     */
    fun scatterPointsInDomainViewport(
        points: List<XyPoint>,
        viewport: ChartViewport,
    ): List<Pair<Int, XyPoint>> {
        if (points.isEmpty()) return emptyList()
        val out = ArrayList<Pair<Int, XyPoint>>()
        for (i in points.indices) {
            val p = points[i]
            if (pointInsideViewport(p.x, p.y, viewport)) {
                out.add(i to p)
            }
        }
        return out
    }

    private fun pointInsideViewport(
        x: Double,
        y: Double,
        viewport: ChartViewport,
    ): Boolean {
        val xr = viewport.xRange
        val yr = viewport.yRange
        return x >= xr.start - FpMath.EPS && x <= xr.endInclusive + FpMath.EPS &&
            y >= yr.start - FpMath.EPS && y <= yr.endInclusive + FpMath.EPS
    }

    private fun segmentIntersectsViewportDomain(
        x0: Double,
        y0: Double,
        x1: Double,
        y1: Double,
        viewport: ChartViewport,
    ): Boolean {
        val xr = viewport.xRange
        val yr = viewport.yRange
        val xMin = xr.start
        val xMax = xr.endInclusive
        val yMin = yr.start
        val yMax = yr.endInclusive

        if (pointInsideViewport(x0, y0, viewport) || pointInsideViewport(x1, y1, viewport)) {
            return true
        }

        var u1 = 0.0
        var u2 = 1.0
        val dx = x1 - x0
        val dy = y1 - y0

        fun clipT(
            p: Double,
            q: Double,
        ): Boolean {
            if (FpMath.isNearlyZero(p)) {
                return q >= -FpMath.EPS
            }
            val r = q / p
            if (p < 0) {
                if (r > u2 + FpMath.EPS) return false
                if (r > u1) u1 = r
            } else {
                if (r < u1 - FpMath.EPS) return false
                if (r < u2) u2 = r
            }
            return true
        }

        if (!clipT(-dx, x0 - xMin)) return false
        if (!clipT(dx, xMax - x0)) return false
        if (!clipT(-dy, y0 - yMin)) return false
        if (!clipT(dy, yMax - y0)) return false

        if (u1 > u2 + FpMath.EPS) return false

        u1 = u1.coerceIn(0.0, 1.0)
        u2 = u2.coerceIn(0.0, 1.0)
        if (u1 > u2 + FpMath.EPS) return false
        return true
    }

    /**
     * True if a vertical bar column centered at [domainX] with total width [barWidthPx] in pixels
     * could intersect [plotBounds] horizontally (after mapping [domainX] through [centerXPx]).
     */
    fun barColumnIntersectsPlotHorizontally(
        centerXPx: Double,
        barWidthPx: Double,
        plotBounds: RectD,
        plotEdgeSlackPx: Double = BAR_COLUMN_PLOT_EDGE_SLACK_PX,
    ): Boolean {
        val slack = plotEdgeSlackPx.coerceAtLeast(0.0)
        val half = (barWidthPx / 2.0).coerceAtLeast(0.0)
        val left = centerXPx - half
        val right = centerXPx + half
        return left <= plotBounds.right + slack + FpMath.EPS && right >= plotBounds.left - slack - FpMath.EPS
    }

    /**
     * True if a horizontal bar row centered at [centerYPx] with total thickness [barHeightPx] in pixels
     * could intersect [plotBounds] vertically (category band culling).
     */
    fun barRowIntersectsPlotVertically(
        centerYPx: Double,
        barHeightPx: Double,
        plotBounds: RectD,
        plotEdgeSlackPx: Double = BAR_COLUMN_PLOT_EDGE_SLACK_PX,
    ): Boolean {
        val slack = plotEdgeSlackPx.coerceAtLeast(0.0)
        val half = (barHeightPx / 2.0).coerceAtLeast(0.0)
        val top = centerYPx - half
        val bottom = centerYPx + half
        return top <= plotBounds.bottom + slack + FpMath.EPS && bottom >= plotBounds.top - slack - FpMath.EPS
    }
}
