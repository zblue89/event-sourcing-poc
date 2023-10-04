package com.slalom.build.shihao.eventsourcingpoc.product

import io.andrewohara.dynamokt.DynamoKtPartitionKey
import io.andrewohara.dynamokt.DynamoKtSortKey
import java.time.Instant

data class ProductEvent(
        @DynamoKtPartitionKey
        val sku: String,
        @DynamoKtSortKey
        val version: Int,
        val name: String,
        val payload: String,
        val createdAt: Instant
)