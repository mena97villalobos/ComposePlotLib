package com.mena97villalobos.composeplotlib.core.spec

/**
 * How selection changes are surfaced to accessibility services as a live region ([chart-compose] maps this to
 * platform semantics). Plain enum stays platform-agnostic in [chart-core].
 */
enum class ChartLiveRegionMode {
    /** No live-region semantics for selection. */
    Off,
    Polite,
    Assertive,
}
