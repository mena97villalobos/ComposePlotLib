package com.mena97villalobos.composeplotlib.core.model

/**
 * One step in a **waterfall** chart. Entries are an ordered sequence plotted left-to-right at integer category
 * positions (x = index). A running total accumulates across non-total entries; each bar floats from the running
 * total before the step to the running total after it.
 *
 * @property delta The change this step contributes to the running total. Ignored when [isTotal] is true.
 * @property isTotal When true, this is a **total / subtotal** marker: its bar spans `0..runningTotal` (the
 * accumulated total up to this point) and it does **not** change the running total.
 * @property label Optional category label for this step (shown on the X axis via the spec's axis formatter).
 */
data class WaterfallEntry(
    val delta: Double,
    val isTotal: Boolean = false,
    val label: String = "",
)
