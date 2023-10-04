package com.slalom.build.shihao.eventsourcingpoc.product

data class Product(
        val sku: String,
        val name: String,
        val price: Int,
        val quantity: Int,
        val version: Int
)