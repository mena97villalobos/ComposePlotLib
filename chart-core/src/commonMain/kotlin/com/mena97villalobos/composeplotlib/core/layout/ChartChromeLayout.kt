package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.planning.ellipsizeDisplayLabel
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.spec.LegendSpec
import kotlin.math.max

/**
 * Legend row placement for [ChartReservedRegions] / [ChartLayoutPlan.legendEntries].
 * Shared by cartesian [com.mena97villalobos.composeplotlib.core.layout.planning.CartesianLayoutPlanner] and [com.mena97villalobos.composeplotlib.core.layout.ChartPlanner.planPieChart].
 */
internal fun buildLegendEntries(
    labels: List<Pair<String, ChartColor>>,
    canvasBounds: RectD,
    minimumInsets: InsetsD,
    textMeasurements: ChartTextMeasurementInput,
    legendSpec: LegendSpec,
    labelLayoutScale: Double = 1.0,
    /** Per-entry series id (parallel to [labels]); enables interactive legends. Empty = no ids. */
    seriesIds: List<String?> = emptyList(),
    /** Series ids currently hidden; matching entries are marked [LegendEntryPlan.hidden] (drawn dimmed). */
    hiddenSeriesIds: Set<String> = emptySet(),
): List<LegendEntryPlan> {
    if (!legendSpec.enabled || labels.isEmpty()) return emptyList()

    val s = labelLayoutScale
    val indicatorRadius = legendSpec.style.indicatorRadius
    val indicatorSpacing = 6.0 * s
    val entrySpacing = 16.0 * s
    val rowSpacing = 8.0 * s
    val startX = canvasBounds.left + minimumInsets.left
    val maxRight = canvasBounds.right - minimumInsets.right
    var currentX = startX
    var currentY = canvasBounds.top + 8.0 * s
    var rowHeight = 0.0

    return labels.mapIndexed { index, (label, color) ->
        val seriesId = seriesIds.getOrNull(index)
        val displayLabel = ellipsizeDisplayLabel(label, legendSpec.style.labelMaxChars)
        val labelMetrics = textMeasurements.legendLabelMetrics(displayLabel)
        val labelWidth = labelMetrics.width
        val labelHeight = labelMetrics.height
        val entryWidth = (indicatorRadius * 2.0) + indicatorSpacing + labelWidth
        val entryHeight = maxOf(indicatorRadius * 2.0, labelHeight)

        if (currentX > startX && currentX + entryWidth > maxRight) {
            currentX = startX
            currentY += rowHeight + rowSpacing
            rowHeight = 0.0
        }

        val indicatorCenter = OffsetD(currentX + indicatorRadius, currentY + (entryHeight / 2.0))
        val labelBaselineY = indicatorCenter.y
        val labelTopY = (labelBaselineY - labelMetrics.firstBaselineFromTop).coerceAtLeast(currentY)
        val labelPosition = OffsetD(currentX + (indicatorRadius * 2.0) + indicatorSpacing, labelTopY)
        rowHeight = maxOf(rowHeight, entryHeight, labelTopY + labelHeight - currentY)
        val entry =
            LegendEntryPlan(
                label = displayLabel,
                color = legendSpec.style.textColor.takeUnless { it == ChartColor.Gray } ?: color,
                indicatorRadius = indicatorRadius,
                indicatorCenter = indicatorCenter,
                labelPosition = labelPosition,
                labelMetrics = labelMetrics,
                seriesId = seriesId,
                hidden = seriesId != null && seriesId in hiddenSeriesIds,
            )
        currentX += entryWidth + entrySpacing
        entry
    }
}

/**
 * Axis/legend chrome insets derived from measured labels. Used by cartesian layout and pie (legend-only axes).
 */
