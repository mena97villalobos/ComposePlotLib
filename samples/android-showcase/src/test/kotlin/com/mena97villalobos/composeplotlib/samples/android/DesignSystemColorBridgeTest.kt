package com.mena97villalobos.composeplotlib.samples.android

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mena97villalobos.composeplotlib.chart.TreemapChart
import com.mena97villalobos.composeplotlib.chart.theme.ChartChromeTheme
import com.mena97villalobos.composeplotlib.chart.theme.toChartColor
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.contrastRatio
import com.mena97villalobos.composeplotlib.core.spec.AxisLabelStyle
import com.mena97villalobos.composeplotlib.core.spec.GridLineStyle
import com.mena97villalobos.composeplotlib.core.spec.GridSpec
import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchySpec
import com.mena97villalobos.composeplotlib.samples.android.data.sampleStorageTreemapSpec
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// --- The adapter from docs/style-and-theme.md, "Theming from a non-Material design system" (same code). ---

/** Your design system's roles, named for what they mean (illustrative; use your scheme's real properties). */
class BrandColors(
    val background: Color,
    val accents: List<Color>,
    val divider: Color,
    val mutedText: Color,
    val card: Color,
)

/** One function from the design-system scheme to everything a chart needs. */
class BrandChartColors(
    colors: BrandColors,
) {
    val series: List<ChartColor> = colors.accents.map { it.toChartColor() }
    val chrome =
        ChartChromeTheme(
            plotBorderColor = colors.divider,
            plotBorderStrokeWidthPx = 1f,
            legendBackgroundColor = colors.card,
            legendBorderColor = colors.divider,
            legendBorderStrokeWidthPx = 1f,
            legendCornerRadiusPx = 6f,
            clipCanvasToCanvasBounds = true,
            clipLegendToLegendBounds = true,
        )
    val axisLabels = AxisLabelStyle(color = colors.mutedText.toChartColor())
    val grid = GridSpec(lineStyle = GridLineStyle(color = colors.divider.copy(alpha = 0.5f).toChartColor()))
    val legendText = colors.mutedText.toChartColor()
}

@Composable
fun StorageTreemap(
    spec: TreemapHierarchySpec,
    colors: BrandColors,
) {
    val chart = remember(colors) { BrandChartColors(colors) }
    TreemapChart(spec = spec.copy(palette = chart.series), chromeTheme = chart.chrome)
}

// --- End of the documented adapter. ---

/** A made-up brand scheme in light and dark, standing in for a real design system. */
private val LightBrand =
    BrandColors(
        background = Color(0xFFFAF7F0),
        accents = listOf(Color(0xFF1F6F5C), Color(0xFFB0652B), Color(0xFF4B5BA6), Color(0xFF9C3D54)),
        divider = Color(0xFFD9D2C3),
        mutedText = Color(0xFF5E5A52),
        card = Color(0xFFFFFFFF),
    )
private val DarkBrand =
    BrandColors(
        background = Color(0xFF15171A),
        accents = listOf(Color(0xFF6CC4A9), Color(0xFFE8A266), Color(0xFF93A3EA), Color(0xFFE38AA0)),
        divider = Color(0xFF3A3D42),
        mutedText = Color(0xFFB3B0A8),
        card = Color(0xFF1E2125),
    )

/** Checks that the documented adapter compiles, themes a chart, and that its guidance holds for both modes. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w640dp-h900dp-mdpi")
class DesignSystemColorBridgeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun documentedMapping_meetsTheContrastGuidance_inLightAndDark() {
        for (brand in listOf(LightBrand, DarkBrand)) {
            val background = brand.background.toChartColor()
            val chart = BrandChartColors(brand)
            assertTrue("muted text must reach 4.5:1", chart.legendText.contrastRatio(background) >= 4.5)
            assertTrue("axis labels must reach 4.5:1", chart.axisLabels.color.contrastRatio(background) >= 4.5)
            chart.series.forEach { assertTrue("series colors must reach 3:1", it.contrastRatio(background) >= 3.0) }
        }
    }

    @Test
    fun documentedAdapter_themesATreemap_inDark() {
        composeRule.setContent {
            Surface(modifier = Modifier.size(400.dp, 320.dp), color = DarkBrand.background) {
                StorageTreemap(spec = sampleStorageTreemapSpec().copy(maxDepth = 1), colors = DarkBrand)
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }
}
