## What and why

<!-- What does this change, and what problem does it solve? Link any related issue. -->

## Verification

<!-- Tick what you ran. If you skipped something (e.g. no macOS for the iOS compile), say so. -->

- [ ] `./gradlew ktlintCheck apiCheck :chart-core:verifyChartCorePurity`
- [ ] `./gradlew :chart-core:jvmTest :chart-core:testDebugUnitTest :chart-compose:testDebugUnitTest`
- [ ] `./gradlew :samples:android-showcase:verifyRoborazziDebug :samples:android-showcase:assembleDebug`
- [ ] iOS compile (macOS only): `./gradlew :chart-compose:compileKotlinIosSimulatorArm64`

## If this changes the public API

- [ ] Ran `./gradlew apiDump` and **reviewed** the `.api` diff
- [ ] No constructor parameter was added to an existing public spec `data class` (that breaks
      `copy()` binary compatibility — see [CONTRIBUTING.md](../CONTRIBUTING.md#public-api-changes))

## If this adds a chart family

- [ ] Showcase screen added and wired into `ShowcaseApp.kt`
- [ ] Roborazzi scenario added and goldens recorded
- [ ] `docs/chart-families-overview.md`, `README.md`, `AGENTS.md` and other affected docs updated

## If this changes rendering

- [ ] Roborazzi goldens re-recorded (`recordRoborazziDebug`) and the visual diff is intentional
