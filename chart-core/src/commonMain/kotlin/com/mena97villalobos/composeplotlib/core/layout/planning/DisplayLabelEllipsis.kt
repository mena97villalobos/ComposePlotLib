package com.mena97villalobos.composeplotlib.core.layout.planning

/** Truncate display text to [maxChars] code units, replacing overflow with a single `…` suffix. */
internal fun ellipsizeDisplayLabel(
    text: String,
    maxChars: Int?,
): String {
    if (maxChars == null || maxChars <= 0 || text.length <= maxChars) return text
    val limit = maxChars - 1
    if (limit <= 0) return "…"
    return text.take(limit) + "…"
}
