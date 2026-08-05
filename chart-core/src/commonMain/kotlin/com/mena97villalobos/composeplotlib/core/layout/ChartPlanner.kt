package com.mena97villalobos.composeplotlib.core.layout

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.geometry.InsetsD
import com.mena97villalobos.composeplotlib.core.geometry.OffsetD
import com.mena97villalobos.composeplotlib.core.geometry.PolylineRectClip
import com.mena97villalobos.composeplotlib.core.geometry.RectD
import com.mena97villalobos.composeplotlib.core.layout.ChartPlanner.computeCartesianTickValuePass
import com.mena97villalobos.composeplotlib.core.layout.draw.PathDrawStyle
import com.mena97villalobos.composeplotlib.core.layout.draw.PathSegment
import com.mena97villalobos.composeplotlib.core.layout.draw.ShapeDrawStyle
import com.mena97villalobos.composeplotlib.core.layout.draw.StrokeCap
import com.mena97villalobos.composeplotlib.core.layout.draw.StrokeStyle
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
import com.mena97villalobos.composeplotlib.core.model.OHLCPoint
import com.mena97villalobos.composeplotlib.core.model.ScatterSeries
import com.mena97villalobos.composeplotlib.core.model.heikinAshi
import com.mena97villalobos.composeplotlib.core.model.toCloseXyPoints
import com.mena97villalobos.composeplotlib.core.range.ClosedRangeD
import com.mena97villalobos.composeplotlib.core.selection.ChartSelection
import com.mena97villalobos.composeplotlib.core.selection.SankeyHitResolver
import com.mena97villalobos.composeplotlib.core.selection.WaterfallHitResolver
import com.mena97villalobos.composeplotlib.core.spec.AreaFillMode
import com.mena97villalobos.composeplotlib.core.spec.AreaStackOffset
import com.mena97villalobos.composeplotlib.core.spec.AxisScale
import com.mena97villalobos.composeplotlib.core.spec.AxisSpec
import com.mena97villalobos.composeplotlib.core.spec.BarChartOrientation
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.BarLayoutMode
import com.mena97villalobos.composeplotlib.core.spec.BarStackMode
import com.mena97villalobos.composeplotlib.core.spec.BoxPlotChartSpec
import com.mena97villalobos.composeplotlib.core.spec.BulletChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CalendarHeatmapSpec
import com.mena97villalobos.composeplotlib.core.spec.CandlestickChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CandlestickVariant
import com.mena97villalobos.composeplotlib.core.spec.ChartPalettes
import com.mena97villalobos.composeplotlib.core.spec.CirclePackChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianChartSpec
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianLayer
import com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianYAxisBinding
import com.mena97villalobos.composeplotlib.core.spec.FunnelChartSpec
import com.mena97villalobos.composeplotlib.core.spec.GaugeChartSpec
import com.mena97villalobos.composeplotlib.core.spec.GridLineStyle
import com.mena97villalobos.composeplotlib.core.spec.HeatmapChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LegendSpec
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec
import com.mena97villalobos.composeplotlib.core.spec.LineInterpolation
import com.mena97villalobos.composeplotlib.core.spec.LineRenderSpec
import com.mena97villalobos.composeplotlib.core.spec.LineStyle
import com.mena97villalobos.composeplotlib.core.spec.MarkerShape
import com.mena97villalobos.composeplotlib.core.spec.ParallelAxis
import com.mena97villalobos.composeplotlib.core.spec.ParallelCoordinatesChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PieChartSpec
import com.mena97villalobos.composeplotlib.core.spec.PolarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.RadarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.RadialBarChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ReferenceLine
import com.mena97villalobos.composeplotlib.core.spec.ReferenceOrientation
import com.mena97villalobos.composeplotlib.core.spec.SankeyChartSpec
import com.mena97villalobos.composeplotlib.core.spec.SankeyColumnAlignment
import com.mena97villalobos.composeplotlib.core.spec.SankeyLink
import com.mena97villalobos.composeplotlib.core.spec.SankeyLinkColorMode
import com.mena97villalobos.composeplotlib.core.spec.SankeyLinkOrder
import com.mena97villalobos.composeplotlib.core.spec.SankeyNodeLabelOrientation
import com.mena97villalobos.composeplotlib.core.spec.SankeyNodeLabelPlacement
import com.mena97villalobos.composeplotlib.core.spec.SankeyNodeOrder
import com.mena97villalobos.composeplotlib.core.spec.SankeyOrientation
import com.mena97villalobos.composeplotlib.core.spec.ScatterChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ScatterRenderSpec
import com.mena97villalobos.composeplotlib.core.spec.SelectionStyle
import com.mena97villalobos.composeplotlib.core.spec.StackedAreaChartSpec
import com.mena97villalobos.composeplotlib.core.spec.SunburstChartSpec
import com.mena97villalobos.composeplotlib.core.spec.TreemapChartSpec
import com.mena97villalobos.composeplotlib.core.spec.ViolinChartSpec
import com.mena97villalobos.composeplotlib.core.spec.WaterfallChartSpec
import com.mena97villalobos.composeplotlib.core.spec.clampedProgress
import com.mena97villalobos.composeplotlib.core.spec.formatSliceLabel
import com.mena97villalobos.composeplotlib.core.spec.hasDrawableSweep
import com.mena97villalobos.composeplotlib.core.spec.resolvedColumnAlignment
import com.mena97villalobos.composeplotlib.core.spec.resolvedMaxValue
import com.mena97villalobos.composeplotlib.core.spec.resolvedWeekCount
import com.mena97villalobos.composeplotlib.core.stats.kernelDensity
import com.mena97villalobos.composeplotlib.core.transform.ChartTransform
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import com.mena97villalobos.composeplotlib.core.visible.LineRenderPipeline
import com.mena97villalobos.composeplotlib.core.visible.ScatterRenderPipeline
import com.mena97villalobos.composeplotlib.core.visible.VisibleRangeCalculator
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

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
    /** Minimum half-width (px) of a candle's tappable column so thin / doji candles stay selectable. */
    private const val CANDLE_MIN_TAP_HALF_WIDTH: Double = 8.0

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
        legendSeriesIds: List<String?> = emptyList(),
        hiddenSeriesIds: Set<String> = emptySet(),
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
            legendSeriesIds = legendSeriesIds,
            hiddenSeriesIds = hiddenSeriesIds,
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
                legendSeriesIds = spec.series.map { it.id },
                hiddenSeriesIds = spec.hiddenSeriesIds,
            )

        val visibleSeries = spec.series.filter { it.id !in spec.hiddenSeriesIds }
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
            visibleSeries.flatMap {
                buildLineSeriesCommands(it, frame.viewport, frame.transform, frame.plotBounds, spec.lineRender)
            }

        if (spec.errorBars.enabled) {
            val anchors = ArrayList<ErrorBarAnchor>()
            for (series in visibleSeries) {
                for (p in series.points) {
                    if (p.isGap || !p.x.isFinite() || !p.y.isFinite() || (p.yLow == null && p.yHigh == null)) continue
                    val center = frame.transform.toPixel(XyPoint(p.x, p.y))
                    val lowPx = p.yLow?.let { frame.transform.toPixel(XyPoint(p.x, it)).y }
                    val highPx = p.yHigh?.let { frame.transform.toPixel(XyPoint(p.x, it)).y }
                    anchors.add(ErrorBarAnchor(center.x, center.y, lowPx, highPx))
                }
            }
            plotCommands += buildErrorBarCommands(anchors, spec.errorBars, frame.plotBounds)
        }
        if (spec.trendlines.isNotEmpty()) {
            plotCommands +=
                buildTrendlineCommands(
                    trendlines = spec.trendlines,
                    seriesPoints = visibleSeries.associate { it.id to it.toXyPoints() },
                    orderedSeriesIds = visibleSeries.map { it.id },
                    transform = frame.transform,
                    plotBounds = frame.plotBounds,
                )
        }

        if (spec.dataLabels.enabled) {
            val (charW, lineH) = estimateLabelCharBox(input)
            val anchors = ArrayList<DataLabelAnchor>()
            for (series in visibleSeries) {
                val half = if (series.pointStyle.visible) series.pointStyle.radius.coerceAtLeast(0.0) else 0.0
                for (p in series.points) {
                    if (!p.x.isFinite() || !p.y.isFinite()) continue
                    val pixel = frame.transform.toPixel(XyPoint(p.x, p.y))
                    if (!frame.plotBounds.contains(pixel)) continue
                    anchors.add(DataLabelAnchor(pixel, spec.dataLabels.valueFormatter(p.y), half))
                }
            }
            plotCommands += buildDataLabelCommands(anchors, spec.dataLabels, frame.plotBounds, charW, lineH)
        }

        plotCommands += buildReferenceLineCommands(spec.referenceLines, frame.transform, frame.plotBounds)
        if (spec.annotations.isNotEmpty()) {
            val (annCharW, annLineH) = estimateLabelCharBox(input)
            plotCommands += buildAnnotationCommands(spec.annotations, frame.transform, frame.plotBounds, annCharW, annLineH)
        }

        if (selection != null) {
            plotCommands +=
                if (spec.crosshair.enabled) {
                    val (chW, chH) = estimateLabelCharBox(input)
                    buildCrosshairCommands(
                        selection = selection,
                        crosshair = spec.crosshair,
                        selectionStyle = spec.selectionStyle,
                        transform = frame.transform,
                        plotBounds = frame.plotBounds,
                        xLabelFormatter = spec.xAxis.labelFormatter,
                        yLabelFormatter = spec.yAxis.labelFormatter,
                        charWidthPx = chW,
                        lineHeightPx = chH,
                    )
                } else {
                    buildLineStyleSelectionOverlay(
                        frame.plotBounds,
                        frame.transform.toPixel(selection.target.point),
                        spec.selectionStyle,
                    )
                }
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
                legendSeriesIds = spec.series.map { it.id },
                hiddenSeriesIds = spec.hiddenSeriesIds,
            )

        val visibleSeries = spec.series.filter { it.id !in spec.hiddenSeriesIds }
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
            visibleSeries.flatMap {
                buildScatterSeriesCommands(
                    it,
                    frame.viewport,
                    frame.transform,
                    frame.plotBounds,
                    spec.scatterRender,
                )
            }

        if (spec.errorBars.enabled) {
            val anchors = ArrayList<ErrorBarAnchor>()
            for (series in visibleSeries) {
                for (p in series.points) {
                    if (p.isGap || !p.x.isFinite() || !p.y.isFinite() || (p.yLow == null && p.yHigh == null)) continue
                    val center = frame.transform.toPixel(XyPoint(p.x, p.y))
                    val lowPx = p.yLow?.let { frame.transform.toPixel(XyPoint(p.x, it)).y }
                    val highPx = p.yHigh?.let { frame.transform.toPixel(XyPoint(p.x, it)).y }
                    anchors.add(ErrorBarAnchor(center.x, center.y, lowPx, highPx))
                }
            }
            plotCommands += buildErrorBarCommands(anchors, spec.errorBars, frame.plotBounds)
        }
        if (spec.trendlines.isNotEmpty()) {
            plotCommands +=
                buildTrendlineCommands(
                    trendlines = spec.trendlines,
                    seriesPoints = visibleSeries.associate { it.id to it.toXyPoints() },
                    orderedSeriesIds = visibleSeries.map { it.id },
                    transform = frame.transform,
                    plotBounds = frame.plotBounds,
                )
        }

        if (spec.dataLabels.enabled) {
            val (charW, lineH) = estimateLabelCharBox(input)
            val anchors = ArrayList<DataLabelAnchor>()
            for (series in visibleSeries) {
                for (p in series.points) {
                    if (!p.x.isFinite() || !p.y.isFinite()) continue
                    val pixel = frame.transform.toPixel(XyPoint(p.x, p.y))
                    if (!frame.plotBounds.contains(pixel)) continue
                    val half = (p.markerRadius ?: series.pointStyle.radius).coerceAtLeast(0.0)
                    anchors.add(DataLabelAnchor(pixel, spec.dataLabels.valueFormatter(p.y), half))
                }
            }
            plotCommands += buildDataLabelCommands(anchors, spec.dataLabels, frame.plotBounds, charW, lineH)
        }

        plotCommands += buildReferenceLineCommands(spec.referenceLines, frame.transform, frame.plotBounds)
        if (spec.annotations.isNotEmpty()) {
            val (annCharW, annLineH) = estimateLabelCharBox(input)
            plotCommands += buildAnnotationCommands(spec.annotations, frame.transform, frame.plotBounds, annCharW, annLineH)
        }

        if (selection != null) {
            plotCommands +=
                if (spec.crosshair.enabled) {
                    val (chW, chH) = estimateLabelCharBox(input)
                    buildCrosshairCommands(
                        selection = selection,
                        crosshair = spec.crosshair,
                        selectionStyle = spec.selectionStyle,
                        transform = frame.transform,
                        plotBounds = frame.plotBounds,
                        xLabelFormatter = spec.xAxis.labelFormatter,
                        yLabelFormatter = spec.yAxis.labelFormatter,
                        charWidthPx = chW,
                        lineHeightPx = chH,
                    )
                } else {
                    buildLineStyleSelectionOverlay(
                        frame.plotBounds,
                        frame.transform.toPixel(selection.target.point),
                        spec.selectionStyle,
                    )
                }
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
     * @param selection optional slice highlight: matches when `selection.target.seriesId` equals [PieChartSpec.id]
     * and `selection.target.pointIndex` is the slice index in [PieChartSpec.slices]. A translucent overlay arc
     * is appended for the selected slice when its `value` is positive.
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
     * Gauge / progress-ring chart: a background track arc plus a foreground progress arc (both stroked, no fill),
     * centered in the (square-ish) plot. No ticks, legend, or selection. [GaugeChartSpec.progress] is clamped to
     * `0.0..1.0`; the ring radius is inset by half the stroke so the stroke stays inside the plot.
     */
    fun planGaugeChart(
        input: ChartLayoutInput,
        spec: GaugeChartSpec,
    ): GaugeChartPlan {
        val labelScale = input.labelLayoutScale.normalizedLabelLayoutScale()
        val bounds = input.toPlanningBounds()
        val reservedRegions =
            buildReservedRegions(
                canvasBounds = bounds.canvasBounds,
                minimumInsets = spec.contentPadding,
                xTickLabels = emptyList(),
                yTickLabels = emptyList(),
                legendEntries = emptyList(),
                textMeasurements = input.textMeasurements,
                xAxisEnabled = false,
                yAxisEnabled = false,
                labelLayoutScale = labelScale,
            )
        val plotBounds = bounds.canvasBounds.inset(reservedRegions.contentInsets)
        val radial = PieChartLayoutMath.layout(plotBounds, holeRadiusRatio = null)
        val ringRadius = (radial.outerRadius - spec.strokeWidthPx / 2.0).coerceAtLeast(0.0)

        val plotCommands = mutableListOf<DrawCommand>()
        if (ringRadius > 0.0 && spec.hasDrawableSweep()) {
            plotCommands +=
                DrawCommand.Arc(
                    center = radial.center,
                    radius = ringRadius,
                    startAngleDegrees = spec.startAngleDegrees,
                    sweepAngleDegrees = spec.sweepDegrees,
                    useCenter = false,
                    style = ShapeDrawStyle.Stroke(spec.trackColor, spec.strokeWidthPx),
                    innerRadius = null,
                )
            val progressSweep = spec.sweepDegrees * spec.clampedProgress()
            if (progressSweep != 0.0) {
                val cap = if (spec.roundedCaps) StrokeStyle(cap = StrokeCap.Round) else null
                plotCommands +=
                    DrawCommand.Arc(
                        center = radial.center,
                        radius = ringRadius,
                        startAngleDegrees = spec.startAngleDegrees,
                        sweepAngleDegrees = progressSweep,
                        useCenter = false,
                        style = ShapeDrawStyle.Stroke(spec.progressColor, spec.strokeWidthPx, cap),
                        innerRadius = null,
                    )
            }
        }

        return GaugeChartPlan(
            layout =
                ChartLayoutPlan(
                    canvasBounds = bounds.canvasBounds,
                    plotBounds = plotBounds,
                    reservedRegions = reservedRegions,
                    xTicks = emptyList(),
                    yTicks = emptyList(),
                    legendEntries = emptyList(),
                    plotDrawCommands = plotCommands,
                    overlayDrawCommands = emptyList(),
                ),
        )
    }

    /**
     * Radial bar (circular progress-bar): one concentric ring per [RadialBarChartSpec.tracks] entry, outermost
     * first. Each ring draws a background track arc plus a foreground value arc from
     * [RadialBarChartSpec.startAngleDegrees] spanning `sweepDegrees * value / maxValue`. Read-only; optional legend.
     */
    fun planRadialBarChart(
        input: ChartLayoutInput,
        spec: RadialBarChartSpec,
    ): RadialBarChartPlan {
        val labelScale = input.labelLayoutScale.normalizedLabelLayoutScale()
        val bounds = input.toPlanningBounds()
        val legendEntries =
            buildLegendEntries(
                labels = spec.tracks.map { it.label to it.color },
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
        val radial = PieChartLayoutMath.layout(plotBounds, holeRadiusRatio = null)
        val outerRadius = radial.outerRadius
        val maxValue = spec.resolvedMaxValue()
        val ringStep = spec.trackThicknessPx + spec.trackGapPx

        val plotCommands = mutableListOf<DrawCommand>()
        if (outerRadius > 0.0 && spec.hasDrawableSweep()) {
            spec.tracks.forEachIndexed { index, track ->
                val ringRadius = outerRadius - (spec.trackThicknessPx / 2.0) - index * ringStep
                if (ringRadius <= 0.0) return@forEachIndexed
                plotCommands +=
                    DrawCommand.Arc(
                        center = radial.center,
                        radius = ringRadius,
                        startAngleDegrees = spec.startAngleDegrees,
                        sweepAngleDegrees = spec.sweepDegrees,
                        useCenter = false,
                        style = ShapeDrawStyle.Stroke(spec.backgroundTrackColor, spec.trackThicknessPx),
                        innerRadius = null,
                    )
                val fraction = (track.value / maxValue).coerceIn(0.0, 1.0)
                val valueSweep = spec.sweepDegrees * fraction
                if (valueSweep != 0.0) {
                    val cap = if (spec.roundedCaps) StrokeStyle(cap = StrokeCap.Round) else null
                    plotCommands +=
                        DrawCommand.Arc(
                            center = radial.center,
                            radius = ringRadius,
                            startAngleDegrees = spec.startAngleDegrees,
                            sweepAngleDegrees = valueSweep,
                            useCenter = false,
                            style = ShapeDrawStyle.Stroke(track.color, spec.trackThicknessPx, cap),
                            innerRadius = null,
                        )
                }
            }
        }

        return RadialBarChartPlan(
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
        )
    }

    /**
     * Calendar / year heatmap: places each [CalendarHeatmapDay][com.mena97villalobos.composeplotlib.core.spec.CalendarHeatmapDay]
     * in a week-column × weekday-row grid (`column = (dayIndex + startWeekday) / 7`, `row = … % 7`, row 0 at top),
     * coloring cells by value via the [CalendarHeatmapSpec.colorScale]. Weekday and month labels sit in the reserved
     * padding. Read-only (no selection).
     */
    fun planCalendarHeatmapChart(
        input: ChartLayoutInput,
        spec: CalendarHeatmapSpec,
    ): CalendarHeatmapChartPlan {
        val bounds = input.toPlanningBounds()
        val plotBounds = bounds.canvasBounds.inset(spec.contentPadding)
        val weeks = spec.resolvedWeekCount()
        val plotCommands = mutableListOf<DrawCommand>()
        val overlayCommands = mutableListOf<DrawCommand>()
        if (weeks > 0 && plotBounds.width > 0.0 && plotBounds.height > 0.0) {
            val cellWidth = plotBounds.width / weeks
            val cellHeight = plotBounds.height / 7.0
            val gap = spec.cellGapPx.coerceAtLeast(0.0)

            fun cell(
                column: Int,
                row: Int,
                color: ChartColor,
            ): DrawCommand {
                val left = plotBounds.left + column * cellWidth + gap / 2.0
                val top = plotBounds.top + row * cellHeight + gap / 2.0
                val right = plotBounds.left + (column + 1) * cellWidth - gap / 2.0
                val bottom = plotBounds.top + (row + 1) * cellHeight - gap / 2.0
                return DrawCommand.RoundRect(RectD(left, top, right, bottom), spec.cellCornerRadiusPx, ShapeDrawStyle.Fill(color))
            }

            for (column in 0 until weeks) {
                for (row in 0 until 7) {
                    plotCommands += cell(column, row, spec.emptyCellColor)
                }
            }
            for (day in spec.days) {
                val position = day.dayIndex + spec.startWeekday
                if (position < 0) continue
                val column = position / 7
                val row = position % 7
                if (column >= weeks) continue
                plotCommands += cell(column, row, spec.colorScale.colorFor(day.value))
            }

            val labelColor = ChartColor(0xFF57606A)
            spec.weekdayLabels.take(7).forEachIndexed { row, label ->
                if (label.isNotEmpty()) {
                    val centerY = plotBounds.top + row * cellHeight + cellHeight / 2.0
                    overlayCommands += DrawCommand.Text(text = label, position = OffsetD(bounds.canvasBounds.left + 2.0, centerY - 6.0), color = labelColor)
                }
            }
            spec.monthLabels.forEach { month ->
                if (month.weekIndex in 0 until weeks) {
                    val x = plotBounds.left + month.weekIndex * cellWidth
                    overlayCommands += DrawCommand.Text(text = month.label, position = OffsetD(x, plotBounds.top - 14.0), color = labelColor)
                }
            }
        }

        return CalendarHeatmapChartPlan(
            layout =
                ChartLayoutPlan(
                    canvasBounds = bounds.canvasBounds,
                    plotBounds = plotBounds,
                    reservedRegions = ChartReservedRegions(spec.contentPadding, null, null, null),
                    xTicks = emptyList(),
                    yTicks = emptyList(),
                    legendEntries = emptyList(),
                    plotDrawCommands = plotCommands,
                    overlayDrawCommands = overlayCommands,
                ),
        )
    }

    /**
     * Sunburst (hierarchical radial): draws each [sunburstLayout] slice as an annulus sector (or a center disk for
     * the root) in its ring band, angular span proportional to value within its parent. Read-only (no selection).
     */
    fun planSunburstChart(
        input: ChartLayoutInput,
        spec: SunburstChartSpec,
    ): SunburstChartPlan {
        val bounds = input.toPlanningBounds()
        val plotBounds = bounds.canvasBounds.inset(spec.contentPadding)
        val radial = PieChartLayoutMath.layout(plotBounds, holeRadiusRatio = null)
        val slices = sunburstLayout(spec)
        val maxDepth = slices.maxOfOrNull { it.depth } ?: 0
        val ringCount = maxDepth + 1
        val bandWidth = if (ringCount > 0) radial.outerRadius / ringCount else 0.0
        val gap = spec.ringGapPx.coerceAtLeast(0.0)

        val plotCommands = mutableListOf<DrawCommand>()
        if (radial.outerRadius > 0.0 && bandWidth > 0.0) {
            for (slice in slices) {
                if (slice.sweepAngleDegrees == 0.0) continue
                val outer = ((slice.depth + 1) * bandWidth - gap / 2.0).coerceAtLeast(0.0)
                if (slice.depth == 0) {
                    plotCommands +=
                        DrawCommand.Arc(
                            center = radial.center,
                            radius = outer,
                            startAngleDegrees = slice.startAngleDegrees,
                            sweepAngleDegrees = slice.sweepAngleDegrees,
                            useCenter = true,
                            style = ShapeDrawStyle.Fill(slice.color),
                            innerRadius = null,
                        )
                } else {
                    val inner = (slice.depth * bandWidth + gap / 2.0).coerceIn(0.0, outer)
                    plotCommands +=
                        DrawCommand.Arc(
                            center = radial.center,
                            radius = outer,
                            startAngleDegrees = slice.startAngleDegrees,
                            sweepAngleDegrees = slice.sweepAngleDegrees,
                            useCenter = false,
                            style = ShapeDrawStyle.Fill(slice.color),
                            innerRadius = inner,
                        )
                }
            }
        }

        return SunburstChartPlan(
            layout =
                ChartLayoutPlan(
                    canvasBounds = bounds.canvasBounds,
                    plotBounds = plotBounds,
                    reservedRegions = ChartReservedRegions(spec.contentPadding, null, null, null),
                    xTicks = emptyList(),
                    yTicks = emptyList(),
                    legendEntries = emptyList(),
                    plotDrawCommands = plotCommands,
                    overlayDrawCommands = emptyList(),
                ),
        )
    }

    /**
     * Parallel coordinates: draws one vertical axis per [ParallelCoordinatesChartSpec.axes] entry and one polyline
     * per [ParallelCoordinatesChartSpec.series], connecting each series' value on every axis (each axis normalized to
     * its own `[min, max]`). Read-only (no selection / brushing in v1).
     */
    fun planParallelCoordinatesChart(
        input: ChartLayoutInput,
        spec: ParallelCoordinatesChartSpec,
    ): ParallelCoordinatesChartPlan {
        val labelScale = input.labelLayoutScale.normalizedLabelLayoutScale()
        val bounds = input.toPlanningBounds()
        val legendEntries =
            buildLegendEntries(
                labels = spec.series.map { it.label to it.color },
                canvasBounds = bounds.canvasBounds,
                minimumInsets = spec.contentPadding,
                textMeasurements = input.textMeasurements,
                legendSpec = spec.legend,
                labelLayoutScale = labelScale,
            )
        val reserved =
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
        val plotBounds = bounds.canvasBounds.inset(reserved.contentInsets)
        val axisCount = spec.axes.size
        val plotCommands = mutableListOf<DrawCommand>()
        val overlayCommands = mutableListOf<DrawCommand>()
        if (axisCount >= 1 && plotBounds.width > 0.0 && plotBounds.height > 0.0) {
            fun axisX(index: Int): Double = if (axisCount == 1) (plotBounds.left + plotBounds.right) / 2.0 else plotBounds.left + index * plotBounds.width / (axisCount - 1)

            fun yFor(
                axis: ParallelAxis,
                value: Double,
            ): Double {
                val span = axis.max - axis.min
                val fraction = if (span == 0.0) 0.5 else ((value - axis.min) / span).coerceIn(0.0, 1.0)
                return plotBounds.bottom - fraction * plotBounds.height
            }

            for (series in spec.series) {
                val points =
                    spec.axes.indices.mapNotNull { i ->
                        val value = series.values.getOrNull(i) ?: return@mapNotNull null
                        if (!value.isFinite()) return@mapNotNull null
                        OffsetD(axisX(i), yFor(spec.axes[i], value))
                    }
                if (points.size >= 2) {
                    plotCommands += DrawCommand.Polyline(points = points, color = series.color, strokeWidth = series.strokeWidth)
                }
            }
            spec.axes.forEachIndexed { i, axis ->
                val x = axisX(i)
                overlayCommands +=
                    DrawCommand.Line(start = OffsetD(x, plotBounds.top), end = OffsetD(x, plotBounds.bottom), color = spec.axisColor, strokeWidth = 1.0)
                if (axis.label.isNotEmpty()) {
                    overlayCommands +=
                        DrawCommand.Text(text = axis.label, position = OffsetD(x - axis.label.length * 3.0, plotBounds.bottom + 6.0), color = spec.axisLabelColor)
                }
            }
        }
        return ParallelCoordinatesChartPlan(
            layout =
                ChartLayoutPlan(
                    canvasBounds = bounds.canvasBounds,
                    plotBounds = plotBounds,
                    reservedRegions = reserved,
                    xTicks = emptyList(),
                    yTicks = emptyList(),
                    legendEntries = legendEntries,
                    plotDrawCommands = plotCommands,
                    overlayDrawCommands = overlayCommands,
                ),
        )
    }

    /**
     * Polar bar / wind-rose: [PolarChartSpec.sectors] divide the circle into equal angular wedges, each drawn from
     * the center with radius proportional to its value (`value / maxValue`). Optional concentric grid rings.
     * Read-only (no selection).
     */
    fun planPolarChart(
        input: ChartLayoutInput,
        spec: PolarChartSpec,
    ): PolarChartPlan {
        val bounds = input.toPlanningBounds()
        val plotBounds = bounds.canvasBounds.inset(spec.contentPadding)
        val radial = PieChartLayoutMath.layout(plotBounds, holeRadiusRatio = null)
        val outer = radial.outerRadius
        val count = spec.sectors.size
        val maxValue = spec.resolvedMaxValue()
        val plotCommands = mutableListOf<DrawCommand>()
        if (outer > 0.0 && count >= 1) {
            if (spec.showRings && spec.ringCount > 0) {
                for (ring in 1..spec.ringCount) {
                    val r = outer * ring / spec.ringCount
                    plotCommands +=
                        DrawCommand.Arc(
                            center = radial.center,
                            radius = r,
                            startAngleDegrees = 0.0,
                            sweepAngleDegrees = 360.0,
                            useCenter = false,
                            style = ShapeDrawStyle.Stroke(spec.ringColor, 1.0),
                            innerRadius = null,
                        )
                }
            }
            val sliceSweep = ((360.0 - spec.gapDegrees * count) / count).coerceAtLeast(0.0)
            var angle = spec.startAngleDegrees + spec.gapDegrees / 2.0
            for (sector in spec.sectors) {
                val fraction = (sector.value / maxValue).coerceIn(0.0, 1.0)
                val r = outer * fraction
                if (r > 0.0 && sliceSweep > 0.0) {
                    plotCommands +=
                        DrawCommand.Arc(
                            center = radial.center,
                            radius = r,
                            startAngleDegrees = angle,
                            sweepAngleDegrees = sliceSweep,
                            useCenter = true,
                            style = ShapeDrawStyle.Fill(sector.color),
                            innerRadius = null,
                        )
                }
                angle += sliceSweep + spec.gapDegrees
            }
        }
        return PolarChartPlan(
            layout =
                ChartLayoutPlan(
                    canvasBounds = bounds.canvasBounds,
                    plotBounds = plotBounds,
                    reservedRegions = ChartReservedRegions(spec.contentPadding, null, null, null),
                    xTicks = emptyList(),
                    yTicks = emptyList(),
                    legendEntries = emptyList(),
                    plotDrawCommands = plotCommands,
                    overlayDrawCommands = emptyList(),
                ),
        )
    }

    /**
     * Circle-packing / bubble-pack: packs [CirclePackChartSpec.nodes] as non-overlapping circles (area ∝ value) via
     * [circlePackLayout], fits them to the plot ([fitPackedCircles]), and emits a filled circle per node (plus a
     * label when it fits). Read-only (no selection).
     */
    fun planCirclePackChart(
        input: ChartLayoutInput,
        spec: CirclePackChartSpec,
    ): CirclePackChartPlan {
        val bounds = input.toPlanningBounds()
        val plotBounds = bounds.canvasBounds.inset(spec.contentPadding)
        val packed = fitPackedCircles(circlePackLayout(circlePackRadii(spec.nodes.map { it.value })), plotBounds)
        val plotCommands = mutableListOf<DrawCommand>()
        val overlayCommands = mutableListOf<DrawCommand>()
        spec.nodes.forEachIndexed { index, node ->
            val circle = packed.getOrNull(index) ?: return@forEachIndexed
            if (circle.radius <= 0.0) return@forEachIndexed
            plotCommands += DrawCommand.Circle(center = OffsetD(circle.centerX, circle.centerY), radius = circle.radius, color = node.color)
            if (spec.showLabels && node.label.isNotEmpty() && circle.radius > 18.0) {
                val estimatedHalfWidth = node.label.length * 3.5
                overlayCommands +=
                    DrawCommand.Text(
                        text = node.label,
                        position = OffsetD(circle.centerX - estimatedHalfWidth, circle.centerY - 7.0),
                        color = spec.labelColor,
                    )
            }
        }
        return CirclePackChartPlan(
            layout =
                ChartLayoutPlan(
                    canvasBounds = bounds.canvasBounds,
                    plotBounds = plotBounds,
                    reservedRegions = ChartReservedRegions(spec.contentPadding, null, null, null),
                    xTicks = emptyList(),
                    yTicks = emptyList(),
                    legendEntries = emptyList(),
                    plotDrawCommands = plotCommands,
                    overlayDrawCommands = overlayCommands,
                ),
        )
    }

    /**
     * Treemap: tiles [TreemapChartSpec.nodes] into area-proportional rectangles via [squarifiedTreemap] and emits a
     * filled [DrawCommand.RoundRect] per tile (plus a label when the tile is large enough). Read-only (no selection).
     */
    fun planTreemapChart(
        input: ChartLayoutInput,
        spec: TreemapChartSpec,
    ): TreemapChartPlan {
        val bounds = input.toPlanningBounds()
        val plotBounds = bounds.canvasBounds.inset(spec.contentPadding)
        val rects = squarifiedTreemap(spec.nodes.map { it.value }, plotBounds)
        val gap = spec.cellGapPx.coerceAtLeast(0.0)
        val plotCommands = mutableListOf<DrawCommand>()
        val overlayCommands = mutableListOf<DrawCommand>()
        spec.nodes.forEachIndexed { index, node ->
            val rect = rects[index]
            if (rect.width <= 0.0 || rect.height <= 0.0) return@forEachIndexed
            val left = rect.left + gap / 2.0
            val top = rect.top + gap / 2.0
            val right = (rect.right - gap / 2.0).coerceAtLeast(left)
            val bottom = (rect.bottom - gap / 2.0).coerceAtLeast(top)
            val tile = RectD(left, top, right, bottom)
            plotCommands += DrawCommand.RoundRect(tile, spec.cellCornerRadiusPx, ShapeDrawStyle.Fill(node.color))
            if (spec.showLabels && node.label.isNotEmpty() && tile.width > 34.0 && tile.height > 18.0) {
                overlayCommands += DrawCommand.Text(text = node.label, position = OffsetD(tile.left + 4.0, tile.top + 4.0), color = spec.labelColor)
            }
        }
        return TreemapChartPlan(
            layout =
                ChartLayoutPlan(
                    canvasBounds = bounds.canvasBounds,
                    plotBounds = plotBounds,
                    reservedRegions = ChartReservedRegions(spec.contentPadding, null, null, null),
                    xTicks = emptyList(),
                    yTicks = emptyList(),
                    legendEntries = emptyList(),
                    plotDrawCommands = plotCommands,
                    overlayDrawCommands = overlayCommands,
                ),
        )
    }

    /**
     * Bullet graph: horizontal qualitative range [bands][BulletChartSpec.bands] behind a featured measure bar
     * ([value][BulletChartSpec.value]) and an optional [target][BulletChartSpec.target] marker. Read-only (no
     * selection); optional min/max bound labels sit in the bottom padding.
     */
    fun planBulletChart(
        input: ChartLayoutInput,
        spec: BulletChartSpec,
    ): BulletChartPlan {
        val labelScale = input.labelLayoutScale.normalizedLabelLayoutScale()
        val bounds = input.toPlanningBounds()
        val reservedRegions =
            buildReservedRegions(
                canvasBounds = bounds.canvasBounds,
                minimumInsets = spec.contentPadding,
                xTickLabels = emptyList(),
                yTickLabels = emptyList(),
                legendEntries = emptyList(),
                textMeasurements = input.textMeasurements,
                xAxisEnabled = false,
                yAxisEnabled = false,
                labelLayoutScale = labelScale,
            )
        val plot = bounds.canvasBounds.inset(reservedRegions.contentInsets)
        val plotCommands = mutableListOf<DrawCommand>()
        val overlayCommands = mutableListOf<DrawCommand>()
        val span = spec.domainMax - spec.domainMin

        if (plot.width > 0.0 && plot.height > 0.0 && span > 0.0 && span.isFinite()) {
            fun toX(v: Double): Double = (plot.left + ((v - spec.domainMin) / span) * plot.width).coerceIn(plot.left, plot.right)

            var prev = spec.domainMin
            for (band in spec.bands) {
                if (!band.end.isFinite()) continue
                val x0 = toX(prev)
                val x1 = toX(band.end)
                if (x1 > x0) plotCommands += DrawCommand.Rect(RectD(x0, plot.top, x1, plot.bottom), band.color)
                prev = band.end
            }

            val measureThickness = plot.height * spec.measureThicknessFraction.coerceIn(0.05, 1.0)
            val measureTop = plot.top + (plot.height - measureThickness) / 2.0
            val valueX = toX(spec.value)
            if (valueX > plot.left) {
                plotCommands +=
                    DrawCommand.Rect(RectD(plot.left, measureTop, valueX, measureTop + measureThickness), spec.measureColor)
            }

            spec.target?.let { t ->
                if (t.isFinite()) {
                    val tx = toX(t)
                    plotCommands +=
                        DrawCommand.Line(
                            start = OffsetD(tx, plot.top),
                            end = OffsetD(tx, plot.bottom),
                            color = spec.targetColor,
                            strokeWidth = spec.targetThicknessPx,
                        )
                }
            }

            if (spec.showBoundLabels) {
                val (charW, _) = estimateLabelCharBox(input)
                overlayCommands +=
                    DrawCommand.Text(spec.valueFormatter(spec.domainMin), OffsetD(plot.left, plot.bottom + 4.0), ChartColor.Gray)
                val maxLabel = spec.valueFormatter(spec.domainMax)
                val maxWidth = charW.coerceAtLeast(1.0) * maxLabel.length
                overlayCommands +=
                    DrawCommand.Text(maxLabel, OffsetD((plot.right - maxWidth).coerceAtLeast(plot.left), plot.bottom + 4.0), ChartColor.Gray)
            }
        }

        return BulletChartPlan(
            layout =
                ChartLayoutPlan(
                    canvasBounds = bounds.canvasBounds,
                    plotBounds = plot,
                    reservedRegions = reservedRegions,
                    xTicks = emptyList(),
                    yTicks = emptyList(),
                    legendEntries = emptyList(),
                    plotDrawCommands = plotCommands,
                    overlayDrawCommands = overlayCommands,
                ),
        )
    }

    /**
     * Funnel chart: ordered [stages][FunnelChartSpec.stages] as stacked, centered trapezoids that taper from each
     * stage's width (proportional to its value) to the next stage's width. Read-only; optional centered stage labels
     * are emitted as overlay text.
     */
    fun planFunnelChart(
        input: ChartLayoutInput,
        spec: FunnelChartSpec,
    ): FunnelChartPlan {
        val labelScale = input.labelLayoutScale.normalizedLabelLayoutScale()
        val bounds = input.toPlanningBounds()
        val reservedRegions =
            buildReservedRegions(
                canvasBounds = bounds.canvasBounds,
                minimumInsets = spec.contentPadding,
                xTickLabels = emptyList(),
                yTickLabels = emptyList(),
                legendEntries = emptyList(),
                textMeasurements = input.textMeasurements,
                xAxisEnabled = false,
                yAxisEnabled = false,
                labelLayoutScale = labelScale,
            )
        val plot = bounds.canvasBounds.inset(reservedRegions.contentInsets)
        val plotCommands = mutableListOf<DrawCommand>()
        val overlayCommands = mutableListOf<DrawCommand>()
        val stages = spec.stages
        val maxValue = stages.maxOfOrNull { if (it.value.isFinite()) it.value else 0.0 } ?: 0.0

        if (plot.width > 0.0 && plot.height > 0.0 && stages.isNotEmpty() && maxValue > 0.0) {
            val n = stages.size
            val gap = spec.gapPx.coerceAtLeast(0.0)
            val bandHeight = ((plot.height - gap * (n - 1)) / n).coerceAtLeast(0.0)
            val cx = (plot.left + plot.right) / 2.0
            val (charW, lineH) = estimateLabelCharBox(input)

            fun widthFor(v: Double): Double = (if (v.isFinite()) (v / maxValue).coerceIn(0.0, 1.0) else 0.0) * plot.width

            for (i in stages.indices) {
                val stage = stages[i]
                val top = plot.top + i * (bandHeight + gap)
                val bottom = top + bandHeight
                val topHalf = widthFor(stage.value) / 2.0
                val bottomHalf = widthFor(stages.getOrNull(i + 1)?.value ?: stage.value) / 2.0
                val color = stage.color ?: ChartPalettes.cyclic(spec.palette, i)
                plotCommands +=
                    DrawCommand.Path(
                        segments =
                            listOf(
                                PathSegment.MoveTo(OffsetD(cx - topHalf, top)),
                                PathSegment.LineTo(OffsetD(cx + topHalf, top)),
                                PathSegment.LineTo(OffsetD(cx + bottomHalf, bottom)),
                                PathSegment.LineTo(OffsetD(cx - bottomHalf, bottom)),
                                PathSegment.Close,
                            ),
                        style = PathDrawStyle.Fill(color = color),
                    )
                if (spec.showLabels) {
                    val text = "${stage.label} ${spec.valueFormatter(stage.value)}"
                    val textWidth = charW.coerceAtLeast(1.0) * text.length
                    val midY = (top + bottom) / 2.0
                    overlayCommands +=
                        DrawCommand.Text(text, OffsetD(cx - textWidth / 2.0, midY - lineH / 2.0), spec.labelColor)
                }
            }
        }

        return FunnelChartPlan(
            layout =
                ChartLayoutPlan(
                    canvasBounds = bounds.canvasBounds,
                    plotBounds = plot,
                    reservedRegions = reservedRegions,
                    xTicks = emptyList(),
                    yTicks = emptyList(),
                    legendEntries = emptyList(),
                    plotDrawCommands = plotCommands,
                    overlayDrawCommands = overlayCommands,
                ),
        )
    }

    /**
     * Radar / spider chart: polygon grid (rings) + spokes + series polygons.
     *
     * Axes are not cartesian; [ChartLayoutPlan.xTicks] and [ChartLayoutPlan.yTicks] stay empty and there is no
     * cartesian axis chrome. Category labels are emitted as [DrawCommand.Text] near the outer vertices.
     */
    fun planRadarChart(
        input: ChartLayoutInput,
        spec: RadarChartSpec,
        selection: ChartSelection?,
    ): RadarChartPlan {
        val labelScale = input.labelLayoutScale.normalizedLabelLayoutScale()
        val bounds = input.toPlanningBounds()

        val legendEntries =
            buildLegendEntries(
                labels = spec.series.map { it.label to it.lineStyle.color },
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
        val radial = PieChartLayoutMath.layout(plotBounds, holeRadiusRatio = null)
        val axisCount = spec.axes.size
        val angles = RadarChartLayoutMath.spokeAnglesDegrees(axisCount, spec.startAngleDegrees)

        // Auto radial range from all series values.
        val all =
            buildList {
                for (s in spec.series) {
                    // Only consider values aligned to the axes list.
                    if (s.values.size != axisCount) continue
                    for (v in s.values) if (v.isFinite()) add(v)
                }
            }
        val minV0 = all.minOrNull() ?: 0.0
        val maxV0 = all.maxOrNull() ?: 1.0
        val minV = min(0.0, minV0)
        val maxV =
            when {
                maxV0 == minV -> minV + FpMath.DEFAULT_DEGENERATE_PAD
                else -> maxV0
            }
        val span = max(maxV - minV, FpMath.EPS_SPAN)

        fun valueToRadiusPx(v: Double): Double {
            val safe = if (v.isFinite()) v else minV
            val t = ((safe - minV) / span).coerceIn(0.0, 1.0)
            return radial.outerRadius * t
        }

        fun polygonPointsForRadius(radiusPx: Double): List<OffsetD> = List(axisCount) { i -> RadarChartLayoutMath.polarToPixel(radial.center, radiusPx, angles[i]) }

        val plotCommands = mutableListOf<DrawCommand>()

        // Grid first (behind series).
        val grid = spec.grid
        if (grid.enabled && axisCount >= 3 && radial.outerRadius > 0.0) {
            val rings = grid.ringCount.coerceAtLeast(0)
            if (rings > 0) {
                for (ri in 1..rings) {
                    val r = radial.outerRadius * (ri.toDouble() / rings.toDouble())
                    val ringPoints = polygonPointsForRadius(r)
                    val segments =
                        buildList<PathSegment>(ringPoints.size + 2) {
                            add(PathSegment.MoveTo(ringPoints.first()))
                            for (i in 1 until ringPoints.size) add(PathSegment.LineTo(ringPoints[i]))
                            add(PathSegment.Close)
                        }
                    plotCommands +=
                        DrawCommand.Path(
                            segments = segments,
                            style =
                                com.mena97villalobos.composeplotlib.core.layout.draw.PathDrawStyle.Stroke(
                                    color = grid.ringLineStyle.color,
                                    strokeWidth = grid.ringLineStyle.strokeWidth,
                                    strokeStyle = grid.ringLineStyle.strokeStyle,
                                ),
                        )
                }
            }
            if (grid.showSpokes) {
                for (i in 0 until axisCount) {
                    val end = RadarChartLayoutMath.polarToPixel(radial.center, radial.outerRadius, angles[i])
                    plotCommands +=
                        DrawCommand.Line(
                            start = radial.center,
                            end = end,
                            color = grid.spokeLineStyle.color,
                            strokeWidth = grid.spokeLineStyle.strokeWidth,
                            strokeStyle = grid.spokeLineStyle.strokeStyle,
                        )
                }
            }
        }

        // Series polygons.
        if (axisCount >= 3 && radial.outerRadius > 0.0) {
            for (s in spec.series) {
                if (s.values.size != axisCount) continue
                val pts =
                    List(axisCount) { i ->
                        RadarChartLayoutMath.polarToPixel(
                            center = radial.center,
                            radiusPx = valueToRadiusPx(s.values[i]),
                            angleDegrees = angles[i],
                        )
                    }
                val segments =
                    buildList<PathSegment>(pts.size + 2) {
                        add(PathSegment.MoveTo(pts.first()))
                        for (i in 1 until pts.size) add(PathSegment.LineTo(pts[i]))
                        add(PathSegment.Close)
                    }
                if (s.fillColor != null) {
                    plotCommands +=
                        DrawCommand.Path(
                            segments = segments,
                            style =
                                com.mena97villalobos.composeplotlib.core.layout.draw.PathDrawStyle
                                    .Fill(s.fillColor),
                        )
                }
                plotCommands +=
                    DrawCommand.Path(
                        segments = segments,
                        style =
                            com.mena97villalobos.composeplotlib.core.layout.draw.PathDrawStyle.Stroke(
                                color = s.lineStyle.color,
                                strokeWidth = s.lineStyle.strokeWidth,
                            ),
                    )

                if (s.pointStyle.visible) {
                    val r = s.pointStyle.radius.coerceAtLeast(0.0)
                    val c = s.pointStyle.color ?: s.lineStyle.color
                    plotCommands +=
                        pts.map { p ->
                            DrawCommand.Circle(
                                center = p,
                                radius = r,
                                color = c,
                            )
                        }
                }
            }
        }

        // Selection overlay: emphasize the selected vertex.
        if (selection != null) {
            val selSeriesId = selection.target.seriesId
            val axisIndex = selection.target.pointIndex
            val series = spec.series.firstOrNull { it.id == selSeriesId }
            if (series != null && series.values.size == axisCount && axisIndex in 0 until axisCount) {
                val px =
                    RadarChartLayoutMath.polarToPixel(
                        center = radial.center,
                        radiusPx = valueToRadiusPx(series.values[axisIndex]),
                        angleDegrees = angles[axisIndex],
                    )
                plotCommands +=
                    DrawCommand.Circle(
                        center = px,
                        radius = spec.selectionStyle.pointRadius,
                        color = spec.selectionStyle.pointColor,
                    )
            }
        }

        // Axis labels: simple quadrant-based placement near outer vertices.
        val overlayCommands = mutableListOf<DrawCommand>()
        if (grid.showAxisLabels && axisCount >= 3 && radial.outerRadius > 0.0) {
            val pad = 8.0 * labelScale
            for (i in 0 until axisCount) {
                val label = spec.axes[i]
                val m = input.textMeasurements.tickLabelMetrics(label)
                if (m.width <= 0.0 || m.height <= 0.0) continue
                val rim = RadarChartLayoutMath.polarToPixel(radial.center, radial.outerRadius + pad, angles[i])
                // Use the direction vector (from center to rim) to decide anchoring.
                val dx = rim.x - radial.center.x
                val dy = rim.y - radial.center.y
                val left =
                    if (dx >= 0.0) {
                        rim.x
                    } else {
                        rim.x - m.width
                    }
                val top =
                    if (dy >= 0.0) {
                        rim.y
                    } else {
                        rim.y - m.height
                    }
                overlayCommands +=
                    DrawCommand.Text(
                        text = label,
                        position = OffsetD(left, top),
                        color = spec.axisLabelStyle.color,
                        firstBaselineFromTop = m.firstBaselineFromTop,
                    )
            }
        }

        return RadarChartPlan(
            layout =
                ChartLayoutPlan(
                    canvasBounds = bounds.canvasBounds,
                    plotBounds = plotBounds,
                    reservedRegions = reservedRegions,
                    xTicks = emptyList(),
                    yTicks = emptyList(),
                    legendEntries = legendEntries,
                    plotDrawCommands = plotCommands,
                    overlayDrawCommands = overlayCommands,
                ),
            selection = selection,
        )
    }

    /**
     * Sankey chart (explicit columns): emits:
     * - one [DrawCommand.Path] fill per link (ribbon), colored per [SankeyLinkColorMode]
     * - one [DrawCommand.RoundRect] fill per node
     * - optional node labels ([SankeyNodeLabelPlacement]) and link labels
     *
     * Layout runs in normalized **flow/cross** space (flow = column direction, cross = node stacking) and maps to
     * domain x/y once per [SankeyOrientation], flipping the cross axis so "top/start" enums mean what they say on
     * screen. Column indices are normalized to the smallest used index; the node scale follows the d3-sankey rule
     * (min over columns of usable-extent / column-total) so no column can overflow. Axes are not used;
     * [ChartLayoutPlan.xTicks] and [ChartLayoutPlan.yTicks] stay empty.
     */
    fun planSankeyChart(
        input: ChartLayoutInput,
        spec: SankeyChartSpec,
        selection: ChartSelection?,
    ): SankeyChartPlan {
        val labelScale = input.labelLayoutScale.normalizedLabelLayoutScale()
        val bounds = input.toPlanningBounds()

        val legendEntries =
            buildLegendEntries(
                labels = emptyList(),
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

        val layout = spec.layout
        val orientation = layout.orientation
        val pad = layout.plotPaddingFraction.coerceIn(0.0, 0.45)
        val flowStart = pad
        val flowEnd = 1.0 - pad
        val crossStart = pad
        val crossEnd = 1.0 - pad
        val innerFlow = max(flowEnd - flowStart, FpMath.EPS_SPAN)
        val innerCross = max(crossEnd - crossStart, FpMath.EPS_SPAN)

        val nodeGap = layout.nodeGap.coerceAtLeast(0.0)
        val nodeCornerR = layout.nodeCornerRadius.coerceAtLeast(0.0)
        val minTh = layout.minLinkThickness.coerceAtLeast(0.0)
        // Zero-value nodes keep a small visible stub; all-zero columns cap per-node extent instead of filling.
        val placeholderExtent = max(minTh * 2.0, 0.008)
        val allZeroColumnMaxExtent = 0.05

        val nodesById = spec.nodes.associateBy { it.id }
        val specIndexById = HashMap<String, Int>(spec.nodes.size)
        spec.nodes.forEachIndexed { i, n -> specIndexById[n.id] = i }

        // Single pass over links: per-node in/out sums (planner runs on the gesture hot path).
        val outSumById = HashMap<String, Double>()
        val inSumById = HashMap<String, Double>()
        for (link in spec.links) {
            val v = link.value.coerceAtLeast(0.0)
            if (v <= 0.0) continue
            outSumById[link.sourceNodeId] = (outSumById[link.sourceNodeId] ?: 0.0) + v
            inSumById[link.targetNodeId] = (inSumById[link.targetNodeId] ?: 0.0) + v
        }

        fun nodeValue(nodeId: String): Double = max(outSumById[nodeId] ?: 0.0, inSumById[nodeId] ?: 0.0)

        // Column indices normalized to the smallest used index; interior gaps keep an empty slot.
        val minColumn = spec.nodes.minOfOrNull { it.column } ?: 0
        val columns: Map<Int, List<Int>> =
            spec.nodes
                .withIndex()
                .groupBy({ it.value.column - minColumn }, { it.index })
                .entries
                .sortedBy { it.key }
                .associate { it.key to it.value }
        val columnCount = ((columns.keys.maxOrNull() ?: 0) + 1).coerceAtLeast(1)

        // Cap the flow-axis thickness so adjacent columns can never overlap (at the cap, the
        // inter-column gap equals the node thickness, so links always flow forward).
        val nodeThickness =
            if (columnCount > 1) {
                min(layout.nodeWidth.coerceAtLeast(0.0), innerFlow / (2.0 * columnCount - 1.0))
            } else {
                layout.nodeWidth.coerceIn(0.0, innerFlow)
            }
        val flowPositions =
            DoubleArray(columnCount) { c ->
                if (columnCount == 1) {
                    flowStart
                } else {
                    flowStart + (c.toDouble() / (columnCount - 1).toDouble()) * (innerFlow - nodeThickness)
                }
            }

        // d3-sankey scale rule: the binding column decides, so no column can overflow its cross extent.
        var valueToExtent = Double.POSITIVE_INFINITY
        for ((_, nodeIdxs) in columns) {
            val total = nodeIdxs.sumOf { idx -> nodeValue(spec.nodes[idx].id) }
            if (total <= 0.0) continue
            val zeroCount = nodeIdxs.count { idx -> nodeValue(spec.nodes[idx].id) <= 0.0 }
            val gaps = max(0, nodeIdxs.size - 1)
            val usable = (innerCross - gaps * nodeGap - zeroCount * placeholderExtent).coerceAtLeast(0.0)
            valueToExtent = min(valueToExtent, usable / total)
        }
        if (!valueToExtent.isFinite()) valueToExtent = 0.0

        fun orderedNodeIndices(nodeIdxs: List<Int>): List<Int> {
            if (nodeIdxs.size <= 1) return nodeIdxs
            val stableTieBreaker: (Int) -> Int = { idx -> idx }
            return when (layout.nodeOrder) {
                SankeyNodeOrder.Input -> nodeIdxs
                SankeyNodeOrder.ValueDesc ->
                    nodeIdxs.sortedWith(
                        compareByDescending<Int> { idx -> nodeValue(spec.nodes[idx].id) }
                            .thenBy(stableTieBreaker),
                    )

                SankeyNodeOrder.IncomingDesc ->
                    nodeIdxs.sortedWith(
                        compareByDescending<Int> { idx -> inSumById[spec.nodes[idx].id] ?: 0.0 }
                            .thenBy(stableTieBreaker),
                    )

                SankeyNodeOrder.OutgoingDesc ->
                    nodeIdxs.sortedWith(
                        compareByDescending<Int> { idx -> outSumById[spec.nodes[idx].id] ?: 0.0 }
                            .thenBy(stableTieBreaker),
                    )

                SankeyNodeOrder.LabelAsc ->
                    nodeIdxs.sortedWith(
                        compareBy<Int> { idx -> spec.nodes[idx].label.lowercase() }
                            .thenBy(stableTieBreaker),
                    )
            }
        }

        data class NodeLayoutBox(
            val nodeId: String,
            val column: Int,
            val label: String,
            val f0: Double,
            val f1: Double,
            val c0: Double,
            val c1: Double,
        )

        val alignment = layout.resolvedColumnAlignment()
        val nodeBoxes = mutableListOf<NodeLayoutBox>()
        for ((col, nodeIdxsUnordered) in columns) {
            val nodeIdxs = orderedNodeIndices(nodeIdxsUnordered)
            val f0 = flowPositions[col]
            val f1 = f0 + nodeThickness
            val gaps = max(0, nodeIdxs.size - 1)
            val total = nodeIdxs.sumOf { idx -> nodeValue(spec.nodes[idx].id) }
            val extents: List<Double> =
                if (total > 0.0 && valueToExtent > 0.0) {
                    nodeIdxs.map { idx ->
                        val v = nodeValue(spec.nodes[idx].id)
                        if (v > 0.0) v * valueToExtent else placeholderExtent
                    }
                } else {
                    val usable = (innerCross - gaps * nodeGap).coerceAtLeast(0.0)
                    val each =
                        if (nodeIdxs.isEmpty()) {
                            0.0
                        } else {
                            min(usable / nodeIdxs.size.toDouble(), allZeroColumnMaxExtent)
                        }
                    nodeIdxs.map { each }
                }
            val usedExtent = extents.sum() + gaps * nodeGap
            val extraSpace = (innerCross - usedExtent).coerceAtLeast(0.0)
            val effectiveGap =
                if (alignment == SankeyColumnAlignment.Justify && nodeIdxs.size >= 2) {
                    nodeGap + extraSpace / gaps.toDouble().coerceAtLeast(1.0)
                } else {
                    nodeGap
                }
            var c =
                when (alignment) {
                    SankeyColumnAlignment.Start, SankeyColumnAlignment.Justify -> crossStart
                    SankeyColumnAlignment.Center -> crossStart + extraSpace / 2.0
                    SankeyColumnAlignment.End -> crossStart + extraSpace
                }
            for ((pos, idx) in nodeIdxs.withIndex()) {
                val n = spec.nodes[idx]
                val extent = extents[pos]
                nodeBoxes +=
                    NodeLayoutBox(
                        nodeId = n.id,
                        column = n.column,
                        label = n.label,
                        f0 = f0,
                        f1 = f1,
                        c0 = c,
                        // The scale rule guarantees fit; the coerce only absorbs FP dust.
                        c1 = min(c + extent, crossEnd),
                    )
                c += extent + effectiveGap
            }
        }

        val nodeBoxById = nodeBoxes.associateBy { it.nodeId }

        data class IndexedLink(
            val index: Int,
            val link: SankeyLink,
        )

        val validLinks =
            spec.links.withIndex().mapNotNull { (i, link) ->
                if (link.value > 0.0 &&
                    nodeBoxById[link.sourceNodeId] != null &&
                    nodeBoxById[link.targetNodeId] != null
                ) {
                    IndexedLink(i, link)
                } else {
                    null
                }
            }
        val outgoingBySource: Map<String, List<IndexedLink>> = validLinks.groupBy { it.link.sourceNodeId }
        val incomingByTarget: Map<String, List<IndexedLink>> = validLinks.groupBy { it.link.targetNodeId }

        fun midCross(box: NodeLayoutBox?): Double = if (box == null) 0.0 else (box.c0 + box.c1) / 2.0

        fun compareLinks(
            order: SankeyLinkOrder,
            a: IndexedLink,
            b: IndexedLink,
        ): Int {
            val av = a.link.value.coerceAtLeast(0.0)
            val bv = b.link.value.coerceAtLeast(0.0)
            val primary =
                when (order) {
                    SankeyLinkOrder.SpecOrder -> a.index.compareTo(b.index)
                    SankeyLinkOrder.ValueDesc -> bv.compareTo(av)
                    SankeyLinkOrder.ValueAsc -> av.compareTo(bv)
                    SankeyLinkOrder.TargetTopToBottom ->
                        midCross(nodeBoxById[a.link.targetNodeId]).compareTo(midCross(nodeBoxById[b.link.targetNodeId]))
                    SankeyLinkOrder.TargetBottomToTop ->
                        midCross(nodeBoxById[b.link.targetNodeId]).compareTo(midCross(nodeBoxById[a.link.targetNodeId]))
                    SankeyLinkOrder.SourceTopToBottom ->
                        midCross(nodeBoxById[a.link.sourceNodeId]).compareTo(midCross(nodeBoxById[b.link.sourceNodeId]))
                    SankeyLinkOrder.SourceBottomToTop ->
                        midCross(nodeBoxById[b.link.sourceNodeId]).compareTo(midCross(nodeBoxById[a.link.sourceNodeId]))
                }
            return if (primary != 0) primary else a.index.compareTo(b.index)
        }

        // Per-end band allocation. When minLinkThickness (or a squeezed node) makes the stacked demand
        // exceed the node's extent, bands are rescaled proportionally instead of collapsing to zero.
        val srcBand = HashMap<Int, DoubleArray>()
        val tgtBand = HashMap<Int, DoubleArray>()

        fun allocateBands(
            byNode: Map<String, List<IndexedLink>>,
            order: SankeyLinkOrder,
            into: HashMap<Int, DoubleArray>,
        ) {
            for ((nodeId, list) in byNode) {
                val box = nodeBoxById[nodeId] ?: continue
                val nodeExtent = (box.c1 - box.c0).coerceAtLeast(0.0)
                val sorted = list.sortedWith { a, b -> compareLinks(order, a, b) }
                val raw = DoubleArray(sorted.size) { i -> max(sorted[i].link.value * valueToExtent, minTh) }
                val rawSum = raw.sum()
                val scale = if (rawSum > nodeExtent) (if (rawSum > 0.0) nodeExtent / rawSum else 0.0) else 1.0
                var offset = box.c0
                for ((i, il) in sorted.withIndex()) {
                    val th = raw[i] * scale
                    into[il.index] = doubleArrayOf(offset, offset + th)
                    offset += th
                }
            }
        }
        allocateBands(outgoingBySource, layout.outgoingLinkOrder, srcBand)
        allocateBands(incomingByTarget, layout.incomingLinkOrder, tgtBand)

        // Cross-axis pixel density: used for sub-pixel ribbon widening and label fit gates.
        val pixelsPerCross =
            when (orientation) {
                SankeyOrientation.Horizontal -> plotBounds.height / max(input.viewport.yRange.span, FpMath.EPS_SPAN)
                SankeyOrientation.Vertical -> plotBounds.width / max(input.viewport.xRange.span, FpMath.EPS_SPAN)
            }
        val minVisibleCrossExtent = if (pixelsPerCross > 0.0) 1.0 / pixelsPerCross else 0.0

        /** Rendering-only widening: keeps positive bands at least ~1px thick on small charts. */
        fun widenedBand(band: DoubleArray): DoubleArray {
            val extent = band[1] - band[0]
            if (extent <= 0.0 || extent >= minVisibleCrossExtent) return band
            val mid = (band[0] + band[1]) / 2.0
            val half = minVisibleCrossExtent / 2.0
            return doubleArrayOf(mid - half, mid + half)
        }

        data class LinkLayoutBox(
            val specIndex: Int,
            val sourceNodeId: String,
            val targetNodeId: String,
            val value: Double,
            val sf: Double,
            val tf: Double,
            val sc0: Double,
            val sc1: Double,
            val tc0: Double,
            val tc1: Double,
        )

        val linkBoxes = mutableListOf<LinkLayoutBox>()
        for (il in validLinks) {
            val s = nodeBoxById[il.link.sourceNodeId] ?: continue
            val t = nodeBoxById[il.link.targetNodeId] ?: continue
            val sb = srcBand[il.index] ?: continue
            val tb = tgtBand[il.index] ?: continue
            if (sb[1] - sb[0] <= 0.0 && tb[1] - tb[0] <= 0.0) continue
            val ws = widenedBand(sb)
            val wt = widenedBand(tb)
            linkBoxes +=
                LinkLayoutBox(
                    specIndex = il.index,
                    sourceNodeId = s.nodeId,
                    targetNodeId = t.nodeId,
                    value = il.link.value,
                    sf = s.f1,
                    tf = t.f0,
                    sc0 = ws[0],
                    sc1 = ws[1],
                    tc0 = wt[0],
                    tc1 = wt[1],
                )
        }

        // Orientation seam: flow/cross → domain x/y. The cross flip makes layout "cross increases downward"
        // match the screen after ChartTransform's y inversion; Vertical swaps the axes (flow advances downward).
        val transform = ChartTransform.forPlotBounds(input.viewport, plotBounds)

        fun px(
            f: Double,
            c: Double,
        ): OffsetD =
            transform.toPixel(
                when (orientation) {
                    SankeyOrientation.Horizontal -> XyPoint(f, 1.0 - c)
                    SankeyOrientation.Vertical -> XyPoint(c, 1.0 - f)
                },
            )

        fun nodeFill(nodeId: String): ChartColor {
            nodesById[nodeId]?.color?.let { return it }
            val palette = layout.nodePalette
            if (palette.isNotEmpty()) {
                return palette[(specIndexById[nodeId] ?: 0) % palette.size]
            }
            return layout.defaultNodeColor
        }

        val plannedNodes = mutableListOf<PlannedSankeyNode>()
        val nodeFillCommands = mutableListOf<DrawCommand>()
        val nodeLabelCommands = mutableListOf<DrawCommand?>()
        val nodeCorners = mutableListOf<Double>()
        val flowMidpoint = (flowStart + flowEnd) / 2.0
        for (box in nodeBoxes) {
            val p1 = px(box.f0, box.c0)
            val p2 = px(box.f1, box.c1)
            val rect =
                RectD(
                    left = min(p1.x, p2.x),
                    top = min(p1.y, p2.y),
                    right = max(p1.x, p2.x),
                    bottom = max(p1.y, p2.y),
                )
            val anchor = rect.center
            plannedNodes +=
                PlannedSankeyNode(
                    nodeId = box.nodeId,
                    column = box.column,
                    label = box.label,
                    pixelBounds = rect,
                    markerAnchor = anchor,
                )
            val corner =
                min(
                    nodeCornerR * min(plotBounds.width, plotBounds.height),
                    min(rect.width, rect.height) / 2.0,
                )
            nodeCorners += corner
            val fillColor = nodeFill(box.nodeId)
            nodeFillCommands +=
                DrawCommand.RoundRect(
                    bounds = rect,
                    cornerRadius = corner,
                    style = ShapeDrawStyle.Fill(fillColor),
                )

            val nodeLabels = layout.nodeLabels
            var labelCommand: DrawCommand? = null
            if (nodeLabels.enabled) {
                val flowThicknessPx =
                    when (orientation) {
                        SankeyOrientation.Horizontal -> rect.width
                        SankeyOrientation.Vertical -> rect.height
                    }
                val m = input.textMeasurements.tickLabelMetrics(box.label)
                when (nodeLabels.placement) {
                    SankeyNodeLabelPlacement.Adjacent -> {
                        if (m.width > 0.0 && m.height > 0.0) {
                            val padPx =
                                max(nodeLabels.paddingFractionOfNodeWidth.coerceAtLeast(0.0) * flowThicknessPx, 4.0)
                            // d3 rule: labels point inward — after the node in the first half of the
                            // flow, before it in the second half.
                            val firstHalf = (box.f0 + box.f1) / 2.0 < flowMidpoint
                            val x: Double
                            val y: Double
                            when (orientation) {
                                SankeyOrientation.Horizontal -> {
                                    x = if (firstHalf) rect.right + padPx else rect.left - padPx - m.width
                                    y = rect.center.y - m.height / 2.0
                                }
                                SankeyOrientation.Vertical -> {
                                    x = rect.center.x - m.width / 2.0
                                    y = if (firstHalf) rect.bottom + padPx else rect.top - padPx - m.height
                                }
                            }
                            // Keep labels at their natural position: panned-out nodes must not have
                            // their labels clamped back onto the plot edge. Skip when neither the
                            // node nor the label overlaps the visible plot.
                            val labelRect = RectD(x, y, x + m.width, y + m.height)
                            if (rect.intersects(plotBounds) && labelRect.intersects(plotBounds)) {
                                labelCommand =
                                    DrawCommand.Text(
                                        text = box.label,
                                        position = OffsetD(x, y),
                                        color = nodeLabels.adjacentColor,
                                        firstBaselineFromTop = m.firstBaselineFromTop,
                                    )
                            }
                        }
                    }
                    SankeyNodeLabelPlacement.Inside -> {
                        val padPx = (nodeLabels.paddingFractionOfNodeWidth.coerceAtLeast(0.0) * flowThicknessPx).coerceAtLeast(0.0)
                        val availW = max(rect.width - 2.0 * padPx, 0.0)
                        val availH = max(rect.height - 2.0 * padPx, 0.0)
                        if (m.width > 0.0 && m.height > 0.0) {
                            when (nodeLabels.orientation) {
                                SankeyNodeLabelOrientation.Horizontal -> {
                                    val fitted = ellipsizedLabelToWidth(box.label, m.width, availW)
                                    if (fitted != null && m.height <= availH) {
                                        val x = (rect.left + padPx).coerceIn(plotBounds.left, plotBounds.right)
                                        val y = (rect.center.y - (m.height / 2.0)).coerceIn(plotBounds.top, plotBounds.bottom)
                                        labelCommand =
                                            DrawCommand.Text(
                                                text = fitted.text,
                                                position = OffsetD(x, y),
                                                color = nodeLabels.color,
                                                firstBaselineFromTop = m.firstBaselineFromTop,
                                            )
                                    }
                                }
                                SankeyNodeLabelOrientation.Vertical -> {
                                    // Rotated -90°: text width consumes vertical space; text height consumes horizontal space.
                                    val fitted = ellipsizedLabelToWidth(box.label, m.width, availH)
                                    if (fitted != null && m.height <= availW) {
                                        val x = (rect.center.x - (fitted.width / 2.0)).coerceIn(plotBounds.left, plotBounds.right)
                                        val y = (rect.center.y - (m.height / 2.0)).coerceIn(plotBounds.top, plotBounds.bottom)
                                        labelCommand =
                                            DrawCommand.Text(
                                                text = fitted.text,
                                                position = OffsetD(x, y),
                                                color = nodeLabels.color,
                                                firstBaselineFromTop = m.firstBaselineFromTop,
                                                rotationDegrees = -90.0,
                                            )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            nodeLabelCommands += labelCommand
        }

        fun boundsOf(points: List<OffsetD>): RectD {
            var minX = Double.POSITIVE_INFINITY
            var minY = Double.POSITIVE_INFINITY
            var maxX = Double.NEGATIVE_INFINITY
            var maxY = Double.NEGATIVE_INFINITY
            for (p in points) {
                minX = min(minX, p.x)
                minY = min(minY, p.y)
                maxX = max(maxX, p.x)
                maxY = max(maxY, p.y)
            }
            if (!minX.isFinite() || !minY.isFinite() || !maxX.isFinite() || !maxY.isFinite()) {
                return RectD(0.0, 0.0, 0.0, 0.0)
            }
            return RectD(minX, minY, maxX, maxY)
        }

        fun cubicPoint(
            p0: OffsetD,
            p1: OffsetD,
            p2: OffsetD,
            p3: OffsetD,
            t: Double,
        ): OffsetD {
            val u = 1.0 - t
            val a = u * u * u
            val b = 3.0 * u * u * t
            val c = 3.0 * u * t * t
            val d = t * t * t
            return OffsetD(
                x = a * p0.x + b * p1.x + c * p2.x + d * p3.x,
                y = a * p0.y + b * p1.y + c * p2.y + d * p3.y,
            )
        }

        val ribbonAlpha = 0x66L

        fun ChartColor.withRibbonAlpha(): ChartColor = ChartColor((argb and 0x00FFFFFF) or (ribbonAlpha shl 24))

        val plannedLinks = mutableListOf<PlannedSankeyLink>()
        val linkFillCommands = mutableListOf<DrawCommand>()
        val linkLabelCommands = mutableListOf<DrawCommand?>()
        val curvature = layout.linkCurvature.coerceIn(0.0, 1.0)
        val curveSamples = 16
        for (l in linkBoxes) {
            val c1f = l.sf + (l.tf - l.sf) * curvature
            val c2f = l.tf - (l.tf - l.sf) * curvature
            val sTop = px(l.sf, l.sc0)
            val cTop1 = px(c1f, l.sc0)
            val cTop2 = px(c2f, l.tc0)
            val tTop = px(l.tf, l.tc0)
            val sBot = px(l.sf, l.sc1)
            val tBot = px(l.tf, l.tc1)
            val tBotCtrl = px(c2f, l.tc1)
            val sBotCtrl = px(c1f, l.sc1)
            val segments =
                listOf(
                    PathSegment.MoveTo(sTop),
                    PathSegment.CubicTo(cTop1, cTop2, tTop),
                    PathSegment.LineTo(tBot),
                    PathSegment.CubicTo(tBotCtrl, sBotCtrl, sBot),
                    PathSegment.Close,
                )

            // Flattened Bézier edges: the hit polygon matches the drawn ribbon (control points alone
            // both overshoot and undershoot the curve, and can self-intersect at high curvature).
            val polygon =
                buildList(2 * (curveSamples + 1)) {
                    for (i in 0..curveSamples) {
                        add(cubicPoint(sTop, cTop1, cTop2, tTop, i.toDouble() / curveSamples))
                    }
                    for (i in 0..curveSamples) {
                        add(cubicPoint(tBot, tBotCtrl, sBotCtrl, sBot, i.toDouble() / curveSamples))
                    }
                }
            val bb = boundsOf(polygon)

            // Centerline Bézier midpoint (t = 0.5): (P0 + 3·C1 + 3·C2 + P3) / 8, computed in flow/cross space.
            val sMidCross = (l.sc0 + l.sc1) / 2.0
            val tMidCross = (l.tc0 + l.tc1) / 2.0
            val anchor =
                px(
                    (l.sf + 3.0 * c1f + 3.0 * c2f + l.tf) / 8.0,
                    (sMidCross + tMidCross) / 2.0,
                )

            val explicitColor = spec.links[l.specIndex].color
            val fillStyle: PathDrawStyle =
                when {
                    explicitColor != null -> PathDrawStyle.Fill(explicitColor)
                    else ->
                        when (layout.linkColorMode) {
                            SankeyLinkColorMode.Static -> PathDrawStyle.Fill(layout.defaultLinkColor)
                            SankeyLinkColorMode.SourceNode ->
                                PathDrawStyle.Fill(nodeFill(l.sourceNodeId).withRibbonAlpha())
                            SankeyLinkColorMode.TargetNode ->
                                PathDrawStyle.Fill(nodeFill(l.targetNodeId).withRibbonAlpha())
                            SankeyLinkColorMode.SourceToTargetGradient -> {
                                val sMidPx = px(l.sf, sMidCross)
                                val tMidPx = px(l.tf, tMidCross)
                                PathDrawStyle.LinearGradientFill(
                                    startColor = nodeFill(l.sourceNodeId).withRibbonAlpha(),
                                    endColor = nodeFill(l.targetNodeId).withRibbonAlpha(),
                                    startX = sMidPx.x,
                                    startY = sMidPx.y,
                                    endX = tMidPx.x,
                                    endY = tMidPx.y,
                                )
                            }
                        }
                }
            linkFillCommands += DrawCommand.Path(segments = segments, style = fillStyle)
            plannedLinks +=
                PlannedSankeyLink(
                    sourceNodeId = l.sourceNodeId,
                    targetNodeId = l.targetNodeId,
                    value = l.value,
                    pixelBounds = bb,
                    pixelPolygon = polygon,
                    markerAnchor = anchor,
                    pathSegments = segments,
                )

            val linkLabels = layout.linkLabels
            var labelCommand: DrawCommand? = null
            if (linkLabels.enabled) {
                val sLabel = nodesById[l.sourceNodeId]?.label ?: l.sourceNodeId
                val tLabel = nodesById[l.targetNodeId]?.label ?: l.targetNodeId
                val rawText = linkLabels.formatter(sLabel, tLabel, l.value)
                val m = input.textMeasurements.tickLabelMetrics(rawText)
                // Fit against the ribbon itself, not its bounding box: the label's cross extent must fit
                // the thinner attach band, and its flow extent must fit the ribbon's flow span.
                val bandPx = min(l.sc1 - l.sc0, l.tc1 - l.tc0) * pixelsPerCross
                val fits =
                    m.width > 0.0 &&
                        m.height > 0.0 &&
                        when (orientation) {
                            SankeyOrientation.Horizontal -> m.height <= bandPx && m.width <= bb.width
                            SankeyOrientation.Vertical -> m.width <= bandPx && m.height <= bb.height
                        }
                if (fits) {
                    val x = anchor.x - m.width / 2.0
                    val y = anchor.y - m.height / 2.0
                    val alpha = linkLabels.alpha.coerceIn(0.0, 1.0)
                    labelCommand =
                        DrawCommand.Group(
                            alpha = alpha,
                            children =
                                listOf(
                                    DrawCommand.Text(
                                        text = rawText,
                                        position = OffsetD(x, y),
                                        color = linkLabels.color,
                                        firstBaselineFromTop = m.firstBaselineFromTop,
                                    ),
                                ),
                        )
                }
            }
            linkLabelCommands += labelCommand
        }

        // Selection emphasis: exclude the selected element from the dim group, then re-emit it at full
        // alpha with an outline — the highlight follows the actual ribbon/node geometry.
        val selectedForSpec = selection?.takeIf { it.target.seriesId == spec.id }
        val selectedNodeIdx =
            if (selectedForSpec?.target?.layerId == SankeyHitResolver.LayerNode) selectedForSpec.target.pointIndex else -1
        val selectedLinkIdx =
            if (selectedForSpec?.target?.layerId == SankeyHitResolver.LayerLink) selectedForSpec.target.pointIndex else -1

        fun assembleBase(
            excludeNodeIdx: Int,
            excludeLinkIdx: Int,
        ): List<DrawCommand> {
            val commands = mutableListOf<DrawCommand>()

            fun addLinks() {
                for (i in linkFillCommands.indices) {
                    if (i == excludeLinkIdx) continue
                    commands += linkFillCommands[i]
                    linkLabelCommands[i]?.let { commands += it }
                }
            }

            fun addNodes() {
                for (i in nodeFillCommands.indices) {
                    if (i == excludeNodeIdx) continue
                    commands += nodeFillCommands[i]
                    nodeLabelCommands[i]?.let { commands += it }
                }
            }
            if (layout.linksBehindNodes) {
                addLinks()
                addNodes()
            } else {
                addNodes()
                addLinks()
            }
            return commands
        }

        val plotCommands = mutableListOf<DrawCommand>()
        if (selectedForSpec != null) {
            // SankeyMATIC-like focus: dim everything except the selected element…
            plotCommands += DrawCommand.Group(alpha = 0.55, children = assembleBase(selectedNodeIdx, selectedLinkIdx))
            // …then re-emit the selected element undimmed with an outline in the selection color.
            val selectedNode = plannedNodes.getOrNull(selectedNodeIdx)
            val selectedLink = plannedLinks.getOrNull(selectedLinkIdx)
            when {
                selectedNode != null && selectedNodeIdx in nodeFillCommands.indices -> {
                    plotCommands += nodeFillCommands[selectedNodeIdx]
                    nodeLabelCommands[selectedNodeIdx]?.let { plotCommands += it }
                    plotCommands +=
                        DrawCommand.RoundRect(
                            bounds = selectedNode.pixelBounds,
                            cornerRadius = nodeCorners.getOrElse(selectedNodeIdx) { 0.0 },
                            style =
                                ShapeDrawStyle.Stroke(
                                    color = spec.selectionStyle.pointColor,
                                    strokeWidth = 1.5,
                                ),
                        )
                }

                selectedLink != null && selectedLinkIdx in linkFillCommands.indices -> {
                    plotCommands += linkFillCommands[selectedLinkIdx]
                    linkLabelCommands[selectedLinkIdx]?.let { plotCommands += it }
                    plotCommands +=
                        DrawCommand.Path(
                            segments = selectedLink.pathSegments,
                            style =
                                PathDrawStyle.Stroke(
                                    color = spec.selectionStyle.pointColor,
                                    strokeWidth = 1.5,
                                ),
                        )
                }
            }
        } else {
            plotCommands += assembleBase(-1, -1)
        }

        return SankeyChartPlan(
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
            nodes = plannedNodes,
            links = plannedLinks,
            selection = selection,
        )
    }

    private data class FittedLabel(
        val text: String,
        val width: Double,
    )

    /**
     * Fits [text] (whose full rendered width is [fullWidth]) into [availWidth]. Returns the full label, an
     * ellipsized prefix (`…` suffix, width proportionally estimated), or `null` when not even one character
     * plus the ellipsis fits.
     */
    private fun ellipsizedLabelToWidth(
        text: String,
        fullWidth: Double,
        availWidth: Double,
    ): FittedLabel? {
        if (fullWidth <= 0.0 || text.isEmpty()) return null
        if (fullWidth <= availWidth) return FittedLabel(text, fullWidth)
        val perChar = fullWidth / text.length
        if (perChar <= 0.0) return null
        // Truncated widths are proportional ESTIMATES (the renderer re-measures the real string), so
        // budget only 90% of the available width and reserve one character slot for the ellipsis.
        val maxChars = ((availWidth * 0.9 / perChar) - 1.0).toInt().coerceAtMost(text.length - 1)
        if (maxChars < 1) return null
        return FittedLabel(text.take(maxChars) + "…", perChar * (maxChars + 1))
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
                legendSeriesIds = spec.groups.map { it.id },
                hiddenSeriesIds = spec.hiddenSeriesIds,
            )

        val visibleGroups = spec.groups.filter { it.id !in spec.hiddenSeriesIds }
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
                groups = visibleGroups,
                layoutMode = effectiveBarLayout,
                clusterBarGapPx = spec.clusterBarGapPx,
                orientation = spec.orientation,
                viewport = frame.viewport,
                transform = frame.transform,
                plotBounds = frame.plotBounds,
                layerId = null,
                percentStack = spec.stackMode == BarStackMode.PercentOfTotal,
            )
        plotCommands +=
            buildBarDrawCommands(
                segments = barSegments,
                selection = selection,
                selectionStyle = spec.selectionStyle,
            )
        if (spec.errorBars.enabled && spec.orientation == BarChartOrientation.Vertical) {
            val anchors =
                barSegments.mapNotNull { seg ->
                    val entry = seg.entry
                    if (!entry.y.isFinite() || (entry.yLow == null && entry.yHigh == null)) return@mapNotNull null
                    val crossPx = (seg.bounds.left + seg.bounds.right) / 2.0
                    val centerPx = frame.transform.toPixel(XyPoint(0.0, entry.y)).y
                    val lowPx = entry.yLow?.let { frame.transform.toPixel(XyPoint(0.0, it)).y }
                    val highPx = entry.yHigh?.let { frame.transform.toPixel(XyPoint(0.0, it)).y }
                    ErrorBarAnchor(crossPx, centerPx, lowPx, highPx)
                }
            plotCommands += buildErrorBarCommands(anchors, spec.errorBars, frame.plotBounds)
        }
        if (spec.dataLabels.enabled) {
            val (charW, lineH) = estimateLabelCharBox(input)
            val anchors =
                barSegments.mapNotNull { seg ->
                    val v = seg.entry.y
                    if (!v.isFinite()) return@mapNotNull null
                    val cx = (seg.bounds.left + seg.bounds.right) / 2.0
                    val cy = (seg.bounds.top + seg.bounds.bottom) / 2.0
                    val mark =
                        when (seg.orientation) {
                            BarChartOrientation.Vertical ->
                                OffsetD(cx, if (v >= 0.0) seg.bounds.top else seg.bounds.bottom)
                            BarChartOrientation.Horizontal ->
                                OffsetD(if (v >= 0.0) seg.bounds.right else seg.bounds.left, cy)
                        }
                    DataLabelAnchor(mark, spec.dataLabels.valueFormatter(v), 0.0)
                }
            plotCommands += buildDataLabelCommands(anchors, spec.dataLabels, frame.plotBounds, charW, lineH)
        }
        plotCommands += buildReferenceLineCommands(spec.referenceLines, frame.transform, frame.plotBounds)
        if (spec.annotations.isNotEmpty()) {
            val (annCharW, annLineH) = estimateLabelCharBox(input)
            plotCommands += buildAnnotationCommands(spec.annotations, frame.transform, frame.plotBounds, annCharW, annLineH)
        }

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

    /**
     * Stacked-area chart: fills a band per series accumulated over the shared x values, per the
     * [offset][StackedAreaChartSpec.offset] (zero / 100% expand / streamgraph silhouette). Cartesian frame with
     * grid + axis overlay; no per-point selection.
     */
    fun planStackedAreaChart(
        input: ChartLayoutInput,
        spec: StackedAreaChartSpec,
        tickValuePass: TickValuePass? = null,
    ): StackedAreaChartPlan {
        val frame =
            layoutCartesianFrame(
                input = input,
                xAxis = spec.xAxis,
                yAxis = spec.yAxis,
                legend = spec.legend,
                contentPadding = spec.contentPadding,
                legendLabelColors = spec.series.map { it.label to it.color },
                tickValuePass = tickValuePass,
                legendSeriesIds = spec.series.map { it.id },
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
        plotCommands += buildStackedAreaCommands(spec, frame.transform, frame.plotBounds)

        val overlayCommands =
            buildAxisAndTickLabelsOverlay(frame.plotBounds, spec.xAxis, spec.yAxis, frame.xTicks, frame.yTicks)

        return StackedAreaChartPlan(
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
        )
    }

    private fun buildStackedAreaCommands(
        spec: StackedAreaChartSpec,
        transform: ChartTransform,
        plotBounds: RectD,
    ): List<DrawCommand> {
        val xs = spec.xValues
        val n = xs.size
        val m = spec.series.size
        if (n < 2 || m == 0) return emptyList()
        val lower = Array(m) { DoubleArray(n) }
        val upper = Array(m) { DoubleArray(n) }
        for (i in 0 until n) {
            val values =
                DoubleArray(m) { s ->
                    spec.series[s]
                        .values
                        .getOrElse(i) { 0.0 }
                        .let { if (it.isFinite()) it else 0.0 }
                }
            val total = values.sum()
            var cumulative = if (spec.offset == AreaStackOffset.Silhouette) -total / 2.0 else 0.0
            for (s in 0 until m) {
                val contribution =
                    if (spec.offset == AreaStackOffset.Expand) {
                        if (total != 0.0) values[s] / total else 0.0
                    } else {
                        values[s]
                    }
                lower[s][i] = cumulative
                cumulative += contribution
                upper[s][i] = cumulative
            }
        }
        val out = ArrayList<DrawCommand>(m)
        for (s in 0 until m) {
            val segments = ArrayList<PathSegment>(2 * n + 2)
            segments += PathSegment.MoveTo(transform.toPixel(XyPoint(xs[0], upper[s][0])))
            for (i in 1 until n) segments += PathSegment.LineTo(transform.toPixel(XyPoint(xs[i], upper[s][i])))
            for (i in n - 1 downTo 0) segments += PathSegment.LineTo(transform.toPixel(XyPoint(xs[i], lower[s][i])))
            segments += PathSegment.Close
            out += DrawCommand.Path(segments = segments, style = PathDrawStyle.Fill(color = spec.series[s].color))
        }
        return out
    }

    /**
     * Candlestick / OHLC chart: emits a high-low wick [DrawCommand.Line] beneath an open-close body
     * [DrawCommand.Rect] per visible candle, colored bull/bear by `close >= open`. Selection appends a stroked
     * [DrawCommand.RoundRect] accent around the selected body.
     *
     * @param selection optional candle highlight: matches when `selection.target.seriesId` equals the series id
     * and `selection.target.pointIndex` is the **global** candle index.
     * @param tickValuePass optional precomputed pass-2 tick values for [spec]'s primary axes (default: compute)
     */
    fun planCandlestickChart(
        input: ChartLayoutInput,
        spec: CandlestickChartSpec,
        selection: ChartSelection? = null,
        tickValuePass: TickValuePass? = null,
    ): CandlestickChartPlan {
        val frame =
            layoutCartesianFrame(
                input = input,
                xAxis = spec.xAxis,
                yAxis = spec.yAxis,
                legend = spec.legend,
                contentPadding = spec.contentPadding,
                legendLabelColors = spec.series.map { it.label to it.style.bullColor },
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
        val candleSpec =
            if (spec.variant == CandlestickVariant.HeikinAshi) {
                spec.copy(series = spec.series.map { it.copy(points = heikinAshi(it.points)) })
            } else {
                spec
            }
        val candles =
            computePlannedCandles(
                spec = candleSpec,
                viewport = frame.viewport,
                transform = frame.transform,
                plotBounds = frame.plotBounds,
            )
        plotCommands +=
            buildCandleDrawCommands(
                candles = candles,
                selection = selection,
                selectionStyle = spec.selectionStyle,
                variant = spec.variant,
            )

        val overlayCommands =
            buildAxisAndTickLabelsOverlay(
                frame.plotBounds,
                spec.xAxis,
                spec.yAxis,
                frame.xTicks,
                frame.yTicks,
            )

        return CandlestickChartPlan(
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
            candles = candles,
        )
    }

    private fun computePlannedCandles(
        spec: CandlestickChartSpec,
        viewport: ChartViewport,
        transform: ChartTransform,
        plotBounds: RectD,
    ): List<PlannedCandle> {
        if (spec.series.isEmpty()) return emptyList()
        val cap = spec.candleRender.maxCandles
        val padding = spec.candleRender.viewportContinuityPadding.coerceAtLeast(0)
        val out = mutableListOf<PlannedCandle>()
        for (series in spec.series) {
            if (series.points.isEmpty()) continue
            val indexed = visibleCandlesIndexed(series.points, viewport, padding, cap)
            if (indexed.isEmpty()) continue
            val style = series.style
            val half = style.bodyWidth / 2.0
            val hitHalf = max(half, CANDLE_MIN_TAP_HALF_WIDTH)
            for ((globalIndex, p) in indexed) {
                val centerXPx = transform.toPixel(XyPoint(p.x, viewport.yRange.start)).x
                if (!VisibleRangeCalculator.barColumnIntersectsPlotHorizontally(centerXPx, style.bodyWidth, plotBounds)) {
                    continue
                }
                val openY = transform.toPixel(XyPoint(p.x, p.open)).y
                val closeY = transform.toPixel(XyPoint(p.x, p.close)).y
                val highY = transform.toPixel(XyPoint(p.x, p.high)).y
                val lowY = transform.toPixel(XyPoint(p.x, p.low)).y
                val isBull = p.close >= p.open
                val bodyColor = if (isBull) style.bullColor else style.bearColor
                val wickColor = style.wickColor ?: bodyColor
                val bodyTop = min(openY, closeY)
                var bodyBottom = max(openY, closeY)
                if (bodyBottom - bodyTop < 1.0) bodyBottom = bodyTop + 1.0
                out +=
                    PlannedCandle(
                        seriesId = series.id,
                        index = globalIndex,
                        point = p,
                        bodyBounds = RectD(centerXPx - half, bodyTop, centerXPx + half, bodyBottom),
                        hitBounds = RectD(centerXPx - hitHalf, min(highY, lowY), centerXPx + hitHalf, max(highY, lowY)),
                        wickTop = OffsetD(centerXPx, highY),
                        wickBottom = OffsetD(centerXPx, lowY),
                        isBull = isBull,
                        bodyColor = bodyColor,
                        wickColor = wickColor,
                        wickWidth = style.wickWidth,
                    )
            }
        }
        return out
    }

    /**
     * Visible candle window (with continuity padding) as `(globalIndex, point)` pairs; uniformly subsampled
     * to [maxCandles] when capped. The **global** index is preserved so selection/markers stay correct.
     */
    private fun visibleCandlesIndexed(
        points: List<OHLCPoint>,
        viewport: ChartViewport,
        paddingEachSide: Int,
        maxCandles: Int,
    ): List<Pair<Int, OHLCPoint>> {
        val range =
            VisibleRangeCalculator.visiblePointRangeWithContinuity(
                points = points.toCloseXyPoints(),
                viewport = viewport,
                paddingEachSide = paddingEachSide,
            ) ?: return emptyList()
        val windowLen = range.endInclusive - range.start + 1
        if (maxCandles > 0 && windowLen > maxCandles) {
            return uniformSampleCandles(points, range.start, range.endInclusive, maxCandles)
        }
        val out = ArrayList<Pair<Int, OHLCPoint>>(windowLen)
        var i = range.start
        while (i <= range.endInclusive) {
            out.add(i to points[i])
            i++
        }
        return out
    }

    private fun uniformSampleCandles(
        points: List<OHLCPoint>,
        start: Int,
        endInclusive: Int,
        maxPoints: Int,
    ): List<Pair<Int, OHLCPoint>> {
        val n = endInclusive - start + 1
        if (maxPoints < 2 || n <= maxPoints) {
            val all = ArrayList<Pair<Int, OHLCPoint>>(n)
            var i = start
            while (i <= endInclusive) {
                all.add(i to points[i])
                i++
            }
            return all
        }
        val seen = HashSet<Int>(maxPoints * 4 / 3 + 1)
        val out = ArrayList<Pair<Int, OHLCPoint>>(maxPoints)
        for (k in 0 until maxPoints) {
            val offset = round(k * (n - 1).toDouble() / (maxPoints - 1)).toInt().coerceIn(0, n - 1)
            val i = start + offset
            if (seen.add(i)) out.add(i to points[i])
        }
        return out
    }

    private fun buildCandleDrawCommands(
        candles: List<PlannedCandle>,
        selection: ChartSelection?,
        selectionStyle: SelectionStyle,
        variant: CandlestickVariant,
    ): List<DrawCommand> =
        candles.flatMap { candle ->
            val glyph =
                when (variant) {
                    CandlestickVariant.OhlcBars -> ohlcBarCommands(candle)
                    CandlestickVariant.HollowCandles -> hollowCandleCommands(candle)
                    // Heikin-Ashi candles are drawn like normal candles (the OHLC was already smoothed).
                    CandlestickVariant.Candles, CandlestickVariant.HeikinAshi -> filledCandleCommands(candle)
                }
            if (selection != null && selectionMatchesCandle(selection, candle)) {
                val accent =
                    DrawCommand.RoundRect(
                        bounds = candle.bodyBounds,
                        cornerRadius = 0.0,
                        style =
                            ShapeDrawStyle.Stroke(
                                color = selectionStyle.lineColor,
                                strokeWidth = selectionStyle.lineStrokeWidth * 2.0,
                            ),
                    )
                glyph + accent
            } else {
                glyph
            }
        }

    // Wick first so the body paints over it where they overlap (z-order is bottom -> top).
    private fun candleWick(candle: PlannedCandle): DrawCommand.Line = DrawCommand.Line(start = candle.wickTop, end = candle.wickBottom, color = candle.wickColor, strokeWidth = candle.wickWidth)

    private fun filledCandleCommands(candle: PlannedCandle): List<DrawCommand> = listOf(candleWick(candle), DrawCommand.Rect(bounds = candle.bodyBounds, color = candle.bodyColor))

    private fun hollowCandleCommands(candle: PlannedCandle): List<DrawCommand> {
        val body: DrawCommand =
            if (candle.isBull) {
                DrawCommand.RoundRect(
                    bounds = candle.bodyBounds,
                    cornerRadius = 0.0,
                    style = ShapeDrawStyle.Stroke(color = candle.bodyColor, strokeWidth = candle.wickWidth),
                )
            } else {
                DrawCommand.Rect(bounds = candle.bodyBounds, color = candle.bodyColor)
            }
        return listOf(candleWick(candle), body)
    }

    private fun ohlcBarCommands(candle: PlannedCandle): List<DrawCommand> {
        val bounds = candle.bodyBounds
        val centerX = (bounds.left + bounds.right) / 2.0
        val halfWidth = bounds.width / 2.0
        // Higher price = smaller y. For a bull candle close sits at the top edge; for a bear candle open does.
        val openY = if (candle.isBull) bounds.bottom else bounds.top
        val closeY = if (candle.isBull) bounds.top else bounds.bottom
        return listOf(
            DrawCommand.Line(start = candle.wickTop, end = candle.wickBottom, color = candle.bodyColor, strokeWidth = candle.wickWidth),
            DrawCommand.Line(start = OffsetD(centerX - halfWidth, openY), end = OffsetD(centerX, openY), color = candle.bodyColor, strokeWidth = candle.wickWidth),
            DrawCommand.Line(start = OffsetD(centerX, closeY), end = OffsetD(centerX + halfWidth, closeY), color = candle.bodyColor, strokeWidth = candle.wickWidth),
        )
    }

    private fun selectionMatchesCandle(
        selection: ChartSelection,
        candle: PlannedCandle,
    ): Boolean = selection.target.seriesId == candle.seriesId && selection.target.pointIndex == candle.index

    /**
     * Violin: for each [ViolinChartSpec.categories] entry, a mirrored Gaussian kernel-density shape along the value
     * (y) axis centered on the category's x slot, half-width ∝ density. Reuses the cartesian frame (categorical x).
     * Read-only (no selection).
     */
    fun planViolinChart(
        input: ChartLayoutInput,
        spec: ViolinChartSpec,
        tickValuePass: TickValuePass? = null,
    ): ViolinChartPlan {
        val categoryAxis = spec.xAxis.copy(scale = AxisScale.Category(spec.categories.map { it.label }))
        val frame =
            layoutCartesianFrame(
                input = input,
                xAxis = categoryAxis,
                yAxis = spec.yAxis,
                legend = LegendSpec(enabled = false),
                contentPadding = spec.contentPadding,
                legendLabelColors = emptyList(),
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
        val resolution = spec.resolution.coerceAtLeast(2)
        val halfSlot = (spec.violinWidth.coerceIn(0.0, 1.0)) / 2.0
        spec.categories.forEachIndexed { index, category ->
            val samples = category.samples.filter { it.isFinite() }
            if (samples.size < 2) return@forEachIndexed
            val yMin = samples.min()
            val yMax = samples.max()
            if (yMax <= yMin) return@forEachIndexed
            val evalYs = (0..resolution).map { yMin + (yMax - yMin) * it / resolution }
            val densities = kernelDensity(samples, evalYs)
            val maxDensity = densities.max()
            if (maxDensity <= 0.0) return@forEachIndexed
            val halfWidths = densities.map { (it / maxDensity) * halfSlot }
            val rightEdge = evalYs.indices.map { j -> frame.transform.toPixel(XyPoint(index + halfWidths[j], evalYs[j])) }
            val leftEdge = evalYs.indices.reversed().map { j -> frame.transform.toPixel(XyPoint(index - halfWidths[j], evalYs[j])) }
            val outline = rightEdge + leftEdge
            val segments = mutableListOf<PathSegment>(PathSegment.MoveTo(outline.first()))
            for (k in 1 until outline.size) segments.add(PathSegment.LineTo(outline[k]))
            segments.add(PathSegment.Close)
            plotCommands += DrawCommand.Path(segments = segments, style = PathDrawStyle.Fill(category.color))

            if (spec.showMedian) {
                val sorted = samples.sorted()
                val median =
                    if (sorted.size % 2 == 1) {
                        sorted[sorted.size / 2]
                    } else {
                        (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
                    }
                val leftPixel = frame.transform.toPixel(XyPoint(index - halfSlot * 0.6, median))
                val rightPixel = frame.transform.toPixel(XyPoint(index + halfSlot * 0.6, median))
                plotCommands += DrawCommand.Line(start = leftPixel, end = rightPixel, color = spec.medianColor, strokeWidth = 1.5)
            }
        }

        val overlayCommands =
            buildAxisAndTickLabelsOverlay(
                frame.plotBounds,
                categoryAxis,
                spec.yAxis,
                frame.xTicks,
                frame.yTicks,
            )
        return ViolinChartPlan(
            layout =
                ChartLayoutPlan(
                    canvasBounds = frame.canvasBounds,
                    plotBounds = frame.plotBounds,
                    reservedRegions = frame.reservedRegions,
                    xTicks = frame.xTicks,
                    yTicks = frame.yTicks,
                    legendEntries = emptyList(),
                    plotDrawCommands = plotCommands,
                    overlayDrawCommands = overlayCommands,
                ),
        )
    }

    /**
     * Box-and-whisker chart: per category emits whiskers (min..q1, q3..max) with end caps, a translucent Q1–Q3 box
     * with a border, a median line, and outlier points. Selection strokes an accent around the selected box.
     *
     * @param selection optional box highlight: matches when `selection.target.seriesId` equals the series id and
     * `selection.target.pointIndex` is the **global** entry index.
     * @param tickValuePass optional precomputed pass-2 tick values for [spec]'s primary axes (default: compute)
     */
    fun planBoxPlotChart(
        input: ChartLayoutInput,
        spec: BoxPlotChartSpec,
        selection: ChartSelection? = null,
        tickValuePass: TickValuePass? = null,
    ): BoxPlotChartPlan {
        val frame =
            layoutCartesianFrame(
                input = input,
                xAxis = spec.xAxis,
                yAxis = spec.yAxis,
                legend = spec.legend,
                contentPadding = spec.contentPadding,
                legendLabelColors = spec.series.map { it.label to it.style.boxColor },
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
        val boxes = computePlannedBoxes(spec, frame.transform, frame.plotBounds)
        plotCommands += buildBoxPlotDrawCommands(boxes, selection, spec.selectionStyle)

        val overlayCommands =
            buildAxisAndTickLabelsOverlay(
                frame.plotBounds,
                spec.xAxis,
                spec.yAxis,
                frame.xTicks,
                frame.yTicks,
            )

        return BoxPlotChartPlan(
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
            boxes = boxes,
        )
    }

    private fun computePlannedBoxes(
        spec: BoxPlotChartSpec,
        transform: ChartTransform,
        plotBounds: RectD,
    ): List<PlannedBox> {
        if (spec.series.isEmpty()) return emptyList()
        val out = mutableListOf<PlannedBox>()
        for (series in spec.series) {
            val style = series.style
            val half = style.boxWidthPx / 2.0
            val hitHalf = max(half, CANDLE_MIN_TAP_HALF_WIDTH)
            val capHalf = half * style.whiskerCapFraction.coerceIn(0.0, 1.0)
            series.entries.forEachIndexed { index, e ->
                val centerXPx = transform.toPixel(XyPoint(e.x, 0.0)).x
                if (!VisibleRangeCalculator.barColumnIntersectsPlotHorizontally(centerXPx, style.boxWidthPx, plotBounds)) {
                    return@forEachIndexed
                }
                val q1Y = transform.toPixel(XyPoint(e.x, e.q1)).y
                val q3Y = transform.toPixel(XyPoint(e.x, e.q3)).y
                val medianY = transform.toPixel(XyPoint(e.x, e.median)).y
                val minY = transform.toPixel(XyPoint(e.x, e.min)).y
                val maxY = transform.toPixel(XyPoint(e.x, e.max)).y
                val boxTop = min(q1Y, q3Y)
                var boxBottom = max(q1Y, q3Y)
                if (boxBottom - boxTop < 1.0) boxBottom = boxTop + 1.0
                out +=
                    PlannedBox(
                        seriesId = series.id,
                        index = index,
                        entry = e,
                        boxBounds = RectD(centerXPx - half, boxTop, centerXPx + half, boxBottom),
                        hitBounds = RectD(centerXPx - hitHalf, min(minY, maxY), centerXPx + hitHalf, max(minY, maxY)),
                        medianY = medianY,
                        whiskerTop = OffsetD(centerXPx, maxY),
                        whiskerBottom = OffsetD(centerXPx, minY),
                        capHalfWidth = capHalf,
                        outlierCenters = e.outliers.map { OffsetD(centerXPx, transform.toPixel(XyPoint(e.x, it)).y) },
                        style = style,
                    )
            }
        }
        return out
    }

    private fun buildBoxPlotDrawCommands(
        boxes: List<PlannedBox>,
        selection: ChartSelection?,
        selectionStyle: SelectionStyle,
    ): List<DrawCommand> {
        val out = ArrayList<DrawCommand>(boxes.size * 8)
        for (box in boxes) {
            val style = box.style
            val cx = (box.boxBounds.left + box.boxBounds.right) / 2.0
            // Whiskers from the box edges out to max/min, with end caps.
            out += DrawCommand.Line(OffsetD(cx, box.boxBounds.top), box.whiskerTop, style.whiskerColor, style.strokeWidth)
            out += DrawCommand.Line(OffsetD(cx, box.boxBounds.bottom), box.whiskerBottom, style.whiskerColor, style.strokeWidth)
            out +=
                DrawCommand.Line(
                    OffsetD(cx - box.capHalfWidth, box.whiskerTop.y),
                    OffsetD(cx + box.capHalfWidth, box.whiskerTop.y),
                    style.whiskerColor,
                    style.strokeWidth,
                )
            out +=
                DrawCommand.Line(
                    OffsetD(cx - box.capHalfWidth, box.whiskerBottom.y),
                    OffsetD(cx + box.capHalfWidth, box.whiskerBottom.y),
                    style.whiskerColor,
                    style.strokeWidth,
                )
            // Box fill + border.
            out += DrawCommand.Rect(bounds = box.boxBounds, color = style.boxColor.withAlpha(style.boxFillAlpha))
            out +=
                DrawCommand.RoundRect(
                    bounds = box.boxBounds,
                    cornerRadius = 0.0,
                    style = ShapeDrawStyle.Stroke(color = style.borderColor, strokeWidth = style.strokeWidth),
                )
            // Median line across the box.
            out +=
                DrawCommand.Line(
                    OffsetD(box.boxBounds.left, box.medianY),
                    OffsetD(box.boxBounds.right, box.medianY),
                    style.medianColor,
                    style.strokeWidth,
                )
            for (center in box.outlierCenters) {
                out += DrawCommand.Circle(center = center, radius = style.outlierRadius, color = style.outlierColor)
            }
            if (selection != null &&
                selection.target.seriesId == box.seriesId &&
                selection.target.pointIndex == box.index
            ) {
                out +=
                    DrawCommand.RoundRect(
                        bounds = box.boxBounds,
                        cornerRadius = 0.0,
                        style =
                            ShapeDrawStyle.Stroke(
                                color = selectionStyle.lineColor,
                                strokeWidth = selectionStyle.lineStrokeWidth * 2.0,
                            ),
                    )
            }
        }
        return out
    }

    /**
     * Waterfall chart: each step's bar floats from the running total before the step to the running total after it
     * (increases / decreases / totals colored distinctly), with optional connector lines between consecutive bars.
     * Selection strokes an accent around the selected bar.
     *
     * @param selection optional bar highlight: matches when `selection.target.pointIndex` is the entry index.
     * @param tickValuePass optional precomputed pass-2 tick values for [spec]'s primary axes (default: compute)
     */
    fun planWaterfallChart(
        input: ChartLayoutInput,
        spec: WaterfallChartSpec,
        selection: ChartSelection? = null,
        tickValuePass: TickValuePass? = null,
    ): WaterfallChartPlan {
        val frame =
            layoutCartesianFrame(
                input = input,
                xAxis = spec.xAxis,
                yAxis = spec.yAxis,
                legend = LegendSpec(),
                contentPadding = spec.contentPadding,
                legendLabelColors = emptyList(),
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
        val bars = computePlannedWaterfallBars(spec, frame.transform, frame.plotBounds)
        plotCommands += buildWaterfallDrawCommands(bars, spec, frame.transform, selection)

        val overlayCommands =
            buildAxisAndTickLabelsOverlay(
                frame.plotBounds,
                spec.xAxis,
                spec.yAxis,
                frame.xTicks,
                frame.yTicks,
            )

        return WaterfallChartPlan(
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
            bars = bars,
        )
    }

    private fun computePlannedWaterfallBars(
        spec: WaterfallChartSpec,
        transform: ChartTransform,
        plotBounds: RectD,
    ): List<PlannedWaterfallBar> {
        if (spec.entries.isEmpty()) return emptyList()
        val half = spec.barWidthPx / 2.0
        val out = mutableListOf<PlannedWaterfallBar>()
        var cumulative = 0.0
        spec.entries.forEachIndexed { index, e ->
            val startValue: Double
            val endValue: Double
            val color: ChartColor
            val isIncrease: Boolean
            if (e.isTotal) {
                startValue = 0.0
                endValue = cumulative
                color = spec.totalColor
                isIncrease = cumulative >= 0.0
            } else {
                startValue = cumulative
                endValue = cumulative + e.delta
                cumulative = endValue
                color = if (e.delta >= 0.0) spec.increaseColor else spec.decreaseColor
                isIncrease = e.delta >= 0.0
            }
            val cx = transform.toPixel(XyPoint(index.toDouble(), 0.0)).x
            if (!VisibleRangeCalculator.barColumnIntersectsPlotHorizontally(cx, spec.barWidthPx, plotBounds)) {
                return@forEachIndexed
            }
            val yA = transform.toPixel(XyPoint(index.toDouble(), startValue)).y
            val yB = transform.toPixel(XyPoint(index.toDouble(), endValue)).y
            val top = min(yA, yB)
            var bottom = max(yA, yB)
            if (bottom - top < 1.0) bottom = top + 1.0
            out +=
                PlannedWaterfallBar(
                    index = index,
                    entry = e,
                    bounds = RectD(cx - half, top, cx + half, bottom),
                    startValue = startValue,
                    endValue = endValue,
                    cumulativeAfter = cumulative,
                    isTotal = e.isTotal,
                    isIncrease = isIncrease,
                    color = color,
                )
        }
        return out
    }

    private fun buildWaterfallDrawCommands(
        bars: List<PlannedWaterfallBar>,
        spec: WaterfallChartSpec,
        transform: ChartTransform,
        selection: ChartSelection?,
    ): List<DrawCommand> {
        val out = ArrayList<DrawCommand>(bars.size * 3)
        if (spec.connectors && bars.size > 1) {
            for (i in 0 until bars.size - 1) {
                val a = bars[i]
                val b = bars[i + 1]
                val y = transform.toPixel(XyPoint(0.0, a.cumulativeAfter)).y
                out +=
                    DrawCommand.Line(
                        start = OffsetD(a.bounds.right, y),
                        end = OffsetD(b.bounds.left, y),
                        color = spec.connectorColor,
                        strokeWidth = spec.connectorStrokeWidth,
                    )
            }
        }
        for (bar in bars) {
            out += DrawCommand.Rect(bounds = bar.bounds, color = bar.color)
            if (selection != null &&
                selection.target.seriesId == WaterfallHitResolver.SERIES_ID &&
                selection.target.pointIndex == bar.index
            ) {
                out +=
                    DrawCommand.RoundRect(
                        bounds = bar.bounds,
                        cornerRadius = 0.0,
                        style =
                            ShapeDrawStyle.Stroke(
                                color = spec.selectionStyle.lineColor,
                                strokeWidth = spec.selectionStyle.lineStrokeWidth * 2.0,
                            ),
                    )
            }
        }
        return out
    }

    private fun buildLineSeriesCommands(
        series: LineSeries,
        viewport: ChartViewport,
        transform: ChartTransform,
        plotBounds: RectD,
        lineRender: LineRenderSpec,
    ): List<DrawCommand> {
        // Split into runs of finite points separated by gap markers (non-finite y). Each run is windowed,
        // decimated, and drawn independently so the stroke/area break at gaps; connectAcrossGaps bridges them.
        val runs = finiteLineRuns(series.toXyPoints(), lineRender.connectAcrossGaps)
        if (runs.isEmpty()) return emptyList()

        val commands = mutableListOf<DrawCommand>()
        val pr = series.pointStyle.radius.coerceAtLeast(0.0)
        for (run in runs) {
            val indexed = LineRenderPipeline.prepareIndexed(run, viewport, lineRender)
            if (indexed.isEmpty()) continue

            val pixelPoints = ArrayList<OffsetD>(indexed.size)
            for ((_, p) in indexed) {
                pixelPoints.add(transform.toPixel(p))
            }
            // Area fill (if configured) renders beneath the line, built from the display pixel points; the
            // plot-bounds clip applied by the renderer trims any overflow.
            if (series.lineStyle.interpolation == LineInterpolation.MonotoneCubic) {
                val topEdge = monotoneCubicSegments(pixelPoints)
                buildAreaFillCommand(pixelPoints, plotBounds, transform, series.lineStyle, curvedTopEdge = topEdge)
                    ?.let { commands += it }
                if (pixelPoints.size >= 2) {
                    commands +=
                        DrawCommand.Path(
                            segments = topEdge,
                            style =
                                PathDrawStyle.Stroke(
                                    color = series.lineStyle.color,
                                    strokeWidth = series.lineStyle.strokeWidth,
                                ),
                        )
                }
            } else {
                val displayPoints =
                    if (series.lineStyle.interpolation == LineInterpolation.Linear) {
                        pixelPoints
                    } else {
                        steppedLinePoints(pixelPoints, series.lineStyle.interpolation)
                    }
                buildAreaFillCommand(displayPoints, plotBounds, transform, series.lineStyle)?.let { commands += it }
                for (fragment in PolylineRectClip.clipPolylineToRect(displayPoints, plotBounds)) {
                    commands +=
                        DrawCommand.Polyline(
                            points = fragment,
                            color = series.lineStyle.color,
                            strokeWidth = series.lineStyle.strokeWidth,
                        )
                }
            }
            if (series.pointStyle.visible) {
                for (pixel in pixelPoints) {
                    if (!plotBounds.intersectsDisc(pixel, pr)) continue
                    commands +=
                        DrawCommand.Circle(
                            center = pixel,
                            radius = pr,
                            color = series.pointStyle.color ?: series.lineStyle.color,
                        )
                }
            }
        }

        return commands
    }

    /**
     * Partitions [points] into runs of finite samples separated by gap markers (non-finite x or y). When
     * [connectAcrossGaps] is `true`, returns a single run with the gap points removed (bridging the holes).
     */
    private fun finiteLineRuns(
        points: List<XyPoint>,
        connectAcrossGaps: Boolean,
    ): List<List<XyPoint>> {
        if (points.isEmpty()) return emptyList()
        if (connectAcrossGaps) {
            val kept = points.filter { it.x.isFinite() && it.y.isFinite() }
            return if (kept.isEmpty()) emptyList() else listOf(kept)
        }
        val runs = ArrayList<List<XyPoint>>()
        var current = ArrayList<XyPoint>()
        for (p in points) {
            if (p.x.isFinite() && p.y.isFinite()) {
                current.add(p)
            } else if (current.isNotEmpty()) {
                runs.add(current)
                current = ArrayList()
            }
        }
        if (current.isNotEmpty()) runs.add(current)
        return runs
    }

    /**
     * Builds the optional area-fill command beneath a line series, or `null` when [LineStyle.areaFill] is unset.
     * The polygon spans the (unclipped) line pixel points down to a baseline determined by [LineStyle.areaFillMode];
     * [AreaFillMode.BelowThreshold] wraps the fill in a [DrawCommand.ClipRect] so only the sub-threshold band shows.
     */
    private fun buildAreaFillCommand(
        pixelPoints: List<OffsetD>,
        plotBounds: RectD,
        transform: ChartTransform,
        style: LineStyle,
        curvedTopEdge: List<PathSegment>? = null,
    ): DrawCommand? {
        val fill = style.areaFill ?: return null
        if (pixelPoints.size < 2) return null
        val baselineY =
            when (val mode = style.areaFillMode) {
                AreaFillMode.ToPlotBottom -> plotBounds.bottom
                is AreaFillMode.ToBaseline ->
                    transform.toPixel(XyPoint(0.0, mode.value)).y.coerceIn(plotBounds.top, plotBounds.bottom)
                is AreaFillMode.BelowThreshold ->
                    transform.toPixel(XyPoint(0.0, mode.value)).y.coerceIn(plotBounds.top, plotBounds.bottom)
            }
        val segments =
            if (curvedTopEdge != null) {
                monotoneCubicAreaSegments(pixelPoints, curvedTopEdge, baselineY) ?: return null
            } else {
                val straight = ArrayList<PathSegment>(pixelPoints.size + 3)
                straight += PathSegment.MoveTo(OffsetD(pixelPoints.first().x, baselineY))
                for (p in pixelPoints) straight += PathSegment.LineTo(p)
                straight += PathSegment.LineTo(OffsetD(pixelPoints.last().x, baselineY))
                straight += PathSegment.Close
                straight
            }

        val pathStyle =
            if (style.areaFillEnd != null) {
                var topY = baselineY
                var bottomY = baselineY
                for (p in pixelPoints) {
                    if (p.y < topY) topY = p.y
                    if (p.y > bottomY) bottomY = p.y
                }
                PathDrawStyle.VerticalGradientFill(
                    topColor = fill,
                    bottomColor = style.areaFillEnd,
                    topY = topY,
                    bottomY = bottomY,
                )
            } else {
                PathDrawStyle.Fill(color = fill)
            }
        val path = DrawCommand.Path(segments = segments, style = pathStyle)
        return if (style.areaFillMode is AreaFillMode.BelowThreshold) {
            DrawCommand.ClipRect(
                bounds = RectD(left = plotBounds.left, top = baselineY, right = plotBounds.right, bottom = plotBounds.bottom),
                children = listOf(path),
            )
        } else {
            path
        }
    }

    /**
     * Builds dashed/solid [DrawCommand.Line]s (+ optional label) for [referenceLines], mapped through [transform]
     * and spanning [plotBounds]. Returned commands are appended above the series and clipped to the plot, so a
     * line whose value is off-screen is naturally hidden.
     */
    private fun buildReferenceLineCommands(
        referenceLines: List<ReferenceLine>,
        transform: ChartTransform,
        plotBounds: RectD,
    ): List<DrawCommand> {
        if (referenceLines.isEmpty()) return emptyList()
        val commands = ArrayList<DrawCommand>(referenceLines.size)
        for (ref in referenceLines) {
            val stroke = ref.dashIntervals?.takeIf { it.isNotEmpty() }?.let { StrokeStyle(dashIntervals = it) }
            when (ref.orientation) {
                ReferenceOrientation.Horizontal -> {
                    val y = transform.toPixel(XyPoint(0.0, ref.value)).y
                    commands +=
                        DrawCommand.Line(
                            start = OffsetD(plotBounds.left, y),
                            end = OffsetD(plotBounds.right, y),
                            color = ref.color,
                            strokeWidth = ref.strokeWidth,
                            strokeStyle = stroke,
                        )
                    ref.label?.let { label ->
                        commands += DrawCommand.Text(text = label, position = OffsetD(plotBounds.left + 4.0, y + 2.0), color = ref.color)
                    }
                }
                ReferenceOrientation.Vertical -> {
                    val x = transform.toPixel(XyPoint(ref.value, 0.0)).x
                    commands +=
                        DrawCommand.Line(
                            start = OffsetD(x, plotBounds.top),
                            end = OffsetD(x, plotBounds.bottom),
                            color = ref.color,
                            strokeWidth = ref.strokeWidth,
                            strokeStyle = stroke,
                        )
                    ref.label?.let { label ->
                        commands += DrawCommand.Text(text = label, position = OffsetD(x + 4.0, plotBounds.top + 4.0), color = ref.color)
                    }
                }
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
        val noDecimationCap = scatterRender.maxPointsPerSeries <= 0
        val prepared: List<Pair<Int, XyPoint>> =
            if (noDecimationCap) {
                series.points.mapIndexed { i, p -> i to XyPoint(p.x, p.y) }
            } else {
                ScatterRenderPipeline.prepareIndexed(series.toXyPoints(), viewport, scatterRender)
            }
        if (prepared.isEmpty()) return emptyList()
        val commands = ArrayList<DrawCommand>(min(prepared.size, 4096))
        for ((idx, xy) in prepared) {
            val pixel = transform.toPixel(xy)
            val pr = series.resolvedRadiusAt(idx)
            if (!plotBounds.intersectsDisc(pixel, pr)) continue
            commands += markerCommand(pixel, pr, series.resolvedColorAt(idx), series.resolvedShapeAt(idx))
        }
        return commands
    }

    /** Builds the [DrawCommand] for one scatter marker glyph of radius [r] centered at [center]. */
    private fun markerCommand(
        center: OffsetD,
        r: Double,
        color: ChartColor,
        shape: MarkerShape,
    ): DrawCommand =
        when (shape) {
            MarkerShape.Circle -> DrawCommand.Circle(center = center, radius = r, color = color)
            MarkerShape.Square ->
                DrawCommand.Rect(
                    bounds = RectD(center.x - r, center.y - r, center.x + r, center.y + r),
                    color = color,
                )
            MarkerShape.Triangle ->
                DrawCommand.Path(
                    segments =
                        listOf(
                            PathSegment.MoveTo(OffsetD(center.x, center.y - r)),
                            PathSegment.LineTo(OffsetD(center.x - r, center.y + r)),
                            PathSegment.LineTo(OffsetD(center.x + r, center.y + r)),
                            PathSegment.Close,
                        ),
                    style = PathDrawStyle.Fill(color = color),
                )
            MarkerShape.Diamond ->
                DrawCommand.Path(
                    segments =
                        listOf(
                            PathSegment.MoveTo(OffsetD(center.x, center.y - r)),
                            PathSegment.LineTo(OffsetD(center.x + r, center.y)),
                            PathSegment.LineTo(OffsetD(center.x, center.y + r)),
                            PathSegment.LineTo(OffsetD(center.x - r, center.y)),
                            PathSegment.Close,
                        ),
                    style = PathDrawStyle.Fill(color = color),
                )
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
        percentStack: Boolean = false,
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
                                percentStack,
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
                                percentStack,
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

    /** A stacked bar segment's positive contribution to its category total (non-finite / negative → 0). */
    private fun positiveStackValue(v: Double): Double = if (v.isFinite() && v > 0.0) v else 0.0

    private fun buildStackedBarSegments(
        groups: List<BarGroup>,
        viewport: ChartViewport,
        transform: ChartTransform,
        plotBounds: RectD,
        barLayoutMode: BarLayoutMode,
        layerId: String?,
        percentStack: Boolean = false,
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
            val categoryTotal = if (percentStack) groups.sumOf { positiveStackValue(it.entries[j].y) } else 0.0
            var cumulative = y0
            for (group in groups) {
                val entry = group.entries[j]
                val contribution =
                    if (percentStack) {
                        if (categoryTotal > 0.0) positiveStackValue(entry.y) / categoryTotal else 0.0
                    } else {
                        entry.y
                    }
                val yLo = cumulative
                val yHi = cumulative + contribution
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
        percentStack: Boolean = false,
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
            val categoryTotal = if (percentStack) groups.sumOf { positiveStackValue(it.entries[j].y) } else 0.0
            var cumulative = x0
            for (group in groups) {
                val entry = group.entries[j]
                val contribution =
                    if (percentStack) {
                        if (categoryTotal > 0.0) positiveStackValue(entry.y) / categoryTotal else 0.0
                    } else {
                        entry.y
                    }
                val xLo = cumulative
                val xHi = cumulative + contribution
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
    ): RectD =
        barRectWithPixelCenter(
            group = group,
            xDomain = xDomain,
            yTopDomain = yTopDomain,
            viewport = viewport,
            transform = transform,
            plotBounds = plotBounds,
            centerXPx = transform.toPixel(XyPoint(xDomain, viewport.yRange.start)).x,
        )

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
     * Horizontal bar: category on Y ([categoryDomain]), value length along X from `viewport.xRange.start`.
     */
    private fun barRectForHorizontalRow(
        group: BarGroup,
        categoryDomain: Double,
        valueDomain: Double,
        viewport: ChartViewport,
        transform: ChartTransform,
        plotBounds: RectD,
        centerYPx: Double,
    ): RectD =
        barRectWithPixelCenterHorizontal(
            group = group,
            categoryDomain = categoryDomain,
            valueDomain = valueDomain,
            viewport = viewport,
            transform = transform,
            plotBounds = plotBounds,
            centerYPx = centerYPx,
        )

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
    ): List<DrawCommand> =
        segments.flatMap { seg ->
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
            cachedXs != null &&
            cachedXs.size == nx &&
            cachedYs != null &&
            cachedYs.size == ny
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
     * @param reserveRightYAxisSpace When true, reserves canvas width for [CombinedCartesianChartSpec.yAxisSecondary]
     * tick labels (after measurement pass).
     * @param tickValuePass optional precomputed pass-2 tick values for [spec]'s primary X / shared Y axes (default: compute).
     */
    fun planCombinedCartesianChart(
        input: ChartLayoutInput,
        spec: CombinedCartesianChartSpec,
        selection: ChartSelection?,
        reserveRightYAxisSpace: Boolean = true,
        tickValuePass: TickValuePass? = null,
        secondaryYOverride: ClosedRangeD? = null,
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
                combinedSecondaryViewport(input.viewport, spec, secondaryYOverride)
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
                                val n =
                                    layer.groups
                                        .first()
                                        .entries.size
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

        plotCommands += buildReferenceLineCommands(spec.referenceLines, transformPrimary, frame.plotBounds)

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
        return buildLineStyleSelectionOverlay(plotBounds, selection.pixelOffset, spec.selectionStyle)
    }

    /**
     * Vertical guideline + knot at [anchor]. Callers that own a [ChartTransform] pass the projection of
     * `selection.target.point` so the overlay tracks the data rather than a possibly stale caller-supplied
     * pixel; callers without one (combined bar segments) pass the selection's own planned pixel.
     */
    private fun buildLineStyleSelectionOverlay(
        plotBounds: RectD,
        anchor: OffsetD,
        selectionStyle: SelectionStyle,
    ): List<DrawCommand> =
        listOf(
            DrawCommand.Line(
                start = OffsetD(anchor.x, plotBounds.top),
                end = OffsetD(anchor.x, plotBounds.bottom),
                color = selectionStyle.lineColor,
                strokeWidth = selectionStyle.lineStrokeWidth,
            ),
            DrawCommand.Circle(
                center = anchor,
                radius = selectionStyle.pointRadius,
                color = selectionStyle.pointColor,
            ),
        )

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
        override: ClosedRangeD? = null,
    ): ChartViewport {
        if (override != null) {
            return ChartViewport(xRange = primaryViewport.xRange, yRange = override)
        }
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

    // ── Heatmap ──────────────────────────────────────────────────────────────────────────────────

    /**
     * Plans a heatmap chart: a 2-D grid of colored cells where each cell's color encodes a value.
     *
     * Both axes are forced to [AxisScale.Category] using [HeatmapChartSpec.columnLabels] and
     * [HeatmapChartSpec.rowLabels]. The [HeatmapChartSpec.xAxis] and [HeatmapChartSpec.yAxis] fields
     * control styling (label color, line visibility) but their [AxisSpec.scale] is ignored.
     *
     * Cells that reference out-of-range column/row indices are silently skipped.
     */
    fun planHeatmapChart(
        input: ChartLayoutInput,
        spec: HeatmapChartSpec,
        selection: ChartSelection? = null,
        tickValuePass: TickValuePass? = null,
    ): HeatmapChartPlan {
        val cols = spec.columnLabels.size
        val rows = spec.rowLabels.size

        val xAxisWithCat = spec.xAxis.copy(scale = AxisScale.Category(spec.columnLabels))
        val yAxisWithCat = spec.yAxis.copy(scale = AxisScale.Category(spec.rowLabels))

        val frame =
            layoutCartesianFrame(
                input = input,
                xAxis = xAxisWithCat,
                yAxis = yAxisWithCat,
                legend = spec.legend,
                contentPadding = spec.contentPadding,
                legendLabelColors = emptyList(),
                tickValuePass = tickValuePass,
            )

        val plotCommands = mutableListOf<DrawCommand>()
        val cellW = if (cols > 0) frame.plotBounds.width / cols else 0.0
        val cellH = if (rows > 0) frame.plotBounds.height / rows else 0.0
        val halfGap = spec.cellGapPx / 2.0

        val plannedCells = mutableListOf<PlannedHeatmapCell>()
        spec.cells.forEachIndexed { idx, cell ->
            if (cell.col < 0 || cell.col >= cols || cell.row < 0 || cell.row >= rows) return@forEachIndexed
            val left = frame.plotBounds.left + cell.col * cellW + halfGap
            val right = left + cellW - spec.cellGapPx
            val bottom = frame.plotBounds.bottom - cell.row * cellH - halfGap
            val top = bottom - cellH + spec.cellGapPx
            if (right <= left || bottom <= top) return@forEachIndexed
            val bounds = RectD(left, top, right, bottom)
            val color = spec.colorScale.colorFor(cell.value)
            plannedCells += PlannedHeatmapCell(cell.col, cell.row, cell.value, color, bounds, idx)
            plotCommands += DrawCommand.Rect(bounds, color)
        }

        // Selection highlight: stroke border drawn on top of the selected cell
        if (selection != null) {
            val selCell =
                plannedCells.firstOrNull {
                    it.cellIndex == selection.target.pointIndex &&
                        selection.target.seriesId == spec.id
                }
            if (selCell != null) {
                plotCommands +=
                    DrawCommand.RoundRect(
                        bounds = selCell.bounds,
                        cornerRadius = 0.0,
                        style =
                            ShapeDrawStyle.Stroke(
                                color = spec.selectionStyle.lineColor,
                                strokeWidth = spec.selectionStyle.lineStrokeWidth * 2,
                            ),
                    )
            }
        }

        val overlayCommands =
            buildAxisAndTickLabelsOverlay(
                frame.plotBounds,
                xAxisWithCat,
                yAxisWithCat,
                frame.xTicks,
                frame.yTicks,
            )

        return HeatmapChartPlan(
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
            heatmapCells = plannedCells,
        )
    }
}
