package com.mena97villalobos.composeplotlib.core.visible

import com.mena97villalobos.composeplotlib.core.domain.XyPoint
import com.mena97villalobos.composeplotlib.core.math.FpMath
import com.mena97villalobos.composeplotlib.core.range.IndexRange
import com.mena97villalobos.composeplotlib.core.spec.LineDecimationStrategy
import com.mena97villalobos.composeplotlib.core.spec.LineRenderSpec
import com.mena97villalobos.composeplotlib.core.viewport.ChartViewport
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/**
 * Builds the indexed point list used for line polylines and hit-testing: viewport window, continuity,
 * then optional decimation.
 */
object LineRenderPipeline {
    fun prepareIndexed(
        points: List<XyPoint>,
        viewport: ChartViewport,
        spec: LineRenderSpec,
    ): List<Pair<Int, XyPoint>> {
        if (points.isEmpty()) return emptyList()
        val range =
            VisibleRangeCalculator.visiblePointRangeWithContinuity(
                points = points,
                viewport = viewport,
                paddingEachSide = spec.viewportContinuityPadding.coerceAtLeast(0),
            ) ?: return emptyList()

        val cap = spec.maxPointsPerSeries
        val windowLen = range.endInclusive - range.start + 1
        if (cap > 0 && windowLen > cap) {
            return when (spec.decimation) {
                LineDecimationStrategy.Uniform -> uniformSampleRange(points, range, cap)
                LineDecimationStrategy.MinMaxY -> minMaxYSampleRange(points, range, cap)
                LineDecimationStrategy.Lttb -> lttbSampleRange(points, range, cap)
            }
        }

        val window = buildWindowIndexed(points, range)
        return decimateIndexed(window, spec)
    }

    internal fun decimateIndexed(
        window: List<Pair<Int, XyPoint>>,
        spec: LineRenderSpec,
    ): List<Pair<Int, XyPoint>> {
        val cap = spec.maxPointsPerSeries
        if (cap <= 0 || window.size <= cap) return window
        return when (spec.decimation) {
            LineDecimationStrategy.Uniform -> uniformIndexed(window, cap)
            LineDecimationStrategy.MinMaxY -> minMaxYIndexed(window, cap)
            LineDecimationStrategy.Lttb -> lttbIndexed(window, cap)
        }
    }

    private fun lttbSampleRange(
        points: List<XyPoint>,
        range: IndexRange,
        maxPoints: Int,
    ): List<Pair<Int, XyPoint>> {
        val n = range.endInclusive - range.start + 1
        return lttbCore(
            n = n,
            maxPoints = maxPoints,
            globalIndexAt = { local -> range.start + local },
            pointAt = { local -> points[range.start + local] },
        )
    }

    private fun lttbIndexed(
        window: List<Pair<Int, XyPoint>>,
        maxPoints: Int,
    ): List<Pair<Int, XyPoint>> =
        lttbCore(
            n = window.size,
            maxPoints = maxPoints,
            globalIndexAt = { local -> window[local].first },
            pointAt = { local -> window[local].second },
        )

