package com.example.productapp.service;

import com.example.productapp.model.Product;
import com.example.productapp.model.ProductVariant;
import com.example.productapp.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product addProduct(String title, String vendor, String productType, BigDecimal price,
            List<ProductVariant> variants) {
        Product product = new Product();
        product.setTitle(title);
        product.setVendor(vendor);
        product.setProductType(productType);
        product.setPrice(price);
        product.setCreatedAt(LocalDateTime.now());

        Long id = productRepository.save(product);
        product.setId(id);

        if (variants != null) {
            for (ProductVariant variant : variants) {
                variant.setProductId(id);
                productRepository.saveVariantManual(variant);
            }
            product.setVariants(variants);
        }

        return product;
    }

    public long getProductCount() {
        return productRepository.count();
    }
}
