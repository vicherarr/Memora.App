package com.vicherarr.memora.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.vicherarr.memora.database.MemoraDatabase

/**
 * Implementación de DatabaseDriverFactory para iOS
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(MemoraDatabase.Schema, "memora.db")
    }
}