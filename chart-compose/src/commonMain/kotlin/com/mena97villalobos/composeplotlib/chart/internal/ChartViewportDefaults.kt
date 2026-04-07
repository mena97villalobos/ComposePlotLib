package com.mena97villalobos.composeplotlib.chart.internal

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.model.toXyPoints
import com.mena97villalobos.composeplotlib.core.spec.BarChartSpec

/** Maps each bar group id to its [XyPoint] list for helpers and tests. */
internal fun BarChartSpec.toXyPointsByGroup(): Map<String, List<XyPoint>> =
    groups.associate { group ->
        group.id to group.toXyPoints()
    }
