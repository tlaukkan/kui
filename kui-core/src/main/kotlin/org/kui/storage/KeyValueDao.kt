package org.kui.storage

import com.fasterxml.jackson.databind.ObjectMapper
import org.kui.security.Crypto
import org.kui.storage.cassandra.CassandraKeyValueTable
import org.kui.storage.dynamodb.DynamoDbKeyValueTable
import org.kui.storage.jpa.JpaKeyValueTable
import org.kui.util.getProperty

class KeyValueDao {

    val keyValueTable : KeyValueTable =
            if ("cassandra".equals(getProperty("storage", "storage.type"))) {
                CassandraKeyValueTable("key_value")
            } else if ("dynamodb".equals(getProperty("storage", "storage.type"))) {
                DynamoDbKeyValueTable()
            } else {
                JpaKeyValueTable()
            }

    fun add(key: String, value: Any) {
        val type = value.javaClass.name
        val mapper = ObjectMapper()
        val keyAndType = (key + ":" + type)
        val nonce = keyAndType.hashCode().toString().toByteArray()
        val aad = keyAndType.toByteArray()
        val plainText = mapper.writeValueAsBytes(value)
        val cipherText = Crypto.encrypt(nonce, aad, plainText)
        keyValueTable.add(key, type, cipherText)
    }

    fun update(key: String, value: Any) {
        val type = value.javaClass.name
        val mapper = ObjectMapper()
        val keyAndType = (key + ":" + type)
        val nonce = keyAndType.hashCode().toString().toByteArray()
        val aad = keyAndType.toByteArray()
        val plainText = mapper.writeValueAsBytes(value)
        val cipherText = Crypto.encrypt(nonce, aad, plainText)
        keyValueTable.update(key, type, cipherText)
    }

    fun <T> remove(key: String, clazz: Class<T>) {
        remove(key, clazz.name)
    }

    fun remove(key: String, type: String) {
        keyValueTable.remove(key, type)
    }

    fun <T> has(key: String, clazz: Class<T>): Boolean {
        return has(key, clazz.name)
    }

    fun has(key: String, type: String): Boolean {
        return keyValueTable.has(key, type)
    }

    fun get(key: String, type: String): ByteArray? {
        val keyAndType = (key + ":" + type)
        val nonce = keyAndType.hashCode().toString().toByteArray()
        val aad = keyAndType.toByteArray()
        val cipherText = keyValueTable.get(key, type) ?: return null
        val plainText = Crypto.decrypt(nonce, aad, cipherText)
        return plainText
    }

    fun <T> get(key: String, clazz: Class<T>): T? {
        val type = clazz.name
        val mapper = ObjectMapper()
        val keyAndType = (key + ":" + type)
        val nonce = keyAndType.hashCode().toString().toByteArray()
        val aad = keyAndType.toByteArray()
        val cipherText = keyValueTable.get(key, type) ?: return null
        val plainText = Crypto.decrypt(nonce, aad, cipherText)
        return mapper.readValue(plainText, clazz)
    }

    fun get(type: String): List<ByteArray> {
        val resultSet = keyValueTable.getAll(type)
        val values = arrayListOf<ByteArray>()
        for (row in resultSet) {
            val key = row.key
            val cipherText = row.value

            val keyAndType = (key + ":" + type)
            val nonce = keyAndType.hashCode().toString().toByteArray()
            val aad = keyAndType.toByteArray()
            val plainText = Crypto.decrypt(nonce, aad, cipherText)

            values.add(plainText)
        }
        return values
    }

    fun <T> get(clazz: Class<T>): List<T> {
        val type = clazz.name
        val mapper = ObjectMapper()
        val resultSet = keyValueTable.getAll(type)
        val values = arrayListOf<T>()
        for (row in resultSet) {
            val key = row.key
            val cipherText = row.value

            val keyAndType = (key + ":" + type)
            val nonce = keyAndType.hashCode().toString().toByteArray()
            val aad = keyAndType.toByteArray()
            val plainText = Crypto.decrypt(nonce, aad, cipherText)

            values.add(mapper.readValue(plainText, clazz))
        }
        return values
    }

    fun <T> getWithKeyPrefix(keyPrefix: String, clazz: Class<T>): List<T> {
        val type = clazz.name
        val mapper = ObjectMapper()
        val resultSet = keyValueTable.getWithKeyPrefix(keyPrefix, type)
        val values = arrayListOf<T>()
        for (row in resultSet) {
            val key = row.key
            val cipherText = row.value

            val keyAndType = (key + ":" + type)
            val nonce = keyAndType.hashCode().toString().toByteArray()
            val aad = keyAndType.toByteArray()
            val plainText = Crypto.decrypt(nonce, aad, cipherText)

            values.add(mapper.readValue(plainText, clazz))
        }
        return values
    }


    fun getKeys(type: String): List<String> {
        return keyValueTable.getKeys(type)
    }

}