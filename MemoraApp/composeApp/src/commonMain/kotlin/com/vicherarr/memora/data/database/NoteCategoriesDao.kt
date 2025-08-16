package com.vicherarr.memora.data.database

import com.vicherarr.memora.database.Categories
import com.vicherarr.memora.database.Notes
import com.vicherarr.memora.database.Note_categories
import com.vicherarr.memora.database.MemoraDatabase
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for NoteCategories table
 * Handles N:M relationship between Notes and Categories
 * Following Clean Architecture and SOLID principles
 */
class NoteCategoriesDao(
    private val database: MemoraDatabase,
    private val categoriesDao: CategoriesDao // ✅ FIX: Dependencia para validar categorías
) {
    
    private val queries = database.noteCategoriesQueries
    
    // === Relationship Management ===
    
    /**
     * Insert a new note-category relationship
     */
    suspend fun insertNoteCategory(
        id: String,
        noteId: String,
        categoryId: String,
        createdAt: String,
        syncStatus: String = "PENDING",
        needsUpload: Long = 1,
        localCreatedAt: Long,
        remoteId: String? = null
    ) {
        queries.insertNoteCategory(
            id = id,
            note_id = noteId,
            category_id = categoryId,
            created_at = createdAt,
            sync_status = syncStatus,
            needs_upload = needsUpload,
            local_created_at = localCreatedAt,
            remote_id = remoteId
        )
    }
    
    /**
     * Delete a note-category relationship by ID
     */
    suspend fun deleteNoteCategory(noteCategoryId: String) {
        queries.deleteNoteCategory(noteCategoryId)
    }
    
    /**
     * Delete all categories for a specific note
     */
    suspend fun deleteNoteCategoriesByNoteId(noteId: String) {
        queries.deleteNoteCategoriesByNoteId(noteId)
    }
    
    /**
     * Delete all notes for a specific category
     */
    suspend fun deleteNoteCategoriesByCategoryId(categoryId: String) {
        queries.deleteNoteCategoriesByCategoryId(categoryId)
    }
    
    /**
     * Delete a specific note-category relationship
     */
    suspend fun deleteSpecificNoteCategory(noteId: String, categoryId: String) {
        queries.deleteSpecificNoteCategory(noteId, categoryId)
    }
    
    // === Queries for Categories by Note ===
    
    /**
     * Get all categories assigned to a specific note
     */
    suspend fun getCategoriesByNoteId(noteId: String): List<Categories> {
        return queries.getCategoriesByNoteId(noteId).executeAsList()
    }
    
    /**
     * Get categories for a note as Flow for reactive UI updates
     */
    fun getCategoriesByNoteIdFlow(noteId: String): Flow<List<Categories>> {
        return queries.getCategoriesByNoteIdFlow(noteId).asFlow().mapToList(Dispatchers.IO)
    }
    
    /**
     * Get all note-category relationships as Flow for reactive UI updates
     * Used for notes list to detect when categories change
     */
    fun getAllNoteCategoriesFlow(): Flow<List<Note_categories>> {
        return queries.getAllNoteCategories().asFlow().mapToList(Dispatchers.IO)
    }
    
    // === Queries for Notes by Category ===
    
    /**
     * Get all notes assigned to a specific category
     */
    suspend fun getNotesByCategory(categoryId: String): List<Notes> {
        return queries.getNotesByCategory(categoryId).executeAsList()
    }
    
    /**
     * Get notes for a category as Flow for reactive UI updates
     */
    fun getNotesByCategoryFlow(categoryId: String): Flow<List<Notes>> {
        return queries.getNotesByCategoryFlow(categoryId).asFlow().mapToList(Dispatchers.IO)
    }
    
    // === Batch Operations ===
    
    /**
     * Get all note-category relationships for a user
     * Used for sync operations
     */
    suspend fun getNoteCategoriesByUserId(userId: String): List<Note_categories> {
        return queries.getNoteCategoriesByUserId(userId).executeAsList()
    }
    
    /**
     * Get all note-category relationships for a user
     * Alternative method name for fingerprint generation
     */
    suspend fun getNotesCategoriesByUserId(userId: String): List<Note_categories> {
        return getNoteCategoriesByUserId(userId)
    }
    
    /**
     * Check if a specific note-category relationship exists
     */
    suspend fun existsNoteCategory(noteId: String, categoryId: String): Boolean {
        return queries.existsNoteCategory(noteId, categoryId).executeAsOne()
    }
    
    // === Sync Operations ===
    
    /**
     * Get note-category relationships pending synchronization
     */
    suspend fun getNoteCategoriesPendingSync(userId: String): List<Note_categories> {
        return queries.getNoteCategoriesPendingSync(userId).executeAsList()
    }
    
    /**
     * Mark note-category relationship as successfully synced
     */
    suspend fun markNoteCategoryAsSynced(noteCategoryId: String) {
        val currentTimestamp = getCurrentTimestamp()
        queries.markNoteCategoryAsSynced(currentTimestamp, noteCategoryId)
    }
    
    /**
     * Mark note-category sync as failed
     */
    suspend fun markNoteCategoryAsFailed(noteCategoryId: String) {
        val currentTimestamp = getCurrentTimestamp()
        queries.markNoteCategoryAsFailed(currentTimestamp, noteCategoryId)
    }
    
    /**
     * Update remote ID after successful upload
     */
    suspend fun updateNoteCategoryRemoteId(noteCategoryId: String, remoteId: String) {
        queries.updateNoteCategoryRemoteId(remoteId, noteCategoryId)
    }
    
    // === Statistics for Sync Metadata ===
    
    /**
     * Get total count of note-category relationships for a user
     */
    suspend fun getNoteCategoriesCount(userId: String): Long {
        return queries.getNoteCategoriesCount(userId).executeAsOne()
    }
    
    /**
     * Get hash data for sync metadata calculation
     */
    suspend fun getNoteCategoriesHashData(userId: String): List<NoteCategoryHashData> {
        return queries.getNoteCategoriesHashData(userId).executeAsList().map { row ->
            NoteCategoryHashData(
                id = row.id,
                noteId = row.note_id,
                categoryId = row.category_id,
                createdAt = row.created_at
            )
        }
    }
    
    // === Cleanup Operations ===
    
    /**
     * Get categories that have no notes assigned (orphaned categories)
     */
    suspend fun getOrphanedCategories(userId: String): List<Categories> {
        return queries.getOrphanedCategories(userId).executeAsList()
    }
    
    /**
     * Count notes by category for cleanup and statistics
     */
    suspend fun countNotesByCategory(userId: String): List<CategoryNoteCount> {
        return queries.countNotesByCategory(userId).executeAsList().map { row ->
            CategoryNoteCount(
                categoryId = row.id,
                categoryName = row.name,
                noteCount = row.noteCount?.toInt() ?: 0
            )
        }
    }
    
    // === Batch Assignment Operations ===
    
    /**
     * Assign multiple categories to a note
     * This is a convenience method for bulk operations
     */
    suspend fun assignCategoriesToNote(
        noteId: String, 
        categoryIds: List<String>,
        timestamp: String = getCurrentTimestamp().toString()
    ) {
        // First, remove existing categories for the note
        deleteNoteCategoriesByNoteId(noteId)
        
        // ✅ FIX: Verificar que las categorías existen antes de crear relaciones
        println("NoteCategoriesDao: 🔍 VALIDANDO ${categoryIds.size} categorías para nota $noteId")
        categoryIds.forEachIndexed { index, categoryId ->
            println("NoteCategoriesDao: Validando categoría ${index + 1}/${ categoryIds.size}: $categoryId")
            
            // Verificar que la categoría existe
            val existingCategory = categoriesDao.getCategoryById(categoryId)
            if (existingCategory != null) {
                println("NoteCategoriesDao: ✅ CATEGORÍA ENCONTRADA: ${existingCategory.name} (${existingCategory.id})")
                val relationshipId = "nc_${getCurrentTimestamp()}_${noteId.take(8)}_${categoryId.take(8)}"
                insertNoteCategory(
                    id = relationshipId,
                    noteId = noteId,
                    categoryId = categoryId,
                    createdAt = timestamp,
                    localCreatedAt = getCurrentTimestamp()
                )
                println("NoteCategoriesDao: ✅ RELACIÓN CREADA: $relationshipId")
            } else {
                println("NoteCategoriesDao: ❌ CATEGORÍA NO ENCONTRADA: $categoryId - SALTANDO RELACIÓN")
            }
        }
        println("NoteCategoriesDao: 🏁 Validación completada para nota $noteId")
    }
    
    /**
     * Remove a note from a specific category
     */
    suspend fun removeNoteFromCategory(noteId: String, categoryId: String) {
        deleteSpecificNoteCategory(noteId, categoryId)
    }
}

/**
 * Data class for note category hash calculation
 */
data class NoteCategoryHashData(
    val id: String,
    val noteId: String,
    val categoryId: String,
    val createdAt: String
)

/**
 * Data class for category note count
 */
data class CategoryNoteCount(
    val categoryId: String,
    val categoryName: String,
    val noteCount: Int
)