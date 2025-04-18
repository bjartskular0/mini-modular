package dev.akerstrom.app

import com.akuleshov7.ktoml.Toml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

@Serializable
data class AppConfig(
    val pluginsDir: String
) {
    companion object {
        fun loadConfig(): AppConfig {
            val input = AppConfig::class.java.getResource("/config.toml")?.readText()
                ?: throw IllegalArgumentException("No such file")
            return Toml.decodeFromString<AppConfig>(input)
        }
    }
}
