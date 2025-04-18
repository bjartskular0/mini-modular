/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.akerstrom.plugins.ktor

import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import java.io.*

/**
 * Serves a single-page application.
 * You can learn more from [Serving single-page applications](https://ktor.io/docs/serving-spa.html).
 *
 * A basic configuration for the application served from the `filesPath` folder
 * with `index.html` as a default file:
 *
 * ```
 * application {
 *     routing {
 *        singlePageApplication {
 *           filesPath = "application/project_path"
 *         }
 *     }
 * }
 * ```
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.singlePageApplication)
 */
public fun Route.singlePageApplication(configBuilder: SPAConfig.() -> Unit = {}) {
    val config = SPAConfig()
    configBuilder.invoke(config)

    staticResources(config.applicationRoute, config.filesPath, index = config.defaultPage) {
        resourceClass = config.resourceClass

        default(config.defaultPage)
        config.ignoredFiles.forEach { ignoreConfig ->
            exclude { url ->
                ignoreConfig(url.path)
            }
        }
    }
}

/**
 * Configuration for the [Route.singlePageApplication] plugin.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.SPAConfig)
 */
public class SPAConfig(
    /**
     * The default name of a file or resource to serve when path inside [applicationRoute] is requested
     */
    public var defaultPage: String = "index.html",

    /**
     * The URL path under which the content should be served
     */
    public var applicationRoute: String = "/",

    /**
     * The path under which the static content is located.
     * Corresponds to a resource path.
     */
    public var filesPath: String = "",

    /**
     * The class to call getResource on. Defaults to the application classLoader if null.
     */
    public var resourceClass: Class<*>? = null,

    /**
     * A list of callbacks checking if a file or resource in [filesPath] is ignored.
     * Requests for such files or resources fail with the 403 Forbidden status code
     */
    internal val ignoredFiles: MutableList<(String) -> Boolean> = mutableListOf()
)

/**
 * Registers a [block] in [ignoredFiles]
 * [block] returns true if [path] should be ignored.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.ignoreFiles)
 */
public fun SPAConfig.ignoreFiles(block: (path: String) -> Boolean) {
    ignoredFiles += block
}

/**
 * Creates an application configuration for the Angular project.
 * Resources will be shared from the filesPath directory. The root file is index.html
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.angular)
 */
public fun SPAConfig.angular(filesPath: String) {
    this.filesPath = filesPath
}

/**
 * Creates an application configuration for the React project.
 * Resources will be shared from the filesPath directory. The root file is index.html
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.react)
 */
public fun SPAConfig.react(filesPath: String) {
    this.filesPath = filesPath
}

/**
 * Creates an application configuration for the Vue project.
 * Resources will be shared from the filesPath directory. The root file is index.html
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.vue)
 */
public fun SPAConfig.vue(filesPath: String) {
    this.filesPath = filesPath
}

/**
 * Creates an application configuration for the Ember project.
 * Resources will be shared from the filesPath directory. The root file is index.html
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.ember)
 */
public fun SPAConfig.ember(filesPath: String) {
    this.filesPath = filesPath
}

/**
 * Creates an application configuration for the Backbone project.
 * Resources will be shared from the filesPath directory. The root file is index.html
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.backbone)
 */
public fun SPAConfig.backbone(filesPath: String) {
    this.filesPath = filesPath
}