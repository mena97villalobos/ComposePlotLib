package com.mena97villalobos.composeplotlib.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.chart.theme.materialChartPalette
import com.mena97villalobos.composeplotlib.chart.theme.withMaterialGroupColors
import com.mena97villalobos.composeplotlib.chart.theme.withMaterialScatterSeriesColors
import com.mena97villalobos.composeplotlib.chart.theme.withMaterialSeriesColors
import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.model.BarEntry
import com.mena97villalobos.composeplotlib.core.model.BarGroup
import com.mena97villalobos.composeplotlib.core.model.CandlestickSeries
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.HeatmapCell
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.model.OHLCPoint
import com.mena97villalobos.composeplotlib.core.model.PieChartSlice
import com.mena97villalobos.composeplotlib.core.model.ScatterSeries
import com.mena97villalobos.composeplotlib.core.spec.AxisLabelStyle
import com.mena97villalobos.composeplotlib.core.spec.AxisScale
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.BarChartOrientation
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.BarLayoutMode
import com.mena97villalobos.composeplotlib.core.spec.BarStyle
import com.mena97villalobos.composeplotlib.core.spec.CandlestickChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ChartAccessibilitySpec
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianLayer
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianYAxisBinding
import com.mena97villalobos.composeplotlib.core.spec.EpochMillisTickPolicy
import com.mena97villalobos.composeplotlib.core.spec.GaugeChartSpec
import com.mena97villalobos.composeplotlib.core.spec.GridLineStyle
import com.mena97villalobos.composeplotlib.core.spec.GridSpec
import com.mena97villalobos.composeplotlib.core.spec.HeatmapChartSpec
import com.mena97villalobos.composeplotlib.core.spec.HeatmapColorScale
import com.mena97villalobos.composeplotlib.core.spec.LegendSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import com.mena97villalobos.composeplotlib.core.spec.MarkerShape
import com.mena97villalobos.composeplotlib.core.spec.PieChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PieSliceLabelsSpec
import com.mena97villalobos.composeplotlib.core.spec.PointStyle
import com.mena97villalobos.composeplotlib.core.spec.RadarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.RadarSeries
import com.mena97villalobos.composeplotlib.core.spec.ReferenceLine
import com.mena97villalobos.composeplotlib.core.spec.ReferenceOrientation
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec
import com.mena97villalobos.composeplotlib.core.spec.StackedBarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.clampedProgress

/**
 * Minimal multiplatform preview host: cycles [LineChart], [BarChart], [StackedBarChart], horizontal [BarChart],
 * [ScatterChart], [PieChart], [CombinedCartesianChart], and a short **domain axes** demo (category, long-label truncation,
 * epoch-millis time, UTC weekday tick policy), with selection hints.
 *
 * Intended for `composeApp` and tooling previews, not as a production root composable.
 * When adding a new chart family, wire a preview here and update `AGENTS.md` and files under the `docs` directory per **Obligations**.
 */
