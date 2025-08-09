package com.vicherarr.memora.sync

import com.vicherarr.memora.data.database.DatabaseManager
import com.vicherarr.memora.data.database.getCurrentTimestamp
import com.vicherarr.memora.database.Notes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Formato serializable para sincronización de notas
 */
@Serializable
data class SerializableNote(
    val id: String,
    val titulo: String?,
    val contenido: String,
    val fechaCreacion: String,
    val fechaModificacion: String,
    val usuarioId: String,
    val eliminado: Boolean = false
)

/**
 * Formato serializable para la base de datos completa
 */
@Serializable
data class SerializableDatabase(
    val version: String = "1.0",
    val timestamp: Long,
    val notas: List<SerializableNote>
)

/**
 * Servicio que conecta SQLDelight con el sistema de sincronización
 * Maneja la serialización/deserialización de datos reales de la base de datos
 */
class DatabaseSyncService(
    private val databaseManager: DatabaseManager
) {
    
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true 
    }
    
    /**
     * Serializa todas las notas locales a ByteArray para subir a la nube
     */
    suspend fun serializeLocalDatabase(userId: String): ByteArray = withContext(Dispatchers.IO) {
        try {
            println("DatabaseSyncService: Serializando base de datos local para usuario $userId")
            
            // DEBUGGING: Obtener TODAS las notas sin filtro para ver qué usuario_id tienen
            println("DatabaseSyncService: 🔍 DEBUGGING - Consultando TODAS las notas...")
            
            // Obtener todas las notas del usuario desde SQLDelight
            val localNotes = databaseManager.notesDao.getNotesByUserId(userId)
            
            println("DatabaseSyncService: ====== SERIALIZACIÓN DB LOCAL ======")
            println("DatabaseSyncService: Usuario ID: $userId")
            println("DatabaseSyncService: Encontradas ${localNotes.size} notas locales")
            
            // LOGS DETALLADOS DE CADA NOTA
            localNotes.forEachIndexed { index, note ->
                println("  📝 NOTA ${index + 1}:")
                println("    - ID: ${note.id}")
                println("    - Título: '${note.titulo ?: "Sin título"}'")
                println("    - Contenido (primeros 50 chars): '${note.contenido.take(50)}${if (note.contenido.length > 50) "..." else ""}'")
                println("    - Fecha Modificación: ${note.fecha_modificacion}")
                println("    - Usuario ID: ${note.usuario_id}")
            }
            
            // Convertir a formato serializable
            val serializableNotes = localNotes.map { note ->
                SerializableNote(
                    id = note.id,
                    titulo = note.titulo,
                    contenido = note.contenido,
                    fechaCreacion = note.fecha_creacion,
                    fechaModificacion = note.fecha_modificacion,
                    usuarioId = note.usuario_id,
                    eliminado = false // TODO: Agregar campo eliminado al esquema
                )
            }
            
            // Crear estructura de base de datos serializable
            val serializableDb = SerializableDatabase(
                timestamp = getCurrentTimestamp(),
                notas = serializableNotes
            )
            
            // Serializar a JSON y convertir a ByteArray
            val jsonString = json.encodeToString(serializableDb)
            val bytes = jsonString.encodeToByteArray()
            
            println("DatabaseSyncService: ====== JSON GENERADO ======")
            println("DatabaseSyncService: Tamaño JSON: ${jsonString.length} caracteres")
            println("DatabaseSyncService: Tamaño ByteArray: ${bytes.size} bytes")
            println("DatabaseSyncService: JSON (primeros 500 chars):")
            println(jsonString.take(500) + if (jsonString.length > 500) "..." else "")
            println("DatabaseSyncService: =============================")
            
            return@withContext bytes
            
        } catch (e: Exception) {
            println("DatabaseSyncService: Error serializando DB local: ${e.message}")
            throw e
        }
    }
    
    /**
     * Deserializa la base de datos remota desde ByteArray
     */
    suspend fun deserializeRemoteDatabase(remoteDbBytes: ByteArray): List<DatabaseNote> = withContext(Dispatchers.Default) {
        try {
            println("DatabaseSyncService: ====== DESERIALIZACIÓN DB REMOTA ======")
            println("DatabaseSyncService: Tamaño ByteArray recibido: ${remoteDbBytes.size} bytes")
            
            val jsonString = remoteDbBytes.decodeToString()
            println("DatabaseSyncService: JSON recibido (primeros 500 chars):")
            println(jsonString.take(500) + if (jsonString.length > 500) "..." else "")
            
            val serializableDb = json.decodeFromString<SerializableDatabase>(jsonString)
            
            println("DatabaseSyncService: DB remota deserializada:")
            println("  - Versión: ${serializableDb.version}")
            println("  - Timestamp: ${serializableDb.timestamp}")
            println("  - Número de notas: ${serializableDb.notas.size}")
            
            // LOGS DETALLADOS DE CADA NOTA REMOTA
            serializableDb.notas.forEachIndexed { index, note ->
                println("  🌥️ NOTA REMOTA ${index + 1}:")
                println("    - ID: ${note.id}")
                println("    - Título: '${note.titulo ?: "Sin título"}'")
                println("    - Contenido (primeros 50 chars): '${note.contenido.take(50)}${if (note.contenido.length > 50) "..." else ""}'")
                println("    - Fecha Modificación: ${note.fechaModificacion}")
                println("    - Usuario ID: ${note.usuarioId}")
            }
            
            // Convertir a formato de DatabaseNote para el merger
            val databaseNotes = serializableDb.notas.map { note ->
                DatabaseNote(
                    id = note.id,
                    titulo = note.titulo,
                    contenido = note.contenido,
                    fechaModificacion = parseTimestamp(note.fechaModificacion),
                    eliminado = note.eliminado
                )
            }
            
            println("DatabaseSyncService: ${databaseNotes.size} notas remotas convertidas para fusión")
            println("DatabaseSyncService: ========================================")
            
            return@withContext databaseNotes
            
        } catch (e: Exception) {
            println("DatabaseSyncService: Error deserializando DB remota: ${e.message}")
            // Si falla la deserialización, retornar lista vacía en lugar de fallar
            println("DatabaseSyncService: Retornando lista vacía debido al error")
            return@withContext emptyList()
        }
    }
    
    /**
     * Convierte las notas locales de SQLDelight a formato DatabaseNote
     */
    suspend fun getLocalNotesForMerging(userId: String): List<DatabaseNote> = withContext(Dispatchers.IO) {
        try {
            println("DatabaseSyncService: Obteniendo notas locales para fusión")

            // ==================== INICIO DEBUGGING ====================
            println("DEBUGGING: Mostrando TODAS las notas en la DB local ANTES de filtrar por usuario:")
            val allNotes = databaseManager.notesDao.getAllNotes()
            if (allNotes.isNotEmpty()) {
                allNotes.forEach { note ->
                    println("  -> Nota en DB: ID=${note.id}, UsuarioID='${note.usuario_id}', Título='${note.titulo}'")
                }
            } else {
                println("  -> La tabla de notas está completamente vacía.")
            }
            println("==================== FIN DEBUGGING ====================")
            
            val localNotes = databaseManager.notesDao.getNotesByUserId(userId)
            
            val databaseNotes = localNotes.map { note ->
                DatabaseNote(
                    id = note.id,
                    titulo = note.titulo,
                    contenido = note.contenido,
                    fechaModificacion = parseTimestamp(note.fecha_modificacion),
                    eliminado = false // TODO: Agregar campo eliminado
                )
            }
            
            println("DatabaseSyncService: ${databaseNotes.size} notas locales convertidas para fusión")
            return@withContext databaseNotes
            
        } catch (e: Exception) {
            println("DatabaseSyncService: Error obteniendo notas locales: ${e.message}")
            return@withContext emptyList()
        }
    }
    
    /**
     * Aplica las notas fusionadas de vuelta a la base de datos local
     */
    suspend fun applyMergedNotes(mergedNotes: List<DatabaseNote>, userId: String) = withContext(Dispatchers.IO) {
        try {
            println("DatabaseSyncService: Aplicando ${mergedNotes.size} notas fusionadas a la DB local")
            
            mergedNotes.forEach { note ->
                if (note.eliminado) {
                    // Nota eliminada - borrar de la DB local
                    println("  - Eliminando nota: ${note.id}")
                    databaseManager.notesDao.deleteNote(note.id)
                } else {
                    // Verificar si la nota ya existe
                    val existingNote = databaseManager.notesDao.getNoteById(note.id)
                    
                    if (existingNote != null) {
                        // Actualizar nota existente
                        println("  - Actualizando nota: ${note.id}")
                        databaseManager.notesDao.updateNote(
                            noteId = note.id,
                            titulo = note.titulo,
                            contenido = note.contenido,
                            fechaModificacion = formatTimestamp(note.fechaModificacion)
                        )
                    } else {
                        // Insertar nota nueva
                        println("  - Insertando nota nueva: ${note.id}")
                        databaseManager.notesDao.insertNote(
                            id = note.id,
                            titulo = note.titulo,
                            contenido = note.contenido,
                            fechaCreacion = formatTimestamp(note.fechaModificacion), // Usar fecha modificación como creación
                            fechaModificacion = formatTimestamp(note.fechaModificacion),
                            usuarioId = userId,
                            syncStatus = "SYNCED", // Marcar como sincronizada
                            needsUpload = 0 // No necesita subir
                        )
                    }
                    
                    // Marcar como sincronizada
                    databaseManager.notesDao.markAsSynced(note.id)
                }
            }
            
            println("DatabaseSyncService: Notas fusionadas aplicadas exitosamente")
            
        } catch (e: Exception) {
            println("DatabaseSyncService: Error aplicando notas fusionadas: ${e.message}")
            throw e
        }
    }
    
    /**
     * Convierte timestamp de string a Long (milisegundos)
     */
    private fun parseTimestamp(timestampString: String): Long {
        return try {
            // Asumir que el timestamp está en formato de milisegundos como string
            timestampString.toLongOrNull() ?: getCurrentTimestamp()
        } catch (e: Exception) {
            println("DatabaseSyncService: Error parseando timestamp '$timestampString', usando actual")
            getCurrentTimestamp()
        }
    }
    
    /**
     * Convierte timestamp de Long a String
     */
    private fun formatTimestamp(timestamp: Long): String {
        return timestamp.toString()
    }
    
    /**
     * Obtiene el timestamp de la última sincronización exitosa
     * TODO: Implementar usando tabla MetadatosSincronizacion
     */
    suspend fun getLastSyncTimestamp(): Long {
        // TODO: Consultar tabla MetadatosSincronizacion
        return 0L
    }
    
    /**
     * Actualiza el timestamp de la última sincronización exitosa
     * TODO: Implementar usando tabla MetadatosSincronizacion
     */
    suspend fun updateLastSyncTimestamp(timestamp: Long) {
        // TODO: Actualizar tabla MetadatosSincronizacion
        println("DatabaseSyncService: TODO - Actualizar timestamp de última sincronización: $timestamp")
    }
}