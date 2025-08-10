package com.vicherarr.memora.di

import com.vicherarr.memora.data.database.AttachmentsDao
import com.vicherarr.memora.data.database.DatabaseDriverFactory
import com.vicherarr.memora.data.database.DatabaseManager
import com.vicherarr.memora.data.database.NotesDao
import com.vicherarr.memora.data.database.SyncMetadataDao
import org.koin.dsl.module

/**
 * Koin module for database dependencies
 * Provides SQLDelight database, DAOs, and related components
 */
val databaseModule = module {
    
    // DatabaseDriverFactory is provided by platform-specific modules
    // (see androidDatabaseModule and iosDatabaseModule)
    
    // DatabaseManager - single instance
    single<DatabaseManager> {
        DatabaseManager(get<DatabaseDriverFactory>())
    }
    
    // NotesDao - from DatabaseManager
    single<NotesDao> {
        get<DatabaseManager>().notesDao
    }
    
    // AttachmentsDao - from DatabaseManager
    single<AttachmentsDao> {
        get<DatabaseManager>().attachmentsDao
    }
    
    // SyncMetadataDao - from DatabaseManager
    single<SyncMetadataDao> {
        get<DatabaseManager>().syncMetadataDao
    }
}