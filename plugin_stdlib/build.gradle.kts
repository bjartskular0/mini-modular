plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm-module.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    id("buildsrc.convention.module-info")
    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    // Apply the kotlinx bundle of dependencies from the version catalog (`gradle/libs.versions.toml`).
    api(libs.bundles.kotlinxEcosystem)
    api(libs.bundles.ktorEcosystem)
    api(libs.bundles.ktomlEcosystem)
    api(libs.logback.classic)
//    api(project(":autodeps"))
    testImplementation(kotlin("test"))
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "dev.akerstrom.${project.name}")
    }
}
