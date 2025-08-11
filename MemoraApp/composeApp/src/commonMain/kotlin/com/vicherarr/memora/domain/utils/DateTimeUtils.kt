package com.vicherarr.memora.domain.utils

import com.vicherarr.memora.data.database.getCurrentTimestamp

/**
 * Utility functions for date and time formatting
 * Following Clean Architecture - Domain layer utilities
 */
object DateTimeUtils {
    
    /**
     * Format timestamp as relative time (e.g., "Hace 2h", "Hace 3d")
     * @param timestamp The timestamp to format
     * @return Formatted relative time string
     */
    fun formatRelativeTime(timestamp: Long): String {
        val now = getCurrentTimestamp()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "Hace un momento"
            diff < 3_600_000 -> {
                val minutes = diff / 60_000
                "Hace ${minutes}min"
            }
            diff < 86_400_000 -> {
                val hours = diff / 3_600_000
                "Hace ${hours}h"
            }
            diff < 604_800_000 -> {
                val days = diff / 86_400_000
                "Hace ${days}d"
            }
            else -> {
                val weeks = diff / 604_800_000
                "Hace ${weeks}sem"
            }
        }
    }
    
    /**
     * Check if timestamp is within specified time range
     * @param timestamp The timestamp to check
     * @param millisecondsRange The time range in milliseconds
     * @return True if timestamp is within range
     */
    fun isWithinTimeRange(timestamp: Long, millisecondsRange: Long): Boolean {
        val now = getCurrentTimestamp()
        return (now - timestamp) < millisecondsRange
    }
    
    /**
     * Time range constants for filtering
     */
    object TimeRanges {
        const val ONE_DAY = 24 * 60 * 60 * 1000L
        const val ONE_WEEK = 7 * ONE_DAY
        const val ONE_MONTH = 30 * ONE_DAY
    }
}