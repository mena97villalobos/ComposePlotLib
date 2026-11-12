package com.mena97villalobos.composeplotlib.samples.android.data

import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchyNode
import com.mena97villalobos.composeplotlib.core.spec.TreemapHierarchySpec
import com.mena97villalobos.composeplotlib.core.spec.layoutSize
import kotlin.math.roundToLong

private const val KB = 1_024.0
private const val MB = KB * 1_024.0
private const val GB = MB * 1_024.0

private fun file(
    id: String,
    name: String,
    bytes: Double,
) = TreemapHierarchyNode(id = id, label = name, value = bytes)

private fun folder(
    id: String,
    name: String,
    vararg children: TreemapHierarchyNode,
    ownBytes: Double = 0.0,
) = TreemapHierarchyNode(id = id, label = name, value = ownBytes, children = children.toList())

/** `count` files named `prefix_001.ext` … with deterministic, gently decreasing sizes. */
private fun files(
    parentId: String,
    prefix: String,
    extension: String,
    count: Int,
    largest: Double,
): Array<TreemapHierarchyNode> =
    Array(count) { i ->
        val n = (i + 1).toString().padStart(3, '0')
        file("$parentId/$prefix$n", "${prefix}_$n.$extension", largest / (1.0 + i * 0.35))
    }

/**
 * A phone's internal storage as a folder tree: a few big folders and a long tail of small files, which is what the
 * hierarchical treemap's minimum tile size and aggregate tile are for. Sizes are bytes.
 */
fun sampleStorageTree(): List<TreemapHierarchyNode> =
    listOf(
        folder(
            "dcim",
            "DCIM",
            folder("dcim/camera", "Camera", *files("dcim/camera", "IMG", "jpg", 60, 9.5 * MB)),
            folder("dcim/screenshots", "Screenshots", *files("dcim/screenshots", "Screenshot", "png", 40, 1.4 * MB)),
            folder("dcim/video", "Video", *files("dcim/video", "VID", "mp4", 8, 820 * MB)),
        ),
        folder(
            "movies",
            "Movies",
            file("movies/trip", "Trip to the coast.mp4", 3.1 * GB),
            file("movies/concert", "Concert.mp4", 1.6 * GB),
            file("movies/birthday", "Birthday.mp4", 900 * MB),
        ),
        folder(
            "download",
            "Download",
            file("download/os-image", "os-image.zip", 2.2 * GB),
            file("download/manual", "Camera manual.pdf", 48 * MB),
            *files("download", "invoice", "pdf", 30, 1.2 * MB),
            ownBytes = 0.0,
        ),
        folder(
            "android",
            "Android",
            folder("android/data", "data", *files("android/data", "cache", "bin", 12, 300 * MB)),
            folder("android/media", "media", *files("android/media", "voice", "opus", 25, 4 * MB)),
            // Bytes in Android/obb that are not listed individually: an undrawn remainder.
            ownBytes = 4.2 * GB,
        ),
        folder(
            "music",
            "Music",
            *files("music", "Track", "flac", 35, 42 * MB),
        ),
        folder(
            "documents",
            "Documents",
            *files("documents", "Scan", "pdf", 20, 6 * MB),
        ),
        *files("root", "note", "txt", 25, 64 * KB),
    )

/** Human-readable byte size with one decimal (`"3.1 GB"`), without `String.format`. */
fun formatBytes(bytes: Double): String {
    val (scaled, unit) =
        when {
            bytes >= GB -> bytes / GB to "GB"
            bytes >= MB -> bytes / MB to "MB"
            bytes >= KB -> bytes / KB to "KB"
            else -> bytes to "B"
        }
    val tenths = (scaled * 10.0).roundToLong()
    return "${tenths / 10}.${tenths % 10} $unit"
}

/** The storage tree as a two-level treemap with byte labels and a 48px minimum tile side. */
fun sampleStorageTreemapSpec(minTileSidePx: Double = 48.0): TreemapHierarchySpec =
    TreemapHierarchySpec(
        nodes = sampleStorageTree(),
        maxDepth = 2,
        id = "storage",
        valueFormatter = { node -> formatBytes(node.layoutSize()) },
        minTileSidePx = minTileSidePx,
        aggregateLabel = { count -> "$count smaller items" },
    )
