package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.draw.PathSegment
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
    /** Pixel bounds of the sampled ribbon outline (tight; used as a hit-test pre-filter). */
    val pixelBounds: RectD,
    /**
     * Simple polygon approximating the ribbon outline in pixel space (closed; both Bézier edges are
     * flattened into samples so hit-testing matches the drawn ribbon).
     */
    val pixelPolygon: List<OffsetD>,
    /** Anchor for markers: the ribbon centerline's Bézier midpoint. */
    val markerAnchor: OffsetD,
    /** Exact ribbon outline segments (as drawn); reusable for selection emphasis overlays. */
    val pathSegments: List<PathSegment>,
)
