package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.PolylineRectClip
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.draw.ShapeDrawStyle
import com.mena97villalobos.composeplotlib.core.layout.planning.CartesianLayoutPlanner
import com.mena97villalobos.composeplotlib.core.layout.planning.TickDensityReducer
import com.mena97villalobos.composeplotlib.core.layout.planning.TickValuePass
import com.mena97villalobos.composeplotlib.core.layout.planning.TickValuePlanner
import com.mena97villalobos.composeplotlib.core.layout.planning.axisTickLabel
import com.mena97villalobos.composeplotlib.core.layout.planning.plannerFinalizeYTicksForCanvas
import com.mena97villalobos.composeplotlib.core.layout.planning.plannerMinimumCapFromMeasuredLabelWidth
import com.mena97villalobos.composeplotlib.core.layout.planning.toPlanningBounds
import com.mena97villalobos.composeplotlib.core.math.FpMath
import com.mena97villalobos.composeplotlib.core.model.BarGroup
import com.mena97villalobos.composeplotlib.core.model.ChartColor
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.model.ScatterSeries
import com.mena97villalobos.composeplotlib.core.model.toXyPoints
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.BarChartOrientation
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.BarLayoutMode
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianLayer
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianYAxisBinding
import com.mena97villalobos.composeplotlib.core.spec.GridLineStyle
import com.mena97villalobos.composeplotlib.core.spec.LegendSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineRenderSpec
import com.mena97villalobos.composeplotlib.core.spec.PieChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ScatterRenderSpec
import com.mena97villalobos.composeplotlib.core.spec.SelectionStyle
import com.mena97villalobos.composeplotlib.core.spec.formatSliceLabel
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import com.mena97villalobos.composeplotlib.core.visible.LineRenderPipeline
import com.mena97villalobos.composeplotlib.core.visible.ScatterRenderPipeline
import com.mena97villalobos.composeplotlib.core.visible.VisibleRangeCalculator
import kotlin.math.max
import kotlin.math.min

private fun Double.normalizedLabelLayoutScale(): Double = coerceIn(0.5, 4.0)

/**
 * Builds immutable plans ([LineChartPlan], [BarChartPlan], [ScatterChartPlan], [CombinedCartesianChartPlan], [PieChartPlan], …) from specs,
 * layout input, and optional selection.
 *
 * Planning is intentionally multi-pass (see **`docs/architecture.md`** → *Planner passes*):
 * 1. [com.mena97villalobos.composeplotlib.core.layout.planning.PlanningBounds] — canvas + viewport
 * 2. [com.mena97villalobos.composeplotlib.core.layout.planning.TickValuePass] — domain tick values + reserve labels
 *    ([computeCartesianTickValuePass]; optional reuse when viewport + primary axes are stable across calls)
 * 3. Reserved regions / chrome — legend + axis label bins from [ChartTextMeasurementInput] ([buildReservedRegions])
 * 4. Cartesian **final layout** — plot bounds + [ChartTransform] + positioned ticks ([CartesianLayoutPlanner])
 * 5. [com.mena97villalobos.composeplotlib.core.layout.planning.TickDensityReducer] inside final layout — pixel-aware thinning; overlap / clip; draw lists assembled here
 *
 * Per-chart orchestration: `planLineChart` / `planScatterChart` / `planBarChart` / `planCombinedCartesianChart` share cartesian layout + grid + series commands + axis overlay; `planPieChart` uses legend reservation + radial layout only.
 */
object ChartPlanner {
    /**
     * Last emitted grid-line draw list when plot bounds, tick geometry, and style match the previous cartesian plan.
     * Thread-unsafe; intended for single-threaded UI planning. If tick positions change, commands are rebuilt.
     */
    private var gridLinesCachePlotBounds: RectD? = null
    private var gridLinesCacheStyle: GridLineStyle? = null
    private var gridLinesCacheXAxisXs: DoubleArray? = null
    private var gridLinesCacheYAxisYs: DoubleArray? = null
    private var gridLinesCacheCommands: List<DrawCommand>? = null

    private fun layoutCartesianFrame(
        input: ChartLayoutInput,
        xAxis: AxisSpec,
        yAxis: AxisSpec,
        legend: LegendSpec,
        contentPadding: InsetsD,
        legendLabelColors: List<Pair<String, ChartColor>>,
        rightAxisTickLabels: List<String> = emptyList(),
        reserveRightYAxisSpace: Boolean = false,
        tickValuePass: TickValuePass? = null,
    ): CartesianFrame =
        CartesianLayoutPlanner.resolveCartesianFrame(
            input = input,
            xAxis = xAxis,
            yAxis = yAxis,
            legend = legend,
            contentPadding = contentPadding,
            legendLabelColors = legendLabelColors,
            rightAxisTickLabels = rightAxisTickLabels,
            reserveRightYAxisSpace = reserveRightYAxisSpace,
            tickValuePass = tickValuePass,
        )

    private fun buildAxisAndTickLabelsOverlay(
        plotBounds: RectD,
        xAxis: AxisSpec,
        yAxis: AxisSpec,
        xTicks: List<AxisTick>,
        yTicks: List<AxisTick>,
    ): List<DrawCommand> {
        val overlayCommands = mutableListOf<DrawCommand>()
        overlayCommands +=
            buildAxisLines(
                plotBounds = plotBounds,
                xAxisColor = xAxis.lineStyle.color,
                yAxisColor = yAxis.lineStyle.color,
                drawBottomAxis = xAxis.enabled && xAxis.lineStyle.visible,
                drawLeftAxis = yAxis.enabled && yAxis.lineStyle.visible,
            )
        overlayCommands +=
            xTicks.mapNotNull { t ->
                if (t.showLabel) {
                    DrawCommand.Text(
                        text = t.label,
                        position = t.position,
                        color = xAxis.labelStyle.color,
                        firstBaselineFromTop = t.labelMetrics.firstBaselineFromTop,
                    )
                } else {
                    null
                }
            }
        overlayCommands +=
            yTicks.mapNotNull { t ->
                if (t.showLabel) {
                    DrawCommand.Text(
                        text = t.label,
                        position = t.position,
                        color = yAxis.labelStyle.color,
                        isYAxisTick = true,
                        firstBaselineFromTop = t.labelMetrics.firstBaselineFromTop,
                    )
                } else {
                    null
                }
            }
        return overlayCommands
    }

    /**
     * Domain tick values for primary X/Y axes before pixel-space density reduction.
     * Reuse across repeated cartesian planning when [viewport] and [xAxis] / [yAxis] are unchanged.
     */
    fun computeCartesianTickValuePass(
        viewport: ChartViewport,
        xAxis: AxisSpec,
        yAxis: AxisSpec,
    ): TickValuePass = TickValuePlanner.compute(viewport, xAxis, yAxis)

