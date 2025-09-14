import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.Constants
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask
import java.util.EnumSet

fun properties(key: String): String = providers.gradleProperty(key).get()
fun environment(key: String): Provider<String> = providers.environmentVariable(key)
fun extra(key: String): String = project.ext.get(key) as String

val javaVersion = properties("javaVersion")
val platformBundledPlugins = providers.gradleProperty("platformBundledPlugins")
val platformPlugins = providers.gradleProperty("platformPlugins")
val platformType = properties("platformType")
val platformVersion = properties("platformVersion")
val pluginGroup = properties("pluginGroup")
val pluginName = properties("pluginName")
val pluginRepositoryUrl = properties("pluginRepositoryUrl")
val pluginSinceBuild = properties("pluginSinceBuild")
val pluginUntilBuild = properties("pluginUntilBuild")
val pluginVerifierExcludeFailureLevels = properties("pluginVerifierExcludeFailureLevels")
val pluginVerifierIdeVersions = properties("pluginVerifierIdeVersions")
val pluginVerifierMutePluginProblems = properties("pluginVerifierMutePluginProblems")
val pluginVersion = properties("pluginVersion")
val shouldPublishPlugin = properties("publishPlugin").equals("true")

plugins {
    id("java")
    id("idea")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
    id("com.dorongold.task-tree")
}

group = pluginGroup
version = pluginVersion

repositories {
    mavenCentral()

    // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
    intellijPlatform {
        defaultRepositories()
        // `jetbrainsRuntime()` is necessary mostly for EAP/SNAPSHOT releases of IJ so that the IDE pulls the correct JBR
        //      - https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-jetbrains-runtime.html#obtained-with-intellij-platform-from-maven
        val isSnapshot = platformVersion.endsWith("-SNAPSHOT")
        if (isSnapshot) {
            jetbrainsRuntime()
        }
    }
}


dependencies {
    // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        val isSnapshot = platformVersion.endsWith("-SNAPSHOT")
        create(
            type = IntelliJPlatformType.fromCode(platformType),
            version = platformVersion,
        ) {
            // `useInstaller` needs to be set to 'false' (aka, `isSnapshot` = 'true') to resolve EAP releases.
            useInstaller = !isSnapshot
            useCache = true
        }

        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
        plugins(platformPlugins.map { it.split(',') })
        // Plugin Dependencies. Uses `platformBundledPlugins` property from the gradle.properties file for bundled IntelliJ Platform plugins.
        bundledPlugins(platformBundledPlugins.map { it.split(',') })

        // `jetbrainsRuntime()` is necessary mostly for EAP/SNAPSHOT releases of IJ so that the IDE pulls the correct JBR
        //      - https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-jetbrains-runtime.html#obtained-with-intellij-platform-from-maven
        if (isSnapshot) {
            jetbrainsRuntime()
        }
        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
    }

    testImplementation(group = "junit", name = "junit", version = "4.13.2")
    testImplementation("org.testng:testng:7.10.2")
    testImplementation("org.mockito:mockito-core:5.12.0")
}

java {
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}

idea {
    project {
        // Set default IntelliJ SDK & language level to the version specified in `gradle.properties`
        jdkName = javaVersion
        languageLevel = IdeaLanguageLevel(javaVersion)
    }
}

val isNotCI = System.getenv("CI") != "true"
if (isNotCI) {
    // The below file (jetbrainsCredentials.gradle) should contain the below:
    //     project.ext.set("intellijSignPluginCertificateChain", new File("./chain.crt").getText("UTF-8"))
    //     project.ext.set("intellijSignPluginPrivateKey", new File("./private.pem").getText("UTF-8"))
    //     project.ext.set("intellijSignPluginPassword", "YOUR_PRIV_KEY_PASSWORD_HERE")
    //     project.ext.set("intellijPluginPublishToken", "YOUR_TOKEN_HERE")
    //
    // Because this contains credentials, this file is also included in .gitignore file.
    apply(from = "jetbrainsCredentials.gradle")
}
fun resolve(extraKey: String, environmentKey: String): String =
    if (isNotCI) extra(extraKey) else environment(environmentKey).getOrElse("DEFAULT_INVALID_$environmentKey")

