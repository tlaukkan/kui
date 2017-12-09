package org.kui.server

import java.io.InputStream
import java.io.OutputStream
import java.util.regex.Pattern

abstract class StreamRestProcessor : RestProcessor {

    private val path: String

    override val groups: List<String>
    override val method: String

    override var idPlaceHolders: Set<String> = mutableSetOf<String>()
    override var pathRegex: String

    constructor(path: String, method: String, groups: List<String>) {
        this.path = path
        this.method = method
        this.groups = groups
        idPlaceHolders = getIdGroupsFromPathRegex(path)

        pathRegex = path
        for (id in idPlaceHolders) {
            pathRegex = pathRegex.replace("<$id>", "(?<$id>[a-z0-9.%]*)")
        }
    }

    abstract override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream)

    protected fun getIdGroupsFromPathRegex(path: String) : Set<String> {
        val idGroups = mutableSetOf<String>()

        val m = Pattern.compile("<([a-zA-Z][a-zA-Z0-9]*)>").matcher(path)

        while (m.find()) {
            idGroups.add(m.group(1))
        }

        return idGroups
    }
}