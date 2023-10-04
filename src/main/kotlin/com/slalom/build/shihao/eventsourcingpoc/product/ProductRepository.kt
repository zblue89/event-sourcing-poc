package com.slalom.build.shihao.eventsourcingpoc.product

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.andrewohara.dynamokt.DataClassTableSchema
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.net.URI
import java.time.Instant

@Component
class ProductRepository(
        private val objectMapper: ObjectMapper,
        private val simpMessagingTemplate: SimpMessagingTemplate
) {

    private val ddb: DynamoDbClient = DynamoDbClient.builder()
            .region(Region.AP_SOUTHEAST_1)
            .credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("x", "x")
                    )
            )
            .endpointOverride(URI.create("http://localhost:4566"))
            .build()

    private val enhancedClient: DynamoDbEnhancedClient = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(ddb)
            .build()

    private val eventTable: DynamoDbTable<ProductEvent> = enhancedClient.table("ProductEvents", DataClassTableSchema(ProductEvent::class))
    private val snapshotTable: DynamoDbTable<ProductSnapshot> = enhancedClient.table("ProductSnapshot", DataClassTableSchema(ProductSnapshot::class))

    init {
        eventTable.createTable()
        snapshotTable.createTable()
        ddb.waiter().waitUntilTableExists {
            it.tableName(eventTable.tableName())
        }
        ddb.waiter().waitUntilTableExists {
            it.tableName(snapshotTable.tableName())
        }
    }

    fun addProduct(product: Product, eventPayload: Any){
        eventTable.putItem(ProductEvent(
                sku = product.sku,
                version = 1,
                name = eventPayload.javaClass.simpleName,
                payload = objectMapper.writeValueAsString(eventPayload),
                createdAt = Instant.now()
        ))
        snapshotTable.putItem(ProductSnapshot(
                sku = product.sku,
                version = 1,
                data = objectMapper.writeValueAsString(product)
        ))
    }

    fun addEvent(product: Product, eventPayload: Any) {
        val event = ProductEvent(
                sku = product.sku,
                version = product.version + 1,
                name = eventPayload.javaClass.simpleName,
                payload = objectMapper.writeValueAsString(eventPayload),
                createdAt = Instant.now()
        )
        eventTable.putItem(event)
        simpMessagingTemplate.convertAndSend("/topic/products/${event.sku}", event)
    }

    fun getProduct(sku: String): Product? {
        val latestSnapshotPage = snapshotTable.query { builder ->
            builder.queryConditional(
                    QueryConditional.keyEqualTo { keyBuilder ->
                        keyBuilder.partitionValue(sku)
                    }
            ).limit(1).scanIndexForward(false)
        }
        if (latestSnapshotPage.first().items().isEmpty()) {
            return null
        }
        val latestSnapshot = latestSnapshotPage.first().items().first()
        val result = eventTable.query { builder ->
            builder.queryConditional(
                    QueryConditional.sortGreaterThan { keyBuilder ->
                        keyBuilder.partitionValue(sku).sortValue(latestSnapshot.version)
                    }
            )
        }
        var updatedProduct: Product = objectMapper.readValue(latestSnapshot.data)
        result.forEach {
            it.items().forEach { event ->
                updatedProduct = when (event.name) {
                    "ProductRestockedEvent" -> objectMapper.readValue<ProductRestockedEvent>(event.payload)
                            .apply(updatedProduct, event.version)
                    "ProductPurchasedEvent" -> objectMapper.readValue<ProductPurchasedEvent>(event.payload)
                            .apply(updatedProduct, event.version)
                    "ProductPriceChangedEvent" -> objectMapper.readValue<ProductPriceChangedEvent>(event.payload)
                            .apply(updatedProduct, event.version)
                    else -> updatedProduct
                }
            }
        }

        if (updatedProduct.version - latestSnapshot.version > 10) {
            snapshotTable.putItem(ProductSnapshot(
                    sku= sku,
                    version= updatedProduct.version,
                    data=objectMapper.writeValueAsString(updatedProduct))
            )
        }
        return updatedProduct
    }
}