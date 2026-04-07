package com.mena97villalobos.composeplotlib.core

/**
 * Lightweight metadata describing the published library name and targets.
 *
 * For version information, use your build system (Gradle version catalog / POM), not this type.
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