    /**
     * @param input canvas size, viewport, and measured label sizes
     * @param spec line styling, axes, grid, legend
     * @param selection optional tap highlight (vertical line + point) in plot commands
     * @param tickValuePass optional precomputed pass-2 tick values for [spec]'s primary axes (default: compute)
     */
    fun planLineChart(
        input: ChartLayoutInput,
        spec: LineChartSpec,
        selection: ChartSelection?,
        tickValuePass: TickValuePass? = null,
    ): LineChartPlan {
        val frame =
            layoutCartesianFrame(
                input = input,
                xAxis = spec.xAxis,
                yAxis = spec.yAxis,
                legend = spec.legend,
                contentPadding = spec.contentPadding,
                legendLabelColors = spec.series.map { it.label to it.lineStyle.color },
                tickValuePass = tickValuePass,
            )

        val plotCommands = mutableListOf<DrawCommand>()
        if (spec.xAxis.showGridLines || spec.yAxis.showGridLines) {
            plotCommands +=
                buildGridLines(
                    plotBounds = frame.plotBounds,
                    xTicks = if (spec.xAxis.showGridLines) frame.xTicks else emptyList(),
                    yTicks = if (spec.yAxis.showGridLines) frame.yTicks else emptyList(),
                    lineStyle = spec.grid.lineStyle,
                )
        }
        plotCommands +=
            spec.series.flatMap {
                buildLineSeriesCommands(it, frame.viewport, frame.transform, frame.plotBounds, spec.lineRender)
            }

        if (selection != null) {
            plotCommands += buildLineStyleSelectionOverlay(frame.plotBounds, selection, spec.selectionStyle)
        }

        val overlayCommands =
            buildAxisAndTickLabelsOverlay(
                frame.plotBounds,
                spec.xAxis,
                spec.yAxis,
                frame.xTicks,
                frame.yTicks,
            )

        return LineChartPlan(
            layout =
                ChartLayoutPlan(
                    canvasBounds = frame.canvasBounds,
                    plotBounds = frame.plotBounds,
                    reservedRegions = frame.reservedRegions,
                    xTicks = frame.xTicks,
                    yTicks = frame.yTicks,
                    legendEntries = frame.legendEntries,
                    plotDrawCommands = plotCommands,
                    overlayDrawCommands = overlayCommands,
                ),
            selection = selection,
        )
    }

    /**
     * @param input canvas size, viewport, and measured label sizes
     * @param spec scatter series, axes, grid, legend
     * @param selection optional tap highlight (vertical line + point) in plot commands
     * @param tickValuePass optional precomputed pass-2 tick values for [spec]'s primary axes (default: compute)
     */
    fun planScatterChart(
        input: ChartLayoutInput,
        spec: ScatterChartSpec,
        selection: ChartSelection?,
        tickValuePass: TickValuePass? = null,
    ): ScatterChartPlan {
        val frame =
            layoutCartesianFrame(
                input = input,
                xAxis = spec.xAxis,
                yAxis = spec.yAxis,
                legend = spec.legend,
                contentPadding = spec.contentPadding,
                legendLabelColors = spec.series.map { it.label to it.resolvedPointColor() },
                tickValuePass = tickValuePass,
            )

        val plotCommands = mutableListOf<DrawCommand>()
        if (spec.xAxis.showGridLines || spec.yAxis.showGridLines) {
            plotCommands +=
                buildGridLines(
                    plotBounds = frame.plotBounds,
                    xTicks = if (spec.xAxis.showGridLines) frame.xTicks else emptyList(),
                    yTicks = if (spec.yAxis.showGridLines) frame.yTicks else emptyList(),
                    lineStyle = spec.grid.lineStyle,
                )
        }
        plotCommands +=
            spec.series.flatMap {
                buildScatterSeriesCommands(
                    it,
                    frame.viewport,
                    frame.transform,
                    frame.plotBounds,
                    spec.scatterRender,
                )
            }

        if (selection != null) {
            plotCommands += buildLineStyleSelectionOverlay(frame.plotBounds, selection, spec.selectionStyle)
        }

        val overlayCommands =
            buildAxisAndTickLabelsOverlay(
                frame.plotBounds,
                spec.xAxis,
                spec.yAxis,
                frame.xTicks,
                frame.yTicks,
            )

        return ScatterChartPlan(
            layout =
                ChartLayoutPlan(
                    canvasBounds = frame.canvasBounds,
                    plotBounds = frame.plotBounds,
                    reservedRegions = frame.reservedRegions,
                    xTicks = frame.xTicks,
                    yTicks = frame.yTicks,
                    legendEntries = frame.legendEntries,
                    plotDrawCommands = plotCommands,
                    overlayDrawCommands = overlayCommands,
                ),
            selection = selection,
        )
    }

    /**
     * Radial pie / donut chart: emits one [DrawCommand.Arc] fill per positive-value slice (draw order matches [PieChartSpec.slices]).
     * [ChartLayoutPlan.xTicks] and [ChartLayoutPlan.yTicks] stay empty and [ChartLayoutPlan.overlayDrawCommands] has no axis chrome.
     * When [PieChartSpec.sliceLabels] is enabled, appends [DrawCommand.Polyline] leaders and [DrawCommand.Text] callouts
     * per eligible slice (uses [ChartTextMeasurementInput.pieSliceLabelMeasurements] from the measurement pass).
     *
     * @param selection optional slice highlight: matches when [ChartSelection.target.seriesId] equals [PieChartSpec.id]
     * and [ChartSelection.target.pointIndex] is the slice index in [PieChartSpec.slices]. A translucent overlay arc
     * is appended for the selected slice when its [PieChartSlice.value] is positive.
     */
    fun planPieChart(
        input: ChartLayoutInput,
        spec: PieChartSpec,
        selection: ChartSelection?,
    ): PieChartPlan {
        val labelScale = input.labelLayoutScale.normalizedLabelLayoutScale()
        val bounds = input.toPlanningBounds()
        val legendEntries =
            buildLegendEntries(
                labels = spec.slices.map { it.label to it.color },
                canvasBounds = bounds.canvasBounds,
                minimumInsets = spec.contentPadding,
                textMeasurements = input.textMeasurements,
                legendSpec = spec.legend,
                labelLayoutScale = labelScale,
            )
        val reservedRegions =
            buildReservedRegions(
                canvasBounds = bounds.canvasBounds,
                minimumInsets = spec.contentPadding,
                xTickLabels = emptyList(),
                yTickLabels = emptyList(),
                legendEntries = legendEntries,
                textMeasurements = input.textMeasurements,
                xAxisEnabled = false,
                yAxisEnabled = false,
                labelLayoutScale = labelScale,
            )
        val plotBounds = bounds.canvasBounds.inset(reservedRegions.contentInsets)
        val radial = PieChartLayoutMath.layout(plotBounds, spec.holeRadiusRatio)

        val contributing = spec.slices.mapIndexed { index, s -> index to s }.filter { it.second.value > 0.0 }
        val total = contributing.sumOf { it.second.value }
        val gapDeg = PieSweep.effectiveSliceGapDegrees(contributing.size, spec.sliceGapDegrees)
        val sweeps = PieSweep.sweepsDegrees(contributing, total, spec.sliceGapDegrees)
        val plotCommands = mutableListOf<DrawCommand>()
        if (total > 0.0 && radial.outerRadius > 0.0 && sweeps.isNotEmpty()) {
            var angle = spec.startAngleDegrees
            for (i in contributing.indices) {
                val slice = contributing[i].second
                val sweep = sweeps[i]
                if (sweep <= 0.0) continue
                plotCommands +=
                    DrawCommand.Arc(
                        center = radial.center,
                        radius = radial.outerRadius,
                        startAngleDegrees = angle,
                        sweepAngleDegrees = sweep,
                        useCenter = true,
                        style = ShapeDrawStyle.Fill(slice.color),
                        innerRadius = radial.innerRadius,
                    )
                angle += sweep + gapDeg
            }
        }

        val selectedSliceIndex =
            when {
                selection == null -> null
                selection.target.seriesId != spec.id -> null
                else -> selection.target.pointIndex
            }
        if (selectedSliceIndex != null &&
            selectedSliceIndex in spec.slices.indices &&
            total > 0.0 &&
            radial.outerRadius > 0.0
        ) {
            val selSlice = spec.slices[selectedSliceIndex]
            if (selSlice.value > 0.0 && sweeps.isNotEmpty()) {
                var angle = spec.startAngleDegrees
                var startForSelection = spec.startAngleDegrees
                var sweepForSelection = 0.0
                var matched = false
                for (i in contributing.indices) {
                    val (idx, s) = contributing[i]
                    val sweep = sweeps[i]
                    if (idx == selectedSliceIndex) {
                        startForSelection = angle
                        sweepForSelection = sweep
                        matched = true
                        break
                    }
                    angle += sweep + gapDeg
                }
                if (matched && sweepForSelection > 0.0) {
                    plotCommands +=
                        DrawCommand.Group(
                            alpha = 0.35,
                            children =
                                listOf(
                                    DrawCommand.Arc(
                                        center = radial.center,
                                        radius = radial.outerRadius,
                                        startAngleDegrees = startForSelection,
                                        sweepAngleDegrees = sweepForSelection,
                                        useCenter = true,
                                        style = ShapeDrawStyle.Fill(spec.selectionStyle.pointColor),
                                        innerRadius = radial.innerRadius,
                                    ),
                                ),
                        )
                }
            }
        }

        val sliceLabels = spec.sliceLabels
        if (sliceLabels.enabled && total > 0.0 && radial.outerRadius > 0.0 && sweeps.isNotEmpty()) {
            var labelAngle = spec.startAngleDegrees
            for (i in contributing.indices) {
                val (idx, slice) = contributing[i]
                val sweep = sweeps[i]
                if (sweep < sliceLabels.minSweepDegrees) {
                    labelAngle += sweep + gapDeg
                    continue
                }
                val midDeg = labelAngle + sweep / 2.0
                val pct = 100.0 * slice.value / total
                val text = sliceLabels.formatSliceLabel(slice, pct)
                val metrics = input.textMeasurements.pieSliceLabelMeasurement(idx)
                val callout =
                    layoutPieSliceCallout(
                        plotBounds = plotBounds,
                        center = radial.center,
                        outerRadius = radial.outerRadius,
                        midAngleDegrees = midDeg,
                        textWidth = metrics.width,
                        textHeight = metrics.height,
                        spec = sliceLabels,
                    )
                plotCommands +=
                    DrawCommand.Polyline(
                        points = callout.leaderPoints,
                        color = sliceLabels.leaderColor,
                        strokeWidth = sliceLabels.leaderStrokeWidthPx,
                    )
                plotCommands +=
                    DrawCommand.Text(
                        text = text,
                        position = callout.textTopLeft,
                        color = sliceLabels.labelColor,
                        isPieSliceLabel = true,
                        firstBaselineFromTop = metrics.firstBaselineFromTop,
                    )
                labelAngle += sweep + gapDeg
            }
        }

        return PieChartPlan(
            layout =
                ChartLayoutPlan(
                    canvasBounds = bounds.canvasBounds,
                    plotBounds = plotBounds,
                    reservedRegions = reservedRegions,
                    xTicks = emptyList(),
                    yTicks = emptyList(),
                    legendEntries = legendEntries,
                    plotDrawCommands = plotCommands,
                    overlayDrawCommands = emptyList(),
                ),
            selection = selection,
        )
    }

