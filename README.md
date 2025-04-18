# mini-modular

A barebones project to play around with making a plugin system and isolating each plugins resources.

* Run `./gradlew buildAndRun` to build and run the application.
This task will move generated plugin jar files to `build/libs` between the build and run steps.


This project follows the suggested multi-module setup and consists of the `app`, `plugin_stdlib`, `plugin_one` and `plugin_two` subprojects.
The shared build logic was extracted to convention plugins located in `buildSrc`.

This project uses a version catalog (see `gradle/libs.versions.toml`) to declare and version dependencies
and both a build cache and a configuration cache (see `gradle.properties`).

## Ktor

This version showcases how we can use dynamic plugins to set up routes and websites.
Where, for example, each plugin can define their own api routes and static routes.

Route structure:

* App Routes
    * `/`
    * `/routes`
* Plugin One Routes
    * `/routes/plugin_one/app` 
    * `/routes/plugin_one/api/hello`
* Plugin Two Routes
    * `/routes/plugin_two/app`
    * `/routes/plugin_two/api/goodbye` 
