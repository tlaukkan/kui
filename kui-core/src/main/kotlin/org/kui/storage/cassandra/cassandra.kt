package org.kui.storage.cassandra

import org.kui.util.getProperty
import java.util.*

fun getCassandraCredentials(): CassandraCredentials {
    val storageHost = getProperty("storage", "cassandra.host")
    val storagePort = getProperty("storage", "cassandra.port").toInt()
    val storageUsername = getProperty("storage", "cassandra.username")
    val storagePassword = getProperty("storage", "cassandra.password")
    val keyStorePath = getProperty("storage", "cassandra.keystore.path")
    val keyStorePassword = getProperty("storage", "cassandra.keystore.password")
    return CassandraCredentials(storageHost, storagePort, storageUsername, storagePassword, keyStorePath, keyStorePassword)
}

private val timeUuidGeneratorForComparison = TimeUuidGenerator("", false)
fun generateTimedUuidForComparison(timeSeconds: Long) : UUID {
    return timeUuidGeneratorForComparison.generate(timeSeconds)
}