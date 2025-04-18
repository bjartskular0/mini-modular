package dev.akerstrom.app

import dev.akerstrom.plugins.PluginService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.lang.module.ModuleFinder
import java.util.*
import kotlin.io.path.Path

object App {
    @JvmStatic
    fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)
}

fun Application.module() {
    // Read config
    val config = AppConfig.loadConfig()

    // Get a path to the plugins directory
    val pluginsPath = Path(config.pluginsDir).toAbsolutePath().normalize()

    // We're using the Java 9+ JPM system to load the plugins. Each plugin is a module.
    val finder = ModuleFinder.of(pluginsPath)
    val parent = ModuleLayer.boot()
    // `resolveAndBind` is required for service discovery
    val cf = parent.configuration().resolveAndBind(finder, ModuleFinder.of(), emptySet())
    val scl = ClassLoader.getSystemClassLoader()
    // We use ManyLoaders to allow multiple plugins with overlapping packages and resources.
    val layer = parent.defineModulesWithManyLoaders(cf, scl)

    // Load the plugins
    val serviceLoader = ServiceLoader.load(layer, PluginService::class.java)

    // Ktor Setup
    install(IgnoreTrailingSlash)

    // Initialize the plugins
    routing {
        get("/") {
            call.respondText { "App Landing Page" }
        }
        route("/plugins") {
            get("/") {
                call.respondText(contentType = ContentType.Text.Html) {
                    "Plugins:<ul>${
                        serviceLoader.joinToString(separator = "") { s ->
                            "<a href=${call.request.uri.removeSuffix("/")}/${s.getHostname()}/><li>${s.getName()}</li></a>"
                        }
                    }</ul>"
                }
            }
            serviceLoader.forEach { s ->
                s.routes()()
            }
        }
    }
}
