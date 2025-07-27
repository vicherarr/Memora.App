package com.vicherarr.memora.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.vicherarr.memora.database.MemoraDatabase

/**
 * Implementación de DatabaseDriverFactory para Android
 */
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(MemoraDatabase.Schema, context, "memora.db")
    }
}