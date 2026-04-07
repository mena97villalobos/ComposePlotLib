package com.mena97villalobos.composeplotlib.chart.internal.marker

import com.mena97villalobos.composeplotlib.core.geometry.RectD
import kotlin.math.roundToInt

/**
 * Picks above/below placement that fits in [bounds], then clamps so the marker rectangle stays inside.
 */
internal fun computeContainedMarkerTopLeft(
    anchorX: Double,
    anchorY: Double,
    markerWidth: Int,
    markerHeight: Int,
    gapPx: Int,
    bounds: RectD,
    preferAbove: Boolean,
): Pair<Int, Int> {
    val w = markerWidth.toDouble()
    val h = markerHeight.toDouble()
    val g = gapPx.toDouble()

    fun fits(
        x: Double,
        y: Double,
    ): Boolean =
        w > 0.0 && h > 0.0 &&
            x >= bounds.left && y >= bounds.top &&
            x + w <= bounds.right && y + h <= bounds.bottom

    val aboveX = anchorX - w / 2.0
    val aboveY = anchorY - g - h
    val belowX = anchorX - w / 2.0
    val belowY = anchorY + g

    val ordered =
        if (preferAbove) {
            listOf(aboveX to aboveY, belowX to belowY)
        } else {
            listOf(belowX to belowY, aboveX to aboveY)
        }

    val (xCand, yCand) = ordered.firstOrNull { (x, y) -> fits(x, y) } ?: ordered[0]

    val xFinal = coerceHorizontal(xCand, w, bounds)
    val yFinal = coerceVertical(yCand, h, bounds)
    return xFinal.roundToInt() to yFinal.roundToInt()
}

/** Previous default: above anchor, min x/y at zero. */
internal fun computeMarkerTopLeftLegacy(
    anchorX: Double,
    anchorY: Double,
    markerWidth: Int,
    markerHeight: Int,
    gapPx: Int,
): Pair<Int, Int> {
    val x = (anchorX - markerWidth / 2.0).roundToInt().coerceAtLeast(0)
    val y = (anchorY - markerHeight - gapPx).roundToInt().coerceAtLeast(0)
    return x to y
}

private fun coerceHorizontal(
    x: Double,
    w: Double,
    bounds: RectD,
): Double {
    if (w <= 0.0) return x.coerceAtLeast(bounds.left)
    if (w >= bounds.width) {
        return bounds.left + (bounds.width - w) / 2.0
    }
    return x.coerceIn(bounds.left, bounds.right - w)
}

private fun coerceVertical(
    y: Double,
    h: Double,
    bounds: RectD,
): Double {
    if (h <= 0.0) return y.coerceAtLeast(bounds.top)
    if (h >= bounds.height) {
        return bounds.top + (bounds.height - h) / 2.0
    }
    return y.coerceIn(bounds.top, bounds.bottom - h)
}

/**
 * Elbow polyline in parent pixels: anchor → (anchor x, box edge y) → box edge midpoint.
 * Chooses top vs bottom edge from whether the anchor lies below or above the box vertical center.
 */
internal fun computeCalloutElbowPolyline(
    anchorX: Double,
    anchorY: Double,
    markerOffsetX: Int,
    markerOffsetY: Int,
    markerWidth: Int,
    markerHeight: Int,
): List<Pair<Double, Double>> {
    val w = markerWidth
    val h = markerHeight
    if (w <= 0 || h <= 0) return emptyList()
    val ox = markerOffsetX.toDouble()
    val oy = markerOffsetY.toDouble()
    val boxTop = oy
    val boxBottom = oy + h
    val cx = ox + w / 2.0
    val boxCenterY = oy + h / 2.0
    val edgeY = if (anchorY > boxCenterY) boxBottom else boxTop
    val elbow = anchorX to edgeY
    val end = cx to edgeY
    return listOf(anchorX to anchorY, elbow, end)
}
