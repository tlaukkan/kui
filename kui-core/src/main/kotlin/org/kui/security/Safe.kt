package org.kui.security

import org.kui.security.model.*
import org.kui.storage.keyValueDao
import java.util.*

/**
 * Safe is an encrypted key value store for storing records with
 * group based access control mechanism.
 */
object Safe {

    /**
     * Adds [record] to safe.
     */
    fun add(record: Record) {
        if (record.key!!.contains(':')) {
            throw SecurityException("Record key ${record.key} may not contain ':' character as it is reserved as key part delimiter.")
        }

        UserManagement.checkPrivilege(record, PRIVILEGE_ADD)

        record.created = Date()
        record.modified = Date()

        val key = record.key!!
        val type = record.javaClass.name

        if (has(key, record.javaClass)) {
            throw SecurityException("Record already exists: ${record.key}:$type")
        }

        keyValueDao.add(key, record)
    }

    /**
     * Updates [record] in safe.
     */
    fun update(record: Record) {
        UserManagement.checkPrivilege(record, PRIVILEGE_UPDATE)

        record.modified = Date()

        val key = record.key!!
        val type = record.javaClass.name

        if (!has(key, record.javaClass)) {
            throw SecurityException("Record does not exist: ${record.key}:$type")
        }

        keyValueDao.update(key, record)
    }

    /**
     * Removes [record] from safe.
     */
    fun remove(record: Record) {
        val key = record.key!!
        val type = record.javaClass.name
        remove(key, type)
    }

    /**
     * Removes record of [clazz] with given [key] from safe.
     */
    fun <T : Record> remove(key: String, clazz: Class<T>) : Unit {
        val type = clazz.name
        remove(key, type)
    }

    /**
     * Removes record of [type] with given [key] from safe.
     */
    fun remove(key: String, type: String) : Unit {
        UserManagement.checkPrivilege(key, type, PRIVILEGE_REMOVE)
        if (has(key, type)) {
            keyValueDao.remove(key, type)
        }
    }

    /**
     * Checks if record of [clazz] with given [key] is in safe.
     */
    fun <T : Record> has(key: String, clazz: Class<T>) : Boolean {
        val type = clazz.name
        return has(key, type)
    }

    /**
     * Checks if record of [type] with given [key] is in safe.
     */
    fun has(key: String, type: String) : Boolean {
        return keyValueDao.has(key, type)
    }

    /**
     * Gets record of [clazz] with given [key] from safe.
     * @return the record
     */
    fun <T : Record> get(key: String, clazz: Class<T>): T? {
        val type = clazz.name
        if (keyValueDao.has(key, clazz)) {
            UserManagement.checkPrivilege(key, type, PRIVILEGE_GET)
        }
        return keyValueDao.get(key, clazz)
    }

    /**
     * Gets record keys of [clazz] from safe.
     * @return list of record keys
     */
    fun <T : Record> getKeys(clazz: Class<T>): List<String> {
        val type = clazz.name
        return keyValueDao.getKeys(type)
    }

    /**
     * Gets records of [clazz] from safe.
     * @return list of records
     */
    fun <T : Record> getAll(clazz: Class<T>): List<T> {
        val records = keyValueDao.get(clazz)
        for (record in records) {
            UserManagement.checkPrivilege(record.key!!, clazz.name, PRIVILEGE_GET)
        }
        return records
    }

    /**
     * Gets records of [clazz] with given [keyPrefix] from safe.
     * @return list of records
     */
    fun <T : Record> getWithKeyPrefix(keyPrefix: String, clazz: Class<T>): List<T> {
        val records = keyValueDao.getWithKeyPrefix(keyPrefix, clazz)
        for (record in records) {
            UserManagement.checkPrivilege(record.key!!, clazz.name, PRIVILEGE_GET)
        }
        return records
    }
}