    /**
     * @param input canvas size, viewport, and measured label sizes
     * @param spec bar groups, axes, grid, legend
     * @param selection optional tap highlight (top edge stroke on the matching bar)
     * @param tickValuePass optional precomputed pass-2 tick values for [spec]'s primary axes (default: compute)
     */
    fun planBarChart(
        input: ChartLayoutInput,
        spec: BarChartSpec,
        selection: ChartSelection? = null,
        tickValuePass: TickValuePass? = null,
    ): BarChartPlan {
        val frame =
            layoutCartesianFrame(
                input = input,
                xAxis = spec.xAxis,
                yAxis = spec.yAxis,
                legend = spec.legend,
                contentPadding = spec.contentPadding,
                legendLabelColors = spec.groups.map { it.label to it.style.color },
                tickValuePass = tickValuePass,
            )

        val plotCommands = mutableListOf<DrawCommand>()
        if (spec.xAxis.showGridLines || spec.yAxis.showGridLines) {
            plotCommands +=
                buildGridLines(
                    plotBounds = frame.plotBounds,
                    xTicks = if (spec.xAxis.showGridLines) frame.xTicks else emptyList(),
                    yTicks = if (spec.yAxis.showGridLines) frame.yTicks else emptyList(),
                    lineStyle = spec.grid.lineStyle,
                )
        }
        val effectiveBarLayout = spec.resolvedBarLayout()
        val barSegments =
            computePlannedBarSegments(
                groups = spec.groups,
                layoutMode = effectiveBarLayout,
                clusterBarGapPx = spec.clusterBarGapPx,
                orientation = spec.orientation,
                viewport = frame.viewport,
                transform = frame.transform,
                plotBounds = frame.plotBounds,
                layerId = null,
            )
        plotCommands +=
            buildBarDrawCommands(
                segments = barSegments,
                selection = selection,
                selectionStyle = spec.selectionStyle,
            )

        val overlayCommands =
            buildAxisAndTickLabelsOverlay(
                frame.plotBounds,
                spec.xAxis,
                spec.yAxis,
                frame.xTicks,
                frame.yTicks,
            )

        return BarChartPlan(
            layout =
                ChartLayoutPlan(
                    canvasBounds = frame.canvasBounds,
                    plotBounds = frame.plotBounds,
                    reservedRegions = frame.reservedRegions,
                    xTicks = frame.xTicks,
                    yTicks = frame.yTicks,
                    legendEntries = frame.legendEntries,
                    plotDrawCommands = plotCommands,
                    overlayDrawCommands = overlayCommands,
                ),
            selection = selection,
            orientation = spec.orientation,
            barSegments = barSegments,
        )
    }

    private fun buildLineSeriesCommands(
        series: LineSeries,
        viewport: ChartViewport,
        transform: ChartTransform,
        plotBounds: RectD,
        lineRender: LineRenderSpec,
    ): List<DrawCommand> {
        val indexed = LineRenderPipeline.prepareIndexed(series.toXyPoints(), viewport, lineRender)
        if (indexed.isEmpty()) return emptyList()

        val pixelPoints = ArrayList<OffsetD>(indexed.size)
        for ((_, p) in indexed) {
            pixelPoints.add(transform.toPixel(p))
        }
        val lineFragments = PolylineRectClip.clipPolylineToRect(pixelPoints, plotBounds)
        val commands = mutableListOf<DrawCommand>()
        for (fragment in lineFragments) {
            commands +=
                DrawCommand.Polyline(
                    points = fragment,
                    color = series.lineStyle.color,
                    strokeWidth = series.lineStyle.strokeWidth,
                )
        }

        if (series.pointStyle.visible) {
            val pr = series.pointStyle.radius.coerceAtLeast(0.0)
            commands +=
                pixelPoints.mapNotNull { pixel ->
                    if (!plotBounds.intersectsDisc(pixel, pr)) {
                        return@mapNotNull null
                    }
                    DrawCommand.Circle(
                        center = pixel,
                        radius = pr,
                        color = series.pointStyle.color ?: series.lineStyle.color,
                    )
                }
        }

        return commands
    }

    private fun buildScatterSeriesCommands(
        series: ScatterSeries,
        viewport: ChartViewport,
        transform: ChartTransform,
        plotBounds: RectD,
        scatterRender: ScatterRenderSpec,
    ): List<DrawCommand> {
        if (!series.pointStyle.visible) return emptyList()
        val fillColor = series.resolvedPointColor()
        val r = series.pointStyle.radius.coerceAtLeast(0.0)
        val noDecimationCap = scatterRender.maxPointsPerSeries <= 0
        val prepared: List<Pair<Int, XyPoint>> =
            if (noDecimationCap) {
                series.points.mapIndexed { i, p -> i to XyPoint(p.x, p.y) }
            } else {
                ScatterRenderPipeline.prepareIndexed(series.toXyPoints(), viewport, scatterRender)
            }
        if (prepared.isEmpty()) return emptyList()
        val commands = ArrayList<DrawCommand>(min(prepared.size, 4096))
        for ((_, xy) in prepared) {
            val pixel = transform.toPixel(xy)
            if (!plotBounds.intersectsDisc(pixel, r)) continue
            commands +=
                DrawCommand.Circle(
                    center = pixel,
                    radius = r,
                    color = fillColor,
                )
        }
        return commands
    }

