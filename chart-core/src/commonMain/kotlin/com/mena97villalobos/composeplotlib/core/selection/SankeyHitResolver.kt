package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.layout.PlannedSankeyLink
import com.mena97villalobos.composeplotlib.core.layout.PlannedSankeyNode
import com.mena97villalobos.composeplotlib.core.layout.SankeyChartPlan
import com.mena97villalobos.composeplotlib.core.spec.SankeyChartSpec

object SankeyHitResolver {
    const val LayerNode: String = "node"
    const val LayerLink: String = "link"

    fun resolve(
        touch: OffsetD,
        spec: SankeyChartSpec,
        plan: SankeyChartPlan,
    ): ChartSelection? {
        // Nodes are drawn on top of links by default; prioritize node hits first.
        resolveNode(touch, spec, plan.nodes)?.let { return it }
        resolveLink(touch, spec, plan.links)?.let { return it }
        return null
    }

    private fun resolveNode(
        touch: OffsetD,
        spec: SankeyChartSpec,
        nodes: List<PlannedSankeyNode>,
    ): ChartSelection? {
        val idx = nodes.indexOfLast { it.pixelBounds.contains(touch) }
        if (idx < 0) return null
        val n = nodes[idx]
        return ChartSelection(
            target =
                HighlightTarget(
                    seriesId = spec.id,
                    pointIndex = idx,
                    point = XyPoint(0.0, 0.0),
                    layerId = LayerNode,
                ),
            pixelOffset = n.markerAnchor,
            markerAnchor = n.markerAnchor,
            hitBounds = n.pixelBounds,
        )
    }

    private fun resolveLink(
        touch: OffsetD,
        spec: SankeyChartSpec,
        links: List<PlannedSankeyLink>,
    ): ChartSelection? {
        // Topmost link should win; last link drawn is last in list.
        for (i in links.indices.reversed()) {
            val l = links[i]
            if (!l.pixelBounds.contains(touch)) continue
            if (!pointInPolygon(touch, l.pixelPolygon)) continue
            return ChartSelection(
                target =
                    HighlightTarget(
                        seriesId = spec.id,
                        pointIndex = i,
                        point = XyPoint(0.0, 0.0),
                        layerId = LayerLink,
                    ),
                pixelOffset = l.markerAnchor,
                markerAnchor = l.markerAnchor,
                hitBounds = l.pixelBounds,
            )
        }
        return null
    }
}

/**
 * Ray casting test for point-in-polygon.
 *
 * Polygon is expected to be closed (first != last is fine; algorithm treats it as closed).
 */
private fun pointInPolygon(
    p: OffsetD,
    polygon: List<OffsetD>,
): Boolean {
    if (polygon.size < 3) return false
    var inside = false
    var j = polygon.lastIndex
    for (i in polygon.indices) {
        val pi = polygon[i]
        val pj = polygon[j]
        val intersects =
            ((pi.y > p.y) != (pj.y > p.y)) &&
                (p.x < (pj.x - pi.x) * (p.y - pi.y) / ((pj.y - pi.y).takeIf { it != 0.0 } ?: 1e-9) + pi.x)
        if (intersects) inside = !inside
        j = i
    }
    return inside
}

