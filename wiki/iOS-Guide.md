# iOS Guide

ComposePlotLib is a Kotlin Multiplatform library, and iOS is a first-class **library** target: the
same `commonMain` code, the same specs, the same composables. What iOS does *not* have is a bundled
demo app or an automated behavioral test suite — read [Verification status](#verification-status)
before you assume parity.

Canonical reference:
[`docs/ios-library-usage.md`](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/ios-library-usage.md)
and
[`docs/ios-gesture-validation.md`](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/ios-gesture-validation.md).

## What ships for iOS

Both modules build and publish Kotlin/Native artifacts:

| Module | `iosArm64` | `iosSimulatorArm64` | `iosX64` |
|---|---|---|---|
| `chart-core` | yes | yes | no |
| `chart-compose` | yes | yes | no |

The release workflow publishes those Apple targets from a macOS job, alongside the Kotlin
Multiplatform *metadata* publication for both modules — so a multiplatform consumer depends on the
root coordinates and Gradle resolves the right variant. See
[Installation](Installation) for repository setup and credentials.

All 26 chart families come from `commonMain`; none of them are Android-only. The two
platform-specific pieces in `chart-compose` are both implemented for iOS:

- **PNG export** — `ImageBitmap.encodeToChartPng()` uses Skia's `Image.encodeToData(PNG)`, the same
  rasterizer Compose already uses to draw on iOS.
- **Reduced motion** — animation policy reads `UIAccessibilityIsReduceMotionEnabled()`.

## No `iosX64` — and why

Compose Multiplatform 1.11.1 (the version this library builds against, see
[`gradle/libs.versions.toml`](https://github.com/mena97villalobos/ComposePlotLib/blob/main/gradle/libs.versions.toml))
publishes **no `ios_x64` klib variants** — `compose.runtime`, `compose.foundation`,
`compose.material3` and `compose.ui` all fail variant resolution for that target. This is an
upstream gap, not a project decision.

`chart-core` alone would compile for `iosX64`, but shipping half the library for a target where the
rendering module cannot exist would be worse than not shipping it. Practical consequence: **Intel-Mac
iOS simulators are not supported.** Use an Apple-silicon Mac (`iosSimulatorArm64`) or a physical
device (`iosArm64`). The target returns if Compose Multiplatform restores `ios_x64`.

## Consuming from a Compose Multiplatform app

Your app module needs the Compose Multiplatform plugin, the Apple targets you care about, and a
`commonMain` dependency on `chart-compose` (which re-exports `chart-core`):

```kotlin
// <your-app>/build.gradle.kts
kotlin {
    iosArm64()
    iosSimulatorArm64().binaries.framework {
        baseName = "MyChartsApp"
        isStatic = true
    }

    sourceSets {
        commonMain.dependencies {
            implementation("com.mena97villalobos.composeplotlib:chart-compose:1.2.0")
        }
    }
}
```

Chart code then lives in `commonMain` and is shared verbatim with Android:

```kotlin
import androidx.compose.runtime.Composable
import com.mena97villalobos.composeplotlib.chart.ChartGestureConfig
import com.mena97villalobos.composeplotlib.chart.LineChart
import com.mena97villalobos.composeplotlib.chart.defaultViewport
import com.mena97villalobos.composeplotlib.chart.state.rememberChartSelectionState
import com.mena97villalobos.composeplotlib.chart.state.rememberChartViewportState
import com.mena97villalobos.composeplotlib.core.model.ChartPoint
import com.mena97villalobos.composeplotlib.core.model.LineSeries
import com.mena97villalobos.composeplotlib.core.spec.LineChartSpec

@Composable
fun Downloads() {
    val spec =
        LineChartSpec(
            series =
                listOf(
                    LineSeries(
                        id = "downloads",
                        label = "Downloads",
                        points = listOf(ChartPoint(0.0, 1.0), ChartPoint(1.0, 3.0), ChartPoint(2.0, 2.0)),
                    ),
                ),
        )
    LineChart(
        spec = spec,
        viewportState = rememberChartViewportState(initialViewport = defaultViewport(spec)),
        selectionState = rememberChartSelectionState(),
        gestureConfig = ChartGestureConfig.cartesianInteractive(),
    )
}
```

## SwiftUI bridge

There is no ComposePlotLib-specific bridge — you use the standard Compose Multiplatform one. Expose a
`ComposeUIViewController` from your app module's `iosMain`:

```kotlin
// <your-app>/src/iosMain/kotlin/.../MainViewController.kt
fun MainViewController() = ComposeUIViewController {
    Downloads()
}
```

Then wrap it with `UIViewControllerRepresentable` in Swift:

```swift
import SwiftUI
import MyChartsApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

The `MainViewControllerKt` symbol is derived from the Kotlin file name and framework `baseName`;
rename either and the Swift call changes with it.

One theming caveat: `chartChromeTheme()` reads Material 3's `MaterialTheme.colorScheme`, so your root
composable must provide a `MaterialTheme` — or pass an explicit `ChartChromeTheme` via the
`chromeTheme` parameter. See [Styling and Theming](Styling-and-Theming).

## Gestures on iOS

Gesture handling lives in shared `chart-compose` code, so the `ChartGestureConfig` surface — and
every preset on it — is identical on both platforms. Configure iOS exactly as you configure
Android; the presets are listed in [Interactivity and State](Interactivity-and-State).

Known behavioral differences to keep in mind:

- **Fling end detection.** `ChartFlingGesturePolicy` approximates gesture end with a short debounce
  after the last transform event; that timing can land slightly differently than on Android.
- **Text measurement.** The same logical layout can measure slightly differently than Android, which
  shifts axis-label fitting and truncation at the margins. Tracked as an open parity item in
  [`docs/roadmap.md`](https://github.com/mena97villalobos/ComposePlotLib/blob/main/docs/roadmap.md).
- **Simulator is not enough.** Touch timing and multi-touch routing need a physical device to judge.

## Verification status

Be clear-eyed about what "supported" means here.

| Layer | iOS coverage |
|---|---|
| Compilation | **Automated.** Every PR runs a `macos-14` job compiling `chart-core` and `chart-compose` for `iosArm64` and `iosSimulatorArm64`, so a `commonMain` change that breaks iOS fails at PR time. |
| Unit tests | Android/JVM only. There is no iOS test task in CI. |
| Screenshots | Roborazzi goldens are **Android-only**. There is no iOS golden tooling. |
| Gestures | **Manual QA only**, against the checklist in `docs/ios-gesture-validation.md`. |

So: iOS correctness is guaranteed to the level of *"it compiles and the shared logic is the same code
Android's tests cover"* — plus whatever manual device QA a maintainer logged in the validation record
of that doc. There are **no automated behavioral iOS tests**.

**There is no bundled iOS sample app.** The only runnable demo is `samples:android-showcase`. If you
want to eyeball a chart on iOS, wire it into your own KMP app using the snippets above.

To reproduce the compile gate locally on a Mac:

```bash
./gradlew :chart-compose:compileKotlinIosSimulatorArm64
./gradlew :chart-compose:compileKotlinIosArm64   # device slice
```

Apple targets are only configured when the build detects a macOS host; set
`COMPOSEPLOTLIB_FORCE_IOS_TARGETS=true` (or `false`) to override that detection.
