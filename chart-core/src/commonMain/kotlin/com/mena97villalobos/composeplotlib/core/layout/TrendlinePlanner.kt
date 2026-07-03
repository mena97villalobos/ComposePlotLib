package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.PolylineRectClip
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.draw.PathDrawStyle
import com.mena97villalobos.composeplotlib.core.layout.draw.PathSegment
import com.mena97villalobos.composeplotlib.core.layout.draw.StrokeStyle
import com.mena97villalobos.composeplotlib.core.math.LinearRegression
import com.mena97villalobos.composeplotlib.core.spec.TrendlineSpec
import com.mena97villalobos.composeplotlib.core.spec.TrendlineType
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import kotlin.math.ln
import kotlin.math.sqrt

private const val CONFIDENCE_BAND_SAMPLES = 48

/**
 * Projects [trendlines] into fitted-line (+ optional confidence band) [DrawCommand]s. [seriesPoints] maps a
 * series id to its domain points (already filtered to visible series; non-finite points are dropped here). A
 * trendline with a null `seriesId` fits every id in [orderedSeriesIds]; otherwise only the named series.
 */
internal fun buildTrendlineCommands(
    trendlines: List<TrendlineSpec>,
    seriesPoints: Map<String, List<XyPoint>>,
    orderedSeriesIds: List<String>,
    transform: ChartTransform,
    plotBounds: RectD,
): List<DrawCommand> {
    if (trendlines.isEmpty() || seriesPoints.isEmpty()) return emptyList()
    val out = ArrayList<DrawCommand>()
    for (spec in trendlines) {
        val targetIds = if (spec.seriesId == null) orderedSeriesIds else listOf(spec.seriesId)
        for (id in targetIds) {
            val raw = seriesPoints[id] ?: continue
            val finite = raw.filter { it.x.isFinite() && it.y.isFinite() }.sortedBy { it.x }
            if (finite.size < 2) continue
            when (val type = spec.type) {
                is TrendlineType.Linear -> out += linearTrendCommands(finite, spec, transform, plotBounds)
                is TrendlineType.MovingAverage ->
                    out += movingAverageCommands(finite, type.window, spec, transform, plotBounds)
            }
        }
    }
    return out
}

private fun linearTrendCommands(
    points: List<XyPoint>,
    spec: TrendlineSpec,
    transform: ChartTransform,
    plotBounds: RectD,
): List<DrawCommand> {
    val xs = DoubleArray(points.size) { points[it].x }
    val ys = DoubleArray(points.size) { points[it].y }
    val fit = LinearRegression.fit(xs, ys) ?: return emptyList()
    val xMin = xs.first()
    val xMax = xs.last()
    if (xMax <= xMin) return emptyList()

    val out = ArrayList<DrawCommand>()
    if (spec.confidenceBand) {
        confidenceBandCommand(fit, xMin, xMax, normalTwoSidedZ(spec.confidenceLevel), spec, transform)
            ?.let { out += it }
    }
    val stroke = spec.dashIntervals?.takeIf { it.isNotEmpty() }?.let { StrokeStyle(dashIntervals = it) }
    val endpoints =
        listOf(
            transform.toPixel(XyPoint(xMin, fit.predict(xMin))),
            transform.toPixel(XyPoint(xMax, fit.predict(xMax))),
        )
    for (fragment in PolylineRectClip.clipPolylineToRect(endpoints, plotBounds)) {
        out += DrawCommand.Polyline(points = fragment, color = spec.color, strokeWidth = spec.strokeWidth, strokeStyle = stroke)
    }
    return out
}

private fun confidenceBandCommand(
    fit: LinearRegression.Fit,
    xMin: Double,
    xMax: Double,
    z: Double,
    spec: TrendlineSpec,
    transform: ChartTransform,
): DrawCommand? {
    if (!z.isFinite() || z <= 0.0) return null
    val samples = CONFIDENCE_BAND_SAMPLES
    val upper = ArrayList<OffsetD>(samples + 1)
    val lower = ArrayList<OffsetD>(samples + 1)
    for (i in 0..samples) {
        val x = xMin + (xMax - xMin) * (i.toDouble() / samples)
        val se = fit.meanResponseStandardError(x)
        if (!se.isFinite()) return null
        val yHat = fit.predict(x)
        upper += transform.toPixel(XyPoint(x, yHat + z * se))
        lower += transform.toPixel(XyPoint(x, yHat - z * se))
    }
    val segments = ArrayList<PathSegment>(2 * (samples + 1) + 1)
    segments += PathSegment.MoveTo(upper.first())
    for (i in 1..samples) segments += PathSegment.LineTo(upper[i])
    for (i in samples downTo 0) segments += PathSegment.LineTo(lower[i])
    segments += PathSegment.Close
    val fill = spec.bandColor ?: spec.color.withAlpha(0.15)
    return DrawCommand.Path(segments = segments, style = PathDrawStyle.Fill(color = fill))
}

