package com.mena97villalobos.composeplotlib.benchmarks

import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutInput
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.layout.TreemapHierarchyPlan
import com.mena97villalobos.composeplotlib.core.layout.TreemapLabelMeasurer
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchyNode
import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchySpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Layout (planning) time of the hierarchical treemap: [ChartPlanner.planTreemapHierarchy] with labels and a value
 * formatter, on a phone-sized 1080 x 1400 px canvas.
 *
 * - [nodes]: total node count (folders included).
 * - [shape]: `flat` (one level) or `twoLevel` (about √n folders, the rest spread over them as files).
 * - [minTileSidePx]: `0` (no aggregation) or `126` (48 dp at 420 dpi, with aggregation).
 *
 * Sizes follow a heavy-tailed (Zipf-like) distribution, like file sizes, and are deterministic.
 * Run: `./gradlew :chart-core-benchmarks:jmh --no-configuration-cache`
 */
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
open class TreemapLayoutBenchmark {
    @Param("100", "500", "2000")
    var nodes: Int = 0

    @Param("flat", "twoLevel")
    var shape: String = ""

    @Param("0", "126")
    var minTileSidePx: Double = 0.0

    private lateinit var spec: TreemapHierarchySpec
    private lateinit var input: ChartLayoutInput
    private val measurer = TreemapLabelMeasurer.estimate(charWidthPx = 18.0, lineHeightPx = 42.0)

    @Setup
    fun setup() {
        val tree = if (shape == "flat") flatTree(nodes) else twoLevelTree(nodes)
        spec =
            TreemapHierarchySpec(
                nodes = tree,
                maxDepth = 2,
                minTileSidePx = minTileSidePx,
                valueFormatter = { node -> "${(node.value / 1_000_000.0).toLong()} MB" },
            )
        input =
            ChartLayoutInput(
                canvasSize = SizeD(1080.0, 1400.0),
                viewport = ChartViewport(ClosedRangeD(0.0, 1.0), ClosedRangeD(0.0, 1.0)),
            )
    }

    @Benchmark
    fun planTreemapHierarchy(): TreemapHierarchyPlan = ChartPlanner.planTreemapHierarchy(input, spec, labelMeasurer = measurer)

    companion object {
        /** Zipf-like size for rank [rank]: a few large items and a long tail. */
        fun sizeOf(rank: Int): Double = 5e9 / (rank + 1.0).pow(1.1)

        fun flatTree(count: Int): List<TreemapHierarchyNode> = List(count) { i -> TreemapHierarchyNode("n$i", "file_$i.bin", sizeOf(i)) }

        fun twoLevelTree(count: Int): List<TreemapHierarchyNode> {
            val folders = ceil(sqrt(count.toDouble())).toInt().coerceAtLeast(1)
            val files = (count - folders).coerceAtLeast(0)
            val children = List(folders) { ArrayList<TreemapHierarchyNode>() }
            for (i in 0 until files) {
                children[i % folders] += TreemapHierarchyNode("f$i", "file_$i.bin", sizeOf(i))
            }
            return List(folders) { f -> TreemapHierarchyNode("d$f", "Folder $f", children = children[f]) }
        }
    }
}
