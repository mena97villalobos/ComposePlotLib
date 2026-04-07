package com.mena97villalobos.composeplotlib.core.range

/** Inclusive index span into a list ([start]..[endInclusive]). */
data class IndexRange(
    val start: Int,
    val endInclusive: Int,
) {
    init {
        require(start <= endInclusive) { "Index range start must be <= endInclusive." }
    }

    val count: Int get() = endInclusive - start + 1
}
