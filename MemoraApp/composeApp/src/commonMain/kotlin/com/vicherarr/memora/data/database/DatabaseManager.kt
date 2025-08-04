package com.vicherarr.memora.data.database

import com.vicherarr.memora.database.MemoraDatabase

/**
 * Database manager that provides access to SQLDelight database and DAOs
 * Centralizes database initialization and DAO creation
 */
class DatabaseManager(private val driverFactory: DatabaseDriverFactory) {
    
    private val driver = driverFactory.createDriver()
    private val database = MemoraDatabase(driver)
    
    /**
     * Notes Data Access Object
     */
    val notesDao: NotesDao by lazy {
        NotesDao(database)
    }
    
    /**
     * Get direct access to database if needed
     */
    fun getDatabase(): MemoraDatabase = database
    
    /**
     * Close database connection
     */
    fun close() {
        driver.close()
    }
}