package dev.akerstrom.plugins

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.response.*
import io.ktor.server.routing.*

val logger = KotlinLogging.logger {}
abstract class PluginService {
    private val config: ModuleConfig = ModuleConfig.loadConfig(this)

    fun getConfig(): ModuleConfig = config
    fun getName() = config.name
    fun getHostname() = config.hostname
    fun getOrigin(): String = this::class.java.protectionDomain.codeSource.location.toString()

    abstract fun Route.routes()
    abstract fun shutdown()

    init {
        logger.debug { "Instantiated ${getName()}" }
    }

    fun Route.pluginRoute(): Route = route("/${getHostname()}") {
        get {
            call.respondText("I exist.")
        }
        routes()
    }

}