plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm-module.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    id("buildsrc.convention.module-info")

    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.ktorPlugin)

    // Apply the Application plugin to add support for building an executable JVM application.
    application
}

dependencies {
    // Project "app" depends on project "utils". (Project paths are separated with ":", so ":utils" refers to the top-level "utils" project.)
    implementation(project(":plugin_stdlib"))
    implementation(libs.bundles.ktomlEcosystem)
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainModule = "dev.akerstrom.app"
    mainClass = "dev.akerstrom.app.App"
}
