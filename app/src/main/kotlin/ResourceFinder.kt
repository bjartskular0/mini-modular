/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.akerstrom.app

import java.io.File
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL

/**
 * @author David Blevins
 * @version $Rev$ $Date$
 */
class ResourceFinder(
    private var path: String,
    private val classLoader: ClassLoader,
    vararg urls: URL
) {
    val urls: Array<URL>?

    init {
        if (path.isNotEmpty() && !path.endsWith("/")) {
            path += "/"
        }

        if (urls.isEmpty()) {
            this.urls = null
        } else {
            this.urls = arrayOf(*urls)

            for ((i, url) in this.urls.withIndex()) {
                if ("jar" == url.protocol || isDirectory(url)) {
                    continue
                }
                try {
                    this.urls[i] = URI("jar:$url!/").toURL()
                } catch (_: URISyntaxException) {}
                catch (_: MalformedURLException) {}
            }
        }

    }

    constructor(vararg urls: URL) : this("", Thread.currentThread().contextClassLoader, *urls)
    constructor(path: String) : this(path, Thread.currentThread().contextClassLoader)


    companion object {
        private fun isDirectory(url: URL): Boolean {
            val file = url.file
            return (file.isNotEmpty() && file.get(file.length - 1) == '/') || File(file).isDirectory
        }
    }
}