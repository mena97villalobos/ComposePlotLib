package com.mena97villalobos.composeplotlib.samples.android.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.ChartAnimationPolicy
import com.mena97villalobos.composeplotlib.chart.TreemapChart
import com.mena97villalobos.composeplotlib.chart.state.ChartDrillState
import com.mena97villalobos.composeplotlib.chart.state.rememberSaveableChartDrillState
import com.mena97villalobos.composeplotlib.core.spec.HierarchyHighlightStyle
import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchyNode
import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchySpec
import com.mena97villalobos.composeplotlib.core.spec.findNode
import com.mena97villalobos.composeplotlib.core.spec.layoutSize
import com.mena97villalobos.composeplotlib.samples.android.data.formatBytes
import com.mena97villalobos.composeplotlib.samples.android.data.sampleStorageTreemapSpec

/** Test tag of the treemap on [InteractiveTreemapShowcaseScreen]. */
const val INTERACTIVE_TREEMAP_TAG = "interactiveTreemap"

/**
 * A storage explorer built on the hierarchical [TreemapChart]: tap a folder to drill in, tap a file (or a list row)
 * to highlight it in both places, long-press a tile for its details, and go back with the breadcrumb or the system
 * back gesture. The drill path and highlight survive configuration changes.
 */
@Composable
fun InteractiveTreemapShowcaseScreen(
    spec: TreemapHierarchySpec = remember { sampleStorageTreemapSpec().copy(highlight = HierarchyHighlightStyle(dimOthersAlpha = 0.45)) },
    animation: ChartAnimationPolicy = ChartAnimationPolicy(viewportChangesEnabled = true),
    drill: ChartDrillState<String?> = rememberSaveableChartDrillState<String?>(null),
) {
    var highlighted by rememberSaveable { mutableStateOf(emptySet<String>()) }
    var details by rememberSaveable { mutableStateOf<String?>(null) }

    fun drillTo(depth: Int) {
        while (drill.depth > depth) drill.drillUp()
        highlighted = emptySet()
    }

    BackHandler(enabled = drill.canDrillUp) { drillTo(drill.depth - 1) }

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Tap a folder to open it, a file to highlight it; long-press for details.", style = MaterialTheme.typography.bodyMedium)
        Breadcrumb(spec, drill.path, onSelect = ::drillTo)
        TreemapChart(
            spec = spec,
            modifier = Modifier.fillMaxWidth().height(340.dp).testTag(INTERACTIVE_TREEMAP_TAG),
            rootNodeId = drill.current,
            highlightedNodeIds = highlighted,
            onNodeClick = { hit ->
                when {
                    hit.hasChildren -> {
                        drill.drillDown(hit.nodeId)
                        highlighted = emptySet()
                    }
                    hit.isAggregate -> highlighted = hit.memberIds.toSet()
                    else -> highlighted = setOf(hit.nodeId)
                }
            },
            onNodeLongClick = { hit ->
                details = "${hit.label}: ${hit.valueText ?: hit.value}" + if (hit.isAggregate) " (${hit.memberIds.size} items)" else ""
            },
            animation = animation,
        )
        Text(details ?: "Long-press a tile for its details.", style = MaterialTheme.typography.labelMedium)
        val level = drill.current?.let { spec.findNode(it)?.children } ?: spec.nodes
        level
            .filter { it.layoutSize() > 0.0 }
            .sortedByDescending { it.layoutSize() }
            .forEach { node ->
                LevelRow(
                    node = node,
                    selected = node.id in highlighted,
                    onClick = {
                        if (node.children.isNotEmpty()) {
                            drill.drillDown(node.id)
                            highlighted = emptySet()
                        } else {
                            highlighted = if (node.id in highlighted) emptySet() else setOf(node.id)
                        }
                    },
                )
            }
    }
}

@Composable
private fun Breadcrumb(
    spec: TreemapHierarchySpec,
    path: List<String?>,
    onSelect: (depth: Int) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        path.forEachIndexed { depth, id ->
            if (depth > 0) Text("›")
            val label = id?.let { spec.findNode(it)?.label } ?: "Storage"
            TextButton(onClick = { onSelect(depth) }, enabled = depth < path.size - 1) { Text(label) }
        }
    }
}

@Composable
private fun LevelRow(
    node: TreemapHierarchyNode,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .semantics { this.selected = selected }
                .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(if (node.children.isNotEmpty()) "${node.label} ›" else node.label, style = MaterialTheme.typography.bodyMedium)
        Text(formatBytes(node.layoutSize()), style = MaterialTheme.typography.bodyMedium)
    }
}
