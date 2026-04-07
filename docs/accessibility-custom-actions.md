# Accessibility custom actions

Charts expose optional TalkBack / VoiceOver **custom actions** on the chart container via `accessibilityCustomActions: List<ChartCustomAccessibilityAction>` (label + callback).

## Viewport shortcuts helper

[`chartViewportAccessibilityActions`](../chart-compose/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/chart/ChartCustomAccessibilityAction.kt) builds **Reset chart viewport**, and when zoom is allowed, **Zoom in** and **Zoom out** at the current window center, respecting axis locks via `ChartViewportState.axisPolicy`.

## Merging with app actions

Use [`chartAccessibilityActionsMergeViewport`](../chart-compose/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/chart/ChartCustomAccessibilityAction.kt) to place viewport actions **first**, then append product-specific actions (export, clear selection only, drill-down, etc.):

```kotlin
LineChart(
    spec = spec,
    viewportState = viewportState,
    accessibilityCustomActions = chartAccessibilityActionsMergeViewport(
        viewportState = viewportState,
        gestureConfig = ChartGestureConfig.cartesianInteractive(),
        appActions = listOf(
            ChartCustomAccessibilityAction("Clear selection only") {
                selectionState.clear()
            },
        ),
    ),
)
```

**Ordering:** viewport reset/zoom appear before app actions so users hear stable shortcuts first. Duplicate labels are allowed but avoid confusing duplicates (e.g. two “Reset” actions).

**Idempotency:** callbacks may run multiple times if the user repeats the action; keep them safe to invoke repeatedly.

## Related

- [api-and-state.md](api-and-state.md) — `ChartAccessibilitySpec`, keyboard roam, semantics
- [viewport-configuration.md](viewport-configuration.md) — keyboard pan/zoom vs Shift+arrow selection
