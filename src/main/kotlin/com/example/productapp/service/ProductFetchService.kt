package com.example.productapp.service

import com.example.productapp.model.Product
import com.example.productapp.model.ProductVariant
import com.example.productapp.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class ProductFetchService(
    private val productRepository: ProductRepository,
    private val objectMapper: ObjectMapper
) {

    private val log = LoggerFactory.getLogger(ProductFetchService::class.java)
    private val restClient: RestClient = RestClient.create()

    @Value("\${product.fetch-url}")
    private lateinit var fetchUrl: String

    @Value("\${product.max-products}")
    private var maxProducts: Int = 50

    @Scheduled(fixedDelay = 86400000, initialDelay = 5000)
    fun fetchProducts() {
        log.info("Starting scheduled product fetch from {}", fetchUrl)
        try {
            val response = restClient.get()
                .uri(fetchUrl)
                .retrieve()
                .body(String::class.java)

            val root = objectMapper.readTree(response)
            val productsNode = root.get("products")

            if (productsNode == null || !productsNode.isArray) {
                log.warn("No products array found in response")
                return
            }

            var count = 0
            for (productNode in productsNode) {
                if (count >= maxProducts) break

                try {
                    val product = parseProduct(productNode)
                    val productId = productRepository.upsertProduct(product)

                    val variantsNode = productNode.get("variants")
                    if (variantsNode != null && variantsNode.isArray) {
                        for (variantNode in variantsNode) {
                            val variant = parseVariant(variantNode, productId)
                            productRepository.saveVariant(variant)
                        }
                    }

                    count++
                    log.debug("Saved product: {} (ID: {})", product.title, productId)
                } catch (e: Exception) {
                    log.error("Error processing product: {}", e.message)
                }
            }

            log.info("Finished fetching products. Total saved/updated: {}", count)
        } catch (e: Exception) {
            log.error("Error fetching products from {}: {}", fetchUrl, e.message, e)
        }
    }

    private fun parseProduct(node: JsonNode): Product {
        val images = node.get("images")
        val imageUrl = if (images != null && images.isArray && !images.isEmpty) {
            images.get(0).get("src").stringValue()
        } else null

        val variants = node.get("variants")
        val price = if (variants != null && variants.isArray && !variants.isEmpty) {
            BigDecimal(variants.get(0).get("price").stringValue())
        } else null

        return Product(
            externalId = node.get("id").asLong(),
            title = node.get("title").stringValue(),
            handle = if (node.has("handle")) node.get("handle").stringValue() else null,
            vendor = if (node.has("vendor")) node.get("vendor").stringValue() else null,
            productType = if (node.has("product_type")) node.get("product_type").stringValue() else null,
            imageUrl = imageUrl,
            price = price,
            createdAt = LocalDateTime.now()
        )
    }

    private fun parseVariant(node: JsonNode, productId: Long): ProductVariant {
        return ProductVariant(
            productId = productId,
            externalId = node.get("id").asLong(),
            title = if (node.has("title")) node.get("title").stringValue() else null,
            sku = if (node.has("sku")) node.get("sku").stringValue() else null,
            price = BigDecimal(node.get("price").stringValue()),
            available = node.has("available") && node.get("available").asBoolean()
        )
    }
}
