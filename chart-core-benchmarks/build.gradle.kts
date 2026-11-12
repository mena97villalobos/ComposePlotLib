plugins {
    `java-library`
    // Kotlin for benchmarks that build Kotlin specs with default arguments (no version: the Kotlin Gradle plugin is
    // already on the build classpath from the root project).
    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.jmh)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(project(":chart-core"))
}

jmh {
    includes = listOf("com\\.mena97villalobos\\.composeplotlib\\.benchmarks\\..*")
}
