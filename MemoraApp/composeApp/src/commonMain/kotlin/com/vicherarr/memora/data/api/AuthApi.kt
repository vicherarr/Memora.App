package com.vicherarr.memora.data.api

import com.vicherarr.memora.data.dto.LoginResponseDto
import com.vicherarr.memora.data.dto.LoginUserDto
import com.vicherarr.memora.data.dto.RegisterResponseDto
import com.vicherarr.memora.data.dto.RegisterUserDto
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST

interface AuthApi {
    
    @POST("autenticacion/login")
    suspend fun login(@Body request: LoginUserDto): LoginResponseDto
    
    @POST("autenticacion/registrar")
    suspend fun register(@Body request: RegisterUserDto): RegisterResponseDto
}



