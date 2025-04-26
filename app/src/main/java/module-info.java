module dev.akerstrom.app {
    requires kotlin.stdlib;
    requires kotlin.reflect;
    requires kotlinx.io.core;
    requires kotlinx.coroutines.core;
    requires kotlinx.serialization.core;
    requires kotlinx.datetime;

    requires io.ktor.server.core;
    requires io.ktor.server.cio;
    requires io.ktor.http;
    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;

//    requires jdk.unsupported;

    requires dev.akerstrom.plugin_stdlib;
    requires io.ktor.utils;

    uses dev.akerstrom.plugins.PluginService;

    exports dev.akerstrom.app to kotlin.reflect;
}