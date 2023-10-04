package com.slalom.build.shihao.eventsourcingpoc.product

import io.andrewohara.dynamokt.DynamoKtPartitionKey
import io.andrewohara.dynamokt.DynamoKtSortKey

data class ProductSnapshot(
        @DynamoKtPartitionKey
        val sku: String,
        @DynamoKtSortKey
        val version: Int,
        val data: String
)