    private fun computePlannedBarSegments(
        groups: List<BarGroup>,
        layoutMode: BarLayoutMode,
        clusterBarGapPx: Double,
        orientation: BarChartOrientation,
        viewport: ChartViewport,
        transform: ChartTransform,
        plotBounds: RectD,
        layerId: String? = null,
    ): List<PlannedBarSegment> {
        if (groups.isEmpty()) {
            return emptyList()
        }
        val raw =
            when (orientation) {
                BarChartOrientation.Vertical ->
                    when (layoutMode) {
                        BarLayoutMode.Independent ->
                            buildIndependentBarSegments(
                                groups,
                                viewport,
                                transform,
                                plotBounds,
                                layoutMode,
                                layerId,
                            )
                        BarLayoutMode.Clustered ->
                            buildClusteredBarSegments(
                                groups,
                                clusterBarGapPx,
                                viewport,
                                transform,
                                plotBounds,
                                layoutMode,
                                layerId,
                            )
                        BarLayoutMode.Stacked ->
                            buildStackedBarSegments(
                                groups,
                                viewport,
                                transform,
                                plotBounds,
                                layoutMode,
                                layerId,
                            )
                    }
                BarChartOrientation.Horizontal ->
                    when (layoutMode) {
                        BarLayoutMode.Independent ->
                            buildHorizontalIndependentBarSegments(
                                groups,
                                viewport,
                                transform,
                                plotBounds,
                                layoutMode,
                                layerId,
                            )
                        BarLayoutMode.Clustered ->
                            buildHorizontalClusteredBarSegments(
                                groups,
                                clusterBarGapPx,
                                viewport,
                                transform,
                                plotBounds,
                                layoutMode,
                                layerId,
                            )
                        BarLayoutMode.Stacked ->
                            buildHorizontalStackedBarSegments(
                                groups,
                                viewport,
                                transform,
                                plotBounds,
                                layoutMode,
                                layerId,
                            )
                    }
            }
        val hSlack = VisibleRangeCalculator.BAR_COLUMN_PLOT_EDGE_SLACK_PX
        val plotHitSlack =
            when (orientation) {
                BarChartOrientation.Vertical ->
                    plotBounds.inset(
                        InsetsD(
                            left = -(hSlack + FpMath.EPS),
                            top = -FpMath.EPS,
                            right = -(hSlack + FpMath.EPS),
                            bottom = -FpMath.EPS,
                        ),
                    )
                BarChartOrientation.Horizontal ->
                    plotBounds.inset(
                        InsetsD(
                            left = -FpMath.EPS,
                            top = -(hSlack + FpMath.EPS),
                            right = -FpMath.EPS,
                            bottom = -(hSlack + FpMath.EPS),
                        ),
                    )
            }
        return raw.filter { it.bounds.intersects(plotHitSlack) }
    }

    private fun buildIndependentBarSegments(
        groups: List<BarGroup>,
        viewport: ChartViewport,
        transform: ChartTransform,
        plotBounds: RectD,
        barLayoutMode: BarLayoutMode,
        layerId: String?,
    ): List<PlannedBarSegment> {
        return groups.flatMap { group ->
            group.entries.mapIndexedNotNull { index, entry ->
                val centerXPx = transform.toPixel(XyPoint(entry.x, viewport.yRange.start)).x
                if (!VisibleRangeCalculator.barColumnIntersectsPlotHorizontally(centerXPx, group.style.barWidth, plotBounds)) {
                    return@mapIndexedNotNull null
                }
                PlannedBarSegment(
                    groupId = group.id,
                    categoryIndex = index,
                    entry = entry,
                    bounds = barRectForVerticalColumn(group, entry.x, entry.y, viewport, transform, plotBounds),
                    fillColor = group.style.color,
                    orientation = BarChartOrientation.Vertical,
                    barLayoutMode = barLayoutMode,
                    layerId = layerId,
                )
            }
        }
    }

    private fun buildHorizontalIndependentBarSegments(
        groups: List<BarGroup>,
        viewport: ChartViewport,
        transform: ChartTransform,
        plotBounds: RectD,
        barLayoutMode: BarLayoutMode,
        layerId: String?,
    ): List<PlannedBarSegment> {
        val x0 = viewport.xRange.start
        return groups.flatMap { group ->
            group.entries.mapIndexedNotNull { index, entry ->
                val centerYPx = transform.toPixel(XyPoint(x0, entry.x)).y
                if (!VisibleRangeCalculator.barRowIntersectsPlotVertically(centerYPx, group.style.barWidth, plotBounds)) {
                    return@mapIndexedNotNull null
                }
                PlannedBarSegment(
                    groupId = group.id,
                    categoryIndex = index,
                    entry = entry,
                    bounds =
                        barRectForHorizontalRow(
                            group = group,
                            categoryDomain = entry.x,
                            valueDomain = entry.y,
                            viewport = viewport,
                            transform = transform,
                            plotBounds = plotBounds,
                            centerYPx = centerYPx,
                        ),
                    fillColor = group.style.color,
                    orientation = BarChartOrientation.Horizontal,
                    barLayoutMode = barLayoutMode,
                    layerId = layerId,
                )
            }
        }
    }

    private fun buildClusteredBarSegments(
        groups: List<BarGroup>,
        clusterBarGapPx: Double,
        viewport: ChartViewport,
        transform: ChartTransform,
        plotBounds: RectD,
        barLayoutMode: BarLayoutMode,
        layerId: String?,
    ): List<PlannedBarSegment> {
        val n = groups.first().entries.size
        val gap = clusterBarGapPx.coerceAtLeast(0.0)
        val out = mutableListOf<PlannedBarSegment>()
        for (j in 0 until n) {
            val categoryX = groups.first().entries[j].x
            val widths = groups.map { it.style.barWidth }
            val totalW = widths.sum() + gap * max(0, groups.size - 1)
            val cx = transform.toPixel(XyPoint(categoryX, viewport.yRange.start)).x
            var xLeft = cx - totalW / 2.0
            for ((gi, group) in groups.withIndex()) {
                val w = group.style.barWidth
                val barCenterX = xLeft + w / 2.0
                val entry = group.entries[j]
                if (!VisibleRangeCalculator.barColumnIntersectsPlotHorizontally(barCenterX, w, plotBounds)) {
                    xLeft += w + if (gi < groups.lastIndex) gap else 0.0
                    continue
                }
                val rect = barRectWithPixelCenter(group, categoryX, entry.y, viewport, transform, plotBounds, barCenterX)
                out.add(
                    PlannedBarSegment(
                        groupId = group.id,
                        categoryIndex = j,
                        entry = entry,
                        bounds = rect,
                        fillColor = group.style.color,
                        orientation = BarChartOrientation.Vertical,
                        barLayoutMode = barLayoutMode,
                        layerId = layerId,
                    ),
                )
                xLeft += w + if (gi < groups.lastIndex) gap else 0.0
            }
        }
        return out
    }

