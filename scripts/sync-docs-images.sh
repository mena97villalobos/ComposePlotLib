#!/usr/bin/env bash
#
# Copies Roborazzi goldens into docs/images/, which is where the README and the wiki read them from.
#
# The two sets are the same pixels, but nothing enforced that: the copy was manual, so a re-record
# left docs/ showing whatever the goldens looked like the last time somebody remembered. Run this
# after `./gradlew :samples:android-showcase:recordRoborazziDebug`.
#
#   ./scripts/sync-docs-images.sh            # copy
#   ./scripts/sync-docs-images.sh --check     # fail if any doc image is stale (for CI)
set -euo pipefail

cd "$(dirname "$0")/.."

GOLDENS="samples/android-showcase/src/androidUnitTest/resources/roborazzi"
PREFIX="com.mena97villalobos.composeplotlib.samples.android.ShowcaseRoborazziTest"
DOCS="docs/images"

# docs/images name : golden test name
MAPPING="
bar-chart:barChart_roborazzi_selectionHighlighted
boxplot-chart:boxPlotChartShowcaseScreen
bubble-chart:bubbleChartShowcaseScreen
bullet-chart:bulletChartShowcaseScreen
calendar-heatmap:calendarHeatmapShowcaseScreen
candlestick-chart:candlestickChartShowcaseScreen
circlepack-chart:circlePackChartShowcaseScreen
combined-chart:combinedChartShowcaseScreen
funnel-chart:funnelChartShowcaseScreen
gauge-chart:gaugeChartShowcaseScreen
heatmap-chart:heatmapChartShowcaseScreen
line-chart:lineChart_roborazzi_selectionHighlighted
parallel-coordinates:parallelCoordinatesShowcaseScreen
pie-chart:pieChart_roborazzi_selectionHighlighted
polar-chart:polarChartShowcaseScreen
radar-chart:radarChartShowcaseScreen
radialbar-chart:radialBarChartShowcaseScreen
sankey-chart:sankeyChartShowcaseScreen
scatter-chart:scatterChart_roborazzi_selectionHighlighted
showcase-overview:showcaseApp_overview
stackedarea-chart:stackedAreaChartShowcaseScreen
sunburst-chart:sunburstChartShowcaseScreen
treemap-chart:treemapChartShowcaseScreen
violin-chart:violinChartShowcaseScreen
waterfall-chart:waterfallChartShowcaseScreen
"

check_only=false
[[ "${1:-}" == "--check" ]] && check_only=true

stale=0
copied=0
while IFS=: read -r doc golden; do
    [[ -z "$doc" ]] && continue
    src="$GOLDENS/$PREFIX.$golden.png"
    dst="$DOCS/$doc.png"

    if [[ ! -f "$src" ]]; then
        echo "missing golden: $src" >&2
        exit 1
    fi

    if cmp -s "$src" "$dst"; then
        continue
    fi

    if $check_only; then
        echo "stale: $dst"
        stale=$((stale + 1))
    else
        cp "$src" "$dst"
        echo "updated: $dst"
        copied=$((copied + 1))
    fi
done <<< "$MAPPING"

if $check_only; then
    if (( stale > 0 )); then
        echo "$stale image(s) out of date — run ./scripts/sync-docs-images.sh" >&2
        exit 1
    fi
    echo "docs/images is in sync with the goldens"
else
    echo "$copied image(s) updated"
fi
