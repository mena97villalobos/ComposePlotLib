package com.mena97villalobos.composeplotlib.core.accessibility

import com.mena97villalobos.composeplotlib.core.spec.AxisLabelFormatter
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec

/*
 * Auto-generated, screen-reader-friendly data narratives for charts — a short prose summary of each series'
 * point count, value range (min/max), and overall first->last trend, in x order. KMP-safe (chart-core), so the
 * same text is produced on every platform. Wired into the accessibility contentDescription when
 * ChartAccessibilitySpec.includeDataNarrative is on, and public for hosts that want to speak or display it directly.
 *
 * Trend is classified by net first->last change against a small fraction of the value range so noise reads as
 * "steady". Values are formatted with the y-axis label formatter so the narrative matches the visible ticks.
 */

/** Trend of a series from its first to last sample in x order. */
enum class ChartTrend {
    Rising,
    Falling,
    Steady,
}

/** Narrative for a [LineChartSpec]: one sentence per series (count, range, trend). */
fun lineChartNarrative(spec: LineChartSpec): String {
    if (spec.series.isEmpty()) return "Line chart with no series."
    val sentences =
        spec.series.joinToString(" ") { s ->
            seriesSentence(s.label, s.points.map { it.x to it.y }, spec.yAxis.labelFormatter)
        }
    return "Line chart, ${spec.series.size} series. $sentences"
}

/** Narrative for a [ScatterChartSpec]: one sentence per series (count, range, trend). */
fun scatterChartNarrative(spec: ScatterChartSpec): String {
    if (spec.series.isEmpty()) return "Scatter chart with no series."
    val sentences =
        spec.series.joinToString(" ") { s ->
            seriesSentence(s.label, s.points.map { it.x to it.y }, spec.yAxis.labelFormatter)
        }
    return "Scatter chart, ${spec.series.size} series. $sentences"
}

/** Narrative for a [BarChartSpec]: one sentence per group (count, range, trend across categories). */
fun barChartNarrative(spec: BarChartSpec): String {
    if (spec.groups.isEmpty()) return "Bar chart with no groups."
    val sentences =
        spec.groups.joinToString(" ") { g ->
            seriesSentence(g.label, g.entries.map { it.x to it.y }, spec.yAxis.labelFormatter)
        }
    val groupWord = if (spec.groups.size == 1) "group" else "groups"
    return "Bar chart, ${spec.groups.size} $groupWord. $sentences"
}

private fun seriesSentence(
    label: String,
    points: List<Pair<Double, Double>>,
    valueFormatter: AxisLabelFormatter,
): String {
    val finite = points.filter { it.first.isFinite() && it.second.isFinite() }.sortedBy { it.first }
    if (finite.isEmpty()) return "$label has no data."
    val ys = finite.map { it.second }
    val first = ys.first()
    val last = ys.last()
    val min = ys.min()
    val max = ys.max()
    val n = ys.size
    val trend = classifyTrend(first, last, min, max)
    val trendPhrase =
        when (trend) {
            ChartTrend.Rising -> "rises from ${valueFormatter(first)} to ${valueFormatter(last)}"
            ChartTrend.Falling -> "falls from ${valueFormatter(first)} to ${valueFormatter(last)}"
            ChartTrend.Steady -> "stays around ${valueFormatter(first)}"
        }
    val pointWord = if (n == 1) "point" else "points"
    return "$label: $n $pointWord, $trendPhrase, ranging ${valueFormatter(min)} to ${valueFormatter(max)}."
}

/** Classifies first→last change as [ChartTrend], treating changes below 5% of the value range as steady. */
internal fun classifyTrend(
    first: Double,
    last: Double,
    min: Double,
    max: Double,
): ChartTrend {
    val net = last - first
    val threshold = (max - min) * 0.05
    return when {
        net > threshold -> ChartTrend.Rising
        net < -threshold -> ChartTrend.Falling
        else -> ChartTrend.Steady
    }
}