    private fun buildHorizontalClusteredBarSegments(
        groups: List<BarGroup>,
        clusterBarGapPx: Double,
        viewport: ChartViewport,
        transform: ChartTransform,
        plotBounds: RectD,
        barLayoutMode: BarLayoutMode,
        layerId: String?,
    ): List<PlannedBarSegment> {
        val x0 = viewport.xRange.start
        val n = groups.first().entries.size
        val gap = clusterBarGapPx.coerceAtLeast(0.0)
        val out = mutableListOf<PlannedBarSegment>()
        for (j in 0 until n) {
            val categoryY = groups.first().entries[j].x
            val thicknesses = groups.map { it.style.barWidth }
            val totalH = thicknesses.sum() + gap * max(0, groups.size - 1)
            val cy = transform.toPixel(XyPoint(x0, categoryY)).y
            var yTop = cy - totalH / 2.0
            for ((gi, group) in groups.withIndex()) {
                val h = group.style.barWidth
                val barCenterY = yTop + h / 2.0
                val entry = group.entries[j]
                if (!VisibleRangeCalculator.barRowIntersectsPlotVertically(barCenterY, h, plotBounds)) {
                    yTop += h + if (gi < groups.lastIndex) gap else 0.0
                    continue
                }
                val rect =
                    barRectForHorizontalRow(
                        group = group,
                        categoryDomain = categoryY,
                        valueDomain = entry.y,
                        viewport = viewport,
                        transform = transform,
                        plotBounds = plotBounds,
                        centerYPx = barCenterY,
                    )
                out.add(
                    PlannedBarSegment(
                        groupId = group.id,
                        categoryIndex = j,
                        entry = entry,
                        bounds = rect,
                        fillColor = group.style.color,
                        orientation = BarChartOrientation.Horizontal,
                        barLayoutMode = barLayoutMode,
                        layerId = layerId,
                    ),
                )
                yTop += h + if (gi < groups.lastIndex) gap else 0.0
            }
        }
        return out
    }

    private fun buildStackedBarSegments(
        groups: List<BarGroup>,
        viewport: ChartViewport,
        transform: ChartTransform,
        plotBounds: RectD,
        barLayoutMode: BarLayoutMode,
        layerId: String?,
    ): List<PlannedBarSegment> {
        val n = groups.first().entries.size
        val y0 = viewport.yRange.start
        val out = mutableListOf<PlannedBarSegment>()
        for (j in 0 until n) {
            val categoryX = groups.first().entries[j].x
            val columnCenterX = transform.toPixel(XyPoint(categoryX, y0)).x
            val columnBarWidth = groups.maxOf { it.style.barWidth }
            if (!VisibleRangeCalculator.barColumnIntersectsPlotHorizontally(columnCenterX, columnBarWidth, plotBounds)) {
                continue
            }
            var cumulative = y0
            for (group in groups) {
                val entry = group.entries[j]
                val yLo = cumulative
                val yHi = cumulative + entry.y
                cumulative = yHi
                val centerX = transform.toPixel(XyPoint(categoryX, y0)).x
                val pTop = transform.toPixel(XyPoint(categoryX, yHi))
                val pBot = transform.toPixel(XyPoint(categoryX, yLo))
                val topY = min(pTop.y, pBot.y)
                val botY = max(pTop.y, pBot.y).coerceAtMost(plotBounds.bottom)
                val half = group.style.barWidth / 2.0
                val rect = RectD(centerX - half, topY, centerX + half, botY)
                out.add(
                    PlannedBarSegment(
                        groupId = group.id,
                        categoryIndex = j,
                        entry = entry,
                        bounds = rect,
                        fillColor = group.style.color,
                        orientation = BarChartOrientation.Vertical,
                        barLayoutMode = barLayoutMode,
                        layerId = layerId,
                    ),
                )
            }
        }
        return out
    }

    private fun buildHorizontalStackedBarSegments(
        groups: List<BarGroup>,
        viewport: ChartViewport,
        transform: ChartTransform,
        plotBounds: RectD,
        barLayoutMode: BarLayoutMode,
        layerId: String?,
    ): List<PlannedBarSegment> {
        val n = groups.first().entries.size
        val x0 = viewport.xRange.start
        val out = mutableListOf<PlannedBarSegment>()
        for (j in 0 until n) {
            val categoryY = groups.first().entries[j].x
            val columnCenterY = transform.toPixel(XyPoint(x0, categoryY)).y
            val columnBarThickness = groups.maxOf { it.style.barWidth }
            if (!VisibleRangeCalculator.barRowIntersectsPlotVertically(columnCenterY, columnBarThickness, plotBounds)) {
                continue
            }
            var cumulative = x0
            for (group in groups) {
                val entry = group.entries[j]
                val xLo = cumulative
                val xHi = cumulative + entry.y
                cumulative = xHi
                val pLeft = transform.toPixel(XyPoint(xLo, categoryY))
                val pRight = transform.toPixel(XyPoint(xHi, categoryY))
                val leftX = min(pLeft.x, pRight.x).coerceAtLeast(plotBounds.left)
                val rightX = max(pLeft.x, pRight.x).coerceAtMost(plotBounds.right)
                val half = group.style.barWidth / 2.0
                val rect = RectD(leftX, columnCenterY - half, rightX, columnCenterY + half)
                out.add(
                    PlannedBarSegment(
                        groupId = group.id,
                        categoryIndex = j,
                        entry = entry,
                        bounds = rect,
                        fillColor = group.style.color,
                        orientation = BarChartOrientation.Horizontal,
                        barLayoutMode = barLayoutMode,
                        layerId = layerId,
                    ),
                )
            }
        }
        return out
    }

    private fun barRectForVerticalColumn(
        group: BarGroup,
        xDomain: Double,
        yTopDomain: Double,
        viewport: ChartViewport,
        transform: ChartTransform,
        plotBounds: RectD,
    ): RectD {
        return barRectWithPixelCenter(
            group = group,
            xDomain = xDomain,
            yTopDomain = yTopDomain,
            viewport = viewport,
            transform = transform,
            plotBounds = plotBounds,
            centerXPx = transform.toPixel(XyPoint(xDomain, viewport.yRange.start)).x,
        )
    }

    private fun barRectWithPixelCenter(
        group: BarGroup,
        xDomain: Double,
        yTopDomain: Double,
        viewport: ChartViewport,
        transform: ChartTransform,
        plotBounds: RectD,
        centerXPx: Double,
    ): RectD {
        val top = transform.toPixel(XyPoint(xDomain, yTopDomain))
        val bottom = transform.toPixel(XyPoint(xDomain, viewport.yRange.start))
        val half = group.style.barWidth / 2.0
        val topY = min(top.y, bottom.y)
        val botY = max(top.y, bottom.y).coerceAtMost(plotBounds.bottom)
        return RectD(
            left = centerXPx - half,
            top = topY,
            right = centerXPx + half,
            bottom = botY,
        )
    }

    /**
     * Horizontal bar: category on Y ([categoryDomain]), value length along X from [viewport.xRange.start].
     */
    private fun barRectForHorizontalRow(
        group: BarGroup,
        categoryDomain: Double,
        valueDomain: Double,
        viewport: ChartViewport,
        transform: ChartTransform,
        plotBounds: RectD,
        centerYPx: Double,
    ): RectD {
        return barRectWithPixelCenterHorizontal(
            group = group,
            categoryDomain = categoryDomain,
            valueDomain = valueDomain,
            viewport = viewport,
            transform = transform,
            plotBounds = plotBounds,
            centerYPx = centerYPx,
        )
    }

    private fun barRectWithPixelCenterHorizontal(
        group: BarGroup,
        categoryDomain: Double,
        valueDomain: Double,
        viewport: ChartViewport,
        transform: ChartTransform,
        plotBounds: RectD,
        centerYPx: Double,
    ): RectD {
        val x0 = viewport.xRange.start
        val left = transform.toPixel(XyPoint(x0, categoryDomain))
        val right = transform.toPixel(XyPoint(x0 + valueDomain, categoryDomain))
        val leftX = min(left.x, right.x).coerceAtLeast(plotBounds.left)
        val rightX = max(left.x, right.x).coerceAtMost(plotBounds.right)
        val half = group.style.barWidth / 2.0
        return RectD(
            left = leftX,
            top = centerYPx - half,
            right = rightX,
            bottom = centerYPx + half,
        )
    }

