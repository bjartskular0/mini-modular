# mini-modular

A barebones project to play around with making a plugin system and isolating each plugins resources.

* Run `./gradlew buildAndRun` to build and run the application.
This task will move generated plugin jar files to `build/libs` between the build and run steps.


This project follows the suggested multi-module setup and consists of the `app`, `plugin_stdlib`, `module1` and `module2` subprojects.
The shared build logic was extracted to a convention plugin located in `buildSrc`.

This project uses a version catalog (see `gradle/libs.versions.toml`) to declare and version dependencies
and both a build cache and a configuration cache (see `gradle.properties`).