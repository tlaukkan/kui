package org.kui.storage

data class CassandraCredentials(
        val cassandraHost: String,
        val cassandraPort: Int = 9142,
        val username: String,
        val password: String,
        val keyStorePath: String,
        val keyStorePassword: String)