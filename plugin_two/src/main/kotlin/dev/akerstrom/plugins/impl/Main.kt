package dev.akerstrom.plugins.impl

import dev.akerstrom.plugins.PluginService
import dev.akerstrom.plugins.ktor.singlePageApplication
import io.ktor.server.response.*
import io.ktor.server.routing.*

// Do not change the name or package of PluginImpl
class PluginImpl : PluginService() {
    override fun Route.routes() {
        route("/api") {
            get("/goodbye") {
                call.respondText("Goodbye from ${getName()}!")
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
