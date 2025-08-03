package com.vicherarr.memora.data.api

import com.vicherarr.memora.data.dto.CreateNotaDto
import com.vicherarr.memora.data.dto.NotaDto
import com.vicherarr.memora.data.dto.PaginatedNotasDto
import com.vicherarr.memora.data.dto.UpdateNotaDto
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface NotesApi {
    
    @GET("notas")
    suspend fun getNotes(
        @Query("pageNumber") pageNumber: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): PaginatedNotasDto
    
    @GET("notas/{id}")
    suspend fun getNoteById(@Path("id") id: String): NotaDto
    
    @POST("notas")
    suspend fun createNote(@Body request: CreateNotaDto): NotaDto
    
    @PUT("notas/{id}")
    suspend fun updateNote(
        @Path("id") id: String,
        @Body request: UpdateNotaDto
    ): NotaDto
    
    @DELETE("notas/{id}")
    suspend fun deleteNote(@Path("id") id: String)
}