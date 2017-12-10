package org.kui.server.api.users

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.slf4j.LoggerFactory
import org.kui.security.GROUP_ANONYMOUS
import org.kui.security.GROUP_USER
import org.kui.server.api.StreamRestProcessor
import org.kui.util.getProperty
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream

class GetTemplates : StreamRestProcessor("/api/templates", "GET", listOf(GROUP_USER, GROUP_ANONYMOUS)) {

    private val log = LoggerFactory.getLogger(GetTemplates::class.java.name)

    override fun process(ids: Map<String, String>, parameters: Map<String, String>, inputStream: InputStream, outputStream: OutputStream) {
        val paths = parameters["paths"]!!.split(',')

        log.info("Loading templates $paths")

        val templates = mutableListOf<String>()
        val allowedCharactersRegEx = Regex("^[a-zA-Z0-9/.]+$")
        for (path in paths) {
            if (path.length == 0) {
                log.warn("Empty template path.")
                continue
            }
            if (StringUtils.countMatches(path, "") > 1) {
                log.warn("Template path contained . character more than once: $path")
                continue
            }
            if (!path.matches(allowedCharactersRegEx)) {
                log.warn("Template path contains prohibited characters: $path")
                continue
            }
            val inputStream: InputStream?
            if ("production".equals(getProperty("web","web.mode"))) {
                inputStream = GetTemplates::class.java.classLoader.getResourceAsStream(path)
            } else {
                val localPath = "ui/src/main/kotlin/" + path
                if (File(localPath).exists()) {
                    inputStream = FileInputStream("ui/src/main/kotlin/" + path)
                } else {
                    inputStream = null
                }
            }
            if (inputStream == null) {
                log.warn("Template file not found at path: $path")
                continue
            }
            templates.add(path)
            templates.add(IOUtils.toString(inputStream))
        }

        val mapper = ObjectMapper()
        mapper.writeValue(outputStream, templates)
    }
}
