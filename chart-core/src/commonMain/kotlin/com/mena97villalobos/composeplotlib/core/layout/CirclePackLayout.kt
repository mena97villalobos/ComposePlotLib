package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.RectD
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** A packed circle: center and radius in the layout's local (pre-fit) space or fitted pixel space. */
data class PackedCircle(
    val centerX: Double,
    val centerY: Double,
    val radius: Double,
)

/**
 * Packs circles with the given [radii] around the origin without overlap, returning centers **aligned to the input
 * order**. Circles are placed largest-first, each tangent to two already-placed circles at the position closest to
 * the origin (a greedy front-packing similar to d3's `packSiblings`), so the result is compact and deterministic.
 */
fun circlePackLayout(radii: List<Double>): List<PackedCircle> {
    val n = radii.size
    if (n == 0) return emptyList()
    val order = radii.indices.sortedByDescending { radii[it] }
    val placed = ArrayList<PackedCircle>(n)
    val result = arrayOfNulls<PackedCircle>(n)
    for ((k, index) in order.withIndex()) {
        val r = radii[index].coerceAtLeast(0.0)
        val circle =
            when (k) {
                0 -> PackedCircle(0.0, 0.0, r)
                1 -> PackedCircle(placed[0].radius + r, 0.0, r)
                else -> bestPackPosition(placed, r)
            }
        placed.add(circle)
        result[index] = circle
    }
    return result.map { it ?: PackedCircle(0.0, 0.0, 0.0) }
}

/**
 * Translates and uniformly scales [circles] to fit centered inside [bounds] (as a group), preserving relative sizes.
 * Returns circles in the same order.
 */
fun fitPackedCircles(
    circles: List<PackedCircle>,
    bounds: RectD,
): List<PackedCircle> {
    if (circles.isEmpty()) return circles
    var minX = Double.MAX_VALUE
    var minY = Double.MAX_VALUE
    var maxX = -Double.MAX_VALUE
    var maxY = -Double.MAX_VALUE
    for (c in circles) {
        minX = min(minX, c.centerX - c.radius)
        minY = min(minY, c.centerY - c.radius)
        maxX = max(maxX, c.centerX + c.radius)
        maxY = max(maxY, c.centerY + c.radius)
    }
    val spanX = maxX - minX
    val spanY = maxY - minY
    if (spanX <= 0.0 || spanY <= 0.0 || bounds.width <= 0.0 || bounds.height <= 0.0) return circles
    val scale = min(bounds.width / spanX, bounds.height / spanY)
    val groupCenterX = (minX + maxX) / 2.0
    val groupCenterY = (minY + maxY) / 2.0
    val targetCenterX = (bounds.left + bounds.right) / 2.0
    val targetCenterY = (bounds.top + bounds.bottom) / 2.0
    return circles.map { c ->
        PackedCircle(
            centerX = targetCenterX + (c.centerX - groupCenterX) * scale,
            centerY = targetCenterY + (c.centerY - groupCenterY) * scale,
            radius = c.radius * scale,
        )
    }
}

private fun bestPackPosition(
    placed: List<PackedCircle>,
    r: Double,
): PackedCircle {
    var best: PackedCircle? = null
    var bestDistance = Double.MAX_VALUE
    for (i in placed.indices) {
        for (j in i + 1 until placed.size) {
            for (candidate in tangentPositions(placed[i], placed[j], r)) {
                if (overlapsAny(candidate, placed)) continue
                val distance = hypot(candidate.centerX, candidate.centerY)
                if (distance < bestDistance) {
                    bestDistance = distance
                    best = candidate
                }
            }
        }
    }
    if (best != null) return best
    // Fallback: place clear of everything on the +x axis so packing can always proceed.
    val extent = placed.maxOf { hypot(it.centerX, it.centerY) + it.radius }
    return PackedCircle(extent + r, 0.0, r)
}

/** The (up to two) centers where a circle of radius [r] is externally tangent to both [a] and [b]. */
private fun tangentPositions(
    a: PackedCircle,
    b: PackedCircle,
    r: Double,
): List<PackedCircle> {
    val dx = b.centerX - a.centerX
    val dy = b.centerY - a.centerY
    val d = hypot(dx, dy)
    val r1 = a.radius + r
    val r2 = b.radius + r
    if (d <= 0.0 || d > r1 + r2 || d < kotlin.math.abs(r1 - r2)) return emptyList()
    val aDist = (r1 * r1 - r2 * r2 + d * d) / (2.0 * d)
    val h2 = r1 * r1 - aDist * aDist
    if (h2 < 0.0) return emptyList()
    val h = sqrt(h2)
    val mx = a.centerX + aDist * dx / d
    val my = a.centerY + aDist * dy / d
    val ox = -h * dy / d
    val oy = h * dx / d
    return listOf(
        PackedCircle(mx + ox, my + oy, r),
        PackedCircle(mx - ox, my - oy, r),
    )
}

private fun overlapsAny(
    candidate: PackedCircle,
    placed: List<PackedCircle>,
): Boolean {
    for (c in placed) {
        val distance = hypot(candidate.centerX - c.centerX, candidate.centerY - c.centerY)
        if (distance < candidate.radius + c.radius - 1e-6) return true
    }
    return false
}

/** Convenience: circle radii proportional to `sqrt(value)` so **area** encodes value. Non-positive → radius 0. */
fun circlePackRadii(values: List<Double>): List<Double> = values.map { if (it > 0.0) sqrt(it) else 0.0 }
