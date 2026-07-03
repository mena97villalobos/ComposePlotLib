# GitHub Actions, Packages, and Pages

## CI overview

- **Pull requests** (`.github/workflows/pr.yml`): on `ubuntu-latest`, runs `ktlintCheck`, unit tests (`chart-core` JVM + Android, `chart-compose` Android), Roborazzi verification, and `:samples:android-showcase:assembleDebug`. Sets `COMPOSEPLOTLIB_VERSION=debug` (also the default from `gradle.properties` when the env var is unset).
- **Releases** (`.github/workflows/release.yml`): triggers on tags `vMAJOR.MINOR.PATCH` or `vMAJOR.MINOR.PATCH-prerelease`. Verifies the tag is reachable from `main`, runs the same checks as PRs, builds Dokka HTML, publishes `:chart-core` and `:chart-compose` to **GitHub Packages**, creates a **GitHub Release** with `dokka-html.zip`, and deploys the Dokka site to **GitHub Pages** (see below).

## Library version in CI

The Gradle version is resolved as:

1. Environment variable `COMPOSEPLOTLIB_VERSION` (set in workflows)
2. Otherwise Gradle property `composeplotlib.version` in `gradle.properties`
3. Otherwise `debug`

Release workflows strip the leading `v` from the tag (e.g. `v1.2.3` → `1.2.3`).

## Consuming published artifacts (GitHub Packages)

Add the Maven repository and authenticate with a token that has `read:packages` (classic PAT) or the GitHub Packages read scope (fine-grained PAT).

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/OWNER/REPO")
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
implementation("com.mena97villalobos.composeplotlib:chart-core:VERSION")
implementation("com.mena97villalobos.composeplotlib:chart-compose:VERSION")
```

Use the same **VERSION** string as published (e.g. `1.2.3` without `v`).

**Note:** Linux CI does not register Kotlin/Native iOS targets; published artifacts from CI include JVM and Android (and Kotlin metadata). Build iOS klibs on a Mac locally or add a separate macOS publishing job if you need Apple artifacts in GitHub Packages.

## GitHub Pages (Dokka)

1. In the repository on GitHub: **Settings → Pages**.
2. Under **Build and deployment**, set **Source** to **GitHub Actions** (not “Deploy from a branch”) so the `deploy-pages` job can publish.
3. The first successful run of the Release workflow after this setting will deploy `build/dokka/html` (aggregated Dokka for `chart-core` and `chart-compose`).
4. Optional: set a **Custom domain** in the same Settings page; add DNS records as prompted by GitHub.

If GitHub prompts for an environment, approve the **github-pages** environment once so the deploy job can run.
