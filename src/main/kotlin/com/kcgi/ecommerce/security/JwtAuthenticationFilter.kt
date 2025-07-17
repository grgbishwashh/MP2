package com.kcgi.ecommerce.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            val email = jwtUtil.extractEmail(token)

            if (email != null && SecurityContextHolder.getContext().authentication == null) {
                if (jwtUtil.validateToken(token)) {
                    val claims = jwtUtil.getClaims(token)

                    val roles = claims["roles"] as? List<*> ?: emptyList<Any>()
                    val authorities = roles.map { SimpleGrantedAuthority("ROLE_$it") }

                    val authToken = UsernamePasswordAuthenticationToken(
                        org.springframework.security.core.userdetails.User(email, "", authorities),
                        null,
                        authorities
                    )

                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)

                    println("🔐 Email from token: $email")
                    println("🔐 Roles from token: $roles")
                    println("✅ Authorities being set: $authorities")

                    SecurityContextHolder.getContext().authentication = authToken
                } else {
                    println("❌ Invalid token for email: $email")
                }
            } else {
                if (email == null) println("❌ Could not extract email from token")
                else println("🔒 Authentication already set in context")
            }
        }

        filterChain.doFilter(request, response)
    }
}
