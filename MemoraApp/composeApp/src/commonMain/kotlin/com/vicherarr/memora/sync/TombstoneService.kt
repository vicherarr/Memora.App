package com.vicherarr.memora.sync

import com.vicherarr.memora.data.database.DeletionsDao

/**
 * Servicio independiente para verificar tombstones (registros de eliminación)
 * Sigue principios SOLID:
 * - Single Responsibility: Solo maneja lógica de tombstones
 * - Open/Closed: Extensible sin modificar código existente  
 * - Dependency Inversion: Depende de abstracción (DeletionsDao)
 */
class TombstoneService(
    private val deletionsDao: DeletionsDao
) {
    
    /**
     * Verifica si un attachment específico ha sido eliminado localmente (tiene tombstone)
     * @param attachmentId ID del attachment a verificar
     * @param userId ID del usuario propietario
     * @return true si el attachment tiene tombstone (fue eliminado), false en caso contrario
     */
    suspend fun isAttachmentDeleted(attachmentId: String, userId: String): Boolean {
        return try {
            val userDeletions = deletionsDao.getDeletionsByUserId(userId)
            userDeletions.any { deletion ->
                deletion.table_name == "attachments" && deletion.record_id == attachmentId
            }
        } catch (e: Exception) {
            println("TombstoneService: Error checking attachment tombstone: ${e.message}")
            false // Si hay error, no bloquear operación
        }
    }
    
    /**
     * Verifica si una nota específica ha sido eliminada localmente (tiene tombstone)
     * @param noteId ID de la nota a verificar  
     * @param userId ID del usuario propietario
     * @return true si la nota tiene tombstone (fue eliminada), false en caso contrario
     */
    suspend fun isNoteDeleted(noteId: String, userId: String): Boolean {
        return try {
            val userDeletions = deletionsDao.getDeletionsByUserId(userId)
            userDeletions.any { deletion ->
                deletion.table_name == "notes" && deletion.record_id == noteId
            }
        } catch (e: Exception) {
            println("TombstoneService: Error checking note tombstone: ${e.message}")
            false // Si hay error, no bloquear operación
        }
    }
}