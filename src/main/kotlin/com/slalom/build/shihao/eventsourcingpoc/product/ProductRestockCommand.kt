package com.slalom.build.shihao.eventsourcingpoc.product

import com.slalom.build.shihao.eventsourcingpoc.ResourceNotFoundException
import org.springframework.stereotype.Component

data class ProductRestockCommand(
        val quantity: Int
)

data class ProductRestockedEvent(
        val quantity: Int
) {
    fun apply(product: Product, version: Int): Product {
        return product.copy(quantity = product.quantity + quantity, version = version)
    }
}

@Component
class ProductRestockCommandHandler(
        private val repository: ProductRepository
) {
    fun handle(sku: String, cmd: ProductRestockCommand) {
        val product = repository.getProduct(sku) ?: throw ResourceNotFoundException()
        repository.addEvent(product, ProductRestockedEvent(quantity = cmd.quantity))
    }
}