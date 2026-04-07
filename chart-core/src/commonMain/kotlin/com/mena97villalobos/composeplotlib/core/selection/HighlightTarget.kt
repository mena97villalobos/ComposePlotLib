package com.mena97villalobos.composeplotlib.core.selection

import com.mena97villalobos.composeplotlib.core.domain.XyPoint

/**
 * Identifies a highlighted data point: series id, index in that series, and domain-space [point].
 *
 * **Bar charts:** [seriesId] is the owning [com.mena97villalobos.composeplotlib.core.model.BarGroup.id]
 * (legend row id). [pointIndex] is the **category index** — the index of [com.mena97villalobos.composeplotlib.core.model.BarEntry]
 * within that group (same for stacked/clustered: one row per group). [point] is the entry’s domain
 * `(x, y)` from [com.mena97villalobos.composeplotlib.core.model.BarEntry].
 *
 * @param layerId When non-null, ties selection to a [com.mena97villalobos.composeplotlib.core.spec.CombinedCartesianLayer];
 * standalone charts leave this null.
 */
data class HighlightTarget(
    val seriesId: String,
    val pointIndex: Int,
    val point: XyPoint,
    val layerId: String? = null,
)
