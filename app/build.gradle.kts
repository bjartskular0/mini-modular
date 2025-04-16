plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm-module.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")

    alias(libs.plugins.kotlinPluginSerialization)

    // Apply the Application plugin to add support for building an executable JVM application.
    application
}

dependencies {
    // Project "app" depends on project "utils". (Project paths are separated with ":", so ":utils" refers to the top-level "utils" project.)
    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.bundles.ktomlEcosystem)
    implementation(project(":plugin_stdlib"))
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "dev.akerstrom.app.AppKt"
}
