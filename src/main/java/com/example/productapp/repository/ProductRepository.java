package com.example.productapp.repository;

import com.example.productapp.model.Product;
import com.example.productapp.model.ProductVariant;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {

    private final JdbcClient jdbcClient;

    public ProductRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Product> findAll() {
        List<Product> products = jdbcClient.sql("""
                SELECT id, external_id, title, handle, vendor, product_type, image_url, price, created_at
                FROM products ORDER BY created_at DESC
                """)
                .query((rs, rowNum) -> mapProduct(rs))
                .list();

        for (Product product : products) {
            List<ProductVariant> variants = findVariantsByProductId(product.getId());
            product.setVariants(variants);
        }

        return products;
    }

    public Optional<Product> findById(Long id) {
        return jdbcClient.sql("""
                SELECT id, external_id, title, handle, vendor, product_type, image_url, price, created_at
                FROM products WHERE id = :id
                """)
                .param("id", id)
                .query((rs, rowNum) -> mapProduct(rs))
                .optional();
    }

    public boolean existsByExternalId(Long externalId) {
        return jdbcClient.sql("SELECT COUNT(*) FROM products WHERE external_id = :externalId")
                .param("externalId", externalId)
                .query(Integer.class)
                .single() > 0;
    }

    public Long save(Product product) {
        return jdbcClient.sql("""
                INSERT INTO products (external_id, title, handle, vendor, product_type, image_url, price, created_at)
                VALUES (:externalId, :title, :handle, :vendor, :productType, :imageUrl, :price, :createdAt)
                RETURNING id
                """)
                .param("externalId", product.getExternalId())
                .param("title", product.getTitle())
                .param("handle", product.getHandle())
                .param("vendor", product.getVendor())
                .param("productType", product.getProductType())
                .param("imageUrl", product.getImageUrl())
                .param("price", product.getPrice())
                .param("createdAt", product.getCreatedAt() != null ? product.getCreatedAt() : LocalDateTime.now())
                .query(Long.class)
                .single();
    }

    public Long upsertProduct(Product product) {
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
                """)
                .param("externalId", product.getExternalId())
                .param("title", product.getTitle())
                .param("handle", product.getHandle())
                .param("vendor", product.getVendor())
                .param("productType", product.getProductType())
                .param("imageUrl", product.getImageUrl())
                .param("price", product.getPrice())
                .param("createdAt", product.getCreatedAt() != null ? product.getCreatedAt() : LocalDateTime.now())
                .query(Long.class)
                .single();
    }

    public List<ProductVariant> findVariantsByProductId(Long productId) {
        return jdbcClient.sql("""
                SELECT id, product_id, external_id, title, sku, price, available
                FROM product_variants WHERE product_id = :productId ORDER BY id
                """)
                .param("productId", productId)
                .query((rs, rowNum) -> mapVariant(rs))
                .list();
    }

    public void saveVariant(ProductVariant variant) {
        jdbcClient
                .sql("""
                        INSERT INTO product_variants (product_id, external_id, title, sku, price, available)
                        VALUES (:productId, :externalId, :title, :sku, :price, :available)
                        ON CONFLICT (external_id) DO UPDATE SET
                            title = EXCLUDED.title,
                            sku = EXCLUDED.sku,
                            price = EXCLUDED.price,
                            available = EXCLUDED.available
                        """)
                .param("productId", variant.getProductId())
                .param("externalId", variant.getExternalId())
                .param("title", variant.getTitle())
                .param("sku", variant.getSku())
                .param("price", variant.getPrice())
                .param("available", variant.getAvailable())
                .update();
    }

    public void saveVariantManual(ProductVariant variant) {
        jdbcClient.sql("""
                INSERT INTO product_variants (product_id, title, sku, price, available)
                VALUES (:productId, :title, :sku, :price, :available)
                """)
                .param("productId", variant.getProductId())
                .param("title", variant.getTitle())
                .param("sku", variant.getSku())
                .param("price", variant.getPrice())
                .param("available", variant.getAvailable())
                .update();
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM products")
                .query(Long.class)
                .single();
    }

    public List<Product> searchByTitle(String query) {
        List<Product> products = jdbcClient.sql("""
                SELECT id, external_id, title, handle, vendor, product_type, image_url, price, created_at
                FROM products WHERE title ILIKE :query ORDER BY created_at DESC
                """)
                .param("query", "%" + query + "%")
                .query((rs, rowNum) -> mapProduct(rs))
                .list();

        for (Product product : products) {
            List<ProductVariant> variants = findVariantsByProductId(product.getId());
            product.setVariants(variants);
        }

        return products;
    }

    public void update(Product product) {
        jdbcClient.sql("""
                UPDATE products SET title = :title, vendor = :vendor, product_type = :productType, price = :price
                WHERE id = :id
                """)
                .param("id", product.getId())
                .param("title", product.getTitle())
                .param("vendor", product.getVendor())
                .param("productType", product.getProductType())
                .param("price", product.getPrice())
                .update();
    }

    public void deleteById(Long id) {
        jdbcClient.sql("DELETE FROM products WHERE id = :id")
                .param("id", id)
                .update();
    }

    public Optional<ProductVariant> findVariantById(Long variantId) {
        return jdbcClient.sql("""
                SELECT id, product_id, external_id, title, sku, price, available
                FROM product_variants WHERE id = :id
                """)
                .param("id", variantId)
                .query((rs, rowNum) -> mapVariant(rs))
                .optional();
    }

    public void toggleVariantAvailability(Long variantId) {
        jdbcClient.sql("UPDATE product_variants SET available = NOT available WHERE id = :id")
                .param("id", variantId)
                .update();
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setExternalId(rs.getObject("external_id", Long.class));
        p.setTitle(rs.getString("title"));
        p.setHandle(rs.getString("handle"));
        p.setVendor(rs.getString("vendor"));
        p.setProductType(rs.getString("product_type"));
        p.setImageUrl(rs.getString("image_url"));
        BigDecimal price = rs.getBigDecimal("price");
        p.setPrice(price);
        p.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        return p;
    }

    private ProductVariant mapVariant(ResultSet rs) throws SQLException {
        ProductVariant v = new ProductVariant();
        v.setId(rs.getLong("id"));
        v.setProductId(rs.getLong("product_id"));
        v.setExternalId(rs.getObject("external_id", Long.class));
        v.setTitle(rs.getString("title"));
        v.setSku(rs.getString("sku"));
        v.setPrice(rs.getBigDecimal("price"));
        v.setAvailable(rs.getBoolean("available"));
        return v;
    }
}
