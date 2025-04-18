module dev.akerstrom.plugin_stdlib {
    requires transitive kotlin.stdlib;
    requires transitive kotlinx.serialization.core;
    requires transitive io.ktor.server.core;

    requires ktoml.core.jvm;

    exports dev.akerstrom.plugins;
}