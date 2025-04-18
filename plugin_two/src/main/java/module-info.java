import dev.akerstrom.plugins.impl.PluginTwo;

// LEAVE OPEN! Must be open to allow proper resource finding.
open module dev.akerstrom.plugin_two {
    requires kotlin.stdlib;
    requires kotlinx.serialization.core;
    requires io.ktor.server.core;
    requires dev.akerstrom.plugin_stdlib;

    provides dev.akerstrom.plugins.PluginService with PluginTwo;
}