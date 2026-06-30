# Changelog

All notable changes to `ComposePlotLib` are documented here.

Versions are published to GitHub Packages from the Release workflow: pushing a semver tag like
`v1.1.0` publishes version `1.1.0` (see `README.md` → Install).

## 1.1.0 — unreleased

Additive, backward-compatible release. Existing specs keep their prior behavior with defaults.

### Added

- **Area / threshold-band fill on `LineChart`** — `LineStyle.areaFill`, `LineStyle.areaFillEnd`
  (vertical gradient), and `LineStyle.areaFillMode` (`AreaFillMode.ToPlotBottom`,
  `ToBaseline(value)`, `BelowThreshold(value)`).
- **Reference / threshold lines** — `ReferenceLine` (`ReferenceOrientation.Horizontal` / `Vertical`,
  `value`, `color`, `strokeWidth`, `dashIntervals`, `label`) via a `referenceLines` field on
  `LineChartSpec`, `BarChartSpec`, `ScatterChartSpec`, and `CombinedCartesianChartSpec`.
- **Marker shapes + per-point styling** — `MarkerShape` (`Circle` / `Square` / `Triangle` /
  `Diamond`) and `PointStyle.shape`, plus per-point overrides `ChartPoint.markerShape`,
  `ChartPoint.markerColor`, and `ChartPoint.markerRadius`.
- **Bubble sizing on `ScatterChart`** — a per-point `ChartPoint.markerRadius` override encodes a
  third dimension as point size.
- **Gauge chart (new family)** — `GaugeChart` / `GaugeChartSpec`: a radial progress ring with an
  optional `centerContent` slot; non-interactive.
- **Candlestick chart (new family)** — `CandlestickChart` / `CandlestickChartSpec` with `OHLCPoint`,
  `CandlestickSeries`, `CandlestickStyle`, `CandlestickRenderSpec`, and `CandlestickValueFormatter`;
  cartesian bodies + wicks, bull/bear coloring, pan/zoom, decimation, and a tap-to-OHLC marker.
  (Library shipped; app adoption is gated on an OHLC data feed.)
- **Sparkline** — a thin, axis-less / grid-less / non-interactive wrapper over `LineChart` with an
  optional area fill.

### Internal

- One new draw primitive: `PathDrawStyle.VerticalGradientFill` (for line area / threshold-band
  gradient fill). The planning → draw-command → canvas pipeline is otherwise unchanged.

### Publishing

- Publishing happens on a `v1.1.0` git tag (GitHub Packages, via the Release workflow).
