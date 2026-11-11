package com.mena97villalobos.composeplotlib.samples.android

import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performCustomAccessibilityActionWithLabel
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mena97villalobos.composeplotlib.chart.ChartAnimationPolicy
import com.mena97villalobos.composeplotlib.chart.TreemapChart
import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.geometry.SizeD
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutInput
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner
import com.mena97villalobos.composeplotlib.core.layout.TreemapHierarchyPlan
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.selection.HierarchyNodeHit
import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchyNode
import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchySpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import com.mena97villalobos.composeplotlib.samples.android.screens.InteractiveTreemapShowcaseScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Compose UI tests for the interactive hierarchical [TreemapChart] (tap, long-press, drill-in, semantics). */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w640dp-h900dp-mdpi")
class InteractiveTreemapTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun leaf(
        id: String,
        value: Double,
    ) = TreemapHierarchyNode(id, id.uppercase(), value)

    /** Labels off, so the geometry depends only on the spec and the canvas size, not on fonts. */
    private val spec =
        TreemapHierarchySpec(
            nodes =
                listOf(
                    TreemapHierarchyNode("a", "A", children = listOf(leaf("a1", 3.0), leaf("a2", 1.0))),
                    leaf("b", 2.0),
                ),
            showLabels = false,
            nodePaddingPx = 12.0,
            tileGapPx = 6.0,
            contentPadding = InsetsD(0.0, 0.0, 0.0, 0.0),
        )

    private val chartWidth = 400.0
    private val chartHeight = 300.0

    /** The plan the composable builds for [spec] at 400x300 px (density 1). */
    private fun expectedPlan(): TreemapHierarchyPlan =
        ChartPlanner.planTreemapHierarchy(
            ChartLayoutInput(SizeD(chartWidth, chartHeight), ChartViewport(ClosedRangeD(0.0, 1.0), ClosedRangeD(0.0, 1.0))),
            spec,
        )

    private fun centerOf(
        plan: TreemapHierarchyPlan,
        id: String,
    ): Offset =
        plan.tiles
            .single { it.nodeId == id }
            .bounds.center
            .let { Offset(it.x.toFloat(), it.y.toFloat()) }

    @Composable
    private fun AtDensityOne(content: @Composable () -> Unit) {
        CompositionLocalProvider(LocalDensity provides Density(1f, 1f)) { MaterialTheme { content() } }
    }

    private fun setChart(
        spec: TreemapHierarchySpec = this.spec,
        onClick: ((HierarchyNodeHit) -> Unit)? = null,
        onLongClick: ((HierarchyNodeHit) -> Unit)? = null,
    ) {
        composeRule.setContent {
            AtDensityOne {
                Box(Modifier.size(chartWidth.dp, chartHeight.dp)) {
                    TreemapChart(
                        spec = spec,
                        modifier = Modifier.size(chartWidth.dp, chartHeight.dp).testTag("chart"),
                        onNodeClick = onClick,
                        onNodeLongClick = onLongClick,
                    )
                }
            }
        }
    }

    // --- L-003: tap and long-press ------------------------------------------------------------------------------

    @Test
    fun tapAndLongPress_reportTheTileUnderThePointer_atBothLevels() {
        val clicks = mutableListOf<HierarchyNodeHit>()
        val longClicks = mutableListOf<HierarchyNodeHit>()
        setChart(onClick = { clicks += it }, onLongClick = { longClicks += it })
        val plan = expectedPlan()
        val a = plan.tiles.single { it.nodeId == "a" }.bounds

        composeRule.onNodeWithTag("chart").performTouchInput { click(centerOf(plan, "a1")) }
        composeRule.onNodeWithTag("chart").performTouchInput { click(centerOf(plan, "b")) }
        // Inside a's padding, outside both children: the parent.
        composeRule.onNodeWithTag("chart").performTouchInput { click(Offset((a.left + 4.0).toFloat(), a.center.y.toFloat())) }
        composeRule.onNodeWithTag("chart").performTouchInput { longClick(centerOf(plan, "a2")) }
        composeRule.onNodeWithTag("chart").performTouchInput { longClick(centerOf(plan, "b")) }
        composeRule.waitForIdle()

        assertEquals(listOf("a1", "b", "a"), clicks.map { it.nodeId })
        assertEquals(listOf("a", "a1"), clicks[0].path)
        assertEquals(2, clicks[0].depth)
        assertTrue(clicks[2].hasChildren)
        assertEquals(listOf("a2", "b"), longClicks.map { it.nodeId })
    }

    @Test
    fun withoutCallbacks_theChartAddsNoPointerInput_andAnEnclosingScrollStillScrolls() {
        val scroll = androidx.compose.foundation.ScrollState(0)
        composeRule.setContent {
            AtDensityOne {
                Column(Modifier.size(400.dp, 300.dp).verticalScroll(scroll)) {
                    TreemapChart(spec = spec, modifier = Modifier.size(400.dp, 300.dp).testTag("chart"))
                    Spacer(Modifier.height(600.dp))
                }
            }
        }
        composeRule.onNodeWithTag("chart").performTouchInput { swipeUp() }
        composeRule.waitForIdle()
        assertTrue("scroll moved to ${scroll.value}", scroll.value > 0)
        // No tile is clickable without a callback.
        composeRule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.OnClick)).assertCountEquals(0)
    }

    // --- L-008: accessibility -----------------------------------------------------------------------------------

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun semanticsTree_exposesEachTileWithItsValue_nestedUnderItsParent_withActions() {
        val clicks = mutableListOf<String>()
        val longClicks = mutableListOf<String>()
        setChart(
            spec = spec.copy(valueFormatter = { "${it.id} bytes" }),
            onClick = { clicks += it.nodeId },
            onLongClick = { longClicks += it.nodeId },
        )
        composeRule.onNodeWithContentDescription("A, a bytes").assertExists()
        composeRule.onNodeWithContentDescription("B, b bytes").assertExists()
        val a1 = composeRule.onNodeWithContentDescription("A1, a1 bytes")
        a1.onParent().assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf("A, a bytes")))
        a1.assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnClick))
        a1.assert(SemanticsMatcher("has the long-press action") { node -> node.config.getOrNull(SemanticsActions.CustomActions)?.any { it.label == "More options" } == true })
        a1.assert(SemanticsMatcher("position 1 of 2") { it.config.getOrNull(SemanticsProperties.CollectionItemInfo)?.rowIndex == 0 })
        a1.performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithContentDescription("A2, a2 bytes").performCustomAccessibilityActionWithLabel("More options")
        assertEquals(listOf("a1"), clicks)
        assertEquals(listOf("a2"), longClicks)
    }

    @Test
    fun semantics_highlightedTileIsSelected() {
        composeRule.setContent {
            AtDensityOne {
                TreemapChart(spec = spec, modifier = Modifier.size(400.dp, 300.dp), highlightedNodeIds = setOf("a2", "nope"))
            }
        }
        composeRule.onNodeWithContentDescription("A2", substring = true).assertIsSelected()
    }

    @Test
    fun semantics_pastTheCap_onlyTheTopLevelIsExposed_andTheSummarySaysSo() {
        setChart(spec = spec.copy(maxAccessibleTiles = 2))
        composeRule.onAllNodesWithContentDescription("A1", substring = true).assertCountEquals(0)
        composeRule.onNodeWithContentDescription("A, 4.0").assertExists()
        composeRule.onNodeWithContentDescription("2 smaller tiles are not listed individually", substring = true).assertExists()
    }

    // --- L-004: drill-in ----------------------------------------------------------------------------------------

    @Test
    fun drillDownThreeLevelsAndBack_inTheShowcase() {
        composeRule.setContent { AtDensityOne { InteractiveTreemapShowcaseScreen(animation = ChartAnimationPolicy.Default) } }
        composeRule.onNodeWithContentDescription("DCIM,", substring = true).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithText("Video ›").assertExists()
        composeRule.onNodeWithText("Camera ›").performClick()
        composeRule.onNodeWithText("IMG_001.jpg").assertExists()
        // Three levels deep: Storage › DCIM › Camera. Back up through the breadcrumb.
        composeRule.onNodeWithText("DCIM").performClick()
        composeRule.onNodeWithText("Video ›").assertExists()
        composeRule.onNodeWithText("Storage").performClick()
        composeRule.onNodeWithText("Movies ›").assertExists()
    }

    @Test
    fun drillState_survivesAConfigurationChange() {
        val restoration = StateRestorationTester(composeRule)
        restoration.setContent { AtDensityOne { InteractiveTreemapShowcaseScreen(animation = ChartAnimationPolicy.Default) } }
        composeRule.onNodeWithContentDescription("DCIM,", substring = true).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithText("Video ›").assertExists()
        restoration.emulateSavedInstanceStateRestore()
        composeRule.onNodeWithText("Video ›").assertExists()
        composeRule.onNodeWithText("Camera ›").assertExists()
    }

    private fun setAnimatorScale(scale: Float) {
        val resolver = ApplicationProvider.getApplicationContext<android.content.Context>().contentResolver
        Settings.Global.putFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, scale)
    }

    /** Renders the chart and drills into "a"; returns the frames before, three frames after, and once settled. */
    private fun captureDrillFrames(): Triple<IntArray, IntArray, IntArray> {
        var root by mutableStateOf<String?>(null)
        composeRule.setContent {
            AtDensityOne {
                TreemapChart(
                    spec = spec,
                    modifier = Modifier.size(400.dp, 300.dp).testTag("chart"),
                    rootNodeId = root,
                    animation = ChartAnimationPolicy(viewportChangesEnabled = true, viewportDurationMillis = 400),
                )
            }
        }
        composeRule.waitForIdle()
        val before = composeRule.onNodeWithTag("chart").captureToImage().pixels()
        composeRule.mainClock.autoAdvance = false
        composeRule.runOnUiThread {
            root = "a"
            // Deliver the change to the recomposer now, so the frames below are the ones that apply it.
            Snapshot.sendApplyNotifications()
        }
        repeat(3) { composeRule.mainClock.advanceTimeByFrame() }
        composeRule.waitForIdle()
        val early = composeRule.onNodeWithTag("chart").captureToImage().pixels()
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.waitForIdle()
        val settled = composeRule.onNodeWithTag("chart").captureToImage().pixels()
        composeRule.mainClock.autoAdvance = true
        return Triple(before, early, settled)
    }

    private fun IntArray.differsFrom(other: IntArray): Boolean = indices.any { this[it] != other[it] }

    private fun androidx.compose.ui.graphics.ImageBitmap.pixels(): IntArray = IntArray(width * height).also { readPixels(it) }

    @Test
    fun drillTransition_animatesWhenMotionIsAllowed() {
        setAnimatorScale(1f)
        val (before, early, settled) = captureDrillFrames()
        assertTrue("the drill should change the picture", before.differsFrom(settled))
        assertTrue("three frames in, the new level should be on screen", early.differsFrom(before))
        assertTrue("three frames in, the zoom should still be running", early.differsFrom(settled))
    }

    @Test
    fun drillTransition_isInstantWithReducedMotion() {
        setAnimatorScale(0f)
        try {
            val (before, early, settled) = captureDrillFrames()
            assertTrue("the drill should change the picture", before.differsFrom(settled))
            assertEquals("with reduced motion the first frame is already the final one", settled.toList(), early.toList())
        } finally {
            setAnimatorScale(1f)
        }
    }
}
