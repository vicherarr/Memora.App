# 👤 ProfileScreen - Plan de Diseño

## 🎯 Objetivo
Crear un ProfileScreen profesional y completo que incluya información del usuario, estadísticas, gestión de cuenta y configuraciones esenciales.

## 📱 Diseño Visual Propuesto

### **🏗️ Arquitectura de la pantalla**
```
┌─────────────────────────────────┐
│     Header con info usuario     │  ← Avatar Google + Nombre + Email
├─────────────────────────────────┤
│      📊 Estadísticas Cards      │  ← Tarjetas con métricas
├─────────────────────────────────┤
│      🌥️ Cloud Sync Status      │  ← Estado actual (ya implementado)
├─────────────────────────────────┤
│       ⚙️ Configuraciones       │  ← Versión, términos, etc.
├─────────────────────────────────┤
│      🚪 Gestión de Cuenta       │  ← Logout
└─────────────────────────────────┘
```

## 🔧 Componentes a Implementar

### **1. 👤 Header de Usuario**
**Diseño**: Card elevado con gradiente sutil
```kotlin
UserProfileHeader(
    avatarUrl: String?,           // Foto de Google o fallback
    displayName: String,          // Nombre del usuario
    email: String,               // Email del usuario
    memberSince: LocalDate       // Fecha de registro
)
```

**Fuente de datos**: 
- 🤔 **PREGUNTA**: ¿La información del usuario (nombre, email) viene del JWT token o necesitas que cree un endpoint específico en el API?

### **2. 📊 Estadísticas**
**Diseño**: Grid de tarjetas con iconos y números prominentes
```kotlin
StatisticsSection(
    totalNotes: Int,             // Total de notas creadas
    totalAttachments: Int,       // Total de archivos adjuntos
    totalStorageUsed: String,    // Tamaño total usado (MB/GB)
    notesThisMonth: Int,         // Notas creadas este mes
    lastSyncDate: LocalDateTime? // Última sincronización
)
```

**🤔 PREGUNTAS sobre estadísticas**:
- ¿Qué estadísticas específicas quieres mostrar?
- ¿Prefieres métricas simples (total notas) o más avanzadas (notas por mes, tendencias)?
- ¿Quieres mostrar uso de almacenamiento?

### **3. ⚙️ Configuraciones y Info**
**Diseño**: Lista de opciones con iconos Material
```kotlin
SettingsSection(
    appVersion: String,          // Versión actual de la app
    buildNumber: String,         // Build number
    termsUrl: String?,           // URL términos y condiciones
    privacyUrl: String?,         // URL política de privacidad
    supportEmail: String?        // Email de soporte
)
```

**🤔 PREGUNTAS sobre configuraciones**:
- ¿Quieres enlaces a términos y privacidad reales o placeholders por ahora?
- ¿Cómo prefieres manejar el soporte? ¿Email, formulario web, o solo información?

### **4. 🚪 Gestión de Cuenta**
**Diseño**: Sección separada con botón prominente
```kotlin
AccountManagementSection(
    onLogout: () -> Unit,
    platform: Platform           // Android (Google) / iOS (Mock)
)
```

## 🏗️ Arquitectura Técnica

### **ViewModels Necesarios**
```kotlin
// Nuevo ViewModel para el perfil
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val notesRepository: NotesRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    data class ProfileUiState(
        val user: User? = null,
        val statistics: UserStatistics? = null,
        val appInfo: AppInfo = AppInfo(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    ) : BaseUiState
}

// Modelos de datos
data class User(
    val id: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String?,
    val memberSince: LocalDate
)

data class UserStatistics(
    val totalNotes: Int,
    val totalAttachments: Int,
    val totalStorageBytes: Long,
    val notesThisMonth: Int,
    val lastSyncDate: LocalDateTime?
)

data class AppInfo(
    val versionName: String = BuildConfiguration.versionName,
    val versionCode: Int = BuildConfiguration.versionCode,
    val buildType: String = BuildConfiguration.buildType
)
```

### **Repositorios Necesarios**
```kotlin
interface UserRepository {
    suspend fun getCurrentUser(): Result<User>
    suspend fun getUserStatistics(): Result<UserStatistics>
    suspend fun logout(): Result<Unit>
}

// Implementación con API real para Android, mock para iOS
expect class UserRepositoryImpl : UserRepository
```

## 🎨 Design System

### **Colores y Estilo**
- **Header**: Gradiente sutil con colores primarios
- **Statistics Cards**: Material Design 3 containers
- **Iconografía**: Material Icons consistente
- **Spacing**: Grid de 16dp estándar
- **Elevaciones**: Cards con 4dp, header con 8dp

### **Estados de UI**
- **Loading**: Skeletons en statistics cards
- **Error**: Snackbar para errores de red
- **Empty**: Fallbacks para datos no disponibles
- **Logout**: Diálogo de confirmación

## 📋 Plan de Implementación

### **Fase 1**: Estructura básica
1. Crear ProfileViewModel y estados
2. Implementar UserRepository (mock inicial)
3. Refactorizar ProfileScreen con nueva estructura

### **Fase 2**: Componentes visuales
1. UserProfileHeader component
2. StatisticsSection component  
3. SettingsSection component
4. AccountManagementSection component

### **Fase 3**: Integración de datos
1. Implementar UserRepository real (Android)
2. Mantener mock para iOS
3. Conectar con Google Auth para avatar
4. Calcular estadísticas reales

### **Fase 4**: Polish y UX
1. Animaciones y transiciones
2. Estados de loading y error
3. Refresh pull-to-refresh
4. Testing en ambas plataformas

## 🤔 Preguntas Pendientes

1. **Datos de usuario**: ¿JWT token o endpoint específico?
2. **Estadísticas**: ¿Qué métricas específicas mostrar?
3. **Enlaces legales**: ¿URLs reales o placeholders?
4. **Soporte**: ¿Email, web, o solo info?
5. **Avatar Google**: ¿Integración directa con Google Auth o endpoint del API?
6. **Refresh de datos**: ¿Pull-to-refresh o automático?

## 🚀 Resultado Esperado

ProfileScreen profesional con:
- ✅ **Información completa del usuario** con avatar de Google
- ✅ **Estadísticas visuales** atractivas y útiles  
- ✅ **Configuraciones esenciales** bien organizadas
- ✅ **Logout funcional** según plataforma
- ✅ **Diseño consistente** con Material Design 3
- ✅ **Arquitectura MVVM** limpia y mantenible