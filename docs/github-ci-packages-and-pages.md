# GitHub Actions, Packages, and Pages

## CI overview

- **Pull requests** (`.github/workflows/pr.yml`): the `check` job on `ubuntu-latest` runs `ktlintCheck`, `apiCheck` (binary API check against the `.api` files), `:chart-core:verifyChartCorePurity` (KMP purity), unit tests (`chart-core` JVM + Android, `chart-compose` Android), Roborazzi verification, and `:samples:android-showcase:assembleDebug`. A separate `ios` job on `macos-14` (with `COMPOSEPLOTLIB_FORCE_IOS_TARGETS=true`) compiles the `iosArm64` and `iosSimulatorArm64` targets of both modules, so a `commonMain` change that breaks iOS is caught at PR time. Sets `COMPOSEPLOTLIB_VERSION=debug` (also the default from `gradle.properties` when the env var is unset).
- **Releases** (`.github/workflows/release.yml`): triggers on tags `vMAJOR.MINOR.PATCH` or `vMAJOR.MINOR.PATCH-prerelease`. The `validate` job verifies the tag is reachable from `main`. The `build-and-release` job (ubuntu) runs `ktlintCheck`, unit tests, Roborazzi verification, and `:samples:android-showcase:assembleDebug` (note: it does *not* re-run `apiCheck`, `verifyChartCorePurity`, or the iOS compile job), builds Dokka HTML, publishes the Android + JVM artifacts to **GitHub Packages**, creates a **GitHub Release** with `dokka-html.zip`, and uploads the Pages artifact. A `publish-ios` job on `macos-latest` publishes the iOS + Kotlin Multiplatform metadata artifacts (see [Publishing](#consuming-published-artifacts-github-packages) below). `deploy-pages` deploys the Dokka site to **GitHub Pages** (see below), and `publish-maven-central`, off until the repository variable `PUBLISH_TO_MAVEN_CENTRAL` is `true`, publishes to **Maven Central** (see [Maven Central](#maven-central-central-portal)).

A third workflow, `.github/workflows/sample-app.yml`, builds the sample app and is outside the scope of this doc.

## Library version in CI

The Gradle version is resolved as:

1. Environment variable `COMPOSEPLOTLIB_VERSION` (set in workflows)
2. Otherwise Gradle property `composeplotlib.version` in `gradle.properties`
3. Otherwise `debug`

Release workflows strip the leading `v` from the tag (e.g. `v1.2.3` → `1.2.3`).

## Consuming published artifacts (GitHub Packages)

Publishing happens across two release jobs:

- **`build-and-release` (ubuntu)** publishes the JVM + Android artifacts: `chart-core` Android + JVM, and `chart-compose` Android. `chart-compose` targets Android, iOS and Wasm only (no JVM target), so it has no JVM publication.
- **`publish-ios` (macOS)** publishes the `iosArm64`, `iosSimulatorArm64` and `wasmJs` artifacts plus the `kotlinMultiplatform` metadata publication for **both** `chart-core` and `chart-compose`. The Wasm target is declared on macOS hosts only, with the iOS targets, because Kotlin 2.4 writes one klib ABI dump for all of them; this job also compiles Wasm, runs `chart-core`'s tests on it and runs `apiCheck` against that dump before publishing. The KMP metadata publication lets multiplatform consumers depend on the root coordinates (`com.mena97villalobos.composeplotlib:chart-core` / `:chart-compose`) and have Gradle resolve the right platform variant automatically.

Add the Maven repository and authenticate with a token that has `read:packages` (classic PAT) or the GitHub Packages read scope (fine-grained PAT).

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/mena97villalobos/ComposePlotLib")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}
```

Dependencies (example):

```kotlin
implementation("com.mena97villalobos.composeplotlib:chart-core:1.2.0")
implementation("com.mena97villalobos.composeplotlib:chart-compose:1.2.0")
```

Use the same **VERSION** string as published (e.g. `1.2.3` without `v`).

**Note:** iOS/Apple artifacts **are** published by CI. The `publish-ios` job (on `macos-latest`) publishes `iosArm64`, `iosSimulatorArm64`, and the Kotlin Multiplatform metadata publications for both `chart-core` and `chart-compose`, alongside the Android + JVM artifacts from the Linux `build-and-release` job. Multiplatform consumers can depend on the KMP artifacts directly — no local Mac build or manual publishing job is required.

## GitHub Pages (Dokka)

1. In the repository on GitHub: **Settings → Pages**.
2. Under **Build and deployment**, set **Source** to **GitHub Actions** (not “Deploy from a branch”) so the `deploy-pages` job can publish.
3. The first successful run of the Release workflow after this setting will deploy `build/dokka/html` (aggregated Dokka for `chart-core` and `chart-compose`).
4. Optional: set a **Custom domain** in the same Settings page; add DNS records as prompted by GitHub.

If GitHub prompts for an environment, approve the **github-pages** environment once so the deploy job can run.

## Maven Central (Central Portal)

GitHub Packages requires every consumer — and every consumer's CI — to authenticate with a personal
access token, even for public packages. Maven Central does not, so it is the channel that makes the
library realistically adoptable. The build is already wired for it; what remains is account setup.

Publishing is handled by the [vanniktech maven-publish plugin](https://vanniktech.github.io/gradle-maven-publish-plugin/)
applied in `build.gradle.kts`. It attaches a sources jar and a Dokka-generated javadoc jar to **every**
KMP publication (including the `kotlinMultiplatform` metadata module, which Central rejects a bundle
for if its javadoc jar is missing), signs everything, and uploads a single bundle to the Portal.

### One-time setup (outside the repo)

1. **Create a Central Portal account** at <https://central.sonatype.com>.

2. **Verify the `com.mena97villalobos` namespace.** Because this is a `com.` group id rather than an
   `io.github.` one, Central requires proof that you own **`mena97villalobos.com`**. In the Portal,
   add the namespace, then create the DNS record it shows you — a `TXT` record on the apex of
   `mena97villalobos.com` whose value is the verification key the Portal generates (a short
   alphanumeric token). Once DNS propagates, click Verify.

   > If you do not own `mena97villalobos.com`, this namespace cannot be verified. The alternative is
   > switching the group id to `io.github.mena97villalobos`, which Central verifies instantly against
   > the GitHub account — but that changes consumer coordinates and is a breaking change.

3. **Generate a user token** in the Portal (Account → Generate User Token). This yields a username
   and password pair distinct from your login credentials.

4. **Create a GPG signing key** and publish the public half:

   ```shell
   gpg --full-generate-key                      # RSA 4096, no expiry is fine
   gpg --list-secret-keys --keyid-format=short  # note the 8-char key id
   gpg --keyserver keyserver.ubuntu.com --send-keys <FULL_FINGERPRINT>
   ```

   The key cannot be replaced the way an app keystore can: consumers verify every published artifact
   against it. Store it in klave (below) as soon as it exists.

### Repository configuration

The five values live in **klave**, not in GitHub secrets. The `publish-maven-central` job fetches them
with `klave-fetch` right before its publish step; klave's grant `composeplotlib-release` lets only
`release.yml` on a `v*` tag read them.

| klave path | Contents |
|------------|----------|
| `composeplotlib/maven-central-username` | Portal user-token username |
| `composeplotlib/maven-central-password` | Portal user-token password |
| `composeplotlib/signing-key` | ASCII-armored private key from `gpg --armor --export-secret-keys` |
| `composeplotlib/signing-key-id` | 8-character short key id |
| `composeplotlib/signing-key-password` | Passphrase for the GPG key |

Store them with klave's `scripts/klave.sh` and an automation token (klave's `docs/automation.md`), piping
each value in so it never lands in the shell history or a file:

```shell
KLAVE=~/Desktop/bmv/klave/scripts/klave.sh
read -rs v && printf %s "$v" | "$KLAVE" put-text composeplotlib/maven-central-username; unset v
read -rs v && printf %s "$v" | "$KLAVE" put-text composeplotlib/maven-central-password; unset v
gpg --armor --export-secret-keys <KEY_ID> | "$KLAVE" put-text composeplotlib/signing-key
printf %s <KEY_ID> | "$KLAVE" put-text composeplotlib/signing-key-id
read -rs v && printf %s "$v" | "$KLAVE" put-text composeplotlib/signing-key-password; unset v
```

Then add the **variable** `PUBLISH_TO_MAVEN_CENTRAL` = `true`. The `publish-maven-central` job in
`release.yml` is gated on it, so releases keep succeeding until every item above is in place. Once it
is on, a Maven Central release needs klave to be up.

### Behavior

Deployments land in the Portal in `VALIDATED` state — they are **not** auto-released. Review and
press Publish in the Portal UI. Signing is conditional on `signingInMemoryKey` being present, so PR
builds, `publishToMavenLocal`, and the GitHub Packages jobs all continue to work without a key.

Verify locally before tagging:

```shell
./gradlew :chart-core:publishToMavenLocal :chart-compose:publishToMavenLocal
ls ~/.m2/repository/com/mena97villalobos/composeplotlib/
```

Each published module directory should contain a main jar, `-sources.jar`, `-javadoc.jar`, `.pom`
and `.module`.
