package com.mena97villalobos.composeplotlib.core.spec

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SankeyChartSpecValidationTest {
    private val nodes = listOf(SankeyNode("A", column = 0), SankeyNode("B", column = 1))

    @Test
    fun nonFiniteLinkValue_isRejected() {
        assertFailsWith<IllegalArgumentException> {
            SankeyChartSpec(id = "s", nodes = nodes, links = listOf(SankeyLink("A", "B", Double.NaN)))
        }
        assertFailsWith<IllegalArgumentException> {
            SankeyChartSpec(id = "s", nodes = nodes, links = listOf(SankeyLink("A", "B", Double.POSITIVE_INFINITY)))
        }
    }

    @Test
    fun duplicateNodeIds_areRejected() {
        assertFailsWith<IllegalArgumentException> {
            SankeyChartSpec(
                id = "s",
                nodes = listOf(SankeyNode("A", column = 0), SankeyNode("A", column = 1)),
                links = emptyList(),
            )
        }
    }

    @Test
    fun linkReferencingUnknownNode_isRejected() {
        assertFailsWith<IllegalArgumentException> {
            SankeyChartSpec(id = "s", nodes = nodes, links = listOf(SankeyLink("A", "missing", 1.0)))
        }
        assertFailsWith<IllegalArgumentException> {
            SankeyChartSpec(id = "s", nodes = nodes, links = listOf(SankeyLink("missing", "B", 1.0)))
        }
    }

    @Test
    fun negativeColumns_areAllowed() {
        // Normalized by the planner; not a spec error.
        SankeyChartSpec(
            id = "s",
            nodes = listOf(SankeyNode("A", column = -2), SankeyNode("B", column = 0)),
            links = listOf(SankeyLink("A", "B", 1.0)),
        )
    }

    @Test
    fun legacyAlignment_mapsToCrossAxisAlignment() {
        assertEquals(
            SankeyColumnAlignment.Start,
            SankeyLayoutSpec(columnVerticalAlignment = SankeyColumnVerticalAlignment.Top).resolvedColumnAlignment(),
        )
        assertEquals(
            SankeyColumnAlignment.End,
            SankeyLayoutSpec(columnVerticalAlignment = SankeyColumnVerticalAlignment.Bottom).resolvedColumnAlignment(),
        )
        assertEquals(
            SankeyColumnAlignment.Justify,
            SankeyLayoutSpec(columnVerticalAlignment = SankeyColumnVerticalAlignment.Justify).resolvedColumnAlignment(),
        )
        // Explicit neutral alignment wins over the legacy knob.
        assertEquals(
            SankeyColumnAlignment.Center,
            SankeyLayoutSpec(
                columnVerticalAlignment = SankeyColumnVerticalAlignment.Top,
                columnAlignment = SankeyColumnAlignment.Center,
            ).resolvedColumnAlignment(),
        )
    }

    @Test
    fun chartValueFormatter_rendersWholeAndFractionalValues() {
        assertEquals("9", formatChartValue(9.0))
        assertEquals("9.3", formatChartValue(9.25))
        assertEquals("—", formatChartValue(Double.NaN))
        assertEquals("0", formatChartValue(0.0))
        assertEquals("-2.5", formatChartValue(-2.5))
        assertEquals("-0.2", formatChartValue(-0.2))
    }

    @Test
    fun chartValueFormatter_largeValues_neitherSaturateNorUseScientificNotation() {
        // Int-based rounding used to saturate at 2147483647 for values past ~2.1e8.
        assertEquals("300000000", formatChartValue(3.0e8))
        assertEquals("2500000000", formatChartValue(2.5e9))
        assertEquals("-300000000", formatChartValue(-3.0e8))
        // Double.toString would render fractional values >= 1e7 as scientific notation.
        assertEquals("12345678.9", formatChartValue(12345678.9))
    }
}
