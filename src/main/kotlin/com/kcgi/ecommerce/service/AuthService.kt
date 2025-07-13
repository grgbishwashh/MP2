package com.kcgi.ecommerce.service

import com.kcgi.ecommerce.entity.User
import com.kcgi.ecommerce.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

data class RegisterRequest(
    val email: String,
    val password: String,
    val studentId: String,
    val lastName: String,
    val firstName: String,
    val dateOfBirth: String,  // format: "YYYY-MM-DD"
    val mobileNumber: String,
    val address: String,
    val intakeYear: Int,
    val course: String
)

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val emailService: EmailService
) {
    private val encoder = BCryptPasswordEncoder()

    fun registerUser(req: RegisterRequest): String {
        val kcgiEnforced = false // toggle this if needed

        if (kcgiEnforced && !req.email.endsWith("@m2.kcg.edu")) {
            return "Only KCGI student emails are allowed."
        }

        if (userRepository.findByEmail(req.email) != null) {
            return "Email already registered."
        }

        val token = UUID.randomUUID().toString()
        val user = User(
            email = req.email,
            password = encoder.encode(req.password),
            studentId = req.studentId,
            lastName = req.lastName,
            firstName = req.firstName,
            dateOfBirth = LocalDate.parse(req.dateOfBirth),
            mobileNumber = req.mobileNumber,
            address = req.address,
            intakeYear = req.intakeYear,
            course = req.course,
            verificationToken = token
        )

        userRepository.save(user)
        emailService.sendVerificationEmail(req.email, token)
        return "📩 Verification email sent to ${req.email}"
    }

    fun verifyUser(token: String): String {
        val user = userRepository.findByVerificationToken(token)
            ?: return "Invalid or expired token"

        user.isVerified = true
        user.verificationToken = null
        userRepository.save(user)
        return "✅ Email verified successfully!"
    }
}
