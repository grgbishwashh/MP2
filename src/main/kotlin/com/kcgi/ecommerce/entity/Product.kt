package com.kcgi.ecommerce.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var title: String,

    var description: String? = null,

    @Column(nullable = false)
    var price: Double,

    var imageUrl: String? = null,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false, columnDefinition = "boolean default false")
    var isSold: Boolean = false,

    @Column
    var isBlocked: Boolean? = false,


    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    val seller: User,

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    var buyer: User? = null // ✅ new: buyer is nullable until purchased
)