    private fun buildBarDrawCommands(
        segments: List<PlannedBarSegment>,
        selection: ChartSelection?,
        selectionStyle: SelectionStyle,
    ): List<DrawCommand> {
        return segments.flatMap { seg ->
            val rect = DrawCommand.Rect(bounds = seg.bounds, color = seg.fillColor)
            if (selection != null && selectionMatchesSegment(selection, seg)) {
                val accent =
                    when (seg.orientation) {
                        BarChartOrientation.Vertical ->
                            DrawCommand.Line(
                                start = OffsetD(seg.bounds.left, seg.bounds.top),
                                end = OffsetD(seg.bounds.right, seg.bounds.top),
                                color = selectionStyle.lineColor,
                                strokeWidth = selectionStyle.lineStrokeWidth,
                            )
                        BarChartOrientation.Horizontal -> {
                            // Value-outer edge: max X in plot space (stack grows from the X baseline).
                            val outerX = max(seg.bounds.left, seg.bounds.right)
                            DrawCommand.Line(
                                start = OffsetD(outerX, seg.bounds.top),
                                end = OffsetD(outerX, seg.bounds.bottom),
                                color = selectionStyle.lineColor,
                                strokeWidth = selectionStyle.lineStrokeWidth,
                            )
                        }
                    }
                listOf(rect, accent)
            } else {
                listOf(rect)
            }
        }
    }

    private fun selectionMatchesSegment(
        selection: ChartSelection,
        segment: PlannedBarSegment,
    ): Boolean {
        val selLayer = selection.target.layerId
        val segLayer = segment.layerId
        if (selLayer != null || segLayer != null) {
            if (selLayer != segLayer) return false
        }
        if (selection.target.seriesId != segment.groupId) return false
        if (selection.target.pointIndex != segment.categoryIndex) return false
        return when (segment.barLayoutMode) {
            BarLayoutMode.Independent -> FpMath.nearlyEqual(selection.target.point.x, segment.entry.x)
            BarLayoutMode.Clustered, BarLayoutMode.Stacked -> true
        }
    }

    private fun buildGridLines(
        plotBounds: RectD,
        xTicks: List<AxisTick>,
        yTicks: List<AxisTick>,
        lineStyle: GridLineStyle,
    ): List<DrawCommand> {
        val nx = xTicks.size
        val ny = yTicks.size
        val cachedXs = gridLinesCacheXAxisXs
        val cachedYs = gridLinesCacheYAxisYs
        val cachedCmds = gridLinesCacheCommands
        if (cachedCmds != null &&
            gridLinesCachePlotBounds == plotBounds &&
            gridLinesCacheStyle == lineStyle &&
            cachedXs != null && cachedXs.size == nx &&
            cachedYs != null && cachedYs.size == ny
        ) {
            var match = true
            var i = 0
            while (i < nx) {
                if (xTicks[i].axisPosition.x != cachedXs[i]) {
                    match = false
                    break
                }
                i++
            }
            if (match) {
                i = 0
                while (i < ny) {
                    if (yTicks[i].axisPosition.y != cachedYs[i]) {
                        match = false
                        break
                    }
                    i++
                }
            }
            if (match) {
                return cachedCmds
            }
        }

        val total = nx + ny
        val out = ArrayList<DrawCommand>(total)
        for (t in xTicks) {
            out.add(
                DrawCommand.Line(
                    start = OffsetD(t.axisPosition.x, plotBounds.top),
                    end = OffsetD(t.axisPosition.x, plotBounds.bottom),
                    color = lineStyle.color,
                    strokeWidth = lineStyle.strokeWidth,
                    strokeStyle = lineStyle.strokeStyle,
                ),
            )
        }
        for (t in yTicks) {
            out.add(
                DrawCommand.Line(
                    start = OffsetD(plotBounds.left, t.axisPosition.y),
                    end = OffsetD(plotBounds.right, t.axisPosition.y),
                    color = lineStyle.color,
                    strokeWidth = lineStyle.strokeWidth,
                    strokeStyle = lineStyle.strokeStyle,
                ),
            )
        }

        var xs = gridLinesCacheXAxisXs
        if (xs == null || xs.size != nx) {
            xs = DoubleArray(nx)
            gridLinesCacheXAxisXs = xs
        }
        for (xi in 0 until nx) {
            xs[xi] = xTicks[xi].axisPosition.x
        }
        var ys = gridLinesCacheYAxisYs
        if (ys == null || ys.size != ny) {
            ys = DoubleArray(ny)
            gridLinesCacheYAxisYs = ys
        }
        for (yi in 0 until ny) {
            ys[yi] = yTicks[yi].axisPosition.y
        }
        gridLinesCachePlotBounds = plotBounds
        gridLinesCacheStyle = lineStyle
        gridLinesCacheCommands = out
        return out
    }

    private fun buildAxisLines(
        plotBounds: RectD,
        xAxisColor: ChartColor,
        yAxisColor: ChartColor,
        drawBottomAxis: Boolean,
        drawLeftAxis: Boolean,
    ): List<DrawCommand> {
        val commands = mutableListOf<DrawCommand>()
        if (drawBottomAxis) {
            commands +=
                DrawCommand.Line(
                    start = OffsetD(plotBounds.left, plotBounds.bottom),
                    end = OffsetD(plotBounds.right, plotBounds.bottom),
                    color = xAxisColor,
                )
        }
        if (drawLeftAxis) {
            commands +=
                DrawCommand.Line(
                    start = OffsetD(plotBounds.left, plotBounds.top),
                    end = OffsetD(plotBounds.left, plotBounds.bottom),
                    color = yAxisColor,
                )
        }
        return commands
    }

    private fun buildRightYTicks(
        plotBounds: RectD,
        viewport: ChartViewport,
        tickValues: List<Double>,
        textMeasurements: ChartTextMeasurementInput,
        axisSpec: AxisSpec,
        labelLayoutScale: Double,
    ): List<AxisTick> {
        val yAxisPad = Y_TICK_LABEL_AXIS_PADDING * labelLayoutScale
        return tickValues.reversed().map { value ->
            val label = axisTickLabel(axisSpec, value)
            val metrics = textMeasurements.tickLabelMetrics(label)
            val fraction =
                if (viewport.yRange.span == 0.0) {
                    0.0
                } else {
                    (viewport.yRange.endInclusive - value) / viewport.yRange.span
                }
            val y = plotBounds.top + (plotBounds.height * fraction)
            val ascentApprox = metrics.firstBaselineFromTop.coerceAtMost(metrics.height * 0.5)
            val boxMidToBaseline = metrics.firstBaselineFromTop - metrics.height / 2.0
            val opticalShift =
                (boxMidToBaseline - ascentApprox * 0.35).coerceIn(-metrics.height * 0.2, metrics.height * 0.2)
            val labelTopY = y - metrics.height / 2.0 - opticalShift
            AxisTick(
                value = value,
                label = label,
                axisPosition = OffsetD(plotBounds.right, y),
                position = OffsetD(plotBounds.right + yAxisPad, labelTopY),
                labelMetrics = metrics,
            )
        }
    }