    /**
     * Largest-Triangle-Three-Buckets over `n` local samples `0..n-1`. [globalIndexAt] / [pointAt] map a local index
     * to its stable global index and domain point. Keeps the first and last sample and one point per interior bucket.
     * Falls back to returning every sample when `n <= maxPoints` or `maxPoints < 3`.
     */
    private inline fun lttbCore(
        n: Int,
        maxPoints: Int,
        globalIndexAt: (Int) -> Int,
        pointAt: (Int) -> XyPoint,
    ): List<Pair<Int, XyPoint>> {
        if (n <= 0) return emptyList()
        if (maxPoints < 3 || n <= maxPoints) {
            return ArrayList<Pair<Int, XyPoint>>(n).apply {
                for (i in 0 until n) add(globalIndexAt(i) to pointAt(i))
            }
        }
        val out = ArrayList<Pair<Int, XyPoint>>(maxPoints)
        out.add(globalIndexAt(0) to pointAt(0))
        // Interior points 1..n-2 are split into (maxPoints - 2) buckets.
        val bucketSize = (n - 2).toDouble() / (maxPoints - 2)
        var selected = 0 // local index of the last kept point
        for (bucket in 0 until maxPoints - 2) {
            // Average point of the *next* bucket (used as the third triangle vertex).
            val nextStart = (kotlin.math.floor((bucket + 1) * bucketSize).toInt() + 1).coerceIn(1, n - 1)
            val nextEnd = (kotlin.math.floor((bucket + 2) * bucketSize).toInt() + 1).coerceIn(nextStart, n)
            var avgX = 0.0
            var avgY = 0.0
            var avgCount = 0
            for (j in nextStart until nextEnd) {
                val p = pointAt(j)
                avgX += p.x
                avgY += p.y
                avgCount++
            }
            if (avgCount == 0) {
                val last = pointAt(n - 1)
                avgX = last.x
                avgY = last.y
            } else {
                avgX /= avgCount
                avgY /= avgCount
            }
            // Current bucket range.
            val rangeStart = (kotlin.math.floor(bucket * bucketSize).toInt() + 1).coerceIn(1, n - 1)
            val rangeEnd = (kotlin.math.floor((bucket + 1) * bucketSize).toInt() + 1).coerceIn(rangeStart + 1, n)
            val a = pointAt(selected)
            var bestArea = -1.0
            var bestIdx = rangeStart
            for (j in rangeStart until rangeEnd) {
                val p = pointAt(j)
                // Twice the triangle area (constant 0.5 dropped — only the argmax matters).
                val area = kotlin.math.abs((a.x - avgX) * (p.y - a.y) - (a.x - p.x) * (avgY - a.y))
                if (area > bestArea) {
                    bestArea = area
                    bestIdx = j
                }
            }
            out.add(globalIndexAt(bestIdx) to pointAt(bestIdx))
            selected = bestIdx
        }
        out.add(globalIndexAt(n - 1) to pointAt(n - 1))
        return out
    }

    private fun buildWindowIndexed(
        points: List<XyPoint>,
        range: IndexRange,
    ): List<Pair<Int, XyPoint>> {
        val n = range.endInclusive - range.start + 1
        if (n <= 0) return emptyList()
        val out = ArrayList<Pair<Int, XyPoint>>(n)
        var i = range.start
        val end = range.endInclusive
        while (i <= end) {
            out.add(i to points[i])
            i++
        }
        return out
    }

    private fun uniformSampleRange(
        points: List<XyPoint>,
        range: IndexRange,
        maxPoints: Int,
    ): List<Pair<Int, XyPoint>> {
        val n = range.endInclusive - range.start + 1
        if (maxPoints < 2 || n <= maxPoints) {
            return buildWindowIndexed(points, range)
        }
        val out = ArrayList<Pair<Int, XyPoint>>(maxPoints)
        for (k in 0 until maxPoints) {
            val offset = round(k * (n - 1).toDouble() / (maxPoints - 1)).toInt().coerceIn(0, n - 1)
            val i = range.start + offset
            out.add(i to points[i])
        }
        return dedupePreserveOrder(out)
    }

    private fun minMaxYSampleRange(
        points: List<XyPoint>,
        range: IndexRange,
        maxPoints: Int,
    ): List<Pair<Int, XyPoint>> {
        val n = range.endInclusive - range.start + 1
        if (maxPoints < 4 || n <= maxPoints) {
            return uniformSampleRange(points, range, maxPoints)
        }
        val bucketPairs = max(2, maxPoints / 2)
        val xMin = points[range.start].x
        val xMax = points[range.endInclusive].x
        val xSpan = (xMax - xMin).coerceAtLeast(FpMath.EPS)

        val bucketMinIdx = IntArray(bucketPairs) { -1 }
        val bucketMaxIdx = IntArray(bucketPairs) { -1 }
        val bucketMinY = DoubleArray(bucketPairs) { Double.POSITIVE_INFINITY }
        val bucketMaxY = DoubleArray(bucketPairs) { Double.NEGATIVE_INFINITY }

        for (idx in range.start..range.endInclusive) {
            val p = points[idx]
            val b = bucketIndexForDomainX(p.x, xMin, xSpan, bucketPairs)
            if (p.y <= bucketMinY[b] + FpMath.EPS) {
                bucketMinY[b] = p.y
                bucketMinIdx[b] = idx
            }
            if (p.y >= bucketMaxY[b] - FpMath.EPS) {
                bucketMaxY[b] = p.y
                bucketMaxIdx[b] = idx
            }
        }

        val out = ArrayList<Pair<Int, XyPoint>>(maxPoints)
        out.add(range.start to points[range.start])
        for (b in 0 until bucketPairs) {
            val minI = bucketMinIdx[b]
            val maxI = bucketMaxIdx[b]
            if (minI < 0) continue
            if (minI == maxI) {
                out.add(minI to points[minI])
            } else if (minI < maxI) {
                out.add(minI to points[minI])
                out.add(maxI to points[maxI])
            } else {
                out.add(maxI to points[maxI])
                out.add(minI to points[minI])
            }
        }
        out.add(range.endInclusive to points[range.endInclusive])
        return dedupePreserveOrder(out)
    }

