package com.mena97villalobos.composeplotlib.samples.android.data

import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.PieChartSlice
import com.mena97villalobos.composeplotlib.core.spec.PieChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PieSliceLabelsSpec

fun samplePieChartSpec(): PieChartSpec =
    PieChartSpec(
        id = "revenue",
        slices =
            listOf(
                PieChartSlice(42.0, "Product A", ChartColor.Blue),
                PieChartSlice(28.0, "Product B", ChartColor.Green),
                PieChartSlice(18.0, "Services", ChartColor.Red),
                PieChartSlice(12.0, "Other", ChartColor.Gray),
            ),
        holeRadiusRatio = null,
        sliceLabels = PieSliceLabelsSpec(enabled = true, labelColor = ChartColor.Black),
    )

fun sampleDonutChartSpec(): PieChartSpec =
    samplePieChartSpec().copy(
        id = "donut",
        holeRadiusRatio = 0.45,
    )

fun emptyPieChartSpec(): PieChartSpec =
    PieChartSpec(
        slices =
            listOf(
                PieChartSlice(0.0, "Empty", ChartColor.LightGray),
            ),
    )
