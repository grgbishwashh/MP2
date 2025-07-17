package com.kcgi.ecommerce.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.stereotype.Component
import java.util.Date
import java.security.Key
import java.util.Base64
import javax.crypto.spec.SecretKeySpec

@Component
class JwtUtil {

    // WARNING: Storing a secret key directly in the code is a security risk.
    // It is highly recommended to externalize this configuration using Spring's @Value
    // to load it from application.properties or a dedicated secrets manager.
    private val secretKey = "yR0496Xo0yXqTEEOMJNTZeEEPaQ4pUGB8bmLPZEoHH8="
    private val expirationMs: Long = 1000 * 60 * 60 * 24 // 24 hours

    // Create the signing key using the legacy approach for jjwt-0.11.5
    private val signingKey: Key by lazy {
        val decodedKey = Base64.getDecoder().decode(secretKey)
        // Use SecretKeySpec for versions prior to the introduction of io.jsonwebtoken.security.Keys
        SecretKeySpec(decodedKey, 0, decodedKey.size, "HmacSHA256")
    }

    fun generateToken(email: String, roles: List<String>): String {

        val now = Date()
        val expiryDate = Date(now.time + expirationMs)

        return Jwts.builder()
            .setSubject(email)
            .claim("roles", roles) // 👈 add this line
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact()

    }

    fun validateToken(token: String): Boolean {
        return try {
            !isTokenExpired(token)
        } catch (e: Exception) {
            // It's good practice to log the specific exception (e.g., SignatureException, ExpiredJwtException)
            // logger.error("Invalid JWT token: {}", e.message)
            false
        }
    }

    fun extractEmail(token: String): String {
        return getClaims(token).subject
    }

    fun getClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .body
    }

    private fun isTokenExpired(token: String): Boolean {
        return getClaims(token).expiration.before(Date())
    }
}