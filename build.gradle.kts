import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.changelog.Changelog

plugins {
    kotlin("jvm") version "1.9.21"
    groovy

    id("org.jetbrains.intellij") version "1.16.1"
    id("org.jetbrains.changelog") version "2.2.0"
}

group = "io.github.lobodpav"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.stdlib)

    testImplementation(testLibs.bundles.unit.spec)
}

kotlin {
    jvmToolchain(17)
}

intellij {
    // The version of the IntelliJ Platform IDE that will be used to build the plugin
    version = "2023.3"

    // The type of the IntelliJ-based IDE distribution
    type = "IC" // IntelliJ Community

    // Dependencies of this plugin
    plugins = listOf("com.intellij.java", "org.intellij.groovy")
}

tasks {
    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    test {
        useJUnitPlatform()

        testLogging {
            events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
            exceptionFormat = TestExceptionFormat.FULL
        }

        // A reference to the `IntelliJ Community` sources that contains a bundled mockJDK.
        // Prevents highlighting from returning a lot of noise, such as `Cannot resolve symbol 'java'`.
        systemProperty("idea.home.path", providers.environmentVariable("INTELLIJ_COMMUNITY_SOURCES").get())
    }

    // Allows Groovy code and tests to see Kotlin code
    compileGroovy.get().classpath += files(compileKotlin.get().destinationDirectory)
    compileTestGroovy.get().classpath += files(compileTestKotlin.get().destinationDirectory)

    signPlugin {
        certificateChain = providers.environmentVariable("INTELLIJ_CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("INTELLIJ_PRIVATE_KEY")
        password = providers.environmentVariable("INTELLIJ_PRIVATE_KEY_PASSWORD")
    }

    publishPlugin {
        token = providers.environmentVariable("INTELLIJ_PUBLISH_TOKEN")
    }

    patchPluginXml {
        sinceBuild = "233" // Support IntelliJ 2023.3 and up
        untilBuild = ""    // Allow all future IntelliJ versions
        changeNotes = provider {
            changelog.getAll().values.asSequence()
                .filter { !it.isUnreleased }
                .map { changelog.renderItem(it, Changelog.OutputType.HTML) }
                .toList()
                .joinToString("")
        }
    }
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
