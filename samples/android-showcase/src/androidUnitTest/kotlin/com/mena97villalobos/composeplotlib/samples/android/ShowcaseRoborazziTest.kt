package com.mena97villalobos.composeplotlib.samples.android

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
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.mena97villalobos.composeplotlib.samples.android.screens.BarChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.CombinedChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.DomainAxesShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.LineChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.PieChartShowcaseScreen
import com.mena97villalobos.composeplotlib.samples.android.screens.ScatterChartShowcaseScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

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
        composeRule.onRoot().captureRoboImage()
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
        composeRule.onRoot().captureRoboImage()
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
        composeRule.onRoot().captureRoboImage()
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
        composeRule.onRoot().captureRoboImage()
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
        composeRule.onRoot().captureRoboImage()
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
        composeRule.onRoot().captureRoboImage()
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
        composeRule.onRoot().captureRoboImage()
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
        composeRule.onRoot().captureRoboImage()
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
        composeRule.onRoot().captureRoboImage()
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
        composeRule.onRoot().captureRoboImage()
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
        composeRule.onRoot().captureRoboImage()
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
        composeRule.onRoot().captureRoboImage()
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
        composeRule.onRoot().captureRoboImage()
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
        composeRule.onRoot().captureRoboImage()
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
        composeRule.onRoot().captureRoboImage()
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
        composeRule.onRoot().captureRoboImage()
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
        composeRule.onRoot().captureRoboImage()
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
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun showcaseApp_overview() {
        composeRule.setContent {
            Surface(modifier = Modifier.size(width = 400.dp, height = 720.dp)) {
                ShowcaseApp()
            }
        }
        composeRule.onRoot().captureRoboImage()
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
        composeRule.onRoot().captureRoboImage()
    }
}
