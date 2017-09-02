package org.kui.storage

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.NettySSLOptions
import com.datastax.driver.core.Session
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslProvider
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import javax.net.ssl.TrustManagerFactory

open class CassandraTable(credentials: CassandraCredentials, keyspace: String) {
    protected val cluster: Cluster
    protected val session: Session

    init {
        val builder = Cluster.builder()
                .withSSL(getSSLOptions(credentials))
                .addContactPoint(credentials.cassandraHost)
                .withPort(credentials.cassandraPort)
                .withCredentials(credentials.username, credentials.password)

        cluster = builder.build()
        session = cluster.connect()

        session.execute("CREATE KEYSPACE IF NOT EXISTS $keyspace WITH replication = {'class':'SimpleStrategy','replication_factor':3};")
        session.execute("USE $keyspace;")

    }

    fun close() {
        session.close()
        cluster.close()
    }

    protected fun getSSLOptions(cassandraCredentials: CassandraCredentials): NettySSLOptions {

        val ks = KeyStore.getInstance("JKS")
        var trustStore = this.javaClass.classLoader.getResourceAsStream(cassandraCredentials.keyStorePath)
        if (trustStore == null) {
            trustStore = FileInputStream(File(cassandraCredentials.keyStorePath))
        }

        ks.load(trustStore, cassandraCredentials.keyStorePassword.toCharArray())
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(ks)

        val builder = SslContextBuilder
                .forClient()
                .sslProvider(SslProvider.JDK)
                .trustManager(tmf)
        val sslOptions = NettySSLOptions(builder.build())
        return sslOptions
    }
}