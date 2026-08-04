import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import java.io.File
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
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
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.binaryCompatibilityValidator)
}

// Track the published binary API (JVM/Android ABI) so `copy()`/constructor/signature changes on the
// public spec, model, and composable surface are a reviewed `.api` diff (run `apiDump` to refresh).
// Only the published library modules are validated; apps, the showcase, and benchmarks are excluded.
apiValidation {
    ignoredProjects.addAll(listOf("android-showcase", "chart-core-benchmarks"))
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

                // Every published artifact must be self-describing: consumers' dependency
                // scanners read the license/SCM metadata off the POM, not off the repo.
                publications.withType<MavenPublication>().configureEach {
                    pom {
                        name.set("ComposePlotLib ${project.name}")
                        description.set(
                            "Kotlin Multiplatform + Compose Multiplatform charting library " +
                                "for Android and iOS.",
                        )
                        url.set("https://github.com/mena97villalobos/ComposePlotLib")
                        licenses {
                            license {
                                name.set("The Apache License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                                distribution.set("repo")
                            }
                        }
                        developers {
                            developer {
                                id.set("mena97villalobos")
                                name.set("Bryan Mena")
                                url.set("https://github.com/mena97villalobos")
                            }
                        }
                        scm {
                            url.set("https://github.com/mena97villalobos/ComposePlotLib")
                            connection.set(
                                "scm:git:https://github.com/mena97villalobos/ComposePlotLib.git",
                            )
                            developerConnection.set(
                                "scm:git:ssh://git@github.com/mena97villalobos/ComposePlotLib.git",
                            )
                        }
                    }
                }
            }
        }
    }
}

// Maven Central (Central Portal) publishing. Only the two library modules apply the plugin. It
// reuses the publications maven-publish already creates and adds the sources jar, javadoc jar, GPG
// signatures, and the Portal bundle upload that Central requires. The GitHubPackages repository and
// POM block above are untouched, so GitHub Packages publishing keeps working exactly as before.
subprojects {
    pluginManager.withPlugin("com.vanniktech.maven.publish") {
        extensions.configure<MavenPublishBaseExtension> {
            // KMP layout is the plugin's job: it attaches javadoc + sources jars to EVERY target
            // publication including the Kotlin metadata one. Central rejects any module missing a
            // javadoc jar, and hand-rolled maven-publish config routinely misses the metadata one.
            configure(
                KotlinMultiplatform(
                    javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
                    sourcesJar = true,
                ),
            )

            // Deployments land VALIDATED; a human presses Publish in the Portal UI. Deliberately
            // not auto-released.
            publishToMavenCentral()

            // Signing stays conditional so PR builds, publishToMavenLocal, and the existing GitHub
            // Packages release steps keep working without a GPG key present.
            if (providers.gradleProperty("signingInMemoryKey").isPresent) {
                signAllPublications()
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
