# Contributing

The full, authoritative guide is
[`CONTRIBUTING.md`](https://github.com/mena97villalobos/ComposePlotLib/blob/main/CONTRIBUTING.md) in
the repository. This page is the short version.

## Setup

JDK 17 required. iOS targets only build on macOS.

```shell
git clone https://github.com/mena97villalobos/ComposePlotLib.git
cd ComposePlotLib
./gradlew :samples:android-showcase:assembleDebug
```

The Android showcase app is where you see and manually verify chart behavior.

## Run what CI runs

`apiCheck` and `verifyChartCorePurity` are the two people forget, and both will fail your PR:

```shell
./gradlew ktlintCheck apiCheck :chart-core:verifyChartCorePurity \
  :chart-core:jvmTest :chart-core:testDebugUnitTest :chart-compose:testDebugUnitTest \
  :samples:android-showcase:verifyRoborazziDebug :samples:android-showcase:assembleDebug
```

| Failure | Fix |
|---------|-----|
| `ktlintCheck` | `./gradlew ktlintFormat` |
| `apiCheck` | You changed the public API. If intentional, `./gradlew apiDump` and **review the `.api` diff** |
| `verifyChartCorePurity` | You used a Compose/Android/JVM type in `chart-core` |
| `verifyRoborazziDebug` | Rendering changed. If intentional, `./gradlew :samples:android-showcase:recordRoborazziDebug` |

## The two hard rules

**1. `chart-core` contains no Compose, Android, or JVM types.** It is the platform-agnostic engine
and defines its own geometry primitives (`RectD`, `OffsetD`, `InsetsD`). Enforced by
`verifyChartCorePurity` in CI.

**2. Everything in `commonMain` must be KMP-compatible.** No `java.*` imports, no `String.format`,
no `android.*`/`androidx.*` outside `androidMain`.

## Adding a chart family

Beyond the code, three things are required and checked in review:

1. A showcase screen wired into `ShowcaseApp.kt`, **with a Roborazzi scenario**.
2. Docs updated — `docs/chart-families-overview.md` is the source of truth for the family list, plus
   the README table, `AGENTS.md`, and anything else whose claims become false.
3. A [Chart Gallery](Chart-Gallery) entry in this wiki.

## Public API changes

The ABI is tracked by binary-compatibility-validator. Specs are immutable `data class`es, so
**adding a constructor parameter changes `copy()` and breaks binary compatibility** even though it
is source-compatible. That is a major-version change, not a routine `apiDump` refresh — prefer
adding a new spec type over widening an existing one.

## Editing this wiki

Wiki pages are generated from
[`wiki/`](https://github.com/mena97villalobos/ComposePlotLib/tree/main/wiki) on `main` and synced by
a GitHub Action. **Edit the files in that folder and open a PR** — changes made in the wiki UI are
overwritten on the next sync.

## Reporting bugs

Use the [issue templates](https://github.com/mena97villalobos/ComposePlotLib/issues/new/choose). The
most useful bug report includes the chart family, a minimal spec that reproduces it, and your
platform plus library version. Security issues go through
[private advisories](https://github.com/mena97villalobos/ComposePlotLib/security/advisories/new), not
public issues — see
[`SECURITY.md`](https://github.com/mena97villalobos/ComposePlotLib/blob/main/SECURITY.md).
