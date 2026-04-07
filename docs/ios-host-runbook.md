# iOS host runbook (ComposePlotLib)

This runbook validates the shared milestone UI (`ComposePlotApp` in `chart-compose` via `composeApp`) on an iOS simulator or device. **`ComposePlotApp` cycles line → bar → stacked bar → horizontal bar → scatter → pie → radar → combined → sankey → domain axes** (button). Shared KMP code paths are covered by the iOS compile tasks below. **`samples/android-showcase`** adds richer demos and Roborazzi (still Android-focused).

**Integrating charts in your own KMP+iOS app** (framework wiring, `ComposeUIViewController`, SwiftUI bridge): **[`ios-library-usage.md`](ios-library-usage.md)**.

For **library production gaps** (layout, gestures, performance, next features), see **`production-readiness-next-steps.md`** — especially **Cross-Platform Readiness → iOS** and gesture parity notes. For touch QA matrices and copy-paste **`ChartGestureConfig`** presets (**`cartesianInteractive`**, **`PieDefault`**, etc.), see **`docs/ios-gesture-validation.md`**.

## Prerequisites

- macOS with Xcode (project is set to iOS deployment **18.2** in `iosApp/iosApp.xcodeproj`; adjust if your Xcode requires it).
- JDK and Gradle as used elsewhere in the repo.
- Apple **Development Team** for code signing: set `TEAM_ID` in `iosApp/Configuration/Config.xcconfig` (empty placeholder by default).

## What the iOS shell runs

- SwiftUI hosts a `UIViewController` from Kotlin: `MainViewControllerKt.MainViewController()` → `ComposePlotApp()`.
- The first Xcode build phase runs Gradle: `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode` (working directory: **repository root** `ComposePlotLib/`, one level above `iosApp/`).

## Open the Xcode project

From the `ComposePlotLib` root:

```text
iosApp/iosApp.xcodeproj
```

## Command-line compile check (no Xcode UI)

Simulator (Apple Silicon host):

```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

Device:

```bash
./gradlew :composeApp:compileKotlinIosArm64
```

These tasks compile `chart-core`, `chart-compose`, and `composeApp` for the selected Apple target. Use them in CI or before opening Xcode when you only need a compile gate.

## Manual verification checklist

Use the detailed gesture table in **`docs/ios-gesture-validation.md`** when you need per-gesture pass/fail tracking; this list stays a quick smoke path.

On simulator or device, exercise `ComposePlotApp`:

1. **Line screen**: pinch zoom and two-finger pan; tap to select a point; confirm marker and subtitle update; **double-tap inside the plot zooms in** (`ChartGestureConfig.cartesianInteractive()` — match this preset in your own app when comparing behavior).
2. **Bar screen**: same gestures; tap bars (including overlapping series); confirm marker and subtitle.
3. **Stacked bar screen**: tap a stacked segment; confirm marker and subtitle.
4. **Horizontal bar screen**: tap a horizontal bar; confirm marker and subtitle.
5. **Scatter screen**: switch until the scatter preview appears; pinch/pan; tap to select a point; confirm marker and subtitle.
6. **Pie screen**: tap-to-select; pan/pinch inactive by default.
7. **Radar screen**: tap a vertex to select; confirm marker and subtitle.
8. **Combined screen**: bar + dual-axis line overlay; tap bars or line (top layer wins); pinch/pan on primary viewport.
9. **Sankey screen**: tap nodes or links; pinch/pan when enabled.
10. **Domain axes**: category and time line demos render and accept gestures.
11. **Switch** through the full cycle back to line; confirm no crash and state feels reasonable when returning to a screen.

When adding a **new chart family**, extend **`ComposePlotApp`** and this checklist (**`AGENTS.md` → Obligations**).

**Accessibility:** opt-in **`requestFocusOnMarker`** on a spec is easiest to validate with **TalkBack** (Android) or **VoiceOver** (iOS): select a point/slice, then confirm focus can land on the marker/tooltip node. Behavior depends on host focus order and merges; there is no iOS golden for it—use a device pass when shipping the flag. With **`keyboardSelectionRoamEnabled`**, validate **Shift+arrow** selection moves on a **hardware keyboard** (simulator or iPad keyboard); unmodified arrows still pan when **`keyboardViewportNudgeEnabled`** is on.

## Platform differences (expected)

- **Typography and tick labels** may differ slightly from Android because text measurement uses host fonts and Compose Multiplatform text layout.
- **Pixel density** differs by device; geometry should remain consistent in data space.
- **Screenshot baselines** (Roborazzi) are Android-only; iOS has no committed goldens in this repo yet.

## Troubleshooting

- **Framework / Gradle phase fails**: run `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode` from `ComposePlotLib/` with the same `CONFIGURATION`, `SDK_NAME`, and architecture env vars Xcode sets, or inspect the Xcode build log for the exact Gradle error.
- **Signing**: fill `TEAM_ID` in `Config.xcconfig` or assign a team in target **Signing & Capabilities**.

## Related types

- `ChartColor` in `chart-core` is a multiplatform-safe `data class` so Kotlin/Native and Android/JVM share one definition (see `core/model/ChartColor.kt`).