val signPluginCertificateChain = resolve("intellijSignPluginCertificateChain", "CERTIFICATE_CHAIN")
val signPluginPrivateKey = resolve("intellijSignPluginPrivateKey", "PRIVATE_KEY")
val signPluginPassword = resolve("intellijSignPluginPassword", "PRIVATE_KEY_PASSWORD")
val publishPluginToken = resolve("intellijPluginPublishToken", "PUBLISH_TOKEN")

// https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html
intellijPlatform {
    pluginConfiguration {
        name = pluginName
        version = pluginVersion

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = providers.provider { pluginVersion }.map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased()).withHeader(false).withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = pluginSinceBuild
            untilBuild = pluginUntilBuild
        }

        // Vendor information -> https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html#intellijPlatform-pluginConfiguration-vendor
        vendor {
            name = "Chris Carini"
            email = "jetbrains@chriscarini.com"
            url = "https://jetbrains.chriscarini.com"
        }
    }

    signing {
        certificateChain = signPluginCertificateChain
        privateKey = signPluginPrivateKey
        password = signPluginPassword
    }

    publishing {
        token = publishPluginToken

        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html#specifying-a-release-channel
        channels = providers.provider { pluginVersion }.map {
            listOf(it.substringAfter('-', "").substringBefore('.').lowercase().ifEmpty { "default" })
        }
    }

    pluginVerification {
        val pluginVerifierMutePluginProblems = pluginVerifierMutePluginProblems
        if (pluginVerifierMutePluginProblems.isNotEmpty()) {
            logger.lifecycle("Muting the following Plugin Verifier Problems: $pluginVerifierMutePluginProblems")
            freeArgs = listOf("-mute", pluginVerifierMutePluginProblems)
        }

        fun getFailureLevels(): EnumSet<VerifyPluginTask.FailureLevel> {
            val includeFailureLevels = EnumSet.allOf(VerifyPluginTask.FailureLevel::class.java)
            val desiredFailureLevels =
                pluginVerifierExcludeFailureLevels.split(",").map(String::trim)
                    .filter(String::isNotEmpty) // Remove empty strings; this happens when user sets nothing (ie, `pluginVerifierExcludeFailureLevels =`)

            desiredFailureLevels.forEach { failureLevel ->
                when (failureLevel) {
                    "ALL" -> return EnumSet.allOf(VerifyPluginTask.FailureLevel::class.java)
                    "NONE" -> return EnumSet.noneOf(VerifyPluginTask.FailureLevel::class.java)
                    else -> {
                        try {
                            val enumFailureLevel = VerifyPluginTask.FailureLevel.valueOf(failureLevel)
                            includeFailureLevels.remove(enumFailureLevel)
                        } catch (ignored: Exception) {
                            val msg = "Failure Level \"$failureLevel\" is *NOT* valid. Please select from: ${
                                EnumSet.allOf(VerifyPluginTask.FailureLevel::class.java)
                            }."
                            logger.error(msg)
                            throw Exception(msg)
                        }
                    }
                }
            }

            return includeFailureLevels
        }

        val failureLevels = getFailureLevels()
        logger.debug("Using ${failureLevels.size} Failure Levels: $failureLevels")
        failureLevel.set(failureLevels)
        ides {
            logger.lifecycle("Verifying against IntelliJ Platform $platformType $platformVersion")
            create(IntelliJPlatformType.fromCode(platformType), platformVersion) {
                useCache = true
            }

            recommended()
        }
    }
}

// Configure CHANGELOG.md - https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    repositoryUrl = pluginRepositoryUrl
}

