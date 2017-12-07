package org.kui.security

import org.slf4j.LoggerFactory
import org.kui.security.model.*
import org.kui.storage.keyValueDao
import org.kui.util.getProperty
import java.util.*

class UserManagement {

    private val log = LoggerFactory.getLogger("org.kui.security")

    init {

        if (!keyValueDao.has(GROUP_SYSTEM, GroupRecord::class.java.name)) {
            val systemUser = UserRecord(USER_SYSTEM_USER, Date(), Date(), null, null)
            keyValueDao.add(systemUser.key!!, systemUser)

            val systemGroup = GroupRecord(GROUP_SYSTEM, Date(), Date())
            keyValueDao.add(systemGroup.key!!, systemGroup)

            val adminGroup = GroupRecord(GROUP_ADMIN, Date(), Date())
            keyValueDao.add(adminGroup.key!!, adminGroup)

            val userGroup = GroupRecord(GROUP_USER, Date(), Date())
            keyValueDao.add(userGroup.key!!, userGroup)


            contextService.setThreadContext(SecurityContext(systemUser.key!!, listOf(systemGroup.key!!, adminGroup.key!!), ByteArray(0), Date()))

            grantGroup(systemUser.key!!, GROUP_SYSTEM)
            grantGroup(systemUser.key!!, GROUP_ADMIN)
            grantGroup(systemUser.key!!, GROUP_USER)

            val defaultAdminUser = UserRecord(USER_DEFAULT_ADMIN, Date(), Date(), null, crypto.passwordHash(USER_DEFAULT_ADMIN, getProperty("security","default.admin.user.password")))
            keyValueDao.add(USER_DEFAULT_ADMIN, defaultAdminUser)
            grantGroup(USER_DEFAULT_ADMIN, GROUP_ADMIN)
            grantGroup(USER_DEFAULT_ADMIN, GROUP_USER)

            val defaultClientUser = UserRecord(getProperty("security","default.client.user.name"), Date(), Date(), null, crypto.passwordHash(getProperty("security","default.client.user.name"), getProperty("security","default.client.user.password")))
            keyValueDao.add(getProperty("security","default.client.user.name"), defaultClientUser)
            //TODO create client group
            grantGroup(getProperty("security","default.client.user.name"), GROUP_USER)

            contextService.clearThreadContext()
        }

    }

    fun configure() {
        log.info("Security configured.")
    }

    fun grantGroup(userKey: String, groupKey: String) {
        checkPrivilege(groupKey, GroupRecord::class.java.name, PRIVILEGE_UPDATE)

        val groupUserMember = GroupMemberRecord("g:$groupKey:$userKey", Date(), Date(), groupKey, userKey)
        keyValueDao.add(groupUserMember.key!!, groupUserMember)
        val userGroupMember = GroupMemberRecord("u:$userKey:$groupKey", Date(), Date(), groupKey, userKey)
        keyValueDao.add(userGroupMember.key!!, userGroupMember)
        log.info("AUDIT '${contextService.getThreadContext().user}' granted '$userKey' membership to '$groupKey'.")
    }

    fun revokeGroup(userKey: String, groupKey: String) {
        checkPrivilege(groupKey, GroupRecord::class.java.name, PRIVILEGE_UPDATE)

        keyValueDao.remove("g:$groupKey:$userKey", GroupMemberRecord::class.java.name)
        keyValueDao.remove("u:$userKey:$groupKey", GroupMemberRecord::class.java.name)

        log.info("AUDIT '${contextService.getThreadContext().user}' revoked '$userKey' membership from '$groupKey'.")
    }

    fun checkGroup(groupKey: String){
        val context = contextService.getThreadContext()
        if (context.groups.contains(groupKey)) {
            return
        }
        if (context.groups.contains(GROUP_SYSTEM)) {
            return
        }
        throw SecurityException("Context $context did not have group $groupKey.")
    }

    fun checkPrivilege(record: Record, operation: String) {
        checkPrivilege(record.key!!, record.javaClass.name, operation)
    }

    fun checkPrivilege(recordKey: String, recordType: String, operation: String) {
        checkGroup(getRequiredGroup(recordType, operation))
    }

    fun getUserGroups(user: String) : List<String> {
        val keyPrefix = "u:$user:"
        val groupMemberRecords = keyValueDao.getWithKeyPrefix(keyPrefix, GroupMemberRecord::class.java)

        val groups = arrayListOf<String>()
        for (groupMemberRecord in groupMemberRecords) {
            groups.add(groupMemberRecord.group!!)
        }

        return groups
    }

    fun getGroupMembers(group: String) : List<String> {
        val keyPrefix = "g:$group:"
        val groupMemberRecords = keyValueDao.getWithKeyPrefix(keyPrefix, GroupMemberRecord::class.java)

        val users = arrayListOf<String>()
        for (groupMemberRecord in groupMemberRecords) {
            users.add(groupMemberRecord.user!!)
        }

        return users
    }

    fun getUsers() : List<UserRecord> {
        return safe.getAll(UserRecord::class.java)
    }

    fun getUser(key: String) : UserRecord {
        return safe.get(key, UserRecord::class.java)!!
    }

    fun addUser(key: String, email: String, password: String) : Unit {
        if (password.length < 12) {
            throw SecurityException("Passwords under length of 12 are prohibited.")
        }
        safe.add(UserRecord(key, Date(), Date(), email, crypto.passwordHash(key, password)))
        grantGroup(key, GROUP_USER)
        log.info("AUDIT '${contextService.getThreadContext().user}' added user '$key'.")
    }

    fun updateUser(key: String, email: String, password: String?) : Unit {
        val userRecord = safe.get(key, UserRecord::class.java)!!
        userRecord.email = email
        if (password != null) {
            if (password.length < 12) {
                throw SecurityException("Passwords under length of 12 are prohibited.")
            }
            userRecord.passwordHash = crypto.passwordHash(key, password!!)
        }
        safe.update(userRecord)
        log.info("AUDIT '${contextService.getThreadContext().user}' updated user '$key'.")
    }


    fun removeUser(key: String) : Unit {
        safe.remove(key, UserRecord::class.java)
        log.info("AUDIT '${contextService.getThreadContext().user}' removed user '$key'.")
    }

    fun getGroup(group: String) : GroupRecord {
        return keyValueDao.get(group, GroupRecord::class.java)!!
    }

    private fun createGroupPrivilegeKey(groupKey: String, recordKey: String, recordType: String?, operation: String) = "g:$groupKey:$recordKey:$recordType:$operation"

    private fun createRecordPrivilegeKey(recordKey: String, recordType: String?, groupKey: String, operation: String) = "r:$recordKey:$recordType:$groupKey:$operation"

    fun changeOwnPassword(oldPassword: String, newPassword: String) : Unit {
        val key = contextService.getThreadContext().user
        val userRecord = keyValueDao.get(key, UserRecord::class.java)!!

        val oldPasswordHash = crypto.passwordHash(key, oldPassword!!)

        if (!(oldPasswordHash contentEquals userRecord.passwordHash!!)) {
            throw SecurityException("Change password access denied. Old password incorrect for user: $key")
        }

        userRecord.passwordHash = crypto.passwordHash(key, newPassword!!)

        keyValueDao.update(key, userRecord)

        log.info("AUDIT $key changed password.")
    }

    fun validatePassword(userKey: String, password: String) : Boolean {
        val userRecord = keyValueDao.get(userKey, UserRecord::class.java) ?: return false
        val passwordHash = crypto.passwordHash(userKey, password)
        return passwordHash contentEquals userRecord.passwordHash!!
    }

}
