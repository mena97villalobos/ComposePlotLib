package com.mena97villalobos.composeplotlib.chart.internal.gesture

import com.mena97villalobos.composeplotlib.chart.ChartGestureConfig
import com.mena97villalobos.composeplotlib.core.viewport.ViewportAxisPolicy

internal data class EffectiveTransformFlags(
    val panX: Boolean,
    val panY: Boolean,
    val zoomX: Boolean,
    val zoomY: Boolean,
)

internal fun effectiveTransformFlags(
    config: ChartGestureConfig,
    axisPolicy: ViewportAxisPolicy,
): EffectiveTransformFlags =
    EffectiveTransformFlags(
        panX = config.transform.panXEnabled && !axisPolicy.lockXToDomain,
        panY = config.transform.panYEnabled && !axisPolicy.lockYToDomain,
        zoomX = config.transform.zoomXEnabled && !axisPolicy.lockXToDomain,
        zoomY = config.transform.zoomYEnabled && !axisPolicy.lockYToDomain,
    )
