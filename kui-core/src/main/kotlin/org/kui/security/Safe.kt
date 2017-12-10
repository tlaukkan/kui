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
     * Map of privilege and list of permitted groups.
     */
    private val privilegeGroupMap = mutableMapOf<String, List<String>>()

    /**
     * Initialize core safe record types.
     */
    init {
        registerType(GroupMemberRecord::class.java)
        permitOperation(GroupMemberRecord::class.java, PRIVILEGE_ADD, listOf(GROUP_ADMIN))
        permitOperation(GroupMemberRecord::class.java, PRIVILEGE_UPDATE, listOf(GROUP_ADMIN))
        permitOperation(GroupMemberRecord::class.java, PRIVILEGE_REMOVE, listOf(GROUP_ADMIN))
        permitOperation(GroupMemberRecord::class.java, PRIVILEGE_GET, listOf(GROUP_ADMIN))

        registerType(GroupRecord::class.java)
        permitOperation(GroupRecord::class.java, PRIVILEGE_ADD, listOf(GROUP_ADMIN))
        permitOperation(GroupRecord::class.java, PRIVILEGE_UPDATE, listOf(GROUP_ADMIN))
        permitOperation(GroupRecord::class.java, PRIVILEGE_REMOVE, listOf(GROUP_ADMIN))
        permitOperation(GroupRecord::class.java, PRIVILEGE_GET, listOf(GROUP_ADMIN))

        registerType(HostRecord::class.java)
        permitOperation(HostRecord::class.java, PRIVILEGE_ADD, listOf(GROUP_USER))
        permitOperation(HostRecord::class.java, PRIVILEGE_UPDATE, listOf(GROUP_ADMIN))
        permitOperation(HostRecord::class.java, PRIVILEGE_REMOVE, listOf(GROUP_ADMIN))
        permitOperation(HostRecord::class.java, PRIVILEGE_GET, listOf(GROUP_USER))

        registerType(LogRecord::class.java)
        permitAllOperations(LogRecord::class.java, listOf(GROUP_USER))

        registerType(UserRecord::class.java)
        permitOperation(UserRecord::class.java, PRIVILEGE_ADD, listOf(GROUP_ADMIN))
        permitOperation(UserRecord::class.java, PRIVILEGE_UPDATE, listOf(GROUP_ADMIN))
        permitOperation(UserRecord::class.java, PRIVILEGE_REMOVE, listOf(GROUP_ADMIN))
        permitOperation(UserRecord::class.java, PRIVILEGE_GET, listOf(GROUP_ADMIN))

        /*
        if (type.equals(HostRecord::class.java.simpleName) && operation.equals(PRIVILEGE_UPDATE)) {
            return GROUP_ADMIN
        }
        if (type.equals(HostRecord::class.java.simpleName) && operation.equals(PRIVILEGE_REMOVE)) {
            return GROUP_ADMIN
        }
        if (type.equals(UserRecord::class.java.simpleName)) {
            return GROUP_ADMIN
        }
        if (type.equals(GroupRecord::class.java.simpleName)) {
            return GROUP_ADMIN
        }
        if (type.equals(GroupMemberRecord::class.java.simpleName)) {
            return GROUP_ADMIN
        }
        */

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

        checkPrivilege(record, PRIVILEGE_ADD)

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
        checkPrivilege(record, PRIVILEGE_UPDATE)

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
        checkPrivilege(type, PRIVILEGE_REMOVE)
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
            checkPrivilege(type, PRIVILEGE_GET)
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
            checkPrivilege(type, PRIVILEGE_GET)
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
            checkPrivilege(type, PRIVILEGE_GET)
        }
        return records
    }

    /**
     * Checks if current user has privilege to do an [operation] on a [record].
     * @throws SecurityException if current user does not have the privilege.
     */
    fun checkPrivilege(record: Record, operation: String) {
        checkPrivilege(record.javaClass.simpleName.toLowerCase(), operation)
    }

    /**
     * Checks if current user has privilege to do an [operation] on a record of [recordType]
     * and identified by [recordKey].
     * @throws SecurityException if current user does not have the privilege.
     */
    fun checkPrivilege(recordType: String, operation: String) {
        UserManagement.checkGroup(getRequiredGroup(recordType, operation))
    }

    /**
     * Gets required group for applying [operation] on given safe record [type].
     * @return group key
     */
    @Synchronized private fun getRequiredGroup(type: String, operation: String) : List<String> {
        val privilege = "$type/$operation"
        if (privilegeGroupMap.containsKey(privilege)) {
            return privilegeGroupMap[privilege]!!
        }
        return listOf(GROUP_SYSTEM)
    }

    /**
     * Grant []operation] on a [type] for listed [groups].
     */
    @Synchronized fun permitOperation(clazz: Class<out Record>, operation: String, groups: List<String>) {
        val type = getType(clazz)
        val privilege = "$type/$operation"
        if (privilegeGroupMap.containsKey(privilege)) {
            throw SecurityException("$privilege permitted groups already set.")
        }
        privilegeGroupMap.put(privilege, groups)
    }

    /**
     * Permits all operations on record class [clazz] for [groups].
     */
    fun permitAllOperations(clazz: Class<out Record>, groups: List<String>) {
        permitOperation(clazz, PRIVILEGE_ADD, listOf(GROUP_USER))
        permitOperation(clazz, PRIVILEGE_UPDATE, listOf(GROUP_USER))
        permitOperation(clazz, PRIVILEGE_REMOVE, listOf(GROUP_USER))
        permitOperation(clazz, PRIVILEGE_GET, listOf(GROUP_USER))
    }

}
