package org.kui.storage

import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Table
import java.nio.ByteBuffer
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap
import com.amazonaws.services.dynamodbv2.model.*
import java.util.ArrayList


fun getKeyValueTable(dynamoDB : DynamoDB, tableName: String) : Table {
    if (dynamoDB.getTable(tableName) == null) {
        val attributeDefinitions = ArrayList<AttributeDefinition>()
        attributeDefinitions.add(AttributeDefinition().withAttributeName("ClassType").withAttributeType("S"))
        attributeDefinitions.add(AttributeDefinition().withAttributeName("ItemKey").withAttributeType("S"))

        val keySchema = ArrayList<KeySchemaElement>()
        keySchema.add(KeySchemaElement().withAttributeName("ClassType").withKeyType(KeyType.HASH))
        keySchema.add(KeySchemaElement().withAttributeName("ItemKey").withKeyType(KeyType.RANGE))

        val request = CreateTableRequest().withTableName(tableName).withKeySchema(keySchema)
                .withAttributeDefinitions(attributeDefinitions).withProvisionedThroughput(
                ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L))

        val table = dynamoDB.createTable(request)

        table.waitForActive()
    }

    return dynamoDB.getTable(tableName)
}

class DynamoDbKeyValueTable : KeyValueTable, DynamoDbTable() {

    val table = getKeyValueTable(dynamoDb, "KeyValue")

    override fun add(key: String, type: String, bytes: ByteArray) {
        val item = Item()
                .withPrimaryKey("ItemKey", key, "ClassType", type)
                .withBinary("Value", ByteBuffer.wrap(bytes))
        table.putItem(item)
    }

    override fun update(key: String, type: String, bytes: ByteArray) {
        val item = Item()
                .withPrimaryKey("ItemKey", key, "ClassType", type)
                .withBinary("Value", ByteBuffer.wrap(bytes))
        table.putItem(item)
    }

    override fun remove(key: String, type: String) {
        table.deleteItem("ClassType", type, "ItemKey", key)
    }

    override fun get(key: String, type: String): ByteArray? {
        val item = table.getItem("ClassType", type, "ItemKey", key)
        if (item == null) {
            return null
        }
        return item.getBinary("Value")
    }

    override fun has(key: String, type: String): Boolean {
        val item = table.getItem("ClassType", type, "ItemKey", key)
        return item != null
    }

    override fun getKeys(type: String): List<String> {
        val result = table.query("ClassType", type)
        val rows = mutableListOf<String>()
        for (item in result) {
            val key = item.getString("ItemKey")
            rows.add(key)
        }
        return rows
    }

    override fun getWithKeyPrefix(keyStartsWith: String, type: String) : List<KeyValueRow> {
        val spec = QuerySpec().withKeyConditionExpression("ClassType = :v_type and ItemKey between :v_start_key and :v_end_key")
                .withValueMap(ValueMap().withString(":v_type", type).withString(":v_start_key", keyStartsWith)
                        .withString(":v_end_key", "${keyStartsWith}z"))

        val rows = mutableListOf<KeyValueRow>()
        val result = table.query(spec)
        for (item in result) {
            val key = item.getString("ItemKey")
            val value = item.getBinary("Value")
            rows.add(KeyValueRow(key, value))
        }
        return rows
    }

    override fun getAll(type: String) : List<KeyValueRow> {
        val result = table.query("ClassType", type)
        val rows = mutableListOf<KeyValueRow>()
        for (item in result) {
            val key = item.getString("ItemKey")
            val value = item.getBinary("Value")
            rows.add(KeyValueRow(key, value))
        }
        return rows
    }
}