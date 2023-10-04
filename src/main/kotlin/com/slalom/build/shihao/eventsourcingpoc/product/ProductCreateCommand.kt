package com.slalom.build.shihao.eventsourcingpoc.product

import org.springframework.stereotype.Component

data class ProductCreateCommand(
        val sku: String,
        val name: String,
        val price: Int
)

data class ProductCreatedEvent(
        val sku: String,
        val name: String,
        val price: Int
)

@Component
class ProductCreateCommandHandler(
        private val repository: ProductRepository
) {
    fun handle(cmd: ProductCreateCommand) {
        repository.addProduct(Product(
                sku = cmd.sku,
                name = cmd.name,
                price = cmd.price,
                quantity = 0,
                version = 1
        ), ProductCreatedEvent(
                sku = cmd.sku,
                name = cmd.name,
                price = cmd.price
        ))
    }
}