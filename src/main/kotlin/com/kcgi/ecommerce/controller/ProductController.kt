package com.kcgi.ecommerce.controller

import com.kcgi.ecommerce.entity.Product
import com.kcgi.ecommerce.repository.ProductRepository
import com.kcgi.ecommerce.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) {

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/sell", consumes = ["multipart/form-data"])
    fun sellProductWithImage(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam("title") title: String,
        @RequestParam("description", required = false) description: String?,
        @RequestParam("price") price: Double,
        @RequestParam("image", required = false) imageFile: MultipartFile?
    ): ResponseEntity<String> {
        val seller = userRepository.findByEmail(userDetails.username)
            ?: return ResponseEntity.badRequest().body("User not found")

        var imageUrl: String? = null
        if (imageFile != null && !imageFile.isEmpty) {
            val uploadsDir = File(System.getProperty("user.dir"), "uploads")
            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs()
            }

            val filename = "${UUID.randomUUID()}_${imageFile.originalFilename}"
            val destinationFile = File(uploadsDir, filename)
            imageFile.transferTo(destinationFile)

            imageUrl = "/uploads/$filename"
        }

        val product = Product(
            title = title,
            description = description,
            price = price,
            imageUrl = imageUrl,
            seller = seller,
            createdAt = LocalDateTime.now()
        )

        productRepository.save(product)
        return ResponseEntity.ok("✅ Product listed for sale!")
    }

    @GetMapping("/all")
    fun getAllProducts(): ResponseEntity<List<Product>> {
        val products = productRepository.findByIsSoldFalseAndIsBlockedFalse()
        return ResponseEntity.ok(products)
    }


    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/mine")
    fun getMyProducts(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<List<Product>> {
        val seller = userRepository.findByEmail(userDetails.username)
            ?: return ResponseEntity.status(404).build()
        val myProducts = productRepository.findBySeller(seller)
        return ResponseEntity.ok(myProducts)
    }

    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: Long): ResponseEntity<Product> {
        val product = productRepository.findById(id)
        return if (product.isPresent) ResponseEntity.ok(product.get())
        else ResponseEntity.notFound().build()
    }

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
            return ResponseEntity.status(403).body("Unauthorized to edit this product")
        }

        product.title = request.title
        product.description = request.description
        product.price = request.price
        product.imageUrl = request.imageUrl

        productRepository.save(product)
        return ResponseEntity.ok("✅ Product updated")
    }

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
            return ResponseEntity.status(403).body("Unauthorized to delete this product")
        }

        productRepository.delete(product)
        return ResponseEntity.ok("🗑️ Product deleted")
    }

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
    fun getPurchasedProducts(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<List<Product>> {
        val buyer = userRepository.findByEmail(userDetails.username)
            ?: return ResponseEntity.status(404).build()
        val purchased = productRepository.findByBuyer(buyer)
        return ResponseEntity.ok(purchased)
    }
}

data class ProductRequest(
    val title: String,
    val description: String?,
    val price: Double,
    val imageUrl: String? = null
)
