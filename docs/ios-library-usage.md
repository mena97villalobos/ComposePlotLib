# Using ComposePlotLib on iOS (library integration)

This page describes how a **consumer** composes charts in a **Kotlin Multiplatform** app that also ships an iOS target—the same public APIs as Android, backed by Compose Multiplatform on both sides. There is no in-repo runnable iOS app; iOS is supported purely as a **library** target.

## Preconditions

- A KMP module that applies the **Compose Multiplatform** plugin and includes **`iosArm64`** / **`iosSimulatorArm64`** (or your required Apple targets).
- `commonMain` dependency on **`chart-compose`** (which pulls in **`chart-core`**):

```kotlin
// build.gradle.kts — inside kotlin { sourceSets { commonMain.dependencies { … } } }
implementation(project(":chart-compose")) // monorepo; substitute Maven coordinates if you consume a published artifact
```

- iOS binaries configured so Swift can call into Kotlin. The **reference shape** in this repo is a **static framework** produced from the app module:

```kotlin
iosSimulatorArm64().binaries.framework {
    baseName = "MyChartsApp"
    isStatic = true
}
```

Adjust `baseName` to your product; Xcode will import that module name (e.g. `import MyChartsApp`).

## Compose entry point for `UIViewController`

Expose a top-level Kotlin function (in your app module’s `iosMain`) that returns `ComposeUIViewController { ... }`, and build your UI with the same composables you use on Android (`LineChart`, `BarChart`, etc.):

```kotlin
// <your-app>/src/iosMain/kotlin/.../MainViewController.kt
fun MainViewController() = ComposeUIViewController {
    LineChart(spec = /* your LineChartSpec */)
    // …or your own screens composed from the chart composables
}
```

## Embedding in SwiftUI

Wrap the generated Kotlin facade in `UIViewControllerRepresentable`:

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

The exact `MainViewControllerKt` name comes from the Kotlin file + module; if you rename the function or package, adjust the Swift call.

## Theme and Material

`chartChromeTheme()` uses **Material 3** (`MaterialTheme.colorScheme`). Ensure your root composable provides **`MaterialTheme`** if you rely on default chrome; otherwise pass an explicit **`ChartChromeTheme`** (see **[`style-and-theme.md`](style-and-theme.md)**).

## Verification

From the project root (same as **`AGENTS.md`**):

```bash
./gradlew :chart-compose:compileKotlinIosSimulatorArm64
```

Use **`:chart-compose:compileKotlinIosArm64`** when targeting physical devices. Substitute your own consumer module task to compile the charts as embedded in your app.

## Scope and limitations

- **Roborazzi** and committed screenshot goldens in this repo are **Android-only**; iOS remains manual or custom CI.
- **Text measurement** may differ slightly from Android for the same logical layout; see **Cross-Platform Readiness → iOS** in [`production-readiness-next-steps.md`](production-readiness-next-steps.md).

## Related

- **[`ios-gesture-validation.md`](ios-gesture-validation.md)** — gesture checklist on iOS
- **[`api-and-state.md`](api-and-state.md)** — specs, viewport, selection, gestures
- **[`architecture.md`](architecture.md)** shared planner → draw pipeline on all targets
