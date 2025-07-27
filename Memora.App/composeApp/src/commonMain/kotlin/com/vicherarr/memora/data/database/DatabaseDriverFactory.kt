package com.vicherarr.memora.data.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Factory para crear el driver de SQLDelight específico por plataforma
 * Utiliza el patrón expect/actual para implementaciones específicas
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}