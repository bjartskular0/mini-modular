package dev.akerstrom.plugins.impl

import dev.akerstrom.plugins.PluginService
import dev.akerstrom.plugins.ktor.singlePageApplication
import io.ktor.server.response.*
import io.ktor.server.routing.*

class PluginImpl : PluginService() {
    override fun Route.routes() {
        route("/api") {
            get("/hello") {
                call.respondText("Hello from ${getName()}!")
            }
        }
        route("/app") {
            singlePageApplication {
                filesPath = "dist"
                resourceClass = PluginImpl::class.java
            }
        }
    }

    override fun shutdown() {
    }
}
