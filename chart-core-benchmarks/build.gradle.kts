plugins {
    `java-library`
    alias(libs.plugins.jmh)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(project(":chart-core"))
}

jmh {
    includes = listOf("com\\.mena97villalobos\\.composeplotlib\\.benchmarks\\..*")
}
