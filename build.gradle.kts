import java.io.File
import org.gradle.api.publish.PublishingExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.jmh) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.dokka)
}

val composeplotlibVersion: String =
    System.getenv("COMPOSEPLOTLIB_VERSION")?.takeIf { it.isNotBlank() }
        ?: (findProperty("composeplotlib.version") as? String)?.takeIf { it.isNotBlank() }
        ?: "debug"

subprojects {
    group = "com.mena97villalobos.composeplotlib"
    version = composeplotlibVersion
}

subprojects {
    afterEvaluate {
        pluginManager.withPlugin("maven-publish") {
            extensions.configure<PublishingExtension> {
                repositories {
                    // GitHub Actions sets GITHUB_REPOSITORY. Locally, set the env var or
                    // composeplotlib.github.repository in gradle.properties (owner/repo).
                    val githubRepo =
                        providers.environmentVariable("GITHUB_REPOSITORY").orNull?.takeIf { it.isNotBlank() }
                            ?: (findProperty("composeplotlib.github.repository") as? String)?.takeIf { it.isNotBlank() }
                    if (!githubRepo.isNullOrBlank()) {
                        maven {
                            name = "GitHubPackages"
                            url = uri("https://maven.pkg.github.com/$githubRepo")
                            credentials {
                                username = System.getenv("GITHUB_ACTOR")
                                password = System.getenv("GITHUB_TOKEN")
                            }
                        }
                    }
                }
            }
        }
    }
}

subprojects {
    val ktlintProjects = setOf("chart-core", "chart-compose", "android-showcase")
    if (name in ktlintProjects) {
        apply(plugin = "org.jlleitschuh.gradle.ktlint")
    }
}

subprojects {
    afterEvaluate {
        val buildDirPrefix = project.projectDir.absolutePath + File.separator + "build"
        pluginManager.withPlugin("org.jlleitschuh.gradle.ktlint") {
            extensions.configure<KtlintExtension> {
                filter {
                    exclude { element ->
                        element.file.absolutePath.startsWith(buildDirPrefix)
                    }
                }
            }
        }
    }
}

dokka {
    dokkaPublications.html {
        moduleName.set("ComposePlotLib")
        outputDirectory.set(layout.buildDirectory.dir("dokka/html"))
        includes.from(layout.projectDirectory.file("docs/dokka/quick-start.md"))
    }
}

dependencies {
    dokka(project(":chart-core"))
    dokka(project(":chart-compose"))
}
