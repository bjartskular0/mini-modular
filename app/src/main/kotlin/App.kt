package dev.akerstrom.app

import dev.akerstrom.plugins.PluginService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.lang.module.Configuration
import java.lang.module.ModuleFinder
import java.net.URLClassLoader
import java.nio.file.InvalidPathException
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.reflect.cast

object App {
    @JvmStatic
//    fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

    fun main(args: Array<String>): Unit {

        Thread.sleep(1000)

        // Read config
        val config = AppConfig.loadConfig()

        // Get a path to the plugins directory
        val pluginsPath = Path(config.pluginsDir).toAbsolutePath().normalize()
        require(pluginsPath.isDirectory()) { "config.pluginsDir must be a directory. Value: ${config.pluginsDir}" }

//        val pluginManager = PluginManager.create<PluginService>(pluginsPath, ClassLoader.getSystemClassLoader())
//        println(pluginManager.plugins())
//        pluginManager.removePlugin("plugin_one.jar")

        for (file in pluginsPath.listDirectoryEntries("*.jar")) {
            println(file)
            val url = file.toUri().toURL()
            println(url)

            var pluginImplClass: Class<*>? = null
            run {
                val ucl = URLClassLoader(arrayOf(url))
                pluginImplClass = ucl.loadClass("dev.akerstrom.plugins.impl.PluginImpl")
                println(pluginImplClass)
//                if (PluginService::class.java.isAssignableFrom(pluginImplClass)) {
//                    val p = PluginService::class.cast(pluginImplClass.getDeclaredConstructor().newInstance())
//                }
                ucl.close()

                null
            }

            println(pluginImplClass)

            System.gc()
            Thread.sleep(2000)

            pluginImplClass?.newInstance()

            val f = File(file.toString())
            if (f.delete() && !f.exists()) {
                println("Delete Succeeded")
            } else {
                println("Delete Failed")
            }
        }

        while(true) {}
    }
}

fun Application.module() {
    // Read config
    val config = AppConfig.loadConfig()

    // Get a path to the plugins directory
    val pluginsPath = Path(config.pluginsDir).toAbsolutePath().normalize()
    require(pluginsPath.isDirectory()) { "config.pluginsDir must be a directory. Value: ${config.pluginsDir}" }

    val pluginManager = PluginManager.create<PluginService>(pluginsPath, ClassLoader.getSystemClassLoader())

    println(pluginManager.plugins())

    pluginManager.removePlugin("plugin_one.jar")
}

fun Application.module2() {
    // Read config
    val config = AppConfig.loadConfig()

    // Get a path to the plugins directory
    val pluginsPath = Path(config.pluginsDir).toAbsolutePath().normalize()

    // Get a list of plugins in the directory
    if (!pluginsPath.isDirectory()) throw InvalidPathException(pluginsPath.toString(), "Not a directory")
    val pluginsPathList = pluginsPath.listDirectoryEntries("*.jar")

    // We're using the Java 9+ JPM system to load the plugins. Each plugin is a module.
    // Get a list of finders, one per module
    val finders = pluginsPathList.map { ModuleFinder.of(it) }.toMutableList()
    val parent = ModuleLayer.boot()

    val configurations = finders.map { parent.configuration().resolveAndBind(it, ModuleFinder.of(), emptySet()) }.toMutableList()
    println(configurations)

    val layers = configurations.map { parent.defineModulesWithOneLoader(it, ClassLoader.getSystemClassLoader()) }.toMutableList()
    var myCf = Configuration.resolveAndBind(ModuleFinder.of(), configurations, ModuleFinder.of(), emptySet())
    var myLayer = ModuleLayer.defineModulesWithOneLoader(myCf, layers, ClassLoader.getSystemClassLoader()).layer()

    println(myCf.parents())
    println(myLayer.parents())



    // Load the plugins
    val loadedServices = ServiceLoader.load(myLayer, PluginService::class.java).toMutableList()

    // Ktor Setup
    install(IgnoreTrailingSlash)

    // Initialize the plugins
    val pluginRoutes = mutableMapOf<String, RoutingNode>()
    var pluginsRoute: Route? = null

    fun removeModule(plugin: String): Boolean {
        var service = loadedServices.firstOrNull { it.getHostname() == plugin }
        if (service == null) return false

        // Remove service routes
        val route = pluginsRoute as RoutingNode
        pluginRoutes[service.getHostname()]?.let { route.removeRoute(it.selector) }

        // Remove containing configuration and module layer
        var module = service::class.java.module
        println(finders)
        println(configurations)
        println(layers)
        println(finders.removeIf { it.find(module.name).isPresent })
        println(configurations.removeIf { it.findModule(module.name).isPresent })
        println(layers.removeIf { it.findModule(module.name).isPresent })
        println(finders)
        println(configurations)
        println(layers)

        // Reconstruct final configuration and module layer
        myCf = Configuration.resolveAndBind(ModuleFinder.of(), configurations, ModuleFinder.of(), emptySet())
        myLayer = ModuleLayer.defineModulesWithOneLoader(myCf, layers, ClassLoader.getSystemClassLoader()).layer()
        println(myCf.parents())
        println(myLayer.parents())

        // Remove service from service list and create a new service loader
        loadedServices.remove(service)

        // Set any remaining references to null
        module = null
        service = null

        // Force garbage dump
        System.gc()

//        println("Removing: ${serviceLoader.filter { it::class.java.module == service::class.java.module }}")
//        serviceLoader.removeAll { it::class.java.module == service::class.java.module }
        return true
    }

    routing {
        get("/") {
            call.respondText { "App Landing Page" }
        }
        pluginsRoute = route("/plugins") {
//            get("/") {
//                call.respondText(contentType = ContentType.Text.Html) {
//                    "Plugins:<ul>${
//                        loadedServices.joinToString(separator = "") { s ->
//                            "<a href=${call.request.uri.removeSuffix("/")}/${s.getHostname()}/><li>${s.getName()}</li></a>"
//                        }
//                    }</ul>"
//                }
//            }
//            loadedServices.forEach { s ->
//                pluginRoutes[s.getHostname()] = with(s) { pluginRoute() } as RoutingNode
//            }
        }

        get("/rm/{plugin}") {
            call.parameters["plugin"]?.let {
                if (removeModule(it)) call.response.status(HttpStatusCode.OK)
                else call.response.status(HttpStatusCode.InternalServerError)
            }
                ?: call.response.status(HttpStatusCode.BadRequest)
        }
    }



}
