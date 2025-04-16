package dev.akerstrom.baseModule.api

import com.akuleshov7.ktoml.Toml
import dev.akerstrom.plugins.ModuleService
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlin.reflect.KClass

@Serializable
data class ModuleConfig (
    val name: String
) {
    companion object {
        inline fun <reified T: ModuleService>loadConfig(service: T): ModuleConfig {
            val input = service::class.getPluginResource("/server.module.toml")?.readText()
                ?: throw IllegalArgumentException("No such file")
            return Toml.decodeFromString<ModuleConfig>(input)
        }
    }
}
