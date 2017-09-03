package org.kui.storage

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.model.*
import org.kui.util.getProperty
import java.util.ArrayList

open class DynamoDbTable {
    val client = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials(
                    getProperty("dynamodb", "storage.username"),
                    getProperty("dynamodb", "storage.password"))))
            .withRegion(getProperty("dynamodb", "storage.region"))
            .build()
    val dynamoDb = DynamoDB(client)

}