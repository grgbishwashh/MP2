package com.kcgi.ecommerce.controller

import com.kcgi.ecommerce.entity.Product
import com.kcgi.ecommerce.repository.ProductRepository
import com.kcgi.ecommerce.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

// 📦 Request format for adding/updating products
data class ProductRequest(
    val title: String,
    val description: String?,
    val price: Double,
    val imageUrl: String? = null
)

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) {

    // ✅ SELLER can list a product
    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/sell")
    fun sellProduct(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: ProductRequest
    ): ResponseEntity<String> {
        val seller = userRepository.findByEmail(userDetails.username)
            ?: return ResponseEntity.badRequest().body("User not found")

        val product = Product(
            title = request.title,
            description = request.description,
            price = request.price,
            imageUrl = request.imageUrl,
            seller = seller,
            createdAt = LocalDateTime.now()
        )

        productRepository.save(product)
        return ResponseEntity.ok("✅ Product listed for sale!")
    }

    // ✅ Public: Anyone can view all unsold products
    @GetMapping("/all")
    fun getAllProducts(): ResponseEntity<List<Product>> {
        val products = productRepository.findByIsSoldFalse()
        return ResponseEntity.ok(products)
    }

    // ✅ SELLER: View own listed products
    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/mine")
    fun getMyProducts(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<Product>> {
        val seller = userRepository.findByEmail(userDetails.username)
            ?: return ResponseEntity.status(404).build()

        val myProducts = productRepository.findBySeller(seller)
        return ResponseEntity.ok(myProducts)
    }

    // ✅ Public: Get product by ID
    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: Long): ResponseEntity<Product> {
        val product = productRepository.findById(id)
        return if (product.isPresent) {
            ResponseEntity.ok(product.get())
        } else {
            ResponseEntity.notFound().build()
        }
    }

    // ✅ SELLER: Update their own product
    @PutMapping("/{id}/update")
    @PreAuthorize("hasRole('SELLER')")
    fun updateProduct(
        @PathVariable id: Long,
        @RequestBody request: ProductRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<String> {
        val seller = userRepository.findByEmail(userDetails.username)
            ?: return ResponseEntity.status(404).body("Seller not found")

        val product = productRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(404).body("Product not found")

        if (product.seller.id != seller.id) {
            return ResponseEntity.status(403).body("You are not authorized to edit this product")
        }

        product.title = request.title
        product.description = request.description
        product.price = request.price
        product.imageUrl = request.imageUrl

        productRepository.save(product)
        return ResponseEntity.ok("✅ Product updated")
    }

    // ✅ SELLER: Delete their own product
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    fun deleteProduct(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<String> {
        val seller = userRepository.findByEmail(userDetails.username)
            ?: return ResponseEntity.status(404).body("Seller not found")

        val product = productRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(404).body("Product not found")

        if (product.seller.id != seller.id) {
            return ResponseEntity.status(403).body("You are not authorized to delete this product")
        }

        productRepository.delete(product)
        return ResponseEntity.ok("🗑️ Product deleted")
    }

    // ✅ BUYER: Confirm purchase
    @PreAuthorize("hasRole('BUYER')")
    @PostMapping("/{id}/buy")
    fun buyProduct(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<String> {
        val buyer = userRepository.findByEmail(userDetails.username)
            ?: return ResponseEntity.status(404).body("Buyer not found")

        val product = productRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(404).body("Product not found")

        if (product.isSold) {
            return ResponseEntity.badRequest().body("❌ This product is already sold.")
        }

        product.isSold = true
        product.buyer = buyer
        productRepository.save(product)

        return ResponseEntity.ok("✅ Product purchased successfully.")
    }

    @PreAuthorize("hasRole('BUYER')")
    @GetMapping("/purchased")
    fun getPurchasedProducts(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<Product>> {
        val buyer = userRepository.findByEmail(userDetails.username)
            ?: return ResponseEntity.status(404).build()

        val purchased = productRepository.findByBuyer(buyer)
        return ResponseEntity.ok(purchased)
    }


}
