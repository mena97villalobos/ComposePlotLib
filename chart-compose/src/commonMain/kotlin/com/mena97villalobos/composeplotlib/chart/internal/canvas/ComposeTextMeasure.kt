package com.mena97villalobos.composeplotlib.chart.internal.canvas

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints

/**
 * Matches [com.mena97villalobos.composeplotlib.chart.internal.layout.measuredLayoutInput] so planner metrics
 * align with what we draw on the canvas.
 */
@OptIn(ExperimentalTextApi::class)
internal fun TextMeasurer.measureChartText(
    text: String,
    style: TextStyle,
): TextLayoutResult =
    measure(
        text = AnnotatedString(text),
        style = style,
        overflow = TextOverflow.Clip,
        softWrap = false,
        maxLines = 1,
        constraints = Constraints(maxWidth = Int.MAX_VALUE),
    )
