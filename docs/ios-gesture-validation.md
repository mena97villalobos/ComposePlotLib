# iOS gesture parity (manual QA)

Run these checks on **physical** iPhone/iPad hosts (simulator is useful but not sufficient for touch timing and multi-touch routing).

Library-wide iOS status and blocking issues: **`production-readiness-next-steps.md`** (Cross-Platform → iOS). Host setup: **`ios-host-runbook.md`**.

## Recommended configs (copy-paste)

Use **`ChartGestureConfig`** presets so Android and iOS hosts exercise the **same** policy without duplicating literals:

| Goal | Kotlin |
|------|--------|
| Cartesian default (tap + pan/pinch; no double-tap / fling) | `ChartGestureConfig.CartesianDefault` or `ChartGestureConfig()` |
| Shared host / demo line style (double-tap zoom-in) | `ChartGestureConfig.cartesianInteractive()` — same as **`ComposePlotApp`** line tab |
| Full cartesian QA (fling + double-tap zoom out + reset + zoom in) | `ChartGestureConfig.cartesianInteractiveFullExploration()` — same as `cartesianInteractive(doubleTapZoomOutEnabled = true, doubleTapResetViewportEnabled = true, panFlingEnabled = true)` (zoom-in stays on) |
| Full cartesian QA (manual flag bundle) | `ChartGestureConfig.cartesianInteractive(doubleTapZoomOutEnabled = true, doubleTapResetViewportEnabled = true, panFlingEnabled = true)` then trim flags per row below |
| Pan/zoom only (no tap selection) | `ChartGestureConfig.exploreOnly()` |
| Static chart (no gestures) | `ChartGestureConfig.readOnly()` |
| Pie / donut | `ChartGestureConfig.PieDefault` (also the default on [PieChart]) |

Always combine with the same **`ChartViewportState`** domain, **`ViewportConstraints`**, and **`ViewportAxisPolicy`** you intend to ship; axis lock can disable pan/zoom on a per-axis basis even when `ChartTransformGesturePolicy` leaves them enabled (see **`docs/viewport-configuration.md`**).

## Line / bar / scatter (shared cartesian pipeline)

Use a screen that shows [LineChart], [BarChart], or [ScatterChart] with data, `transform.enabled = true`, and (optionally) `fling.panFlingEnabled = true`, `transform.doubleTapZoomInEnabled`, `transform.doubleTapZoomOutEnabled`, and/or `transform.doubleTapResetViewportEnabled`. Prefer **`ChartGestureConfig.cartesianInteractive(...)`** when you want parity with **`ComposePlotApp`** without hand-building nested policies.

| Check | Pass criteria |
|--------|----------------|
| Single-finger pan | Pan tracks finger; viewport stays clamped to domain/constraints. |
| Pinch zoom | Zoom centers sensibly; axis locks from [ViewportAxisPolicy] still respected. |
| Double-tap zoom / reset | Only fires when the corresponding flag is set; **reset** wins over **zoom out** wins over **zoom in** if multiple are enabled; hit must be inside plot; zoom-out scale is &lt; 1; reset matches [ChartViewportState.reset]. |
| Fling | After quick pan release, inertial drift matches direction; new pan cancels in-flight fling. |
| Tap vs pan | Quick tap selects nearest point (line/scatter) or bar (bar); small movement does not mis-fire as pan (slop feels natural). |
| After pinch | Pan-only fling does not use mixed pinch samples; no runaway velocity. |
| Keyboard (optional) | With `ChartAccessibilitySpec.keyboardViewportNudgeEnabled` and a focused chart: arrow keys pan; +/− zoom; Escape clears selection when tap selection is on. |

## Policy toggles

Confirm [ChartGestureConfig] groups behave independently:

- `tap.selectionEnabled = false` — no selection; pan/zoom unchanged if transform is on.
- `transform.enabled = false` — no pan/zoom/double-tap; tap selection can still work.
- `fling.panFlingEnabled = false` — no inertial coasting.

## Notes

- [ChartFlingGesturePolicy] uses a short **debounce** after the last transform event to approximate gesture end; timing can differ slightly from Android.
- If a Compose Multiplatform pointer or drag issue appears only on iOS, capture OS version and file an issue upstream with a minimal repro.

## Validation record (maintainers)

| Date | Host (device/sim) | iOS version | Config preset | Result |
|------|-------------------|-------------|----------------|--------|
| 2026-04-06 | CI / dev (no touch) | n/a (compile only) | `compileKotlinIosSimulatorArm64` | **Pass** — shared `chart-compose` gesture code compiles for iOS simulator; does not validate touch timing, fling, or double-tap behavior. |
| *Add before release* | Physical iPhone/iPad | *e.g. 18.x* | `cartesianInteractive()` or `cartesianInteractiveFullExploration()` | *Pass/fail + notes after manual matrix* |

**Automated gate (no touch):** `./gradlew :composeApp:compileKotlinIosSimulatorArm64` (and device slice if needed) compiles shared gesture code for the iOS host; it does **not** replace a **physical** row. Add a device row when you rely on touch QA for a release.
