import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.dokka)
    `maven-publish`
}

dokka {
    dokkaPublications.html {
        moduleName.set("chart-compose")
    }
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
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
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
