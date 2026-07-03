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
        // Hit priority follows visual z-order: whichever layer draws on top wins the tap.
        return if (spec.layout.linksBehindNodes) {
            resolveNode(touch, spec, plan.nodes) ?: resolveLink(touch, spec, plan.links)
        } else {
            resolveLink(touch, spec, plan.links) ?: resolveNode(touch, spec, plan.nodes)
        }
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
        // The crossing precondition guarantees pi.y != pj.y on entry.
        val intersects =
            ((pi.y > p.y) != (pj.y > p.y)) &&
                (p.x < (pj.x - pi.x) * (p.y - pi.y) / (pj.y - pi.y) + pi.x)
        if (intersects) inside = !inside
        j = i
    }
    return inside
}
