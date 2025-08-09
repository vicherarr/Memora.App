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
}

/**
 * Factory function para obtener la implementación específica de la plataforma
 */
expect fun getCloudStorageProvider(): CloudStorageProvider