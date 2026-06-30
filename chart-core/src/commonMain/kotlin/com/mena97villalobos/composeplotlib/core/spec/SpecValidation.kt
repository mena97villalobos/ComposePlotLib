package com.mena97villalobos.composeplotlib.core.spec

/**
 * Fails fast when [ids] contains duplicates. Series/node ids are used as selection keys and resolved with
 * `firstOrNull { it.id == ... }`, so duplicates silently bind markers/highlights to the wrong element — a
 * library should reject that at the spec boundary rather than render wrong data.
 *
 * @param what human-readable description of the id collection (used in the error message).
 */
internal fun requireUniqueIds(
    ids: List<String>,
    what: String,
) {
    if (ids.size < 2) return
    val seen = HashSet<String>(ids.size)
    val duplicates = LinkedHashSet<String>()
    for (id in ids) {
        if (!seen.add(id)) duplicates += id
    }
    require(duplicates.isEmpty()) {
        "$what must be unique; duplicate id(s): ${duplicates.joinToString(prefix = "[", postfix = "]")}"
    }
}