    private fun buildRightAxisOverlay(
        plotBounds: RectD,
        yAxis: AxisSpec,
        yTicks: List<AxisTick>,
    ): List<DrawCommand> {
        val overlayCommands = mutableListOf<DrawCommand>()
        if (yAxis.enabled && yAxis.lineStyle.visible) {
            overlayCommands +=
                DrawCommand.Line(
                    start = OffsetD(plotBounds.right, plotBounds.top),
                    end = OffsetD(plotBounds.right, plotBounds.bottom),
                    color = yAxis.lineStyle.color,
                )
        }
        overlayCommands +=
            yTicks.mapNotNull { t ->
                if (t.showLabel) {
                    DrawCommand.Text(
                        text = t.label,
                        position = t.position,
                        color = yAxis.labelStyle.color,
                        isYAxisTick = true,
                        isSecondaryYAxisTick = true,
                        firstBaselineFromTop = t.labelMetrics.firstBaselineFromTop,
                    )
                } else {
                    null
                }
            }
        return overlayCommands
    }

    /**
     * @param reserveRightYAxisSpace When true, reserves canvas width for [spec.yAxisSecondary] tick labels (after measurement pass).
     * @param tickValuePass optional precomputed pass-2 tick values for [spec]'s primary X / shared Y axes (default: compute).
     */
    fun planCombinedCartesianChart(
        input: ChartLayoutInput,
        spec: CombinedCartesianChartSpec,
        selection: ChartSelection?,
        reserveRightYAxisSpace: Boolean = true,
        tickValuePass: TickValuePass? = null,
    ): CombinedCartesianChartPlan {
        require(spec.layers.isNotEmpty()) { "CombinedCartesianChartSpec.layers must not be empty" }
        val needsSecondary = spec.layers.any { it.yAxisBinding == CombinedCartesianYAxisBinding.SecondaryY }
        require(!needsSecondary || spec.yAxisSecondary != null) {
            "yAxisSecondary is required when any layer uses SecondaryY"
        }

        val labelScale = input.labelLayoutScale.normalizedLabelLayoutScale()
        val ySecSpec = spec.yAxisSecondary
        val secondaryViewport =
            if (needsSecondary && ySecSpec != null) {
                combinedSecondaryViewport(input.viewport, spec)
            } else {
                null
            }

        val primaryTickPass = tickValuePass ?: computeCartesianTickValuePass(input.viewport, spec.xAxis, spec.yAxis)

        val frame0 =
            layoutCartesianFrame(
                input = input,
                xAxis = spec.xAxis,
                yAxis = spec.yAxis,
                legend = spec.legend,
                contentPadding = spec.contentPadding,
                legendLabelColors = combinedLegendLabels(spec),
                rightAxisTickLabels = emptyList(),
                reserveRightYAxisSpace = false,
                tickValuePass = primaryTickPass,
            )

        val rightLabelsForReserve =
            if (needsSecondary && ySecSpec != null && reserveRightYAxisSpace && ySecSpec.enabled) {
                val secVp = checkNotNull(secondaryViewport)
                val tickPassSec =
                    TickValuePlanner.compute(
                        ChartViewport(frame0.viewport.xRange, secVp.yRange),
                        spec.xAxis,
                        ySecSpec,
                    )
                val yGapSecPre = (ySecSpec.minVerticalTickLabelGapPx ?: Y_TICK_LABEL_MIN_VERTICAL_GAP) * labelScale
                val yMinCapSec =
                    plannerMinimumCapFromMeasuredLabelWidth(
                        axis = ySecSpec,
                        axisSpanPx = frame0.plotBounds.height,
                        labels = tickPassSec.yLabels(ySecSpec),
                        textMeasurements = input.textMeasurements,
                        labelGapPx = yGapSecPre,
                    )
                val yValsDenseSec =
                    TickDensityReducer.reduce(
                        tickPassSec.yValues,
                        frame0.plotBounds.height,
                        ySecSpec,
                        minimumCapFromMeasuredLabelWidth = yMinCapSec,
                    )
                val yTicksRawSec =
                    buildRightYTicks(
                        frame0.plotBounds,
                        secVp,
                        yValsDenseSec,
                        input.textMeasurements,
                        ySecSpec,
                        labelLayoutScale = labelScale,
                    )
                val yGapSec = (ySecSpec.minVerticalTickLabelGapPx ?: Y_TICK_LABEL_MIN_VERTICAL_GAP) * labelScale
                val edgeMargin = TICK_LABEL_CANVAS_EDGE_MARGIN * labelScale
                plannerFinalizeYTicksForCanvas(
                    yTicksRawSec,
                    yGapSec,
                    frame0.canvasBounds,
                    edgeMargin,
                ).map { it.label }
            } else {
                emptyList()
            }

        val frame =
            if (needsSecondary && reserveRightYAxisSpace && ySecSpec != null && ySecSpec.enabled && rightLabelsForReserve.isNotEmpty()) {
                layoutCartesianFrame(
                    input = input,
                    xAxis = spec.xAxis,
                    yAxis = spec.yAxis,
                    legend = spec.legend,
                    contentPadding = spec.contentPadding,
                    legendLabelColors = combinedLegendLabels(spec),
                    rightAxisTickLabels = rightLabelsForReserve,
                    reserveRightYAxisSpace = true,
                    tickValuePass = primaryTickPass,
                )
            } else {
                frame0
            }

        val secVpFinal = secondaryViewport
        val yTicksSecondary =
            if (needsSecondary && ySecSpec != null && secVpFinal != null) {
                val tickPassSec =
                    TickValuePlanner.compute(
                        ChartViewport(frame.viewport.xRange, secVpFinal.yRange),
                        spec.xAxis,
                        ySecSpec,
                    )
                val yGapSecPre = (ySecSpec.minVerticalTickLabelGapPx ?: Y_TICK_LABEL_MIN_VERTICAL_GAP) * labelScale
                val yMinCapSec =
                    plannerMinimumCapFromMeasuredLabelWidth(
                        axis = ySecSpec,
                        axisSpanPx = frame.plotBounds.height,
                        labels = tickPassSec.yLabels(ySecSpec),
                        textMeasurements = input.textMeasurements,
                        labelGapPx = yGapSecPre,
                    )
                val yValsDenseSec =
                    TickDensityReducer.reduce(
                        tickPassSec.yValues,
                        frame.plotBounds.height,
                        ySecSpec,
                        minimumCapFromMeasuredLabelWidth = yMinCapSec,
                    )
                val yTicksRawSec =
                    buildRightYTicks(
                        frame.plotBounds,
                        secVpFinal,
                        yValsDenseSec,
                        input.textMeasurements,
                        ySecSpec,
                        labelLayoutScale = labelScale,
                    )
                val yGapSec = (ySecSpec.minVerticalTickLabelGapPx ?: Y_TICK_LABEL_MIN_VERTICAL_GAP) * labelScale
                val edgeMargin = TICK_LABEL_CANVAS_EDGE_MARGIN * labelScale
                plannerFinalizeYTicksForCanvas(
                    yTicksRawSec,
                    yGapSec,
                    frame.canvasBounds,
                    edgeMargin,
                )
            } else {
                emptyList()
            }

        val transformPrimary = ChartTransform.forPlotBounds(frame.viewport, frame.plotBounds)
        val transformSecondary =
            secVpFinal?.let {
                ChartTransform.forPlotBounds(
                    ChartViewport(frame.viewport.xRange, it.yRange),
                    frame.plotBounds,
                )
            }

        fun transformFor(binding: CombinedCartesianYAxisBinding): ChartTransform =
            when (binding) {
                CombinedCartesianYAxisBinding.SharedY -> transformPrimary
                CombinedCartesianYAxisBinding.SecondaryY -> transformSecondary ?: transformPrimary
            }

        fun viewportFor(binding: CombinedCartesianYAxisBinding): ChartViewport =
            when (binding) {
                CombinedCartesianYAxisBinding.SharedY -> frame.viewport
                CombinedCartesianYAxisBinding.SecondaryY ->
                    secVpFinal?.let { ChartViewport(frame.viewport.xRange, it.yRange) } ?: frame.viewport
            }

        val plotCommands = mutableListOf<DrawCommand>()
        if (spec.xAxis.showGridLines || spec.yAxis.showGridLines) {
            plotCommands +=
                buildGridLines(
                    plotBounds = frame.plotBounds,
                    xTicks = if (spec.xAxis.showGridLines) frame.xTicks else emptyList(),
                    yTicks = if (spec.yAxis.showGridLines) frame.yTicks else emptyList(),
                    lineStyle = spec.grid.lineStyle,
                )
        }

        val hitLayers = mutableListOf<CombinedHitTestLayer>()
        val allBarSegments = mutableListOf<PlannedBarSegment>()

        for (layer in spec.layers) {
            when (layer) {
                is CombinedCartesianLayer.Line -> {
                    val t = transformFor(layer.yAxisBinding)
                    val vp = viewportFor(layer.yAxisBinding)
                    plotCommands +=
                        layer.series.flatMap {
                            buildLineSeriesCommands(it, vp, t, frame.plotBounds, spec.lineRender)
                        }
                    hitLayers += CombinedHitTestLayer.Lines(layer.id, layer.yAxisBinding, layer.series)
                }
                is CombinedCartesianLayer.Scatter -> {
                    val t = transformFor(layer.yAxisBinding)
                    val vp = viewportFor(layer.yAxisBinding)
                    plotCommands +=
                        layer.series.flatMap {
                            buildScatterSeriesCommands(
                                it,
                                vp,
                                t,
                                frame.plotBounds,
                                spec.scatterRender,
                            )
                        }
                    hitLayers += CombinedHitTestLayer.Scatters(layer.id, layer.yAxisBinding, layer.series)
                }
                is CombinedCartesianLayer.Bar -> {
                    val t = transformFor(layer.yAxisBinding)
                    val vp = viewportFor(layer.yAxisBinding)
                    val effectiveLayout =
                        if (layer.barLayout == BarLayoutMode.Clustered || layer.barLayout == BarLayoutMode.Stacked) {
                            if (layer.groups.isEmpty()) {
                                BarLayoutMode.Independent
                            } else {
                                val n = layer.groups.first().entries.size
                                if (layer.groups.all { it.entries.size == n }) layer.barLayout else BarLayoutMode.Independent
                            }
                        } else {
                            layer.barLayout
                        }
                    val segs =
                        computePlannedBarSegments(
                            groups = layer.groups,
                            layoutMode = effectiveLayout,
                            clusterBarGapPx = layer.clusterBarGapPx,
                            orientation = layer.orientation,
                            viewport = vp,
                            transform = t,
                            plotBounds = frame.plotBounds,
                            layerId = layer.id,
                        )
                    allBarSegments += segs
                    plotCommands +=
                        buildBarDrawCommands(
                            segments = segs,
                            selection = selection,
                            selectionStyle = spec.selectionStyle,
                        )
                    hitLayers += CombinedHitTestLayer.Bars(layer.id, layer.yAxisBinding, segs)
                }
            }
        }

        plotCommands +=
            appendCombinedSelectionNonBar(
                plotBounds = frame.plotBounds,
                selection = selection,
                spec = spec,
                allBarSegments = allBarSegments,
            )

        val overlayPrimary =
            buildAxisAndTickLabelsOverlay(
                frame.plotBounds,
                spec.xAxis,
                spec.yAxis,
                frame.xTicks,
                frame.yTicks,
            )
        val overlaySecondary =
            if (ySecSpec != null && yTicksSecondary.isNotEmpty()) {
                buildRightAxisOverlay(frame.plotBounds, ySecSpec, yTicksSecondary)
            } else {
                emptyList()
            }

        return CombinedCartesianChartPlan(
            layout =
                ChartLayoutPlan(
                    canvasBounds = frame.canvasBounds,
                    plotBounds = frame.plotBounds,
                    reservedRegions = frame.reservedRegions,
                    xTicks = frame.xTicks,
                    yTicks = frame.yTicks,
                    legendEntries = frame.legendEntries,
                    plotDrawCommands = plotCommands,
                    overlayDrawCommands = overlayPrimary + overlaySecondary,
                ),
            selection = selection,
            yTicksSecondary = yTicksSecondary,
            secondaryViewport = secVpFinal,
            allBarSegments = allBarSegments,
            hitTestLayers = hitLayers,
        )
    }

