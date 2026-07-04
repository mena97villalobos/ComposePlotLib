package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.RectD
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SquarifiedTreemapTest {
    private val bounds = RectD(0.0, 0.0, 600.0, 400.0)

    private fun RectD.area() = width * height

    @Test
    fun rectAreasAreProportionalToValues() {
        val values = listOf(6.0, 6.0, 4.0, 3.0, 2.0, 1.0)
        val rects = squarifiedTreemap(values, bounds)
        val total = values.sum()
        val totalArea = bounds.area()
        values.indices.forEach { i ->
            val expected = values[i] / total * totalArea
            assertEquals(expected, rects[i].area(), totalArea * 1e-6)
        }
    }

    @Test
    fun rectsStayWithinBounds() {
        val rects = squarifiedTreemap(listOf(5.0, 3.0, 2.0, 8.0, 1.0), bounds)
        rects.forEach { r ->
            assertTrue(r.left >= bounds.left - 1e-6 && r.right <= bounds.right + 1e-6)
            assertTrue(r.top >= bounds.top - 1e-6 && r.bottom <= bounds.bottom + 1e-6)
        }
    }

    @Test
    fun rectsTileTheBoundsWithoutOverlapArea() {
        val values = listOf(4.0, 3.0, 2.0, 1.0)
        val rects = squarifiedTreemap(values, bounds)
        // Total covered area equals the bounds area (a full tiling).
        assertEquals(bounds.area(), rects.sumOf { it.area() }, bounds.area() * 1e-6)
    }

    @Test
    fun nonPositiveAndDegenerateCollapse() {
        val rects = squarifiedTreemap(listOf(0.0, -1.0), bounds)
        rects.forEach { assertEquals(0.0, it.area(), 1e-9) }
        val degenerate = squarifiedTreemap(listOf(1.0, 2.0), RectD(0.0, 0.0, 0.0, 0.0))
        degenerate.forEach { assertEquals(0.0, it.area(), 1e-9) }
    }
}
