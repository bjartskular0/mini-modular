package dev.akerstrom.plugins

import io.ktor.server.routing.*


interface ModuleService {
    val config: ModuleConfig

    val hostname: String

    fun getName(): String {
        return config.name
    }

    fun getOrigin(): String

    fun routes(): Route.() -> Unit

}

abstract class PluginService {
    private val config: ModuleConfig = ModuleConfig.loadConfig(this)

    fun getConfig(): ModuleConfig = config
    fun getName() = config.name
    fun getHostname() = config.hostname
    fun getOrigin(): String = this::class.java.protectionDomain.codeSource.location.toString()

    abstract fun routes(): Route.() -> Unit

}