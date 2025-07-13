package com.kcgi.ecommerce.repository

import com.kcgi.ecommerce.entity.Order
import com.kcgi.ecommerce.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long> {
    fun findByBuyer(buyer: User): List<Order>
}
