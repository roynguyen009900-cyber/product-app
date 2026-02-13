package com.example.productapp.controller;

import com.example.productapp.model.Product;
import com.example.productapp.model.ProductVariant;
import com.example.productapp.service.ProductFetchService;
import com.example.productapp.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ProductController {

    private final ProductService productService;
    private final ProductFetchService productFetchService;

    public ProductController(ProductService productService, ProductFetchService productFetchService) {
        this.productService = productService;
        this.productFetchService = productFetchService;
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

    @PostMapping("/products/fetch")
    public String fetchProducts(Model model) {
        productFetchService.fetchProductsManually();
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "fragments/product-table :: productTable";
    }
}
