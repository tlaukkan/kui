import org.kui.storage.TimeUuidGenerator
import org.kui.storage.cassandra.CassandraCredentials
import org.kui.util.getProperty
import java.util.*

fun getCassandraCredentials(): CassandraCredentials {
    val propertyCategoryKey = "storage"
    val storageHost = getProperty(propertyCategoryKey, "cassandra.host")
    val storagePort = getProperty(propertyCategoryKey, "cassandra.port").toInt()
    val storageUsername = getProperty(propertyCategoryKey, "cassandra.username")
    val storagePassword = getProperty(propertyCategoryKey, "cassandra.password")
    val keyStorePath = getProperty(propertyCategoryKey, "cassandra.keystore.path")
    val keyStorePassword = getProperty(propertyCategoryKey, "cassandra.keystore.password")
    val storage = CassandraCredentials(storageHost, storagePort, storageUsername, storagePassword, keyStorePath, keyStorePassword)
    return storage
}

private val timeUuidGeneratorForComparison = TimeUuidGenerator("", false)
fun generateTimedUuidForComparison(timeSeconds: Long) : UUID {
    return timeUuidGeneratorForComparison.generate(timeSeconds)
}