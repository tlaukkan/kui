package org.kui.security

import org.slf4j.LoggerFactory
import org.kui.security.model.SecurityContext
import java.util.*

/**
 * SessionContext service.
 */
class ContextService {
    private val log = LoggerFactory.getLogger(ContextService::class.java.name)

    /**
     * setThreadContext contexts contains context as long as thread is executing that context.
     */
    private val threadContexts = HashMap<Thread, SecurityContext>()

    /**
     * Contexts contains contexts as long as they are valid.
     */
    private val contexts = HashMap<String, SecurityContext>()

    /**
     * Creates security context and security token.
     * @return the security token
     */
    @Synchronized fun createContext(user: String, groups: List<String>): String {
        val securityToken = crypto.createSecurityToken()
        val securityTokenHash = crypto.securityTokenHash(securityToken)
        val securityTokenHashString = Base64.getEncoder().encodeToString(securityTokenHash)
        val securityContext = SecurityContext(user, groups, securityTokenHash, Date())
        contexts.put(securityTokenHashString, securityContext)
        return securityToken
    }

    /**
     * Gets security context.
     * @param securityTokenHash the security token
     * @return the security context
     */
    @Synchronized fun getContext(securityTokenHash: String) : SecurityContext? {
        val tokenHashes = contexts.keys.toList()

        // Destroy expired security contexts.
        for (tokenHash in tokenHashes) {
            if (System.currentTimeMillis() - contexts[tokenHash]!!.lastAccess.time > 15 * 60 * 1000) {
                log.info("Destroyed expired security context: " + contexts[tokenHash]!!.user)
                destroyContext(tokenHash)
            }
        }

        return contexts[securityTokenHash]
    }

    /**
     * Destroys security context
     * @param securityTokenHash the security token
     */
    @Synchronized fun destroyContext(securityTokenHash: String) {
        contexts.remove(securityTokenHash)
    }

    /**
     * Sets security context to thread.
     * @param context the context
     *
     * @return
     */
    @Synchronized fun setThreadContext(context: SecurityContext): Unit {
        if (threadContexts.containsKey(Thread.currentThread())) {
            throw SecurityException("Security context already exists for thread.")
        }
        Thread.currentThread().name = context.user
        threadContexts.put(Thread.currentThread(), context)
        context.lastAccess = Date()
    }

    /**
     * Gets thread security context.
     * @return the context
     */
    @Synchronized fun getThreadContext() : SecurityContext {
        if (!threadContexts.containsKey(Thread.currentThread())) {
            throw SecurityException("Security context does not exist for thread.")
        }
        return threadContexts[Thread.currentThread()]!!
    }

    /**
     * Removes security context from thread.
     */
    @Synchronized fun clearThreadContext() {
        if (threadContexts.containsKey(Thread.currentThread())) {
            threadContexts.remove(Thread.currentThread())
        }
        Thread.currentThread().name = ""
    }
}
