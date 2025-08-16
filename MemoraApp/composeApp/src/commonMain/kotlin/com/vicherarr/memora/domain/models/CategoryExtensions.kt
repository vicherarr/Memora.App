package com.vicherarr.memora.domain.models

/**
 * Category Extension Properties
 * Clean Architecture - Domain Layer
 * 
 * Provides presentation-related computed properties for Category domain objects
 * without modifying the core domain model. Follows Kotlin best practices
 * and is compatible with Kotlin Multiplatform.
 */

/**
 * Display name for UI presentation
 * Converts category name to uppercase for consistent visual presentation
 * across all UI components.
 * 
 * @return The category name in uppercase format
 */
val Category.displayName: String 
    get() = name.uppercase()

/**
 * Short display name for compact UI elements
 * Provides abbreviated version of category name for small UI components
 * like chips or badges where space is limited.
 * 
 * @param maxLength Maximum number of characters to display (default 10)
 * @return Truncated uppercase category name with ellipsis if needed
 */
fun Category.shortDisplayName(maxLength: Int = 10): String {
    val upperName = name.uppercase()
    return if (upperName.length <= maxLength) {
        upperName
    } else {
        "${upperName.take(maxLength - 1)}…"
    }
}