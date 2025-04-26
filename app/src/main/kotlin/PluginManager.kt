package dev.akerstrom.app

import dev.akerstrom.plugins.PluginService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.collections.immutable.toImmutableList
import java.lang.ref.WeakReference
import java.net.URI
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.reflect.KClass
import kotlin.reflect.cast

private val logger = KotlinLogging.logger {}
class PluginManager<T : PluginService, R>(
    private val pluginClass: KClass<T>,
    private val pluginsDir: Path,
    private val parent: ClassLoader
) : Iterable<PluginProvider<T, URLClassLoader>>
where R : ClassLoader,
      R : AutoCloseable {
    private val pluginsMap: MutableMap<String, PluginProvider<T, URLClassLoader>> = mutableMapOf()

    companion object {
        inline fun <reified T : PluginService> create(pluginsDir: Path) = PluginManager<T, URLClassLoader>(T::class, pluginsDir, Thread.currentThread().contextClassLoader)
        inline fun <reified T : PluginService> create(pluginsDir: Path, parent: ClassLoader) = PluginManager<T, URLClassLoader>(T::class, pluginsDir, parent)
    }

    init {
        reloadPlugins()
    }

    fun reloadPlugins() {
        for (entry in pluginsDir.listDirectoryEntries("*.jar")) {
            if (!hasPlugin(entry)) {
                addPlugin(entry)
            }
        }

    }

    fun addPlugin(plugin: Path) {
        if (hasPlugin(plugin)) {
            logger.debug { "Already has plugin: $plugin" }
            return
        }

        URLClassLoader(arrayOf(plugin.toJarURL())).use { ucl ->
            val pluginImplClass = ucl.loadClass("dev.akerstrom.plugins.impl.PluginImpl")

            if (pluginClass.java.isAssignableFrom(pluginImplClass)) {
                val p = pluginClass.cast(pluginImplClass.getDeclaredConstructor().newInstance())

                val name = plugin.toPluginName()
                logger.debug { "Found plugin: $name" }

    //            pluginsMap[name] = PluginProvider(pluginClass, name, WeakReference(ucl), p)
            }
        }
    }

    private fun hasPlugin(plugin: Path): Boolean {
        return pluginsMap.contains(plugin.toPluginName())
    }

    fun removePlugin(pluginName: String) {
        val plugin = pluginsMap[pluginName]
        if (plugin == null) {
            logger.debug { "Tried to remove Plugin: $pluginName but it did not exist" }
            return
        }

        plugin.shutdownHooks.forEach { it() }
        plugin.instance.shutdown()
        plugin.classLoader.get()?.close()
        pluginsMap.remove(pluginName)
        logger.info { "Removed Plugin: $pluginName" }
    }

    override fun iterator(): Iterator<PluginProvider<T, URLClassLoader>> = pluginsMap.values.toImmutableList().iterator()
    fun plugins() = pluginsMap.values.toImmutableList()

    fun Path.toJarURL(): URL = this.toUri().toURL()
    fun Path.toPluginName() = this.fileName.toString()
}

data class PluginProvider<T, R> (
    internal val pluginClass: KClass<T>,
    val name: String,
    val classLoader: WeakReference<R>,
    val instance: T,
    val shutdownHooks: MutableList<() -> Boolean> = mutableListOf()
) where T : PluginService,
        R : ClassLoader,
        R : AutoCloseable