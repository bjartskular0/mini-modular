/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.akerstrom.plugins.ktor

import com.sun.nio.file.SensitivityWatchEventModifier
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.http.content.FileSystemPaths.Companion.paths
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import java.io.File
import java.net.URL
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import kotlin.io.path.isDirectory
import kotlin.io.path.pathString

/**
 * Attribute to assign the path of a static file served in the response.  The main use of this attribute is to indicate
 * to subsequent interceptors that a static file was served via the `ApplicationCall.isStaticContent()` extension
 * function.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.StaticFileLocationProperty)
 */
public val StaticFileLocationProperty: AttributeKey<String> = AttributeKey("StaticFileLocation")

private const val pathParameterName = "static-content-path-parameter"

private val staticRootFolderKey = AttributeKey<File>("BaseFolder")

private val StaticContentAutoHead = createRouteScopedPlugin("StaticContentAutoHead") {

    class HeadResponse(val original: OutgoingContent) : OutgoingContent.NoContent() {
        override val status: HttpStatusCode? get() = original.status
        override val contentType: ContentType? get() = original.contentType
        override val contentLength: Long? get() = original.contentLength
        override fun <T : Any> getProperty(key: AttributeKey<T>) = original.getProperty(key)
        override fun <T : Any> setProperty(key: AttributeKey<T>, value: T?) = original.setProperty(key, value)
        override val headers get() = original.headers
    }

    on(ResponseBodyReadyForSend) { call, content ->
        check(call.request.local.method == HttpMethod.Head)
        if (content is OutgoingContent.ReadChannelContent) content.readFrom().cancel(null)
        transformBodyTo(HeadResponse(content))
    }
}

/**
 * A config for serving static content
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.StaticContentConfig)
 */
public class StaticContentConfig<Resource : Any> internal constructor() {

    private val defaultContentType: (Resource) -> ContentType = {
        when (it) {
            is File -> ContentType.defaultForFile(it)
            is URL -> ContentType.defaultForFilePath(it.path)
            is Path -> ContentType.defaultForPath(it)
            else ->
                throw IllegalArgumentException("Argument can be only of type File, Path or URL, but was ${it::class}")
        }
    }
    internal var contentType: (Resource) -> ContentType = defaultContentType
    internal var cacheControl: (Resource) -> List<CacheControl> = { emptyList() }
    internal var modifier: suspend (Resource, ApplicationCall) -> Unit = { _, _ -> }
    internal var exclude: (Resource) -> Boolean = { false }
    internal var extensions: List<String> = emptyList()
    internal var defaultPath: String? = null
    internal var preCompressedFileTypes: List<CompressedFileType> = emptyList()
    internal var autoHeadResponse: Boolean = false
    internal var resourceClass: Class<*>? = null

    /**
     * Enables pre-compressed files or resources.
     *
     * For example, for static files, by setting `preCompressed(CompressedFileType.BROTLI)`, the local file
     * /foo/bar.js.br can be found at "/foo/bar.js"
     *
     * Appropriate headers will be set and compression will be suppressed if pre-compressed file is found.
     *
     * The order in types is *important*.
     * It will determine the priority of serving one versus serving another.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.StaticContentConfig.preCompressed)
     */
    public fun preCompressed(vararg types: CompressedFileType) {
        preCompressedFileTypes = types.toList()
    }

    /**
     * Enables automatic response to a `HEAD` request for every file/resource that has a `GET` defined.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.StaticContentConfig.enableAutoHeadResponse)
     */
    public fun enableAutoHeadResponse() {
        autoHeadResponse = true
    }

    /**
     * Configures default [Resource] to respond with, when requested file is not found.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.StaticContentConfig.default)
     */
    public fun default(path: String?) {
        this.defaultPath = path
    }

    /**
     * Configures [ContentType] for requested static content.
     * If the [block] returns `null`, default behaviour of guessing [ContentType] from the header will be used.
     * For files, [Resource] is a requested [File].
     * For resources, [Resource] is a [URL] to a requested resource.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.StaticContentConfig.contentType)
     */
    public fun contentType(block: (Resource) -> ContentType?) {
        contentType = { resource -> block(resource) ?: defaultContentType(resource) }
    }

    /**
     * Configures [CacheControl] for requested static content.
     * For files, [Resource] is a requested [File].
     * For resources, [Resource] is a [URL] to a requested resource.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.StaticContentConfig.cacheControl)
     */
    public fun cacheControl(block: (Resource) -> List<CacheControl>) {
        cacheControl = block
    }

