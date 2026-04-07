package com.mena97villalobos.composeplotlib.core.model

import com.mena97villalobos.composeplotlib.core.domain.XyPoint

/** Maps bar entries to [XyPoint] for shared geometry helpers. */
fun BarGroup.toXyPoints(): List<XyPoint> =
    entries.map { entry ->
        XyPoint(entry.x, entry.y)
    }
