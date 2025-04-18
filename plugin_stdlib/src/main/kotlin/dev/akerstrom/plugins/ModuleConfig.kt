package dev.akerstrom.plugins

import com.akuleshov7.ktoml.Toml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

@Serializable
data class ModuleConfig (
    val name: String,
    val hostname: String,
) {
    companion object {
        inline fun <reified T: PluginService>loadConfig(service: T): ModuleConfig {
            val input = service::class.java.getResource("/server.module.toml")?.readText()
                ?: throw IllegalArgumentException("No such file")
            return Toml.decodeFromString<ModuleConfig>(input)
        }
    }
}