    /**
     * Configures modification of a call for requested content.
     * Useful to add headers to the response, such as [HttpHeaders.ETag]
     * For files, [Resource] is a requested [File].
     * For resources, [Resource] is a [URL] to a requested resource.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.StaticContentConfig.modify)
     */
    public fun modify(block: suspend (Resource, ApplicationCall) -> Unit) {
        modifier = block
    }

    /**
     * Configures resources that should not be served.
     * If this block returns `true` for [Resource], [Application] will respond with [HttpStatusCode.Forbidden].
     * Can be invoked multiple times.
     * For files, [Resource] is a requested [File].
     * For resources, [Resource] is a [URL] to a requested resource.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.StaticContentConfig.exclude)
     */
    public fun exclude(block: (Resource) -> Boolean) {
        val oldBlock = exclude
        exclude = {
            if (oldBlock(it)) {
                true
            } else {
                block(it)
            }
        }
    }

    /**
     * Configures file extension fallbacks.
     * When set, if a file is not found, the search will repeat with the given extensions added to the file name.
     * The first match will be served.
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.StaticContentConfig.extensions)
     */
    public fun extensions(vararg extensions: String) {
        this.extensions = extensions.toList()
    }
}

/**
 * Sets up [RoutingRoot] to serve resources as static content.
 * All resources inside [basePackage] will be accessible recursively at "[remotePath]/path/to/resource".
 * If requested resource doesn't exist and [index] is not `null`,
 * then response will be [index] resource in the requested package.
 *
 * If requested resource doesn't exist and no [index] specified, response will be 404 Not Found.
 *
 * You can use [block] for additional set up.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.staticResources)
 */
public fun Route.staticResources(
    remotePath: String,
    basePackage: String?,
    index: String? = "index.html",
    block: StaticContentConfig<URL>.() -> Unit = {}
): Route {
    val staticRoute = StaticContentConfig<URL>().apply(block)
    val autoHead = staticRoute.autoHeadResponse
    val compressedTypes = staticRoute.preCompressedFileTypes
    val contentType = staticRoute.contentType
    val cacheControl = staticRoute.cacheControl
    val extensions = staticRoute.extensions
    val modifier = staticRoute.modifier
    val exclude = staticRoute.exclude
    val defaultPath = staticRoute.defaultPath
    val resourceClass = staticRoute.resourceClass
    return staticContentRoute(remotePath, autoHead) {
        respondStaticResource(
            index = index,
            basePackage = basePackage,
            compressedTypes = compressedTypes,
            contentType = contentType,
            cacheControl = cacheControl,
            modifier = modifier,
            exclude = exclude,
            extensions = extensions,
            defaultPath = defaultPath,
            resourceClass = resourceClass,
        )
    }
}

private fun Route.staticContentRoute(
    remotePath: String,
    autoHead: Boolean,
    handler: suspend (ApplicationCall).() -> Unit
) = createChild(object : RouteSelector() {
    override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
        RouteSelectorEvaluation.Success(quality = RouteSelectorEvaluation.qualityTailcard)
}).apply {
    route(remotePath) {
        route("{$pathParameterName...}") {
            get {
                call.handler()
            }
            if (autoHead) {
                method(HttpMethod.Head) {
                    install(StaticContentAutoHead)
                    handle {
                        call.handler()
                    }
                }
            }
        }
    }
}

private suspend fun ApplicationCall.respondStaticFile(
    index: String?,
    dir: File,
    compressedTypes: List<CompressedFileType>?,
    contentType: (File) -> ContentType,
    cacheControl: (File) -> List<CacheControl>,
    modify: suspend (File, ApplicationCall) -> Unit,
    exclude: (File) -> Boolean,
    extensions: List<String>,
    defaultPath: String?
) {
    val relativePath = parameters.getAll(pathParameterName)?.joinToString(File.separator) ?: return
    val requestedFile = dir.combineSafe(relativePath)

    suspend fun checkExclude(file: File): Boolean {
        if (!exclude(file)) return false
        respond(HttpStatusCode.Forbidden)
        return true
    }

    val isDirectory = requestedFile.isDirectory
    if (index != null && isDirectory) {
        respondStaticFile(File(requestedFile, index), compressedTypes, contentType, cacheControl, modify)
    } else if (!isDirectory) {
        if (checkExclude(requestedFile)) return

        respondStaticFile(requestedFile, compressedTypes, contentType, cacheControl, modify)
        if (isHandled) return
        for (extension in extensions) {
            val fileWithExtension = File("${requestedFile.path}.$extension")
            if (checkExclude(fileWithExtension)) return
            respondStaticFile(fileWithExtension, compressedTypes, contentType, cacheControl, modify)
            if (isHandled) return
        }
    }

    if (isHandled) return
    if (defaultPath != null) {
        respondStaticFile(File(dir, defaultPath), compressedTypes, contentType, cacheControl, modify)
    }
}

