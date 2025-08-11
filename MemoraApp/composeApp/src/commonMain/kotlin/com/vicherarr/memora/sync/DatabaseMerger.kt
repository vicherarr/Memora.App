package com.vicherarr.memora.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.vicherarr.memora.data.database.getCurrentTimestamp

/**
 * Estrategias de resolución de conflictos durante la sincronización
 */
enum class ConflictResolutionStrategy {
    KEEP_LOCAL,      // Priorizar cambios locales
    KEEP_REMOTE,     // Priorizar cambios remotos  
    KEEP_NEWER,      // Priorizar el más reciente por timestamp
    MERGE_SMART      // Fusión inteligente (más sofisticada)
}

/**
 * Representa una nota en el contexto de sincronización
 */
data class DatabaseNote(
    val id: String,
    val titulo: String?,
    val contenido: String,
    val fechaModificacion: Long, // Unix timestamp UTC
    val eliminado: Boolean = false
)

/**
 * Representa un conflicto entre una nota local y remota
 */
data class NoteConflict(
    val noteId: String,
    val localNote: DatabaseNote?,
    val remoteNote: DatabaseNote?,
    val conflictType: ConflictType
)

enum class ConflictType {
    BOTH_MODIFIED,     // Ambas versiones modificadas
    LOCAL_DELETED,     // Local eliminada, remota modificada
    REMOTE_DELETED,    // Remota eliminada, local modificada
    BOTH_CREATED       // Mismo ID creado en ambos lados
}

/**
 * Resultado de la operación de fusión
 */
data class MergeResult(
    val notasInsertadas: Int = 0,
    val notasActualizadas: Int = 0,
    val notasEliminadas: Int = 0,
    val conflictosResueltos: Int = 0,
    val mergedNotes: List<DatabaseNote> = emptyList(),
    val conflicts: List<NoteConflict> = emptyList(),
    val mergeStrategy: ConflictResolutionStrategy = ConflictResolutionStrategy.KEEP_NEWER
)

/**
 * Clase responsable de fusionar dos bases de datos SQLite:
 * - Base de datos local (actual del dispositivo)
 * - Base de datos remota (descargada de la nube)
 * 
 * Implementa múltiples estrategias de resolución de conflictos.
 */
