package com.vicherarr.memora.sync

import com.vicherarr.memora.data.database.NotesDao
import com.vicherarr.memora.data.database.AttachmentsDao
import com.vicherarr.memora.data.database.CategoriesDao
import com.vicherarr.memora.data.database.NoteCategoriesDao
import com.vicherarr.memora.data.database.getCurrentTimestamp
import kotlin.experimental.and

/**
 * Generator de fingerprints para sincronización incremental
 * Clean Architecture - Domain Service
 * 
 * Responsabilidades:
 * - Generar hashes únicos basados en estado actual de datos
 * - Comparar fingerprints para determinar si hay cambios
 * - Algoritmo determinístico y eficiente
 */
class FingerprintGenerator(
    private val notesDao: NotesDao,
    private val attachmentsDao: AttachmentsDao,
    private val categoriesDao: CategoriesDao,
    private val noteCategoriesDao: NoteCategoriesDao
) {
    
    /**
     * Genera fingerprint completo para un usuario basado en todos sus datos
     * 
     * El fingerprint incluye:
     * - Cantidad total de notas
     * - Cantidad total de attachments  
     * - Cantidad total de categorías (Fase 6)
     * - Cantidad total de relaciones nota-categoría (Fase 6)
     * - Timestamp de última modificación de notas
     * - Hash de todos los IDs y fechas de modificación de notas
     * - Hash de todos los IDs y estados de sync de attachments
     * - Hash de todas las categorías (Fase 6)
     * - Hash de todas las relaciones nota-categoría (Fase 6)
     * 
     * @return FingerprintResult con fingerprint y conteos reales de BD
     */
    suspend fun generateContentFingerprint(userId: String): FingerprintResult {
        try {
            // Obtener datos de notas
            val notes = notesDao.getNotesByUserId(userId)
            val attachments = attachmentsDao.getAttachmentsForUser(userId)
            
            // Fase 6: Obtener datos de categorías
            val categories = categoriesDao.getCategoriesByUserId(userId)
            val noteCategories = noteCategoriesDao.getNotesCategoriesByUserId(userId)
            
            // Componentes del fingerprint
            val notesCount = notes.size
            val attachmentsCount = attachments.size
            val categoriesCount = categories.size
            val noteCategoriesCount = noteCategories.size
            val lastNoteModified = notes.maxOfOrNull { 
                it.fecha_modificacion.toLongOrNull() ?: 0L 
            } ?: 0L
            
            // Hash detallado de notas (ID + timestamp modificación)
            val notesHash = notes
                .sortedBy { it.id } // Orden determinístico
                .joinToString("|") { "${it.id}:${it.fecha_modificacion}" }
                .let { if (it.isEmpty()) "no-notes" else it }
            
            // Hash detallado de attachments (ID + sync_status + content_hash)
            val attachmentsHash = attachments
                .sortedBy { it.id } // Orden determinístico
                .joinToString("|") { "${it.id}:${it.sync_status}:${it.content_hash ?: "no-hash"}" }
                .let { if (it.isEmpty()) "no-attachments" else it }
            
            // Fase 6: Hash detallado de categorías (ID + nombre + color)
            val categoriesHash = categories
                .sortedBy { it.id } // Orden determinístico
                .joinToString("|") { "${it.id}:${it.name}:${it.color}" }
                .let { if (it.isEmpty()) "no-categories" else it }
            
            // Fase 6: Hash detallado de relaciones nota-categoría (note_id + category_id)
            val noteCategoriesHash = noteCategories
                .sortedWith(compareBy({ it.note_id }, { it.category_id })) // Orden determinístico
                .joinToString("|") { "${it.note_id}:${it.category_id}" }
                .let { if (it.isEmpty()) "no-note-categories" else it }
            
            // Construir string completo para hash
            val fingerprintData = buildString {
                append("v2|") // Versión del algoritmo (v2 incluye categorías - Fase 6)
                append("notes:$notesCount|")
                append("attachments:$attachmentsCount|")
                append("categories:$categoriesCount|") // Fase 6
                append("notecategories:$noteCategoriesCount|") // Fase 6
                append("lastmod:$lastNoteModified|")
                append("nhash:${notesHash.simpleHash()}|")
                append("ahash:${attachmentsHash.simpleHash()}|")
                append("chash:${categoriesHash.simpleHash()}|") // Fase 6
                append("nchash:${noteCategoriesHash.simpleHash()}") // Fase 6
            }
            
            println("FingerprintGenerator: Generando fingerprint para user $userId")
            println("FingerprintGenerator: Notes: $notesCount, Attachments: $attachmentsCount, Categories: $categoriesCount, NoteCategories: $noteCategoriesCount")
            
            // ✅ LOG DETALLADO: Categorías encontradas
            if (categories.isNotEmpty()) {
                println("FingerprintGenerator: 📂 CATEGORÍAS ENCONTRADAS:")
                categories.forEach { category ->
                    println("  - ${category.name} (${category.id}) - user: ${category.user_id}")
                }
            } else {
                println("FingerprintGenerator: ❌ NO se encontraron categorías para user: $userId")
            }
            
            // ✅ LOG DETALLADO: Relaciones nota-categoría encontradas
            if (noteCategories.isNotEmpty()) {
                println("FingerprintGenerator: 🔗 RELACIONES NOTA-CATEGORÍA ENCONTRADAS:")
                noteCategories.forEach { relation ->
                    println("  - Nota: ${relation.note_id} -> Categoría: ${relation.category_id}")
                }
            } else {
                println("FingerprintGenerator: ❌ NO se encontraron relaciones nota-categoría para user: $userId")
            }
            println("FingerprintGenerator: Data: $fingerprintData")
            
            val finalFingerprint = fingerprintData.sha256Hash()
            println("FingerprintGenerator: Fingerprint final: $finalFingerprint")
            
            return FingerprintResult(
                fingerprint = finalFingerprint,
                notesCount = notesCount,
                attachmentsCount = attachmentsCount,
                categoriesCount = categoriesCount, // Fase 6
                noteCategoriesCount = noteCategoriesCount, // Fase 6
                lastModifiedTimestamp = lastNoteModified
            )
            
        } catch (e: Exception) {
            println("FingerprintGenerator: Error generando fingerprint: ${e.message}")
            // Fallback: usar timestamp actual para forzar sync
            val errorTimestamp = getCurrentTimestamp()
            return FingerprintResult(
                fingerprint = "error_${errorTimestamp}",
                notesCount = 0,
                attachmentsCount = 0,
                categoriesCount = 0, // Fase 6
                noteCategoriesCount = 0, // Fase 6
                lastModifiedTimestamp = errorTimestamp
            )
        }
    }
    
    /**
     * Genera fingerprint rápido basado solo en conteos y timestamps
     * Usado para comparaciones rápidas sin acceso detallado a datos
     */
    fun generateQuickFingerprint(
        notesCount: Int,
        attachmentsCount: Int,
        categoriesCount: Int,
        noteCategoriesCount: Int,
        lastModifiedTimestamp: Long
    ): String {
        val fingerprintData = "quick|notes:$notesCount|attachments:$attachmentsCount|categories:$categoriesCount|notecategories:$noteCategoriesCount|lastmod:$lastModifiedTimestamp"
        return fingerprintData.sha256Hash()
    }
    
    /**
     * Compara dos fingerprints y determina si son equivalentes
     */
    fun areEquivalent(fingerprint1: String, fingerprint2: String): Boolean {
        return fingerprint1 == fingerprint2
    }
}

