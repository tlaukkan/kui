package org.kui.security

import org.apache.log4j.xml.DOMConfigurator
import org.junit.After
import org.junit.Before
import org.kui.security.model.SecurityContext
import org.kui.storage.jpa.clearEntityManagerFactory
import org.kui.storage.jpa.entityManagerFactory
import java.sql.Connection
import java.util.*

open class MemoryDatabaseTest {

    @Before
    fun before() {
        DOMConfigurator.configure("log4j.xml")
        UserManagement.initialize()
        ContextService.setThreadContext(SecurityContext(USER_DEFAULT_ADMIN, listOf(GROUP_USER, GROUP_ADMIN), ByteArray(0), Date()))
    }

    @After
    fun after() {
        val entityManager = entityManagerFactory.createEntityManager()
        entityManager.transaction.begin()
        val con = entityManager.unwrap(Connection::class.java)
        val statement = con.createStatement()
        statement.execute("DROP ALL OBJECTS")
        statement.close()
        entityManager.transaction.commit()
        entityManager.close()
        clearEntityManagerFactory()
        ContextService.clearThreadContext()
    }

}