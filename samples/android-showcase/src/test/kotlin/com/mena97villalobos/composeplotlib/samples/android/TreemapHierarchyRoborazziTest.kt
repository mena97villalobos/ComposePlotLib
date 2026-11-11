package com.mena97villalobos.composeplotlib.samples.android

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mena97villalobos.composeplotlib.chart.TreemapChart
import com.mena97villalobos.composeplotlib.core.spec.HierarchyHighlightStyle
import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchySpec
import com.mena97villalobos.composeplotlib.samples.android.data.sampleStorageTreemapSpec
import com.mena97villalobos.composeplotlib.samples.android.screens.InteractiveTreemapShowcaseScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot scenarios for the hierarchical [TreemapChart]: nested tiles, header bands, aggregate tiles and two-line
 * labels at several canvas sizes and font scales (labels must stay inside their tiles in every one).
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// A screen large enough for the requested sizes (Robolectric defaults to a 320dp-wide mdpi screen).
@Config(sdk = [34], qualifiers = "w640dp-h800dp-mdpi")
class TreemapHierarchyRoborazziTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun capture(
        width: Dp,
        height: Dp,
        fontScale: Float = 1f,
        dark: Boolean = false,
        rootNodeId: String? = null,
        highlighted: Set<String> = emptySet(),
        spec: () -> TreemapHierarchySpec = { sampleStorageTreemapSpec() },
    ) {
        composeRule.setContent {
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else MaterialTheme.colorScheme) {
                val base = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density = base.density, fontScale = fontScale)) {
                    Surface(modifier = Modifier.size(width = width, height = height)) {
                        TreemapChart(
                            spec = spec(),
                            modifier = Modifier.fillMaxSize(),
                            rootNodeId = rootNodeId,
                            highlightedNodeIds = highlighted,
                        )
                    }
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun treemapHierarchy_nested() = capture(400.dp, 400.dp)

    @Test
    fun treemapHierarchy_nested_compact() = capture(280.dp, 480.dp)

    @Test
    fun treemapHierarchy_nested_wide() = capture(640.dp, 280.dp)

    @Test
    fun treemapHierarchy_nested_fontScale1_4() = capture(400.dp, 400.dp, fontScale = 1.4f)

    @Test
    fun treemapHierarchy_nested_fontScale2() = capture(400.dp, 400.dp, fontScale = 2f)

    @Test
    fun treemapHierarchy_nested_dark() = capture(400.dp, 400.dp, dark = true)

    @Test
    fun treemapHierarchy_noMinimumTileSize() = capture(400.dp, 400.dp) { sampleStorageTreemapSpec(minTileSidePx = 0.0) }

    @Test
    fun treemapHierarchy_singleLevel() = capture(400.dp, 300.dp) { sampleStorageTreemapSpec().copy(maxDepth = 1) }

    @Test
    fun treemapHierarchy_highlightedLeaf() = capture(400.dp, 400.dp, highlighted = setOf("movies/concert"))

    @Test
    fun treemapHierarchy_highlightedParent() = capture(400.dp, 400.dp, highlighted = setOf("dcim"))

    @Test
    fun treemapHierarchy_highlightedDimmed() =
        capture(400.dp, 400.dp, highlighted = setOf("movies/concert")) {
            sampleStorageTreemapSpec().copy(highlight = HierarchyHighlightStyle(dimOthersAlpha = 0.35))
        }

    @Test
    fun treemapHierarchy_drilledIntoAFolder() = capture(400.dp, 400.dp, rootNodeId = "dcim")

    @Test
    fun treemapHierarchy_interactiveShowcaseScreen() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 760.dp)) {
                    InteractiveTreemapShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }
}