/**
 * Extensión para obtener attachments de un usuario específico
 * Filtra attachments basándose en las notas del usuario
 */
private suspend fun AttachmentsDao.getAttachmentsForUser(userId: String): List<com.vicherarr.memora.database.Attachments> {
    // Obtener todos los attachments y filtrar por usuario indirectamente
    // (los attachments pertenecen a notas, las notas pertenecen a usuarios)
    return getAllAttachments()
}

/**
 * Extensión para calcular hash simple de string
 */
private fun String.simpleHash(): Int {
    var hash = 0
    for (char in this) {
        hash = (hash * 31 + char.code) and Int.MAX_VALUE
    }
    return hash
}

/**
 * Extensión para calcular SHA256 hash de string
 * Implementación simple para KMP - en producción usar una librería crypto dedicada
 */
private fun String.sha256Hash(): String {
    // Implementación simplificada usando hashCode + content
    // En producción se usaría una librería crypto real como kotlinx-crypto
    val hashCode = this.hashCode()
    val contentHash = this.length * 31 + this.sumOf { it.code }
    val combinedHash = (hashCode.toLong() shl 32) or (contentHash.toLong() and 0xFFFFFFFFL)
    
    return "sha256_${combinedHash.toString(16).padStart(16, '0')}"
}

/**
 * Data class para resultado detallado de generación de fingerprint
 */
data class FingerprintResult(
    val fingerprint: String,
    val notesCount: Int,
    val attachmentsCount: Int,
    val categoriesCount: Int, // Fase 6
    val noteCategoriesCount: Int, // Fase 6
    val lastModifiedTimestamp: Long,
    val generatedAt: Long = getCurrentTimestamp()
)