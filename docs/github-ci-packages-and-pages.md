# GitHub Actions, Packages, and Pages

## CI overview

- **Pull requests** (`.github/workflows/pr.yml`): the `check` job on `ubuntu-latest` runs `ktlintCheck`, `apiCheck` (binary API check against the `.api` files), `:chart-core:verifyChartCorePurity` (KMP purity), unit tests (`chart-core` JVM + Android, `chart-compose` Android), Roborazzi verification, and `:samples:android-showcase:assembleDebug`. A separate `ios` job on `macos-14` (with `COMPOSEPLOTLIB_FORCE_IOS_TARGETS=true`) compiles the `iosArm64` and `iosSimulatorArm64` targets of both modules, so a `commonMain` change that breaks iOS is caught at PR time. Sets `COMPOSEPLOTLIB_VERSION=debug` (also the default from `gradle.properties` when the env var is unset).
- **Releases** (`.github/workflows/release.yml`): triggers on tags `vMAJOR.MINOR.PATCH` or `vMAJOR.MINOR.PATCH-prerelease`. The `validate` job verifies the tag is reachable from `main`. The `build-and-release` job (ubuntu) runs `ktlintCheck`, unit tests, Roborazzi verification, and `:samples:android-showcase:assembleDebug` (note: it does *not* re-run `apiCheck`, `verifyChartCorePurity`, or the iOS compile job), builds Dokka HTML, publishes the Android + JVM artifacts to **GitHub Packages**, creates a **GitHub Release** with `dokka-html.zip`, and uploads the Pages artifact. A `publish-ios` job on `macos-latest` publishes the iOS + Kotlin Multiplatform metadata artifacts (see [Publishing](#consuming-published-artifacts-github-packages) below). A `notify-bom` job then POSTs a `repository_dispatch` (`event_type: library-released`) to the `ComposeLibsBom` repo (requires the `BOM_REPO_TOKEN` secret), and `deploy-pages` deploys the Dokka site to **GitHub Pages** (see below).

A third workflow, `.github/workflows/sample-app.yml`, builds the sample app and is outside the scope of this doc.

## Library version in CI

The Gradle version is resolved as:

1. Environment variable `COMPOSEPLOTLIB_VERSION` (set in workflows)
2. Otherwise Gradle property `composeplotlib.version` in `gradle.properties`
3. Otherwise `debug`

Release workflows strip the leading `v` from the tag (e.g. `v1.2.3` → `1.2.3`).

## Consuming published artifacts (GitHub Packages)

Publishing happens across two release jobs:

- **`build-and-release` (ubuntu)** publishes the JVM + Android artifacts: `chart-core` Android + JVM, and `chart-compose` Android. `chart-compose` targets Android and iOS only (no JVM target), so it has no JVM publication.
- **`publish-ios` (macOS)** publishes the `iosArm64` and `iosSimulatorArm64` artifacts plus the `kotlinMultiplatform` metadata publication for **both** `chart-core` and `chart-compose`. The KMP metadata publication lets multiplatform consumers depend on the root coordinates (`com.mena97villalobos.composeplotlib:chart-core` / `:chart-compose`) and have Gradle resolve the right platform variant automatically.

Add the Maven repository and authenticate with a token that has `read:packages` (classic PAT) or the GitHub Packages read scope (fine-grained PAT).

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/mena97villalobos/ComposePlotLib")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}
```

Dependencies (example):

```kotlin
implementation("com.mena97villalobos.composeplotlib:chart-core:1.2.0")
implementation("com.mena97villalobos.composeplotlib:chart-compose:1.2.0")
```

Use the same **VERSION** string as published (e.g. `1.2.3` without `v`).

**Note:** iOS/Apple artifacts **are** published by CI. The `publish-ios` job (on `macos-latest`) publishes `iosArm64`, `iosSimulatorArm64`, and the Kotlin Multiplatform metadata publications for both `chart-core` and `chart-compose`, alongside the Android + JVM artifacts from the Linux `build-and-release` job. Multiplatform consumers can depend on the KMP artifacts directly — no local Mac build or manual publishing job is required.

## GitHub Pages (Dokka)

1. In the repository on GitHub: **Settings → Pages**.
2. Under **Build and deployment**, set **Source** to **GitHub Actions** (not “Deploy from a branch”) so the `deploy-pages` job can publish.
3. The first successful run of the Release workflow after this setting will deploy `build/dokka/html` (aggregated Dokka for `chart-core` and `chart-compose`).
4. Optional: set a **Custom domain** in the same Settings page; add DNS records as prompted by GitHub.

If GitHub prompts for an environment, approve the **github-pages** environment once so the deploy job can run.