    private fun appendCombinedSelectionNonBar(
        plotBounds: RectD,
        selection: ChartSelection?,
        spec: CombinedCartesianChartSpec,
        allBarSegments: List<PlannedBarSegment>,
    ): List<DrawCommand> {
        if (selection == null) return emptyList()
        if (allBarSegments.any { selectionMatchesSegment(selection, it) }) {
            return emptyList()
        }
        return buildLineStyleSelectionOverlay(plotBounds, selection, spec.selectionStyle)
    }

    private fun buildLineStyleSelectionOverlay(
        plotBounds: RectD,
        selection: ChartSelection,
        selectionStyle: SelectionStyle,
    ): List<DrawCommand> {
        return listOf(
            DrawCommand.Line(
                start = OffsetD(selection.pixelOffset.x, plotBounds.top),
                end = OffsetD(selection.pixelOffset.x, plotBounds.bottom),
                color = selectionStyle.lineColor,
                strokeWidth = selectionStyle.lineStrokeWidth,
            ),
            DrawCommand.Circle(
                center = selection.pixelOffset,
                radius = selectionStyle.pointRadius,
                color = selectionStyle.pointColor,
            ),
        )
    }

    private fun combinedLegendLabels(spec: CombinedCartesianChartSpec): List<Pair<String, ChartColor>> {
        val out = ArrayList<Pair<String, ChartColor>>()
        for (layer in spec.layers) {
            when (layer) {
                is CombinedCartesianLayer.Line ->
                    layer.series.forEach { out += it.label to it.lineStyle.color }
                is CombinedCartesianLayer.Bar ->
                    layer.groups.forEach { out += it.label to it.style.color }
                is CombinedCartesianLayer.Scatter ->
                    layer.series.forEach { out += it.label to it.resolvedPointColor() }
            }
        }
        return out
    }

    private fun combinedSecondaryViewport(
        primaryViewport: ChartViewport,
        spec: CombinedCartesianChartSpec,
    ): ChartViewport {
        val ys = ArrayList<Double>()
        for (layer in spec.layers) {
            if (layer.yAxisBinding != CombinedCartesianYAxisBinding.SecondaryY) continue
            when (layer) {
                is CombinedCartesianLayer.Line ->
                    layer.series.forEach { s -> s.points.forEach { ys += it.y } }
                is CombinedCartesianLayer.Bar ->
                    layer.groups.forEach { g -> g.entries.forEach { ys += it.y } }
                is CombinedCartesianLayer.Scatter ->
                    layer.series.forEach { s -> s.points.forEach { ys += it.y } }
            }
        }
        if (ys.isEmpty()) {
            return ChartViewport(
                xRange = primaryViewport.xRange,
                yRange = primaryViewport.yRange,
            )
        }
        val minY = ys.minOrNull() ?: 0.0
        var maxY = ys.maxOrNull() ?: 1.0
        if (maxY == minY) {
            maxY = minY + FpMath.DEFAULT_DEGENERATE_PAD
        }
        val hasBar =
            spec.layers.any {
                it is CombinedCartesianLayer.Bar && it.yAxisBinding == CombinedCartesianYAxisBinding.SecondaryY
            }
        val yLow = if (hasBar) minOf(0.0, minY) else minY
        return ChartViewport(
            xRange = primaryViewport.xRange,
            yRange = ClosedRangeD(yLow, maxY),
        )
    }
}
