import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.dokka)
    `maven-publish`
    alias(libs.plugins.mavenPublish)
}

dokka {
    dokkaPublications.html {
        moduleName.set("chart-core")
        failOnWarning.set(false)
    }
}

// Kotlin/Native iOS targets require Xcode; Linux CI only builds JVM + Android.
// Set COMPOSEPLOTLIB_FORCE_IOS_TARGETS=false on a Mac to match Linux CI (no Apple targets).
val isMacOsHost: Boolean =
    when (System.getenv("COMPOSEPLOTLIB_FORCE_IOS_TARGETS")?.lowercase()) {
        "true", "1", "yes" -> true
        "false", "0", "no" -> false
        else -> System.getProperty("os.name").contains("Mac", ignoreCase = true)
    }

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    if (isMacOsHost) {
        iosArm64()
        iosSimulatorArm64()
    }

    sourceSets {
        commonMain.dependencies {
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.mena97villalobos.composeplotlib.core"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// chart-core is commonMain-only and MUST stay KMP-pure: no Compose, JVM (java/javax), or Android imports
// (it compiles for iOS too). This guard runs in the Ubuntu PR gate (via `check`), where the iOS compile job
// is absent and so cannot catch a stray java.*/android.* import in commonMain.
val verifyChartCorePurity =
    tasks.register("verifyChartCorePurity") {
        group = "verification"
        description = "Fails if chart-core commonMain imports forbidden (Compose / JVM / Android) packages."
        val commonMainKotlin = layout.projectDirectory.dir("src/commonMain/kotlin").asFile
        val moduleRoot = projectDir
        inputs.dir(commonMainKotlin).withPropertyName("commonMainKotlin")
        doLast {
            val forbidden =
                Regex("^\\s*import\\s+(android\\.|androidx\\.|java\\.|javax\\.|org\\.jetbrains\\.compose)")
            val violations = mutableListOf<String>()
            commonMainKotlin.walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .forEach { file ->
                    file.readLines().forEachIndexed { index, line ->
                        if (forbidden.containsMatchIn(line)) {
                            violations += "${file.relativeTo(moduleRoot)}:${index + 1}: ${line.trim()}"
                        }
                    }
                }
            if (violations.isNotEmpty()) {
                throw GradleException(
                    "chart-core must stay KMP-pure (no Compose / JVM / Android imports in commonMain). " +
                        "Move platform-specific code to chart-compose or an androidMain source set.\nViolations:\n" +
                        violations.joinToString("\n"),
                )
            }
        }
    }

tasks.named("check") {
    dependsOn(verifyChartCorePurity)
}
