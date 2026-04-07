package com.mena97villalobos.composeplotlib.chart.internal.gesture

import androidx.compose.ui.geometry.Offset
import com.mena97villalobos.composeplotlib.chart.ChartGestureConfig
import com.mena97villalobos.composeplotlib.chart.state.ChartViewportState
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.layout.ChartLayoutPlan
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport

internal fun cartesianDoubleTapViewportEnabled(config: ChartGestureConfig): Boolean {
    val t = config.transform
    return t.enabled &&
        (t.doubleTapResetViewportEnabled || t.doubleTapZoomOutEnabled || t.doubleTapZoomInEnabled)
}

internal fun handleCartesianChartDoubleTapViewport(
    offset: Offset,
    gestureConfig: ChartGestureConfig,
    transformFlags: EffectiveTransformFlags,
    viewportState: ChartViewportState,
    renderViewport: ChartViewport,
    layoutPlan: ChartLayoutPlan?,
) {
    val t = gestureConfig.transform
    if (!t.enabled || layoutPlan == null) return
    val centroid = OffsetD(offset.x.toDouble(), offset.y.toDouble())
    if (!layoutPlan.plotBounds.contains(centroid)) return

    when {
        t.doubleTapResetViewportEnabled -> viewportState.reset()
        t.doubleTapZoomOutEnabled -> {
            val transform = ChartTransform.forPlotBounds(renderViewport, layoutPlan.plotBounds)
            val pivot = transform.toDomain(centroid)
            val z = t.doubleTapZoomOutScale.toDouble().coerceIn(0.02, 0.95)
            val zx = if (transformFlags.zoomX) z else 1.0
            val zy = if (transformFlags.zoomY) z else 1.0
            viewportState.zoom(zx, zy, pivot.x, pivot.y)
        }
        t.doubleTapZoomInEnabled -> {
            val transform = ChartTransform.forPlotBounds(renderViewport, layoutPlan.plotBounds)
            val pivot = transform.toDomain(centroid)
            val z = t.doubleTapZoomScale.toDouble().coerceIn(1.05, 50.0)
            val zx = if (transformFlags.zoomX) z else 1.0
            val zy = if (transformFlags.zoomY) z else 1.0
            viewportState.zoom(zx, zy, pivot.x, pivot.y)
        }
    }
}
