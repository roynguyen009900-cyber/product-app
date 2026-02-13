package com.example.productapp.service;

import com.example.productapp.model.Product;
import com.example.productapp.model.ProductVariant;
import com.example.productapp.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ProductFetchService {

    private static final Logger log = LoggerFactory.getLogger(ProductFetchService.class);

    private final ProductRepository productRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${product.fetch-url}")
    private String fetchUrl;

    @Value("${product.max-products}")
    private int maxProducts;

    public ProductFetchService(ProductRepository productRepository, ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.restClient = RestClient.create();
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 86400000, initialDelay = 5000) // Daily, 5 sec initial delay
    public void fetchProducts() {
        log.info("Starting scheduled product fetch from {}", fetchUrl);
        try {
            String response = restClient.get()
                    .uri(fetchUrl)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode productsNode = root.get("products");

            if (productsNode == null || !productsNode.isArray()) {
                log.warn("No products array found in response");
                return;
            }

            int count = 0;
            for (JsonNode productNode : productsNode) {
                if (count >= maxProducts) {
                    break;
                }

                try {
                    Product product = parseProduct(productNode);
                    Long productId = productRepository.upsertProduct(product);

                    // Parse and save variants
                    JsonNode variantsNode = productNode.get("variants");
                    if (variantsNode != null && variantsNode.isArray()) {
                        for (JsonNode variantNode : variantsNode) {
                            ProductVariant variant = parseVariant(variantNode, productId);
                            productRepository.saveVariant(variant);
                        }
                    }

                    count++;
                    log.debug("Saved product: {} (ID: {})", product.getTitle(), productId);
                } catch (Exception e) {
                    log.error("Error processing product: {}", e.getMessage());
                }
            }

            log.info("Finished fetching products. Total saved/updated: {}", count);
        } catch (Exception e) {
            log.error("Error fetching products from {}: {}", fetchUrl, e.getMessage(), e);
        }
    }

    public void fetchProductsManually() {
        fetchProducts();
    }

    private Product parseProduct(JsonNode node) {
        Product product = new Product();
        product.setExternalId(node.get("id").asLong());
        product.setTitle(node.get("title").asText());
        product.setHandle(node.has("handle") ? node.get("handle").asText() : null);
        product.setVendor(node.has("vendor") ? node.get("vendor").asText() : null);
        product.setProductType(node.has("product_type") ? node.get("product_type").asText() : null);

        // Get first image URL
        JsonNode images = node.get("images");
        if (images != null && images.isArray() && !images.isEmpty()) {
            product.setImageUrl(images.get(0).get("src").asText());
        }

        // Get price from first variant
        JsonNode variants = node.get("variants");
        if (variants != null && variants.isArray() && !variants.isEmpty()) {
            String priceStr = variants.get(0).get("price").asText();
            product.setPrice(new BigDecimal(priceStr));
        }

        product.setCreatedAt(LocalDateTime.now());
        return product;
    }

    private ProductVariant parseVariant(JsonNode node, Long productId) {
        ProductVariant variant = new ProductVariant();
        variant.setProductId(productId);
        variant.setExternalId(node.get("id").asLong());
        variant.setTitle(node.has("title") ? node.get("title").asText() : null);
        variant.setSku(node.has("sku") ? node.get("sku").asText() : null);
        variant.setPrice(new BigDecimal(node.get("price").asText()));
        variant.setAvailable(node.has("available") && node.get("available").asBoolean());
        return variant;
    }
}
