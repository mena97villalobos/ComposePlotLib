import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.dokka)
    `maven-publish`
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
