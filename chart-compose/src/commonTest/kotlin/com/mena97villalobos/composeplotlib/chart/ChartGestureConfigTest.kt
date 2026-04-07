package com.mena97villalobos.composeplotlib.chart

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChartGestureConfigTest {
    @Test
    fun cartesianDefault_matchesParameterlessConstructor() {
        assertTrue(ChartGestureConfig.CartesianDefault == ChartGestureConfig())
    }

    @Test
    fun pieDefault_disablesTransformAndFling() {
        val c = ChartGestureConfig.PieDefault
        assertTrue(c.tap.selectionEnabled)
        assertFalse(c.transform.enabled)
        assertFalse(c.fling.panFlingEnabled)
    }

    @Test
    fun readOnly_disablesAllInteraction() {
        val c = ChartGestureConfig.readOnly()
        assertFalse(c.tap.selectionEnabled)
        assertFalse(c.transform.enabled)
        assertFalse(c.fling.panFlingEnabled)
    }

    @Test
    fun exploreOnly_keepsTransformWithoutTap() {
        val c = ChartGestureConfig.exploreOnly()
        assertFalse(c.tap.selectionEnabled)
        assertTrue(c.transform.enabled)
    }

    @Test
    fun cartesianInteractive_enablesDoubleTapZoomInByDefault() {
        val c = ChartGestureConfig.cartesianInteractive()
        assertTrue(c.tap.selectionEnabled)
        assertTrue(c.transform.enabled)
        assertTrue(c.transform.doubleTapZoomInEnabled)
        assertFalse(c.transform.doubleTapZoomOutEnabled)
        assertFalse(c.transform.doubleTapResetViewportEnabled)
        assertFalse(c.fling.panFlingEnabled)
    }

    @Test
    fun cartesianInteractive_respectsFlingAndDoubleTapFlags() {
        val c =
            ChartGestureConfig.cartesianInteractive(
                doubleTapZoomInEnabled = false,
                doubleTapZoomOutEnabled = true,
                doubleTapResetViewportEnabled = true,
                panFlingEnabled = true,
            )
        assertFalse(c.transform.doubleTapZoomInEnabled)
        assertTrue(c.transform.doubleTapZoomOutEnabled)
        assertTrue(c.transform.doubleTapResetViewportEnabled)
        assertTrue(c.fling.panFlingEnabled)
    }

    @Test
    fun cartesianInteractiveFullExploration_matchesExplicitCartesianInteractive() {
        val full = ChartGestureConfig.cartesianInteractiveFullExploration()
        val explicit =
            ChartGestureConfig.cartesianInteractive(
                doubleTapZoomInEnabled = true,
                doubleTapZoomOutEnabled = true,
                doubleTapResetViewportEnabled = true,
                panFlingEnabled = true,
            )
        assertTrue(full == explicit)
        assertTrue(full.transform.doubleTapZoomInEnabled)
        assertTrue(full.transform.doubleTapZoomOutEnabled)
        assertTrue(full.transform.doubleTapResetViewportEnabled)
        assertTrue(full.fling.panFlingEnabled)
    }
}
