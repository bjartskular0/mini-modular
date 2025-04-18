package dev.akerstrom.plugins

import java.net.URI
import java.net.URL
import kotlin.reflect.KClass

// Finds a resources from the calling class's own JAR file.
// TODO: Find a better way to isolate and get a plugins own resources
inline fun <reified T : ModuleService> KClass<out T>.getPluginResource(name: String): URL? =
    URI("jar:${T::class.java.protectionDomain.codeSource.location.toString()}!${name}").toURL()