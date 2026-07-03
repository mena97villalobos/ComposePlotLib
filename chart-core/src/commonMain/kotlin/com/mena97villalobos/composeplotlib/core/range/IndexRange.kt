package com.mena97villalobos.composeplotlib.core.range

/**
 * Inclusive index span into a list ([start]..[endInclusive]).
 *
 * @property start Inclusive first index.
 * @property endInclusive Inclusive last index; must be `>= start`.
 */
data class IndexRange(
    val start: Int,
    val endInclusive: Int,
) {
    init {
        require(start <= endInclusive) { "Index range start must be <= endInclusive." }
    }

    /** Number of indices covered (`endInclusive - start + 1`); always `>= 1`. */
    val count: Int get() = endInclusive - start + 1
}
