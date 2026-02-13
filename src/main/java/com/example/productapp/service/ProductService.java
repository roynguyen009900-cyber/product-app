package com.example.productapp.service;

import com.example.productapp.model.Product;
import com.example.productapp.model.ProductVariant;
import com.example.productapp.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        product.ifPresent(p -> p.setVariants(productRepository.findVariantsByProductId(p.getId())));
        return product;
    }

    public List<Product> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return productRepository.findAll();
        }
        return productRepository.searchByTitle(query.trim());
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

    public void updateProduct(Long id, String title, String vendor, String productType, BigDecimal price) {
        Product product = new Product();
        product.setId(id);
        product.setTitle(title);
        product.setVendor(vendor);
        product.setProductType(productType);
        product.setPrice(price);
        productRepository.update(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public Optional<ProductVariant> toggleVariantAvailability(Long variantId) {
        productRepository.toggleVariantAvailability(variantId);
        return productRepository.findVariantById(variantId);
    }

    public long getProductCount() {
        return productRepository.count();
    }
}
