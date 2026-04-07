# AI agent milestone prompts (copy-paste)

Each block below is a **single residual mission**: **only what is still open** for that theme. **Shipped** work and `(M#)` history live in **[`production-readiness-next-steps.md`](production-readiness-next-steps.md)**—read that file first for context. Pick **one** block per run.

**Index & rules:** **[`ai-agent-full-backlog-prompt.md`](ai-agent-full-backlog-prompt.md)**

**Retired as copy-paste missions (shipped history in [`production-readiness-next-steps.md`](production-readiness-next-steps.md)):** **M3** → folded into **M8**; **M7** (draw pipeline); **M12** ([`ChartAnimationPolicy`](../chart-compose/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/chart/ChartAnimationPolicy.kt)); **M13** ([`MaterialChartPalette`](../chart-compose/src/commonMain/kotlin/com/mena97villalobos/composeplotlib/chart/theme/MaterialChartPalette.kt)); **M14** (pie/combined polish); **M15** (tests + Roborazzi); **M16** ([`api-checklist.md`](api-checklist.md)). Do **not** paste retired blocks—only sections **M1, M2, M4, M5, M6, M8, M9, M10, M11** below remain.

---

## Rules (every milestone)

- **`chart-core`:** no Compose UI types (`Modifier`, `Color`, `Dp`, `Offset`, `Rect`, `Path`, `Canvas`, `TextStyle`, …). Math, planning, geometry, specs, viewport, selection only.
- **`chart-compose`:** declarative, immutable-first public APIs; interaction in `ChartViewportState` / `ChartSelectionState`.
- **Planner before renderer:** change `ChartPlanner` / `ChartLayoutPlan` / specs in `chart-core` before draw/gesture code in `chart-compose`.
- **Docs:** when behavior or public API changes, update [`AGENTS.md`](../AGENTS.md), [`api-and-state.md`](api-and-state.md), [`architecture.md`](architecture.md), [`production-readiness-next-steps.md`](production-readiness-next-steps.md) (shipped vs open), and any `docs/*.md` that become wrong. New chart families: `ComposePlotApp` + showcase per obligations. Releases / broad API surface: walk [`api-checklist.md`](api-checklist.md).
- **Verify** after substantive changes:

  ```bash
  ./gradlew :chart-core:testDebugUnitTest :chart-compose:compileDebugKotlinAndroid :samples:android-showcase:verifyRoborazziDebug :composeApp:assembleDebug
  ```

  On macOS, if shared or iOS-used paths changed:

  ```bash
  ./gradlew :composeApp:compileKotlinIosSimulatorArm64
  ```

  If showcase screenshots change intentionally: `./gradlew :samples:android-showcase:recordRoborazziDebug`

---

## M1 — Chart-core: geometry & viewport (residual)

**Maps to:** [`production-readiness-next-steps.md`](production-readiness-next-steps.md) → **Chart-Core Improvements** → *Geometry and numeric robustness*; *Viewport model* (still open).

**Open items:**
- Optional precision helpers for broader chart-math comparisons beyond viewport.
- Deeper empty/degenerate **data** paths outside viewport (planner/transform callers already handle many cases).
- Viewport **future work:** double-tap zoom and optional inertial panning if worth supporting; optional one-axis-only *zoom* modes beyond axis-lock.

**Scope:** `chart-core` (+ tests). No new chart families unless explicitly expanded.

**Out of scope:** Compose UI; full gesture physics rewrite.

**Done when:** Targeted tests and/or doc updates; `:chart-core:testDebugUnitTest` passes.

---

## M2 — Selection & hit testing (residual)

**Maps to:** [`production-readiness-next-steps.md`](production-readiness-next-steps.md) → **Chart-Core Improvements** → *Selection model* (needed improvements / future work).

**Open items:**
- Richer **optional** selection metadata for custom markers and accessibility (beyond current `HighlightTarget` / `ChartSelection` fields).
- Further unify line/scatter **render vs hit** if a concrete mismatch appears (combined resolver documents shared ordering; extend only when a bug or product requirement justifies it).

**Scope:** `chart-core` (`selection/*`, planner selection). Touch `chart-compose` only if `ChartSelectionState` or composable contract must change.

**Out of scope:** Pie + cartesian single surface.

**Done when:** Tests lock behavior; `api-and-state.md` updated if user-visible.

---

## M4 — Domain axes & tick strategy (residual)

**Maps to:** [`production-readiness-next-steps.md`](production-readiness-next-steps.md) → **§2 Formatters and domain-aware axes**; **Chart-Core Improvements** → *Tick generation* (still open).

**Open items:**
- Trading holidays and session-hour calendars in core.
- Zone-aligned “weekday” tick policy (today’s weekend policy is **UTC** calendar days only—apps shift data or use custom formatters for other zones).

**Scope:** `chart-core` (`TickValuePlanner`, `AxisSpec`, scales) + `docs/` (cookbook); `chart-compose` only if new spec fields surface.

