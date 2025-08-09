package com.vicherarr.memora.sync

/**
 * Implementación de CloudStorageProvider para Android usando Google Drive
 */
actual interface CloudStorageProvider {
    actual suspend fun autenticar()
    actual suspend fun descargarDB(): ByteArray?
    actual suspend fun subirDB(data: ByteArray)
    actual suspend fun obtenerMetadatosRemotos(): Long?
}

/**
 * Implementación temporal básica para Google Drive
 */
class GoogleDriveStorageProvider : CloudStorageProvider {
    override suspend fun autenticar() {
        // TODO: Implementar autenticación con Google Drive
    }
    
    override suspend fun descargarDB(): ByteArray? {
        // TODO: Implementar descarga desde Google Drive
        return null
    }
    
    override suspend fun subirDB(data: ByteArray) {
        // TODO: Implementar subida a Google Drive
    }
    
    override suspend fun obtenerMetadatosRemotos(): Long? {
        // TODO: Implementar obtención de metadatos
        return null
    }
}

/**
 * Factory function para Android
 */
actual fun getCloudStorageProvider(): CloudStorageProvider {
    return GoogleDriveStorageProvider()
}