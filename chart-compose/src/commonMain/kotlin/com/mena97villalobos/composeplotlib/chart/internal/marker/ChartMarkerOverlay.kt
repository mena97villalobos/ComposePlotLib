package com.mena97villalobos.composeplotlib.chart.internal.marker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.mena97villalobos.composeplotlib.chart.ChartMarkerContainment
import com.mena97villalobos.composeplotlib.chart.ChartMarkerPlacement
import com.mena97villalobos.composeplotlib.core.geometry.RectD

/**
 * Positions [marker] near ([anchorX], [anchorY]) in chart pixel coordinates.
 */
@Composable
internal fun BoxScope.ChartMarkerOverlay(
    anchorX: Double,
    anchorY: Double,
    plotBounds: RectD,
    canvasBounds: RectD,
    placement: ChartMarkerPlacement,
    contentAlpha: Float = 1f,
    markerContainerModifier: Modifier = Modifier,
    marker: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val gapPx =
        remember(placement.verticalGap, density) {
            with(density) { placement.verticalGap.roundToPx() }
        }
    var markerSize by remember { mutableStateOf(IntSize.Zero) }

    val containmentRect =
        when (placement.containment) {
            ChartMarkerContainment.PlotBounds -> plotBounds
            ChartMarkerContainment.CanvasBounds -> canvasBounds
            ChartMarkerContainment.None -> plotBounds
        }

    val offsetPixels =
        remember(
            anchorX,
            anchorY,
            markerSize,
            gapPx,
            placement.containment,
            placement.preferAbove,
            containmentRect,
        ) {
            if (placement.containment == ChartMarkerContainment.None) {
                computeMarkerTopLeftLegacy(
                    anchorX = anchorX,
                    anchorY = anchorY,
                    markerWidth = markerSize.width,
                    markerHeight = markerSize.height,
                    gapPx = gapPx,
                )
            } else {
                computeContainedMarkerTopLeft(
                    anchorX = anchorX,
                    anchorY = anchorY,
                    markerWidth = markerSize.width,
                    markerHeight = markerSize.height,
                    gapPx = gapPx,
                    bounds = containmentRect,
                    preferAbove = placement.preferAbove,
                )
            }
        }

    val calloutEnabled = placement.callout.enabled
    val customCallout = placement.callout.customCallout
    val calloutPolyline =
        remember(
            calloutEnabled,
            customCallout,
            anchorX,
            anchorY,
            offsetPixels.first,
            offsetPixels.second,
            markerSize.width,
            markerSize.height,
        ) {
            if (!calloutEnabled || markerSize.width <= 0 || markerSize.height <= 0) {
                emptyList()
            } else {
                val ml = offsetPixels.first.toDouble()
                val mt = offsetPixels.second.toDouble()
                val mw = markerSize.width.toDouble()
                val mh = markerSize.height.toDouble()
                if (customCallout != null) {
                    customCallout.polyline(anchorX, anchorY, ml, mt, mw, mh)
                } else {
                    computeCalloutElbowPolyline(
                        anchorX = anchorX,
                        anchorY = anchorY,
                        markerOffsetX = offsetPixels.first,
                        markerOffsetY = offsetPixels.second,
                        markerWidth = markerSize.width,
                        markerHeight = markerSize.height,
                    )
                }
            }
        }

    val strokePx =
        remember(calloutEnabled, placement.callout.strokeWidth, density) {
            if (!calloutEnabled) {
                0f
            } else {
                with(density) { placement.callout.strokeWidth.toPx() }
            }
        }

    if (!calloutEnabled) {
        Box(
            modifier =
                Modifier
                    .graphicsLayer { alpha = contentAlpha }
                    .offset {
                        IntOffset(offsetPixels.first, offsetPixels.second)
                    }
                    .onSizeChanged { markerSize = it }
                    .then(markerContainerModifier),
        ) {
            marker()
        }
    } else {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = contentAlpha },
        ) {
            if (calloutPolyline.size >= 2 && strokePx > 0f) {
                Canvas(Modifier.fillMaxSize()) {
                    val path = Path()
                    path.moveTo(
                        calloutPolyline[0].first.toFloat(),
                        calloutPolyline[0].second.toFloat(),
                    )
                    for (i in 1 until calloutPolyline.size) {
                        path.lineTo(
                            calloutPolyline[i].first.toFloat(),
                            calloutPolyline[i].second.toFloat(),
                        )
                    }
                    drawPath(
                        path = path,
                        color = placement.callout.color,
                        style = Stroke(width = strokePx),
                    )
                }
            }
            Box(
                modifier =
                    Modifier
                        .offset {
                            IntOffset(offsetPixels.first, offsetPixels.second)
                        }
                        .onSizeChanged { markerSize = it }
                        .then(markerContainerModifier),
            ) {
                marker()
            }
        }
    }
}
