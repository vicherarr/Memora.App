package com.vicherarr.memora.domain.utils

import com.vicherarr.memora.data.database.getCurrentTimestamp
import com.vicherarr.memora.domain.models.DateFilter
import com.vicherarr.memora.domain.models.DateRange

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
     * Check if timestamp matches the specified date filter
     * Handles both preset filters and custom ranges
     */
    fun matchesDateFilter(timestamp: Long, dateFilter: DateFilter, customRange: DateRange? = null): Boolean {
        return when (dateFilter) {
            DateFilter.ALL -> true
            DateFilter.TODAY -> isWithinTimeRange(timestamp, TimeRanges.ONE_DAY)
            DateFilter.WEEK -> isWithinTimeRange(timestamp, TimeRanges.ONE_WEEK)
            DateFilter.MONTH -> isWithinTimeRange(timestamp, TimeRanges.ONE_MONTH)
            DateFilter.LAST_30_DAYS -> isWithinTimeRange(timestamp, TimeRanges.THIRTY_DAYS)
            DateFilter.LAST_90_DAYS -> isWithinTimeRange(timestamp, TimeRanges.NINETY_DAYS)
            DateFilter.CUSTOM_RANGE -> customRange?.contains(timestamp) ?: true
        }
    }
    
    /**
     * Get the date range for a preset filter
     * Returns null for ALL and CUSTOM_RANGE filters
     */
    fun getPresetDateRange(dateFilter: DateFilter): DateRange? {
        val now = getCurrentTimestamp()
        return when (dateFilter) {
            DateFilter.TODAY -> DateRange(now - TimeRanges.ONE_DAY, now)
            DateFilter.WEEK -> DateRange(now - TimeRanges.ONE_WEEK, now)
            DateFilter.MONTH -> DateRange(now - TimeRanges.ONE_MONTH, now)
            DateFilter.LAST_30_DAYS -> DateRange(now - TimeRanges.THIRTY_DAYS, now)
            DateFilter.LAST_90_DAYS -> DateRange(now - TimeRanges.NINETY_DAYS, now)
            else -> null
        }
    }
    
    /**
     * Time range constants for filtering
     */
    object TimeRanges {
        const val ONE_DAY = 24 * 60 * 60 * 1000L
        const val ONE_WEEK = 7 * ONE_DAY
        const val ONE_MONTH = 30 * ONE_DAY
        const val THIRTY_DAYS = 30 * ONE_DAY
        const val NINETY_DAYS = 90 * ONE_DAY
    }
}