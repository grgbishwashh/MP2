package com.kcgi.ecommerce.controller

import com.kcgi.ecommerce.service.AuthService
import com.kcgi.ecommerce.security.JwtUtil
import com.kcgi.ecommerce.repository.UserRepository
import com.kcgi.ecommerce.repository.PasswordResetTokenRepository
import com.kcgi.ecommerce.entity.PasswordResetToken
import com.kcgi.ecommerce.service.EmailService
import com.kcgi.ecommerce.service.RegisterRequest
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil,
    private val resetTokenRepository: PasswordResetTokenRepository,
    private val emailService: EmailService
) {
    data class LoginRequest(val email: String, val password: String)
    data class LoginResponse(val token: String)
    data class PasswordResetRequest(val token: String, val newPassword: String)

    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): ResponseEntity<String> {
        val result = authService.registerUser(req)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<Any> {
        val user = userRepository.findByEmail(req.email)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("message" to "Invalid email or password"))

        if (!user.isVerified) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("message" to "Email not verified."))
        }

        val passwordEncoder = BCryptPasswordEncoder()
        if (!passwordEncoder.matches(req.password, user.password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("message" to "Invalid email or password"))
        }

        val token = jwtUtil.generateToken(user.email)
        return ResponseEntity.ok(LoginResponse(token))
    }

    @GetMapping("/verify")
    fun verify(@RequestParam token: String): ResponseEntity<String> {
        val result = authService.verifyUser(token)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestBody body: Map<String, String>): ResponseEntity<String> {
        val email = body["email"] ?: return ResponseEntity.badRequest().body("Email is required")

        val user = userRepository.findByEmail(email)
            ?: return ResponseEntity.status(404).body("❌ Email not registered")

        val token = UUID.randomUUID().toString()
        val tokenEntity = PasswordResetToken(token = token, email = email)
        resetTokenRepository.save(tokenEntity)

        emailService.sendPasswordResetEmail(email, token)

        return ResponseEntity.ok("✅ Password reset link sent to $email")
    }

    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody req: PasswordResetRequest): ResponseEntity<String> {
        val tokenEntity = resetTokenRepository.findByToken(req.token)
            .orElse(null) ?: return ResponseEntity.status(400).body("Invalid or expired token")

        val user = userRepository.findByEmail(tokenEntity.email)
            ?: return ResponseEntity.status(404).body("User not found")

        val encoder = BCryptPasswordEncoder()
        user.password = encoder.encode(req.newPassword)
        userRepository.save(user)

        resetTokenRepository.delete(tokenEntity)

        return ResponseEntity.ok("✅ Password updated successfully")
    }
}
