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
     * Type class map.
     */
    private val typeClassMap = mutableMapOf<String, Class<out Record>>()
    /**
     * Class type map.
     */
    private val classTypeMap = mutableMapOf<Class<out Record>, String>()

    /**
     * Initialize core safe record types.
     */
    init {
        Safe.registerType(GroupMemberRecord::class.java)
        Safe.registerType(GroupRecord::class.java)
        Safe.registerType(HostRecord::class.java)
        Safe.registerType(LogRecord::class.java)
        Safe.registerType(UserRecord::class.java)
    }

    /**
     * Registers record type to be stored to safe.
     * @param clazz the record class
     */
    @Synchronized fun registerType(clazz: Class<out Record>) {
        typeClassMap.put(clazz.simpleName.toLowerCase(), clazz)
        classTypeMap.put(clazz, clazz.simpleName.toLowerCase())
    }

    /**
     * Gets record type.
     * @param clazz the record class
     * @return the record type
     */
    @Synchronized fun getType(clazz: Class<out Record>): String {
        if (!classTypeMap.containsKey(clazz)) {
            throw SecurityException("Unknown record type ${clazz.simpleName.toLowerCase()}")
        }
        return classTypeMap.get(clazz)!!
    }

    /**
     * Gets record type.
     * @param type the record type
     * @return the record class
     */
    @Synchronized fun getClass(type: String): Class<out Record> {
        if (!typeClassMap.containsKey(type)) {
            throw SecurityException("Unknown record type $type")
        }
        return typeClassMap.get(type)!!
    }

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
        val type = getType(record.javaClass)

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
        val type = getType(record.javaClass)

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
        val type = getType(record.javaClass)
        remove(key, type)
    }

    /**
     * Removes record of [clazz] with given [key] from safe.
     */
    fun <T : Record> remove(key: String, clazz: Class<T>) {
        val type = getType(clazz)
        remove(key, type)
    }

    /**
     * Removes record of [type] with given [key] from safe.
     */
    private fun remove(key: String, type: String) {
        UserManagement.checkPrivilege(key, type, PRIVILEGE_REMOVE)
        if (has(key, type)) {
            keyValueDao.remove(key, type)
        }
    }

    /**
     * Checks if record of [clazz] with given [key] is in safe.
     */
    fun <T : Record> has(key: String, clazz: Class<T>) : Boolean {
        val type = getType(clazz)
        return has(key, type)
    }

    /**
     * Checks if record of [type] with given [key] is in safe.
     */
    private fun has(key: String, type: String) : Boolean {
        return keyValueDao.has(key, type)
    }

    /**
     * Gets record of [clazz] with given [key] from safe.
     * @return the record
     */
    fun <T : Record> get(key: String, clazz: Class<T>): T? {
        val type = getType(clazz)
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
        val type = getType(clazz)
        return keyValueDao.getKeys(type)
    }

    /**
     * Gets records of [clazz] from safe.
     * @return list of records
     */
    fun <T : Record> getAll(clazz: Class<T>): List<T> {
        val records = keyValueDao.get(clazz)
        val type = getType(clazz)
        for (record in records) {
            UserManagement.checkPrivilege(record.key!!, type, PRIVILEGE_GET)
        }
        return records
    }

    /**
     * Gets records of [clazz] with given [keyPrefix] from safe.
     * @return list of records
     */
    fun <T : Record> getWithKeyPrefix(keyPrefix: String, clazz: Class<T>): List<T> {
        val records = keyValueDao.getWithKeyPrefix(keyPrefix, clazz)
        val type = getType(clazz)
        for (record in records) {
            UserManagement.checkPrivilege(record.key!!, type, PRIVILEGE_GET)
        }
        return records
    }
}