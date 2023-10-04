package com.slalom.build.shihao.eventsourcingpoc.product

import com.slalom.build.shihao.eventsourcingpoc.BadRequestException
import com.slalom.build.shihao.eventsourcingpoc.ResourceNotFoundException
import org.springframework.stereotype.Component

data class ProductPurchaseCommand(
        val quantity: Int
)

data class ProductPurchasedEvent(
        val quantity: Int
) {
    fun apply(product: Product, version: Int): Product {
        return product.copy(quantity = product.quantity - quantity, version = version)
    }
}

@Component
class ProductPurchaseCommandHandler(
        private val repository: ProductRepository
) {
    fun handle(sku: String, cmd: ProductPurchaseCommand) {
        val product = repository.getProduct(sku) ?: throw ResourceNotFoundException()
        if (product.quantity < cmd.quantity) {
            throw BadRequestException("Purchase quantity is more than the available quantity")
        }
        repository.addEvent(product, ProductPurchasedEvent(quantity = cmd.quantity))
    }
}