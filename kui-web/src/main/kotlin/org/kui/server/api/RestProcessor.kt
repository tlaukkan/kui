package org.kui.server.api

import java.io.InputStream
import java.io.OutputStream

interface RestProcessor {
    val method: String
    val pathRegex: String
    val groups: List<String>
    val idPlaceHolders: Set<String>
    fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream)
}
