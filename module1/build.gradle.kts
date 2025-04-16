plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm-module.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm-module")
    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)

    `java-library`
}

dependencies {
    implementation(project(":plugin_stdlib"))
    testImplementation(kotlin("test"))
}
