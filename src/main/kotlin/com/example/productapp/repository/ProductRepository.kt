package com.example.productapp.repository

import com.example.productapp.model.Product
import com.example.productapp.model.ProductVariant
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.*

@Repository
class ProductRepository(private val jdbcClient: JdbcClient) {

    fun findAll(): List<Product> {
        val products = jdbcClient.sql("""
            SELECT id, external_id, title, handle, vendor, product_type, image_url, price, created_at
            FROM products ORDER BY created_at DESC
        """.trimIndent())
            .query { rs, _ -> mapProduct(rs) }
            .list()

        for (product in products) {
            val variants = findVariantsByProductId(product.id!!)
            product.variants = variants.toMutableList()
        }
        return products
    }

    fun findById(id: Long): Optional<Product> {
        return jdbcClient.sql("""
            SELECT id, external_id, title, handle, vendor, product_type, image_url, price, created_at
            FROM products WHERE id = :id
        """.trimIndent())
            .param("id", id)
            .query { rs, _ -> mapProduct(rs) }
            .optional()
    }

    fun existsByExternalId(externalId: Long): Boolean {
        return jdbcClient.sql("SELECT COUNT(*) FROM products WHERE external_id = :externalId")
            .param("externalId", externalId)
            .query(Int::class.java)
            .single() > 0
    }

    fun save(product: Product): Long {
        return jdbcClient.sql("""
            INSERT INTO products (external_id, title, handle, vendor, product_type, image_url, price, created_at)
            VALUES (:externalId, :title, :handle, :vendor, :productType, :imageUrl, :price, :createdAt)
            RETURNING id
        """.trimIndent())
            .param("externalId", product.externalId)
            .param("title", product.title)
            .param("handle", product.handle)
            .param("vendor", product.vendor)
            .param("productType", product.productType)
            .param("imageUrl", product.imageUrl)
            .param("price", product.price)
            .param("createdAt", product.createdAt ?: LocalDateTime.now())
            .query(Long::class.java)
            .single()
    }

    fun upsertProduct(product: Product): Long {
        return jdbcClient.sql("""
            INSERT INTO products (external_id, title, handle, vendor, product_type, image_url, price, created_at)
            VALUES (:externalId, :title, :handle, :vendor, :productType, :imageUrl, :price, :createdAt)
            ON CONFLICT (external_id) DO UPDATE SET
                title = EXCLUDED.title,
                handle = EXCLUDED.handle,
                vendor = EXCLUDED.vendor,
                product_type = EXCLUDED.product_type,
                image_url = EXCLUDED.image_url,
                price = EXCLUDED.price
            RETURNING id
        """.trimIndent())
            .param("externalId", product.externalId)
            .param("title", product.title)
            .param("handle", product.handle)
            .param("vendor", product.vendor)
            .param("productType", product.productType)
            .param("imageUrl", product.imageUrl)
            .param("price", product.price)
            .param("createdAt", product.createdAt ?: LocalDateTime.now())
            .query(Long::class.java)
            .single()
    }

    fun findVariantsByProductId(productId: Long): List<ProductVariant> {
        return jdbcClient.sql("""
            SELECT id, product_id, external_id, title, sku, price, available
            FROM product_variants WHERE product_id = :productId ORDER BY id
        """.trimIndent())
            .param("productId", productId)
            .query { rs, _ -> mapVariant(rs) }
            .list()
    }

    fun saveVariant(variant: ProductVariant) {
        jdbcClient.sql("""
            INSERT INTO product_variants (product_id, external_id, title, sku, price, available)
            VALUES (:productId, :externalId, :title, :sku, :price, :available)
            ON CONFLICT (external_id) DO UPDATE SET
                title = EXCLUDED.title,
                sku = EXCLUDED.sku,
                price = EXCLUDED.price,
                available = EXCLUDED.available
        """.trimIndent())
            .param("productId", variant.productId)
            .param("externalId", variant.externalId)
            .param("title", variant.title)
            .param("sku", variant.sku)
            .param("price", variant.price)
            .param("available", variant.available)
            .update()
    }

    fun saveVariantManual(variant: ProductVariant) {
        jdbcClient.sql("""
            INSERT INTO product_variants (product_id, title, sku, price, available)
            VALUES (:productId, :title, :sku, :price, :available)
        """.trimIndent())
            .param("productId", variant.productId)
            .param("title", variant.title)
            .param("sku", variant.sku)
            .param("price", variant.price)
            .param("available", variant.available)
            .update()
    }

    fun count(): Long {
        return jdbcClient.sql("SELECT COUNT(*) FROM products")
            .query(Long::class.java)
            .single()
    }

    fun searchByTitle(query: String): List<Product> {
        val products = jdbcClient.sql("""
            SELECT id, external_id, title, handle, vendor, product_type, image_url, price, created_at
            FROM products WHERE title ILIKE :query ORDER BY created_at DESC
        """.trimIndent())
            .param("query", "%$query%")
            .query { rs, _ -> mapProduct(rs) }
            .list()

        for (product in products) {
            val variants = findVariantsByProductId(product.id!!)
            product.variants = variants.toMutableList()
        }
        return products
    }

    fun update(product: Product) {
        jdbcClient.sql("""
            UPDATE products SET title = :title, vendor = :vendor, product_type = :productType, price = :price
            WHERE id = :id
        """.trimIndent())
            .param("id", product.id)
            .param("title", product.title)
            .param("vendor", product.vendor)
            .param("productType", product.productType)
            .param("price", product.price)
            .update()
    }

    fun deleteById(id: Long) {
        jdbcClient.sql("DELETE FROM products WHERE id = :id")
            .param("id", id)
            .update()
    }

    fun findVariantById(variantId: Long): Optional<ProductVariant> {
        return jdbcClient.sql("""
            SELECT id, product_id, external_id, title, sku, price, available
            FROM product_variants WHERE id = :id
        """.trimIndent())
            .param("id", variantId)
            .query { rs, _ -> mapVariant(rs) }
            .optional()
    }

    fun toggleVariantAvailability(variantId: Long) {
        jdbcClient.sql("UPDATE product_variants SET available = NOT available WHERE id = :id")
            .param("id", variantId)
            .update()
    }

    private fun mapProduct(rs: ResultSet): Product {
        val externalIdRaw = rs.getObject("external_id")
        return Product(
            id = rs.getLong("id"),
            externalId = (externalIdRaw as? Number)?.toLong(),
            title = rs.getString("title"),
            handle = rs.getString("handle"),
            vendor = rs.getString("vendor"),
            productType = rs.getString("product_type"),
            imageUrl = rs.getString("image_url"),
            price = rs.getBigDecimal("price"),
            createdAt = rs.getTimestamp("created_at")?.toLocalDateTime()
        )
    }

    private fun mapVariant(rs: ResultSet): ProductVariant {
        val externalIdRaw = rs.getObject("external_id")
        return ProductVariant(
            id = rs.getLong("id"),
            productId = rs.getLong("product_id"),
            externalId = (externalIdRaw as? Number)?.toLong(),
            title = rs.getString("title"),
            sku = rs.getString("sku"),
            price = rs.getBigDecimal("price"),
            available = rs.getBoolean("available")
        )
    }
}
