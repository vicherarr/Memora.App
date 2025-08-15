package com.vicherarr.memora.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.vicherarr.memora.database.MemoraDatabase

/**
 * Android implementation of DatabaseDriverFactory
 * Uses AndroidSqliteDriver for SQLite on Android with migration support
 */
actual class DatabaseDriverFactory(
    private val context: Context
) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = MemoraDatabase.Schema,
            context = context,
            name = "memora_database.db"
        )
    }
}