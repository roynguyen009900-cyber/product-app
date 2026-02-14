package com.example.productapp.model

import java.math.BigDecimal

data class ProductVariant(
    var id: Long? = null,
    var productId: Long? = null,
    var externalId: Long? = null,
    var title: String? = null,
    var sku: String? = null,
    var price: BigDecimal? = null,
    var available: Boolean? = null
)
