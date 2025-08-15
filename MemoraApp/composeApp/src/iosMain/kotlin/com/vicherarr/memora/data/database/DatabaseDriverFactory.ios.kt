package com.vicherarr.memora.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.vicherarr.memora.database.MemoraDatabase

/**
 * iOS implementation of DatabaseDriverFactory  
 * Uses NativeSqliteDriver for SQLite on iOS with migration support
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = MemoraDatabase.Schema,
            name = "memora_database.db"
        )
    }
}