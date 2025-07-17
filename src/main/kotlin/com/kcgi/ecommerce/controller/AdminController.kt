package com.kcgi.ecommerce.controller

import com.kcgi.ecommerce.entity.Product
import com.kcgi.ecommerce.entity.User
import com.kcgi.ecommerce.repository.ProductRepository
import com.kcgi.ecommerce.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository
) {

    @GetMapping("/users")
    fun getAllUsers(): List<User> = userRepository.findAll()

    @PostMapping("/users/{id}/block")
    fun blockUser(@PathVariable id: Long): ResponseEntity<String> {
        val user = userRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        user.enabled = false
        userRepository.save(user)
        return ResponseEntity.ok("User blocked")
    }

    @PostMapping("/users/{id}/unblock")
    fun unblockUser(@PathVariable id: Long): ResponseEntity<String> {
        val user = userRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        user.enabled = true
        userRepository.save(user)
        return ResponseEntity.ok("User unblocked")
    }

    @DeleteMapping("/users/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build()
        userRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/products")
    fun getAllProducts(): List<Product> {
        println("🛡️ Admin endpoint reached!")
        return productRepository.findAll()
    }


    @DeleteMapping("/products/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        if (!productRepository.existsById(id)) return ResponseEntity.notFound().build()
        productRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/products/{id}/block")
    fun blockProduct(@PathVariable id: Long): ResponseEntity<String> {
        val product = productRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        product.isBlocked = true
        productRepository.save(product)
        return ResponseEntity.ok("Product blocked")
    }

    @PostMapping("/products/{id}/unblock")
    fun unblockProduct(@PathVariable id: Long): ResponseEntity<String> {
        val product = productRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        product.isBlocked = false
        productRepository.save(product)
        return ResponseEntity.ok("Product unblocked")
    }
}
