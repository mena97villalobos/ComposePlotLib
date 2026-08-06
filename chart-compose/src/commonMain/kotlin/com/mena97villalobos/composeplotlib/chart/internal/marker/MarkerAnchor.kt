package com.mena97villalobos.composeplotlib.chart.internal.marker

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform

/**
 * Where a marker box should point, for the charts whose selected datum *is* the anchor.
 *
 * [ChartSelection.markerAnchor] is set by the charts whose marker belongs somewhere other than the
 * point itself — the centre of a bar rectangle, a slice centroid — and is honoured untouched. Line
 * and scatter leave it null, and [ChartSelection.markerPosition] then falls back to
 * [ChartSelection.pixelOffset]: the pixel the hit resolver recorded at the moment of the tap, which
 * nothing recomputes afterwards. An animated viewport, a resize or a rotation left the box behind
 * while the values it prints — read from the domain point every frame — stayed correct.
 *
 * [transform] must be built from the viewport currently being rendered.
 */
internal fun ChartSelection.markerAnchorFrom(transform: ChartTransform): OffsetD = markerAnchor ?: transform.toPixel(target.point)
