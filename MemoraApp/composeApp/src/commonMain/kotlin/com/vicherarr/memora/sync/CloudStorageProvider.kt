package com.vicherarr.memora.sync

/**
 * Interfaz que define el contrato para proveedores de almacenamiento en la nube.
 * Implementaciones específicas:
 * - Android: Google Drive AppDataFolder
 * - iOS: iCloud Drive Ubiquity Container
 */
expect interface CloudStorageProvider {
    /**
     * Autentica al usuario en el servicio de almacenamiento correspondiente
     */
    suspend fun autenticar()
    
    /**
     * Descarga el archivo de base de datos desde la nube
     * @return Los bytes del archivo o null si no existe
     */
    suspend fun descargarDB(): ByteArray?
    
    /**
     * Sube el archivo de base de datos a la nube
     * @param data Los bytes del archivo a subir
     */
    suspend fun subirDB(data: ByteArray)
    
    /**
     * Obtiene el timestamp de la última modificación del archivo remoto
     * @return El timestamp UTC en milisegundos o null si no existe el archivo
     */
    suspend fun obtenerMetadatosRemotos(): Long?
    
    /**
     * TESTING: Force delete remote database for fresh start
     * TODO: Remove this method after testing
     */
    suspend fun forceDeleteRemoteDatabase(): Result<Boolean>
    
    /**
     * TESTING: Force delete ALL remote files for nuclear reset
     * TODO: Remove this method after testing
     */
    suspend fun forceDeleteAllRemoteFiles(): Result<Boolean>
    
    // ========== NUEVOS MÉTODOS: METADATA MANAGEMENT ==========
    
    /**
     * Guarda metadatos de sincronización en el almacenamiento remoto
     * @param userId ID del usuario
     * @param metadataContent Contenido JSON de los metadatos
     * @return Result con el ID del archivo remoto creado
     */
    suspend fun saveMetadata(userId: String, metadataContent: String): Result<String>
    
    /**
     * Carga metadatos de sincronización desde el almacenamiento remoto
     * @param userId ID del usuario
     * @return Result con el contenido JSON o null si no existe
     */
    suspend fun loadMetadata(userId: String): Result<String?>
    
    /**
     * Elimina metadatos de sincronización del almacenamiento remoto
     * @param userId ID del usuario
     * @return Result indicando si la eliminación fue exitosa
     */
    suspend fun deleteMetadata(userId: String): Result<Boolean>
    
    /**
     * Verifica si existen metadatos de sincronización para un usuario
     * @param userId ID del usuario
     * @return Result indicando si los metadatos existen
     */
    suspend fun metadataExists(userId: String): Result<Boolean>
}

/**
 * Factory function para obtener la implementación específica de la plataforma
 */
expect fun getCloudStorageProvider(): CloudStorageProvider