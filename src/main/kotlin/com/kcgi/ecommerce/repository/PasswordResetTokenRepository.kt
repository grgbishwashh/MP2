package com.kcgi.ecommerce.repository

import com.kcgi.ecommerce.entity.PasswordResetToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, Long> {
    fun findByToken(token: String): Optional<PasswordResetToken>
    fun findByEmail(email: String): Optional<PasswordResetToken>
}
