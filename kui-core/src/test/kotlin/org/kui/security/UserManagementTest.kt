package org.kui.security

import org.junit.*
import org.kui.DatabaseUnitTest
import org.kui.security.model.SecurityContext
import java.util.*

class UserManagementTest : DatabaseUnitTest() {

    @Test
    fun testUserManagement() {

        val key = "first.last"
        val email = "email"
        val emailModified = "email-modified"
        val password = "password1234"

        Assert.assertEquals(3, UserManagement.getUsers().size)

        val context = ContextService.getThreadContext()

        // Test that user cannot be added with user without admin group.
        ContextService.clearThreadContext()
        ContextService.setThreadContext(SecurityContext(USER_DEFAULT_ADMIN, listOf(GROUP_USER), ByteArray(0), Date()))
        try {
            UserManagement.addUser(key, email, password)
            Assert.fail()
        } catch (e: SecurityException) {
            Assert.assertEquals("default.admin did not have any of the permitted groups: [admin].", e.message)
        }

        ContextService.clearThreadContext()
        ContextService.setThreadContext(context)

        UserManagement.addUser(key, email, password)


        Assert.assertEquals(4, UserManagement.getUsers().size)

        val user = UserManagement.getUser(key)

        Assert.assertNotNull(user)
        Assert.assertEquals(key, user.key)
        Assert.assertEquals(email, user.email)
        Assert.assertArrayEquals(Crypto.passwordHash(key, password), user.passwordHash)
        Assert.assertNull(null, user.passwordLoginFailed)
        Assert.assertNotNull(user.created)
        Assert.assertNotNull(user.modified)

        UserManagement.updateUser(key, emailModified, password)
        Assert.assertEquals(4, UserManagement.getUsers().size)

        val user2 = UserManagement.getUser(key)

        Assert.assertNotNull(user2)
        Assert.assertEquals(key, user2.key)
        Assert.assertEquals(emailModified, user2.email)
        Assert.assertArrayEquals(Crypto.passwordHash(key, password), user2.passwordHash)
        Assert.assertNull(null, user2.passwordLoginFailed)
        Assert.assertNotNull(user2.created)
        Assert.assertNotNull(user2.modified)

        Assert.assertEquals(1, UserManagement.getUserGroups(key).size)
        Assert.assertTrue(UserManagement.getUserGroups(key).contains(GROUP_USER))

        UserManagement.grantGroup(key, GROUP_ADMIN)

        Assert.assertEquals(2, UserManagement.getUserGroups(key).size)
        Assert.assertTrue(UserManagement.getUserGroups(key).contains(GROUP_USER))
        Assert.assertTrue(UserManagement.getUserGroups(key).contains(GROUP_ADMIN))

        UserManagement.revokeGroup(key, GROUP_ADMIN)

        Assert.assertEquals(1, UserManagement.getUserGroups(key).size)
        Assert.assertTrue(UserManagement.getUserGroups(key).contains(GROUP_USER))

        UserManagement.removeUser(key)
        Assert.assertEquals(3, UserManagement.getUsers().size)
        Assert.assertFalse(UserManagement.hasUser(key))

    }

}