package dev.akerstrom.plugins

import dev.akerstrom.baseModule.api.ModuleConfig


interface ModuleService {
    val config: ModuleConfig

    fun getName(): String {
        return config.name
    }

    fun getOrigin(): String
}