package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.spec.PieSliceLabelsSpec
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Leader polyline points and top-left anchor for a slice callout label. */
data class PieSliceCalloutLayout(
    val leaderPoints: List<OffsetD>,
    val textTopLeft: OffsetD,
)

/**
 * Places a horizontal elbow leader from the outer rim to a label box, keeping the label inside [plotBounds].
 */
fun layoutPieSliceCallout(
    plotBounds: RectD,
    center: OffsetD,
    outerRadius: Double,
    midAngleDegrees: Double,
    textWidth: Double,
    textHeight: Double,
    spec: PieSliceLabelsSpec,
): PieSliceCalloutLayout {
    val w = max(textWidth, 1.0)
    val h = max(textHeight, 1.0)
    val margin = spec.edgePaddingPx.coerceAtLeast(0.0)
    val stub = spec.leaderStubPx.coerceAtLeast(0.0)
    val elbowH = spec.horizontalElbowPx.coerceAtLeast(0.0)
    val pad = spec.labelGapPx.coerceAtLeast(0.0)

    val r = midAngleDegrees * PI / 180.0
    val dirx = cos(r)
    val diry = sin(r)
    val ring = OffsetD(center.x + dirx * outerRadius, center.y + diry * outerRadius)
    val stubEnd = OffsetD(center.x + dirx * (outerRadius + stub), center.y + diry * (outerRadius + stub))
    val towardRight = dirx >= 0.0

    val elbowXUncapped = if (towardRight) stubEnd.x + elbowH else stubEnd.x - elbowH
    val elbowX =
        if (towardRight) {
            min(elbowXUncapped, plotBounds.right - margin)
        } else {
            max(elbowXUncapped, plotBounds.left + margin)
        }
    val elbow = OffsetD(elbowX, stubEnd.y)

    var labelLeft = if (towardRight) elbow.x + pad else elbow.x - pad - w
    labelLeft = labelLeft.coerceIn(plotBounds.left + margin, plotBounds.right - margin - w)

    var labelTop = elbow.y - h / 2.0
    labelTop = labelTop.coerceIn(plotBounds.top + margin, plotBounds.bottom - margin - h)

    val anchorX =
        if (towardRight) {
            (labelLeft - pad).coerceIn(plotBounds.left + margin, plotBounds.right - margin)
        } else {
            (labelLeft + w + pad).coerceIn(plotBounds.left + margin, plotBounds.right - margin)
        }
    val anchor = OffsetD(anchorX, elbow.y)

    val pts = listOf(ring, stubEnd, anchor)
    return PieSliceCalloutLayout(
        leaderPoints = pts,
        textTopLeft = OffsetD(labelLeft, labelTop),
    )
}
