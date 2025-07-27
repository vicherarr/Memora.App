package com.vicherarr.memora.data.repository

import com.vicherarr.memora.domain.models.User
import com.vicherarr.memora.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant
import kotlin.random.Random

/**
 * Implementación mock del repositorio de autenticación
 * Para testing y desarrollo sin backend
 */
class AuthRepositoryMock : AuthRepository {
    
    // Usuario actual autenticado
    private val _currentUser = MutableStateFlow<User?>(null)
    
    // Token JWT simulado
    private var authToken: String? = null
    
    // Base de datos de usuarios simulada
    private val mockUsers = mutableMapOf<String, MockUserData>()
    
    init {
        // Agregar algunos usuarios de prueba
        mockUsers["test@example.com"] = MockUserData(
            id = "user-1",
            username = "Usuario Test",
            email = "test@example.com",
            password = "123456"
        )
        mockUsers["admin@memora.com"] = MockUserData(
            id = "user-2", 
            username = "Admin Memora",
            email = "admin@memora.com",
            password = "admin123"
        )
    }
    
    override suspend fun login(email: String, password: String): User {
        // Simular delay de red
        delay(1500)
        
        // Validar credenciales
        val mockUser = mockUsers[email]
        if (mockUser == null || mockUser.password != password) {
            throw Exception("Credenciales inválidas. Email o contraseña incorrectos.")
        }
        
        // Crear usuario y simular token
        val user = User(
            id = mockUser.id,
            username = mockUser.username,
            email = mockUser.email,
            createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        )
        
        // Simular JWT token
        authToken = generateMockToken(user.id)
        
        // Actualizar usuario actual
        _currentUser.value = user
        
        return user
    }
    
    override suspend fun register(username: String, email: String, password: String): User {
        // Simular delay de red
        delay(2000)
        
        // Validar si el email ya existe
        if (mockUsers.containsKey(email)) {
            throw Exception("El email ya está registrado. Por favor usa otro email.")
        }
        
        // Validaciones básicas
        if (email.isBlank() || !email.contains("@")) {
            throw Exception("Email inválido")
        }
        
        if (password.length < 6) {
            throw Exception("La contraseña debe tener al menos 6 caracteres")
        }
        
        if (username.length < 3) {
            throw Exception("El nombre de usuario debe tener al menos 3 caracteres")
        }
        
        // Crear nuevo usuario
        val userId = "user-${Random.nextInt(1000, 9999)}"
        val user = User(
            id = userId,
            username = username,
            email = email,
            createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        )
        
        // Guardar en base de datos mock
        mockUsers[email] = MockUserData(
            id = userId,
            username = username,
            email = email,
            password = password
        )
        
        // Simular JWT token
        authToken = generateMockToken(userId)
        
        // Actualizar usuario actual
        _currentUser.value = user
        
        return user
    }
    
    override suspend fun logout() {
        // Simular delay de red
        delay(500)
        
        // Limpiar token y usuario actual
        authToken = null
        _currentUser.value = null
    }
    
    override fun getCurrentUser(): Flow<User?> {
        return _currentUser.asStateFlow()
    }
    
    override suspend fun isUserAuthenticated(): Boolean {
        return authToken != null && _currentUser.value != null
    }
    
    override suspend fun getAuthToken(): String? {
        return authToken
    }
    
    override suspend fun saveAuthToken(token: String) {
        authToken = token
    }
    
    override suspend fun clearAuthToken() {
        authToken = null
        _currentUser.value = null
    }
    
    // Funciones auxiliares
    
    private fun generateMockToken(userId: String): String {
        val timestamp = System.currentTimeMillis()
        return "mock_jwt_token_${userId}_$timestamp"
    }
    
    /**
     * Función para agregar usuarios de prueba dinámicamente
     */
    fun addMockUser(email: String, password: String, username: String) {
        val userId = "user-${Random.nextInt(1000, 9999)}"
        mockUsers[email] = MockUserData(userId, username, email, password)
    }
    
    /**
     * Función para obtener la lista de usuarios registrados (solo para debug)
     */
    fun getMockUsers(): Map<String, String> {
        return mockUsers.mapValues { "${it.value.username} (${it.value.email})" }
    }
}

/**
 * Datos de usuario para el mock
 */
private data class MockUserData(
    val id: String,
    val username: String,
    val email: String,
    val password: String
)