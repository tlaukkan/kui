package org.kui.security

import org.slf4j.LoggerFactory
import org.kui.security.model.*
import org.kui.storage.keyValueDao
import org.kui.util.getProperty
import java.util.*

/**
 * User management service provides:
 * - user and group management functions
 */
object UserManagement {
    private val log = LoggerFactory.getLogger("org.kui.security")

    /**
     * Configures user management. If this is first startup then adds default users and groups.
     */
    fun configure() {

        if (!keyValueDao.has(GROUP_SYSTEM, GroupRecord::class.java.simpleName.toLowerCase())) {
            val systemUser = UserRecord(USER_SYSTEM_USER, Date(), Date(), null, null)
            keyValueDao.add(systemUser.key!!, systemUser)

            val systemGroup = GroupRecord(GROUP_SYSTEM, Date(), Date())
            keyValueDao.add(systemGroup.key!!, systemGroup)

            val adminGroup = GroupRecord(GROUP_ADMIN, Date(), Date())
            keyValueDao.add(adminGroup.key!!, adminGroup)

            val userGroup = GroupRecord(GROUP_USER, Date(), Date())
            keyValueDao.add(userGroup.key!!, userGroup)


            val threadContextExisted = ContextService.hasThreadContext()
            if (!threadContextExisted) {
                ContextService.setThreadContext(SecurityContext(systemUser.key!!, listOf(systemGroup.key!!, adminGroup.key!!), ByteArray(0), Date()))
            }

            grantGroup(systemUser.key!!, GROUP_SYSTEM)
            grantGroup(systemUser.key!!, GROUP_ADMIN)
            grantGroup(systemUser.key!!, GROUP_USER)

            val defaultAdminUser = UserRecord(USER_DEFAULT_ADMIN, Date(), Date(), null, Crypto.passwordHash(USER_DEFAULT_ADMIN, getProperty("security","default.admin.user.password")))
            keyValueDao.add(USER_DEFAULT_ADMIN, defaultAdminUser)
            grantGroup(USER_DEFAULT_ADMIN, GROUP_ADMIN)
            grantGroup(USER_DEFAULT_ADMIN, GROUP_USER)

            val defaultClientUser = UserRecord(getProperty("security","default.client.user.name"), Date(), Date(), null, Crypto.passwordHash(getProperty("security","default.client.user.name"), getProperty("security","default.client.user.password")))
            keyValueDao.add(getProperty("security","default.client.user.name"), defaultClientUser)
            //TODO create client group
            grantGroup(getProperty("security","default.client.user.name"), GROUP_USER)

            if (!threadContextExisted) {
                ContextService.clearThreadContext()
            }
        }

    }

    /**
     * Grants user identified by [userKey] membership to group identified by [groupKey].
     */
    fun grantGroup(userKey: String, groupKey: String) {
        checkPrivilege(groupKey, GroupRecord::class.java.simpleName.toLowerCase(), PRIVILEGE_UPDATE)

        val groupUserMember = GroupMemberRecord("g:$groupKey:$userKey", Date(), Date(), groupKey, userKey)
        keyValueDao.add(groupUserMember.key!!, groupUserMember)
        val userGroupMember = GroupMemberRecord("u:$userKey:$groupKey", Date(), Date(), groupKey, userKey)
        keyValueDao.add(userGroupMember.key!!, userGroupMember)
        log.info("AUDIT '${ContextService.getThreadContext().user}' granted '$userKey' membership to '$groupKey'.")
    }

    /**
     * Revokes user identified by [userKey] membership from group identified by [groupKey].
     */
    fun revokeGroup(userKey: String, groupKey: String) {
        checkPrivilege(groupKey, GroupRecord::class.java.simpleName.toLowerCase(), PRIVILEGE_UPDATE)

        keyValueDao.remove("g:$groupKey:$userKey", GroupMemberRecord::class.java.simpleName.toLowerCase())
        keyValueDao.remove("u:$userKey:$groupKey", GroupMemberRecord::class.java.simpleName.toLowerCase())

        log.info("AUDIT '${ContextService.getThreadContext().user}' revoked '$userKey' membership from '$groupKey'.")
    }

    /**
     * Checks if current user has membership in group identified by [groupKey].
     * @throws SecurityException if current user does not have the membership.
     */
    fun checkGroup(groupKey: String){
        val context = ContextService.getThreadContext()
        if (context.groups.contains(groupKey)) {
            return
        }
        if (context.groups.contains(GROUP_SYSTEM)) {
            return
        }
        throw SecurityException("Context $context did not have group $groupKey.")
    }

    /**
     * Checks if current user has privilege to do an [operation] on a [record].
     * @throws SecurityException if current user does not have the privilege.
     */
    fun checkPrivilege(record: Record, operation: String) {
        checkPrivilege(record.key!!, record.javaClass.simpleName.toLowerCase(), operation)
    }

