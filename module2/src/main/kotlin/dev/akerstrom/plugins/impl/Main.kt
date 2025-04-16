package dev.akerstrom.plugins.impl

import dev.akerstrom.baseModule.api.ModuleConfig
import dev.akerstrom.plugins.ModuleService

class Module2 : ModuleService {
    override val config: ModuleConfig
        get() = ModuleConfig.loadConfig(this)

    override fun getName(): String {
        return "I am ${config.name}"
    }

    override fun getOrigin(): String {
        return "I am dynamically loaded from '${this::class.java.protectionDomain.codeSource.location}'"
    }
}
