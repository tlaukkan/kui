package org.kui.storage

import org.kui.util.getProperty
import java.util.*

val keyValueDao = KeyValueDao("whiteice3", "key_value")

private val timeUuidGeneratorForComparison = TimeUuidGenerator("", false)

fun generateTimedUuidForComparison(timeSeconds: Long) : UUID {
    return timeUuidGeneratorForComparison.generate(timeSeconds)
}

fun getStorageCredentials(propertyCategoryKey: String): CassandraCredentials {
    val storageHost = getProperty(propertyCategoryKey, "storage.host")
    val storagePort = getProperty(propertyCategoryKey, "storage.port").toInt()
    val storageUsername = getProperty(propertyCategoryKey, "storage.username")
    val storagePassword = getProperty(propertyCategoryKey, "storage.password")
    val keyStorePath = getProperty(propertyCategoryKey, "storage.keystore.path")
    val keyStorePassword = getProperty(propertyCategoryKey, "storage.keystore.password")
    val storage = CassandraCredentials(storageHost, storagePort, storageUsername, storagePassword, keyStorePath, keyStorePassword)
    return storage
}
