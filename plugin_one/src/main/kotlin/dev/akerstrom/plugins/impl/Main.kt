package dev.akerstrom.plugins.impl

import dev.akerstrom.plugins.PluginService
import dev.akerstrom.plugins.ktor.singlePageApplication
import io.ktor.server.response.*
import io.ktor.server.routing.*

class PluginOne : PluginService() {
    override fun routes(): Route.() -> Unit = {
        route("/${getHostname()}") {
            route("/api") {
                get("/hello") {
                    call.respondText("Hello from ${getName()}!")
                }
            }
            route("/app") {
                singlePageApplication {
                    filesPath = "dist"
                    resourceClass = PluginOne::class.java
                }
            }
        }
    }
}
