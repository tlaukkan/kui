package org.kui.server.rest

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import io.undertow.util.HttpString
import io.undertow.util.StatusCodes
import org.slf4j.LoggerFactory
import org.kui.security.*
import org.kui.security.model.SecurityContext
import org.kui.security.model.UserRecord
import org.kui.storage.keyValueDao
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Pattern

class RestHandler : HttpHandler {

    private val log = LoggerFactory.getLogger(RestHandler::class.java.name)

    val processors = mutableListOf<RestProcessor>()

    override fun handleRequest(exchange: HttpServerExchange) {
        Thread.currentThread().name = "anonymous"

        try {
            // Authorize session.
            val authorizationHeader = exchange.requestHeaders.getFirst("Authorization")
            if (authorizationHeader != null) {
                val splitIndex = authorizationHeader.indexOf(' ')
                val type = authorizationHeader.substring(0, splitIndex).trim()

                if (type.equals("Basic")) {
                    val credentials = String(Base64.getDecoder().decode(authorizationHeader.substring(splitIndex + 1).trim()), Charset.forName("UTF-8")).split(':')
                    if (credentials.size != 2) {
                        log.warn("Basic authentication failed due to malformed Basic authorization header.")
                        exchange.statusCode = StatusCodes.UNAUTHORIZED
                        return
                    }

                    val username = credentials[0]
                    val password = credentials[1]

                    val userRecord = keyValueDao.get(username, UserRecord::class.java)
                    if (userRecord == null) {
                        log.warn("Basic authentication failed due to user not found: $username")
                        exchange.statusCode = StatusCodes.UNAUTHORIZED
                        return
                    }

                    if (userRecord.passwordLoginFailed != null && (System.currentTimeMillis() - userRecord.passwordLoginFailed!!.time) < 1000) {
                        log.warn("Basic authentication retry throttled: $username")
                        exchange.statusCode = StatusCodes.UNAUTHORIZED
                        return
                    }

                    val passwordHash = crypto.passwordHash(username, password!!)
                    if (!(passwordHash contentEquals userRecord.passwordHash!!)) {
                        log.warn("Basic authentication failed due to password mismatch: $username")
                        userRecord.passwordLoginFailed = Date()
                        keyValueDao.update(userRecord.key!!, userRecord)
                        exchange.statusCode = StatusCodes.UNAUTHORIZED
                        return
                    }

                    val securityToken = contextService.createContext(userRecord.key!!, userManagement.getUserGroups(userRecord.key!!))
                    val securityTokenHash = crypto.securityTokenHash(securityToken)
                    val securityTokenHashString = Base64.getEncoder().encodeToString(securityTokenHash)
                    contextService.setThreadContext(contextService.getContext(securityTokenHashString)!!)
                    exchange.responseHeaders.put(HttpString("Security-Token"), securityToken)

                    log.info("User sign in: $username")
                } else if (type.equals("SecurityToken")) {
                    val credentialMap = mutableMapOf<String, String>()
                    val credentials = authorizationHeader.substring(splitIndex + 1).trim().split(',')
                    for (credential in credentials) {
                        val keyAndValue = credential.split('=')
                        if (keyAndValue.size == 2) {
                            credentialMap.put(keyAndValue[0], URLDecoder.decode(keyAndValue[1].replace("'", ""), "UTF-8"))
                        }
                    }

                    val securityToken = credentialMap["token"]!!
                    val securityTokenHash = crypto.securityTokenHash(securityToken)
                    val securityTokenHashString = Base64.getEncoder().encodeToString(securityTokenHash)
                    val securityContext = contextService.getContext(securityTokenHashString)

                    if (securityContext == null) {
                        log.warn("Security token expired.")
                        exchange.statusCode = StatusCodes.UNAUTHORIZED
                        return
                    }

                    contextService.setThreadContext(contextService.getContext(securityTokenHashString)!!)
                }
            } else {
                contextService.setThreadContext(SecurityContext("anonymous", listOf(GROUP_ANONYMOUS), ByteArray(0), Date()))
            }

            val startTimeMillis = System.currentTimeMillis()
            val requestMethod = exchange.requestMethod.toString()
            val path = exchange.requestPath

            for (processor in processors) {
                if (!processor.method.equals(requestMethod)) {
                    continue
                }

                val matcher = Pattern.compile(processor.pathRegex).matcher(path)
                if (!matcher.matches()) {
                    continue
                }

                var requiredGroupFound = false
                for (requiredGroup in processor.groups) {
                    if (contextService.getThreadContext().groups.contains(requiredGroup)) {
                        requiredGroupFound = true
                        break
                    }
                }
                if (!requiredGroupFound) {
                    log.warn("Context ${contextService.getThreadContext().groups} does not have required groups ${processor.groups} for the API call: ${processor.method} ${processor.pathRegex}")
                    exchange.statusCode = StatusCodes.FORBIDDEN
                    return
                }

                val ids = mutableMapOf<String, String>()
                for (idName in processor.idPlaceHolders) {
                    ids[idName] = matcher.group(idName)
                }

                val parameters = mutableMapOf<String, String>()
                for ((key, value) in exchange.queryParameters) {
                    parameters[key] = value.first
                }

                exchange.statusCode = StatusCodes.OK
                exchange.responseHeaders.put(Headers.CONTENT_TYPE, "application/json")
                try {
                    processor.process(ids, parameters, exchange.inputStream, exchange.outputStream)

                    if (path.equals("/api/log/batch") || path.equals("/api/log/rows")) {
                        //println("API $requestMethod $path processed in ${System.currentTimeMillis() - startTimeMillis} ms.")
                    } else {
                        log.info("$requestMethod $path processed in ${System.currentTimeMillis() - startTimeMillis} ms.")
                    }
                } catch (e: SecurityException) {
                    log.error("$requestMethod $path processing error", e)
                    exchange.statusCode = StatusCodes.FORBIDDEN
                } catch (e: Exception) {
                    log.error("$requestMethod $path processing error", e)
                    throw RuntimeException(e)
                }

                return
            }

            log.warn("No implementation for Rest API $requestMethod $path")
            exchange.statusCode = StatusCodes.NOT_FOUND
        } finally {
            contextService.clearThreadContext()
        }
    }
}