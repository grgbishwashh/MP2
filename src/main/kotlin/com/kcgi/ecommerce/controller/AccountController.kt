package com.kcgi.ecommerce.controller

import com.kcgi.ecommerce.entity.Role
import com.kcgi.ecommerce.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/account")
class AccountController(
    private val userRepository: UserRepository
) {

    // 🔒 Only authenticated users can access this endpoint
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/become-seller")
    fun becomeSeller(@AuthenticationPrincipal userDetails: UserDetails?): ResponseEntity<String> {
        // 🛡️ Null check for security context
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Unauthorized: UserDetails is null.")
        }

        val user = userRepository.findByEmail(userDetails.username)
            ?: return ResponseEntity.status(404).body("User not found.")

        // ✅ Grant SELLER role if not already granted
        if (!user.roles.contains(Role.SELLER)) {
            user.roles.add(Role.SELLER)
            userRepository.save(user)
            return ResponseEntity.ok("✅ You are now a seller.")
        }

        return ResponseEntity.ok("⚠️ You are already a seller.")
    }
}
