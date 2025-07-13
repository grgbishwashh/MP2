package com.kcgi.ecommerce.repository

import com.kcgi.ecommerce.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun findByVerificationToken(token: String): User?
}
