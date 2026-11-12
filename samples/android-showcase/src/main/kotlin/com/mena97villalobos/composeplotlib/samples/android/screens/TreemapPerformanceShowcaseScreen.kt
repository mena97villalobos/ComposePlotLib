package com.mena97villalobos.composeplotlib.samples.android.screens

import android.os.SystemClock
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.unit.dp
import com.mena97villalobos.composeplotlib.chart.TreemapChart
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutInput
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.layout.TreemapLabelMeasurer
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchyNode
import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchySpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import com.mena97villalobos.composeplotlib.samples.android.data.formatBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt

private const val PERF_TAG = "TreemapPerf"

/** 500 nodes in two levels (about √500 folders), sizes following a Zipf-like long tail. */
private fun perfTree(count: Int = 500): List<TreemapHierarchyNode> {
    val folders = ceil(sqrt(count.toDouble())).toInt()
    val children = List(folders) { ArrayList<TreemapHierarchyNode>() }
    for (i in 0 until count - folders) {
        children[i % folders] += TreemapHierarchyNode("f$i", "file_$i.bin", 5e9 / (i + 1.0).pow(1.1))
    }
    return List(folders) { f -> TreemapHierarchyNode("d$f", "Folder $f", children = children[f]) }
}

/**
 * Measures, on the device, a 500-node, two-level treemap: the time from composing the chart to its first drawn frame
 * (composition, planning with real Compose text measurement, layout and draw), once cold (first chart after the
 * activity starts) and then [WARM_RUNS] times warm (the chart recreated with a new spec instance, so the plan and the
 * label-measurement cache start empty), plus the planner alone (median of 21 runs, estimated text). Results are shown
 * and logged under the `TreemapPerf` tag; open directly with
 * `adb shell am start -n <package>/.MainActivity --es destination TreemapPerformance`.
 */
@Composable
fun TreemapPerformanceShowcaseScreen() {
    val tree = remember { perfTree() }
    var generation by remember { mutableStateOf(0) }
    val drawTimes = remember { ArrayList<Double>() }
    var summary by remember { mutableStateOf("Measuring…") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(summary, style = MaterialTheme.typography.bodyMedium)
        key(generation) {
            val composedAt = remember { SystemClock.elapsedRealtimeNanos() }
            val spec = remember { TreemapHierarchySpec(nodes = tree, valueFormatter = { formatBytes(it.value) }) }
            // The chart's first draw pass happens before its size is known and draws nothing; the tiles appear in
            // the second pass, after onSizeChanged lets it plan. Time that one.
            var drawPasses by remember { mutableStateOf(0) }
            val drawn = drawPasses >= 2
            TreemapChart(
                spec = spec,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                        .drawWithContent {
                            drawContent()
                            if (drawPasses < 2) {
                                drawPasses += 1
                                val elapsed = (SystemClock.elapsedRealtimeNanos() - composedAt) / 1e6
                                Log.d(PERF_TAG, "generation=$generation drawPass=$drawPasses elapsedMs=$elapsed")
                                if (drawPasses == 2) drawTimes += elapsed
                            }
                        },
            )
            LaunchedEffect(drawn) {
                if (!drawn) return@LaunchedEffect
                if (generation < WARM_RUNS) {
                    generation += 1
                    return@LaunchedEffect
                }
                val plannerMedian = withContext(Dispatchers.Default) { plannerMedianMs(spec) }
                val cold = drawTimes.first()
                val warm = drawTimes.drop(1).sorted()
                val warmMedian = warm[warm.size / 2]
                summary =
                    "500 tiles. Compose to first draw: cold %.1f ms, warm median %.1f ms (max %.1f). Planner alone: %.2f ms."
                        .format(cold, warmMedian, warm.last(), plannerMedian)
                Log.i(PERF_TAG, "tiles=500 coldFirstDrawMs=$cold warmFirstDrawMedianMs=$warmMedian warmMaxMs=${warm.last()} plannerMedianMs=$plannerMedian")
            }
        }
    }
}

private const val WARM_RUNS = 10

private fun plannerMedianMs(spec: TreemapHierarchySpec): Double {
    val input = ChartLayoutInput(SizeD(1080.0, 1400.0), ChartViewport(ClosedRangeD(0.0, 1.0), ClosedRangeD(0.0, 1.0)))
    val measurer = TreemapLabelMeasurer.estimate(charWidthPx = 18.0, lineHeightPx = 42.0)
    val runs =
        List(21) {
            val start = System.nanoTime()
            ChartPlanner.planTreemapHierarchy(input, spec, labelMeasurer = measurer)
            (System.nanoTime() - start) / 1e6
        }
    return runs.sorted()[runs.size / 2]
}
