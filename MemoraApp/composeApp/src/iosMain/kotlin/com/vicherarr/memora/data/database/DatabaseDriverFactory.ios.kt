package com.vicherarr.memora.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

/**
 * iOS implementation of DatabaseDriverFactory  
 * Uses NativeSqliteDriver for SQLite on iOS
 * TODO: Add MemoraDatabase.Schema once SQLDelight generates it
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        // Temporary implementation - will be updated once MemoraDatabase is generated
        TODO("SQLDelight database generation pending")
    }
}