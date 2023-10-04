package com.slalom.build.shihao.eventsourcingpoc.product

import com.slalom.build.shihao.eventsourcingpoc.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/products")
class ProductApiController(
        private val createCommandHandler: ProductCreateCommandHandler,
        private val restockCommandHandler: ProductRestockCommandHandler,
        private val purchaseCommandHandler: ProductPurchaseCommandHandler,
        private val priceChangeCommandHandler: ProductPriceChangeCommandHandler,
        private val repository: ProductRepository
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(@RequestBody cmd: ProductCreateCommand) {
        createCommandHandler.handle(cmd)
    }

    @PostMapping("/{sku}/restock")
    fun restockProduct(@PathVariable sku: String, @RequestBody cmd: ProductRestockCommand) {
        restockCommandHandler.handle(sku, cmd)
    }

    @PostMapping("/{sku}/purchase")
    fun purchaseProduct(@PathVariable sku: String, @RequestBody cmd: ProductPurchaseCommand) {
        purchaseCommandHandler.handle(sku, cmd)
    }

    @PatchMapping("/{sku}")
    fun changeProductPrice(@PathVariable sku: String, @RequestBody cmd: ProductPriceChangeCommand) {
        priceChangeCommandHandler.handle(sku, cmd)
    }

    @GetMapping("/{sku}")
    fun getProduct(@PathVariable sku: String): Product =
            repository.getProduct(sku) ?: throw ResourceNotFoundException()
}