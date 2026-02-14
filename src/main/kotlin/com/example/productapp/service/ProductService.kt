package com.example.productapp.service

import com.example.productapp.model.Product
import com.example.productapp.model.ProductVariant
import com.example.productapp.repository.ProductRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class ProductService(private val productRepository: ProductRepository) {

    fun getAllProducts(): List<Product> = productRepository.findAll()

    fun getProductById(id: Long): Optional<Product> {
        val product = productRepository.findById(id)
        product.ifPresent { p ->
            p.variants = productRepository.findVariantsByProductId(p.id!!).toMutableList()
        }
        return product
    }

    fun searchProducts(query: String?): List<Product> {
        return if (query.isNullOrBlank()) {
            productRepository.findAll()
        } else {
            productRepository.searchByTitle(query.trim())
        }
    }

    fun addProduct(
        title: String,
        vendor: String?,
        productType: String?,
        price: BigDecimal?,
        variants: List<ProductVariant>?
    ): Product {
        val product = Product(
            title = title,
            vendor = vendor,
            productType = productType,
            price = price,
            createdAt = LocalDateTime.now()
        )

        val id = productRepository.save(product)
        product.id = id

        variants?.forEach { variant ->
            variant.productId = id
            productRepository.saveVariantManual(variant)
        }
        product.variants = variants?.toMutableList() ?: mutableListOf()

        return product
    }

    fun updateProduct(id: Long, title: String, vendor: String?, productType: String?, price: BigDecimal?) {
        val product = Product(
            id = id,
            title = title,
            vendor = vendor,
            productType = productType,
            price = price
        )
        productRepository.update(product)
    }

    fun deleteProduct(id: Long) {
        productRepository.deleteById(id)
    }

    fun toggleVariantAvailability(variantId: Long): Optional<ProductVariant> {
        productRepository.toggleVariantAvailability(variantId)
        return productRepository.findVariantById(variantId)
    }

    fun getProductCount(): Long = productRepository.count()
}
