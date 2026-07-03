package com.mena97villalobos.composeplotlib.core.stream

import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StreamingSeriesBufferTest {
    private fun pts(vararg xs: Double) = xs.map { ChartPoint(it, it * 2.0) }

    @Test
    fun appendGrowsUntilCapacity() {
        val buffer = StreamingSeriesBuffer(capacity = 3)
        assertTrue(buffer.isEmpty)
        buffer.append(ChartPoint(0.0, 0.0))
        buffer.append(ChartPoint(1.0, 2.0))
        assertEquals(2, buffer.size)
        assertEquals(listOf(0.0, 1.0), buffer.snapshot().map { it.x })
    }

    @Test
    fun appendPastCapacityDropsOldest() {
        val buffer = StreamingSeriesBuffer(capacity = 3)
        buffer.appendAll(pts(0.0, 1.0, 2.0, 3.0, 4.0))
        assertEquals(3, buffer.size)
        assertEquals(listOf(2.0, 3.0, 4.0), buffer.snapshot().map { it.x })
        assertEquals(4.0, buffer.latest()?.x)
    }

    @Test
    fun initialSeedKeepsLastCapacity() {
        val buffer = StreamingSeriesBuffer(capacity = 2, initial = pts(0.0, 1.0, 2.0))
        assertEquals(listOf(1.0, 2.0), buffer.snapshot().map { it.x })
    }

    @Test
    fun snapshotIsIndependentCopy() {
        val buffer = StreamingSeriesBuffer(capacity = 5)
        buffer.appendAll(pts(0.0, 1.0))
        val snap = buffer.snapshot()
        buffer.append(ChartPoint(2.0, 4.0))
        assertEquals(2, snap.size) // earlier snapshot unaffected
        assertEquals(3, buffer.size)
    }

    @Test
    fun clearEmptiesBuffer() {
        val buffer = StreamingSeriesBuffer(capacity = 5, initial = pts(0.0, 1.0))
        buffer.clear()
        assertTrue(buffer.isEmpty)
        assertNull(buffer.latest())
    }

    @Test
    fun nonPositiveCapacityRejected() {
        assertFailsWith<IllegalArgumentException> { StreamingSeriesBuffer(capacity = 0) }
    }
}
