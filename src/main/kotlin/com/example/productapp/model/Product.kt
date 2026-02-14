package com.example.productapp.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Product(
    var id: Long? = null,
    var externalId: Long? = null,
    var title: String? = null,
    var handle: String? = null,
    var vendor: String? = null,
    var productType: String? = null,
    var imageUrl: String? = null,
    var price: BigDecimal? = null,
    var createdAt: LocalDateTime? = null,
    var variants: MutableList<ProductVariant> = mutableListOf()
)
