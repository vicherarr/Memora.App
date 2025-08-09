package com.vicherarr.memora.sync

/**
 * Implementación de CloudStorageProvider para iOS usando iCloud Drive
 */
actual interface CloudStorageProvider {
    actual suspend fun autenticar()
    actual suspend fun descargarDB(): ByteArray?
    actual suspend fun subirDB(data: ByteArray)
    actual suspend fun obtenerMetadatosRemotos(): Long?
}

/**
 * Implementación temporal básica para iCloud Drive
 */
class iCloudStorageProvider : CloudStorageProvider {
    override suspend fun autenticar() {
        // TODO: Implementar autenticación con iCloud
    }
    
    override suspend fun descargarDB(): ByteArray? {
        // TODO: Implementar descarga desde iCloud Drive
        return null
    }
    
    override suspend fun subirDB(data: ByteArray) {
        // TODO: Implementar subida a iCloud Drive
    }
    
    override suspend fun obtenerMetadatosRemotos(): Long? {
        // TODO: Implementar obtención de metadatos
        return null
    }
}

/**
 * Factory function para iOS
 */
actual fun getCloudStorageProvider(): CloudStorageProvider {
    return iCloudStorageProvider()
}