package com.kcgi.ecommerce.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val email: String,  // College email

    @Column(nullable = false)
    var password: String,

    var isVerified: Boolean = false,

    var verificationToken: String? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "role")
    var roles: MutableSet<Role> = mutableSetOf(Role.BUYER),

    // 🆕 Additional fields
    @Column(nullable = true)
    var studentId: String? = null,

    @Column(nullable = true)
    var lastName: String? = null,

    @Column(nullable = true)
    var firstName: String? = null,

    @Column(nullable = true)
    var dateOfBirth: LocalDate? = null,

    @Column(nullable = true)
    var mobileNumber: String? = null,

    @Column(nullable = true)
    var address: String? = null,

    @Column(nullable = true)
    var intakeYear: Int? = null,

    @Column(nullable = true)
    var course: String? = null  // Store course name directly (you can enum later if needed)
)
