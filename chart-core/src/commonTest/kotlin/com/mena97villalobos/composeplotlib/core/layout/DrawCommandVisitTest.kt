package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import kotlin.test.Test
import kotlin.test.assertEquals

class DrawCommandVisitTest {
    @Test
    fun visitDepthFirstWalksGroupAndClipChildren() {
        val inner = DrawCommand.Circle(OffsetD(1.0, 2.0), 3.0, ChartColor.Red)
        val group = DrawCommand.Group(alpha = 1.0, children = listOf(inner))
        val clip = DrawCommand.ClipRect(bounds = RectD(0.0, 0.0, 10.0, 10.0), children = listOf(group))
        val order = mutableListOf<String>()
        clip.visitDepthFirst {
            order +=
                when (it) {
                    is DrawCommand.ClipRect -> "clip"
                    is DrawCommand.Group -> "group"
                    is DrawCommand.Circle -> "circle"
                    else -> "other"
                }
        }
        assertEquals(listOf("clip", "group", "circle"), order)
    }

    @Test
    fun iterableVisitVisitsEachRoot() {
        val a = DrawCommand.Line(OffsetD(0.0, 0.0), OffsetD(1.0, 1.0), ChartColor.Blue)
        val b = DrawCommand.Rect(RectD(0.0, 0.0, 1.0, 1.0), ChartColor.Green)
        var count = 0
        listOf(a, b).visitDepthFirst { count++ }
        assertEquals(2, count)
    }
}
