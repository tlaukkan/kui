package org.kui.storage

import com.datastax.driver.core.*
import java.nio.ByteBuffer

open class CassandraKeyValueTable(credentials: CassandraCredentials, keyspace: String, table: String, truncate: Boolean = false) : CassandraTable(credentials, keyspace), KeyValueTable {

    private val insert: PreparedStatement
    private val delete: PreparedStatement
    private val select: PreparedStatement
    private val count: PreparedStatement
    private val selectAll: PreparedStatement
    private val selectBetween: PreparedStatement

    init {
        if (truncate) {
            session.execute("DROP TABLE IF EXISTS $table;")
        }

        session.execute("CREATE TABLE IF NOT EXISTS $table(k text, t text, b blob, PRIMARY KEY(t, k));")

        insert = session.prepare(
                "INSERT INTO $table (k, t, b) VALUES (:key, :type, :bytes)")

        delete = session.prepare(
                "DELETE FROM $table WHERE k=:key AND t=:type")

        count = session.prepare(
                "SELECT COUNT(*) FROM $table WHERE k=:key AND t=:type")

        select = session.prepare(
                "SELECT * FROM $table WHERE k=:key AND t=:type")

        selectAll = session.prepare(
                "SELECT * FROM $table WHERE t=:type")

        selectBetween = session.prepare(
                "SELECT * FROM $table WHERE t=:type AND k >= :beginKey AND k <= :endKey")

    }

    override fun add(key: String, type: String, bytes: ByteArray) : Unit {
        session.execute(insert.bind(key, type, ByteBuffer.wrap(bytes)))
    }

    override fun update(key: String, type: String, bytes: ByteArray) : Unit {
        session.execute(insert.bind(key, type, ByteBuffer.wrap(bytes)))
    }

    override fun remove(key: String, type: String) : Unit {
        session.execute(delete.bind(key, type))
    }

    override fun get(key: String, type: String) : ByteArray? {
        val resultSet = session.execute(select.bind(key, type))

        val rows = resultSet.all()
        if (rows.count() == 0) {
            return null
        }
        return rows[0].getBytes("b").array()
    }

    override fun has(key: String, type: String) : Boolean {
        val resultSet = session.execute(count.bind(key, type))

        return resultSet.all()[0].getLong(0) != 0L
    }

    override fun getKeys(type: String) : List<String> {
        val resultSet = session.execute(selectAll.bind(type))

        val records = arrayListOf<String>()
        resultSet.forEach { row ->
            records.add(row.getString("k"))
        }
        return records
    }

    override fun getWithKeyPrefix(keyStartsWith: String, type: String) : List<KeyValueRow> {
        val result = session.execute(selectBetween.bind(type, keyStartsWith, "${keyStartsWith}z"))
        val rows = mutableListOf<KeyValueRow>()
        for (row in result.all()) {
            val key = row.getString("k")
            val value = row.getBytes("b").array()
            rows.add(KeyValueRow(key, value))
        }
        return rows
    }

    override fun getAll(type: String) : List<KeyValueRow> {
        val result = session.execute(selectAll.bind(type))
        val rows = mutableListOf<KeyValueRow>()
        for (row in result.all()) {
            val key = row.getString("k")
            val value = row.getBytes("b").array()
            rows.add(KeyValueRow(key, value))
        }
        return rows
    }

}