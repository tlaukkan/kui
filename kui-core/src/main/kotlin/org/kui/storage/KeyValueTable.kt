package org.kui.storage

import org.kui.storage.model.KeyValueRow

interface KeyValueTable {
    fun add(key: String, type: String, bytes: ByteArray)
    fun update(key: String, type: String, bytes: ByteArray)
    fun remove(key: String, type: String)
    fun get(key: String, type: String) : ByteArray?
    fun has(key: String, type: String) : Boolean
    fun getKeys(type: String) : List<String>
    fun getWithKeyPrefix(keyStartsWith: String, type: String) : List<KeyValueRow>
    fun getAll(type: String) : List<KeyValueRow>
}