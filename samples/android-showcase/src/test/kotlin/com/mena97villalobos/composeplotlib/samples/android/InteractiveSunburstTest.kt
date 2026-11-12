package com.mena97villalobos.composeplotlib.samples.android

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mena97villalobos.composeplotlib.chart.SunburstChart
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutInput
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.layout.SunburstHierarchyPlan
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.selection.HierarchyNodeHit
import com.mena97villalobos.composeplotlib.core.spec.HierarchyHighlightStyle
import com.mena97villalobos.composeplotlib.core.spec.SunburstChartSpec
import com.mena97villalobos.composeplotlib.core.spec.SunburstNode
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Compose UI and screenshot tests for [SunburstChart]'s node callbacks, drill-in and highlight. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w640dp-h900dp-mdpi")
class InteractiveSunburstTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val spec =
        SunburstChartSpec(
            root =
                SunburstNode(
                    label = "Storage",
                    id = "root",
                    children =
                        listOf(
                            SunburstNode(
                                "Pictures",
                                id = "pictures",
                                children = listOf(SunburstNode("Camera", 6.0, id = "camera"), SunburstNode("Screenshots", 2.0, id = "shots")),
                            ),
                            SunburstNode("Music", 3.0, id = "music"),
                            SunburstNode("Documents", children = listOf(SunburstNode("Scans", 1.5, id = "scans")), id = "docs"),
                        ),
                ),
        )

    private fun middleOf(
        plan: SunburstHierarchyPlan,
        id: String,
    ): Offset {
        val s = plan.segments.single { it.nodeId == id }
        val r = if (s.depth == 0) s.outerRadius / 2.0 else (s.innerRadius + s.outerRadius) / 2.0
        val a = (s.startAngleDegrees + s.sweepAngleDegrees / 2.0) * PI / 180.0
        return Offset((plan.center.x + r * cos(a)).toFloat(), (plan.center.y + r * sin(a)).toFloat())
    }

    @Test
    fun tapAndLongPress_reportTheSegment_atEveryRing() {
        val clicks = mutableListOf<HierarchyNodeHit>()
        val longClicks = mutableListOf<HierarchyNodeHit>()
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1f)) {
                MaterialTheme {
                    SunburstChart(
                        spec = spec,
                        modifier = Modifier.size(400.dp, 400.dp).testTag("sunburst"),
                        onNodeClick = { clicks += it },
                        onNodeLongClick = { longClicks += it },
                    )
                }
            }
        }
        val plan =
            ChartPlanner.planSunburstHierarchy(
                ChartLayoutInput(SizeD(400.0, 400.0), ChartViewport(ClosedRangeD(0.0, 1.0), ClosedRangeD(0.0, 1.0))),
                spec,
            )
        for (id in listOf("root", "pictures", "music", "camera", "shots", "scans")) {
            composeRule.onNodeWithTag("sunburst").performTouchInput { click(middleOf(plan, id)) }
        }
        composeRule.onNodeWithTag("sunburst").performTouchInput { longClick(middleOf(plan, "docs")) }
        composeRule.waitForIdle()
        assertEquals(listOf("root", "pictures", "music", "camera", "shots", "scans"), clicks.map { it.nodeId })
        assertEquals(listOf("root", "pictures", "camera"), clicks[3].path)
        assertEquals(listOf(0, 1, 1, 2, 2, 2), clicks.map { it.depth })
        assertEquals(listOf("docs"), longClicks.map { it.nodeId })
    }

    private fun capture(
        rootNodeId: String? = null,
        highlighted: Set<String> = emptySet(),
        style: HierarchyHighlightStyle = HierarchyHighlightStyle(),
    ) {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 360.dp, height = 360.dp)) {
                    SunburstChart(
                        spec = spec,
                        modifier = Modifier.fillMaxSize(),
                        rootNodeId = rootNodeId,
                        highlightedNodeIds = highlighted,
                        highlight = style,
                    )
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun sunburst_highlightedSegment() = capture(highlighted = setOf("camera"), style = HierarchyHighlightStyle(dimOthersAlpha = 0.4))

    @Test
    fun sunburst_drilledIntoABranch() = capture(rootNodeId = "pictures")
}
