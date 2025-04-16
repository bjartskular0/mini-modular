// The code in this file is a convention plugin - a Gradle mechanism for sharing reusable build logic.
// `buildSrc` is a Gradle-recognized directory and every plugin there will be easily available in the rest of the build.
package buildsrc.convention

import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.`java-library`
import org.gradle.kotlin.dsl.support.serviceOf

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin in JVM projects.
    kotlin("jvm")
    `java-library`
}

kotlin {
    // Use a specific Java version to make it easier to work in different environments.
    jvmToolchain(22)
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "dev.akerstrom.${project.name}")
    }
}

tasks.withType<Test>().configureEach {
    // Configure all test Gradle tasks to use JUnitPlatform.
    useJUnitPlatform()

    // Log information about all test results, not only the failed ones.
    testLogging {
        events(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED
        )
    }
}

tasks.register<Copy>("copyLibs") {
    val parentDir = parent?.layout?.buildDirectory
    onlyIf { parentDir != null }

    from(layout.buildDirectory.dir("libs"))
    into(parentDir!!.dir("libs"))
    include("*.jar")
}

tasks.named("build") {
    finalizedBy("copyLibs")
}