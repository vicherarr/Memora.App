package com.vicherarr.memora.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.vicherarr.memora.data.database.CategoriesDao
import com.vicherarr.memora.data.database.NoteCategoriesDao
import com.vicherarr.memora.data.database.getCurrentTimestamp
import com.vicherarr.memora.database.MemoraDatabase
import kotlin.test.*

/**
 * Test para verificar que las migraciones de categorías funcionan correctamente
 * Prueba la integridad de datos y las operaciones básicas
 */
class CategoriesMigrationTest {
    
    private lateinit var driver: SqlDriver
    private lateinit var database: MemoraDatabase
    private lateinit var categoriesDao: CategoriesDao
    private lateinit var noteCategoriesDao: NoteCategoriesDao
    
    @BeforeTest
    fun setup() {
        // Crear driver en memoria para testing
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        
        // Crear esquema de base de datos
        MemoraDatabase.Schema.create(driver)
        
        // Crear instancia de base de datos
        database = MemoraDatabase(driver)
        
        // Crear DAOs
        categoriesDao = CategoriesDao(database)
        noteCategoriesDao = NoteCategoriesDao(database)
    }
    
    @AfterTest
    fun tearDown() {
        driver.close()
    }
    
    @Test
    fun `migration creates categories table successfully`() {
        // Verificar que la tabla categories existe y se puede usar
        val categoryId = "test_category_1"
        val userId = "test_user"
        val timestamp = getCurrentTimestamp()
        
        // Insertar categoría de prueba
        runTest {
            categoriesDao.insertCategory(
                id = categoryId,
                name = "Work",
                color = "#6750A4",
                icon = "work",
                createdAt = timestamp.toString(),
                modifiedAt = timestamp.toString(),
                userId = userId,
                localCreatedAt = timestamp
            )
            
            // Verificar que se insertó correctamente
            val category = categoriesDao.getCategoryById(categoryId)
            assertNotNull(category)
            assertEquals("Work", category.name)
            assertEquals("#6750A4", category.color)
            assertEquals("work", category.icon)
            assertEquals(userId, category.user_id)
        }
    }
    
    @Test
    fun `migration creates note_categories table successfully`() {
        // Verificar que la tabla note_categories existe y se puede usar
        val noteId = "test_note_1"
        val categoryId = "test_category_1" 
        val relationshipId = "test_relationship_1"
        val timestamp = getCurrentTimestamp()
        
        runTest {
            // Insertar relación de prueba
            noteCategoriesDao.insertNoteCategory(
                id = relationshipId,
                noteId = noteId,
                categoryId = categoryId,
                createdAt = timestamp.toString(),
                localCreatedAt = timestamp
            )
            
            // Verificar que se insertó correctamente
            val exists = noteCategoriesDao.existsNoteCategory(noteId, categoryId)
            assertTrue(exists)
        }
    }
    
    @Test
    fun `foreign key constraints work correctly`() {
        val userId = "test_user"
        val noteId = "test_note_1"
        val categoryId = "test_category_1"
        val timestamp = getCurrentTimestamp()
        
        runTest {
            // Crear categoría
            categoriesDao.insertCategory(
                id = categoryId,
                name = "Test Category",
                color = "#FF0000",
                icon = null,
                createdAt = timestamp.toString(),
                modifiedAt = timestamp.toString(),
                userId = userId,
                localCreatedAt = timestamp
            )
            
            // Crear relación nota-categoría
            noteCategoriesDao.insertNoteCategory(
                id = "relationship_1",
                noteId = noteId,
                categoryId = categoryId,
                createdAt = timestamp.toString(),
                localCreatedAt = timestamp
            )
            
            // Verificar que la relación existe
            val exists = noteCategoriesDao.existsNoteCategory(noteId, categoryId)
            assertTrue(exists)
            
            // Eliminar categoría (debe eliminar relación por CASCADE)
            categoriesDao.deleteCategory(categoryId)
            
            // Verificar que la relación se eliminó automáticamente
            val existsAfterDelete = noteCategoriesDao.existsNoteCategory(noteId, categoryId)
            assertFalse(existsAfterDelete)
        }
    }
    
    @Test
    fun `unique constraints prevent duplicates`() {
        val userId = "test_user"
        val categoryName = "Duplicate Test"
        val timestamp = getCurrentTimestamp()
        
        runTest {
            // Insertar primera categoría
            categoriesDao.insertCategory(
                id = "category_1",
                name = categoryName,
                color = "#6750A4",
                icon = null,
                createdAt = timestamp.toString(),
                modifiedAt = timestamp.toString(),
                userId = userId,
                localCreatedAt = timestamp
            )
            
            // Intentar insertar categoría duplicada (mismo nombre y usuario)
            var exceptionThrown = false
            try {
                categoriesDao.insertCategory(
                    id = "category_2",
                    name = categoryName, // Mismo nombre
                    color = "#FF0000",
                    icon = null,
                    createdAt = timestamp.toString(),
                    modifiedAt = timestamp.toString(),
                    userId = userId, // Mismo usuario
                    localCreatedAt = timestamp
                )
            } catch (e: Exception) {
                exceptionThrown = true
            }
            
            // Debe fallar por constraint UNIQUE
            assertTrue(exceptionThrown, "Expected unique constraint violation")
        }
    }
    
    @Test
    fun `sync fields are properly set`() {
        val categoryId = "sync_test_category"
        val userId = "test_user"
        val timestamp = getCurrentTimestamp()
        
        runTest {
            // Insertar categoría
            categoriesDao.insertCategory(
                id = categoryId,
                name = "Sync Test",
                color = "#6750A4",
                icon = null,
                createdAt = timestamp.toString(),
                modifiedAt = timestamp.toString(),
                userId = userId,
                localCreatedAt = timestamp
            )
            
            // Verificar campos de sync por defecto
            val category = categoriesDao.getCategoryById(categoryId)
            assertNotNull(category)
            assertEquals("PENDING", category.sync_status)
            assertEquals(1L, category.needs_upload)
            assertNull(category.last_sync_attempt)
            assertNull(category.remote_id)
            
            // Marcar como sincronizado
            categoriesDao.markCategoryAsSynced(categoryId)
            
            // Verificar cambios
            val syncedCategory = categoriesDao.getCategoryById(categoryId)
            assertNotNull(syncedCategory)
            assertEquals("SYNCED", syncedCategory.sync_status)
            assertEquals(0L, syncedCategory.needs_upload)
            assertNotNull(syncedCategory.last_sync_attempt)
        }
    }
}

// Helper function para testing
private suspend fun runTest(block: suspend () -> Unit) {
    block()
}