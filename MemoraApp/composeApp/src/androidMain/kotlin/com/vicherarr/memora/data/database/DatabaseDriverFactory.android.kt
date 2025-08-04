package com.vicherarr.memora.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Android implementation of DatabaseDriverFactory
 * Uses AndroidSqliteDriver for SQLite on Android
 * TODO: Add MemoraDatabase.Schema once SQLDelight generates it
 */
actual class DatabaseDriverFactory(
    private val context: Context
) {
    actual fun createDriver(): SqlDriver {
        // Temporary implementation - will be updated once MemoraDatabase is generated
        TODO("SQLDelight database generation pending")
    }
}