internal fun buildReservedRegions(
    canvasBounds: RectD,
    minimumInsets: InsetsD,
    xTickLabels: List<String>,
    yTickLabels: List<String>,
    legendEntries: List<LegendEntryPlan>,
    textMeasurements: ChartTextMeasurementInput,
    xAxisEnabled: Boolean,
    yAxisEnabled: Boolean,
    labelLayoutScale: Double,
    rightAxisTickLabels: List<String> = emptyList(),
    reserveRightYAxisSpace: Boolean = false,
    xStaggerExtraBelowPlot: Double = 0.0,
): ChartReservedRegions {
    val s = labelLayoutScale
    val xTickTopInset = X_TICK_LABEL_TOP_INSET * s
    val maxYTickWidth = yTickLabels.maxOfOrNull { textMeasurements.tickLabelSize(it).width } ?: 0.0
    val maxXTickWidth = xTickLabels.maxOfOrNull { textMeasurements.tickLabelSize(it).width } ?: 0.0
    val maxXTickReserveBelowPlot =
        xTickLabels.maxOfOrNull { label ->
            val m = textMeasurements.tickLabelMetrics(label)
            val descenderTail = (m.height - m.lastBaselineFromTop).coerceAtLeast(0.0)
            val belowBaseline = max(X_TICK_DESCENDER_MARGIN * s, descenderTail)
            xTickTopInset + m.height + belowBaseline
        } ?: xTickTopInset

    val leftInset = maxOf(minimumInsets.left, if (yAxisEnabled) maxYTickWidth + 16.0 * s else 0.0)
    val bottomInset =
        maxOf(
            minimumInsets.bottom,
            if (xAxisEnabled) {
                maxXTickReserveBelowPlot + xStaggerExtraBelowPlot
            } else {
                0.0
            },
        )
    val maxRightYTickWidth =
        if (reserveRightYAxisSpace && rightAxisTickLabels.isNotEmpty()) {
            rightAxisTickLabels.maxOfOrNull { textMeasurements.tickLabelSize(it).width } ?: 0.0
        } else {
            0.0
        }
    val rightInset =
        maxOf(
            minimumInsets.right,
            maxXTickWidth / 2.0,
            if (reserveRightYAxisSpace && maxRightYTickWidth > 0.0) maxRightYTickWidth + 16.0 * s else 0.0,
        )
    val topInset =
        maxOf(
            minimumInsets.top,
            legendEntries.maxOfOrNull { it.labelPosition.y + it.labelSize.height }?.plus(8.0 * s) ?: 0.0,
        )

    val contentInsets =
        InsetsD(
            left = leftInset,
            top = topInset,
            right = rightInset,
            bottom = bottomInset,
        )
    val legendBounds =
        if (legendEntries.isEmpty()) {
            null
        } else {
            val minX = legendEntries.minOf { minOf(it.indicatorCenter.x - it.indicatorRadius, it.labelPosition.x) }
            val minY = legendEntries.minOf { minOf(it.indicatorCenter.y - it.indicatorRadius, it.labelPosition.y) }
            val maxX = legendEntries.maxOf { maxOf(it.indicatorCenter.x + it.indicatorRadius, it.labelPosition.x + it.labelSize.width) }
            val maxY = legendEntries.maxOf { maxOf(it.indicatorCenter.y + it.indicatorRadius, it.labelPosition.y + it.labelSize.height) }
            RectD(left = minX, top = minY, right = maxX, bottom = maxY)
        }

    return ChartReservedRegions(
        contentInsets = contentInsets,
        legendBounds = legendBounds,
        xAxisBounds =
            if (xAxisEnabled) {
                RectD(
                    left = leftInset,
                    top = canvasBounds.bottom - bottomInset,
                    right = canvasBounds.right - rightInset,
                    bottom = canvasBounds.bottom,
                )
            } else {
                null
            },
        yAxisBounds =
            if (yAxisEnabled) {
                RectD(
                    left = 0.0,
                    top = topInset,
                    right = leftInset,
                    bottom = canvasBounds.bottom - bottomInset,
                )
            } else {
                null
            },
    )
}
