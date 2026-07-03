package com.mena97villalobos.composeplotlib.core.spec

/**
 * Axis appearance and tick behavior shared by line and bar charts.
 *
 * @property enabled When false, this axis is dropped from layout (no ticks, grid lines, or labels). Defaults to true.
 * @property tickCount Target number of tick **values** for linear / time scales (capped by [tickDensity] after layout).
 * @property scale How tick **values** are chosen along the domain; defaults to [AxisScale.Linear]. See [AxisScale].
 * @property tickDensity Post-layout thinning strategy for tick values once the plot span is known; defaults to
 *   [AxisTickDensity.Auto].
 * @property tickMin When non-null, the minimum domain value used when searching for ticks (intersected with the viewport).
 * @property tickMax When non-null, the maximum domain value used when searching for ticks (intersected with the viewport).
 * @property showGridLines When true, grid lines follow tick positions on that axis (subject to [GridSpec]).
 * @property defaultDensitySpacingPx Used by [AxisTickDensity.Auto] to derive a maximum tick count from plot span.
 * @property categoryLabelMaxChars When non-null and the scale is [AxisScale.Category], tick labels are truncated
 *   to this many Unicode code units with an ellipsis suffix (`…`) if longer. Null keeps full strings.
 * @property epochMillisTickPolicy Applied when [scale] is [AxisScale.TimeEpochMillis]; see [EpochMillisTickPolicy].
 * @property xTickLabelOverlapStrategy How to resolve overlapping X tick labels (see [XTickLabelOverlapStrategy]).
 * @property minVisibleXTickLabels Soft minimum number of visible X tick **labels** after overlap resolution (see KDoc).
 * @property minVisibleYTickLabels Soft minimum number of visible Y tick **labels** after overlap resolution (see KDoc).
 * @property raiseTickDensityCapFromMeasuredLabelWidth When true, raises the tick-density cap using plot span and average measured label width (see KDoc).
 * @property labelFormatter Maps each tick **value** to its label text; defaults to [DefaultAxisLabelFormatter].
 * @property labelStyle Text appearance for tick labels; see [AxisLabelStyle].
 * @property lineStyle Axis line and tick-mark stroke appearance; see [AxisLineStyle].
 */
data class AxisSpec(
    val enabled: Boolean = true,
    val tickCount: Int = 5,
    val scale: AxisScale = AxisScale.Linear,
    val tickMin: Double? = null,
    val tickMax: Double? = null,
    val tickDensity: AxisTickDensity = AxisTickDensity.Auto,
    val defaultDensitySpacingPx: Double = 48.0,
    val categoryLabelMaxChars: Int? = null,
    val epochMillisTickPolicy: EpochMillisTickPolicy = EpochMillisTickPolicy.All,
    val xTickLabelOverlapStrategy: XTickLabelOverlapStrategy = XTickLabelOverlapStrategy.HideOverlapping,
    /**
     * Soft target: after [XTickLabelOverlapStrategy.HideOverlapping] suppression, the planner tries to re-enable
     * hidden labels (without introducing new overlaps) until at least this many are visible, or no further progress.
     * Ignored for [XTickLabelOverlapStrategy.StaggerAdjacent] (stagger has its own row/collision rules).
     * If overlap suppression and canvas clipping still leave fewer labels, the target may not be met.
     */
    val minVisibleXTickLabels: Int? = null,
    /**
     * Soft target: after Y overlap suppression, re-enable hidden labels when possible until at least this many
     * are visible. May be impossible when labels are too tall or the axis span is too small.
     */
    val minVisibleYTickLabels: Int? = null,
    /**
     * When true, increases the effective tick-density cap using `floor(plotSpanPx / (avgMeasuredLabelWidth + gapPx))`
     * (gap from [minHorizontalTickLabelGapPx] or the library default for X, [minVerticalTickLabelGapPx] or default for Y),
     * combined with `max(computedCap, thatFloor)`. Only **raises** the cap when measured labels are narrow; it does not
     * force extra thinning when labels are wide. Uses average width of **pass-2** tick label strings for that axis.
     */
    val raiseTickDensityCapFromMeasuredLabelWidth: Boolean = false,
    val showGridLines: Boolean = true,
    val labelFormatter: AxisLabelFormatter = DefaultAxisLabelFormatter,
    /** Overrides default horizontal gap between x tick labels when labels would overlap (pixels). */
    val minHorizontalTickLabelGapPx: Double? = null,
    /** Overrides default vertical gap between y tick labels when labels would overlap (pixels). */
    val minVerticalTickLabelGapPx: Double? = null,
    val labelStyle: AxisLabelStyle = AxisLabelStyle(),
    val lineStyle: AxisLineStyle = AxisLineStyle(),
    /**
     * When true, this axis is drawn **reversed** — larger domain values map toward the opposite edge (X:
     * right→left; Y: the usual bottom→top becomes top→bottom). Affects the plot transform, so data, grid, ticks,
     * markers, and selection all follow, on every cartesian family. Pan/zoom direction is additionally reversed for
     * `LineChart`, `ScatterChart`, and `BarChart`; other cartesian families render reversed but keep the standard
     * pan direction.
     */
    val inverted: Boolean = false,
)
