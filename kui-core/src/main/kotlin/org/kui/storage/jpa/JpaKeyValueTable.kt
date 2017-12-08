package org.kui.storage.jpa

import org.kui.storage.KeyValueRow
import org.kui.storage.KeyValueTable

class JpaKeyValueTable : KeyValueTable {

    override fun add(key: String, type: String, bytes: ByteArray) {
        val entityManager = entityManagerFactory.createEntityManager()
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

    override fun update(key: String, type: String, bytes: ByteArray) {
        val entityManager = entityManagerFactory.createEntityManager()
        entityManager.transaction.begin()
        try {
            val keyValueEntity = entityManager.find(KeyValueEntity::class.java, "$key:$type") ?: throw RuntimeException("Key value not found: $key:$type")
            keyValueEntity!!.bytes = bytes
            entityManager.persist(entityManager.merge(keyValueEntity))
            entityManager.transaction.commit()
            entityManager.detach(keyValueEntity)
        } catch (e : Exception) {
            if (entityManager.transaction.isActive) {
                entityManager.transaction.rollback()
            }
        }
    }

    override fun remove(key: String, type: String) {
        val entityManager = entityManagerFactory.createEntityManager()
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

    override fun get(key: String, type: String): ByteArray? {
        val entityManager = entityManagerFactory.createEntityManager()
        return entityManager.find(KeyValueEntity::class.java, "$key:$type")?.bytes
    }

    override fun has(key: String, type: String): Boolean {
        val entityManager = entityManagerFactory.createEntityManager()
        return entityManager.find(KeyValueEntity::class.java, "$key:$type") != null
    }

    override fun getKeys(type: String): List<String> {
        val entityManager = entityManagerFactory.createEntityManager()

        val query = entityManager.createQuery("select e from KeyValueEntity e where e.type = :type", KeyValueEntity::class.java)
        query.setParameter("type", type)

        val entities = arrayListOf<String>()
        for (entity in query.resultList) {
            entities.add(entity.key)
        }

        return entities
    }

    override fun getWithKeyPrefix(keyStartsWith: String, type: String): List<KeyValueRow> {
        val entityManager = entityManagerFactory.createEntityManager()

        val query = entityManager.createQuery("select e from KeyValueEntity e where e.key like :keyPrefix and e.type = :type", KeyValueEntity::class.java)
        query.setParameter("keyPrefix", "$keyStartsWith%")
        query.setParameter("type", type)

        val keyValueRows = arrayListOf<KeyValueRow>()
        for (entity in query.resultList) {
            keyValueRows.add(KeyValueRow(entity.key, entity.bytes))
        }

        return keyValueRows
    }

    override fun getAll(type: String): List<KeyValueRow> {
        val entityManager = entityManagerFactory.createEntityManager()

        val query = entityManager.createQuery("select e from KeyValueEntity e where e.type = :type", KeyValueEntity::class.java)
        query.setParameter("type", type)

        val keyValueRows = arrayListOf<KeyValueRow>()
        for (entity in query.resultList) {
            keyValueRows.add(KeyValueRow(entity.key, entity.bytes))
        }

        return keyValueRows
    }

}