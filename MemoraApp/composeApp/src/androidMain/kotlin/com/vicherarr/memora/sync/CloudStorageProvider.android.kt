package com.vicherarr.memora.sync

import android.content.Context
import android.util.Log
import android.accounts.Account
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import com.vicherarr.memora.data.auth.CloudAuthProvider
import com.vicherarr.memora.domain.model.AuthState

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
 * Implementación real para Google Drive AppDataFolder
 * Integrado con CloudAuthProvider para reutilizar autenticación
 */
class GoogleDriveStorageProvider(
    private val context: Context,
    private val cloudAuthProvider: CloudAuthProvider
) : CloudStorageProvider {
    
    companion object {
        private const val TAG = "GoogleDriveStorage"
        private const val DB_FILE_NAME = "memora_sync.db"
        private const val MEMORA_FOLDER_NAME = "Memora"
    }
    
    private var driveService: Drive? = null
    private var memoraFolderId: String? = null
    
    override suspend fun autenticar() {
        try {
            Log.d(TAG, "Iniciando autenticación para Google Drive...")
            
            // Verificar si CloudAuthProvider ya está autenticado
            val authState = cloudAuthProvider.authState.value
            Log.d(TAG, "Estado actual de CloudAuthProvider: $authState")
            
            if (authState !is AuthState.Authenticated) {
                Log.d(TAG, "CloudAuthProvider no autenticado. Intentando autenticación...")
                // Intentar autenticar usando CloudAuthProvider
                val result = cloudAuthProvider.signIn()
                if (result.isFailure) {
                    throw Exception("Error autenticando con CloudAuthProvider: ${result.exceptionOrNull()?.message}")
                }
                Log.d(TAG, "CloudAuthProvider autenticado exitosamente")
            }
            
            // Obtener cuenta desde Google Sign-In (ya autenticada por CloudAuthProvider)
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                throw Exception("No se pudo obtener cuenta de Google después de la autenticación")
            }
            
            Log.d(TAG, "Cuenta de Google encontrada: ${account.email}")
            
            // Crear el servicio Drive
            createDriveService(account)
            Log.d(TAG, "Servicio Google Drive inicializado exitosamente")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error configurando Google Drive: ${e.message}", e)
            throw Exception("Error configurando Google Drive: ${e.message}")
        }
    }
    
    private fun createDriveService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE_APPDATA))
        credential.selectedAccount = account.account
        
        driveService = Drive.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Memora")
            .build()
    }
    
    /**
     * Configuración para usar AppDataFolder correctamente
     * AppDataFolder es un espacio privado para la aplicación, no visible para el usuario
     */
    private fun logAppDataInfo() {
        Log.d(TAG, "Usando Google Drive AppDataFolder (espacio privado de la app)")
        Log.d(TAG, "Scope: DRIVE_APPDATA, Espacio: appDataFolder")
    }
    
    override suspend fun descargarDB(): ByteArray? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Descargando DB desde Google Drive AppDataFolder...")
            logAppDataInfo()
            
            val service = driveService ?: throw Exception("Google Drive service no inicializado")
            
            // SOLUCIÓN PROFESIONAL: Usar spaces='appDataFolder' para búsquedas en AppData
            val fileList = service.files().list()
                .setSpaces("appDataFolder")
                .setQ("name='$DB_FILE_NAME' and trashed=false")
                .setFields("files(id, name, size)")
                .execute()
            
            val files = fileList.files
            if (files.isNullOrEmpty()) {
                Log.d(TAG, "No se encontró archivo de DB remota")
                return@withContext null
            }
            
            val file = files[0]
            Log.d(TAG, "Descargando archivo: ${file.name} (ID: ${file.id})")
            
            // Descargar contenido del archivo
            val outputStream = ByteArrayOutputStream()
            service.files().get(file.id).executeMediaAndDownloadTo(outputStream)
            
            val data = outputStream.toByteArray()
            Log.d(TAG, "Archivo descargado exitosamente (${data.size} bytes)")
            
            data
            
        } catch (e: Exception) {
            Log.e(TAG, "Error descargando DB: ${e.message}", e)
            throw Exception("Error descargando base de datos: ${e.message}")
        }
    }
    
    override suspend fun subirDB(data: ByteArray): Unit = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Subiendo DB a Google Drive AppDataFolder (${data.size} bytes)...")
            logAppDataInfo()
            
            val service = driveService ?: throw Exception("Google Drive service no inicializado")
            
            // SOLUCIÓN PROFESIONAL: Usar spaces='appDataFolder' para búsquedas en AppData
            val fileList = service.files().list()
                .setSpaces("appDataFolder")
                .setQ("name='$DB_FILE_NAME' and trashed=false")
                .setFields("files(id, name)")
                .execute()
            
            val files = fileList.files
            val fileContent = ByteArrayContent("application/octet-stream", data)
            
            if (files.isNullOrEmpty()) {
                // PRIMERA VEZ: Crear archivo nuevo en AppDataFolder
                Log.d(TAG, "Primera vez - Creando archivo '$DB_FILE_NAME' en AppDataFolder...")
                
                val fileMetadata = File()
                    .setName(DB_FILE_NAME)
                    .setParents(listOf("appDataFolder"))
                
                val file = service.files().create(fileMetadata, fileContent)
                    .setFields("id, name")
                    .execute()
                
                Log.d(TAG, "Archivo creado exitosamente: ${file.name} (ID: ${file.id})")
                
            } else {
                // ACTUALIZAR: Archivo ya existe
                val existingFileId = files[0].id
                Log.d(TAG, "Actualizando archivo existente: $existingFileId...")
                
                val updatedFile = service.files().update(existingFileId, null, fileContent)
                    .setFields("id, name, modifiedTime")
                    .execute()
                
                Log.d(TAG, "Archivo actualizado exitosamente: ${updatedFile.name}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error subiendo DB: ${e.message}", e)
            throw Exception("Error subiendo base de datos: ${e.message}")
        }
    }
    
    override suspend fun obtenerMetadatosRemotos(): Long? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Obteniendo metadatos del archivo remoto desde AppDataFolder...")
            logAppDataInfo()
            
            val service = driveService ?: throw Exception("Google Drive service no inicializado")
            
            // SOLUCIÓN PROFESIONAL: Usar spaces='appDataFolder' para búsquedas en AppData
            val fileList = service.files().list()
                .setSpaces("appDataFolder")
                .setQ("name='$DB_FILE_NAME' and trashed=false")
                .setFields("files(id, name, modifiedTime)")
                .execute()
            
            val files = fileList.files
            if (files.isNullOrEmpty()) {
                Log.d(TAG, "No se encontró archivo remoto")
                return@withContext null
            }
            
            val file = files[0]
            val timestamp = file.modifiedTime?.value
            Log.d(TAG, "Archivo remoto encontrado: ${file.name}, timestamp: $timestamp")
            
            timestamp
            
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo metadatos remotos: ${e.message}", e)
            throw Exception("Error obteniendo metadatos remotos: ${e.message}")
        }
    }
}

/**
 * Factory function para Android
 */
actual fun getCloudStorageProvider(): CloudStorageProvider {
    // Se manejará con DI - esto es solo para compatibilidad
    throw NotImplementedError("Use DI to inject GoogleDriveStorageProvider instead")
}

/**
 * Factory function con dependencias para Android
 */
fun getCloudStorageProvider(context: Context, cloudAuthProvider: CloudAuthProvider): CloudStorageProvider {
    return GoogleDriveStorageProvider(context, cloudAuthProvider)
}