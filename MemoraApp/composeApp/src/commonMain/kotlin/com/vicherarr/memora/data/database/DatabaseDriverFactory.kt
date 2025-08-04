package com.vicherarr.memora.data.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Platform-specific database driver factory
 * Uses expect/actual pattern for Android and iOS implementations
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}