@Composable
@Preview
fun ComposePlotApp() {
    MaterialTheme {
        val palette = materialChartPalette()
        var screen by remember { mutableStateOf(PreviewScreen.Line) }
        var lineDemoZoomed by remember { mutableStateOf(false) }
        val lineSpecVariantA = remember(palette) { previewLineChartSpec().withMaterialSeriesColors(palette) }
        val lineSpecVariantB = remember(palette) { previewLineChartSpecDataB().withMaterialSeriesColors(palette) }
        var lineSpecForPreview by remember { mutableStateOf(lineSpecVariantA) }
        val barSpec = remember(palette) { previewBarChartSpec().withMaterialGroupColors(palette) }
        val stackedBarSpec = remember(palette) { previewStackedBarChartSpec().withMaterialGroupColors(palette) }
        val horizontalBarSpec = remember(palette) { previewHorizontalBarChartSpec().withMaterialGroupColors(palette) }
        val scatterSpec = remember(palette) { previewScatterChartSpec().withMaterialScatterSeriesColors(palette) }
        val pieSpec = remember { previewPieChartSpec() }
        val radarSpec = remember { previewRadarChartSpec() }
        val combinedSpec = remember { previewCombinedCartesianSpec() }
        val heatmapSpec = remember { previewHeatmapChartSpec() }
        val gaugeSpec = remember { previewGaugeSpec() }
        val candlestickSpec = remember { previewCandlestickSpec() }
        val sparklinePoints = remember { previewSparklinePoints() }
        val lineViewport = rememberChartViewportState(initialViewport = defaultViewport(lineSpecVariantA))
        val barViewport = rememberChartViewportState(initialViewport = defaultViewport(barSpec))
        val stackedBarViewport =
            rememberChartViewportState(initialViewport = defaultViewport(stackedBarSpec.toBarChartSpec()))
        val horizontalBarViewport = rememberChartViewportState(initialViewport = defaultViewport(horizontalBarSpec))
        val scatterViewport = rememberChartViewportState(initialViewport = defaultViewport(scatterSpec))
        val pieViewport = rememberChartViewportState(initialViewport = defaultViewport(pieSpec))
        val radarViewport = rememberChartViewportState(initialViewport = defaultViewport(radarSpec))
        val combinedViewport = rememberChartViewportState(initialViewport = defaultViewport(combinedSpec))
        val heatmapViewport = rememberChartViewportState(initialViewport = defaultViewport(heatmapSpec))
        val candlestickViewport = rememberChartViewportState(initialViewport = defaultViewport(candlestickSpec))
        val domainCategorySpec = remember { previewCategoryAxisLineSpec() }
        val domainLongCategorySpec = remember { previewLongCategoryAxisLineSpec() }
        val domainTimeSpec = remember { previewEpochMillisLineSpec() }
        val domainWeekdayTimeSpec = remember { previewWeekdayEpochMillisLineSpec() }
        val domainCategoryViewport =
            rememberChartViewportState(initialViewport = defaultViewport(domainCategorySpec))
        val domainLongCategoryViewport =
            rememberChartViewportState(initialViewport = defaultViewport(domainLongCategorySpec))
        val domainTimeViewport = rememberChartViewportState(initialViewport = defaultViewport(domainTimeSpec))
        val domainWeekdayTimeViewport =
            rememberChartViewportState(initialViewport = defaultViewport(domainWeekdayTimeSpec))
        val lineSelection = rememberChartSelectionState()
        val barSelection = rememberChartSelectionState()
        val stackedBarSelection = rememberChartSelectionState()
        val horizontalBarSelection = rememberChartSelectionState()
        val scatterSelection = rememberChartSelectionState()
        val pieSelection = rememberChartSelectionState()
        val radarSelection = rememberChartSelectionState()
        val combinedSelection = rememberChartSelectionState()
        val heatmapSelection = rememberChartSelectionState()
        val candlestickSelection = rememberChartSelectionState()
        val domainCategorySelection = rememberChartSelectionState()
        val domainLongCategorySelection = rememberChartSelectionState()
        val domainTimeSelection = rememberChartSelectionState()
        val domainWeekdayTimeSelection = rememberChartSelectionState()

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .safeContentPadding()
                    .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "ComposePlotLib",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text =
                    "Shared host: line (area fill + reference line), bar, stacked bar, horizontal bar, scatter (mixed marker shapes + bubbles), " +
                        "pie/donut (radial), radar/spider (radial), combined (bar + dual-axis line), sankey (flow), heatmap, gauge (radial progress), " +
                        "candlestick (OHLC), sparkline (axis-less mini line), and domain axes (category + time). Android showcase adds richer demos.",
                style = MaterialTheme.typography.bodyLarge,
            )
            OutlinedButton(
                onClick = {
                    screen =
                        when (screen) {
                            PreviewScreen.Line -> PreviewScreen.Bar
                            PreviewScreen.Bar -> PreviewScreen.StackedBar
                            PreviewScreen.StackedBar -> PreviewScreen.HorizontalBar
                            PreviewScreen.HorizontalBar -> PreviewScreen.Scatter
                            PreviewScreen.Scatter -> PreviewScreen.Pie
                            PreviewScreen.Pie -> PreviewScreen.Radar
                            PreviewScreen.Radar -> PreviewScreen.Combined
                            PreviewScreen.Combined -> PreviewScreen.Sankey
                            PreviewScreen.Sankey -> PreviewScreen.Heatmap
                            PreviewScreen.Heatmap -> PreviewScreen.Gauge
                            PreviewScreen.Gauge -> PreviewScreen.Candlestick
                            PreviewScreen.Candlestick -> PreviewScreen.Sparkline
                            PreviewScreen.Sparkline -> PreviewScreen.DomainAxes
                            PreviewScreen.DomainAxes -> PreviewScreen.Line
                        }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    when (screen) {
                        PreviewScreen.Line -> "Switch to Bar Preview"
                        PreviewScreen.Bar -> "Switch to Stacked Bar Preview"
                        PreviewScreen.StackedBar -> "Switch to Horizontal Bar Preview"
                        PreviewScreen.HorizontalBar -> "Switch to Scatter Preview"
                        PreviewScreen.Scatter -> "Switch to Pie Preview"
                        PreviewScreen.Pie -> "Switch to Radar Preview"
                        PreviewScreen.Radar -> "Switch to Combined Preview"
                        PreviewScreen.Combined -> "Switch to Sankey Preview"
                        PreviewScreen.Sankey -> "Switch to Heatmap Preview"
                        PreviewScreen.Heatmap -> "Switch to Gauge Preview"
                        PreviewScreen.Gauge -> "Switch to Candlestick Preview"
                        PreviewScreen.Candlestick -> "Switch to Sparkline Preview"
                        PreviewScreen.Sparkline -> "Switch to Domain Axes Preview"
                        PreviewScreen.DomainAxes -> "Switch to Line Preview"
                    },
                )
            }

            when (screen) {
                PreviewScreen.Line -> {
                    LineChart(
                        spec = lineSpecForPreview,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .padding(top = 8.dp),
                        viewportState = lineViewport,
                        selectionState = lineSelection,
                        gestureConfig = ChartGestureConfig.cartesianInteractive(),
                        marker = { markerData -> DefaultChartMarker(markerData) },
                        animation =
                            ChartAnimationPolicy(
                                viewportChangesEnabled = true,
                                selectionHighlightEnabled = true,
                                gestureViewportEasingEnabled = true,
                                dataTransitionEnabled = true,
                                dataTransitionDurationMillis = 400,
                            ),
                    )
                    OutlinedButton(
                        onClick = {
                            lineSpecForPreview =
                                if (lineSpecForPreview === lineSpecVariantA) lineSpecVariantB else lineSpecVariantA
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Toggle dataset (coordinate morph)")
                    }
                    OutlinedButton(
                        onClick = {
                            lineDemoZoomed = !lineDemoZoomed
                            if (lineDemoZoomed) {
                                lineViewport.zoomTo(
                                    xSpan = 2.0,
                                    ySpan = 8.0,
                                    centerX = 3.0,
                                    centerY = 18.0,
                                )
                            } else {
                                lineViewport.fitToData()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            if (lineDemoZoomed) {
                                "Animate zoom out to full domain (fit)"
                            } else {
                                "Animate zoom in to demo region (programmatic)"
                            },
                        )
                    }
                    Text(
                        text =
                            lineSelection.selection?.let { sel ->
                                val pt =
                                    lineSpecForPreview.series
                                        .firstOrNull { it.id == sel.target.seriesId }
                                        ?.points
                                        ?.getOrNull(sel.target.pointIndex)
                                        ?: ChartPoint(sel.target.point.x, sel.target.point.y)
                                val label =
                                    lineSpecForPreview.series.firstOrNull { it.id == sel.target.seriesId }?.label
                                        ?: sel.target.seriesId
                                "Selected $label: y=${pt.y}"
                            } ?: "Tap the chart to select a point (pan/zoom with two fingers; double-tap inside the plot zooms in). " +
                                "Gesture viewport uses chase smoothing when enabled; toggle dataset to see data morph.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                PreviewScreen.Bar -> {
                    BarChart(
                        spec = barSpec,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .padding(top = 8.dp),
                        viewportState = barViewport,
                        selectionState = barSelection,
                        marker = { markerData -> DefaultChartMarker(markerData) },
                    )
                    Text(
                        text =
                            barSelection.selection?.let { sel ->
                                val group = barSpec.groups.firstOrNull { it.id == sel.target.seriesId }
                                val entry = group?.entries?.getOrNull(sel.target.pointIndex)
                                val label = group?.label ?: sel.target.seriesId
                                entry?.let { "Selected $label: x=${it.x}, y=${it.y}" }
                                    ?: "Selected $label"
                            } ?: "Tap a bar to select (pan/zoom with two fingers).",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                PreviewScreen.StackedBar -> {
                    StackedBarChart(
                        spec = stackedBarSpec,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .padding(top = 8.dp),
                        viewportState = stackedBarViewport,
                        selectionState = stackedBarSelection,
                        marker = { markerData -> DefaultChartMarker(markerData) },
                    )
                    Text(
                        text =
                            stackedBarSelection.selection?.let { sel ->
                                val group = stackedBarSpec.groups.firstOrNull { it.id == sel.target.seriesId }
                                val entry = group?.entries?.getOrNull(sel.target.pointIndex)
                                val label = group?.label ?: sel.target.seriesId
                                entry?.let { "Selected $label: x=${it.x}, y=${it.y}" }
                                    ?: "Selected $label"
                            } ?: "Tap a stacked segment (pan/zoom with two fingers).",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                PreviewScreen.HorizontalBar -> {
                    BarChart(
                        spec = horizontalBarSpec,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .padding(top = 8.dp),
                        viewportState = horizontalBarViewport,
                        selectionState = horizontalBarSelection,
                        marker = { markerData -> DefaultChartMarker(markerData) },
                    )
                    Text(
                        text =
                            horizontalBarSelection.selection?.let { sel ->
                                val group = horizontalBarSpec.groups.firstOrNull { it.id == sel.target.seriesId }
                                val entry = group?.entries?.getOrNull(sel.target.pointIndex)
                                val label = group?.label ?: sel.target.seriesId
                                entry?.let { "Selected $label: category=${it.x}, value=${it.y}" }
                                    ?: "Selected $label"
                            } ?: "Tap a horizontal bar (pan/zoom with two fingers).",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                PreviewScreen.Scatter -> {
                    ScatterChart(
                        spec = scatterSpec,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .padding(top = 8.dp),
                        viewportState = scatterViewport,
                        selectionState = scatterSelection,
                        marker = { markerData -> DefaultChartMarker(markerData) },
                    )
                    Text(
                        text =
                            scatterSelection.selection?.let { sel ->
                                val series = scatterSpec.series.firstOrNull { it.id == sel.target.seriesId }
                                val pt =
                                    series?.points?.getOrNull(sel.target.pointIndex)
                                        ?: ChartPoint(sel.target.point.x, sel.target.point.y)
                                val label = series?.label ?: sel.target.seriesId
                                "Selected $label: ${scatterSpec.valueFormatter(pt)}"
                            } ?: "Tap a point to select (pan/zoom with two fingers).",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                PreviewScreen.Combined -> {
                    CombinedCartesianChart(
                        spec = combinedSpec,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .padding(top = 8.dp),
                        viewportState = combinedViewport,
                        selectionState = combinedSelection,
                        marker = { markerData -> DefaultChartMarker(markerData) },
                    )
                    Text(
                        text =
                            combinedSelection.selection?.let { sel ->
                                val layer = sel.target.layerId ?: "?"
                                "${sel.target.seriesId} (layer $layer): index ${sel.target.pointIndex}"
                            } ?: "Tap bars or line (dual Y). Top layer wins. Pan/zoom with two fingers.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                PreviewScreen.Sankey -> {
                    val sankeySpec =
                        com.mena97villalobos.composeplotlib.core.spec.SankeyChartSpec(
                            id = "sankey",
                            nodes =
                                listOf(
                                    com.mena97villalobos.composeplotlib.core.spec
                                        .SankeyNode("Applications", column = 0, label = "Applications (total)"),
                                    com.mena97villalobos.composeplotlib.core.spec
                                        .SankeyNode("Rejected", column = 1, label = "Rejected (screening)"),
                                    com.mena97villalobos.composeplotlib.core.spec
                                        .SankeyNode("Interviews", column = 1, label = "Interviews"),
                                    com.mena97villalobos.composeplotlib.core.spec
                                        .SankeyNode("NoAnswer", column = 1, label = "No answer / ghosted"),
                                    com.mena97villalobos.composeplotlib.core.spec
                                        .SankeyNode("Offers", column = 2, label = "Offers extended"),
                                    com.mena97villalobos.composeplotlib.core.spec
                                        .SankeyNode("Accepted", column = 3, label = "Accepted offer"),
                                    com.mena97villalobos.composeplotlib.core.spec
                                        .SankeyNode("Declined", column = 3, label = "Declined offer"),
                                ),
                            links =
                                listOf(
                                    com.mena97villalobos.composeplotlib.core.spec
                                        .SankeyLink("Applications", "Rejected", 9.0),
                                    com.mena97villalobos.composeplotlib.core.spec
                                        .SankeyLink("Applications", "Interviews", 4.0),
                                    com.mena97villalobos.composeplotlib.core.spec
                                        .SankeyLink("Applications", "NoAnswer", 4.0),
                                    com.mena97villalobos.composeplotlib.core.spec
                                        .SankeyLink("Interviews", "Offers", 2.0),
                                    com.mena97villalobos.composeplotlib.core.spec
                                        .SankeyLink("Interviews", "Rejected", 2.0),
                                    com.mena97villalobos.composeplotlib.core.spec
                                        .SankeyLink("Offers", "Accepted", 1.0),
                                    com.mena97villalobos.composeplotlib.core.spec
                                        .SankeyLink("Offers", "Declined", 1.0),
                                ),
                            layout =
                                com.mena97villalobos.composeplotlib.core.spec.SankeyLayoutSpec(
                                    columnVerticalAlignment = com.mena97villalobos.composeplotlib.core.spec.SankeyColumnVerticalAlignment.Justify,
                                    outgoingLinkOrder = com.mena97villalobos.composeplotlib.core.spec.SankeyLinkOrder.TargetTopToBottom,
                                    incomingLinkOrder = com.mena97villalobos.composeplotlib.core.spec.SankeyLinkOrder.SourceTopToBottom,
                                ),
                        )
                    val sankeyViewport =
                        rememberChartViewportState(
                            initialViewport =
                                com.mena97villalobos.composeplotlib.core.viewport.ChartViewport(
                                    com.mena97villalobos.composeplotlib.core.range
                                        .ClosedRangeD(0.0, 1.0),
                                    com.mena97villalobos.composeplotlib.core.range
                                        .ClosedRangeD(0.0, 1.0),
                                ),
                        )
                    val sankeySelection = rememberChartSelectionState()
                    SankeyChart(
                        spec = sankeySpec,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .padding(top = 8.dp),
                        viewportState = sankeyViewport,
                        selectionState = sankeySelection,
                        gestureConfig = ChartGestureConfig.cartesianInteractiveFullExploration(),
                        marker = { markerData -> DefaultChartMarker(markerData, showCoordinateLine = false) },
                    )
                    Text(
                        text =
                            sankeySelection.selection?.let { "Selected ${it.target.layerId} index ${it.target.pointIndex}" }
                                ?: "Tap nodes or links to select. Pan/zoom with two fingers.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                PreviewScreen.Heatmap -> {
                    HeatmapChart(
                        spec = heatmapSpec,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .padding(top = 8.dp),
                        viewportState = heatmapViewport,
                        selectionState = heatmapSelection,
                        marker = { markerData -> DefaultChartMarker(markerData) },
                    )
                    Text(
                        text =
                            heatmapSelection.selection?.let { sel ->
                                val cell = heatmapSpec.cells.getOrNull(sel.target.pointIndex)
                                cell?.let { c ->
                                    val col = heatmapSpec.columnLabels.getOrElse(c.col) { c.col.toString() }
                                    val row = heatmapSpec.rowLabels.getOrElse(c.row) { c.row.toString() }
                                    "Selected $col / $row: ${heatmapSpec.valueFormatter(c)}"
                                } ?: "Selected cell ${sel.target.pointIndex}"
                            } ?: "Tap a cell to select it.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                PreviewScreen.Gauge -> {
                    GaugeChart(
                        spec = gaugeSpec,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .padding(top = 8.dp),
                        centerContent = {
                            val percent = kotlin.math.round(gaugeSpec.clampedProgress() * 100).toInt()
                            Text(
                                text = "$percent%",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                            )
                        },
                    )
                    Text(
                        text = "Read-only radial progress ring (270° sweep). centerContent shows the percentage.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                PreviewScreen.Candlestick -> {
                    CandlestickChart(
                        spec = candlestickSpec,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .padding(top = 8.dp),
                        viewportState = candlestickViewport,
                        selectionState = candlestickSelection,
                        gestureConfig = ChartGestureConfig.cartesianInteractive(),
                        marker = { markerData -> DefaultChartMarker(markerData) },
                    )
                    Text(
                        text =
                            candlestickSelection.selection?.let { sel ->
                                val series = candlestickSpec.series.firstOrNull { it.id == sel.target.seriesId }
                                val candle = series?.points?.getOrNull(sel.target.pointIndex)
                                candle?.let { candlestickSpec.valueFormatter(it) }
                                    ?: "Selected candle ${sel.target.pointIndex + 1}"
                            } ?: "Tap a candle for its OHLC values. Pan/zoom with two fingers.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                PreviewScreen.Sparkline -> {
                    Text(
                        text = "Axis-less mini line for inline trends (KPI cards, table cells), with an optional area fill.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Sparkline(
                        points = sparklinePoints,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(top = 8.dp),
                        color = ChartColor.Blue,
                        areaFill = ChartColor(0x331565C0L),
                    )
                }

                PreviewScreen.Pie -> {
                    PieChart(
                        spec = pieSpec,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .padding(top = 8.dp),
                        viewportState = pieViewport,
                        selectionState = pieSelection,
                        marker = { markerData -> DefaultChartMarker(markerData) },
                        centerContent = {
                            val total = pieSpec.slices.sumOf { it.value }
                            val label =
                                pieSelection.selection?.let { sel ->
                                    val slice = pieSpec.slices.getOrNull(sel.target.pointIndex)
                                    slice?.let { "${kotlin.math.round(it.value / total * 100)}%" }
                                } ?: "${kotlin.math.round(total)}\ntotal"
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                textAlign = TextAlign.Center,
                            )
                        },
                    )
                    Text(
                        text =
                            pieSelection.selection?.let { sel ->
                                val slice = pieSpec.slices.getOrNull(sel.target.pointIndex)
                                slice?.let { "${it.label}: ${pieSpec.sliceValueFormatter(it)}" }
                                    ?: "Selected slice ${sel.target.pointIndex + 1}"
                            } ?: "Tap a slice to see its percentage in the donut hole.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                PreviewScreen.Radar -> {
                    RadarChart(
                        spec = radarSpec,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .padding(top = 8.dp),
                        viewportState = radarViewport,
                        selectionState = radarSelection,
                        marker = { markerData -> DefaultChartMarker(markerData) },
                    )
                    Text(
                        text =
                            radarSelection.selection?.let { sel ->
                                val series = radarSpec.series.firstOrNull { it.id == sel.target.seriesId }
                                val axis = radarSpec.axes.getOrNull(sel.target.pointIndex)
                                val label = series?.label ?: sel.target.seriesId
                                axis?.let { "Selected $label: ${radarSpec.axisValueFormatter(it, sel.target.point.y)}" }
                                    ?: "Selected $label"
                            } ?: "Tap a vertex to select.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                PreviewScreen.DomainAxes -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Category + epoch time: truncates long category tick labels; WeekdaysUtc omits UTC weekend ticks.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        LineChart(
                            spec = domainCategorySpec,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                            viewportState = domainCategoryViewport,
                            selectionState = domainCategorySelection,
                            marker = { markerData -> DefaultChartMarker(markerData) },
                        )
                        LineChart(
                            spec = domainLongCategorySpec,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                            viewportState = domainLongCategoryViewport,
                            selectionState = domainLongCategorySelection,
                            marker = { markerData -> DefaultChartMarker(markerData) },
                        )
                        LineChart(
                            spec = domainTimeSpec,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                            viewportState = domainTimeViewport,
                            selectionState = domainTimeSelection,
                            marker = { markerData -> DefaultChartMarker(markerData) },
                        )
                        LineChart(
                            spec = domainWeekdayTimeSpec,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                            viewportState = domainWeekdayTimeViewport,
                            selectionState = domainWeekdayTimeSelection,
                            marker = { markerData -> DefaultChartMarker(markerData) },
                        )
                    }
                }
            }
        }
    }
}

private fun previewPieChartSpec(): PieChartSpec =
    PieChartSpec(
        id = "demo_pie",
        slices =
            listOf(
                PieChartSlice(35.0, "North", ChartColor.Blue),
                PieChartSlice(25.0, "South", ChartColor.Green),
                PieChartSlice(22.0, "East", ChartColor.Red),
                PieChartSlice(18.0, "West", ChartColor.Gray),
            ),
        holeRadiusRatio = 0.35,
        sliceGapDegrees = 2.0,
        sliceLabels = PieSliceLabelsSpec(enabled = true, labelColor = ChartColor.Black),
    )

private fun previewRadarChartSpec(): RadarChartSpec =
    RadarChartSpec(
        id = "demo_radar",
        axes = listOf("Speed", "Quality", "Cost", "Reliability", "UX", "Support"),
        series =
            listOf(
                RadarSeries(
                    id = "current",
                    label = "Current",
                    values = listOf(6.0, 7.0, 5.0, 6.5, 7.5, 6.0),
                    lineStyle = LineStyle(color = ChartColor.Blue, strokeWidth = 2.5),
                    pointStyle = PointStyle(visible = true, radius = 4.0, color = ChartColor.Blue),
                    fillColor = ChartColor(0x331565C0L),
                ),
                RadarSeries(
                    id = "target",
                    label = "Target",
                    values = listOf(8.0, 8.5, 6.0, 8.0, 9.0, 8.5),
                    lineStyle = LineStyle(color = ChartColor.Green, strokeWidth = 2.5),
                    pointStyle = PointStyle(visible = true, radius = 4.0, color = ChartColor.Green),
                    fillColor = ChartColor(0x332E7D32L),
                ),
            ),
    )

private fun previewLineChartSpec(): LineChartSpec =
    LineChartSpec(
        series =
            listOf(
                LineSeries(
                    id = "revenue",
                    label = "Revenue",
                    lineStyle =
                        LineStyle(
                            color = ChartColor.Blue,
                            // Translucent gradient area fill under the line (ToPlotBottom by default).
                            areaFill = ChartColor(0x551565C0L),
                            areaFillEnd = ChartColor(0x111565C0L),
                        ),
                    points =
                        listOf(
                            ChartPoint(0.0, 12.0),
                            ChartPoint(1.0, 18.0),
                            ChartPoint(2.0, 14.0),
                            ChartPoint(3.0, 21.0),
                            ChartPoint(4.0, 25.0),
                            ChartPoint(5.0, 22.0),
                        ),
                ),
            ),
        // Horizontal reference line marking a target threshold (visible within the fit-to-data viewport).
        referenceLines =
            listOf(
                ReferenceLine(
                    orientation = ReferenceOrientation.Horizontal,
                    value = 20.0,
                    color = ChartColor.Red,
                    strokeWidth = 1.5,
                    dashIntervals = listOf(8.0, 6.0),
                    label = "Target",
                ),
            ),
        accessibility =
            ChartAccessibilitySpec(
                keyboardViewportNudgeEnabled = true,
                keyboardSelectionRoamEnabled = true,
                requestFocusForAccessibility = true,
            ),
    )

/** Same series id and point count as [previewLineChartSpec] for data morph demos. */
private fun previewLineChartSpecDataB(): LineChartSpec =
    LineChartSpec(
        series =
            listOf(
                LineSeries(
                    id = "revenue",
                    label = "Revenue",
                    lineStyle = LineStyle(color = ChartColor.Blue),
                    points =
                        listOf(
                            ChartPoint(0.0, 8.0),
                            ChartPoint(1.0, 22.0),
                            ChartPoint(2.0, 10.0),
                            ChartPoint(3.0, 26.0),
                            ChartPoint(4.0, 18.0),
                            ChartPoint(5.0, 28.0),
                        ),
                ),
            ),
        accessibility =
            ChartAccessibilitySpec(
                keyboardViewportNudgeEnabled = true,
                keyboardSelectionRoamEnabled = true,
                requestFocusForAccessibility = true,
            ),
    )

private fun previewScatterChartSpec(): ScatterChartSpec =
    ScatterChartSpec(
        series =
            listOf(
                ScatterSeries(
                    id = "alpha",
                    label = "Alpha",
                    points =
                        listOf(
                            ChartPoint(0.5, 8.0),
                            // Per-point marker override: a larger diamond bubble.
                            ChartPoint(1.5, 14.0, markerShape = MarkerShape.Diamond, markerRadius = 10.0),
                            ChartPoint(2.5, 11.0),
                            // Bubble sized by markerRadius with its own color.
                            ChartPoint(3.5, 18.0, markerRadius = 13.0, markerColor = ChartColor.Red),
                        ),
                    // Series default glyph is a square; individual points override it above.
                    pointStyle = PointStyle(visible = true, radius = 5.0, color = ChartColor.Blue, shape = MarkerShape.Square),
                ),
                ScatterSeries(
                    id = "beta",
                    label = "Beta",
                    points =
                        listOf(
                            ChartPoint(1.0, 12.0),
                            ChartPoint(2.0, 16.0, markerRadius = 11.0),
                            ChartPoint(3.0, 9.0),
                            ChartPoint(4.0, 20.0),
                        ),
                    // Series default glyph is a triangle.
                    pointStyle = PointStyle(visible = true, radius = 4.0, color = ChartColor.Green, shape = MarkerShape.Triangle),
                ),
            ),
    )

private fun previewLongCategoryAxisLineSpec(): LineChartSpec {
    val labels =
        listOf(
            "North America",
            "Europe, Middle East, Africa",
            "Asia Pacific region",
            "Latin America",
        )
    val ys = listOf(12.0, 18.0, 22.0, 15.0)
    return LineChartSpec(
        series =
            listOf(
                LineSeries(
                    id = "regions",
                    label = "Regions",
                    lineStyle = LineStyle(color = ChartColor.Blue),
                    pointStyle = PointStyle(radius = 4.0),
                    points = labels.indices.map { i -> ChartPoint(i.toDouble(), ys[i]) },
                ),
            ),
        xAxis =
            AxisSpec(
                scale = AxisScale.Category(labels),
                tickCount = labels.size,
                categoryLabelMaxChars = 14,
            ),
        yAxis = AxisSpec(tickCount = 4),
        grid = GridSpec(lineStyle = GridLineStyle(color = ChartColor.LightGray)),
        legend = LegendSpec(),
    )
}

private fun previewCategoryAxisLineSpec(): LineChartSpec {
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val ys = listOf(4.0, 7.0, 5.0, 9.0, 11.0, 6.0, 8.0)
    return LineChartSpec(
        series =
            listOf(
                LineSeries(
                    id = "sessions",
                    label = "Sessions",
                    lineStyle = LineStyle(color = ChartColor.Blue),
                    pointStyle = PointStyle(radius = 4.0),
                    points = labels.indices.map { i -> ChartPoint(i.toDouble(), ys[i]) },
                ),
            ),
        xAxis = AxisSpec(scale = AxisScale.Category(labels), tickCount = labels.size),
        yAxis = AxisSpec(tickCount = 5),
        grid = GridSpec(lineStyle = GridLineStyle(color = ChartColor.LightGray)),
        legend = LegendSpec(),
    )
}

private fun previewEpochMillisLineSpec(): LineChartSpec {
    val origin = 1_700_000_000_000.0
    val step = 86_400_000.0 / 4.0
    val ys = listOf(2.0, 3.0, 4.0, 3.0, 5.0, 6.0, 5.0, 8.0, 7.0, 9.0, 8.0, 10.0, 11.0)
    return LineChartSpec(
        series =
            listOf(
                LineSeries(
                    id = "dwell",
                    label = "Dwell",
                    lineStyle = LineStyle(color = ChartColor.Green),
                    pointStyle = PointStyle(radius = 4.0),
                    points = ys.mapIndexed { i, y -> ChartPoint(origin + i * step, y) },
                ),
            ),
        xAxis = AxisSpec(scale = AxisScale.TimeEpochMillis, tickCount = 6),
        yAxis = AxisSpec(tickCount = 4),
        grid = GridSpec(lineStyle = GridLineStyle(color = ChartColor.LightGray)),
        legend = LegendSpec(),
    )
}

private fun previewWeekdayEpochMillisLineSpec(): LineChartSpec {
    val origin = 1_702_800_000_000.0
    val day = 86_400_000.0
    val ys = listOf(4.0, 5.0, 6.0, 5.5, 7.0, 6.0, 8.0, 7.5, 9.0, 8.5, 7.0, 8.0, 9.0, 8.0)
    return LineChartSpec(
        series =
            listOf(
                LineSeries(
                    id = "daily",
                    label = "Daily",
                    lineStyle = LineStyle(color = ChartColor.Green),
                    pointStyle = PointStyle(radius = 4.0),
                    points = ys.mapIndexed { i, y -> ChartPoint(origin + i * day, y) },
                ),
            ),
        xAxis =
            AxisSpec(
                scale = AxisScale.TimeEpochMillis,
                tickCount = 8,
                epochMillisTickPolicy = EpochMillisTickPolicy.WeekdaysUtc,
            ),
        yAxis = AxisSpec(tickCount = 4),
        grid = GridSpec(lineStyle = GridLineStyle(color = ChartColor.LightGray)),
        legend = LegendSpec(),
    )
}

private fun previewCombinedCartesianSpec(): CombinedCartesianChartSpec =
    CombinedCartesianChartSpec(
        layers =
            listOf(
                CombinedCartesianLayer.Bar(
                    id = "bars",
                    yAxisBinding = CombinedCartesianYAxisBinding.SharedY,
                    groups =
                        listOf(
                            BarGroup(
                                id = "units",
                                label = "Units",
                                style = BarStyle(color = ChartColor.Green),
                                entries =
                                    listOf(
                                        BarEntry(0.0, 12.0),
                                        BarEntry(1.0, 18.0),
                                        BarEntry(2.0, 14.0),
                                        BarEntry(3.0, 20.0),
                                    ),
                            ),
                        ),
                ),
                CombinedCartesianLayer.Line(
                    id = "trend",
                    yAxisBinding = CombinedCartesianYAxisBinding.SecondaryY,
                    series =
                        listOf(
                            LineSeries(
                                id = "rate",
                                label = "Rate %",
                                lineStyle = LineStyle(color = ChartColor.Blue),
                                pointStyle = PointStyle(radius = 5.0),
                                points =
                                    listOf(
                                        ChartPoint(0.0, 42.0),
                                        ChartPoint(1.0, 55.0),
                                        ChartPoint(2.0, 48.0),
                                        ChartPoint(3.0, 62.0),
                                    ),
                            ),
                        ),
                ),
            ),
        xAxis = AxisSpec(tickCount = 5),
        yAxis = AxisSpec(tickCount = 4),
        yAxisSecondary =
            AxisSpec(
                tickCount = 4,
                labelStyle = AxisLabelStyle(color = ChartColor.Blue),
            ),
        grid = GridSpec(lineStyle = GridLineStyle(color = ChartColor.LightGray)),
        legend = LegendSpec(),
        contentPadding = InsetsD(left = 56.0, top = 48.0, right = 48.0, bottom = 40.0),
    )

private fun previewBarChartSpec(): BarChartSpec =
    BarChartSpec(
        groups =
            listOf(
                BarGroup(
                    id = "sales",
                    label = "Sales",
                    style = BarStyle(color = ChartColor.Green),
                    entries =
                        listOf(
                            BarEntry(0.0, 10.0),
                            BarEntry(1.0, 16.0),
                            BarEntry(2.0, 12.0),
                            BarEntry(3.0, 19.0),
                        ),
                ),
                BarGroup(
                    id = "returns",
                    label = "Returns",
                    style = BarStyle(color = ChartColor.Red),
                    entries =
                        listOf(
                            BarEntry(0.5, 4.0),
                            BarEntry(1.5, 5.0),
                            BarEntry(2.5, 3.0),
                            BarEntry(3.5, 6.0),
                        ),
                ),
            ),
    )

private fun previewStackedBarChartSpec(): StackedBarChartSpec =
    StackedBarChartSpec(
        groups =
            listOf(
                BarGroup(
                    id = "sales",
                    label = "Sales",
                    style = BarStyle(color = ChartColor.Green),
                    entries =
                        listOf(
                            BarEntry(0.0, 10.0),
                            BarEntry(1.0, 16.0),
                            BarEntry(2.0, 12.0),
                            BarEntry(3.0, 19.0),
                        ),
                ),
                BarGroup(
                    id = "returns",
                    label = "Returns",
                    style = BarStyle(color = ChartColor.Red),
                    entries =
                        listOf(
                            BarEntry(0.0, 4.0),
                            BarEntry(1.0, 5.0),
                            BarEntry(2.0, 3.0),
                            BarEntry(3.0, 6.0),
                        ),
                ),
            ),
    )

private fun previewHorizontalBarChartSpec(): BarChartSpec =
    BarChartSpec(
        orientation = BarChartOrientation.Horizontal,
        barLayout = BarLayoutMode.Clustered,
        groups =
            listOf(
                BarGroup(
                    id = "sales",
                    label = "Sales",
                    style = BarStyle(color = ChartColor.Green),
                    entries =
                        listOf(
                            BarEntry(0.0, 10.0),
                            BarEntry(1.0, 16.0),
                            BarEntry(2.0, 12.0),
                            BarEntry(3.0, 19.0),
                        ),
                ),
                BarGroup(
                    id = "returns",
                    label = "Returns",
                    style = BarStyle(color = ChartColor.Red),
                    entries =
                        listOf(
                            BarEntry(0.0, 4.0),
                            BarEntry(1.0, 5.0),
                            BarEntry(2.0, 3.0),
                            BarEntry(3.0, 6.0),
                        ),
                ),
            ),
    )

private fun previewHeatmapChartSpec(): HeatmapChartSpec =
    HeatmapChartSpec(
        id = "activity",
        columnLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
        rowLabels = listOf("Week 1", "Week 2", "Week 3", "Week 4"),
        cells =
            listOf(
                HeatmapCell(0, 0, 20.0),
                HeatmapCell(1, 0, 45.0),
                HeatmapCell(2, 0, 80.0),
                HeatmapCell(3, 0, 55.0),
                HeatmapCell(4, 0, 70.0),
                HeatmapCell(5, 0, 10.0),
                HeatmapCell(6, 0, 5.0),
                HeatmapCell(0, 1, 60.0),
                HeatmapCell(1, 1, 75.0),
                HeatmapCell(2, 1, 90.0),
                HeatmapCell(3, 1, 85.0),
                HeatmapCell(4, 1, 50.0),
                HeatmapCell(5, 1, 30.0),
                HeatmapCell(6, 1, 15.0),
                HeatmapCell(0, 2, 40.0),
                HeatmapCell(1, 2, 65.0),
                HeatmapCell(2, 2, 100.0),
                HeatmapCell(3, 2, 95.0),
                HeatmapCell(4, 2, 80.0),
                HeatmapCell(5, 2, 25.0),
                HeatmapCell(6, 2, 20.0),
                HeatmapCell(0, 3, 35.0),
                HeatmapCell(1, 3, 55.0),
                HeatmapCell(2, 3, 70.0),
                HeatmapCell(3, 3, 60.0),
                HeatmapCell(4, 3, 45.0),
                HeatmapCell(5, 3, 40.0),
                HeatmapCell(6, 3, 30.0),
            ),
        colorScale = HeatmapColorScale(0.0, 100.0, ChartColor.LightGray, ChartColor.Blue),
    )

private fun previewGaugeSpec(): GaugeChartSpec =
    GaugeChartSpec(
        progress = 0.68,
        id = "demo_gauge",
        trackColor = ChartColor.LightGray,
        progressColor = ChartColor.Blue,
        strokeWidthPx = 28.0,
        roundedCaps = true,
        sweepDegrees = 270.0,
    )

private fun previewCandlestickSpec(): CandlestickChartSpec =
    CandlestickChartSpec(
        series =
            listOf(
                CandlestickSeries(
                    id = "acme",
                    label = "ACME",
                    points =
                        listOf(
                            OHLCPoint(x = 0.0, open = 20.0, high = 24.0, low = 19.0, close = 23.0),
                            OHLCPoint(x = 1.0, open = 23.0, high = 25.0, low = 21.5, close = 22.0),
                            OHLCPoint(x = 2.0, open = 22.0, high = 22.5, low = 18.0, close = 19.0),
                            OHLCPoint(x = 3.0, open = 19.0, high = 23.0, low = 18.5, close = 22.5),
                            OHLCPoint(x = 4.0, open = 22.5, high = 27.0, low = 22.0, close = 26.5),
                            OHLCPoint(x = 5.0, open = 26.5, high = 28.0, low = 25.0, close = 25.5),
                            OHLCPoint(x = 6.0, open = 25.5, high = 26.0, low = 22.0, close = 23.0),
                            OHLCPoint(x = 7.0, open = 23.0, high = 25.5, low = 22.5, close = 25.0),
                        ),
                ),
            ),
    )

private fun previewSparklinePoints(): List<ChartPoint> =
    listOf(
        ChartPoint(0.0, 4.0),
        ChartPoint(1.0, 6.0),
        ChartPoint(2.0, 5.0),
        ChartPoint(3.0, 8.0),
        ChartPoint(4.0, 7.0),
        ChartPoint(5.0, 11.0),
        ChartPoint(6.0, 9.0),
        ChartPoint(7.0, 13.0),
    )

private enum class PreviewScreen {
    Line,
    Bar,
    StackedBar,
    HorizontalBar,
    Scatter,
    Pie,
    Radar,
    Combined,
    Sankey,
    Heatmap,
    Gauge,
    Candlestick,
    Sparkline,
    DomainAxes,
}
