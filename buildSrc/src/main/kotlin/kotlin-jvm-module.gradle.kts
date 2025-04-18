// The code in this file is a convention plugin - a Gradle mechanism for sharing reusable build logic.
// `buildSrc` is a Gradle-recognized directory and every plugin there will be easily available in the rest of the build.
package buildsrc.convention

import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin in JVM projects.
    kotlin("jvm")
    `java-library`
}

kotlin {
    // Use a specific Java version to make it easier to work in different environments.
    jvmToolchain(20)
}

tasks.jar {
    manifest {
//        attributes("Automatic-Module-Name" to "dev.akerstrom.${project.name}")
    }
}

tasks.named("compileJava", JavaCompile::class.java) {
    val moduleName = "dev.akerstrom.${project.name}"
    val moduleLoc = sourceSets["main"].output.asPath
    options.compilerArgumentProviders.add(CommandLineArgumentProvider {
        // Provide compiled Kotlin classes to javac – needed for Java/Kotlin mixed sources to work
        listOf("--patch-module", "${moduleName}=${moduleLoc}")
    })
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