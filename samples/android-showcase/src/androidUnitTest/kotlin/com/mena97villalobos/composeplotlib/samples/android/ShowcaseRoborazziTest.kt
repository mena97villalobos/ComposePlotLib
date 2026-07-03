package com.mena97villalobos.composeplotlib.samples.android

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dropbox.differ.SimpleImageComparator
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.mena97villalobos.composeplotlib.samples.android.screens.BarChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.BoxPlotChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.BubbleChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.BulletChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.CandlestickChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.CombinedChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.DomainAxesShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.FunnelChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.GaugeChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.HeatmapChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.LineChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.PieChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.RadarChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.SankeyChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.ScatterChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.StackedAreaChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.WaterfallChartShowcaseScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Default Roborazzi verification requires 0% differing pixels. Goldens are often recorded on macOS
 * while CI runs on Linux; Robolectric font/renderer output differs enough to fail that check. This
 * comparator allows small cross-host drift while still catching meaningful visual changes.
 */
private val showcaseRoborazziOptions =
    RoborazziOptions(
        compareOptions =
            RoborazziOptions.CompareOptions(
                changeThreshold = 0.02f,
                imageComparator = SimpleImageComparator(maxDistance = 0.04f),
            ),
    )

private fun SemanticsNodeInteraction.captureShowcaseRoboImage() {
    captureRoboImage(roborazziOptions = showcaseRoborazziOptions)
}

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class ShowcaseRoborazziTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun lineChartShowcaseScreen() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    LineChartShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun lineChartShowcaseScreen_darkMaterialTheme() {
        composeRule.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    LineChartShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun lineChartShowcaseScreen_compactWidth() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 320.dp, height = 720.dp)) {
                    LineChartShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun lineChartShowcaseScreen_elevatedFontScale() {
        composeRule.setContent {
            MaterialTheme {
                val base = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(density = base.density, fontScale = 1.4f),
                ) {
                    Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                        LineChartShowcaseScreen()
                    }
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun lineChart_roborazzi_emptyState_defaultOverlay() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    RoborazziLineChartEmptyDefaultOverlay()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun lineChart_roborazzi_noDataUi_preset() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    RoborazziLineChartNoDataUiPreset()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun lineChart_roborazzi_selectionHighlighted() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    RoborazziLineChartSelectionHighlighted()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun barChart_roborazzi_emptyState_defaultOverlay() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    RoborazziBarChartEmptyDefaultOverlay()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun barChart_roborazzi_selectionHighlighted() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    RoborazziBarChartSelectionHighlighted()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun barChart_roborazzi_selectionHighlighted_elevatedFontScale() {
        composeRule.setContent {
            MaterialTheme {
                val base = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(density = base.density, fontScale = 1.4f),
                ) {
                    Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                        RoborazziBarChartSelectionHighlighted()
                    }
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun scatterChart_roborazzi_emptyState_defaultOverlay() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    RoborazziScatterChartEmptyDefaultOverlay()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun scatterChart_roborazzi_selectionHighlighted() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    RoborazziScatterChartSelectionHighlighted()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun pieChart_roborazzi_emptyState_defaultOverlay() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    RoborazziPieChartEmptyDefaultOverlay()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun pieChart_roborazzi_selectionHighlighted() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    RoborazziPieChartSelectionHighlighted()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun barChartShowcaseScreen() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    BarChartShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun combinedChartShowcaseScreen() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    CombinedChartShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun scatterChartShowcaseScreen() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    ScatterChartShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun pieChartShowcaseScreen() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    PieChartShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun radarChartShowcaseScreen() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    RadarChartShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun sankeyChartShowcaseScreen() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    SankeyChartShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun showcaseApp_overview() {
        composeRule.setContent {
            Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                ShowcaseApp()
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun heatmapChartShowcaseScreen() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    HeatmapChartShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun gaugeChartShowcaseScreen() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    GaugeChartShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun candlestickChartShowcaseScreen() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    CandlestickChartShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun boxPlotChartShowcaseScreen() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    BoxPlotChartShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun stackedAreaChartShowcaseScreen() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    StackedAreaChartShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun funnelChartShowcaseScreen() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    FunnelChartShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun bulletChartShowcaseScreen() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    BulletChartShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun bubbleChartShowcaseScreen() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    BubbleChartShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun waterfallChartShowcaseScreen() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    WaterfallChartShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }

    @Test
    fun domainAxesShowcaseScreen() {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                    DomainAxesShowcaseScreen()
                }
            }
        }
        composeRule.onRoot().captureShowcaseRoboImage()
    }
}
