package org.kui.security

import org.kui.api.model.Tagger
import org.kui.security.model.*
import org.kui.util.getProperty
import views.alerts.activity.ActivityAlert

val SECURITY_PROVIDER = "SunJCE"
val ENCRYPTION_ALGORITHM_AES_GCM = "AES/GCM/NoPadding"

val AES_KEY_SIZE = 128
val GCM_TAG_LENGTH = 16

val PASSWORD_HASH_ALGORITHM_PBKDF2_HMAC_SHA512 = "PBKDF2WithHmacSHA512"
val PASSWORD_HASH_ITERATIONS = 65536
val PASSWORD_HASH_KEY_SIZE = 128

val GROUP_SYSTEM = "system"
val GROUP_ADMIN = "admin"
val GROUP_USER = "user"
val GROUP_ANONYMOUS = "anonymous"

val USER_SYSTEM_USER = "system.user"
val USER_DEFAULT_ADMIN = getProperty("security","default.admin.user.name")

val PRIVILEGE_GET = "get"
val PRIVILEGE_ADD = "add"
val PRIVILEGE_UPDATE = "update"
val PRIVILEGE_REMOVE = "remove"

val contextService = ContextService()
val crypto = Crypto()
val userManagement = UserManagement()
val safe = Safe()

fun getRecordClass(type: String) : Class<Record> {
    if (type.equals("tagger")) {
        return Tagger::class.java as Class<Record>
    } else if (type.equals("activityalert")) {
        return ActivityAlert::class.java as Class<Record>
    } else if (type.equals("host")) {
        return HostRecord::class.java as Class<Record>
    } else {
        throw SecurityException("No such safe record type: $type")
    }
}

fun getRequiredGroup(type: String, operation: String) : String {
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