    /**
     * Checks if current user has privilege to do an [operation] on a record of [recordType]
     * and identified by [recordKey].
     * @throws SecurityException if current user does not have the privilege.
     */
    fun checkPrivilege(recordKey: String, recordType: String, operation: String) {
        checkGroup(getRequiredGroup(recordType, operation))
    }

    /**
     * Gets groups of [user].
     * @return list of group keys
     */
    fun getUserGroups(user: String) : List<String> {
        val keyPrefix = "u:$user:"
        val groupMemberRecords = keyValueDao.getWithKeyPrefix(keyPrefix, GroupMemberRecord::class.java)

        val groups = arrayListOf<String>()
        for (groupMemberRecord in groupMemberRecords) {
            groups.add(groupMemberRecord.group!!)
        }

        return groups
    }

    /**
     * Gets [group] members.
     * @return list of user keys having [group] membership.
     */
    fun getGroupMembers(group: String) : List<String> {
        val keyPrefix = "g:$group:"
        val groupMemberRecords = keyValueDao.getWithKeyPrefix(keyPrefix, GroupMemberRecord::class.java)

        val users = arrayListOf<String>()
        for (groupMemberRecord in groupMemberRecords) {
            users.add(groupMemberRecord.user!!)
        }

        return users
    }

    /**
     * Gets users.
     * @return list of users
     */
    fun getUsers() : List<UserRecord> {
        return Safe.getAll(UserRecord::class.java)
    }

    /**
     * Gets user identified by [key].
     * @return the user
     */
    fun getUser(key: String) : UserRecord {
        return Safe.get(key, UserRecord::class.java)!!
    }

    /**
     * Gets user identified by [key].
     * @return TRUE if user exists.
     */
    fun hasUser(key: String) : Boolean {
        return Safe.has(key, UserRecord::class.java)
    }

    /**
     * Adds user identified by [key].
     * @param email the email address
     * @param password the password
     */
    fun addUser(key: String, email: String, password: String) : Unit {
        if (password.length < 12) {
            throw SecurityException("Passwords under length of 12 are prohibited.")
        }
        Safe.add(UserRecord(key, Date(), Date(), email, Crypto.passwordHash(key, password)))
        grantGroup(key, GROUP_USER)
        log.info("AUDIT '${ContextService.getThreadContext().user}' added user '$key'.")
    }

    /**
     * Updates user information identified by [key].
     * @param email the email address
     * @param password the password
     */
    fun updateUser(key: String, email: String, password: String?) {
        val userRecord = Safe.get(key, UserRecord::class.java)!!
        userRecord.email = email
        if (password != null) {
            if (password.length < 12) {
                throw SecurityException("Passwords under length of 12 are prohibited.")
            }
            userRecord.passwordHash = Crypto.passwordHash(key, password!!)
        }
        Safe.update(userRecord)
        log.info("AUDIT '${ContextService.getThreadContext().user}' updated user '$key'.")
    }

    /**
     * Removes user identified by [key].
     */
    fun removeUser(key: String) {
        Safe.remove(key, UserRecord::class.java)
        log.info("AUDIT '${ContextService.getThreadContext().user}' removed user '$key'.")
    }

    /**
     * Gets group identified by [key].
     */
    fun getGroup(key: String) : GroupRecord {
        return keyValueDao.get(key, GroupRecord::class.java)!!
    }

    /**
     * Changes current user password.
     * @param oldPassword the old password.
     * @param newPassword the new password.
     * @throws SecurityException if [oldPassword] does not match the current password of the current user.
     */
    fun changeCurrentUserPassword(oldPassword: String, newPassword: String) : Unit {
        val key = ContextService.getThreadContext().user
        val userRecord = keyValueDao.get(key, UserRecord::class.java)!!

        val oldPasswordHash = Crypto.passwordHash(key, oldPassword!!)

        if (!(oldPasswordHash contentEquals userRecord.passwordHash!!)) {
            throw SecurityException("Change password access denied. Old password incorrect for user: $key")
        }

        userRecord.passwordHash = Crypto.passwordHash(key, newPassword!!)

        keyValueDao.update(key, userRecord)

        log.info("AUDIT $key changed password.")
    }

    /**
     * Validates [password] of user identified by [userKey].
     */
    fun validatePassword(userKey: String, password: String) : Boolean {
        val userRecord = keyValueDao.get(userKey, UserRecord::class.java) ?: return false
        val passwordHash = Crypto.passwordHash(userKey, password)
        return passwordHash contentEquals userRecord.passwordHash!!
    }

    /**
     * Gets required group for applying [operation] on given safe record [type].
     * @return group key
     */
    private fun getRequiredGroup(type: String, operation: String) : String {
        //TODO Create dynamic mapping
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
        return GROUP_USER
    }
}
