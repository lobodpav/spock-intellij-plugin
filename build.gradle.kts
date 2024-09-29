import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    alias(libs.plugins.kotlin)
    groovy

    alias(libs.plugins.intellij)
    alias(libs.plugins.changelog)
}

group = "io.github.lobodpav"
version = "1.1.0-SNAPSHOT"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)

    testImplementation(testLibs.bundles.unit.spec)

    // A workaround: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-faq.html#junit5-test-framework-refers-to-junit4
    testImplementation(testLibs.junit4)

    testRuntimeOnly(testLibs.mockito.core)

    intellijPlatform {
        // The version of the IntelliJ Platform IDE that will be used to build the plugin.
        // Also dictates the minimum version of the IDE that the plugin will be compatible with because the `ideaVersion.sinceBuild` is not defined.
        // Run `gradle printProductsReleases` to find out the latest production release as well as an EAP release.
        intellijIdeaCommunity("2024.2")
        instrumentationTools()
        pluginVerifier()
        zipSigner()

        bundledPlugins("com.intellij.java", "org.intellij.groovy", "org.jetbrains.kotlin")

        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Plugin.Java)
    }
}

// The `testFramework(TestFrameworkType.Plugin.Java)` requires `org.codehaus.groovy:groovy:3.x` dependency.
// However, this project needs `org.apache.groovy:groovy:4.x`.
// Since the `testFramework` helper method does not support dependency exclusions, this workaround is necessary.
// See https://intellij-support.jetbrains.com/hc/en-us/community/posts/21678535198354-Migrating-from-Gradle-intelliJ-Plugin-v1-to-v2
configurations.testRuntimeClasspath {
    exclude(group = "org.codehaus.groovy", module = "groovy")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    compileKotlin {
        compilerOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    runIde {
        jvmArgumentProviders += CommandLineArgumentProvider {
            listOf("-Didea.kotlin.plugin.use.k2=true")
        }
    }

    test {
        useJUnitPlatform()

        testLogging {
            events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
            exceptionFormat = TestExceptionFormat.FULL
        }

        jvmArgumentProviders += CommandLineArgumentProvider {
            listOf("-Didea.kotlin.plugin.use.k2=true")
        }

        // A reference to the `IntelliJ Community` sources that contains a bundled mockJDK.
        // Prevents highlighting from returning a lot of noise, such as `Cannot resolve symbol 'java'`.
        // https://plugins.jetbrains.com/docs/intellij/testing-faq.html#how-to-test-a-jvm-language
        systemProperty("idea.home.path", providers.environmentVariable("INTELLIJ_COMMUNITY_SOURCES").get())
        // Enable debug logs in tests
        systemProperty("idea.log.debug.categories", "io.github.lobodpav.spock")
    }

    // Allows Groovy code and tests to see Kotlin code
    compileGroovy.get().classpath += files(compileKotlin.get().destinationDirectory)
    compileTestGroovy.get().classpath += files(compileTestKotlin.get().destinationDirectory)

    intellijPlatform {
        pluginConfiguration {
            id = "io.github.lobodpav.spock"
            name = "Spock Framework Support"
            vendor {
                name = "Pavel Lobodinský"
                url = "https://github.com/lobodpav/spock-intellij-plugin"
            }
            ideaVersion {
                untilBuild = provider { null } // All future versions
            }
            description = """
                <p>
                    Provides support for the amazing <a href="https://spockframework.org">Spock testing framework</a>.
                </p>
                <br>
                <p>
                    Inspired by the <a href="https://plugins.jetbrains.com/plugin/7114-spock-framework-enhancements">Spock Framework Enhancements</a> plugin
                    which is <a href="https://github.com/cholick/idea-spock-enhancements/blob/master/README.md">not actively maintained</a>,
                    and does not fully function in the newest IntelliJ versions due to breaking API changes.
                </p>
                
                <h2>Features</h2>
                <ul>
                    <li>Validation of block names, their order and completeness (e.g. invalid blocks, missing <code>then</code> blocks, etc.)</li>
                    <li>Creation of new Spock Specification using a <code>New File</code> action</li>
                    <li>Go To Test allows creation of Spock Specifications in Groovy, Java and Kotlin files</li>
                </ul>
                <br>
                <p>
                    Stay tuned for more.
                </p>
            """.trimIndent()

            changeNotes = provider {
                changelog.getAll().values.asSequence()
                    .filter { !it.isUnreleased }
                    .map { changelog.renderItem(it, Changelog.OutputType.HTML) }
                    .toList()
                    .joinToString("")
            }
        }

        pluginVerification {
            ides {
                recommended()
            }
        }

        signing {
            certificateChain = providers.environmentVariable("INTELLIJ_CERTIFICATE_CHAIN")
            privateKey = providers.environmentVariable("INTELLIJ_PRIVATE_KEY")
            password = providers.environmentVariable("INTELLIJ_PRIVATE_KEY_PASSWORD")
        }

        publishing {
            token = providers.environmentVariable("INTELLIJ_PUBLISH_TOKEN")
        }

        /**
         * See the [gradle-changelog-plugin](https://github.com/JetBrains/gradle-changelog-plugin) for more details.
         *
         * The changelog needs to conform to the [keep a changelog](https://keepachangelog.com) style.
         */
        changelog {
            introduction = "Provides support for the amazing [Spock testing framework](https://spockframework.org)."
            keepUnreleasedSection = false
        }
    }
}
