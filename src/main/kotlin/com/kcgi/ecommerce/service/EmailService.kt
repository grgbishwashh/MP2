package com.kcgi.ecommerce.service

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender
) {
    fun sendVerificationEmail(to: String, token: String) {
        println("📧 Sending verification email to: $to")
        println("🔗 Verification link: http://localhost:8080/api/auth/verify?token=$token")

        val message = SimpleMailMessage()
        message.setFrom("st102350@m2.kcg.edu")  // <- Add this line
        message.setTo(to)
        message.setSubject("Verify your KCGI account")
        message.setText("Click to verify: http://localhost:8080/api/auth/verify?token=$token")
        mailSender.send(message)
    }

    fun sendPasswordResetEmail(to: String, token: String) {
        println("📧 Sending password reset email to: $to")
        println("🔗 Reset link: http://localhost:3000/reset-password?token=$token")

        val message = SimpleMailMessage()
        message.setFrom("st102350@m2.kcg.edu")
        message.setTo(to)
        message.setSubject("Reset your password")
        message.setText("""
        You requested to reset your password.
        Click the link below to set a new password:

        http://localhost:3000/reset-password?token=$token

        If you didn't request this, please ignore this email.
    """.trimIndent())
        mailSender.send(message)
    }


}
