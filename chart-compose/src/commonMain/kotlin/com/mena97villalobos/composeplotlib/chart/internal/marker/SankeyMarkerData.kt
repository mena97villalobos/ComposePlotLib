package com.mena97villalobos.composeplotlib.chart.internal.marker

import com.mena97villalobos.composeplotlib.chart.ChartMarkerData
import com.mena97villalobos.composeplotlib.core.layout.SankeyChartPlan
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection
import com.mena97villalobos.composeplotlib.core.selection.SankeyHitResolver
import com.mena97villalobos.composeplotlib.core.spec.SankeyChartSpec

internal fun buildSankeyMarkerData(
    spec: SankeyChartSpec,
    plan: SankeyChartPlan,
    selection: ChartSelection,
): ChartMarkerData? {
    if (selection.target.seriesId != spec.id) return null
    val idx = selection.target.pointIndex
    return when (selection.target.layerId) {
        SankeyHitResolver.LayerNode -> {
            val node = plan.nodes.getOrNull(idx) ?: return null
            ChartMarkerData(
                selection = selection,
                seriesId = spec.id,
                seriesLabel = "Sankey node",
                xLabel = "col=${node.column}",
                yLabel = node.label,
                valueLabel = node.label,
                accessibilitySummary = "Selected node ${node.label} in column ${node.column}.",
            )
        }

        SankeyHitResolver.LayerLink -> {
            val link = plan.links.getOrNull(idx) ?: return null
            val src = spec.nodes.firstOrNull { it.id == link.sourceNodeId }?.label ?: link.sourceNodeId
            val dst = spec.nodes.firstOrNull { it.id == link.targetNodeId }?.label ?: link.targetNodeId
            ChartMarkerData(
                selection = selection,
                seriesId = spec.id,
                seriesLabel = "Sankey link",
                xLabel = "$src → $dst",
                yLabel = "",
                valueLabel = "value=${link.value}",
                accessibilitySummary = "Selected link $src to $dst. Value ${link.value}.",
            )
        }

        else -> null
    }
}

