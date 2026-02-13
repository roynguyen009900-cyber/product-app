package com.example.productapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Product {

    private Long id;
    private Long externalId;
    private String title;
    private String handle;
    private String vendor;
    private String productType;
    private String imageUrl;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private List<ProductVariant> variants = new ArrayList<>();

    public Product() {
    }

    public Product(Long id, Long externalId, String title, String handle, String vendor,
            String productType, String imageUrl, BigDecimal price, LocalDateTime createdAt) {
        this.id = id;
        this.externalId = externalId;
        this.title = title;
        this.handle = handle;
        this.vendor = vendor;
        this.productType = productType;
        this.imageUrl = imageUrl;
        this.price = price;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ProductVariant> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariant> variants) {
        this.variants = variants;
    }
}
