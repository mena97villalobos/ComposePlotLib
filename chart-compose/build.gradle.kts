import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.dokka)
    `maven-publish`
    alias(libs.plugins.mavenPublish)
}

dokka {
    dokkaPublications.html {
        moduleName.set("chart-compose")
        failOnWarning.set(false)
        includes.from(rootProject.layout.projectDirectory.file("docs/dokka/chart-compose-quick-start.md"))
    }
    dokkaSourceSets.configureEach {
        includes.from(rootProject.layout.projectDirectory.file("docs/dokka/chart-compose-quick-start.md"))
    }
}

// chart-core spec/model/value types are KMP-pure (no Compose annotations allowed), but are immutable.
// Tell the Compose compiler to treat them as stable so charts can skip on structurally-equal specs.
composeCompiler {
    stabilityConfigurationFiles.add(
        rootProject.layout.projectDirectory.file("compose-stability.conf"),
    )
}

// Kotlin/Native iOS targets require Xcode; Linux CI only builds Android. iOS klibs are not produced on Linux.
// Set COMPOSEPLOTLIB_FORCE_IOS_TARGETS=false on a Mac to match Linux CI (no Apple targets).
val isMacOsHost: Boolean =
    when (System.getenv("COMPOSEPLOTLIB_FORCE_IOS_TARGETS")?.lowercase()) {
        "true", "1", "yes" -> true
        "false", "0", "no" -> false
        else -> System.getProperty("os.name").contains("Mac", ignoreCase = true)
    }

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // No iosX64: Compose Multiplatform 1.11.1 publishes no ios_x64 klib variants (verified —
    // compose.runtime / foundation / material3 / ui all fail variant resolution for that target),
    // so Intel-simulator support is blocked upstream rather than by choice. chart-core alone would
    // compile for it, but shipping the target for only half the library would be worse than not
    // shipping it. Revisit when Compose Multiplatform restores ios_x64.
    if (isMacOsHost) {
        iosArm64()
        iosSimulatorArm64()
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        commonMain.dependencies {
            api(project(":chart-core"))
            // `api`, not `implementation`: these artifacts appear in chart-compose's own public
            // signatures (Composer, Modifier, Color, ImageBitmap, BoxScope, material3 ColorScheme),
            // so consumers need them on their compile classpath, not only at runtime.
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.material3)
            api(libs.compose.ui)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.mena97villalobos.composeplotlib.chart"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