class DatabaseMerger(
    private val defaultStrategy: ConflictResolutionStrategy = ConflictResolutionStrategy.KEEP_NEWER,
    private val deletionsDao: com.vicherarr.memora.data.database.DeletionsDao? = null
) {
    
    /**
     * Fusiona los cambios de la base de datos remota en la local
     * 
     * @param localDbPath Ruta a la base de datos local
     * @param remoteDbBytes Bytes de la base de datos remota
     * @param strategy Estrategia de resolución de conflictos
     * @return Resultado detallado de la operación de fusión
     */
    suspend fun fusionarBases(
        localDbPath: String,
        remoteDbBytes: ByteArray,
        strategy: ConflictResolutionStrategy = defaultStrategy
    ): MergeResult = withContext(Dispatchers.Default) {
        
        println("DatabaseMerger: Iniciando fusión con estrategia $strategy")
        
        try {
            // PASO 1: Deserializar base de datos remota
            val remoteNotes = deserializeRemoteDatabase(remoteDbBytes)
            println("DatabaseMerger: ${remoteNotes.size} notas extraídas de la DB remota")
            
            // PASO 2: Obtener notas locales (simuladas por ahora)
            val localNotes = getLocalNotes()
            println("DatabaseMerger: ${localNotes.size} notas locales encontradas")
            
            // PASO 3: Fusionar notas aplicando la estrategia
            val mergeResult = mergeNotes(localNotes, remoteNotes, strategy, null)
            
            // PASO 4: Aplicar cambios a la base de datos local (simulado)
            applyMergedNotesToLocalDatabase(mergeResult.mergedNotes)
            
            // PASO 5: Actualizar timestamp de última sincronización
            actualizarUltimaSincronizacion(getCurrentTimestamp())
            
            println("DatabaseMerger: Fusión completada exitosamente")
            return@withContext mergeResult
            
        } catch (e: Exception) {
            println("DatabaseMerger: Error durante la fusión: ${e.message}")
            throw e
        }
    }
    
    /**
     * Fusiona dos conjuntos de notas aplicando la estrategia de resolución de conflictos
     */
    suspend fun mergeNotes(
        localNotes: List<DatabaseNote>,
        remoteNotes: List<DatabaseNote>,
        strategy: ConflictResolutionStrategy,
        userId: String? = null
    ): MergeResult = withContext(Dispatchers.Default) {
        
        val localMap = localNotes.associateBy { it.id }
        val remoteMap = remoteNotes.associateBy { it.id }
        
        val allNoteIds = (localMap.keys + remoteMap.keys).distinct()
        
        val mergedNotes = mutableListOf<DatabaseNote>()
        val conflicts = mutableListOf<NoteConflict>()
        var notasInsertadas = 0
        var notasActualizadas = 0 
        var notasEliminadas = 0
        var conflictosResueltos = 0
        
        for (noteId in allNoteIds) {
            val localNote = localMap[noteId]
            val remoteNote = remoteMap[noteId]
            
            when {
                // CASO 1: Solo existe localmente
                localNote != null && remoteNote == null -> {
                    mergedNotes.add(localNote)
                    if (!localNote.eliminado) notasInsertadas++
                }
                
                // CASO 2: Solo existe remotamente  
                localNote == null && remoteNote != null -> {
                    // ✅ NUEVO: Verificar tombstones antes de agregar nota remota
                    val isDeletedLocally = if (deletionsDao != null && userId != null) {
                        deletionsDao.isRecordDeleted("notes", remoteNote.id, userId)
                    } else false
                    
                    if (isDeletedLocally) {
                        println("DatabaseMerger: 🪦 Nota ${remoteNote.id} ignorada - existe tombstone local")
                        // NO agregar la nota porque fue eliminada localmente
                    } else {
                        mergedNotes.add(remoteNote)
                        if (!remoteNote.eliminado) notasInsertadas++
                    }
                }
                
                // CASO 3: Existe en ambos lados - POSIBLE CONFLICTO
                localNote != null && remoteNote != null -> {
                    val conflict = detectConflict(localNote, remoteNote)
                    
                    if (conflict != null) {
                        conflicts.add(conflict)
                        conflictosResueltos++
                    }
                    
                    val resolvedNote = applyStrategy(localNote, remoteNote, strategy)
                    mergedNotes.add(resolvedNote)
                    
                    if (resolvedNote.eliminado) notasEliminadas++ 
                    else notasActualizadas++
                }
            }
        }
        
        return@withContext MergeResult(
            notasInsertadas = notasInsertadas,
            notasActualizadas = notasActualizadas,
            notasEliminadas = notasEliminadas,
            conflictosResueltos = conflictosResueltos,
            mergedNotes = mergedNotes,
            conflicts = conflicts,
            mergeStrategy = strategy
        )
    }
    
    /**
     * Detecta si hay conflicto entre dos notas
     */
    private fun detectConflict(localNote: DatabaseNote, remoteNote: DatabaseNote): NoteConflict? {
        val conflictType = when {
            localNote.eliminado && remoteNote.eliminado -> {
                return null // Ambas eliminadas - no es conflicto
            }
            localNote.eliminado && !remoteNote.eliminado -> {
                ConflictType.LOCAL_DELETED
            }
            !localNote.eliminado && remoteNote.eliminado -> {
                ConflictType.REMOTE_DELETED
            }
            localNote.fechaModificacion != remoteNote.fechaModificacion && 
            (localNote.contenido != remoteNote.contenido || localNote.titulo != remoteNote.titulo) -> {
                ConflictType.BOTH_MODIFIED
            }
            else -> {
                return null // No hay conflicto real
            }
        }
        
        return NoteConflict(
            noteId = localNote.id,
            localNote = localNote,
            remoteNote = remoteNote,
            conflictType = conflictType
        )
    }
    
    /**
     * Aplica la estrategia de resolución para decidir qué nota usar
     */
    private fun applyStrategy(
        localNote: DatabaseNote,
        remoteNote: DatabaseNote,
        strategy: ConflictResolutionStrategy
    ): DatabaseNote {
        
        return when (strategy) {
            ConflictResolutionStrategy.KEEP_LOCAL -> {
                println("DatabaseMerger: Conflicto ${localNote.id} - aplicando KEEP_LOCAL")
                localNote
            }
            
            ConflictResolutionStrategy.KEEP_REMOTE -> {
                println("DatabaseMerger: Conflicto ${localNote.id} - aplicando KEEP_REMOTE")
                remoteNote
            }
            
            ConflictResolutionStrategy.KEEP_NEWER -> {
                val winner = if (localNote.fechaModificacion >= remoteNote.fechaModificacion) {
                    localNote
                } else {
                    remoteNote
                }
                println("DatabaseMerger: Conflicto ${localNote.id} - aplicando KEEP_NEWER (winner: ${if (winner == localNote) "local" else "remote"})")
                winner
            }
            
            ConflictResolutionStrategy.MERGE_SMART -> {
                println("DatabaseMerger: Conflicto ${localNote.id} - aplicando MERGE_SMART")
                mergeNotesIntelligently(localNote, remoteNote)
            }
        }
    }
    
    /**
     * Fusión inteligente que combina lo mejor de ambas notas
     */
    private fun mergeNotesIntelligently(
        localNote: DatabaseNote,
        remoteNote: DatabaseNote
    ): DatabaseNote {
        
        // Si una está eliminada y otra no, priorizar la no eliminada a menos que sea muy antigua
        if (localNote.eliminado != remoteNote.eliminado) {
            val timeDifference = kotlin.math.abs(localNote.fechaModificacion - remoteNote.fechaModificacion)
            val oneHourInMillis = 60 * 60 * 1000L
            
            // Si la diferencia es menor a 1 hora, preferir la no eliminada
            if (timeDifference < oneHourInMillis) {
                return if (!localNote.eliminado) localNote else remoteNote
            }
        }
        
        // Por defecto, usar la más reciente
        return if (localNote.fechaModificacion >= remoteNote.fechaModificacion) {
            localNote
        } else {
            remoteNote
        }
    }
    
    /**
     * Deserializa la base de datos remota desde ByteArray
     * TODO: Implementar deserialización real desde SQLite
     */
    private fun deserializeRemoteDatabase(remoteDbBytes: ByteArray): List<DatabaseNote> {
        // Por ahora, simular notas remotas
        val mockRemoteNotes = listOf(
            DatabaseNote(
                id = "remote-note-1",
                titulo = "Nota Remota 1",
                contenido = "Contenido desde la nube",
                fechaModificacion = getCurrentTimestamp() - 1000,
                eliminado = false
            ),
            DatabaseNote(
                id = "remote-note-2",
                titulo = "Nota Remota 2",
                contenido = "Otra nota desde la nube",
                fechaModificacion = getCurrentTimestamp() - 2000,
                eliminado = false
            )
        )
        
        println("DatabaseMerger: Simulando ${mockRemoteNotes.size} notas remotas deserializadas")
        return mockRemoteNotes
    }
    
    /**
     * Obtiene las notas de la base de datos local
     * TODO: Conectar con SQLDelight
     */
    private fun getLocalNotes(): List<DatabaseNote> {
        // Por ahora, simular notas locales
        val mockLocalNotes = listOf(
            DatabaseNote(
                id = "local-note-1",
                titulo = "Nota Local 1",
                contenido = "Contenido local",
                fechaModificacion = getCurrentTimestamp(),
                eliminado = false
            ),
            DatabaseNote(
                id = "conflict-note",
                titulo = "Nota con Conflicto Local",
                contenido = "Versión local del conflicto",
                fechaModificacion = getCurrentTimestamp() - 500,
                eliminado = false
            )
        )
        
        println("DatabaseMerger: Simulando ${mockLocalNotes.size} notas locales")
        return mockLocalNotes
    }
    
    /**
     * Aplica las notas fusionadas a la base de datos local
     * TODO: Conectar con SQLDelight para insertar/actualizar realmente
     */
    private fun applyMergedNotesToLocalDatabase(mergedNotes: List<DatabaseNote>) {
        println("DatabaseMerger: Aplicando ${mergedNotes.size} notas fusionadas a la DB local")
        // TODO: Usar SQLDelight para insertar/actualizar notas
        mergedNotes.forEach { note ->
            println("  - ${note.id}: ${note.titulo} (modificado: ${note.fechaModificacion})")
        }
    }
    
    /**
     * Obtiene el timestamp de la última sincronización exitosa
     */
    private fun obtenerUltimaSincronizacion(): Long {
        // TODO: Consultar tabla MetadatosSincronizacion
        return 0L
    }
    
    /**
     * Actualiza el timestamp de la última sincronización
     */
    private fun actualizarUltimaSincronizacion(timestamp: Long) {
        // TODO: Actualizar tabla MetadatosSincronizacion
        println("DatabaseMerger: Actualizando timestamp de última sincronización: $timestamp")
    }
}