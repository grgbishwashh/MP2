package com.kcgi.ecommerce.controller

import com.kcgi.ecommerce.entity.Order
import com.kcgi.ecommerce.entity.Product
import com.kcgi.ecommerce.entity.Role
import com.kcgi.ecommerce.repository.OrderRepository
import com.kcgi.ecommerce.repository.ProductRepository
import com.kcgi.ecommerce.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/products")
class OrderController(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) {

    @PreAuthorize("hasRole('BUYER')")
    @PostMapping("/buy/{id}")
    fun buyProduct(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<String> {
        val product = productRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(404).body("Product not found")

        if (product.isSold) {
            return ResponseEntity.badRequest().body("❌ This product is already sold.")
        }

        val buyer = userRepository.findByEmail(userDetails.username)
            ?: return ResponseEntity.status(404).body("User not found")

        if (product.seller.id == buyer.id) {
            return ResponseEntity.badRequest().body("You can't buy your own product.")
        }

        // ✅ Save order
        val order = Order(
            buyer = buyer,
            product = product
        )
        orderRepository.save(order)

        // ✅ Mark as sold
        product.isSold = true
        productRepository.save(product)

        return ResponseEntity.ok("✅ Purchase successful")
    }


    @PreAuthorize("hasRole('BUYER')")
    @GetMapping("/orders/mine")
    fun getMyOrders(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<List<Order>> {
        val buyer = userRepository.findByEmail(userDetails.username)
            ?: return ResponseEntity.status(404).build()

        val orders = orderRepository.findByBuyer(buyer)
        return ResponseEntity.ok(orders)
    }

}
