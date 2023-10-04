package com.slalom.build.shihao.eventsourcingpoc.product

import com.slalom.build.shihao.eventsourcingpoc.ResourceNotFoundException
import org.springframework.stereotype.Component

data class ProductPriceChangeCommand(
        val price: Int
)

data class ProductPriceChangedEvent(
        val price: Int
) {
    fun apply(product: Product, version: Int): Product {
        return product.copy(price = price, version = version)
    }
}

@Component
class ProductPriceChangeCommandHandler(
        private val repository: ProductRepository
) {
    fun handle(sku: String, cmd: ProductPriceChangeCommand) {
        val product = repository.getProduct(sku) ?: throw ResourceNotFoundException()
        repository.addEvent(product, ProductPriceChangedEvent(price = cmd.price))
    }
}