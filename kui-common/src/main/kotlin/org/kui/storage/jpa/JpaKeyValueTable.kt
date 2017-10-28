package org.kui.storage.jpa

import org.kui.storage.KeyValueRow
import org.kui.storage.KeyValueTable

class JpaKeyValueTable : KeyValueTable {

    val entityManager = entityManagerFactory.createEntityManager()

    @Synchronized override fun add(key: String, type: String, bytes: ByteArray) {
        entityManager.transaction.begin()
        try {
            entityManager.persist(KeyValueEntity("$key:$type",key, type, bytes))
            entityManager.transaction.commit()
        } catch (e : Exception) {
            if (entityManager.transaction.isActive) {
                entityManager.transaction.rollback()
            }
        }
    }

    @Synchronized override fun update(key: String, type: String, bytes: ByteArray) {
        entityManager.transaction.begin()
        try {
            val keyValueEntity = entityManager.find(KeyValueEntity::class.java, "$key:$type") ?: throw RuntimeException("Key value not found: $key:$type")
            keyValueEntity!!.bytes = bytes
            entityManager.persist(entityManager.merge(keyValueEntity))
            entityManager.detach(keyValueEntity)
            entityManager.transaction.commit()
        } catch (e : Exception) {
            if (entityManager.transaction.isActive) {
                entityManager.transaction.rollback()
            }
        }
    }

    @Synchronized override fun remove(key: String, type: String) {
        entityManager.transaction.begin()
        try {
            val keyValueEntity = entityManager.find(KeyValueEntity::class.java, "$key:$type") ?: throw RuntimeException("Key value not found: $key:$type")
            entityManager.remove(entityManager.merge(keyValueEntity))
            entityManager.transaction.commit()
        } catch (e : Exception) {
            if (entityManager.transaction.isActive) {
                entityManager.transaction.rollback()
            }
        }
    }

    @Synchronized override fun get(key: String, type: String): ByteArray? {
        return entityManager.find(KeyValueEntity::class.java, "$key:$type")?.bytes
    }

    @Synchronized override fun has(key: String, type: String): Boolean {
        return entityManager.find(KeyValueEntity::class.java, "$key:$type") != null
    }

    @Synchronized override fun getKeys(type: String): List<String> {
        val query = entityManager.createQuery("select e from KeyValueEntity e where e.type = :type", KeyValueEntity::class.java)
        query.setParameter("type", type)

        val entities = arrayListOf<String>()
        for (entity in query.resultList) {
            entities.add(entity.key)
        }

        return entities
    }

    @Synchronized override fun getWithKeyPrefix(keyStartsWith: String, type: String): List<KeyValueRow> {
        val query = entityManager.createQuery("select e from KeyValueEntity e where e.key like :keyPrefix and e.type = :type", KeyValueEntity::class.java)
        query.setParameter("keyPrefix", "$keyStartsWith%")
        query.setParameter("type", type)

        val keyValueRows = arrayListOf<KeyValueRow>()
        for (entity in query.resultList) {
            keyValueRows.add(KeyValueRow(entity.key, entity.bytes))
        }

        return keyValueRows
    }

    @Synchronized override fun getAll(type: String): List<KeyValueRow> {
        val query = entityManager.createQuery("select e from KeyValueEntity e where e.type = :type", KeyValueEntity::class.java)
        query.setParameter("type", type)

        val keyValueRows = arrayListOf<KeyValueRow>()
        for (entity in query.resultList) {
            keyValueRows.add(KeyValueRow(entity.key, entity.bytes))
        }

        return keyValueRows
    }

}