tasks {
    publishPlugin {
        dependsOn(patchChangelog)
        logger.lifecycle("Should Publish Plugin?: $shouldPublishPlugin")
        enabled = shouldPublishPlugin
    }

    printProductsReleases {
//        // In addition to `IC` (from `platformType`), we also want to verify against `IU`.
//        types.set(Arrays.asList(platformType, "IU"))
        // Contrary to above, we want to speed up CI, so skip verification of `IU`.
        types = listOf(IntelliJPlatformType.fromCode(platformType))

        // Only get the released versions if we are not targeting an EAP.
        val isEAP = pluginVersion.uppercase().endsWith("-EAP")
        channels = listOf(if (isEAP) ProductRelease.Channel.EAP else ProductRelease.Channel.RELEASE)

        // Verify against the most recent patch release of the `platformVersion` version
        // (ie, if `platformVersion` = 2022.2, and the latest patch release is `2022.2.2`,
        // then the `sinceVersion` will be set to `2022.2.2` and *NOT* `2022.2`.
        sinceBuild.set(platformVersion)
    }

    // Sanity check task to ensure necessary variables are set.
    register("checkJetBrainsSecrets") {
        doLast {
            println("signPluginCertificateChain: ${if (signPluginCertificateChain.isNotEmpty()) "IS" else "is NOT"} set.")
            println("signPluginPrivateKey:       ${if (signPluginPrivateKey.isNotEmpty()) "IS" else "is NOT"} set.")
            println("signPluginPassword:         ${if (signPluginPassword.isNotEmpty()) "IS" else "is NOT"} set.")
            println("publishPluginToken:         ${if (publishPluginToken.isNotEmpty()) "IS" else "is NOT"} set.")
        }
    }

    // In "IntelliJ Platform Gradle Plugin 2.*", the `listProductsReleases` task no longer exists, but
    // instead the `printProductReleases` task does. This task is necessary to take the output of
    // `printProductReleases` and write it to a file for use in the `generateIdeVersionsList` task below.
    val listProductReleasesTaskName = "listProductsReleases"
    register(listProductReleasesTaskName) {
        dependsOn(printProductsReleases)
        val outputF = layout.buildDirectory.file("listProductsReleases.txt").also {
            outputs.file(it)
        }
        val content = printProductsReleases.flatMap { it.productsReleases }.map { it.joinToString("\n") }

        doLast {
            outputF.orNull?.asFile?.writeText(content.get())
        }
    }

    // Task to generate the necessary format for `ChrisCarini/intellij-platform-plugin-verifier-action` GitHub Action.
    register<DefaultTask>("generateIdeVersionsList") {
        dependsOn(project.tasks.named(listProductReleasesTaskName))
        doLast {
            val ideVersionsList = mutableListOf<String>()

            // Include the versions produced from the `listProductsReleases` task.
            project.tasks.named(listProductReleasesTaskName).get().outputs.files.singleFile.forEachLine { line ->
                ideVersionsList.add("idea" + line.replace("-", ":"))
            }

            // Include the versions specified in `gradle.properties` `pluginVerifierIdeVersions` property.
            val environment: String = environment("GITHUB_EVENT_NAME").getOrElse("__")
            pluginVerifierIdeVersions.split(",").map { it.trim() }.forEach { version ->
                // Only add 'LATEST-EAP-SNAPSHOT' during scheduled runs.
                if ("LATEST-EAP-SNAPSHOT".equals(version) && !"schedule".equals(environment)) {
                    return@forEach
                }

                // Note: Used to test against both IC & IU, but now defaulting to just whatever is specified
                // by `platformType` in the `gradle.properties` file.
                listOf(platformType).forEach { type ->
                    ideVersionsList.add("idea$type:$version")
                }
            }

            // Write out file with unique pairs of type + version
            val outFileWriter = File(
                layout.buildDirectory.get().toString(), "intellij-platform-plugin-verifier-action-ide-versions-file.txt"
            ).printWriter()
            ideVersionsList.distinct().forEach { version ->
                outFileWriter.println(version)
            }
            outFileWriter.close()
        }
    }

    withType<JavaCompile> {
        options.compilerArgs.addAll(
            listOf(
                "-Xlint:all",
                "-Xlint:-options",
                "-Xlint:-rawtypes",
                "-Xlint:-processing",
                "-Xlint:-path", // Ignore JBR SDK manifest element warnings
                "-proc:none",
                "-Werror",
                "-Xlint:-classfile"
            )
        )
    }
}

val runIdeForUiTests by intellijPlatformTesting.runIde.registering {
    task {
        jvmArgumentProviders += CommandLineArgumentProvider {
            listOf(
                "-Drobot-server.port=8082",
                "-Dide.mac.message.dialogs.as.sheets=false",
                "-Djb.privacy.policy.text=<!--999.999-->",
                "-Djb.consents.confirmation.enabled=false",
            )
        }
    }

    plugins {
        robotServerPlugin(Constants.Constraints.LATEST_VERSION)
    }
}
