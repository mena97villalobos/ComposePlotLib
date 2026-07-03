package com.mena97villalobos.composeplotlib.samples.android.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.ChartAnimationPolicy
import com.mena97villalobos.composeplotlib.chart.ChartCustomAccessibilityAction
import com.mena97villalobos.composeplotlib.chart.ChartGestureConfig
import com.mena97villalobos.composeplotlib.chart.ChartMarkerCalloutLeader
import com.mena97villalobos.composeplotlib.chart.ChartMarkerCustomCallout
import com.mena97villalobos.composeplotlib.chart.ChartMarkerPlacement
import com.mena97villalobos.composeplotlib.chart.ChartNoDataDefaults
import com.mena97villalobos.composeplotlib.chart.DefaultChartMarker
import com.mena97villalobos.composeplotlib.chart.LineChart
import com.mena97villalobos.composeplotlib.chart.Sparkline
import com.mena97villalobos.composeplotlib.chart.chartAccessibilityActionsMergeViewport
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.samples.android.data.annotatedLineChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.emptyLineChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.gappedLineChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.largeDenseLineChartInitialViewport
import com.mena97villalobos.composeplotlib.samples.android.data.layoutStressLineChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.lineChartSpecCustomCalloutFocusDemo
import com.mena97villalobos.composeplotlib.samples.android.data.lineChartSpecTooltipEdgeDemo
import com.mena97villalobos.composeplotlib.samples.android.data.logScaleLineChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.sampleLargeDenseLineChartSpec
import com.mena97villalobos.composeplotlib.samples.android.data.sampleLineChartSpec

