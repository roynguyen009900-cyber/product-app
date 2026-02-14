package com.example.productapp.controller

import com.example.productapp.model.ProductVariant
import com.example.productapp.service.ProductService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.math.BigDecimal

@Controller
class ProductController(private val productService: ProductService) {

    @GetMapping("/")
    fun index(model: Model): String {
        model.addAttribute("productCount", productService.getProductCount())
        return "index"
    }

    @GetMapping("/products")
    fun loadProducts(model: Model): String {
        val products = productService.getAllProducts()
        model.addAttribute("products", products)
        return "fragments/product-table :: productTable"
    }

    @PostMapping("/products")
    fun addProduct(
        @RequestParam title: String,
        @RequestParam(required = false) vendor: String?,
        @RequestParam(required = false) productType: String?,
        @RequestParam(required = false) price: BigDecimal?,
        @RequestParam(required = false) variantTitles: List<String>?,
        @RequestParam(required = false) variantSkus: List<String>?,
        @RequestParam(required = false) variantPrices: List<BigDecimal>?,
        @RequestParam(required = false) variantAvailables: List<Boolean>?,
        model: Model
    ): String {
        val variants = mutableListOf<ProductVariant>()
        variantTitles?.forEachIndexed { i, vTitle ->
            if (vTitle.isNotBlank()) {
                val variant = ProductVariant(
                    title = vTitle,
                    sku = variantSkus?.getOrNull(i),
                    price = variantPrices?.getOrNull(i) ?: price,
                    available = variantAvailables?.getOrNull(i) ?: true
                )
                variants.add(variant)
            }
        }

        productService.addProduct(title, vendor, productType, price, variants)
        val products = productService.getAllProducts()
        model.addAttribute("products", products)
        return "fragments/product-table :: productTable"
    }

    // ===== Search =====

    @GetMapping("/search")
    fun searchPage(): String = "search"

    @GetMapping("/search/results")
    fun searchResults(@RequestParam(name = "q", defaultValue = "") query: String, model: Model): String {
        val products = productService.searchProducts(query)
        model.addAttribute("products", products)
        return "fragments/product-table :: productTable"
    }

    // ===== Edit Product =====

    @GetMapping("/products/{id}/edit")
    fun editProductPage(@PathVariable id: Long, model: Model): String {
        val product = productService.getProductById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found") }
        model.addAttribute("product", product)
        return "edit-product"
    }

    @PostMapping("/products/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @RequestParam title: String,
        @RequestParam(required = false) vendor: String?,
        @RequestParam(required = false) productType: String?,
        @RequestParam(required = false) price: BigDecimal?,
        redirectAttributes: RedirectAttributes
    ): String {
        productService.updateProduct(id, title, vendor, productType, price)
        redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!")
        return "redirect:/products/$id/edit"
    }

    // ===== Delete Product =====

    @DeleteMapping("/products/{id}")
    fun deleteProduct(@PathVariable id: Long, model: Model): String {
        productService.deleteProduct(id)
        val products = productService.getAllProducts()
        model.addAttribute("products", products)
        return "fragments/product-table :: productTable"
    }

    // ===== Toggle Variant Availability =====

    @PutMapping("/variants/{id}/toggle-availability")
    fun toggleVariantAvailability(@PathVariable id: Long, model: Model): String {
        val variant = productService.toggleVariantAvailability(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Variant not found") }
        model.addAttribute("variant", variant)
        return "fragments/variant-availability :: variantAvailability"
    }
}
