package com.example.productapp.controller;

import com.example.productapp.model.Product;
import com.example.productapp.model.ProductVariant;

import com.example.productapp.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("productCount", productService.getProductCount());
        return "index";
    }

    @GetMapping("/products")
    public String loadProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "fragments/product-table :: productTable";
    }

    @PostMapping("/products")
    public String addProduct(@RequestParam String title,
            @RequestParam(required = false) String vendor,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) BigDecimal price,
            @RequestParam(required = false) List<String> variantTitles,
            @RequestParam(required = false) List<String> variantSkus,
            @RequestParam(required = false) List<BigDecimal> variantPrices,
            @RequestParam(required = false) List<Boolean> variantAvailables,
            Model model) {

        List<ProductVariant> variants = new ArrayList<>();
        if (variantTitles != null) {
            for (int i = 0; i < variantTitles.size(); i++) {
                String vTitle = variantTitles.get(i);
                if (vTitle != null && !vTitle.isBlank()) {
                    ProductVariant v = new ProductVariant();
                    v.setTitle(vTitle);
                    v.setSku(variantSkus != null && i < variantSkus.size() ? variantSkus.get(i) : null);
                    v.setPrice(variantPrices != null && i < variantPrices.size() ? variantPrices.get(i) : price);
                    v.setAvailable(variantAvailables != null && i < variantAvailables.size() ? variantAvailables.get(i)
                            : true);
                    variants.add(v);
                }
            }
        }

        productService.addProduct(title, vendor, productType, price, variants);
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "fragments/product-table :: productTable";
    }

    // ===== Search =====

    @GetMapping("/search")
    public String searchPage() {
        return "search";
    }

    @GetMapping("/search/results")
    public String searchResults(@RequestParam(name = "q", defaultValue = "") String query, Model model) {
        List<Product> products = productService.searchProducts(query);
        model.addAttribute("products", products);
        return "fragments/product-table :: productTable";
    }

    // ===== Edit Product =====

    @GetMapping("/products/{id}/edit")
    public String editProductPage(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        model.addAttribute("product", product);
        return "edit-product";
    }

    @PostMapping("/products/{id}")
    public String updateProduct(@PathVariable Long id,
            @RequestParam String title,
            @RequestParam(required = false) String vendor,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) BigDecimal price,
            Model model) {
        productService.updateProduct(id, title, vendor, productType, price);
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        model.addAttribute("product", product);
        model.addAttribute("successMessage", "Product updated successfully!");
        return "edit-product";
    }

    // ===== Delete Product =====

    @DeleteMapping("/products/{id}")
    public String deleteProduct(@PathVariable Long id, Model model) {
        productService.deleteProduct(id);
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "fragments/product-table :: productTable";
    }

    // ===== Toggle Variant Availability =====

    @PutMapping("/variants/{id}/toggle-availability")
    public String toggleVariantAvailability(@PathVariable Long id, Model model) {
        ProductVariant variant = productService.toggleVariantAvailability(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variant not found"));
        model.addAttribute("variant", variant);
        return "fragments/variant-availability :: variantAvailability";
    }
}
