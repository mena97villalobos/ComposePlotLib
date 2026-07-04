package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.spec.SunburstChartSpec
import com.mena97villalobos.composeplotlib.core.spec.SunburstNode
import com.mena97villalobos.composeplotlib.core.spec.totalValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SunburstLayoutTest {
    private val root =
        SunburstNode(
            label = "root",
            children =
                listOf(
                    SunburstNode(
                        "A",
                        children = listOf(SunburstNode("A1", value = 3.0), SunburstNode("A2", value = 1.0)),
                    ),
                    SunburstNode("B", value = 4.0),
                ),
        )

    @Test
    fun internalNodeValueIsSumOfChildren() {
        assertEquals(4.0, root.children[0].totalValue(), 1e-9) // A = A1 + A2
        assertEquals(8.0, root.totalValue(), 1e-9)
    }

    @Test
    fun childSweepsAreProportionalAndTileTheParent() {
        val slices = sunburstLayout(SunburstChartSpec(root = root, startAngleDegrees = 0.0, sweepDegrees = 360.0))
        val rootSlice = slices.first { it.depth == 0 }
        assertEquals(360.0, rootSlice.sweepAngleDegrees, 1e-9)
        // A and B split the circle 4:4 → 180 each.
        val a = slices.first { it.label == "A" }
        val b = slices.first { it.label == "B" }
        assertEquals(180.0, a.sweepAngleDegrees, 1e-9)
        assertEquals(180.0, b.sweepAngleDegrees, 1e-9)
        // A1 and A2 split A's 180° as 3:1 → 135 and 45.
        assertEquals(135.0, slices.first { it.label == "A1" }.sweepAngleDegrees, 1e-9)
        assertEquals(45.0, slices.first { it.label == "A2" }.sweepAngleDegrees, 1e-9)
        // Depths: root 0, A/B 1, A1/A2 2.
        assertEquals(1, a.depth)
        assertEquals(2, slices.first { it.label == "A1" }.depth)
    }

    @Test
    fun hidingRootOmitsDepthZero() {
        val slices = sunburstLayout(SunburstChartSpec(root = root, showRoot = false))
        assertTrue(slices.none { it.depth == 0 })
        assertTrue(slices.any { it.label == "A" })
    }
}
