package org.kui.server

import io.undertow.Handlers
import io.undertow.Undertow
import io.undertow.server.handlers.BlockingHandler
import io.undertow.server.handlers.resource.ClassPathResourceManager
import io.undertow.server.handlers.resource.FileResourceManager
import io.undertow.server.handlers.resource.ResourceHandler
import org.apache.log4j.xml.DOMConfigurator
import org.slf4j.LoggerFactory
import org.kui.agent.Monitor
import org.kui.security.*
import org.kui.security.model.SecurityContext
import org.kui.server.api.logs.GetLogRows
import org.kui.server.api.safe.*
import org.kui.server.api.users.*
import org.kui.server.api.users.login.*
import org.kui.server.rest.RestHandler
import org.kui.server.work.CoordinationWorker
import org.kui.server.workers.ActivityAlertWorker
import org.kui.storage.cassandra.CassandraKeyValueTable
import org.kui.util.getProperty
import org.kui.util.getZoneOffsetMillis
import org.kui.util.setProperty
import java.io.File
import java.io.FileInputStream
import java.net.InetAddress
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyStore
import java.util.*
import javax.net.ssl.*

private val log = LoggerFactory.getLogger("org.whiteicesecurity.server.main")


fun main(args : Array<String>) {

    val server = configureServer()

    log.info("White Ice Server (${InetAddress.getLocalHost().getHostName()} started.")

    log.info("Time Zone offset: " + getZoneOffsetMillis())

    contextService.setThreadContext(SecurityContext(USER_SYSTEM_USER, listOf(GROUP_SYSTEM, GROUP_ADMIN), ByteArray(0), Date()))

    val activityAlertWorker = ActivityAlertWorker().start()

    val coordinationWorker = CoordinationWorker().start()

    val monitor = Monitor().start()

}

fun configureServer(): Undertow {
    DOMConfigurator.configure("log4j.xml") // Initialize logging

    if (System.getenv("HOSTNAME") != null) {
        println("Set work host name according to environment variables: ${System.getenv("HOSTNAME")}")
        setProperty("work", "host", System.getenv("HOSTNAME"))
    }

    userManagement.configure()

    val sslContext = createSSLContext(
            loadKeyStore(getProperty("web", "web.keystore.path"), getProperty("web", "web.keystore.password")),
            getProperty("web", "web.keystore.password"),
            loadDefaultTrustStore())

    val restHandler = RestHandler()

    restHandler.processors.add(GetTemplates())
    restHandler.processors.add(GetSecurityContext())
    restHandler.processors.add(PutPasswordChange())
    restHandler.processors.add(DeleteSecurityContext())
    restHandler.processors.add(GetUsers())
    restHandler.processors.add(PostUser())
    restHandler.processors.add(PutUser())
    restHandler.processors.add(DeleteUser())
    restHandler.processors.add(GetUser())
    restHandler.processors.add(GetHosts())
    restHandler.processors.add(GetLogs())
    restHandler.processors.add(GetLogRows())
    restHandler.processors.add(PostLogBatch())

    restHandler.processors.add(DeleteRecord())
    restHandler.processors.add(GetRecords())
    restHandler.processors.add(GetRecord())
    restHandler.processors.add(PostRecord())
    restHandler.processors.add(PutRecord())

    val server : Undertow
    if ("production".equals(getProperty("web", "web.mode"))) {
        log.info("Production mode, loading resources from classpath.")
        server = Undertow.builder()
                .addHttpsListener(
                        getProperty("web", "web.listen.port").toInt(),
                        getProperty("web", "web.listen.address").trim(),
                        sslContext)
                .setHandler(Handlers.path()
                        .addExactPath("/", Handlers.redirect("/ui/index.html#log"))
                        .addPrefixPath("/ui", ResourceHandler(ClassPathResourceManager(CassandraKeyValueTable::class.java.classLoader, "")))
                        .addPrefixPath("/ui/static", ResourceHandler(ClassPathResourceManager(CassandraKeyValueTable::class.java.classLoader, "")))
                        .addPrefixPath("/ui/dynamic", ResourceHandler(ClassPathResourceManager(CassandraKeyValueTable::class.java.classLoader, "")))
                        .addPrefixPath("/api", BlockingHandler(restHandler))
                )
                .build()
        server.start()
    } else {
        log.info("Development mode, loading resources from project folders.")
        server = Undertow.builder()
                .addHttpsListener(
                        getProperty("web", "web.listen.port").toInt(),
                        getProperty("web", "web.listen.address").trim(),
                        sslContext)
                .setHandler(Handlers.path()
                        .addExactPath("/", Handlers.redirect("/ui/index.html#log"))
                        //.addPrefixPath("/ui", ResourceHandler(ClassPathResourceManager(KeyValueTable::class.java.classLoader, "")))
                        .addPrefixPath("/ui", ResourceHandler(FileResourceManager(File("ui/src/main/kotlin"))))
                        .addPrefixPath("/ui/static", ResourceHandler(FileResourceManager(File("ui/src/main/resources"))))
                        .addPrefixPath("/ui/dynamic", ResourceHandler(FileResourceManager(File("ui/out/production/classes"))))
                        .addPrefixPath("/api", BlockingHandler(restHandler))
                )
                .build()
        server.start()
    }

    return server
}

private fun createSSLContext(keyStore: KeyStore, keyPassword: String, trustStore: KeyStore): SSLContext {
    val keyManagers: Array<KeyManager>
    val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
    keyManagerFactory.init(keyStore, keyPassword.toCharArray())
    keyManagers = keyManagerFactory.getKeyManagers()

    val trustManagers: Array<TrustManager>
    val trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
    trustManagerFactory.init(trustStore)
    trustManagers = trustManagerFactory.getTrustManagers()

    val sslContext: SSLContext = SSLContext.getInstance("TLS")
    sslContext.init(keyManagers, trustManagers, null)
    return sslContext
}

private fun loadKeyStore(keyStorePath: String, keyStorePassword: String): KeyStore {
    var stream = CassandraKeyValueTable::class.java.classLoader.getResourceAsStream(keyStorePath)
    if (stream == null) {
        stream = Files.newInputStream(Paths.get(keyStorePath)) ?: throw RuntimeException("Could not load keystore")
    }
    stream.use { inputStream ->
        val loadedKeystore = KeyStore.getInstance("JKS")
        loadedKeystore.load(inputStream, keyStorePassword.toCharArray())
        return loadedKeystore
    }
}

private fun loadDefaultTrustStore(): KeyStore {
    val keyStore = KeyStore.getInstance("JKS")

    keyStore.load(FileInputStream(
            System.getProperties()
                    .getProperty("java.home") + File.separator
                    + "lib" + File.separator + "security" + File.separator
                    + "cacerts"), "changeit".toCharArray())

    return keyStore
}