**Out of scope:** Breaking default linear axis behavior; untested trading-calendar product.

**Done when:** Tests + docs; optional `ComposePlotApp` / showcase path if user-visible.

---

## M5 — Layout & text measurement (residual)

**Maps to:** [`production-readiness-next-steps.md`](production-readiness-next-steps.md) → **§1 Layout and text measurement** (still open / deferred).

**Open items:**
- Remaining edge-case overlap; unified baseline policy across all label paths.
- Optional “min labels to keep” and density-aware thinning tied to measured width.
- **Deferred:** baseline strategy across x/y ticks, legend, chrome via `TextLayoutMetrics` end-to-end; **iOS / Android measurement parity** under font scale, locale scripts, large strings (see [`ios-host-runbook.md`](ios-host-runbook.md)).

**Scope:** `ChartPlanner`, `ChartLayoutPlan`, `chart-compose` measurement; no Compose types in `chart-core`.

**Out of scope:** Full typographic engine; whole-planner rewrite in one PR.

**Done when:** Measurable improvement in showcase or tests; `recordRoborazziDebug` if pixels move.

---

## M6 — Planner structure & caching (residual)

**Maps to:** [`production-readiness-next-steps.md`](production-readiness-next-steps.md) → **Planner evolution** (recommended next structure after shipped `TickValuePass` / pass-2 reuse).

**Open items:**
- Evolve `ChartPlanner` toward explicit multi-pass structure (bounds → ticks → measure → final layout → draw commands) as in the mermaid diagram there—incremental refactors with tests, not a monolithic rewrite.

**Scope:** `chart-core` primarily; `chart-compose` only if measurement call pattern must change.

**Out of scope:** Full scene-graph; multithreaded planning.

**Done when:** Structure or caching justified by tests / allocs reduction; `:chart-core:testDebugUnitTest` passes.

---

## M8 — Performance, visible-range tuning & benchmarks (residual)

**Maps to:** [`production-readiness-next-steps.md`](production-readiness-next-steps.md) → **Visible range calculations** (*Remaining / later*); **Performance Work** (*Still needed*). *(Former **M3** follow-ups are tracked here.)*

**Open items:**
- Further tuning of oversample / scatter defaults if product feedback warrants; broader planner caching beyond pass-2 tick reuse, grid-line reuse, and current line defaults.
- Optional **CI** JMH or Android Macrobenchmark baselines (variance; not wired by default).
- Broader memoization / incremental plan fragments beyond grid lines.

**Scope:** `chart-core`, optional benchmark module; `chart-compose` for pixel-budget hooks if needed.

**Out of scope:** GPU compute; multithreaded rendering.

**Done when:** Benchmark or memoization story documented and protected by tests where feasible; verification commands pass.

---

## M9 — Markers & tooltips (residual)

**Maps to:** [`production-readiness-next-steps.md`](production-readiness-next-steps.md) → **§3 Marker / tooltip experience**.

**Open items:**
- Deeper multi-marker / global collision solvers beyond rect helpers.
- Optional bezier/stroked paths in the library itself (apps can approximate via dense polylines or draw inside `marker`).

**Scope:** `chart-compose` markers/overlays; `chart-core` planner hints only if planner-first.

**Out of scope:** Replacing the `marker` slot API.

**Done when:** Showcase + [`marker-tooltip-placement.md`](marker-tooltip-placement.md) updated; Roborazzi if stable.

---

## M10 — Gestures & iOS validation (residual)

**Maps to:** [`production-readiness-next-steps.md`](production-readiness-next-steps.md) → *Gesture system*; **Cross-Platform** → iOS.

**Open items:**
- **Physical device** gesture matrix—append a row to [`ios-gesture-validation.md`](ios-gesture-validation.md) when you rely on it for a release (simulator/compile alone are not sufficient for touch timing).

**Scope:** `chart-compose` gestures; `docs/ios-gesture-validation.md`, `docs/ios-host-runbook.md`.

**Out of scope:** Duplicate platform gesture engines.

**Done when:** Checklist/table updated; compile/tests pass; `api-and-state.md` if API changes.

---

## M11 — Accessibility (residual)

**Maps to:** [`production-readiness-next-steps.md`](production-readiness-next-steps.md) → *Accessibility*.

**Open items (optional product follow-ups):** pre-built multi-step custom-action **presets** beyond the merge helper + app-defined action lists; any further marker/content semantics polish you choose to scope.

**Scope:** `ChartAccessibilitySpec`, `ChartSemantics.kt`, cartesian/pie composables, showcase.

**Out of scope:** Full platform-specific accessibility services.

**Done when:** Scoped follow-up is implemented and documented; `api-and-state.md` updated if user-visible; iOS compile clean when shared paths change.

---

*Keep these prompts aligned with [`production-readiness-next-steps.md`](production-readiness-next-steps.md) **Still open** / **Still needed** sections; do not duplicate shipped narratives here.*
