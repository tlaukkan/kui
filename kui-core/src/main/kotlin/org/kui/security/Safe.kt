package org.kui.security

import org.kui.security.model.*
import org.kui.storage.keyValueDao
import java.util.*

class Safe {

    fun add(record: Record) {
        if (record.key!!.contains(':')) {
            throw SecurityException("Record key ${record.key} may not contain ':' character as it is reserved as key part delimiter.")
        }

        userManagement.checkPrivilege(record, PRIVILEGE_ADD)


        record.created = Date()
        record.modified = Date()

        val key = record.key!!
        val type = record.javaClass.name

        if (has(key, record.javaClass)) {
            throw SecurityException("Record already exists: ${record.key}:$type")
        }

        keyValueDao.add(key, record)

    }

    fun update(record: Record) {
        userManagement.checkPrivilege(record, PRIVILEGE_UPDATE)

        record.modified = Date()

        val key = record.key!!
        val type = record.javaClass.name

        if (!has(key, record.javaClass)) {
            throw SecurityException("Record does not exist: ${record.key}:$type")
        }

        keyValueDao.update(key, record)
    }

    fun remove(record: Record) {
        val key = record.key!!
        val type = record.javaClass.name
        remove(key, type)
    }

    fun <T : Record> remove(key: String, clazz: Class<T>) : Unit {
        val type = clazz.name
        remove(key, type)
    }

    fun remove(key: String, type: String) : Unit {
        userManagement.checkPrivilege(key, type, PRIVILEGE_REMOVE)
        if (has(key, type)) {
            keyValueDao.remove(key, type)
        }
    }

    fun <T : Record> has(key: String, clazz: Class<T>) : Boolean {
        val type = clazz.name
        return has(key, type)
    }

    fun has(key: String, type: String) : Boolean {
        return keyValueDao.has(key, type)
    }

    fun <T : Record> get(key: String, clazz: Class<T>): T? {
        val type = clazz.name
        if (keyValueDao.has(key, clazz)) {
            userManagement.checkPrivilege(key, type, PRIVILEGE_GET)
        }
        return keyValueDao.get(key, clazz)
    }

    fun <T : Record> getKeys(clazz: Class<T>): List<String> {
        val type = clazz.name
        return keyValueDao.getKeys(type)
    }

    fun <T : Record> getAll(clazz: Class<T>): List<T> {
        val records = keyValueDao.get(clazz)
        for (record in records) {
            userManagement.checkPrivilege(record.key!!, clazz.name, PRIVILEGE_GET)
        }
        return records
    }

    fun <T : Record> getWithKeyPrefix(keyPrefix: String, clazz: Class<T>): List<T> {
        val records = keyValueDao.getWithKeyPrefix(keyPrefix, clazz)
        for (record in records) {
            userManagement.checkPrivilege(record.key!!, clazz.name, PRIVILEGE_GET)
        }
        return records
    }
}