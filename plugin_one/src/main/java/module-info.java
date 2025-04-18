import dev.akerstrom.plugins.impl.PluginOne;

// LEAVE OPEN! Must be open to allow proper resource finding.
open module dev.akerstrom.plugin_one {
    requires kotlin.stdlib;
    requires kotlinx.serialization.core;
    requires io.ktor.server.core;
    requires dev.akerstrom.plugin_stdlib;

    provides dev.akerstrom.plugins.PluginService with PluginOne;
}