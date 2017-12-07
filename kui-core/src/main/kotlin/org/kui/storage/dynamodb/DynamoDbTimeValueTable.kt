package org.kui.storage.dynamodb

import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Table
import org.kui.model.TimeValue
import org.kui.model.TimeValueCountResult
import org.kui.model.TimeValueResult
import java.util.*
import com.amazonaws.services.dynamodbv2.document.TableWriteItems
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap
import com.amazonaws.services.dynamodbv2.model.*
import org.kui.model.TimeValueRow
import org.kui.storage.TimeValueTable
import java.nio.ByteBuffer

fun getTimeValueTable(dynamoDb: DynamoDB, tableName: String) : Table {
    if (dynamoDb.getTable(tableName) == null) {
        val attributeDefinitions = ArrayList<AttributeDefinition>()
        attributeDefinitions.add(AttributeDefinition().withAttributeName("ItemKey").withAttributeType("S"))
        attributeDefinitions.add(AttributeDefinition().withAttributeName("Id").withAttributeType("S"))
        attributeDefinitions.add(AttributeDefinition().withAttributeName("ItemTime").withAttributeType("N"))

        val keySchema = ArrayList<KeySchemaElement>()
        keySchema.add(KeySchemaElement().withAttributeName("ItemKey").withKeyType(KeyType.HASH))
        keySchema.add(KeySchemaElement().withAttributeName("Id").withKeyType(KeyType.RANGE))

        val timeIndex = LocalSecondaryIndex()
                .withIndexName("ItemKey-ItemTime-index")
                .withProjection(Projection().withProjectionType(ProjectionType.KEYS_ONLY))

        val indexKeySchema = ArrayList<KeySchemaElement>()

        indexKeySchema.add(KeySchemaElement()
                .withAttributeName("ItemKey")
                .withKeyType(KeyType.HASH))  //Partition key
        indexKeySchema.add(KeySchemaElement()
                .withAttributeName("ItemTime")
                .withKeyType(KeyType.RANGE))  //Sort key

        timeIndex.setKeySchema(indexKeySchema)

        val request = CreateTableRequest().withTableName(tableName).withKeySchema(keySchema)
                .withAttributeDefinitions(attributeDefinitions).withProvisionedThroughput(
                ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L))
                .withLocalSecondaryIndexes(timeIndex)

        val table = dynamoDb.createTable(request)

        table.waitForActive()
    }

    return dynamoDb.getTable(tableName)
}


class DynamoDbTimeValueTable(val type: String) : TimeValueTable, DynamoDbTable() {

    val table = getTimeValueTable(dynamoDb, "TimeValue")

    override fun insert(container: String, key: String, timeValues: List<TimeValue>) {
        val fullKey = "$type:$container:$key"

        var timeValueItems = mutableListOf<Item>()
        var count = 0
        for (timeValue in timeValues) {
            val item = Item().withPrimaryKey("ItemKey", fullKey, "Id", UUID.randomUUID().toString())
                    .withNumber("ItemTime", timeValue.time.time)
                    .withNumber("Received", System.currentTimeMillis())
                    .withBinary("ItemValue", ByteBuffer.wrap(timeValue.value))
            timeValueItems.add(item)
            count ++
            if (count == 25) {
                dynamoDb.batchWriteItem(TableWriteItems("TimeValue").withItemsToPut(timeValueItems))
                timeValueItems = mutableListOf<Item>()
                count = 0
            }
        }

        if (count > 0) {
            dynamoDb.batchWriteItem(TableWriteItems("TimeValue").withItemsToPut(timeValueItems))
        }
    }

    override fun select(beginId: UUID?, beginTime: Date, endTime_: Date, containers: List<String>, keys: List<String>): TimeValueResult {
        val container = containers[0]
        val key = keys[0]
        val fullKey = "$type:$container:$key"

        val spec = QuerySpec().withProjectionExpression("Id, Received, ItemTime, ItemKey, ItemValue")
                .withKeyConditionExpression("ItemKey = :v_key and ItemTime between :v_start_time and :v_end_time")
                .withMaxResultSize(1001)
                .withValueMap(ValueMap().withString(":v_key", fullKey).withNumber(":v_start_time", beginTime.time)
                        .withNumber(":v_end_time", endTime_.time))

        val rows = mutableListOf<TimeValueRow>()
        val index = table.getIndex("ItemKey-ItemTime-index")
        val result = index.query(spec)

        var beginIdFound = false

        for (item in result) {
            val id = item.getString("Id")
            val time = Date(item.getNumber("ItemTime").toLong())
            val received = Date(item.getNumber("Received").toLong())
            val value = item.getBinary("ItemValue")

            if (beginId != null && beginId.equals(id)) {
                beginIdFound = true
            }

            if (beginId != null && !beginIdFound) {
                continue
            }

            rows.add(TimeValueRow(id, container, key, time, received, value))
        }

        if (rows.count() == 1001) {
            val endTime = rows.last().time
            val nextBeginId = rows.last().id
            rows.removeAt(rows.lastIndex)
            return TimeValueResult(endTime, nextBeginId, rows)
        } else {
            return TimeValueResult(null, null, rows)
        }
    }

    override fun selectCount(beginTime: Date, endTime_: Date, containers: List<String>, keys: List<String>): TimeValueCountResult {
        val container = containers[0]
        val key = keys[0]
        val fullKey = "$type:$container:$key"

        val spec = QuerySpec().withKeyConditionExpression("ItemKey = :v_key and ItemTime between :v_start_time and :v_end_time").withSelect(Select.COUNT)

        val expressionAttributeValues = HashMap<String, AttributeValue>()
        expressionAttributeValues.put(":v_key", AttributeValue().withS(fullKey))
        expressionAttributeValues.put(":v_start_time", AttributeValue().withN(beginTime.time.toString()))
        expressionAttributeValues.put(":v_end_time", AttributeValue().withN(endTime_.time.toString()))

        val request = spec.request
        request.tableName = "TimeValue"
        request.indexName = "ItemKey-ItemTime-index"
        request.expressionAttributeNames = spec.nameMap
        request.expressionAttributeValues = expressionAttributeValues

        val result = client.query(request)
        val count = result.count

        return TimeValueCountResult(null, count.toLong())
    }

}