    private fun bucketIndexForDomainX(
        x: Double,
        xMin: Double,
        xSpan: Double,
        bucketCount: Int,
    ): Int {
        if (bucketCount <= 1) return 0
        val rel = ((x - xMin) / xSpan).coerceIn(0.0, 1.0)
        if (rel >= 1.0 - FpMath.EPS) return bucketCount - 1
        return min((rel * bucketCount).toInt(), bucketCount - 1)
    }

    private fun uniformIndexed(
        window: List<Pair<Int, XyPoint>>,
        maxPoints: Int,
    ): List<Pair<Int, XyPoint>> {
        val n = window.size
        if (maxPoints < 2 || n <= maxPoints) return window
        val out = ArrayList<Pair<Int, XyPoint>>(maxPoints)
        for (k in 0 until maxPoints) {
            val idx = round(k * (n - 1).toDouble() / (maxPoints - 1)).toInt().coerceIn(0, n - 1)
            out.add(window[idx])
        }
        return dedupePreserveOrder(out)
    }

    private fun minMaxYIndexed(
        window: List<Pair<Int, XyPoint>>,
        maxPoints: Int,
    ): List<Pair<Int, XyPoint>> {
        val n = window.size
        if (maxPoints < 4 || n <= maxPoints) return uniformIndexed(window, maxPoints)
        val bucketPairs = max(2, maxPoints / 2)
        var xMin = window.first().second.x
        var xMax = xMin
        for (pair in window) {
            val x = pair.second.x
            if (x < xMin) xMin = x
            if (x > xMax) xMax = x
        }
        val xSpan = (xMax - xMin).coerceAtLeast(FpMath.EPS)

        val bucketMinIdx = IntArray(bucketPairs) { -1 }
        val bucketMaxIdx = IntArray(bucketPairs) { -1 }
        val bucketMinY = DoubleArray(bucketPairs) { Double.POSITIVE_INFINITY }
        val bucketMaxY = DoubleArray(bucketPairs) { Double.NEGATIVE_INFINITY }

        for (pair in window) {
            val idx = pair.first
            val p = pair.second
            val b = bucketIndexForDomainX(p.x, xMin, xSpan, bucketPairs)
            if (p.y <= bucketMinY[b] + FpMath.EPS) {
                bucketMinY[b] = p.y
                bucketMinIdx[b] = idx
            }
            if (p.y >= bucketMaxY[b] - FpMath.EPS) {
                bucketMaxY[b] = p.y
                bucketMaxIdx[b] = idx
            }
        }

        val out = ArrayList<Pair<Int, XyPoint>>(maxPoints)
        out.add(window.first())
        for (b in 0 until bucketPairs) {
            val minI = bucketMinIdx[b]
            val maxI = bucketMaxIdx[b]
            if (minI < 0) continue
            if (minI == maxI) {
                pairForGlobalIndex(window, minI)?.let { out.add(it) }
            } else if (minI < maxI) {
                pairForGlobalIndex(window, minI)?.let { out.add(it) }
                pairForGlobalIndex(window, maxI)?.let { out.add(it) }
            } else {
                pairForGlobalIndex(window, maxI)?.let { out.add(it) }
                pairForGlobalIndex(window, minI)?.let { out.add(it) }
            }
        }
        out.add(window.last())
        return dedupePreserveOrder(out)
    }

    private fun pairForGlobalIndex(
        window: List<Pair<Int, XyPoint>>,
        globalIndex: Int,
    ): Pair<Int, XyPoint>? {
        if (window.isEmpty()) return null
        var lo = 0
        var hi = window.lastIndex
        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            val m = window[mid].first
            when {
                m < globalIndex -> lo = mid + 1
                m > globalIndex -> hi = mid - 1
                else -> return window[mid]
            }
        }
        return null
    }

    private fun dedupePreserveOrder(indexed: List<Pair<Int, XyPoint>>): List<Pair<Int, XyPoint>> {
        if (indexed.isEmpty()) return indexed
        val seen = HashSet<Int>(indexed.size * 4 / 3 + 1)
        val out = ArrayList<Pair<Int, XyPoint>>(indexed.size)
        for (pair in indexed) {
            if (seen.add(pair.first)) {
                out.add(pair)
            }
        }
        if (out.size <= 1) return out
        out.sortBy { it.first }
        return out
    }
}
