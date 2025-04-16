package dev.akerstrom.app

import java.net.URL

class PluginClassLoader(
    val urls: List<URL>
) : ClassLoader() {

}