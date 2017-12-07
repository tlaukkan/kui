package org.kui.security

import org.kui.security.model.*
import org.kui.util.getProperty

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

