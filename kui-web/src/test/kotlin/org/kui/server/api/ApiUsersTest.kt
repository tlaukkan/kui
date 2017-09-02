package org.kui.server

import khttp.get
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.kui.server.api.getApiObjectMapper
import org.kui.api.model.User

class ApiUsersTest : ApiTest() {

    @Test
    @Ignore
    fun testGetUsers() {
        val r = get("https://127.0.0.1:8443/api/users")
        println(r.text)
        val mapper = getApiObjectMapper()
        val users: Array<User> = mapper.readValue(r.text, Array<User>::class.java)
        Assert.assertTrue(users.size > 0)
    }

}