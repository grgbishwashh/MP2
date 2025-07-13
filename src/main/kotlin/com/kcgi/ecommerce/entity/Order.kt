package com.kcgi.ecommerce.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    val buyer: User,

    @ManyToOne
    @JoinColumn(name = "product_id")
    val product: Product,

    @Column(nullable = false)
    val orderedAt: LocalDateTime = LocalDateTime.now()
)
