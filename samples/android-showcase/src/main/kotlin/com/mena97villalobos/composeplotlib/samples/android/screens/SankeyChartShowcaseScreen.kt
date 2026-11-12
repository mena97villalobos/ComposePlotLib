package com.mena97villalobos.composeplotlib.samples.android.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.ChartGestureConfig
import com.mena97villalobos.composeplotlib.chart.DefaultChartMarker
import com.mena97villalobos.composeplotlib.chart.SankeyChart
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.SankeyChartSpec
import com.mena97villalobos.composeplotlib.core.spec.SankeyColumnAlignment
import com.mena97villalobos.composeplotlib.core.spec.SankeyLayoutSpec
import com.mena97villalobos.composeplotlib.core.spec.SankeyLink
import com.mena97villalobos.composeplotlib.core.spec.SankeyLinkLabelsSpec
import com.mena97villalobos.composeplotlib.core.spec.SankeyLinkOrder
import com.mena97villalobos.composeplotlib.core.spec.SankeyNode
import com.mena97villalobos.composeplotlib.core.spec.SankeyOrientation
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport

private val showcaseNodes =
    listOf(
        SankeyNode("Applications", column = 0, label = "Applications (total)"),
        SankeyNode("Rejected", column = 1, label = "Rejected (screening)"),
        SankeyNode("Interviews", column = 1),
        SankeyNode("NoAnswer", column = 1, label = "No answer / ghosted"),
        SankeyNode("Offers", column = 2, label = "Offers extended"),
        SankeyNode("Accepted", column = 3, label = "Accepted offer"),
        SankeyNode("Declined", column = 3, label = "Declined offer"),
    )

private val showcaseLinks =
    listOf(
        SankeyLink("Applications", "Rejected", 9.0),
        SankeyLink("Applications", "Interviews", 4.0),
        SankeyLink("Applications", "NoAnswer", 4.0),
        SankeyLink("Interviews", "Offers", 2.0),
        SankeyLink("Interviews", "Rejected", 2.0),
        SankeyLink("Offers", "Accepted", 1.0),
        SankeyLink("Offers", "Declined", 1.0),
    )

@Composable
fun SankeyChartShowcaseScreen() {
    val viewport =
        rememberChartViewportState(
            initialViewport =
                ChartViewport(
                    xRange = ClosedRangeD(0.0, 1.0),
                    yRange = ClosedRangeD(0.0, 1.0),
                ),
        )
    val selection = rememberChartSelectionState()
    val spec =
        SankeyChartSpec(
            id = "job_search",
            nodes = showcaseNodes,
            links = showcaseLinks,
            layout =
                SankeyLayoutSpec(
                    columnAlignment = SankeyColumnAlignment.Justify,
                    outgoingLinkOrder = SankeyLinkOrder.TargetTopToBottom,
                    incomingLinkOrder = SankeyLinkOrder.SourceTopToBottom,
                ),
        )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        SankeyChart(
            spec = spec,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(360.dp),
            viewportState = viewport,
            selectionState = selection,
            gestureConfig = ChartGestureConfig.cartesianInteractiveFullExploration(),
            marker = { markerData -> DefaultChartMarker(markerData, showCoordinateLine = false) },
        )
        Text(
            text =
                selection.selection?.let { "Selected ${it.target.layerId} index ${it.target.pointIndex}" }
                    ?: "Tap nodes or links. Pan/zoom with two fingers.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 12.dp),
        )

        Text(
            text = "Vertical flow (top → bottom)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp),
        )
        val verticalViewport =
            rememberChartViewportState(
                initialViewport =
                    ChartViewport(
                        xRange = ClosedRangeD(0.0, 1.0),
                        yRange = ClosedRangeD(0.0, 1.0),
                    ),
            )
        val verticalSelection = rememberChartSelectionState()
        SankeyChart(
            spec =
                SankeyChartSpec(
                    id = "job_search_vertical",
                    nodes = showcaseNodes,
                    links = showcaseLinks,
                    layout =
                        SankeyLayoutSpec(
                            orientation = SankeyOrientation.Vertical,
                            columnAlignment = SankeyColumnAlignment.Justify,
                            outgoingLinkOrder = SankeyLinkOrder.TargetTopToBottom,
                            incomingLinkOrder = SankeyLinkOrder.SourceTopToBottom,
                            linkLabels = SankeyLinkLabelsSpec.Disabled,
                        ),
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .padding(top = 8.dp),
            viewportState = verticalViewport,
            selectionState = verticalSelection,
            gestureConfig = ChartGestureConfig.cartesianInteractiveFullExploration(),
            marker = { markerData -> DefaultChartMarker(markerData, showCoordinateLine = false) },
        )
    }
}