private suspend fun ApplicationCall.respondStaticPath(
    fileSystem: FileSystemPaths,
    index: String?,
    basePath: String?,
    compressedTypes: List<CompressedFileType>?,
    contentType: (Path) -> ContentType,
    cacheControl: (Path) -> List<CacheControl>,
    modify: suspend (Path, ApplicationCall) -> Unit,
    exclude: (Path) -> Boolean,
    extensions: List<String>,
    defaultPath: String?
) {
    val relativePath = parameters.getAll(pathParameterName)?.joinToString(File.separator) ?: return
    val requestedPath = fileSystem.getPath(basePath.orEmpty()).combineSafe(fileSystem.getPath(relativePath))

    suspend fun checkExclude(path: Path): Boolean {
        if (!exclude(path)) return false
        respond(HttpStatusCode.Forbidden)
        return true
    }

    val isDirectory = requestedPath.isDirectory()
    if (index != null && isDirectory) {
        respondStaticPath(fileSystem, requestedPath.resolve(index), compressedTypes, contentType, cacheControl, modify)
    } else if (!isDirectory) {
        if (checkExclude(requestedPath)) return

        respondStaticPath(fileSystem, requestedPath, compressedTypes, contentType, cacheControl, modify)
        if (isHandled) return
        for (extension in extensions) {
            val pathWithExtension = fileSystem.getPath("${requestedPath.pathString}.$extension")
            if (checkExclude(pathWithExtension)) return
            respondStaticPath(fileSystem, pathWithExtension, compressedTypes, contentType, cacheControl, modify)
            if (isHandled) return
        }
    }

    if (isHandled) return
    if (defaultPath != null) {
        respondStaticPath(
            fileSystem,
            fileSystem.getPath(basePath ?: "", defaultPath),
            compressedTypes,
            contentType,
            cacheControl,
            modify
        )
    }
}

private suspend fun ApplicationCall.respondStaticResource(
    index: String?,
    basePackage: String?,
    compressedTypes: List<CompressedFileType>?,
    contentType: (URL) -> ContentType,
    cacheControl: (URL) -> List<CacheControl>,
    modifier: suspend (URL, ApplicationCall) -> Unit,
    exclude: (URL) -> Boolean,
    extensions: List<String>,
    defaultPath: String?,
    resourceClass: Class<*>?,
) {
    val relativePath = parameters.getAll(pathParameterName)?.joinToString(File.separator) ?: return

    respondStaticResource(
        requestedResource = relativePath,
        packageName = basePackage,
        compressedTypes = compressedTypes,
        contentType = contentType,
        cacheControl = cacheControl,
        modifier = modifier,
        exclude = exclude,
        resourceClass = resourceClass
    )

    if (isHandled) return
    for (extension in extensions) {
        respondStaticResource(
            requestedResource = "$relativePath.$extension",
            packageName = basePackage,
            compressedTypes = compressedTypes,
            contentType = contentType,
            cacheControl = cacheControl,
            modifier = modifier,
            exclude = exclude,
            resourceClass = resourceClass
        )
        if (isHandled) return
    }

    if (index != null) {
        respondStaticResource(
            requestedResource = "$relativePath${File.separator}$index",
            packageName = basePackage,
            compressedTypes = compressedTypes,
            contentType = contentType,
            cacheControl = cacheControl,
            modifier = modifier,
            resourceClass = resourceClass
        )
    }
    if (isHandled || defaultPath == null) return

    respondStaticResource(
        requestedResource = defaultPath,
        packageName = basePackage,
        compressedTypes = compressedTypes,
        contentType = contentType,
        cacheControl = cacheControl,
        modifier = modifier,
        resourceClass = resourceClass
    )
}

/**
 * Wrapper on [FileSystem] for more specific delegation since we use only [getPath] method from it.
 *
 * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.FileSystemPaths)
 */
public interface FileSystemPaths {
    public companion object {
        /**
         * Creates a [FileSystemPaths] instance from a [FileSystem].
         *
         * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.FileSystemPaths.Companion.paths)
         */
        public fun FileSystem.paths(): FileSystemPaths = object : FileSystemPaths {
            override fun getPath(first: String, vararg more: String): Path = this@paths.getPath(first, *more)
        }
    }

    /**
     * Converts a path string, or a sequence of strings that when joined form a path string, to a Path.
     * Equal to [FileSystem.getPath].
     *
     * [Report a problem](https://ktor.io/feedback/?fqname=io.ktor.server.http.content.FileSystemPaths.getPath)
     */
    public fun getPath(first: String, vararg more: String): Path
}