private fun movingAverageCommands(
    points: List<XyPoint>,
    window: Int,
    spec: TrendlineSpec,
    transform: ChartTransform,
    plotBounds: RectD,
): List<DrawCommand> {
    val w = window.coerceAtLeast(1)
    if (points.size < w) return emptyList()
    val pixels = ArrayList<OffsetD>(points.size - w + 1)
    var sum = 0.0
    for (i in points.indices) {
        sum += points[i].y
        if (i >= w) sum -= points[i - w].y
        if (i >= w - 1) pixels += transform.toPixel(XyPoint(points[i].x, sum / w))
    }
    if (pixels.size < 2) return emptyList()
    val stroke = spec.dashIntervals?.takeIf { it.isNotEmpty() }?.let { StrokeStyle(dashIntervals = it) }
    return PolylineRectClip.clipPolylineToRect(pixels, plotBounds).map { fragment ->
        DrawCommand.Polyline(points = fragment, color = spec.color, strokeWidth = spec.strokeWidth, strokeStyle = stroke)
    }
}

/** Two-sided standard-normal multiplier for a [confidenceLevel] in `(0,1)` (e.g. `0.95 → ≈1.96`). */
private fun normalTwoSidedZ(confidenceLevel: Double): Double {
    val cl = confidenceLevel.coerceIn(0.0, 0.999999)
    return inverseStandardNormalCdf((1.0 + cl) / 2.0)
}

/**
 * Inverse standard-normal CDF (probit) via Peter Acklam's rational approximation. Valid for `0 < p < 1`;
 * returns ±∞ at the bounds. Absolute error < ~1.15e-9 across the domain — ample for confidence bands.
 */
private fun inverseStandardNormalCdf(p: Double): Double {
    if (p <= 0.0) return Double.NEGATIVE_INFINITY
    if (p >= 1.0) return Double.POSITIVE_INFINITY
    val a0 = -3.969683028665376e+01
    val a1 = 2.209460984245205e+02
    val a2 = -2.759285104469687e+02
    val a3 = 1.383577518672690e+02
    val a4 = -3.066479806614716e+01
    val a5 = 2.506628277459239e+00
    val b1 = -5.447609879822406e+01
    val b2 = 1.615858368580409e+02
    val b3 = -1.556989798598866e+02
    val b4 = 6.680131188771972e+01
    val b5 = -1.328068155288572e+01
    val c1 = -7.784894002430293e-03
    val c2 = -3.223964580411365e-01
    val c3 = -2.400758277161838e+00
    val c4 = -2.549732539343734e+00
    val c5 = 4.374664141464968e+00
    val c6 = 2.938163982698783e+00
    val d1 = 7.784695709041462e-03
    val d2 = 3.224671290700398e-01
    val d3 = 2.445134137142996e+00
    val d4 = 3.754408661907416e+00
    val pLow = 0.02425
    val pHigh = 1.0 - pLow
    return when {
        p < pLow -> {
            val q = sqrt(-2.0 * ln(p))
            (((((c1 * q + c2) * q + c3) * q + c4) * q + c5) * q + c6) /
                ((((d1 * q + d2) * q + d3) * q + d4) * q + 1.0)
        }
        p <= pHigh -> {
            val q = p - 0.5
            val r = q * q
            (((((a0 * r + a1) * r + a2) * r + a3) * r + a4) * r + a5) * q /
                (((((b1 * r + b2) * r + b3) * r + b4) * r + b5) * r + 1.0)
        }
        else -> {
            val q = sqrt(-2.0 * ln(1.0 - p))
            -(((((c1 * q + c2) * q + c3) * q + c4) * q + c5) * q + c6) /
                ((((d1 * q + d2) * q + d3) * q + d4) * q + 1.0)
        }
    }
}
