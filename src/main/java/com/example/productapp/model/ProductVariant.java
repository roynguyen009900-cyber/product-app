package com.example.productapp.model;

import java.math.BigDecimal;

public class ProductVariant {

    private Long id;
    private Long productId;
    private Long externalId;
    private String title;
    private String sku;
    private BigDecimal price;
    private Boolean available;

    public ProductVariant() {
    }

    public ProductVariant(Long id, Long productId, Long externalId, String title, String sku,
            BigDecimal price, Boolean available) {
        this.id = id;
        this.productId = productId;
        this.externalId = externalId;
        this.title = title;
        this.sku = sku;
        this.price = price;
        this.available = available;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getExternalId() {
        return externalId;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
