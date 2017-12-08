package org.kui.storage.jpa

import org.eclipse.persistence.config.PersistenceUnitProperties
import org.kui.util.getProperty
import java.util.HashMap
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence

val entityManagerFactory get() = getOrCreateEntityManagerFactory()

private var theEntityManagerFactory: EntityManagerFactory? = null

fun getOrCreateEntityManagerFactory() : EntityManagerFactory {
    if (theEntityManagerFactory == null) {
        val properties = HashMap<String, String>()
        properties.put(PersistenceUnitProperties.JDBC_DRIVER, getProperty("storage", "h2.jdbc.driver"))
        properties.put(PersistenceUnitProperties.JDBC_URL, getProperty("storage", "h2.jdbc.url"))
        properties.put(PersistenceUnitProperties.JDBC_USER, getProperty("storage", "h2.jdbc.user"))
        properties.put(PersistenceUnitProperties.JDBC_PASSWORD, getProperty("storage", "h2.jdbc.password"))
        theEntityManagerFactory = Persistence.createEntityManagerFactory("storage", properties)!!
    }
    return theEntityManagerFactory!!
}

fun clearEntityManagerFactory() {
    if (theEntityManagerFactory != null) {
        theEntityManagerFactory!!.close()
        theEntityManagerFactory = null
    }
}