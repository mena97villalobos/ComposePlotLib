package com.mena97villalobos.composeplotlib.core

/**
 * Lightweight metadata describing the published library name and targets.
 *
 * For version information, use your build system (Gradle version catalog / POM), not this type.
 *
 * @property name Human-readable library name.
 * @property supportedTargets Platform targets the published artifacts are intended to support.
 * @property plannedModules High-level module names (diagnostic/roadmap hint; not a dependency graph).
 */
data class ChartLibraryInfo(
    val name: String,
    val supportedTargets: List<String>,
    val plannedModules: List<String>,
)

/** Returns static metadata for tooling or diagnostics. */
fun chartLibraryInfo(): ChartLibraryInfo =
    ChartLibraryInfo(
        name = "ComposePlotLib",
        supportedTargets = listOf("Android", "iOS"),
        plannedModules = listOf("chart-core", "chart-compose", "android-showcase"),
    )
