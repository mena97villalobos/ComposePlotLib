# Using ComposePlotLib on iOS (library integration)

**[`ios-host-runbook.md`](ios-host-runbook.md)** covers the **in-repo** Xcode app: Gradle phases, signing, and a manual checklist for `ComposePlotApp`.

This page describes how a **consumer** composes charts in a **Kotlin Multiplatform** app that also ships an iOS target—the same public APIs as Android, backed by Compose Multiplatform on both sides.

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
    baseName = "ComposeApp"
    isStatic = true
}
```

Adjust `baseName` to your product; Xcode will import that module name (e.g. `import ComposeApp`).

## Compose entry point for `UIViewController`

Expose a top-level Kotlin function that returns `ComposeUIViewController { ... }`, and build your UI with the same composables you use on Android (`LineChart`, `BarChart`, etc.). The repo’s **`composeApp`** module does exactly this:

```kotlin
// composeApp/src/iosMain/kotlin/.../MainViewController.kt
fun MainViewController() = ComposeUIViewController { App() }
```

`App()` (in `commonMain`) can call `ComposePlotApp()` or your own screens.

## Embedding in SwiftUI

Wrap the generated Kotlin facade in `UIViewControllerRepresentable`, as in **`iosApp/iosApp/ContentView.swift`**:

```swift
import SwiftUI
import ComposeApp

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
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

Use **`:composeApp:compileKotlinIosArm64`** when targeting physical devices. Replace `:composeApp` with your consumer module if charts live elsewhere.

## Scope and limitations

- **Roborazzi** and committed screenshot goldens in this repo are **Android-only**; iOS remains manual or custom CI.
- **Text measurement** may differ slightly from Android for the same logical layout; see **Cross-Platform Readiness → iOS** in [`production-readiness-next-steps.md`](production-readiness-next-steps.md).

## Related

- **[`ios-gesture-validation.md`](ios-gesture-validation.md)** — gesture checklist on iOS
- **[`api-and-state.md`](api-and-state.md)** — specs, viewport, selection, gestures
- **[`architecture.md`](architecture.md)** shared planner → draw pipeline on all targets
