package com.vicherarr.memora.data.api

import com.vicherarr.memora.data.api.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

/**
 * Servicio API principal para comunicarse con el backend de Memora
 * Encapsula todas las llamadas HTTP y manejo de errores
 */
class MemoraApiService(
    private val httpClient: HttpClient
) {
    
    // ========== AUTENTICACIÓN ==========
    
    suspend fun login(email: String, password: String): LoginResponseDto {
        return httpClient.post("autenticacion/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(email, password))
        }.body()
    }
    
    suspend fun register(fullName: String, email: String, password: String): LoginResponseDto {
        return httpClient.post("autenticacion/registrar") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequestDto(fullName, email, password))
        }.body()
    }
    
    // ========== NOTAS ==========
    
    suspend fun getNotes(page: Int = 0, pageSize: Int = 20): PaginatedNotesResponseDto {
        return httpClient.get("notas") {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
    }
    
    suspend fun getNoteById(noteId: String): NotaDto {
        return httpClient.get("notas/$noteId").body()
    }
    
    suspend fun createNote(title: String?, content: String): NotaDto {
        return httpClient.post("notas") {
            contentType(ContentType.Application.Json)
            setBody(CreateNotaDto(title, content))
        }.body()
    }
    
    suspend fun updateNote(noteId: String, title: String?, content: String): NotaDto {
        return httpClient.put("notas/$noteId") {
            contentType(ContentType.Application.Json)
            setBody(UpdateNotaDto(title, content))
        }.body()
    }
    
    suspend fun deleteNote(noteId: String) {
        httpClient.delete("notas/$noteId")
    }
    
    suspend fun searchNotes(query: String, page: Int = 0, pageSize: Int = 20): PaginatedNotesResponseDto {
        return httpClient.get("notas/search") {
            parameter("q", query)
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
    }
    
    // ========== ARCHIVOS ==========
    
    suspend fun uploadFile(noteId: String, fileName: String, fileData: ByteArray, mimeType: String): ArchivoAdjuntoDto {
        return httpClient.submitFormWithBinaryData(
            url = "notas/$noteId/archivos",
            formData = formData {
                append("file", fileData, Headers.build {
                    append(HttpHeaders.ContentType, mimeType)
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                })
            }
        ).body()
    }
    
    suspend fun downloadFile(fileId: String): ByteArray {
        return httpClient.get("archivos/$fileId").body()
    }
    
    suspend fun deleteFile(fileId: String) {
        httpClient.delete("archivos/$fileId")
    }
}