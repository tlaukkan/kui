package org.kui.storage.jpa

import org.kui.storage.*
import org.kui.storage.model.TimeValue
import org.kui.storage.model.TimeValueCountResult
import org.kui.storage.model.TimeValueResult
import org.kui.storage.model.TimeValueRow
import java.util.*

class JpaTimeValueTable(val type: String) : TimeValueTable {

    override fun insert(container: String, key: String, timeValues: List<TimeValue>) {
        val fullKey = "$type:$container:$key"

        var entities = mutableListOf<TimeValueEntity>()
        var count = 0
        for (timeValue in timeValues) {
            val timeValueEntity = TimeValueEntity(UUID.randomUUID().toString(), fullKey, timeValue.time, Date(), timeValue.value)
            entities.add(timeValueEntity)
            count ++
            if (count == 25) {
                addTimeValueEntities(entities)
                entities = mutableListOf()
                count = 0
            }
        }

        if (count > 0) {
            addTimeValueEntities(entities)
        }
    }

    private fun addTimeValueEntities(timeValueEntities: List<TimeValueEntity>) {
        val entityManager = entityManagerFactory.createEntityManager()
        entityManager.transaction.begin()
        try {
            for (timeValueEntity in timeValueEntities) {
                entityManager.persist(timeValueEntity)
            }
            entityManager.transaction.commit()
        } catch (e : Exception) {
            if (entityManager.transaction.isActive) {
                entityManager.transaction.rollback()
            }
        }
    }

    override fun select(beginTime: Date, beginId: UUID?, endTime_: Date, containers: List<String>, keys: List<String>): TimeValueResult {
        val entityManager = entityManagerFactory.createEntityManager()
        val container = containers[0]
        val key = keys[0]
        val fullKey = "$type:$container:$key"

        val query = entityManager.createQuery("select e from TimeValueEntity e where e.key = :key and e.time >= :beginTime and e.time <= :endTime", TimeValueEntity::class.java)
        query.setParameter("key", fullKey)
        query.setParameter("beginTime", beginTime)
        query.setParameter("endTime", endTime_)

        val rows = mutableListOf<TimeValueRow>()
        val beginIdString = beginId.toString()
        var beginIdFound = false

        for (item in query.resultList) {
            val id = item.id!!
            val time = item.time!!
            val received = item.received!!
            val value = item.bytes

            if (beginId != null && beginIdString.equals(id)) {
                beginIdFound = true
                continue
            }

            if (beginId != null && !beginIdFound) {
                continue
            }

            if (rows.count() == 1000) {
                break
            }

            rows.add(TimeValueRow(id, container, key, time, received, value))
        }

        if (rows.count() == 1000) {
            val endTime = rows.last().time
            val nextBeginId = rows.last().id
            return TimeValueResult(endTime, nextBeginId, rows)
        } else {
            return TimeValueResult(null, null, rows)
        }
    }

    override fun selectCount(beginTime: Date, endTime_: Date, containers: List<String>, keys: List<String>): TimeValueCountResult {
        val entityManager = entityManagerFactory.createEntityManager()
        val container = containers[0]
        val key = keys[0]
        val fullKey = "$type:$container:$key"

        val query = entityManager.createQuery("select count(e) from TimeValueEntity e where e.key = :key and e.time >= :beginTime and e.time <= :endTime", TimeValueEntity::class.java)
        query.setParameter("key", fullKey)
        query.setParameter("beginTime", beginTime)
        query.setParameter("endTime", endTime_)

        val count = query.singleResult as Long

        return TimeValueCountResult(null, count)
    }


}