package dev.akerstrom.app

import dev.akerstrom.plugins.ModuleService
import java.net.URLClassLoader
import java.util.ServiceLoader
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val config = AppConfig.loadConfig()

    val modulesPath = Path(config.modulesDir).toAbsolutePath().normalize()
    val modulePaths = modulesPath.listDirectoryEntries("*.jar")
    val moduleURLs = modulePaths.map { p ->
        p.toUri().toURL()
    }.toTypedArray()

    val loader = URLClassLoader(moduleURLs)
    val serviceLoader = ServiceLoader.load<ModuleService>(ModuleService::class.java, loader)

    serviceLoader.forEach { s ->
        try {
            println("${s.getName()}, ${s.getOrigin()}")
        } catch (e: Exception) {
            println(e)
        }
    }
}
