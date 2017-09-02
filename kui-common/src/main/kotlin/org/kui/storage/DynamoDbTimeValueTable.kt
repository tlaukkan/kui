package org.kui.storage

import com.amazonaws.services.dynamodbv2.document.Item
import org.kui.model.TimeValue
import org.kui.model.TimeValueCountResult
import org.kui.model.TimeValueResult
import java.util.*
import com.amazonaws.services.dynamodbv2.document.TableWriteItems
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.Select
import org.kui.model.TimeValueRow
import java.nio.ByteBuffer

class DynamoDbTimeValueTable(val type: String) : TimeValueTable, DynamoDbTable() {

    val table = dynamoDB.getTable("TimeValue")

    override fun insert(container: String, key: String, timeValues: List<TimeValue>) {
        val fullKey = "$type:$container:$key"
        val timeValueItems = mutableListOf<Item>()
        for (timeValue in timeValues) {
            val item = Item().withPrimaryKey("ItemKey", fullKey, "Id", UUID.randomUUID().toString())
                    .withNumber("ItemTime", timeValue.time.time)
                    .withNumber("Received", System.currentTimeMillis())
                    .withBinary("ItemValue", ByteBuffer.wrap(timeValue.value))
            timeValueItems.add(item)
        }
        dynamoDB.batchWriteItem(TableWriteItems("TimeValue").withItemsToPut(timeValueItems))
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