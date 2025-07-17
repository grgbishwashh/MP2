package com.kcgi.ecommerce.repository

import com.kcgi.ecommerce.entity.Product
import com.kcgi.ecommerce.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Long> {
    fun findBySeller(seller: User): List<Product>
    fun findByIsSoldFalse(): List<Product>
    fun findByBuyer(buyer: User): List<Product>
    fun findByIsSoldFalseAndIsBlockedFalse(): List<Product>



}