@Composable
fun LineChartShowcaseScreen() {
    var variant by remember { mutableStateOf(LineShowcaseVariant.Styled) }
    var connectGaps by remember { mutableStateOf(false) }
    val largeSpec = remember { sampleLargeDenseLineChartSpec() }
    val layoutStressSpec = remember { layoutStressLineChartSpec() }
    val spec =
        when (variant) {
            LineShowcaseVariant.Styled -> sampleLineChartSpec()
            LineShowcaseVariant.Empty -> emptyLineChartSpec()
            LineShowcaseVariant.LargeSeries -> largeSpec
            LineShowcaseVariant.LayoutStress -> layoutStressSpec
            LineShowcaseVariant.Gaps -> gappedLineChartSpec(connectAcrossGaps = connectGaps)
            LineShowcaseVariant.LogY -> logScaleLineChartSpec()
            LineShowcaseVariant.Annotated -> annotatedLineChartSpec()
        }
    val dataExtent =
        when (variant) {
            LineShowcaseVariant.LargeSeries -> largeDenseLineChartInitialViewport()
            else -> defaultViewport(spec)
        }
    val viewportState =
        rememberChartViewportState(
            initialViewport = dataExtent,
            domain = dataExtent,
        )
    val selectionState = rememberChartSelectionState()
    LaunchedEffect(variant) {
        selectionState.clear()
    }
    val placementDemoSpec = remember { lineChartSpecTooltipEdgeDemo() }
    val placementDemoViewport = rememberChartViewportState(initialViewport = defaultViewport(placementDemoSpec))
    val placementDemoSelection = rememberChartSelectionState()
    val customCalloutSpec = remember { lineChartSpecCustomCalloutFocusDemo() }
    val customCalloutViewport = rememberChartViewportState(initialViewport = defaultViewport(customCalloutSpec))
    val customCalloutSelection = rememberChartSelectionState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .semantics {
                    contentDescription =
                        "Line chart showcase. Choose styled, empty, or large dense data, use viewport actions, " +
                        "tap chart to select a point. " +
                        "Styled chart is focusable: arrow keys pan, plus and minus zoom, Escape clears selection; " +
                        "Shift+arrows move point selection and Shift+Up/Down switch series; double-tap zooms in; " +
                        "TalkBack includes viewport actions plus app actions. " +
                        "Marker focus handoff is on (requestFocusOnMarker): after selecting a point, focus can move " +
                        "to the tooltip for details. " +
                        "Selection highlight fades out when selection clears (Escape or reset) when animation is enabled."
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Line Chart Showcase",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "This screen validates the first real chart slice: pan, zoom, selection, axes, and legend. " +
                    "The Empty variant uses noDataUi presets; use the noData { } slot on LineChart for a fully custom empty layout. " +
                    "Styled data opts into keyboard viewport shortcuts and TalkBack focus. " +
                    "Large (35k pts) opts into plot-width pixel budget + MinMaxY decimation so planning stays bounded.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = { variant = LineShowcaseVariant.Styled }) {
                Text("Styled")
            }
            Button(onClick = { variant = LineShowcaseVariant.Empty }) {
                Text("Empty")
            }
            Button(onClick = { variant = LineShowcaseVariant.LargeSeries }) {
                Text("Large 35k")
            }
            Button(onClick = { variant = LineShowcaseVariant.LayoutStress }) {
                Text("Layout")
            }
            Button(onClick = { variant = LineShowcaseVariant.Gaps }) {
                Text("Gaps")
            }
            Button(onClick = { variant = LineShowcaseVariant.LogY }) {
                Text("Log Y")
            }
            Button(onClick = { variant = LineShowcaseVariant.Annotated }) {
                Text("Annotated")
            }
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = {
                viewportState.reset()
                selectionState.clear()
            }) {
                Text("Reset")
            }
            if (variant == LineShowcaseVariant.Gaps) {
                Button(onClick = { connectGaps = !connectGaps }) {
                    Text(if (connectGaps) "Break at gaps" else "Connect gaps")
                }
            }
            if (variant != LineShowcaseVariant.LargeSeries && variant != LineShowcaseVariant.LayoutStress) {
                Button(onClick = {
                    viewportState.zoomTo(xSpan = 3.0, ySpan = 12.0, centerX = 3.0, centerY = 15.0)
                }) {
                    Text("Zoom To")
                }
                Button(onClick = {
                    viewportState.centerOn(x = 5.0, y = 20.0)
                }) {
                    Text("Center On")
                }
            }
        }
        LineChart(
            spec = spec,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(top = 8.dp),
            viewportState = viewportState,
            selectionState = selectionState,
            gestureConfig = ChartGestureConfig.cartesianInteractive(),
            marker = { markerData ->
                DefaultChartMarker(markerData)
            },
            noDataUi =
                if (variant == LineShowcaseVariant.Empty) {
                    ChartNoDataDefaults.lineChart()
                } else {
                    null
                },
            accessibilityCustomActions =
                chartAccessibilityActionsMergeViewport(
                    viewportState = viewportState,
                    gestureConfig = ChartGestureConfig.cartesianInteractive(),
                    appActions =
                        listOf(
                            ChartCustomAccessibilityAction("Clear selection only") {
                                selectionState.clear()
                            },
                        ),
                ),
            animation =
                ChartAnimationPolicy(
                    selectionHighlightEnabled = true,
                    selectionDurationMillis = 220,
                ),
        )
        Text(
            text =
                selectionState.selection?.let { selection ->
                    val series = spec.series.firstOrNull { it.id == selection.target.seriesId }
                    val point =
                        series?.points?.getOrNull(selection.target.pointIndex)
                            ?: ChartPoint(selection.target.point.x, selection.target.point.y)
                    val seriesLabel = series?.label ?: selection.target.seriesId
                    "Selected $seriesLabel: ${spec.valueFormatter(point)}"
                } ?: "Tap a point to inspect selection.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text =
                "Sparkline: an axis-less, non-interactive mini line with an optional area fill, for inline trends " +
                    "(KPI cards, table cells, list rows).",
            style = MaterialTheme.typography.bodyMedium,
        )
        Sparkline(
            points =
                listOf(
                    ChartPoint(0.0, 4.0),
                    ChartPoint(1.0, 6.0),
                    ChartPoint(2.0, 5.0),
                    ChartPoint(3.0, 8.0),
                    ChartPoint(4.0, 7.0),
                    ChartPoint(5.0, 11.0),
                    ChartPoint(6.0, 9.0),
                    ChartPoint(7.0, 13.0),
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(64.dp),
            color = ChartColor.Blue,
            areaFill = ChartColor(0x551565C0L),
            areaFillEnd = ChartColor(0x111565C0L),
        )

        if (variant == LineShowcaseVariant.Styled) {
            Text(
                text =
                    "High-Y demo: tap a point near the top of the plot; the tooltip flips below the anchor when " +
                        "there is not enough room above. Outlines use DefaultChartMarker(showOutline = true); " +
                        "ChartMarkerPlacement.callout draws an elbow leader to the box.",
                style = MaterialTheme.typography.bodyMedium,
            )
            LineChart(
                spec = placementDemoSpec,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(top = 4.dp),
                viewportState = placementDemoViewport,
                selectionState = placementDemoSelection,
                markerPlacement = ChartMarkerPlacement(callout = ChartMarkerCalloutLeader.DefaultElbow),
                marker = { markerData ->
                    DefaultChartMarker(markerData, showOutline = true)
                },
            )
            Text(
                text =
                    "Custom callout: straight segment from anchor to box center (ChartMarkerCalloutLeader.custom). " +
                        "Focusable button inside marker uses requestFocusOnMarker + unmerged marker semantics (Tab into tooltip).",
                style = MaterialTheme.typography.bodyMedium,
            )
            LineChart(
                spec = customCalloutSpec,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(top = 4.dp),
                viewportState = customCalloutViewport,
                selectionState = customCalloutSelection,
                gestureConfig = ChartGestureConfig.cartesianInteractive(),
                markerPlacement =
                    ChartMarkerPlacement(
                        callout =
                            ChartMarkerCalloutLeader.custom(
                                path =
                                    ChartMarkerCustomCallout { ax, ay, left, top, w, h ->
                                        val cx = left + w * 0.5
                                        val cy = top + h * 0.5
                                        listOf(ax to ay, cx to cy)
                                    },
                            ),
                    ),
                marker = { markerData ->
                    Column {
                        DefaultChartMarker(markerData, showOutline = true)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {}) {
                            Text("Marker action")
                        }
                    }
                },
            )
        }
    }
}

private enum class LineShowcaseVariant {
    Styled,
    Empty,
    LargeSeries,

    /** Staggered X tick labels + legend ellipses (layout stress). */
    LayoutStress,

    /** Missing-data gaps: line/area break at [ChartPoint.gap] markers unless connectAcrossGaps is on. */
    Gaps,

    /** Logarithmic Y axis (AxisScale.Log): exponential data reads as a straight line, decades evenly spaced. */
    LogY,

    /** Annotations layer: a target band, a labeled peak marker, a dashed trend segment, and a free text note. */
    Annotated,
}
