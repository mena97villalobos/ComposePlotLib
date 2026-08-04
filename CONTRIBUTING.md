# Contributing to ComposePlotLib

Thanks for considering a contribution. This guide covers everything you need to get a PR merged.

`AGENTS.md` is the deeper reference for module boundaries and the data-flow pipeline; this file is
the practical workflow. If the two ever disagree, `AGENTS.md` wins for architecture and this file
wins for process.

## Getting set up

**Requirements:** JDK 17. iOS targets only build on macOS.

```shell
git clone https://github.com/mena97villalobos/ComposePlotLib.git
cd ComposePlotLib
./gradlew :samples:android-showcase:assembleDebug
```

The Android showcase app is the primary place to see and manually verify chart behavior.

## Before you open a PR

Run exactly what CI runs. `apiCheck` and `verifyChartCorePurity` are the two people forget, and both
will fail your PR:

```shell
./gradlew ktlintCheck apiCheck :chart-core:verifyChartCorePurity \
  :chart-core:jvmTest :chart-core:testDebugUnitTest :chart-compose:testDebugUnitTest \
  :samples:android-showcase:verifyRoborazziDebug :samples:android-showcase:assembleDebug
```

On macOS, if you touched shared code that iOS compiles:

```shell
./gradlew :chart-compose:compileKotlinIosSimulatorArm64
```

### If a check fails

| Failure | What to do |
|---------|-----------|
| `ktlintCheck` | `./gradlew ktlintFormat` |
| `apiCheck` | You changed the public API. If intentional, `./gradlew apiDump` and **review the `.api` diff** — see [Public API changes](#public-api-changes) |
| `verifyChartCorePurity` | You used a Compose/Android/JVM type in `chart-core` — see [The two hard rules](#the-two-hard-rules) |
| `verifyRoborazziDebug` | Rendering changed. If intentional, `./gradlew :samples:android-showcase:recordRoborazziDebug` and commit the new goldens |

## The two hard rules

Both are enforced by CI, so you will find out either way — but knowing them up front saves a round
trip.

**1. `chart-core` contains no Compose, Android, or JVM types.** It is the platform-agnostic engine.
No `Modifier`, `Color`, `Dp`, `Offset`, `Rect`, `Path`, `Canvas`, or `TextStyle`. `chart-core`
defines its own geometry primitives (`RectD`, `OffsetD`, `InsetsD`) — use those.

**2. Everything in `commonMain` must be KMP-compatible.** Both library modules target Android *and*
iOS. The usual traps:

- `String.format` / `"…".format(…)` — JVM-only. Use `kotlin.math` plus manual string building.
- Any `java.*` import.
- `android.*` / `androidx.*` outside an `androidMain` source set.

When in doubt, check the symbol resolves in `kotlin-stdlib-common`.

## Where does my change go?

| Change | Module |
|--------|--------|
| Math, geometry, specs, layout planning — no UI | `chart-core` |
| Composables, gestures, canvas rendering | `chart-compose` |
| Demo-only | `samples/android-showcase` |

The pipeline is `ChartSpec` + state → `ChartPlanner` (chart-core) → `ChartLayoutPlan` with
`DrawCommand`s → `ComposeCanvasAdapter` (chart-compose) → Compose `Canvas`.

**Extend the planner before the renderer.** If a change affects layout or chart semantics, model it
in `chart-core` planning types first, then adapt rendering. Ad-hoc drawing code in the canvas
adapter that bypasses the plan will be sent back.

## Adding a new chart family

This has extra obligations, all enforced by review:

1. Planner logic and spec types in `chart-core`.
2. The public composable in `chart-compose`.
3. **A showcase screen** in `samples/android-showcase/src/commonMain/.../screens`, wired into
   `ShowcaseApp.kt`, **with a Roborazzi scenario** so the family is screenshot-tested.
4. **Docs updated** so nothing claims fewer families than the code ships:
   `docs/chart-families-overview.md` (the source of truth for the family list), `AGENTS.md`,
   `README.md`, `docs/api-and-state.md`, `docs/architecture.md`, `docs/module-guide.md`,
   `docs/roadmap.md`, and any other file whose claims become false.

## Public API changes

The public/binary surface is tracked by
[binary-compatibility-validator](https://github.com/Kotlin/binary-compatibility-validator). Dumps
live in `chart-core/api/` and `chart-compose/api/` and are checked on every PR.

If you intentionally changed the API, run `./gradlew apiDump` and **read the resulting diff**.
`apiDump` records whatever you did — it does not tell you whether it was safe.

Specs are immutable `data class`es, so **adding a constructor parameter changes `copy()` and breaks
binary compatibility** even though it is source-compatible. That is a major-version change, not a
routine dump refresh. Prefer adding a new spec/model type over widening an existing one, and never
use mutable setters.

See `docs/api-checklist.md` for the release gate.

## Testing expectations

- Add or update `chart-core` tests for new math, transform, tick-generation, or selection logic.
- Add a Roborazzi scenario for anything that changes rendering.
- Keep tests deterministic — no `Random`, no wall-clock time.
- Degenerate input matters: empty series, `NaN`, infinities, zero-size canvas. A charting library
  gets fed real data mid-load, and it must not crash the host app.

## Commits and PRs

- Conventional-commit prefixes (`feat:`, `fix:`, `docs:`, `chore:`, `perf:`, `refactor:`).
- One logical change per PR; keep unrelated refactors out.
- Say what you verified. If you skipped a check (no macOS for iOS, for instance), say so.

## Reporting bugs and proposing features

Open an issue. For bugs, the most useful report includes the chart family, a minimal spec that
reproduces it, what you expected, what happened, and your platform (Android API level or iOS
version) plus the library version.

`docs/roadmap.md` tracks the open backlog if you are looking for something to pick up.

## Code of Conduct

This project follows the [Contributor Covenant](./CODE_OF_CONDUCT.md). By participating you agree to
abide by it.

## License

By contributing, you agree that your contributions are licensed under the
[Apache License 2.0](./LICENSE), the same license that covers this project.
