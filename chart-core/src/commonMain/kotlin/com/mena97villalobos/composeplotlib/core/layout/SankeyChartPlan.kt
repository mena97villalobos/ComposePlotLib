package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection

data class SankeyChartPlan(
    val layout: ChartLayoutPlan,
    val nodes: List<PlannedSankeyNode>,
    val links: List<PlannedSankeyLink>,
    val selection: ChartSelection?,
)

data class PlannedSankeyNode(
    val nodeId: String,
    val column: Int,
    val label: String,
    /** Node bounds in **plot pixel space** (after viewport transform). */
    val pixelBounds: RectD,
    /** Anchor for markers and accessibility focus. */
    val markerAnchor: OffsetD,
)

data class PlannedSankeyLink(
    val sourceNodeId: String,
    val targetNodeId: String,
    val value: Double,
    /** Conservative pixel bounds for hit-testing (the ribbon can be curved). */
    val pixelBounds: RectD,
    /** Polygon describing the ribbon outline in pixel space (closed). */
    val pixelPolygon: List<OffsetD>,
    /** Anchor for markers. */
    val markerAnchor: OffsetD,
)
