package org.kui.client.tracker

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang.StringUtils
import org.kui.api.model.LogBatch
import org.kui.api.model.LogLine
import org.kui.KuiException
import org.slf4j.LoggerFactory
import org.kui.util.getProperty
import java.net.*
import java.util.*
import java.net.InetSocketAddress

/**
 * Log storage client.
 */
class LogStorageClient {

    private val log = LoggerFactory.getLogger(LogStorageClient::class.java.name)

    val hostEnvironmentType = getProperty("client", "host.environment.type")
    val hostEnvironment = getProperty("client", "host.environment")
    val hostType = getProperty("client", "host.type")
    val hostName = getProperty("client", "host")

    private val logStorageApiUrl = getProperty("client", "log.storage.api.url")
    private val logStorageUsername = getProperty("client", "log.storage.username")
    private val logStoragePassword = getProperty("client", "log.storage.password")

    private val securityToken: String

    val proxy: Proxy?

    val mapper = ObjectMapper()

    private var cookieManager = java.net.CookieManager()

    init {

        val httpProxyHost = getProperty("proxy", "proxy.host")
        val httpProxyPort = getProperty("proxy", "proxy.port")

        if (!httpProxyHost.isNullOrEmpty() && !httpProxyPort.isNullOrEmpty()) {
            proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(httpProxyHost, Integer.parseInt(httpProxyPort)))
            log.info("HTTP proxy configured: $httpProxyHost:$httpProxyPort")
        } else {
            proxy = null
        }


        val headers = mutableMapOf<String, String>()
        headers.put("Authorization", "Basic ${Base64.getEncoder().encodeToString("$logStorageUsername:$logStoragePassword".toByteArray())}")

        val response = get(url = "$logStorageApiUrl/security/context", headers =  headers)
        if (response.responseCode != 200) {
            throw SecurityException("Login to log storage API failed with status code ${response.responseCode}")
        }

        securityToken = URLEncoder.encode(response.getHeaderFields().get("Security-Token")!![0], "UTF-8")

        val cookiesHeader = response.getHeaderFields().get("Set-Cookie");

        if (cookiesHeader != null) {
            for (cookie in cookiesHeader) {
                cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0))
            }
        }

        //securityToken = URLEncoder.encode(response.headers["Security-Token"]!!, "UTF-8")
    }

    fun insertLines(logPath: String, lines: List<LogLine>) {
        val headers = mutableMapOf<String, String>()
        headers.put("Authorization", "SecurityToken token='$securityToken'")

        val logBatch = LogBatch(hostEnvironment, hostEnvironmentType, hostName, hostType, logPath, lines.toTypedArray())

        val response = post(url = "$logStorageApiUrl/log/batch", headers = headers, data = logBatch, cookies = cookieManager)

        if (response.responseCode != 200) {
            throw KuiException("Failed to send logs. Status code: ${response.responseCode}")
        }
    }

    private fun get(url: String, headers: Map<String, String>) : HttpURLConnection {
        val connection = getConnection(url)
        try {
            connection.setRequestMethod("GET")
            for (header in headers.entries) {
                connection.setRequestProperty(header.key, header.value)
            }
            return connection
        } finally {
            connection.disconnect()
        }
    }

    private fun post(url: String, headers: Map<String, String>, data: Any, cookies: CookieManager) : HttpURLConnection {

        val connection: HttpURLConnection = getConnection(url)
        try {

            for (header in headers.entries) {
                connection.setRequestProperty(header.key, header.value)
            }

            connection.setRequestProperty("Cookie", StringUtils.join(cookies.getCookieStore().getCookies(), ';'))

            connection.requestMethod = "POST"
            connection.setRequestProperty("User-Client", "Mozilla/5.0")
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5")

            // Send post request
            connection.doOutput = true

            mapper.writeValue(connection.outputStream, data)

            connection.outputStream.flush()
            connection.outputStream.close()

            return connection
        } finally {
            connection.disconnect()
        }
    }

    private fun getConnection(url: String): HttpURLConnection {
        val connection: HttpURLConnection
        if (proxy != null) {
            connection = URL(url).openConnection(proxy) as HttpURLConnection
        } else {
            connection = URL(url).openConnection() as HttpURLConnection
        }
        return connection
    }
}