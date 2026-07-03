package com.mena97villalobos.composeplotlib.core.spec

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.ChartColor

/**
 * One stage of a funnel: a [label], a [value] (bar width is proportional to it), and an optional [color].
 *
 * @property label Stage name (drawn centered in the band when labels are shown).
 * @property value Magnitude for this stage; the widest stage uses the full plot width.
 * @property color Stage fill override, or null to use the spec palette by position.
 */
data class FunnelStage(
    val label: String,
    val value: Double,
    val color: ChartColor? = null,
)

/**
 * Immutable configuration for a **funnel** chart: ordered [stages] drawn as stacked, centered trapezoids that taper
 * from each stage's width to the next (classic conversion-funnel shape). Non-cartesian and read-only (no axes /
 * selection / pan-zoom), like the gauge and bullet. `chart-compose`'s `FunnelChart` renders it.
 *
 * @property stages Ordered stages, top to bottom; the widest is scaled to the full plot width.
 * @property palette Fill colors assigned by stage index when [FunnelStage.color] is null.
 * @property gapPx Vertical gap in **pixels** between stage bands.
 * @property showLabels When true, draws `label` + formatted `value` centered in each band.
 * @property labelColor Stage label text color.
 * @property valueFormatter Formats the per-stage value shown in the label.
 * @property id Chart identity string.
 * @property accessibility Host-agnostic accessibility strings; see [ChartAccessibilitySpec].
 * @property contentPadding Minimum padding around the funnel in **pixels**.
 */
data class FunnelChartSpec(
    val stages: List<FunnelStage>,
    val palette: List<ChartColor> = ChartPalettes.Tableau10,
    val gapPx: Double = 2.0,
    val showLabels: Boolean = true,
    val labelColor: ChartColor = ChartColor.White,
    val valueFormatter: (Double) -> String = DefaultAxisLabelFormatter,
    val id: String = "funnel",
    val accessibility: ChartAccessibilitySpec = ChartAccessibilitySpec(),
    val contentPadding: InsetsD = InsetsD(left = 24.0, top = 24.0, right = 24.0, bottom = 24.0),
)
