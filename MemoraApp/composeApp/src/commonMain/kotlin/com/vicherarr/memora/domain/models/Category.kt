package com.vicherarr.memora.domain.models

/**
 * Domain model for Category
 * Clean Architecture - Domain Layer
 */
data class Category(
    val id: String,
    val name: String,
    val color: String,
    val icon: String?,
    val createdAt: String,
    val modifiedAt: String,
    val userId: String,
    val syncStatus: String = "PENDING",
    val needsUpload: Boolean = true
)